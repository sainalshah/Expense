package com.sas.sainal.expense

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


/**
 * Created by sainal on 12/16/17.
 */

class HistoryAdapter(private val historyList: List<SpendRecord>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun getItemCount(): Int {
        return historyList.size
    }

    override fun onBindViewHolder(historyViewHolder: HistoryViewHolder, i: Int) {
        val historyItem = historyList[i]
        historyViewHolder.type.text = historyItem.type
        historyViewHolder.amount.text = historyItem.amount.toString()
        historyViewHolder.date.text = historyItem.date
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): HistoryViewHolder {
        val layout = R.layout.history_card_layout
        val itemView = LayoutInflater.from(viewGroup.context).inflate(layout, viewGroup, false)
        return HistoryViewHolder(itemView)
    }


    class HistoryViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val type: TextView = v.findViewById(R.id.history_type)
        val amount: TextView = v.findViewById(R.id.history_amount)
        val date: TextView = v.findViewById(R.id.history_date)
    }
}