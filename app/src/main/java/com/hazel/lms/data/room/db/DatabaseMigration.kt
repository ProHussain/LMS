package com.hazel.lms.data.room.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class DatabaseMigration {
    companion object {
        val migration_1_2 : Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val tableName = "student"
                db.execSQL("CREATE TABLE IF NOT EXISTS `${tableName}` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `department` INTEGER NOT NULL, `name` TEXT NOT NULL, `email` TEXT NOT NULL, `phone` TEXT NOT NULL, `address` TEXT NOT NULL, `semester` TEXT NOT NULL, `rollNo` TEXT NOT NULL, `image` TEXT NOT NULL)")
            }
        }
    }

}