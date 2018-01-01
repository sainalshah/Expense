package com.sas.sainal.expense

import android.os.Parcel
import android.os.Parcelable

/**
 * Created by sainal on 12/7/17.
 */

data class SpendRecord(var id: Long?, var type: String, var amount: Double, var date: String, var comment: String) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readValue(Long::class.java.classLoader) as? Long,
            parcel.readString(),
            parcel.readDouble(),
            parcel.readString(),
            parcel.readString())

    constructor(type: String, amount: Double, date: String, comment: String) : this(null, type, amount, date, comment)


    fun getAmount(): String {
        return String.format(AMOUNT_FORMAT, amount)
    }

    fun display(): String {
        return """
        id: $id
        type: $type
        amount: $amount
        date: $date
        comment: $comment"""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(type)
        parcel.writeDouble(amount)
        parcel.writeString(date)
        parcel.writeString(comment)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SpendRecord> {
        var CURRENCY_SYMBOL = "$"
        val DEFAULT_CURRENCY_NAME = "United States Dollar"
        var AMOUNT_FORMAT = "$CURRENCY_SYMBOL%.2f"

        fun updateCurrencySymbol(currencyCode : String){
            AMOUNT_FORMAT = "$currencyCode%.2f"
        }
        override fun createFromParcel(parcel: Parcel): SpendRecord {
            return SpendRecord(parcel)
        }

        override fun newArray(size: Int): Array<SpendRecord?> {
            return arrayOfNulls(size)
        }
    }
}
