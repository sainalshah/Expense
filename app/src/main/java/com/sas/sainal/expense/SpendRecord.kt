package com.sas.sainal.expense

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * Created by sainal on 12/7/17.
 */

data class SpendRecord(var id: Long?, var type: String, var amount: Double, var date: String):Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readValue(Long::class.java.classLoader) as? Long,
            parcel.readString(),
            parcel.readDouble(),
            parcel.readString()) {
    }

    constructor(type: String, amount: Double, date: String) : this(null, type, amount, date)

    fun display():String{
        return """
        id: $id
        type: $type
        amount: $amount
        date: $date"""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(type)
        parcel.writeDouble(amount)
        parcel.writeString(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SpendRecord> {
        override fun createFromParcel(parcel: Parcel): SpendRecord {
            return SpendRecord(parcel)
        }

        override fun newArray(size: Int): Array<SpendRecord?> {
            return arrayOfNulls(size)
        }
    }
}
