package com.healbe.complex_example.connect.fragments

import android.content.Context
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.*
import com.healbe.complex_example.R
import com.healbe.complex_example.connect.ConnectRouter
import com.healbe.complex_example.connect.ConnectionRoutedFragment
import com.healbe.complex_example.databinding.FragmentEnterPinBinding
import com.healbe.complex_example.tools.TextWatcherHelper.onTextChanged
import com.healbe.healbesdk.business_api.HealbeSdk
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class EnterPinFragment : ConnectionRoutedFragment() {
    private val destroy = CompositeDisposable()

    private var binding: FragmentEnterPinBinding? = null
    private var textWatcher: TextWatcher? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = goSearch()
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentEnterPinBinding.inflate(inflater)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            initialState()
            bind()

            val titleTopMargin = title.marginTop
            val skipButtonBottomMargin = skipButton.marginBottom
            ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).let { insets ->
                    title.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        topMargin = titleTopMargin + insets.top
                    }
                    skipButton.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        bottomMargin = skipButtonBottomMargin + insets.bottom
                    }
                }

                WindowInsetsCompat.CONSUMED
            }
        }
    }

    private fun FragmentEnterPinBinding.bind() {
        textWatcher = onTextChanged { pin: String -> checkPin(pin) }
            .also { pinCode.addTextChangedListener(it) }
        skipButton.setOnClickListener { goSearch() }
    }

    private fun FragmentEnterPinBinding.initialState() {
        pinCodeTil.isErrorEnabled = false
        pinHelper.setText(R.string.pin_helper)
        progress.visibility = View.INVISIBLE
    }

    override fun onDestroyView() {
        binding?.apply {
            ViewCompat.setOnApplyWindowInsetsListener(root, null)
            unbind()
            binding = null
        }
        super.onDestroyView()
    }

    private fun FragmentEnterPinBinding.unbind() {
        textWatcher?.let {
            pinCode.removeTextChangedListener(it)
            textWatcher = null
        }

        skipButton.setOnClickListener(null)
    }

    override fun onDestroy() {
        destroy.clear()
        super.onDestroy()
    }

    private fun checkPin(pin: String) {
        if (pin.isNotBlank() && pin.length == 6 && destroy.size() == 0) {
            binding?.progress?.visibility = View.VISIBLE
            //set pin for connection and go connection screen
            destroy.add(
                HealbeSdk.get().GOBE.setPin(pin)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { goConnect() },
                        { t: Throwable ->
                            Timber.e(t)
                            context?.let {
                                Toast.makeText(
                                    it,
                                    "Something goes wrong, try to delete and retry pin-code",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            binding?.progress?.visibility = View.INVISIBLE
                        }
                    )
            )
        }
    }

    private fun goConnect() {
        router?.goState(ConnectRouter.State.CONNECT, true)
    }

    private fun disconnectAnd(r: () -> Unit) {
        destroy.add(
            HealbeSdk.get().GOBE.disconnect()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { r() },
                    { t: Throwable -> Timber.e(t) }
                )
        )
    }

    private fun goSearch() {
        disconnectAnd { router?.goState(ConnectRouter.State.SEARCH, true) }
    }

    companion object {
        fun newInstance(): EnterPinFragment = EnterPinFragment()
    }
}