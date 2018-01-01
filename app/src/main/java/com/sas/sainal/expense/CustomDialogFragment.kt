package com.sas.sainal.expense

import android.content.DialogInterface
import android.R.string.cancel
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.ContextThemeWrapper


/**
 * Created by sainal on 12/24/17.
 */
class CustomDialogFragment(context: AppCompatActivity, msg:Int, successBtn:Int, cancelBtn:Int, success: () -> Unit, fail: () -> Unit) {
    init {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(ContextThemeWrapper(context, R.style.DialogTheme))
        builder.setMessage(msg)
                .setPositiveButton(successBtn,
                        { _, _ ->
                            success()
                        })
                .setNegativeButton(cancelBtn,
                        { _, _ ->
                            fail()
                        })
        // Create the AlertDialog object and return it
        builder.create().show()
    }
}