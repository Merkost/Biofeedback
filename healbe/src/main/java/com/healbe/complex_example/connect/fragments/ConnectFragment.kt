package com.healbe.complex_example.connect.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import com.healbe.complex_example.R
import com.healbe.complex_example.connect.ConnectRouter
import com.healbe.complex_example.connect.ConnectionRoutedFragment
import com.healbe.complex_example.databinding.FragmentConnectBinding
import com.healbe.examples.common.Bluetooth
import com.healbe.healbesdk.business_api.HealbeSdk
import com.healbe.healbesdk.device_api.ClientState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class ConnectFragment : ConnectionRoutedFragment() {
    private val destroy = CompositeDisposable()

    private var binding: FragmentConnectBinding? = null
    private var name: String? = null

    // map connection states to UI states
    private val stateMap: Map<ClientState, () -> Unit> = buildMap {
        put(ClientState.CONNECTING) { statusConnecting(name) }
        put(ClientState.CONNECTED) { statusWaiting(name) }
        put(ClientState.REQUEST_FUNC_FW) { goError() }   // firmware installation has many states so
        put(ClientState.REQUEST_UPDATE_FW) { goError() } //   it is not shown in the example
        put(ClientState.REQUEST_NEW_PIN_CODE) { requestNewPin() }
        put(ClientState.REQUEST_PIN_CODE) { requestPin() }
        put(ClientState.READY) { router?.connected() }
    }

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
        FragmentConnectBinding.inflate(inflater)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            bind()

            val logoTopMargin = logo.marginTop
            val cancelBottomMargin = skipButton.marginBottom
            ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).let { insets ->
                    binding?.apply {
                        logo.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            topMargin = logoTopMargin + insets.top
                        }
                        skipButton.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                            bottomMargin = cancelBottomMargin + insets.bottom
                        }
                    }
                }

                WindowInsetsCompat.CONSUMED
            }
        }

        Timber.d("view created..")
        checkStateAndConnect(view.context)
    }

    private fun FragmentConnectBinding.bind() {
        skipButton.setOnClickListener { goSearch() }
    }

    override fun onDestroyView() {
        binding?.apply {
            ViewCompat.setOnApplyWindowInsetsListener(root, null)
            unbind()
            binding = null
        }
        super.onDestroyView()
    }

    private fun FragmentConnectBinding.unbind() {
        skipButton.setOnClickListener(null)
    }

    override fun onDestroy() {
        destroy.clear()
        super.onDestroy()
    }

    // check necessary bluetooth permission and bluetooth enabling state and connect if all sound
    private fun checkStateAndConnect(context: Context) {
        if (Bluetooth.areAllPermissionsGranted(context) && Bluetooth.isOn(context)) {
            connect()
        } else {
            router?.goState(ConnectRouter.State.PREPARING, true)
        }
    }

    private fun connect() {
        Timber.d("connect..")

        // observe connection states and process that important for us
        destroy.add(
            HealbeSdk.get().GOBE.observeConnectionState()
                .doOnNext { status: ClientState -> Timber.d("state: ${status.name}") }
                .filter { state -> stateMap.containsKey(state) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { state: ClientState -> stateMap[state]?.invoke() },
                    { t: Throwable -> Timber.e(t) }
                )
        )

        destroy.add(
            HealbeSdk.get().GOBE.get() // get devices saved during a scan
                .observeOn(AndroidSchedulers.mainThread())
                // prepare UI
                .doOnSuccess { device -> statusConnecting(device.name) }
                .observeOn(Schedulers.io())
                // and begin connection
                .flatMapCompletable { HealbeSdk.get().GOBE.connect() }
                .subscribe({}, { t: Throwable -> Timber.e(t) })
        )
    }

    private fun disconnectAnd(r: () -> Unit) {
        destroy.clear()
        destroy.add(
            HealbeSdk.get().GOBE.disconnect()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ r() }, { t: Throwable -> Timber.e(t) })
        )
    }

    private fun statusConnecting(name: String?) {
        this.name = name
        binding?.apply {
            status.text = root.context.getString(R.string.connecting_to, name)
        }
    }

    private fun statusWaiting(name: String?) {
        binding?.apply {
            status.text = root.context.getString(R.string.waiting, name)
        }
    }

    private fun requestPin() {
        router?.goState(ConnectRouter.State.ENTER_PIN, false)
    }

    private fun requestNewPin() {
        router?.goState(ConnectRouter.State.SETUP_PIN, false)
    }

    private fun goError() {
        disconnectAnd { router?.goState(ConnectRouter.State.ERROR, false) }
    }

    private fun goSearch() {
        disconnectAnd { router?.goState(ConnectRouter.State.SEARCH, true) }
    }

    companion object {
        fun newInstance(): ConnectFragment = ConnectFragment()
    }
}