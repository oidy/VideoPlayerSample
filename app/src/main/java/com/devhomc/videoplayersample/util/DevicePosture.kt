package com.devhomc.videoplayersample.util

import android.graphics.Rect

sealed interface DevicePosture {
    object Normal : DevicePosture
    data class TableTop(val hingePosition: Rect) : DevicePosture
    data class Book(val hingePosition: Rect) : DevicePosture
}
