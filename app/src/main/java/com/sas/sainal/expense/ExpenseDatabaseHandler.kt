package com.sas.sainal.expense

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat

/**
 * Created by sainal on 12/10/17.
 */

class ExpenseDatabaseHandler(context: Context) : SQLiteOpenHelper(context,
        ExpenseDatabaseHandler.DATABASE_NAME, null, ExpenseDatabaseHandler.DATABASE_VERSION) {

    enum class Table {
        type, records
    }

    // Kotlin does not allow static variables or functions
    companion object {
        val DATABASE_NAME = "record_db_kot"

        val DATABASE_VERSION = 1

        val RECORDS_TABLE_NAME = "records"

        //KEY followed by table name followed by attribute name
        val KEY_RECORDS_ID = "record_id"
        val KEY_RECORDS_TYPE = "record_type"
        val KEY_RECORDS_AMOUNT = "amount"
        val KEY_RECORDS_DATE = "date_of_creation"

        val TYPE_TABLE_NAME = "type"
        val KEY_TYPE_ID = "type_id"
        val KEY_TYPE_NAME = "type_name"

        val NOT_EXIST:Long = -2
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        // Create table with name records, a new column with name id which is INTEGER
        // type and it is primary key for the table, a new column with name
        // type and it is of type TEXT(String), a new column with name amount as
        // TEXT(String) type
        val CREATE_TYPE_TABLE_SCRIPT = "CREATE TABLE " + TYPE_TABLE_NAME +
                "(" +
                KEY_TYPE_ID + " INTEGER PRIMARY KEY," +
                KEY_TYPE_NAME + " TEXT" +
                ")"
        val CREATE_RECORDS_TABLE_SCRIPT = "CREATE TABLE " + RECORDS_TABLE_NAME +
                "(" +
                KEY_RECORDS_ID + " INTEGER PRIMARY KEY," +
                KEY_RECORDS_TYPE + " INTEGER," +
                KEY_RECORDS_AMOUNT + " TEXT," +
                KEY_RECORDS_DATE + " TEXT," +
                "FOREIGN KEY(" + KEY_RECORDS_TYPE + ") REFERENCES " + TYPE_TABLE_NAME + " (" + KEY_TYPE_ID + ")" +
                ")"
        sqLiteDatabase.execSQL(CREATE_TYPE_TABLE_SCRIPT)
        sqLiteDatabase.execSQL(CREATE_RECORDS_TABLE_SCRIPT)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        // Drop older record table if it exists
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + RECORDS_TABLE_NAME)

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
        values.put(KEY_RECORDS_TYPE, getTypeId(record.type)) // SpendRecord Title
        values.put(KEY_RECORDS_AMOUNT, record.amount) // SpendRecord Content
        values.put(KEY_RECORDS_DATE, record.date.toString())
        // Inserting new row into records table
        db.insert(RECORDS_TABLE_NAME, null, values)
        db.close() // Closing database connection
    }


    // Get Single SpendRecord
    fun getSpendRecord(id: Int): SpendRecord? {
        val db = this.readableDatabase
        var record: SpendRecord? = null

        // SQL query for getting a record from the database
        val selectQuery = "SELECT T1." + KEY_RECORDS_ID + ",T2." + KEY_TYPE_NAME +
                "T1." + KEY_RECORDS_AMOUNT + ",T1." + KEY_RECORDS_DATE + " FROM " + RECORDS_TABLE_NAME + "T1, " + TYPE_TABLE_NAME + "T2" +
                "WHERE T1." + KEY_RECORDS_TYPE + "=T2." + KEY_TYPE_ID + " AND T1." + KEY_RECORDS_ID + "=" + id


        val cursor = db.rawQuery(selectQuery, null)


        cursor.let {
            cursor.moveToFirst()
            record = SpendRecord(
                    cursor.getString(0).toInt(),
                    cursor.getString(1),
                    cursor.getString(2).toDouble(),
                    SimpleDateFormat("dd/MM/yyyy").parse(cursor.getString(3))
            )
        }

        // return record
        return record
    }

    // Get All SpendRecords
    fun getAllSpendRecords(): List<SpendRecord>? {

        // here this refers to SQLiteDatabase
        val db = this.readableDatabase

        // SQL query for getting all records from the database
        val selectQuery = "SELECT T1." + KEY_RECORDS_ID + ",T2." + KEY_TYPE_NAME +
                "T1." + KEY_RECORDS_AMOUNT + ",T1." + KEY_RECORDS_DATE + " FROM " + RECORDS_TABLE_NAME + "T1, " + TYPE_TABLE_NAME + "T2" +
                "WHERE T1." + KEY_RECORDS_TYPE + "=T2." + KEY_TYPE_ID


        val cursor = db.rawQuery(selectQuery, null)

        cursor.let {
            if (cursor.moveToFirst()) {
                var recordList = arrayListOf<SpendRecord>()
                do {
                    var record = SpendRecord(
                            cursor.getString(0).toInt(),
                            cursor.getString(1),
                            cursor.getString(2).toDouble(),
                            SimpleDateFormat("dd/MM/yyyy").parse(cursor.getString(3))
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
        values.put(KEY_RECORDS_TYPE, record.type)
        values.put(KEY_RECORDS_AMOUNT, record.amount)
        values.put(KEY_RECORDS_DATE, record.date.toString())

        // updating row
        return db.update(RECORDS_TABLE_NAME, values, KEY_RECORDS_TYPE + " = ?",
                arrayOf(record.type))
    }

    // Deleting single SpendRecord
    fun deleteSpendRecord(record: SpendRecord) {
        val db = this.writableDatabase
        db.delete(
                RECORDS_TABLE_NAME, // table name
                KEY_RECORDS_TYPE + " = ?", // selection
                arrayOf(record.type) // selectionArgs
        )
        db.close()
    }

    // Getting SpendRecords Count
    fun getSpendRecordsCount(table: Table): Int {
        val db = this.writableDatabase

        val countQuery = "SELECT  * FROM " + table.name

        val cursor = db.rawQuery(countQuery, null)
        val count = cursor.count

        cursor.close()
        db.close()

        // return count
        return count
    }

    fun getTypeId(typeName: String): Long {

        //if the type cannot be found, return this value
        var id: Long = NOT_EXIST
        // Get writable database
        val db = this.writableDatabase
        val projection = arrayOf(KEY_TYPE_ID, KEY_TYPE_NAME)


        // Filter results WHERE "column_name" = 'value'
        // here selection is column_name and
        // selectionArgs is value
        val selection = KEY_TYPE_NAME + "=?"
        val selectionArgs = arrayOf(typeName)

        // The order in which your result needs to be returned
        val sortOrder: String? = null // pass null if don't want it to be sorted

        // If passed 5, only 5 blogs will be returned
        val limit: String? = null // pass null if you dont want it to limit

        val cursor = db.query(
                TYPE_TABLE_NAME,        // The table to query
                projection,         // The columns to return
                selection,          // The columns for WHERE clause
                selectionArgs,      // The values for WHERE clause;
                null,        // don't group the rows
                null,         // don't filter
                sortOrder,          // The sort Order
                limit               // don't limit
        )

        cursor.let {
            if (cursor.moveToFirst()) {
                id = cursor.getString(0).toLong()
            }
        }
        return id
    }

    fun getAllSpendType():List<String>?{

        // here this refers to SQLiteDatabase
        val db = this.readableDatabase

        // SQL query for getting all records from the database
        val selectQuery = "SELECT  $KEY_TYPE_NAME FROM $TYPE_TABLE_NAME"
        val cursor = db.rawQuery(selectQuery, null)

        cursor.let {
            if (cursor.moveToFirst()) {
                var listOfType = arrayListOf<String>()
                do {
                    listOfType.add(cursor.getString(0))
                } while (cursor.moveToNext())

                cursor.close()

                db.close()
                return listOfType
            }
        }

        //return null if cursor return null
        return null
    }
    fun addSpendType(type: String): Long {
        var returnVal: Long = NOT_EXIST   //return -2 if type exists, return -1 for db error
        val db = this.writableDatabase
        // SQL query for getting all records from the database
        val selectQuery = "SELECT  * FROM $TYPE_TABLE_NAME WHERE $KEY_TYPE_NAME='$type'"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.count <= 0) {
            val values = ContentValues()
            values.put(KEY_TYPE_NAME, type)

            returnVal = db.insert(TYPE_TABLE_NAME, null, values)
        }
        db.close()
        return returnVal
    }

    fun deleteSpendType(type_name: String) {
        val db = this.writableDatabase
        db.delete(
                TYPE_TABLE_NAME, // table name
                "$KEY_TYPE_NAME = '$type_name'", // selection
                null// selectionArgs
        )
        db.close()
    }


    fun clearTable(table: Table) {
        val db = this.writableDatabase

        val delSql = "DELETE  FROM ${table.name}"

        db.execSQL(delSql)
        db.close()
    }
}