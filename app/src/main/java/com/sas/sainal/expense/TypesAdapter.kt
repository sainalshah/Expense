package com.sas.sainal.expense

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView


/**
 * Created by sainal on 12/16/17.
 */

class TypesAdapter(historyList: List<String>) : RecyclerView.Adapter<TypesAdapter.TypesViewHolder>() {

    private var mDataSet: MutableList<String>? = null

    init {
        mDataSet = historyList.toMutableList()
    }

    override fun getItemCount(): Int {
        return mDataSet!!.size
    }

    override fun onBindViewHolder(typesViewHolder: TypesViewHolder, i: Int) {
        if (i < mDataSet!!.size) {
            val historyItem = mDataSet!![i]
            typesViewHolder.type.text = historyItem
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): TypesViewHolder {
        val layout = R.layout.view_types_card_layout
        val itemView = LayoutInflater.from(viewGroup.context).inflate(layout, viewGroup, false)
        return TypesViewHolder(itemView)
    }

    fun addToTop(type:String){
        mDataSet?.add(0,type)
        notifyItemInserted(0)
    }
    fun remove(position: Int):String {
        val item = mDataSet?.get(position)
        mDataSet?.removeAt(position)
        notifyItemRemoved(position)
        return item!!
    }

    fun getItem(position: Int): String {
        return mDataSet?.get(position)!!
    }

    class TypesViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val type: TextView = v.findViewById(R.id.type_title)
        val closeBtn: ImageButton = v.findViewById(R.id.delete_type_btn)
    }
}