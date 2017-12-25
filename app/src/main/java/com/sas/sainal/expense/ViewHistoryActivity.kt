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
import android.widget.Toast
import com.hudomju.swipe.OnItemClickListener
import com.hudomju.swipe.SwipeToDismissTouchListener
import com.hudomju.swipe.SwipeableItemClickListener
import com.hudomju.swipe.adapter.RecyclerViewAdapter
import java.lang.ref.WeakReference


class ViewHistoryActivity : AppCompatActivity() {


    private val TIME_TO_AUTOMATICALLY_DISMISS_ITEM = 3000
    private var recList: RecyclerView? = null
    val DEBUG_TAG = "ViewHistoryTag"
    private var databaseHandler: ExpenseDatabaseHandler? = null
    var adapter: HistoryAdapter? = null

    companion object {

        class DbHistoryAsyncAdapter(context: ViewHistoryActivity) : AsyncTask<String, Any, Any>() {

            private var activityReference: WeakReference<ViewHistoryActivity>? = WeakReference(context)

            companion object {

                enum class Action {
                    ShowAll, Delete
                }

                var action: Action = Action.ShowAll
                fun showAllRecord(context: ViewHistoryActivity) {
                    action = Action.ShowAll
                    DbHistoryAsyncAdapter(context).execute()
                }

                fun deleteRecord(context: ViewHistoryActivity, id: Long) {
                    action = Action.Delete
                    DbHistoryAsyncAdapter(context).execute(id.toString())
                }
            }

            override fun doInBackground(vararg params: String) {
                val databaseHandler = activityReference?.get()?.getDatabaseHandle()
                if (action == Action.ShowAll) {
                    init(databaseHandler!!.getPeriodSpendRecord(ExpenseDatabaseHandler.Period.ALL))
                } else {
                    databaseHandler!!.deleteSpendRecord(params[0].toLong())
                }
            }

            private fun init(listSR: List<SpendRecord>?) {
                val recyclerView = activityReference?.get()?.recList

                if (listSR != null) {
                    activityReference?.get()?.runOnUiThread({
                        activityReference?.get()?.adapter = HistoryAdapter(listSR)
                        recyclerView?.adapter = activityReference?.get()?.adapter
                    })
                } else {
                    activityReference?.get()?.runOnUiThread({
                        activityReference?.get()?.findViewById<RecyclerView>(R.id.historyCardList)?.visibility = View.GONE
                        activityReference?.get()?.findViewById<TextView>(R.id.history_empty_txt_iew)?.visibility = View.VISIBLE
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

        setupView()
    }

    private fun setupView() {
        val mLayoutManager = LinearLayoutManager(this)
        recList?.layoutManager = mLayoutManager
        val touchListener = SwipeToDismissTouchListener(
                RecyclerViewAdapter(recList),
                object : SwipeToDismissTouchListener.DismissCallbacks<RecyclerViewAdapter> {
                    override fun canDismiss(position: Int): Boolean {
                        return true
                    }

                    override fun onPendingDismiss(recList: RecyclerViewAdapter, position: Int) {

                    }

                    override fun onDismiss(view: RecyclerViewAdapter, position: Int) {
                        val id = adapter?.remove(position)
                        DbHistoryAsyncAdapter.deleteRecord(this@ViewHistoryActivity, id!!)
                    }
                })
        touchListener.setDismissDelay(TIME_TO_AUTOMATICALLY_DISMISS_ITEM.toLong())
        recList?.setOnTouchListener(touchListener)
        // Setting this scroll listener is required to ensure that during ListView scrolling,
        // we don't look for swipes.
        recList?.setOnScrollListener(touchListener.makeScrollListener() as RecyclerView.OnScrollListener)
        recList?.addOnItemTouchListener(SwipeableItemClickListener(this,
                OnItemClickListener { view, position ->
                    when {
                        view.id == R.id.txt_delete -> touchListener.processPendingDismisses()
                        view.id == R.id.txt_undo -> touchListener.undoPendingDismiss()
                        else -> {

                            val intent = Intent(this, NewRecordActivity::class.java).putExtra(NewRecordActivity.INTENT_ACTION_KEY, NewRecordActivity.TYPE_EDIT_SPENDING)
                                    .putExtra(NewRecordActivity.TYPE_SPENDING_OBJECT, adapter?.getItem(position))
                            startActivity(intent)
                        }
                    }
                }))
    }

    override fun onResume() {
        super.onResume()
        updateHistoryPage()
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
            R.id.action_settings -> {
                val settings = Intent(this@ViewHistoryActivity, ViewSettingsActivity::class.java)
                startActivity(settings)
                true
            }
            R.id.db_manager -> {
                val dbmanager = Intent(this@ViewHistoryActivity, AndroidDatabaseManager::class.java)
                startActivity(dbmanager)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateHistoryPage() {
        DbHistoryAsyncAdapter.showAllRecord(this)
    }


}
