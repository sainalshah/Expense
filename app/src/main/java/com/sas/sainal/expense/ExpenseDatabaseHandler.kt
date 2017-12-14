package com.sas.sainal.expense

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Created by sainal on 12/10/17.
 */

class ExpenseDatabaseHandler(context: Context) : SQLiteOpenHelper(context,
        ExpenseDatabaseHandler.DATABASE_NAME, null, ExpenseDatabaseHandler.DATABASE_VERSION) {

    enum class Table {
        TYPE, RECORD
    }

    enum class Key {
        //Records table attribute keys
        RECORD_ID,
        RECORD_TYPE, RECORD_AMOUNT, RECORD_DATE,

        //Type table attribute keys
        TYPE_ID,
        TYPE_NAME
    }

    // Kotlin does not allow static variables or functions
    companion object {
        val DATABASE_NAME = "spend_record_database"

        val DATABASE_VERSION = 1

        val NOT_EXIST: Long = -2
        val SQL_ERROR: Long = -1
    }

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        // Create table with name records, a new column with name id which is INTEGER
        // type and it is primary key for the table, a new column with name
        // type and it is of type TEXT(String), a new column with name amount as
        // TEXT(String) type
        val CREATE_TYPE_TABLE_SCRIPT = "CREATE TABLE " + Table.TYPE.name +
                "(" +
                Key.TYPE_ID.name + " INTEGER PRIMARY KEY," +
                Key.TYPE_NAME.name + " TEXT" +
                ")"
        val CREATE_RECORDS_TABLE_SCRIPT = "CREATE TABLE " + Table.RECORD.name +
                "(" +
                Key.RECORD_ID.name + " INTEGER PRIMARY KEY," +
                Key.RECORD_TYPE.name + " INTEGER," +
                Key.RECORD_AMOUNT.name + " DECIMAL(10, 5)," +
                Key.RECORD_DATE.name + " TEXT," +
                "FOREIGN KEY(" + Key.RECORD_TYPE.name + ") REFERENCES " + Table.TYPE.name + " (" + Key.TYPE_ID.name + ")" +
                ")"
        sqLiteDatabase.execSQL("PRAGMA foreign_keys = ON")
        sqLiteDatabase.execSQL(CREATE_TYPE_TABLE_SCRIPT)
        sqLiteDatabase.execSQL(CREATE_RECORDS_TABLE_SCRIPT)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
        // Drop older record table if it exists
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS ${Table.RECORD.name}")

        // call onCreate method so it creates new table again
        onCreate(sqLiteDatabase)
    }

    /**
     * CRUD Operations (create, Read, Update, Delete)
     */

    fun typeExists(type: String): Boolean {
        return getTypeId(type) >= 0
    }

    // Adding new SpendRecord
    fun addSpendRecord(record: SpendRecord): Long {
        val typeId = getTypeId(record.type)
        if (typeId >= 0) {
            val db = this.writableDatabase

            val values = ContentValues()
            values.put(Key.RECORD_TYPE.name, typeId) // SpendRecord Title
            values.put(Key.RECORD_AMOUNT.name, record.amount) // SpendRecord Content
            values.put(Key.RECORD_DATE.name, record.date)
            // Inserting new row into records table
            return db.insert(Table.RECORD.name, null, values)
        }
        return SQL_ERROR
    }


    // Get Single SpendRecord
    fun getSpendRecord(id: Long): SpendRecord? {
        val db = this.readableDatabase
        var record: SpendRecord? = null

        // SQL query for getting a record from the database
        val selectQuery = """SELECT T1.${Key.RECORD_ID.name}, T2.${Key.TYPE_NAME.name},
                T1.${Key.RECORD_AMOUNT.name},T1.${Key.RECORD_DATE.name} FROM ${Table.RECORD.name} T1, ${Table.TYPE.name} T2
                WHERE T1.${Key.RECORD_TYPE.name}=T2.${Key.TYPE_ID.name} AND T1.${Key.RECORD_ID.name}=$id"""


        val cursor = db.rawQuery(selectQuery, null)


        cursor.let {
            if (cursor.moveToFirst()) {
                record = SpendRecord(
                        cursor.getString(0).toLong(),
                        cursor.getString(1),
                        cursor.getString(2).toDouble(),
                        cursor.getString(3)
                )
            }
        }

        // return record
        return record
    }

    // Get All SpendRecords
    fun getAllSpendRecords(): List<SpendRecord>? {

        // here this refers to SQLiteDatabase
        val db = this.readableDatabase

        // SQL query for getting all records from the database
        val selectQuery = """SELECT T1.${Key.RECORD_ID.name}, T2.${Key.TYPE_NAME.name},
                T1.${Key.RECORD_AMOUNT.name},T1.${Key.RECORD_DATE.name} FROM ${Table.RECORD.name} T1, ${Table.TYPE.name} T2
                WHERE T1.${Key.RECORD_TYPE.name}=T2.${Key.TYPE_ID.name}"""


        val cursor = db.rawQuery(selectQuery, null)

        cursor.let {
            if (cursor.moveToFirst()) {
                var recordList = arrayListOf<SpendRecord>()
                do {
                    var record = SpendRecord(
                            cursor.getString(0).toLong(),
                            cursor.getString(1),
                            cursor.getString(2).toDouble(),
                            cursor.getString(3)
                    )
                    // Adding SpendRecord to list
                    recordList.add(record)
                } while (cursor.moveToNext())

                cursor.close()
                // return recordlist if there is records in database
                return recordList
            }
        }
        //return null if the cursor was null
        return null
    }

    // Updating single record
    fun updateSpendRecord(record: SpendRecord): Long {
        if (record.id != null && typeExists(record.type)) {
            val db = this.writableDatabase
            val updateSql = """UPDATE ${Table.RECORD.name} SET ${Key.RECORD_TYPE}='${getTypeId(record.type)}',
                |${Key.RECORD_AMOUNT}=${record.amount}, ${Key.RECORD_DATE}='${record.date}'
                |WHERE ${Key.RECORD_ID}=${record.id}""".trimMargin()

            db.execSQL(updateSql)
        }
        return SQL_ERROR
    }

    // Deleting single SpendRecord
    fun deleteSpendRecord(id:Long) {
        val db = this.writableDatabase
        db.delete(
                Table.RECORD.name, // table name
                Key.RECORD_ID.name + " = ?", // selection
                arrayOf(id.toString()) // selectionArgs
        )
    }

    // Getting SpendRecords Count
    fun getCount(table: Table): Int {
        val db = this.writableDatabase

        val countQuery = "SELECT  * FROM " + table.name

        val cursor = db.rawQuery(countQuery, null)
        val count = cursor.count

        cursor.close()

        // return count
        return count
    }

    fun getTypeId(typeName: String): Long {

        //if the type cannot be found, return this value
        var id: Long = NOT_EXIST
        // Get writable database
        val db = this.writableDatabase
        val projection = arrayOf(Key.TYPE_ID.name, Key.TYPE_NAME.name)


        // Filter results WHERE "column_name" = 'value'
        // here selection is column_name and
        // selectionArgs is value
        val selection = Key.TYPE_NAME.name + "=?"
        val selectionArgs = arrayOf(typeName)

        // The order in which your result needs to be returned
        val sortOrder: String? = null // pass null if don't want it to be sorted

        // If passed 5, only 5 blogs will be returned
        val limit: String? = null // pass null if you dont want it to limit

        val cursor = db.query(
                Table.TYPE.name,        // The table to query
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
        cursor.close()
        return id
    }

    fun getAllSpendType(): List<String>? {

        // here this refers to SQLiteDatabase
        val db = this.readableDatabase

        // SQL query for getting all records from the database
        val selectQuery = "SELECT  ${Key.TYPE_NAME.name} FROM ${Table.TYPE.name}"
        val cursor = db.rawQuery(selectQuery, null)

        cursor.let {
            if (cursor.moveToFirst()) {
                val listOfType = arrayListOf<String>()
                do {
                    listOfType.add(cursor.getString(0))
                } while (cursor.moveToNext())

                cursor.close()

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
        val selectQuery = "SELECT  * FROM ${Table.TYPE.name} WHERE ${Key.TYPE_NAME.name}='$type'"
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.count <= 0) {
            val values = ContentValues()
            values.put(Key.TYPE_NAME.name, type)

            returnVal = db.insert(Table.TYPE.name, null, values)
        }
        cursor.close()
        return returnVal
    }

    fun deleteSpendType(type_name: String) {
        val db = this.writableDatabase
        db.delete(
                Table.TYPE.name, // table name
                "${Key.TYPE_NAME.name} = '$type_name'", // selection
                null// selectionArgs
        )
    }


    fun clearTable(table: Table) {
        val db = this.writableDatabase

        val delSql = "DELETE  FROM ${table.name}"

        db.execSQL(delSql)
    }
}