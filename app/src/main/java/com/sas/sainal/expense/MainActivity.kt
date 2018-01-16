package com.sas.sainal.expense

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import java.lang.ref.WeakReference
import com.mynameismidori.currencypicker.ExtendedCurrency




class MainActivity : AppCompatActivity() {

    private var mInterstitialAd:InterstitialAd? = null
    private var recList: RecyclerView? = null
    val DEBUG_TAG = "MainActivityTag"
    private var databaseHandler: ExpenseDatabaseHandler? = null

    companion object {

        class GetPeriodSpendingSum(context: MainActivity) : AsyncTask<Any, Any, Array<Double>>() {
            private var activityReference: WeakReference<MainActivity>? = WeakReference(context)
            override fun doInBackground(vararg params: Any): Array<Double> {
                val databaseHandler = activityReference?.get()?.getDatabaseHandle()
                val weeklyAmt = databaseHandler!!.getPeriodSpendingSum(ExpenseDatabaseHandler.Period.LAST_WEEK)
                val monthlyAmt = databaseHandler.getPeriodSpendingSum(ExpenseDatabaseHandler.Period.LAST_WEEK)
                val balance = databaseHandler.getBalance()
                return arrayOf(weeklyAmt, monthlyAmt, balance)
            }

            override fun onPostExecute(result: Array<Double>) {
                updateRecyclerView(result[0], result[1], result[2])
            }

            private fun updateRecyclerView(weeklyAmount: Double, monthlyAmount: Double, balanceAmount: Double) {
                val weekly = String.format(SpendRecord.AMOUNT_FORMAT, weeklyAmount)
                val monthly = String.format(SpendRecord.AMOUNT_FORMAT, monthlyAmount)
                val balance = String.format(SpendRecord.AMOUNT_FORMAT, balanceAmount)
                val summaryAdapter = SummaryAdapter(listOf(
                        SummaryAdapter.SummaryInfo(activityReference!!.get()!!.getString(R.string.weekly_label), weekly),
                        SummaryAdapter.SummaryInfo(activityReference!!.get()!!.getString(R.string.monthly_label), monthly),
                        SummaryAdapter.SummaryInfo(activityReference!!.get()!!.getString(R.string.balance_label), balance)),
                        activityReference?.get()!!)
                activityReference?.get()?.runOnUiThread({
                    activityReference?.get()?.recList?.adapter = summaryAdapter
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
                    for (type in ExpenseDatabaseHandler.ALL_TYPES) {
                        databaseHandler.addSpendType(type)
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
            val intent = Intent(this@MainActivity,
                    NewRecordActivity::class.java).putExtra(NewRecordActivity.INTENT_ACTION_KEY, NewRecordActivity.TYPE_SPENDING)
            startActivity(intent)
        }

        databaseHandler = ExpenseDatabaseHandler(this.applicationContext)
        setupDB()

        recList = findViewById<View>(R.id.spendingCardList) as RecyclerView
        recList?.setHasFixedSize(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
        recList?.layoutManager = llm

        setupCurrency(getKeyValue(getString(R.string.currency_pref_key)))
        updateHomePage()

        MobileAds.initialize(this, getString(R.string.ad_app_id))
        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd!!.adUnitId = getString(R.string.test_interstitial_ad_unit_id)
        mInterstitialAd!!.loadAd(AdRequest.Builder().build())
    }

    override fun onBackPressed() {
        if (mInterstitialAd!!.isLoaded) {
            mInterstitialAd!!.show()
        }else {
            Log.d(DEBUG_TAG, "The interstitial wasn't loaded yet.");
        }
        super.onBackPressed()
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
        // Handle action bar itemTitle clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        return when (id) {
            R.id.action_settings -> {
                val settings = Intent(this@MainActivity, ViewSettingsActivity::class.java)
                startActivity(settings)
                true
            }
            R.id.db_manager -> {
                val dbManager = Intent(this@MainActivity, AndroidDatabaseManager::class.java)
                startActivity(dbManager)
                true
            }
            R.id.view_history -> {
                val viewHistory = Intent(this@MainActivity, ViewHistoryActivity::class.java)
                startActivity(viewHistory)
                true
            }
            R.id.edit_types -> {
                val editTypes = Intent(this@MainActivity, EditTypesActivity::class.java)
                startActivity(editTypes)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateHomePage() {
        GetPeriodSpendingSum(this).execute()
    }


    private fun setupDB() {
        SetupDB(this).execute()
    }

    private fun setupCurrency(currencyName:String) {
        val currency = ExtendedCurrency.getCurrencyByName(currencyName)
        SpendRecord.updateCurrencySymbol(currency.symbol)
    }
    private fun getKeyValue(key: String): String {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val defaultValue = if( key == getString(R.string.currency_pref_key)) SpendRecord.DEFAULT_CURRENCY_NAME else "[]"
        return sharedPref.getString(key, defaultValue)
    }
}
