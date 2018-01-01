package com.sas.sainal.expense

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


/**
 * Created by sainal on 12/16/17.
 */

class HistoryAdapter(private val historyList: List<SpendRecord>, private val context:ViewHistoryActivity) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private var mDataSet: MutableList<SpendRecord>? = null

    init {
        mDataSet = historyList.toMutableList()
    }

    override fun getItemCount(): Int {
        return mDataSet!!.size
    }

    override fun onBindViewHolder(historyViewHolder: HistoryViewHolder, i: Int) {
        if (i < mDataSet!!.size) {
            val historyItem = mDataSet!![i]
            historyViewHolder.type.text = historyItem.type
            historyViewHolder.amount.text = historyItem.getAmount()
            historyViewHolder.date.text = historyItem.date
            if (historyItem.comment.isNotEmpty()) {
                historyViewHolder.comment.text = historyItem.comment
                historyViewHolder.comment.visibility = View.VISIBLE
            }

            //mark all spending as red, sparing income
            if (historyItem.type != ExpenseDatabaseHandler.SPECIAL_TYPE_INCOME)
                historyViewHolder.amount.setTextColor(context.resources.getColor(R.color.material_red))
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): HistoryViewHolder {
        val layout = R.layout.history_card_layout
        val itemView = LayoutInflater.from(viewGroup.context).inflate(layout, viewGroup, false)
        return HistoryViewHolder(itemView)
    }

    fun remove(position: Int): Long {
        val id = mDataSet?.get(position)?.id!!
        mDataSet?.removeAt(position)
        notifyItemRemoved(position)
        return id
    }

    fun getItem(position: Int): SpendRecord {
        return mDataSet?.get(position)!!
    }

    class HistoryViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val type: TextView = v.findViewById(R.id.history_type)
        val amount: TextView = v.findViewById(R.id.history_amount)
        val date: TextView = v.findViewById(R.id.history_date)
        val comment: TextView = v.findViewById(R.id.history_comment)
    }
}