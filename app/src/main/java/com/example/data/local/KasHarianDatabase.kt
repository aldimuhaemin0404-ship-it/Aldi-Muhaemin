package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CategoryEntity::class,
        PocketEntity::class,
        IncomeEntity::class,
        PocketTransferEntity::class,
        AppSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class KasHarianDatabase : RoomDatabase() {
    abstract fun kasHarianDao(): KasHarianDao

    companion object {
        @Volatile
        private var INSTANCE: KasHarianDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE app_settings ADD COLUMN savingsGoalName TEXT NOT NULL DEFAULT 'Target Tabungan Utama'"
                )
                db.execSQL(
                    "ALTER TABLE app_settings ADD COLUMN savingsTarget INTEGER NOT NULL DEFAULT 5000000"
                )
            }
        }

        fun getInstance(context: Context): KasHarianDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KasHarianDatabase::class.java,
                    "kasharian_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
