package com.sas.sainal.expense

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.content.Intent
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.view.View


class MainActivity : AppCompatActivity() {
    companion object {
        var databaseHandler: ExpenseDatabaseHandler? = null
        val DEBUG_TAG = "MainActivityTag"
        private val TOAST_TEXT = """Test ads are being shown. " + "To show live ads, replace the ad
            |unit ID in res/values/strings.xml with your own ad unit ID.""".trimMargin()

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener{
                // Click action
                val intent = Intent(this@MainActivity, NewRecordActivity::class.java)
                startActivity(intent)
        }

        databaseHandler = ExpenseDatabaseHandler(this.applicationContext)
        setup_db()

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

        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }

    fun setup_db(){

        val TEST_TYPE1: String = "shopping"
        val TEST_TYPE2: String = "clothing"
        val TEST_TYPE3: String = "grocery"
        databaseHandler?.addSpendType(TEST_TYPE3)
        databaseHandler?.addSpendType(TEST_TYPE1)
        databaseHandler?.addSpendType(TEST_TYPE2)
    }


}
