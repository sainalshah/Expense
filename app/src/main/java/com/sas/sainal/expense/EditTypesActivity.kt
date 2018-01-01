package com.sas.sainal.expense

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import java.lang.ref.WeakReference


class EditTypesActivity : AppCompatActivity() {


    private val TIME_TO_AUTOMATICALLY_DISMISS_ITEM = 3000
    private var recList: RecyclerView? = null
    val DEBUG_TAG = "ViewHistoryTag"
    private var databaseHandler: ExpenseDatabaseHandler? = null
    var adapter: TypesAdapter? = null

    companion object {

        class DbTypesAsyncAdapter(context: EditTypesActivity) : AsyncTask<String, Any, Any>() {

            private var activityReference: WeakReference<EditTypesActivity>? = WeakReference(context)

            companion object {

                enum class Action {
                    ShowAll, Delete, Add
                }

                var action: Action = Action.ShowAll
                fun showAllType(context: EditTypesActivity) {
                    action = Action.ShowAll
                    DbTypesAsyncAdapter(context).execute()
                }

                fun deleteType(context: EditTypesActivity, type: String) {
                    action = Action.Delete
                    DbTypesAsyncAdapter(context).execute(type)
                }

                fun addType(context: EditTypesActivity, type: String) {
                    action = Action.Add
                    DbTypesAsyncAdapter(context).execute(type)
                }
            }

            override fun doInBackground(vararg params: String) {
                val databaseHandler = activityReference?.get()?.getDatabaseHandle()
                when (action) {
                    Action.ShowAll -> init(databaseHandler!!.getAllSpendType())
                    Action.Add -> databaseHandler!!.addSpendType(params[0])
                    else -> databaseHandler!!.deleteSpendType(params[0])
                }
            }

            override fun onPostExecute(result: Any?) {
                super.onPostExecute(result)
                if(action == Action.Add){
                    activityReference?.get()?.updateEditTypePage()
                }
            }

            private fun init(listSR: List<String>?) {
                val recyclerView = activityReference?.get()?.recList

                if (listSR != null) {
                    activityReference?.get()?.runOnUiThread({
                        activityReference?.get()?.adapter = TypesAdapter(listSR)
                        recyclerView?.adapter = activityReference?.get()?.adapter
                    })
                } else {
                    activityReference?.get()?.runOnUiThread({
                        activityReference?.get()?.findViewById<RecyclerView>(R.id.typeCardList)?.visibility = View.GONE
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
        setContentView(R.layout.acitivity_edit_types)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        databaseHandler = ExpenseDatabaseHandler(this.applicationContext)

        recList = findViewById<View>(R.id.typeCardList) as RecyclerView

        setupView()

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            // Click action
            val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.DialogTheme)).setView(R.layout.new_type_content)
                    .setTitle(R.string.new_type_title)
            val dialog = builder.create()
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.add), { dialogInterface, i ->
                run {
                    var newType = dialog.findViewById<EditText>(R.id.new_type)?.text.toString()
                    if(newType!!.isNotEmpty()) {
                        newType = newType.substring(0, 1).toUpperCase() + newType.substring(1).toLowerCase()
                        DbTypesAsyncAdapter.addType(this@EditTypesActivity, newType)
                    }
                }
            })
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getText(R.string.cancel), { dialogInterface, i ->
                {}
            })
            dialog.show()
        }
    }

    private fun setupView() {
        val mLayoutManager = LinearLayoutManager(this)
        recList?.layoutManager = mLayoutManager
        recList?.addOnItemTouchListener(RecyclerItemClickListener(application, recList, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                // do whatever
                when {
                    view.id == R.id.delete_type_btn -> {
                        val type = adapter?.remove(position)
                        DbTypesAsyncAdapter.deleteType(this@EditTypesActivity, type!!)
                    }
                }
            }

            override fun onLongItemClick(view: View, position: Int) {
                // do whatever
            }
        }))
    }

    override fun onResume() {
        super.onResume()
        updateEditTypePage()
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
                val settings = Intent(this@EditTypesActivity, ViewSettingsActivity::class.java)
                startActivity(settings)
                true
            }
            R.id.db_manager -> {
                val dbmanager = Intent(this@EditTypesActivity, AndroidDatabaseManager::class.java)
                startActivity(dbmanager)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateEditTypePage() {
        DbTypesAsyncAdapter.showAllType(this)
    }


}
