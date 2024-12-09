package com.healbe.examples.common

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

object Bluetooth {
    @JvmStatic
    fun areAllPermissionsGranted(context: Context): Boolean =
        isBluetoothScanPermissionGranted(context) && isBluetoothConnectPermissionGranted(context)

    @JvmStatic
    fun isBluetoothScanPermissionGranted(context: Context): Boolean = when {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.P ->
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S ->
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        else ->
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
    }

    @JvmStatic
    fun isBluetoothConnectPermissionGranted(context: Context): Boolean =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            true
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        }

    @JvmStatic
    fun requestAllBluetoothPermissions(
        context: Context,
        launcher: ActivityResultLauncher<Array<String>>
    ) {
        val allPermissions = when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.P ->
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION)

            Build.VERSION.SDK_INT < Build.VERSION_CODES.S -> arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            else -> arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        }

        val missingPermissions = allPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_DENIED
        }

        launcher.launch(missingPermissions.toTypedArray())
    }

    @JvmStatic
    fun requestBluetoothConnectionPermissions(
        context: Context,
        launcher: ActivityResultLauncher<Array<String>>
    ) {
        val allPermissions = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
              add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        val missingPermissions = allPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_DENIED
        }

        launcher.launch(missingPermissions.toTypedArray())
    }

    @JvmStatic
    fun isOn(context: Context): Boolean =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)
            ?.adapter
            ?.isEnabled
            ?: false

    /**
     * Show system dialog to enable Bluetooth.
     *
     * NOTE: Android 12+ (Android SDK 31+) requires to have BLUETOOTH_CONNECT to do this so
     * don't use this method if `isAllPermissionsGranted()` method returns `false` on
     * Android 12+ (Android SDK 31+).
     */
    @JvmStatic
    fun requestEnableBluetooth(launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        launcher.launch(intent)
    }
}