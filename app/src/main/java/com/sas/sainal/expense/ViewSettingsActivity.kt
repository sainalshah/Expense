package com.sas.sainal.expense

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import java.lang.ref.WeakReference


class ViewSettingsActivity : AppCompatActivity() {


    private var recList: RecyclerView? = null
    val DEBUG_TAG = "ViewSettingsTag"
    private var databaseHandler: ExpenseDatabaseHandler? = null


    fun getDatabaseHandle(): ExpenseDatabaseHandler? {
        return databaseHandler
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_settings)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        databaseHandler = ExpenseDatabaseHandler(this.applicationContext)

        recList = findViewById<View>(R.id.settingsCardList) as RecyclerView
        recList?.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        recList?.layoutManager = llm

        recList?.addOnItemTouchListener(RecyclerItemClickListener(application, recList, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                // do whatever
                when {
                    position == 0 -> {
                    }
                }
            }

            override fun onLongItemClick(view: View, position: Int) {
                // do whatever
            }
        }))
        updateSettingsPage()
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
            R.id.view_history -> {
                val viewHistory = Intent(this@ViewSettingsActivity, ViewHistoryActivity::class.java)
                startActivity(viewHistory)
                true
            }
            R.id.db_manager -> {
                val dbManager = Intent(this@ViewSettingsActivity, AndroidDatabaseManager::class.java)
                startActivity(dbManager)
                true
            }
            R.id.edit_types ->{
                val editTypes = Intent(this@ViewSettingsActivity, EditTypesActivity::class.java)
                startActivity(editTypes)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateSettingsPage() {
        val items = listOf(SettingsAdapter.SettingsItem("Clear All"))
        recList?.adapter = SettingsAdapter(items)
    }


}
