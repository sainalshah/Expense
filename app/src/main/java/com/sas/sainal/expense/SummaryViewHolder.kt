package com.sas.sainal.expense

import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup


/**
 * Created by sainal on 12/16/17.
 */

class SummaryAdapter(private val summaryList: List<SummaryInfo>, private val type: TYPE) : RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder>() {

    enum class TYPE {
        SPENDING, BALANCE
    }

    override fun getItemCount(): Int {
        return summaryList.size
    }

    override fun onBindViewHolder(summaryViewHolder: SummaryViewHolder, i: Int) {
        val summary = summaryList[i]
        summaryViewHolder.title.text = summary.title
        summaryViewHolder.amount.text = summary.amount.toString()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SummaryViewHolder {
        val layout: Int =
                if (type == TYPE.SPENDING) {
                    R.layout.spending_card_layout
                } else {
                    R.layout.balance_card_layout
                }
        val itemView = LayoutInflater.from(viewGroup.context).inflate(layout, viewGroup, false)

        return SummaryViewHolder(itemView)
    }


    class SummaryViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.card_view_title)
        val amount: TextView = v.findViewById(R.id.card_view_amount)
    }
}