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
import com.healbe.complex_example.databinding.FragmentSignUpBinding
import com.healbe.complex_example.tools.TextWatcherHelper.onTextChanged
import com.healbe.complex_example.tools.isValidEmailAddress
import com.healbe.healbesdk.business_api.HealbeSdk
import com.healbe.healbesdk.business_api.user.data.HealbeSessionState
import com.healbe.healbesdk.serverApi.exceptions.ResponseCode
import com.healbe.healbesdk.serverApi.exceptions.ResponseCode.TooMuchWrongTries
import com.healbe.healbesdk.serverApi.exceptions.ServerWrongCodeException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class SignUpFragment : Fragment() {
    private val destroy = CompositeDisposable()

    private var router: EnterRouter? = null
    private var binding: FragmentSignUpBinding? = null
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
        FragmentSignUpBinding.inflate(inflater)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            initialState()
            bind()

            val headerTopMargin = header.marginTop
            val goSignInBottomMargin = goSignIn.marginBottom
            ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
                binding?.apply {
                    windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).let { insets ->
                        header.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            topMargin = headerTopMargin + insets.top
                        }
                        goSignIn.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            bottomMargin = goSignInBottomMargin + insets.bottom
                        }
                    }
                }

                WindowInsetsCompat.CONSUMED
            }
        }
    }

    private fun FragmentSignUpBinding.initialState() {
        showSignUpProgress(false)
        button.isEnabled = false
        email.setText("")
        password.setText("")
        mailLayout.isErrorEnabled = false
        passLayout.isErrorEnabled = false
    }

    private fun FragmentSignUpBinding.bind() {
        button.setOnClickListener { onButtonClick() }
        goSignIn.setOnClickListener { onGoSignUpClick() }
        val textWatcher = onTextChanged { validate() }
            .also { textWatcher = it }
        email.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)
    }

    override fun onDestroyView() {
        binding?.apply {
            ViewCompat.setOnApplyWindowInsetsListener(root, null)
            unbind()
            binding = null
        }
        destroy.clear()
        super.onDestroyView()
    }

    private fun FragmentSignUpBinding.unbind() {
        button.setOnClickListener(null)
        goSignIn.setOnClickListener(null)
        textWatcher?.let {
            email.removeTextChangedListener(it)
            password.removeTextChangedListener(it)
            textWatcher = null
        }
    }

    override fun onDetach() {
        router = null
        super.onDetach()
    }

    private fun FragmentSignUpBinding.onButtonClick() {
        val email = email.text?.toString() ?: ""
        val password = password.text?.toString() ?: ""
        destroy.add(
            HealbeSdk.get().USER.register(email, password)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { binding?.showSignUpProgress(true) }
                .subscribe(
                    { sessionState: HealbeSessionState ->
                        parseState(sessionState, null)
                    },
                    { throwable: Throwable ->
                        parseState(HealbeSessionState.USER_NOT_AUTHORIZED, throwable)
                    }
                )
        )
    }

    private fun parseState(state: HealbeSessionState, throwable: Throwable?) {
        throwable?.let { Timber.e(it) }
        when (state) {
            HealbeSessionState.USER_NOT_AUTHORIZED -> {
                //here we see how to use server response codes from consumed throwable
                val codes: List<ResponseCode> =
                    (throwable as? ServerWrongCodeException)?.codes ?: emptyList()

                binding?.apply {
                    if (codes.contains(TooMuchWrongTries)) { // if too much wrong tries
                        Snackbar.make(root, R.string.too_much, Snackbar.LENGTH_SHORT).show()
                    } else if (codes.contains(ResponseCode.LoginOrPasswordIsWrong)) {
                        Snackbar.make(root, R.string.wrong_login, Snackbar.LENGTH_SHORT).show()
                    } else {
                        // if we don't know what's going wrong (just for example)
                        Snackbar.make(root, R.string.auth_error, Snackbar.LENGTH_SHORT).show()
                    }
                    showSignUpProgress(false)
                }
            }

            HealbeSessionState.NEED_TO_FILL_PERSONAL, HealbeSessionState.NEED_TO_FILL_PARAMS ->
                router?.goFillData()

            HealbeSessionState.VALID_NEW_USER, HealbeSessionState.VALID_OLD_USER ->
                router?.goConnect()

            HealbeSessionState.SESSION_NOT_PREPARED -> router?.goSplash()
        }
    }

    private fun onGoSignUpClick() {
        router?.goLogin()
    }

    private fun FragmentSignUpBinding.validate() {
        val email = email.text?.toString()
        val password = password.text?.toString()

        val emailOk = email.isNullOrBlank() || email.isValidEmailAddress()
        val passwordOk = password.isNullOrBlank() || password.length >= 4
        val buttonActive = emailOk && !email.isNullOrBlank() && passwordOk && !password.isNullOrBlank()

        mailLayout.isErrorEnabled = !emailOk
        mailLayout.error = if (!emailOk) root.context.getString(R.string.login_error) else null
        passLayout.isErrorEnabled = !passwordOk
        passLayout.error = if (!passwordOk) root.context.getString(R.string.password_error) else null
        button.isEnabled = buttonActive
        button.visibility = View.VISIBLE
        goSignIn.visibility = View.VISIBLE
        progress.visibility = View.INVISIBLE
    }

    private fun FragmentSignUpBinding.showSignUpProgress(inProgress: Boolean) {
        button.visibility = if (inProgress) View.INVISIBLE else View.VISIBLE
        goSignIn.isEnabled = !inProgress
        progress.visibility = if (inProgress) View.VISIBLE else View.INVISIBLE
    }

    companion object {
        fun newInstance(): SignUpFragment = SignUpFragment()
    }
}