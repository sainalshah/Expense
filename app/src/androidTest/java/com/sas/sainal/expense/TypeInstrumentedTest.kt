package com.sas.sainal.expense

import android.content.Context
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class TypeInstrumentedTest {

    companion object {
        val TEST_TYPE1: String = "shopping"
        val TEST_TYPE2: String = "clothing"
        val TEST_TYPE3: String = "grocery"

        private val context: Context = InstrumentationRegistry.getTargetContext()
        val databaseHandler = ExpenseDatabaseHandler(context)
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.sas.sainal.expense", appContext.packageName)
    }

    @Before
    fun setup() {
        databaseHandler.clearTable(ExpenseDatabaseHandler.Table.TYPE)
    }

    @Test
    fun testCreateNewType() {
        assertEquals(1, databaseHandler.addSpendType(TEST_TYPE2))
    }

    @Test
    fun testInitialCountType() {
        assertEquals(0, databaseHandler.getCount(ExpenseDatabaseHandler.Table.TYPE))
    }

    @Test
    fun testCountAfterNewType() {
        databaseHandler.addSpendType(TEST_TYPE3)
        databaseHandler.addSpendType(TEST_TYPE1)
        assertEquals(2, databaseHandler.getCount(ExpenseDatabaseHandler.Table.TYPE))
    }
    @Test
    fun testDuplicateType() {
        databaseHandler.addSpendType(TEST_TYPE3)
        assertEquals(ExpenseDatabaseHandler.ERROR_EXIST, databaseHandler.addSpendType(TEST_TYPE3))
    }

    @Test
    fun testGetAllType(){
        val expectedList = arrayListOf(TEST_TYPE1, TEST_TYPE2, TEST_TYPE3)
        databaseHandler.addSpendType(TEST_TYPE3)
        databaseHandler.addSpendType(TEST_TYPE1)
        databaseHandler.addSpendType(TEST_TYPE2)

        //Make sure we're operating only on non null data
        val actualList = databaseHandler.getAllSpendType() ?: arrayListOf() //Elvis operator

        assert(actualList.containsAll(expectedList))
    }

    @Test
    fun testGetTypeId(){
        databaseHandler.addSpendType(TEST_TYPE3)
        assertEquals(databaseHandler.addSpendType(TEST_TYPE2), databaseHandler.getTypeId(TEST_TYPE2))
    }

    @Test
    fun testDeleteType(){
        databaseHandler.addSpendType(TEST_TYPE3)
        databaseHandler.addSpendType(TEST_TYPE1)

        databaseHandler.deleteSpendType(TEST_TYPE3)

        assertEquals(-2, databaseHandler.getTypeId(TEST_TYPE3))
    }
}
