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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.healbe.complex_example.R
import com.healbe.complex_example.connect.ConnectRouter
import com.healbe.complex_example.connect.ConnectionRoutedFragment
import com.healbe.complex_example.databinding.FragmentSearchBinding
import com.healbe.examples.common.Bluetooth
import com.healbe.healbesdk.business_api.HealbeSdk
import com.healbe.healbesdk.business_api.user_storage.entity.HealbeDevice
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SearchFragment : ConnectionRoutedFragment() {
    private val destroy = CompositeDisposable()

    private var binding: FragmentSearchBinding? = null
    private var adapter: SearchAdapter? = null
    private var searchSubscription: Disposable? = null // subscription for search

    override fun onAttach(context: Context) {
        super.onAttach(context)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                stopScan()
                router?.logout()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentSearchBinding.inflate(inflater)
            .also { binding = it }
            .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.apply {
            list.adapter = SearchAdapter { v: View ->
                val device = v.tag as? HealbeDevice ?: return@SearchAdapter
                val adapter = adapter ?: return@SearchAdapter
                setDevice(device, adapter.saved)
            }
                .also { adapter = it }
            val orientation = (list.layoutManager as? LinearLayoutManager)?.orientation
                ?: MaterialDividerItemDecoration.VERTICAL
            list.addItemDecoration(MaterialDividerItemDecoration(root.context, orientation))

            initialState()
            bind()

            val headerTopMargin = header.marginTop
            val logoutButtonBottomMargin = logoutButton.marginBottom
            ViewCompat.setOnApplyWindowInsetsListener(view) { _, windowInsets ->
                windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).let { insets ->
                    header.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        topMargin = headerTopMargin + insets.top
                    }
                    logoutButton.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        bottomMargin = logoutButtonBottomMargin + insets.bottom
                    }
                }

                WindowInsetsCompat.CONSUMED
            }
        }

        // check necessary permissions and bluetooth enabling state
        checkStateAndStartScan(view.context)
    }

    private fun FragmentSearchBinding.bind() {
        logoutButton.setOnClickListener {
            stopScan()
            router?.logout()
        }
        retryButton.setOnClickListener { startScan() }
    }

    override fun onDestroyView() {
        binding?.apply {
            ViewCompat.setOnApplyWindowInsetsListener(root, null)

            adapter?.let {
                list.adapter = null
                adapter = null
            }

            unbind()

            binding = null
        }
        super.onDestroyView()
    }

    private fun FragmentSearchBinding.unbind() {
        logoutButton.setOnClickListener(null)
        retryButton.setOnClickListener(null)
    }

    override fun onDestroy() {
        destroy.clear()
        super.onDestroy()
    }

    private fun setDevice(device: HealbeDevice, saved: HealbeDevice?) {
        Timber.d("setDevice %s:%s", device.name, device.mac)
        val deviceToSet = if (device == saved) {
            device.copy(mac = saved.mac, version = saved.version, pin = saved.pin, isActive = saved.isActive)
        } else {
            device
        }

        // set default device that will be used for connect and other operations
        destroy.add(
            HealbeSdk.get().GOBE.set(deviceToSet)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        Timber.e("ok")
                        stopScan()
                        router?.goState(ConnectRouter.State.CONNECT, false)
                    },
                    { t: Throwable -> Timber.e(t) }
                )
        )
    }

    // check necessary bluetooth permissions and bluetooth enabling state
    private fun checkStateAndStartScan(context: Context) {
        if (Bluetooth.areAllPermissionsGranted(context) && Bluetooth.isOn(context)) {
            startScan()
        } else {
            router?.goState(ConnectRouter.State.PREPARING, true)
        }
    }

    private fun FragmentSearchBinding.initialState() {
        placeholder.visibility = View.VISIBLE
        searching.setText(R.string.searching_devices_nearby)
        errorIcon.visibility = View.INVISIBLE
        progress.visibility = View.INVISIBLE
        list.visibility = View.INVISIBLE
        logoutButton.visibility = View.VISIBLE
        retryButton.visibility = View.INVISIBLE
        divider.visibility = View.INVISIBLE
    }

    private fun FragmentSearchBinding.scanStartState() {
        placeholder.visibility = View.VISIBLE
        searching.setText(R.string.searching_devices_nearby)
        errorIcon.visibility = View.INVISIBLE
        progress.visibility = View.VISIBLE
        list.visibility = View.INVISIBLE
        logoutButton.visibility = View.VISIBLE
        retryButton.visibility = View.INVISIBLE
        divider.visibility = View.INVISIBLE
    }

    private fun FragmentSearchBinding.scanFoundAndContinueScanState() {
        placeholder.visibility = View.INVISIBLE
        searching.setText(R.string.searching_devices_nearby)
        errorIcon.visibility = View.INVISIBLE
        progress.visibility = View.VISIBLE
        list.visibility = View.VISIBLE
        logoutButton.visibility = View.VISIBLE
        retryButton.visibility = View.INVISIBLE
        divider.visibility = View.VISIBLE
    }

    private fun FragmentSearchBinding.scanFoundAndStopState() {
        placeholder.visibility = View.INVISIBLE
        searching.setText(R.string.searching_devices_nearby)
        errorIcon.setImageResource(R.drawable.ic_success)
        errorIcon.visibility = View.VISIBLE
        progress.visibility = View.INVISIBLE
        list.visibility = View.VISIBLE
        logoutButton.visibility = View.VISIBLE
        retryButton.visibility = View.VISIBLE
        divider.visibility = View.VISIBLE
    }

    private fun FragmentSearchBinding.scanNotFoundAndStopState() {
        placeholder.visibility = View.VISIBLE
        searching.setText(R.string.searching_devices_not_found)
        errorIcon.setImageResource(R.drawable.ic_error)
        errorIcon.visibility = View.VISIBLE
        progress.visibility = View.INVISIBLE
        list.visibility = View.INVISIBLE
        logoutButton.visibility = View.VISIBLE
        retryButton.visibility = View.VISIBLE
        divider.visibility = View.INVISIBLE
    }

    private fun startScan() {
        binding?.scanStartState()
        adapter?.clear()

        destroy.add(
            HealbeSdk.get().GOBE.get()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { healbeGoBe: HealbeDevice -> adapter?.saved = healbeGoBe },
                    { t: Throwable -> Timber.e(t) }
                )
        )

        searchSubscription = HealbeSdk.get().GOBE.scan() // start devices scanning
            // if HEALBE GoBe is nearby it should be found in 15 seconds or even less
            .take(15, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { healbeGoBe: HealbeDevice ->
                    binding?.scanFoundAndContinueScanState()
                    Timber.d("found device: [%s:%s]", healbeGoBe.name, healbeGoBe.mac)
                    adapter?.add(healbeGoBe)
                },
                { showErrorIfEmpty() },
                { showErrorIfEmpty() }
            )
            .also { destroy.add(it) } // unsubscribe this on destroy too
    }

    private fun showErrorIfEmpty() {
        if (adapter?.isEmpty() == true) {
            Timber.d("Error [no devices  found]!")
            binding?.scanNotFoundAndStopState()
        } else {
            binding?.scanFoundAndStopState()
        }
        stopScan()
    }

    private fun stopScan() {
        searchSubscription?.dispose()
        searchSubscription = null
    }

    companion object {
        fun newInstance(): SearchFragment = SearchFragment()
    }
}