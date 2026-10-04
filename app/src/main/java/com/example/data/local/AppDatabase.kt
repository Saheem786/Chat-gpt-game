package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        FarmStateEntity::class,
        AnimalEntity::class,
        CropPlotEntity::class,
        InventoryEntity::class,
        WorkshopQueueEntity::class,
        ShopShelfEntity::class,
        ContractEntity::class,
        LogMessageEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun farmDao(): FarmDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE farm_state ADD COLUMN totalExpenses INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE farm_state ADD COLUMN salesToday INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE farm_state ADD COLUMN livestockSoldTotal INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE farm_state ADD COLUMN meatProcessedTotal INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE farm_state ADD COLUMN wholesaleIncomeTotal INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE farm_state ADD COLUMN businessReputation INTEGER NOT NULL DEFAULT 50")
                db.execSQL("ALTER TABLE farm_state ADD COLUMN businessLevel TEXT NOT NULL DEFAULT 'LEVEL_1'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "solarpunk_farm_database"
                )
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
