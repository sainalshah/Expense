package com.sas.sainal.expense

import android.content.Intent
import android.widget.TextView
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button


/**
 * Created by sainal on 12/16/17.
 */

class SummaryAdapter(private val summaryList: List<SummaryInfo>, private val context: MainActivity) : RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder>() {
    private val balanceCardIndex = 2

    override fun getItemCount(): Int {
        return summaryList.size
    }

    override fun onBindViewHolder(summaryViewHolder: SummaryViewHolder, i: Int) {
        val summary = summaryList[i]
        summaryViewHolder.title.text = summary.title
        summaryViewHolder.amount.text = summary.amount
        if (i != balanceCardIndex) {
            summaryViewHolder.addIncome.visibility = View.GONE
        }
        summaryViewHolder.addIncome.setOnClickListener {
            // Click action
            val intent = Intent(context, NewRecordActivity::class.java).putExtra(NewRecordActivity.INTENT_ACTION_KEY, NewRecordActivity.TYPE_INCOME)
            context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): SummaryViewHolder {
        val layout = R.layout.spending_card_layout
        val itemView = LayoutInflater.from(viewGroup.context).inflate(layout, viewGroup, false)
        return SummaryViewHolder(itemView)
    }


    class SummaryViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.card_view_title)
        val amount: TextView = v.findViewById(R.id.card_view_amount)
        val addIncome: Button = v.findViewById(R.id.add_income_btn)
    }

    class SummaryInfo(val title: String, val amount: String)
}