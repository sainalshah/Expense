package com.sas.sainal.expense

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import org.json.JSONArray
import java.lang.ref.WeakReference


class EditTypesActivity : AppCompatActivity() {


    private var recList: RecyclerView? = null
    val DEBUG_TAG = "EditTypesTag"
    private var typeExclusionList: JSONArray = JSONArray()
    private var databaseHandler: ExpenseDatabaseHandler? = null
    var adapter: TypesAdapter? = null

    companion object {

        class DbTypesAsyncAdapter(context: EditTypesActivity) : AsyncTask<String, Any, Any>() {

            private var activityWeakReference: WeakReference<EditTypesActivity>? = WeakReference(context)

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
                val activity = activityWeakReference?.get()
                val databaseHandler = activity?.getDatabaseHandle()
                when (action) {
                    Action.ShowAll -> init(databaseHandler!!.getAllSpendType(activity.typeExclusionList))
                    Action.Add -> {
                        databaseHandler!!.addSpendType(params[0])
                        activity.runOnUiThread({
                            activity.adapter?.addToTop(params[0])
                            activity.recList?.scrollToPosition(0)
                        })
                    }
                    else -> {
                        if (databaseHandler!!.deleteSpendType(params[0]) == ExpenseDatabaseHandler.SQL_ERROR) {
                            /*if sql error is returned, it means this type has dependency in records table
                            * so cannot be actually be deleted from db, but exclude it from showing in the available list*/
                            val exclList = activity.typeExclusionList
                            exclList.put(params[0])
                            activity.saveKeyValue(activity.getString(R.string.type_exclusion_pref_key), exclList.toString())
                        }
                    }
                }
            }

            private fun init(listSR: List<String>?) {
                val activity = activityWeakReference?.get()
                val recyclerView = activity?.recList

                if (listSR != null) {
                    activity?.runOnUiThread({
                        activity.adapter = TypesAdapter(listSR)
                        recyclerView?.adapter = activity.adapter
                    })
                } else {
                    activity?.runOnUiThread({
                        activity.findViewById<RecyclerView>(R.id.typeCardList)?.visibility = View.GONE
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

        typeExclusionList = JSONArray(getKeyValue(getString(R.string.type_exclusion_pref_key)))
        setupView()

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            // Click action
            val builder = AlertDialog.Builder(ContextThemeWrapper(this, R.style.DialogTheme)).setView(R.layout.new_type_content)
                    .setTitle(R.string.new_type_title)
            val dialog = builder.create()
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.add), { _, _ ->
                run {
                    var newType = dialog.findViewById<EditText>(R.id.new_type)?.text.toString()
                    if (newType.isNotEmpty()) {
                        newType = newType.substring(0, 1).toUpperCase() + newType.substring(1).toLowerCase()
                        DbTypesAsyncAdapter.addType(this@EditTypesActivity, newType)
                    }
                }
            })
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, getText(R.string.cancel), { _, _ ->
                run {}
            })
            dialog.show()
        }
    }

    private fun setupView() {
        val mLayoutManager = object : LinearLayoutManager(this) {
            override fun supportsPredictiveItemAnimations(): Boolean {
                return true
            }
        }
        recList?.layoutManager = mLayoutManager
        recList?.itemAnimator = DefaultItemAnimator()
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
        // Handle action bar itemTitle clicks here. The action bar will
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
