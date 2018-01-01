package com.sas.sainal.expense

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import java.lang.ref.WeakReference
import com.mynameismidori.currencypicker.CurrencyPickerListener
import com.mynameismidori.currencypicker.CurrencyPicker




class ViewSettingsActivity : AppCompatActivity() {


    private var recList: RecyclerView? = null
    val DEBUG_TAG = "ViewSettingsTag"
    private var databaseHandler: ExpenseDatabaseHandler? = null


    fun getDatabaseHandle(): ExpenseDatabaseHandler? {
        return databaseHandler
    }

    companion object {
        class InitializeDbAsync(context: ViewSettingsActivity) : AsyncTask<String, Any, Any>() {

            private var activityReference: WeakReference<ViewSettingsActivity>? = WeakReference(context)
            override fun doInBackground(vararg params: String) {
                val databaseHandler = activityReference?.get()?.getDatabaseHandle()
                databaseHandler?.clearTable(ExpenseDatabaseHandler.Table.RECORD)
                databaseHandler?.clearTable(ExpenseDatabaseHandler.Table.TYPE)

                for (type in ExpenseDatabaseHandler.ALL_TYPES) {
                    databaseHandler?.addSpendType(type)
                }
            }
        }
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
                when (position) {
                    0 -> {
                        CustomDialogFragment(this@ViewSettingsActivity, R.string.clear_db_confirm,
                                R.string.yes, R.string.cancel, ::success, ::fail)
                    }
                    1 -> {
                        val picker = CurrencyPicker.newInstance("Select Currency")  // dialog title
                        picker.setListener { name, code, symbol, flagDrawableResID ->
                            // Implement your code here
                            saveKeyValue(getString(R.string.currency_pref_key),name)
                            updateSettingsPage(name)
                            picker.dismiss()
                        }
                        picker.isCancelable = true
                        picker.show(supportFragmentManager, "CURRENCY_PICKER")
                    }
                }
            }

            private fun success() {
                InitializeDbAsync(this@ViewSettingsActivity).execute()
                initializeKeyValue(getString(R.string.type_exclusion_pref_key))
                Toast.makeText(applicationContext,"All records cleared",Toast.LENGTH_LONG).show()
            }

            private fun fail() {}

            override fun onLongItemClick(view: View, position: Int) {
                // do whatever
            }
        }))
        updateSettingsPage(getKeyValue(getString(R.string.currency_pref_key)))
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
        // Handle action bar itemTitle clicks here. The action bar will
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
            R.id.edit_types -> {
                val editTypes = Intent(this@ViewSettingsActivity, EditTypesActivity::class.java)
                startActivity(editTypes)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateSettingsPage(currency:String) {
        val settingsItem = resources.getStringArray(R.array.settings_item_list)
        val items = settingsItem.map { SettingsAdapter.SettingsItem(it) }
        recList?.adapter = SettingsAdapter(items, currency)
    }

    private fun initializeKeyValue(key: String) {
        saveKeyValue(key, "[]")
    }

    private fun saveKeyValue(key: String, valueJSON: String) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val editor = sharedPref.edit()
        editor.putString(key, valueJSON)
        editor.apply()
    }


    private fun getKeyValue(key: String): String {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val defaultValue = "[]"
        return sharedPref.getString(key, defaultValue)
    }
}
