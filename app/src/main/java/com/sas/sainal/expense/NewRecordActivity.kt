package com.sas.sainal.expense

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.AdapterView.TEXT_ALIGNMENT_GRAVITY
import java.text.DateFormat
import java.text.SimpleDateFormat


/**
 * Created by sainal on 12/13/17.
 */
class NewRecordActivity : AppCompatActivity() {

    companion object {
        var databaseHandler: ExpenseDatabaseHandler? = MainActivity.databaseHandler
        val DEBUG_TAG = "NewRecordActivityTag"

        var typeField: Spinner? = null
        var amountField: EditText? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_record)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        populateTypeField()

        typeField = findViewById(R.id.record_type_field)
        amountField = findViewById(R.id.record_amount_field)

        val addBtn = findViewById<Button>(R.id.add_record_btn)
        addBtn.setOnClickListener {

            addNewRecord()
        }
    }

    private fun addNewRecord() {
        val newRecord = SpendRecord(typeField?.selectedItem.toString(),
                amountField?.text.toString().toDouble(), Datetime().getCurrentDatetime())
        databaseHandler?.addSpendRecord(newRecord)
        //close the activity
        finish()
    }

    private fun populateTypeField() {
        val dynamicSpinner = findViewById<Spinner>(R.id.record_type_field)

        val items = databaseHandler?.getAllSpendType()?.toTypedArray()

        val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, items)

        dynamicSpinner.adapter = adapter
        dynamicSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View,
                                        position: Int, id: Long) {
                Log.v(DEBUG_TAG, "${parent.getItemAtPosition(position)} is selected as type")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // TODO Auto-generated method stub
            }
        }
    }

}
