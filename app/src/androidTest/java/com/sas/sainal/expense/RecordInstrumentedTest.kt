package com.sas.sainal.expense

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import org.hamcrest.CoreMatchers.`is`
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class RecordInstrumentedTest {

    val TAG: String = "RECORD_TEST"
    val TEST_TYPE1 = "shopping"
    val TEST_TYPE2 = "clothing"
    val TEST_TYPE3 = "grocery"

    val TEST_INCOME_AMOUNT = 1002.00
    val TEST_SPENDING_AMOUNT1 = 100.00
    val TEST_SPENDING_AMOUNT2 = 50.00

    companion object {

        val dt = Datetime()
        var sqlDate1 = dt.getCurrentDatetime()
        var sqlDate2 = dt.getCurrentDatetime()


        private val context: Context = InstrumentationRegistry.getTargetContext()
        val databaseHandler = ExpenseDatabaseHandler(context)

        val RANDOM_TEST_SAMPLE_SIZE = 10
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.sas.sainal.expense", appContext.packageName)
    }

    @Before
    fun setup() {
        databaseHandler.clearTable(ExpenseDatabaseHandler.Table.RECORD)
        databaseHandler.clearTable(ExpenseDatabaseHandler.Table.TYPE)
    }

    @Test
    fun testCreateNewRecord() {
        databaseHandler.addSpendType(TEST_TYPE2)
        val newRecord = SpendRecord(TEST_TYPE2, TEST_SPENDING_AMOUNT1, sqlDate1)
        assertEquals(1, databaseHandler.addSpendRecord(newRecord))
    }

    @Test
    fun testInitialCountRecord() {
        assertEquals(0, databaseHandler.getCount(ExpenseDatabaseHandler.Table.RECORD))
    }

    @Test
    fun testCountAfterNewType() {
        databaseHandler.addSpendType(TEST_TYPE3)
        databaseHandler.addSpendType(TEST_TYPE1)
        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_SPENDING_AMOUNT1, sqlDate1))
        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE1, TEST_SPENDING_AMOUNT1, sqlDate1))

        assertEquals(2, databaseHandler.getCount(ExpenseDatabaseHandler.Table.RECORD))
    }

    @Test
    fun testNonExistTypeAddRecord() {
        assertEquals(ExpenseDatabaseHandler.SQL_ERROR, databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE1, TEST_SPENDING_AMOUNT1, sqlDate1)))
    }

    @Test
    fun testGetRecord() {
        databaseHandler.addSpendType(TEST_TYPE3)
        val recordId = databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_SPENDING_AMOUNT1, sqlDate1))
        val record = databaseHandler.getSpendRecord(recordId)

        assertEquals(TEST_TYPE3, record?.type)
        assertEquals(TEST_SPENDING_AMOUNT1, record?.amount)
        assertEquals(sqlDate1, record?.date)
    }

    @Test
    fun testUpdateRecordWithId() {
        databaseHandler.addSpendType(TEST_TYPE3)
        databaseHandler.addSpendType(TEST_TYPE1)

        val recordId = databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_SPENDING_AMOUNT1, sqlDate1))
        val retVal = databaseHandler.updateSpendRecord(SpendRecord(recordId, TEST_TYPE1, TEST_SPENDING_AMOUNT2, sqlDate2))
        assert(retVal >= 0)
    }


    @Test
    fun testUpdateRecord() {
        databaseHandler.addSpendType(TEST_TYPE3)
        databaseHandler.addSpendType(TEST_TYPE1)

        val recordId = databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_SPENDING_AMOUNT1, sqlDate1))
        databaseHandler.updateSpendRecord(SpendRecord(recordId, TEST_TYPE1, TEST_SPENDING_AMOUNT2, sqlDate2))

        val updatedRecord = databaseHandler.getSpendRecord(recordId)

        assertEquals(TEST_TYPE1, updatedRecord?.type)
        assertEquals(TEST_SPENDING_AMOUNT2, updatedRecord?.amount)
        assertEquals(sqlDate2, updatedRecord?.date)
    }

    @Test
    fun testNonExistTypeUpdateRecord() {
        databaseHandler.addSpendType(TEST_TYPE3)

        val recordId = databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_SPENDING_AMOUNT1, sqlDate1))
        val retVal = databaseHandler.updateSpendRecord(SpendRecord(recordId, TEST_TYPE1, TEST_SPENDING_AMOUNT2, sqlDate2))


        assertEquals(ExpenseDatabaseHandler.SQL_ERROR, retVal)
    }

    @Test
    fun testGetAllRecord() {

        val expTypeList = listOf(TEST_TYPE1, TEST_TYPE3, TEST_TYPE2)

        databaseHandler.addSpendType(TEST_TYPE3)
        databaseHandler.addSpendType(TEST_TYPE1)
        databaseHandler.addSpendType(TEST_TYPE2)

        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_SPENDING_AMOUNT1, sqlDate1))
        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE1, TEST_SPENDING_AMOUNT2, sqlDate1))
        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE2, TEST_SPENDING_AMOUNT2, sqlDate2))
        val recordList = databaseHandler.getPeriodSpendRecord(ExpenseDatabaseHandler.Period.ALL)

        val actualList = listOf(recordList?.get(0)?.type, recordList?.get(1)?.type, recordList?.get(2)?.type)

        assert(actualList.containsAll(expTypeList))
    }

    @Test
    fun testGetLastWeekRecord() {
        databaseHandler.addSpendType(TEST_TYPE3)
        var i = 0
        while (i < RANDOM_TEST_SAMPLE_SIZE) {
            databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_SPENDING_AMOUNT1, dt.getRandomDatetime(-6, 14.0)))
            i++
        }
        val recordList = databaseHandler.getPeriodSpendRecord(ExpenseDatabaseHandler.Period.LAST_WEEK)

        for (record in recordList!!.iterator()) {
            Log.v(TAG, "testing date from db: ${record.date}")
            assert(dt.getDateDiff(record.date, dt.getCurrentDatetime()) <= 7)
        }
    }

    @Test
    fun testGetLastMonthRecord() {
        databaseHandler.addSpendType(TEST_TYPE3)
        var i = 0
        while (i < RANDOM_TEST_SAMPLE_SIZE) {
            val date = dt.getRandomDatetime(-30, 40.0)
            databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_SPENDING_AMOUNT1, date))
            i++
        }
        val recordList = databaseHandler.getPeriodSpendRecord(ExpenseDatabaseHandler.Period.LAST_MONTH)

        Log.v(TAG, "\n\nall record count: ${recordList?.count()}")
        var lastWeekCount = 0
        for (record in recordList!!.iterator()) {
            Log.v(TAG, "testing date from db: ${record.date}")
            assert(dt.getDateDiff(record.date, dt.getCurrentDatetime()) <= 30)
            lastWeekCount++
        }

        Log.v(TAG, "last week record count: $lastWeekCount")
    }

    @Test
    fun testGetLastDayRecord() {
        databaseHandler.addSpendType(TEST_TYPE3)
        var i = 0
        while (i < RANDOM_TEST_SAMPLE_SIZE) {
            val date = dt.getRandomDatetime(-4, 5.0)
            databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_SPENDING_AMOUNT1, date))
            i++
        }
        val recordList = databaseHandler.getPeriodSpendRecord(ExpenseDatabaseHandler.Period.TODAY)

        Log.v(TAG, "\n\nall record count: ${recordList?.count()}")
        var lastDayCount = 0
        for (record in recordList!!.iterator()) {
            Log.v(TAG, "testing date from db: ${record.date}")
            assert(dt.getDateDiff(record.date, dt.getCurrentDatetime()) <= 30)
            lastDayCount++
        }

        Log.v(TAG, "last day record count: $lastDayCount")
    }

    @Test
    fun testGetLastDaySpendingSum() {
        databaseHandler.addSpendType(TEST_TYPE2)

        val date1 = dt.getRandomDatetime(0, 0.25)
        val date2 = dt.getRandomDatetime(-4, 1.0)
        val date3 = dt.getRandomDatetime(0, 0.25)

        Log.v(TAG, "dates generated: $date1\n$date2\n$date3")
        // add a record for today
        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE2, TEST_SPENDING_AMOUNT1, date1))

        // add a record for 4 days back
        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE2, TEST_SPENDING_AMOUNT2, date2))

        // add a record for today
        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE2, TEST_SPENDING_AMOUNT2, date3))

        assertThat(databaseHandler.getPeriodSpendingSum(ExpenseDatabaseHandler.Period.TODAY), `is`(TEST_SPENDING_AMOUNT2 + TEST_SPENDING_AMOUNT1))
    }

    @Test
    fun testGetSpendingWithIncome() {
        databaseHandler.addSpendType(ExpenseDatabaseHandler.SPECIAL_TYPE_INCOME)

        databaseHandler.addSpendType(TEST_TYPE3)
        databaseHandler.addSpendType(TEST_TYPE1)
        databaseHandler.addSpendType(TEST_TYPE2)

        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_SPENDING_AMOUNT1, sqlDate1))
        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE1, TEST_SPENDING_AMOUNT2, sqlDate1))
        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE2, TEST_SPENDING_AMOUNT2, sqlDate2))

        databaseHandler.addSpendRecord(SpendRecord(ExpenseDatabaseHandler.SPECIAL_TYPE_INCOME, TEST_INCOME_AMOUNT, sqlDate1))

        assertThat(databaseHandler.getPeriodSpendingSum(ExpenseDatabaseHandler.Period.ALL),
                `is`(TEST_SPENDING_AMOUNT2 + TEST_SPENDING_AMOUNT2 + TEST_SPENDING_AMOUNT1))
    }

    @Test
    fun testGetBalanceWithNoSpending() {

        databaseHandler.addSpendType(ExpenseDatabaseHandler.SPECIAL_TYPE_INCOME)
        databaseHandler.addSpendRecord(SpendRecord(ExpenseDatabaseHandler.SPECIAL_TYPE_INCOME, TEST_INCOME_AMOUNT, sqlDate1))
        databaseHandler.addSpendRecord(SpendRecord(ExpenseDatabaseHandler.SPECIAL_TYPE_INCOME, TEST_INCOME_AMOUNT, sqlDate2))

        assertThat(databaseHandler.getBalance(),
                `is`(TEST_INCOME_AMOUNT + TEST_INCOME_AMOUNT))
    }

    @Test
    fun testGetBalanceWithSpending() {

        databaseHandler.addSpendType(ExpenseDatabaseHandler.SPECIAL_TYPE_INCOME)
        databaseHandler.addSpendType(TEST_TYPE3)
        databaseHandler.addSpendType(TEST_TYPE1)
        databaseHandler.addSpendType(TEST_TYPE2)

        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_SPENDING_AMOUNT1, sqlDate1))
        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE1, TEST_SPENDING_AMOUNT2, sqlDate1))
        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE2, TEST_SPENDING_AMOUNT2, sqlDate2))

        databaseHandler.addSpendRecord(SpendRecord(ExpenseDatabaseHandler.SPECIAL_TYPE_INCOME, TEST_INCOME_AMOUNT, sqlDate1))
        databaseHandler.addSpendRecord(SpendRecord(ExpenseDatabaseHandler.SPECIAL_TYPE_INCOME, TEST_INCOME_AMOUNT, sqlDate2))

        assertThat(databaseHandler.getBalance(),
                `is`((TEST_INCOME_AMOUNT * 2) - ((TEST_SPENDING_AMOUNT2*2)+TEST_SPENDING_AMOUNT1)))
    }

    @Test
    fun testDeleteType() {

        val retVal = databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_SPENDING_AMOUNT1, sqlDate1))

        databaseHandler.deleteSpendRecord(retVal)

        assertEquals(null, databaseHandler.getSpendRecord(retVal))
    }
}
