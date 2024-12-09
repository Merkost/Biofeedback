package com.healbe.complex_example.enter

import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.*
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.healbe.complex_example.R
import com.healbe.complex_example.databinding.FragmentLoginBinding
import com.healbe.complex_example.tools.TextWatcherHelper.onTextChanged
import com.healbe.complex_example.tools.isValidEmailAddress
import com.healbe.healbesdk.business_api.HealbeSdk
import com.healbe.healbesdk.business_api.user.data.HealbeSessionState
import com.healbe.healbesdk.serverApi.exceptions.ResponseCode
import com.healbe.healbesdk.serverApi.exceptions.ResponseCode.LoginOrPasswordIsWrong
import com.healbe.healbesdk.serverApi.exceptions.ResponseCode.TooMuchWrongTries
import com.healbe.healbesdk.serverApi.exceptions.ServerWrongCodeException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class LoginFragment : Fragment() {
    private val destroy = CompositeDisposable()

    private var binding: FragmentLoginBinding? = null
    private var router: EnterRouter? = null
    private var textWatcher: TextWatcher? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        router = context as? EnterRouter
            ?: throw RuntimeException("$context must implement EnterRouter")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentLoginBinding.inflate(inflater)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            initialState()
            bind()

            // nice and short text watcher for validation login data and interact button visibility
            onTextChanged { binding?.validate() }.let {
                textWatcher = it
                email.addTextChangedListener(it)
                password.addTextChangedListener(it)
            }

            val headerTopMargin = header.marginTop
            val goSignUpBottomMargin = goSignUp.marginBottom
            ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).let { insets ->
                    header.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        topMargin = headerTopMargin + insets.top
                    }
                    goSignUp.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        bottomMargin = goSignUpBottomMargin + insets.bottom
                    }
                }

                WindowInsetsCompat.CONSUMED
            }
        }
    }

    private fun FragmentLoginBinding.initialState() {
        email.setText("")
        password.setText("")
        button.visibility = View.VISIBLE
        goSignUp.visibility = View.VISIBLE
        progress.visibility = View.INVISIBLE
        button.isEnabled = false
        mailLayout.isErrorEnabled = false
        passLayout.isErrorEnabled = false
    }

    private fun FragmentLoginBinding.bind() {
        button.setOnClickListener { binding?.onButtonClick() }
        goSignUp.setOnClickListener { onGoSignUpClick() }
    }

    private fun FragmentLoginBinding.validate() {
        val emailText = email.text?.toString()
        val passwordText = password.text?.toString()

        val emailOk = emailText.isNullOrBlank() || emailText.isValidEmailAddress()
        val buttonActive = emailOk && !emailText.isNullOrBlank() && !passwordText.isNullOrBlank()

        mailLayout.isErrorEnabled = !emailOk
        mailLayout.error = if (!emailOk) root.context.getString(R.string.login_error) else null
        button.isEnabled = buttonActive
        button.visibility = View.VISIBLE
        goSignUp.visibility = View.VISIBLE
        progress.visibility = View.INVISIBLE
    }

    // send login pair
    private fun FragmentLoginBinding.onButtonClick() {
        button.visibility = View.INVISIBLE
        goSignUp.visibility = View.INVISIBLE
        progress.visibility = View.VISIBLE

        val emailText = email.text?.toString() ?: ""
        val passwordText = password.text?.toString() ?: ""

        destroy.add(
            HealbeSdk.get().USER.login(emailText, passwordText)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { sessionState: HealbeSessionState ->
                        // see if login returns valid state
                        if (HealbeSessionState.isUserValid(sessionState)) {
                            router?.goConnect()
                        } else { // or invalid, not detached
                            onErrorLogin(null, sessionState)
                        }
                    },
                    { throwable ->
                        Timber.e(throwable)
                        onErrorLogin(throwable, HealbeSessionState.USER_NOT_AUTHORIZED)
                    }
                )
        )
    }

    private fun onErrorLogin(throwable: Throwable?, state: HealbeSessionState) {
        when (state) {
            HealbeSessionState.USER_NOT_AUTHORIZED -> {
                //back to initial fragment state
                binding?.initialState()
                //here we see how to use server response codes from consumed throwable
                val codes: List<ResponseCode> =
                    (throwable as? ServerWrongCodeException)?.codes ?: emptyList()

                binding?.root?.let {
                    if (codes.contains(TooMuchWrongTries)) { // if too much wrong tries
                        Snackbar.make(it, R.string.too_much, Snackbar.LENGTH_SHORT).show()
                    } else if (codes.contains(LoginOrPasswordIsWrong)) {
                        Snackbar.make(it, R.string.wrong_login, Snackbar.LENGTH_SHORT).show()
                    } else {
                        // if we don't know what's going wrong (just for example)
                        Snackbar.make(it, R.string.auth_error, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
            HealbeSessionState.NEED_TO_FILL_PERSONAL, HealbeSessionState.NEED_TO_FILL_PARAMS -> {
                router?.goFillData()
            }
            HealbeSessionState.VALID_NEW_USER, HealbeSessionState.VALID_OLD_USER -> {
                router?.goConnect()
            }
            HealbeSessionState.SESSION_NOT_PREPARED -> router?.goSplash()
        }
    }

    private fun onGoSignUpClick() {
        router?.goSignUp()
    }

    override fun onDestroyView() {
        binding?.apply {
            ViewCompat.setOnApplyWindowInsetsListener(root, null)

            textWatcher?.let {
                email.removeTextChangedListener(it)
                password.removeTextChangedListener(it)
                textWatcher = null
            }

            unbind()

            binding = null
        }
        super.onDestroyView()
    }

    private fun FragmentLoginBinding.unbind() {
        button.setOnClickListener(null)
        goSignUp.setOnClickListener(null)
    }

    override fun onDetach() {
        router = null
        super.onDetach()
    }

    override fun onDestroy() {
        destroy.clear()
        super.onDestroy()
    }

    companion object {
        fun newInstance(): LoginFragment = LoginFragment()
    }
}