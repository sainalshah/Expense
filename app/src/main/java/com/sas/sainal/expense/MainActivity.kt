package com.sas.sainal.expense

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View


class MainActivity : AppCompatActivity() {
    companion object {
        var databaseHandler: ExpenseDatabaseHandler? = null
        val DEBUG_TAG = "MainActivityTag"
        var recList: RecyclerView? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            // Click action
            val intent = Intent(this@MainActivity, NewRecordActivity::class.java)
            startActivity(intent)
        }

        databaseHandler = ExpenseDatabaseHandler(this.applicationContext)
        setupDB()

        recList = findViewById<View>(R.id.cardList) as RecyclerView
        recList?.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        recList?.layoutManager = llm

        updateHomePage()
    }


    override fun onResume() {
        super.onResume()
        updateHomePage()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }

    private fun updateHomePage() {
        GetPeriodSpendingSum().execute()
    }

    fun updateRecyclerView(weeklyAmount: Double, monthlyAmount: Double) {
        val summaryAdapter = SummaryAdapter(listOf(SummaryInfo("Weekly expense", weeklyAmount), SummaryInfo("Monthly expense", monthlyAmount)))
        recList?.adapter = summaryAdapter
    }

    private fun setupDB() {

        val TEST_TYPE1: String = "shopping"
        val TEST_TYPE2: String = "clothing"
        val TEST_TYPE3: String = "grocery"
        databaseHandler?.addSpendType(TEST_TYPE3)
        databaseHandler?.addSpendType(TEST_TYPE1)
        databaseHandler?.addSpendType(TEST_TYPE2)
    }

    private  inner class GetPeriodSpendingSum : AsyncTask<Any, Any, Array<Double>>() {
        var databaseHandler = MainActivity.databaseHandler

        override fun doInBackground(vararg params: Any): Array<Double> {
            val weeklyAmt = databaseHandler!!.getPeriodSpendingSum(ExpenseDatabaseHandler.Period.LAST_WEEK)
            val montlyAmt = databaseHandler!!.getPeriodSpendingSum(ExpenseDatabaseHandler.Period.LAST_WEEK)

            return arrayOf(weeklyAmt, montlyAmt)
        }

        override fun onPostExecute(result: Array<Double>) {
            updateRecyclerView(result[0], result[1])
        }
    }
}
