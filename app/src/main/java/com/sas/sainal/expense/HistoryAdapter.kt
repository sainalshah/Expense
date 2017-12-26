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

    private var mDataSet:MutableList<SpendRecord>? = null
    init{
        mDataSet = historyList.toMutableList()
    }
    override fun getItemCount(): Int {
        return mDataSet!!.size
    }

    override fun onBindViewHolder(historyViewHolder: HistoryViewHolder, i: Int) {
        if(i<mDataSet!!.size) {
            val historyItem = mDataSet!![i]
            historyViewHolder.type.text = historyItem.type
            historyViewHolder.amount.text = historyItem.getAmount()
            historyViewHolder.date.text = historyItem.date
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): HistoryViewHolder {
        val layout = R.layout.history_card_layout
        val itemView = LayoutInflater.from(viewGroup.context).inflate(layout, viewGroup, false)
        return HistoryViewHolder(itemView)
    }

    fun remove(position: Int) :Long{
        val id = mDataSet?.get(position)?.id!!
        mDataSet?.removeAt(position)
        notifyItemRemoved(position)
        return  id
    }

    fun getItem(position: Int):SpendRecord{
        return mDataSet?.get(position)!!
    }

    class HistoryViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val type: TextView = v.findViewById(R.id.history_type)
        val amount: TextView = v.findViewById(R.id.history_amount)
        val date: TextView = v.findViewById(R.id.history_date)
    }
}