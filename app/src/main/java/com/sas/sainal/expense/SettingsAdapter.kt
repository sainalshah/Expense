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

class SettingsAdapter(private val historyList: List<SettingsItem>) : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun onBindViewHolder(settingsViewHolder: SettingsViewHolder, i: Int) {
        val historyItem = historyList[i]
        settingsViewHolder.item.text = historyItem.name
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SettingsViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context).inflate(android.R.layout.simple_list_item_1, viewGroup, false)
        return SettingsViewHolder(itemView)
    }


    class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val item: TextView = itemView.findViewById(android.R.id.text1)
    }

    class SettingsItem(val name: String)
}