package com.devhomc.videoplayersample.util

import android.app.Activity
import android.app.PictureInPictureParams
import android.content.pm.PackageManager.FEATURE_PICTURE_IN_PICTURE
import android.os.Build
import android.util.Rational

fun Activity.enterPictureInPicture() {
    if (isPictureInPictureSupported()) {
        try {
            val params = PictureInPictureParams.Builder().build()
            enterPictureInPictureMode(params)
        } catch (e: IllegalStateException) {
            // Device doesn't support picture-in-picture mode.
        }
    }
}

fun Activity.updatePictureInPictureParams(
    isEnabled: Boolean? = null,
    aspectRatio: Rational? = null
) {
    if (isPictureInPictureSupported()) {
        try {
            val paramsBuilder = PictureInPictureParams.Builder()
            if (isEnabled != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                paramsBuilder.setAutoEnterEnabled(isEnabled)
            }
            if (aspectRatio != null) {
                paramsBuilder.setAspectRatio(aspectRatio)
            }
            setPictureInPictureParams(paramsBuilder.build())
        } catch (e: IllegalStateException) {
            // Device doesn't support picture-in-picture mode.
        }
    }
}

private fun Activity.isPictureInPictureSupported() =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            packageManager.hasSystemFeature(FEATURE_PICTURE_IN_PICTURE)
