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
        MarketQuoteEntity::class,
        LogMessageEntity::class
    ],
    version = 4,
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

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `market_quotes` (
                        `itemId` TEXT NOT NULL,
                        `basePrice` INTEGER NOT NULL,
                        `currentPrice` INTEGER NOT NULL,
                        `priceChangePercent` INTEGER NOT NULL,
                        `demand` TEXT NOT NULL,
                        `marketDriver` TEXT NOT NULL,
                        `dayCalculated` INTEGER NOT NULL,
                        PRIMARY KEY(`itemId`)
                    )
                    """.trimIndent()
                )
                db.execSQL("ALTER TABLE npc_contracts ADD COLUMN isPenalized INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE farm_state ADD COLUMN playerX REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE farm_state ADD COLUMN playerY REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE farm_state ADD COLUMN playerZ REAL NOT NULL DEFAULT -6.0")
                db.execSQL("ALTER TABLE farm_state ADD COLUMN playerYaw REAL NOT NULL DEFAULT 180.0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "solarpunk_farm_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
