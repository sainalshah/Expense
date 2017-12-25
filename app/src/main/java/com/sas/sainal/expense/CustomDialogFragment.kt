package com.sas.sainal.expense

import android.content.DialogInterface
import android.R.string.cancel
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog


/**
 * Created by sainal on 12/24/17.
 */
class CustomDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(getActivity())
        builder.setMessage("Test msg")
                .setPositiveButton("Hurray!!", { dialog, id ->
                    // FIRE ZE MISSILES!
                })
                .setNegativeButton(":(:(", { dialog, id ->
                    // User cancelled the dialog
                })
        // Create the AlertDialog object and return it
        return builder.create()
    }
}