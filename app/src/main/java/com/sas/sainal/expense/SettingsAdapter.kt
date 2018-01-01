package com.sas.sainal.expense

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


/**
 * Created by sainal on 12/16/17.
 */

class SettingsAdapter(private val historyList: List<SettingsItem>, private val currency:String) : RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder>() {

    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun onBindViewHolder(settingsViewHolder: SettingsViewHolder, i: Int) {
        val historyItem = historyList[i]
        settingsViewHolder.itemTitle.text = historyItem.name
        if(i == 1){ //second position is currency
            settingsViewHolder.itemValue.visibility = View.VISIBLE
            settingsViewHolder.itemValue.text = "$currency selected"
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SettingsViewHolder {
        val itemView = LayoutInflater.from(viewGroup.context).inflate(R.layout.settings_card_layout, viewGroup, false)
        return SettingsViewHolder(itemView)
    }


    class SettingsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val itemTitle: TextView = itemView.findViewById(R.id.settings_title)
        val itemValue: TextView = itemView.findViewById(R.id.settings_value)
    }

    class SettingsItem(val name: String)
}