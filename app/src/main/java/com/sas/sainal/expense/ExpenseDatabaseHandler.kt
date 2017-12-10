package com.sas.sainal.expense

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by sainal on 12/10/17.
 */

class SpendRecordsDatabaseHandler(context: Context): SQLiteOpenHelper(context,
        SpendRecordsDatabaseHandler.DATABASE_NAME, null, SpendRecordsDatabaseHandler.DATABASE_VERSION) {

    // Kotlin does not allow static variables or functions
    companion object {
        val DATABASE_NAME = "record_db_kot"

        val DATABASE_VERSION = 1

        val TABLE_BLOGS = "records_kotlin"

        val KEY_ID = "id"
        val KEY_TITLE = "title"
        val KEY_CONTENT = "content"
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        // Create table with name records, a new column with name id which is INTEGER
        // type and it is primary key for the table, a new column with name
        // title and it is of type TEXT(String), a new column with name content as
        // TEXT(String) type
        val CREATE_BLOGS_TABLE = "CREATE TABLE " + TABLE_BLOGS +
                "(" +
                KEY_ID + " INTEGER PRIMARY KEY," +
                KEY_TITLE + " TEXT," +
                KEY_CONTENT + " TEXT" +
                ")"

        sqLiteDatabase.execSQL(CREATE_BLOGS_TABLE)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        // Drop older record table if it exists
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_BLOGS)

        // call onCreate method so it creates new table again
        onCreate(sqLiteDatabase)
    }

    /**
     * CRUD Operations (create, Read, Update, Delete)
     */

    // Adding new SpendRecord
    fun addSpendRecord(record: SpendRecord) {
        // Get writable database
        val db = this.writableDatabase

        val values = ContentValues()
        values.put(KEY_TITLE, record.title) // SpendRecord Title
        values.put(KEY_CONTENT, record.content) // SpendRecord Content

        // Inserting new row into records table
        db.insert(TABLE_BLOGS, null, values)
        db.close() // Closing database connection
    }

    // Get Single SpendRecord
    fun getSpendRecord(id: Int): SpendRecord? {
        val db = this.readableDatabase
        var record: SpendRecord? = null

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        val projection = arrayOf(KEY_ID, KEY_TITLE, KEY_CONTENT)


        // Filter results WHERE "column_name" = 'value'
        // here selection is column_name and
        // selectionArgs is value
        val selection = KEY_ID + "=?"
        val selectionArgs = arrayOf(id.toString())

        // The order in which your result needs to be returned
        val sortOrder: String? = null // pass null if don't want it to be sorted

        // If passed 5, only 5 records will be returned
        val limit: String? = null // pass null if you dont want it to limit

        val cursor = db.query(
                TABLE_BLOGS,        // The table to query
                projection,         // The columns to return
                selection,          // The columns for WHERE clause
                selectionArgs,      // The values for WHERE clause;
                null,        // don't group the rows
                null,         // don't filter
                sortOrder,          // The sort Order
                limit               // don't limit
        )

        cursor.let {
            cursor.moveToFirst()
            record = SpendRecord(
                    cursor.getString(0).toInt(),
                    cursor.getString(1),
                    cursor.getString(2)
            )
        }

        // return record
        return record
    }

    // Get All SpendRecords
    fun getAllSpendRecords(): List? {

        // here this refers to SQLiteDatabase
        val db = this.readableDatabase

        // SQL query for getting all records from the database
        val selectQuery = "SELECT  * FROM " + TABLE_BLOGS


        val cursor = db.rawQuery(selectQuery, null)

        cursor.let {
            if (cursor.moveToFirst()) {
                var recordList = arrayListOf()
                do {
                    var record = SpendRecord(
                            cursor.getString(0).toInt(),
                            cursor.getString(1),
                            cursor.getString(2)
                    )
                    // Adding SpendRecord to list
                    recordList.add(record)
                } while (cursor.moveToNext())

                cursor.close()
                db.close()
                // return recordlist if there is records in database
                return recordList
            }
        }
        //return null if the cursor was null
        return null
    }

    // Updating single record
    fun updateSpendRecord(record: SpendRecord): Int {
        val db = this.writableDatabase

        // New values
        val values = ContentValues()
        values.put(KEY_TITLE, record.title)
        values.put(KEY_CONTENT, record.content)

        // updating row
        return db.update(TABLE_BLOGS, values, KEY_TITLE + " = ?",
                arrayOf(record.title))
    }

    // Deleting single SpendRecord
    fun deleteSpendRecord(record: SpendRecord) {
        val db = this.writableDatabase
        db.delete(
                TABLE_BLOGS, // table name
                KEY_TITLE + " = ?", // selection
                arrayOf(record.title) // selectionArgs
        )
        db.close()
    }

    // Getting SpendRecords Count
    fun getSpendRecordsCount(): Int {
        val db = this.writableDatabase

        val countQuery = "SELECT  * FROM " + TABLE_BLOGS

        val cursor = db.rawQuery(countQuery, null)
        val count = cursor.count

        cursor.close()
        db.close()

        // return count
        return count
    }
}