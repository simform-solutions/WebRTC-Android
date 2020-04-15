package com.webrtc_android

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

object ActivityExtensions {
    /**
     * This fun is used to handle runtime permissions.
     * @receiver Fragment
     * @param permission List<String>
     * @param onPermissionGranted () -> Unit
     * @param onPermissionDeny () -> Unit
     */
    fun Activity.requestMultiplePermissions(permission: List<String>, onPermissionGranted: () -> Unit, onPermissionDeny: () -> Unit) {
        Dexter.withActivity(this)
                .withPermissions(permission)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()) {
                            onPermissionGranted()
                        } else {
                            if (report.isAnyPermissionPermanentlyDenied) {
                                onPermissionDeny()
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                        token?.continuePermissionRequest()
                    }
                }).check()

    }

    /**
     * This function is used to start permission activity.
     * @receiver Activity
     */
    fun Activity.startPermissionActivity() {
        Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", packageName, null)
            startActivity(this)
        }
    }
}