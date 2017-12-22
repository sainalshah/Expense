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
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {


    private var recList: RecyclerView? = null
    private var balanceCard: RecyclerView? = null
    val DEBUG_TAG = "MainActivityTag"
    private var databaseHandler: ExpenseDatabaseHandler? = null

    companion object {

        class GetPeriodSpendingSum(context: MainActivity) : AsyncTask<Any, Any, Array<Double>>() {
            private var activityReference: WeakReference<MainActivity>? = WeakReference(context)
            override fun doInBackground(vararg params: Any): Array<Double> {
                val databaseHandler = activityReference?.get()?.getDatabaseHandle()
                val weeklyAmt = databaseHandler!!.getPeriodSpendingSum(ExpenseDatabaseHandler.Period.LAST_WEEK)
                val montlyAmt = databaseHandler!!.getPeriodSpendingSum(ExpenseDatabaseHandler.Period.LAST_WEEK)

                return arrayOf(weeklyAmt, montlyAmt)
            }

            override fun onPostExecute(result: Array<Double>) {
                updateRecyclerView(result[0], result[1])
            }

            private fun updateRecyclerView(weeklyAmount: Double, monthlyAmount: Double) {
                val weekly = String.format("%.2f", weeklyAmount).toDouble()
                val monthly = String.format("%.2f", monthlyAmount).toDouble()
                val summaryAdapter = SummaryAdapter(listOf(
                        SummaryInfo(activityReference!!.get()!!.getString(R.string.weekly_label), weekly),
                        SummaryInfo(activityReference!!.get()!!.getString(R.string.monthly_label), monthly)),
                        SummaryAdapter.TYPE.SPENDING)
                val balanceAdapter = SummaryAdapter(listOf(SummaryInfo(activityReference!!.get()!!.getString(R.string.balance_label), weekly)),
                        SummaryAdapter.TYPE.BALANCE)
                activityReference?.get()?.runOnUiThread(Runnable {
                    activityReference?.get()?.recList?.adapter = summaryAdapter
                    activityReference?.get()?.balanceCard?.adapter = balanceAdapter
                })
            }
        }

        class SetupDB(context: MainActivity) : AsyncTask<String, Any, Any>() {
            private var activityReference: WeakReference<MainActivity>? = WeakReference(context)
            override fun doInBackground(vararg params: String) {
                val databaseHandler = activityReference?.get()?.getDatabaseHandle()

                //don't setup type table if at least SPECIAL_TYPE_INCOME exists.
                if (databaseHandler?.getTypeId(ExpenseDatabaseHandler.SPECIAL_TYPE_INCOME)
                        == ExpenseDatabaseHandler.ERROR_NOT_EXIST) {
                    for (type in params) {
                        databaseHandler?.addSpendType(type)
                    }
                }
            }
        }
    }

    fun getDatabaseHandle(): ExpenseDatabaseHandler? {
        return databaseHandler
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

        recList = findViewById<View>(R.id.spendingCardList) as RecyclerView
        recList?.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        recList?.layoutManager = llm

        balanceCard = findViewById<View>(R.id.balanceCard) as RecyclerView
        balanceCard?.setHasFixedSize(true)
        val balanceLlm = LinearLayoutManager(this)
        balanceLlm.orientation = LinearLayoutManager.VERTICAL
        balanceCard?.layoutManager = balanceLlm

        updateHomePage()
    }

    override fun onDestroy() {
        super.onDestroy()
        recList = null
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

        when (id) {
            R.id.action_settings -> return true
            R.id.db_manager -> {
                val dbmanager = Intent(this@MainActivity, AndroidDatabaseManager::class.java)
                startActivity(dbmanager)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun updateHomePage() {
        GetPeriodSpendingSum(this).execute()
    }


    private fun setupDB() {

        val type1 = "shopping"
        val type2 = "clothing"
        val type3 = "grocery"

        SetupDB(this).execute(ExpenseDatabaseHandler.SPECIAL_TYPE_INCOME, type1, type2, type3)

    }


}
