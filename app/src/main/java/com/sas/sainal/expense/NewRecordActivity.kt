package com.sas.sainal.expense

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.widget.*
import java.lang.ref.WeakReference


/**
 * Created by sainal on 12/13/17.
 */
class NewRecordActivity : AppCompatActivity() {

    private var databaseHandler: ExpenseDatabaseHandler? = null

    private var typeField: Spinner? = null
    private var amountField: EditText? = null

    companion object {
        val DEBUG_TAG = "NewRecordActivityTag"
        val INTENT_KEY = "spending"
        val TYPE_SPENDING = "Spending"
        val TYPE_INCOME = "Balance"

        class PopulateTypeField(context: NewRecordActivity) : AsyncTask<String, Any, Array<String>?>() {
            private var activityReference: WeakReference<NewRecordActivity>? = WeakReference(context)
            override fun doInBackground(vararg params: String): Array<String>? {
                val databaseHandler = activityReference?.get()?.getDatabaseHandle()

                return if (params[0] == NewRecordActivity.TYPE_SPENDING) {
                    databaseHandler?.getAllSpendType()?.toTypedArray()
                } else {
                    arrayOf(ExpenseDatabaseHandler.SPECIAL_TYPE_INCOME)
                }
            }

            override fun onPostExecute(result: Array<String>?) {
                populateTypeField(result)
            }

            private fun populateTypeField(items: Array<String>?) {
                activityReference?.get()?.runOnUiThread({
                    val dynamicSpinner = activityReference?.get()?.findViewById<Spinner>(R.id.record_type_field)


                    val adapter = ArrayAdapter(activityReference?.get()?.application,
                            android.R.layout.simple_spinner_item, items)

                    dynamicSpinner?.adapter = adapter
                })

            }
        }

        class AddNewRecord(context: NewRecordActivity) : AsyncTask<String, Any, Any>() {
            private var activityReference: WeakReference<NewRecordActivity>? = WeakReference(context)
            override fun doInBackground(vararg params: String) {
                val databaseHandler = activityReference?.get()?.getDatabaseHandle()


                val newRecord = SpendRecord(params[0], params[1].toDouble(), Datetime().getCurrentDatetime())
                databaseHandler?.addSpendRecord(newRecord)
            }

            override fun onPostExecute(result: Any) {
                activityReference?.get()?.finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_record)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        databaseHandler = ExpenseDatabaseHandler(this.applicationContext)

        val type = intent.extras.get(INTENT_KEY) as String
        PopulateTypeField(this).execute(type)

        typeField = findViewById(R.id.record_type_field)
        amountField = findViewById(R.id.record_amount_field)
        val addBtn = findViewById<Button>(R.id.add_record_btn)
        if (type == TYPE_INCOME) {
            val label = getText(R.string.add_income_text)
            addBtn.text = label
            title = label
        }
        addBtn.setOnClickListener {

            addNewRecord()
        }
    }

    fun getDatabaseHandle(): ExpenseDatabaseHandler? {
        return databaseHandler
    }

    private fun addNewRecord() {
        val type = typeField?.selectedItem.toString()
        val amtTxt = amountField?.text.toString()
        if (amtTxt.isNotEmpty()) {
            AddNewRecord(this).execute(type, amtTxt)
        } else {
            Toast.makeText(applicationContext, R.string.amount_empty_error, Toast.LENGTH_LONG).show()
        }
    }


}
