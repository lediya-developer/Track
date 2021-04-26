package com.lediya.trackingapp.view.alertdialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import androidx.databinding.DataBindingUtil
import com.lediya.trackingapp.R
import com.lediya.trackingapp.databinding.NegativeAlertDialogBinding

class NegativeAlertDialog(context: Context, theme: Int) : Dialog(context, theme) {

    private var binding: NegativeAlertDialogBinding = DataBindingUtil.inflate(
        LayoutInflater.from(context),
        R.layout.negative_alert_dialog,
        null,
        false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        setCancelable(false)
    }

    private fun setDialogItems(
        title: String,
        message: String,
        negativeTitle: String,
        positiveTitle: String,
        negativeAlertDialogListener: NegativeAlertDialogListener? = null
    ) {
        binding.dialogTitle.text = title
        binding.dialogMessage.text = message
        binding.confirmButton.text = positiveTitle
        binding.cancelButton.text = negativeTitle
        binding.cancelButton.setOnClickListener {
            this.dismiss()
            negativeAlertDialogListener?.negativeActionButtonClicked()
        }
        binding.confirmButton.setOnClickListener {
            this.dismiss()
            negativeAlertDialogListener?.positiveActionButtonClicked()
        }
        binding.closeButton.setOnClickListener {
            this.dismiss()
            negativeAlertDialogListener?.closeButtonClicked()
        }
    }
    private fun setDialogItems(
        title: String,
        message: String,
        negativeAlertDialogListener: NegativeAlertDialogListener? = null
    ) {
        binding.dialogTitle.text = title
        binding.dialogMessage.text = message
        binding.confirmButton.text = context.getString(R.string.ok)
        binding.closeButton.visibility = View.GONE
        binding.cancelButton.visibility = View.GONE
        binding.confirmButton.setOnClickListener {
            this.dismiss()
            negativeAlertDialogListener?.negativeActionButtonClicked()
        }
    }
    fun createAlert(
        title: String,
        message: String,
        negativeTitle: String,
        positiveTitle: String,
        negativeAlertDialogListener: NegativeAlertDialogListener? = null
    ) {
        setDialogItems(title, message, negativeTitle, positiveTitle, negativeAlertDialogListener)
    }

    fun createWarning(
        message: String,
        negativeTitle: String,
        positiveTitle: String,
        negativeAlertDialogListener: NegativeAlertDialogListener? = null
    ) {
        setDialogItems(
            context.getString(R.string.warning_title),
            message,
            negativeTitle,
            positiveTitle,
            negativeAlertDialogListener
        )
    }
    fun createSuccessDialog(
        message: String,
        negativeAlertDialogListener: NegativeAlertDialogListener? = null
    ) {
        setDialogItems(
            context.getString(R.string.success_title),
            message,
            negativeAlertDialogListener
        )
    }
    fun createCancelOKWarning(
        message: String,
        negativeAlertDialogListener: NegativeAlertDialogListener? = null
    ) {
        setDialogItems(
            context.getString(R.string.warning_title),
            message,
            context.getString(R.string.ok),
            context.getString(R.string.cancel),
            negativeAlertDialogListener
        )
    }

    /**
     * Listener interface for NegativeAlertDialog
     */
    interface NegativeAlertDialogListener {
        fun negativeActionButtonClicked()
        fun positiveActionButtonClicked()
        fun closeButtonClicked()
    }
}