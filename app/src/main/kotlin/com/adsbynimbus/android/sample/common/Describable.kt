package com.adsbynimbus.android.sample.common

interface Describable {
    val description: String

    val identifier: String get() = description
        .split(" ")
        .joinToString(separator = "") {
            it.lowercase().replaceFirstChar { char -> char.uppercaseChar() }
        }
}
