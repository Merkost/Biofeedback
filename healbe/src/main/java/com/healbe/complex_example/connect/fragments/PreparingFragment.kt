package com.healbe.complex_example.connect.fragments

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.healbe.complex_example.R
import com.healbe.complex_example.connect.ConnectRouter
import com.healbe.complex_example.connect.ConnectionRoutedFragment
import com.healbe.complex_example.databinding.FragmentPreparingBinding
import com.healbe.examples.common.Bluetooth
import com.healbe.healbesdk.business_api.HealbeSdk
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber

class PreparingFragment : ConnectionRoutedFragment() {
    private val destroy = CompositeDisposable()

    private var binding: FragmentPreparingBinding? = null

    private val requestPermissionLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val context = context ?: return@registerForActivityResult
            checkStateAndGoNext(context, result.all { (_, isGranted) -> isGranted })
        }

    private val requestEnableBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val context = context ?: return@registerForActivityResult
            if (result.resultCode == RESULT_OK) {
                checkStateAndGoNext(context)
            } else {
                showBluetoothNeededDialog(context)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        FragmentPreparingBinding.inflate(inflater)
            .also { binding = it }
            .root

    override fun onStart() {
        super.onStart()
        checkStateAndGoNext(requireContext())
    }

    private fun checkStateAndGoNext(context: Context, areAllPermissionsGranted: Boolean? = null) {
        if (!(areAllPermissionsGranted ?: Bluetooth.areAllPermissionsGranted(context))) {
            showBluetoothPermissionsNeededDialog(context)
        } else if (!Bluetooth.isOn(context)) {
            showBluetoothNeededDialog(context)
        } else {
            goNext()
        }
    }

    private fun showBluetoothPermissionsNeededDialog(context: Context) {
        val message =
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) R.string.warn_loc_permission
            else com.healbe.examples.common.R.string.warn_blu_permission

        MaterialAlertDialogBuilder(context, com.healbe.examples.common.R.style.AlertDialogCustom)
            .setMessage(message)
            .setPositiveButton(getString(com.healbe.examples.common.R.string.ok)) { _: DialogInterface?, _: Int ->
                val cnt = this.context ?: return@setPositiveButton
                Bluetooth.requestAllBluetoothPermissions(cnt, requestPermissionLauncher)
            }
            .setNegativeButton(getString(com.healbe.examples.common.R.string.logout)) { _: DialogInterface?, _: Int -> router?.logout() }
            .setCancelable(false)
            .show()
    }

    private fun showBluetoothNeededDialog(context: Context) {
        MaterialAlertDialogBuilder(context, com.healbe.examples.common.R.style.AlertDialogCustom)
            .setMessage(com.healbe.examples.common.R.string.warn_blu)
            .setPositiveButton(getString(com.healbe.examples.common.R.string.ok)) { _: DialogInterface?, _: Int ->
                val cnt = this.context ?: return@setPositiveButton
                if (Bluetooth.areAllPermissionsGranted(cnt)) {
                    Bluetooth.requestEnableBluetooth(requestEnableBluetooth)
                } else {
                    showBluetoothPermissionsNeededDialog(cnt)
                }
            }
            .setNegativeButton(getString(com.healbe.examples.common.R.string.logout)) { _: DialogInterface?, _: Int -> router?.logout() }
            .setCancelable(false)
            .show()
    }

    private fun goNext() {
        destroy.add(
            // try to get default wristband
            HealbeSdk.get().GOBE.get()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { device ->
                        // if we have an active wristband (user has entered|set correct pin in past)
                        router?.goState(
                            if (device.isActive) ConnectRouter.State.CONNECT else ConnectRouter.State.SEARCH,
                            false
                        )
                    },
                    { t: Throwable ->
                        Timber.e(t)
                        router?.goState(ConnectRouter.State.SEARCH, false)
                    }
                )
        )
    }

    override fun onDestroy() {
        destroy.clear()
        super.onDestroy()
    }

    companion object {
        fun newInstance(): PreparingFragment = PreparingFragment()
    }
}