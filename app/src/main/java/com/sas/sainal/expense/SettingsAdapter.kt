package com.sas.sainal.expense

import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


/**
 * Created by sainal on 12/16/17.
 */

class SettingsAdapter(private val historyList: List<SettingsItem>, private val context: ViewSettingsActivity) : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun onBindViewHolder(settingsViewHolder: SettingsViewHolder, i: Int) {
        val historyItem = historyList[i]
        settingsViewHolder.item.text = historyItem.name
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SettingsViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context).inflate(android.R.layout.simple_list_item_1, viewGroup, false)
        return SettingsViewHolder(itemView,context)
    }


    class SettingsViewHolder(itemView: View, private var context: ViewSettingsActivity) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            Log.v("testClick","item clicked")
//            CustomDialogFragment().show(context.fragmentManager,"dialog")
            val builder =AlertDialog.Builder(context).setView(R.layout.new_type_content)
                    .setTitle(R.string.new_type_title)
            val dialog = builder.create()
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getText(R.string.add), {
                dialogInterface, i ->{}
            })
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, context.getText(R.string.cancel), {
                dialogInterface, i ->{}
            })
            dialog.show()
        }

        val item: TextView = itemView.findViewById(android.R.id.text1)
    }

    class SettingsItem(val name: String)
}