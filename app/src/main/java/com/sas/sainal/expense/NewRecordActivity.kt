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
    private var addBtn: Button? = null

    private var record: SpendRecord? = null
    companion object {
        val DEBUG_TAG = "NewRecordActivityTag"
        val INTENT_ACTION_KEY = "spending"
        val TYPE_SPENDING = "Spending"
        val TYPE_INCOME = "Balance"

        val TYPE_EDIT_SPENDING = "Edit"
        val TYPE_SPENDING_OBJECT = "Spending_object"

        class PopulateTypeField(context: NewRecordActivity) : AsyncTask<String, Any, Array<String>?>() {

            private var record: SpendRecord? = null

            constructor(context: NewRecordActivity, record: SpendRecord) : this(context) {
                this.record = record
            }


            private var activityReference: WeakReference<NewRecordActivity>? = WeakReference(context)
            override fun doInBackground(vararg params: String): Array<String>? {
                val databaseHandler = activityReference?.get()?.getDatabaseHandle()

                return if (params[0] == TYPE_SPENDING || params[0] == TYPE_EDIT_SPENDING) {
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
                    val dynamicSpinner = activityReference?.get()?.typeField
                    val amtField = activityReference?.get()?.amountField


                    val adapter = ArrayAdapter(activityReference?.get()?.application,
                            android.R.layout.simple_spinner_item, items)

                    dynamicSpinner?.adapter = adapter
                    if (record != null) {
                        activityReference?.get()?.record = record
                        dynamicSpinner?.setSelection(getStringPosition(items!!, record!!.type))
                        amtField?.setText(record!!.amount.toString())
                    }
                })

            }

            private fun getStringPosition(typeValues: Array<String>, item: String): Int {
                val n = typeValues.size
                for (i in 0 until n) {
                    val value = typeValues[i]
                    if (value == item)
                        return i
                }
                return -1
            }
        }

        class AddNewRecord(context: NewRecordActivity) : AsyncTask<String, Any, Any>() {
            private var activityReference: WeakReference<NewRecordActivity>? = WeakReference(context)

            enum class Action {
                Add, Edit
            }

            companion object {
                private var action = AddNewRecord.Action.Add
                fun addRecord(context: NewRecordActivity, type: String, amt: String, date: String) {
                    action = AddNewRecord.Action.Add
                    AddNewRecord(context).execute(type, amt, date)
                }

                fun editRecord(context: NewRecordActivity, id: String, type: String, amt: String, date: String) {
                    action = AddNewRecord.Action.Edit
                    AddNewRecord(context).execute(id, type, amt, date)
                }
            }

            override fun doInBackground(vararg params: String) {
                val databaseHandler = activityReference?.get()?.getDatabaseHandle()

                if (action == Action.Add) {
                    val newRecord = SpendRecord(params[0], params[1].toDouble(), Datetime().getCurrentDatetime())
                    databaseHandler?.addSpendRecord(newRecord)
                } else {
                    val newRecord = SpendRecord(params[0].toLong(), params[1], params[2].toDouble(), Datetime().getCurrentDatetime())
                    databaseHandler?.updateSpendRecord(newRecord)
                }
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

        val type = intent.extras.get(INTENT_ACTION_KEY) as String

        typeField = findViewById(R.id.record_type_field)
        amountField = findViewById(R.id.record_amount_field)
        addBtn = findViewById(R.id.add_record_btn)
        val tempAddBtn = addBtn as Button
        when (type) {
            TYPE_INCOME -> {
                val label = getText(R.string.add_income_text)
                tempAddBtn.text = label
                title = label
                PopulateTypeField(this).execute(type)
            }
            TYPE_EDIT_SPENDING -> {
                val label = getText(R.string.edit_spending_text)
                tempAddBtn.text = label
                title = label

                val record: SpendRecord = intent.extras.getParcelable(TYPE_SPENDING_OBJECT)
                PopulateTypeField(this, record).execute(type)
            }
            else -> //populate all type
                PopulateTypeField(this).execute(type)
        }
        tempAddBtn.setOnClickListener {

            doAction()
        }
    }

    fun getDatabaseHandle(): ExpenseDatabaseHandler? {
        return databaseHandler
    }


    private fun doAction() {
        val type = typeField?.selectedItem.toString()
        val amtTxt = amountField?.text.toString()
        if (amtTxt.isNotEmpty()) {
            if (addBtn?.text == getText(R.string.edit_spending_text)) {
                AddNewRecord.editRecord(this, record?.id.toString(),type, amtTxt, Datetime().getCurrentDatetime())
            } else {
                AddNewRecord.addRecord(this, type, amtTxt, Datetime().getCurrentDatetime())
            }
        } else {
            Toast.makeText(applicationContext, R.string.amount_empty_error, Toast.LENGTH_LONG).show()
        }
    }


}
