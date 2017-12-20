package com.sas.sainal.expense

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by sainal on 12/19/17.
 */

class Datetime{
    companion object {
        val DATE_FORMAT = "YYYY-MM-dd hh:mm:ss"
        val df: DateFormat = SimpleDateFormat(DATE_FORMAT)
    }
    /*get random date in the period specified.
        * First argument is how days back is the starting date from current date
        * Second argument is how many days back is the end date from current date*/
    fun getRandomDatetime(numberDaysBack: Int, period: Double): String {
        val cal = Calendar.getInstance()
        cal.time = Date()
        cal.add(Calendar.DATE, numberDaysBack)
        val dateBeforeXDays = cal.time

        val rnd = Random()
        // Get an Epoch value with dateBeforeXDays
        val ms: Long = dateBeforeXDays.time  + Math.abs(rnd.nextLong()) % (Math.abs(period) * 24L * 60 * 60 * 1000).toLong()

        // Construct a date

        val dt = Date(ms)

        val sqlDate = java.sql.Date(dt.time)
        return df.format(sqlDate)
    }

    fun getCurrentDatetime(): String {
        val sqlDate = java.sql.Date(java.util.Date().time)
        return df.format(sqlDate)
    }

    fun getDateDiff(date1: String, date2: String): Long {
        val d1 = df.parse(date1)
        val d2 = df.parse(date2)
        val diff = d2.time - d1.time

        return Math.abs(TimeUnit.MILLISECONDS.toDays(diff))
    }
}