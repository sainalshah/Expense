package com.sas.sainal.expense

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import android.util.Log
import org.junit.Assert.assertEquals
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

    companion object {

        val dt = Datetime()
        val TAG: String = "RECORD_TEST"
        val TEST_TYPE1: String = "shopping"
        val TEST_TYPE2: String = "clothing"
        val TEST_TYPE3: String = "grocery"

        val TEST_AMOUNT1: Double = 100.00
        val TEST_AMOUNT2: Double = 50.00
        var sqlDate1 = dt.getCurrentDatetime()
        var sqlDate2 = dt.getCurrentDatetime()


        private val context: Context = InstrumentationRegistry.getTargetContext()
        val databaseHandler = ExpenseDatabaseHandler(context)

        val RANDOM_TEST_SAMPLE_SIZE = 100
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
        val newRecord = SpendRecord(TEST_TYPE2, TEST_AMOUNT1, sqlDate1)
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
        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_AMOUNT1, sqlDate1))
        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE1, TEST_AMOUNT1, sqlDate1))

        assertEquals(2, databaseHandler.getCount(ExpenseDatabaseHandler.Table.RECORD))
    }

    @Test
    fun testNonExistTypeAddRecord() {
        assertEquals(ExpenseDatabaseHandler.SQL_ERROR, databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE1, TEST_AMOUNT1, sqlDate1)))
    }

    @Test
    fun testGetRecord() {
        databaseHandler.addSpendType(TEST_TYPE3)
        val recordId = databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_AMOUNT1, sqlDate1))
        val record = databaseHandler.getSpendRecord(recordId)

        assertEquals(TEST_TYPE3, record?.type)
        assertEquals(TEST_AMOUNT1, record?.amount)
        assertEquals(sqlDate1, record?.date)
    }

    @Test
    fun testUpdateRecordWithId() {
        databaseHandler.addSpendType(TEST_TYPE3)
        databaseHandler.addSpendType(TEST_TYPE1)

        val recordId = databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_AMOUNT1, sqlDate1))
        val retVal = databaseHandler.updateSpendRecord(SpendRecord(recordId, TEST_TYPE1, TEST_AMOUNT2, sqlDate2))
        assert(retVal >= 0)
    }


    @Test
    fun testUpdateRecord() {
        databaseHandler.addSpendType(TEST_TYPE3)
        databaseHandler.addSpendType(TEST_TYPE1)

        val recordId = databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_AMOUNT1, sqlDate1))
        databaseHandler.updateSpendRecord(SpendRecord(recordId, TEST_TYPE1, TEST_AMOUNT2, sqlDate2))

        val updatedRecord = databaseHandler.getSpendRecord(recordId)

        assertEquals(TEST_TYPE1, updatedRecord?.type)
        assertEquals(TEST_AMOUNT2, updatedRecord?.amount)
        assertEquals(sqlDate2, updatedRecord?.date)
    }

    @Test
    fun testNonExistTypeUpdateRecord() {
        databaseHandler.addSpendType(TEST_TYPE3)

        val recordId = databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_AMOUNT1, sqlDate1))
        val retVal = databaseHandler.updateSpendRecord(SpendRecord(recordId, TEST_TYPE1, TEST_AMOUNT2, sqlDate2))


        assertEquals(ExpenseDatabaseHandler.SQL_ERROR, retVal)
    }

    @Test
    fun testGetAllRecord() {

        val expTypeList = listOf(TEST_TYPE1, TEST_TYPE3, TEST_TYPE2)

        databaseHandler.addSpendType(TEST_TYPE3)
        databaseHandler.addSpendType(TEST_TYPE1)
        databaseHandler.addSpendType(TEST_TYPE2)

        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_AMOUNT1, sqlDate1))
        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE1, TEST_AMOUNT2, sqlDate1))
        databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE2, TEST_AMOUNT2, sqlDate2))
        val recordList = databaseHandler.getAllSpendRecords(ExpenseDatabaseHandler.Period.ALL)

        val actualList = listOf(recordList?.get(0)?.type, recordList?.get(1)?.type, recordList?.get(2)?.type)

        assert(actualList.containsAll(expTypeList))
    }

    @Test
    fun testGetLastWeekRecord() {
        databaseHandler.addSpendType(TEST_TYPE3)
        var i = 0
        while (i < RANDOM_TEST_SAMPLE_SIZE) {
            databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_AMOUNT1, dt.getRandomDatetime(-6, 14)))
            i++
        }
        val recordList = databaseHandler.getAllSpendRecords(ExpenseDatabaseHandler.Period.LAST_WEEK)

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
            val date = dt.getRandomDatetime(-30, 40)
            databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_AMOUNT1, date))
            i++
        }
        val recordList = databaseHandler.getAllSpendRecords(ExpenseDatabaseHandler.Period.LAST_MONTH)

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
            val date = dt.getRandomDatetime(-4, 5)
            databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_AMOUNT1, date))
            i++
        }
        val recordList = databaseHandler.getAllSpendRecords(ExpenseDatabaseHandler.Period.LAST_DAY)

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
    fun testDeleteType() {

        val retVal = databaseHandler.addSpendRecord(SpendRecord(TEST_TYPE3, TEST_AMOUNT1, sqlDate1))

        databaseHandler.deleteSpendRecord(retVal)

        assertEquals(null, databaseHandler.getSpendRecord(retVal))
    }
}
