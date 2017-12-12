package com.sas.sainal.expense

import java.util.*

/**
 * Created by sainal on 12/7/17.
 */

data class SpendRecord(var id: Long?, var type: String, var amount: Double, var date: String) {
    constructor(type: String, amount: Double, date: String) : this(null, type, amount, date)
}
