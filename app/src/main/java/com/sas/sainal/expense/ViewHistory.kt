package com.sas.sainal.expense

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import java.lang.ref.WeakReference


class ViewHistory : AppCompatActivity() {


    private var recList: RecyclerView? = null
    val DEBUG_TAG = "ViewHistoryTag"
    private var databaseHandler: ExpenseDatabaseHandler? = null

    companion object {

        class ShowHistory(context: ViewHistory) : AsyncTask<Any, Any, List<SpendRecord>?>() {
            private var activityReference: WeakReference<ViewHistory>? = WeakReference(context)
            override fun doInBackground(vararg params: Any): List<SpendRecord>? {
                val databaseHandler = activityReference?.get()?.getDatabaseHandle()
                return databaseHandler!!.getPeriodSpendRecord(ExpenseDatabaseHandler.Period.ALL)
            }

            override fun onPostExecute(result: List<SpendRecord>?) {
                updateRecyclerView(result)
            }

            private fun updateRecyclerView(listSR: List<SpendRecord>?) {
                if (listSR != null) {
                    val historyAdapter = HistoryAdapter(listSR)
                    activityReference?.get()?.runOnUiThread({
                        activityReference?.get()?.recList?.adapter = historyAdapter
                        activityReference?.get()?.findViewById<TextView>(R.id.history_empty_txt_iew)?.visibility = View.GONE
                    })
                }
            }
        }
    }

    fun getDatabaseHandle(): ExpenseDatabaseHandler? {
        return databaseHandler
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_history)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        databaseHandler = ExpenseDatabaseHandler(this.applicationContext)

        recList = findViewById<View>(R.id.historyCardList) as RecyclerView
        recList?.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        recList?.layoutManager = llm

        updateHomePage()
    }

    override fun onDestroy() {
        super.onDestroy()
        recList = null
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

        return when (id) {
            R.id.action_settings -> true
            R.id.db_manager -> {
                val dbmanager = Intent(this@ViewHistory, AndroidDatabaseManager::class.java)
                startActivity(dbmanager)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateHomePage() {
        ShowHistory(this).execute()
    }


}
