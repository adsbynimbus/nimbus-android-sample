package com.adsbynimbus.android.sample.common

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.adsbynimbus.android.sample.R
import com.adsbynimbus.android.sample.databinding.CustomDialogBinding

fun showCustomDialog(message: String, inflater: LayoutInflater, context: Context?) : AlertDialog {
    val customDialog: CustomDialogBinding = CustomDialogBinding.inflate(inflater)
    val alertDialogBuilder = AlertDialog.Builder(context)
        .setView(customDialog.root)
        .setCancelable(false)
    val alertDialog = alertDialogBuilder.create()

    customDialog.description.text = context?.resources?.getString(R.string.custom_dialog_message, message)
    customDialog.button.setOnClickListener {
        alertDialog.dismiss()
    }

    return alertDialog
}
