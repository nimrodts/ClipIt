package com.nimroddayan.couponmanager.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nimroddayan.couponmanager.data.model.Category
import com.nimroddayan.couponmanager.data.model.Coupon
import com.nimroddayan.couponmanager.data.model.CouponHistory

@Database(
        entities = [Coupon::class, Category::class, CouponHistory::class],
        version = 16,
        exportSchema = false
)
@TypeConverters(com.nimroddayan.couponmanager.data.db.TypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun couponDao(): CouponDao
    abstract fun categoryDao(): CategoryDao
    abstract fun couponHistoryDao(): CouponHistoryDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        private val MIGRATION_12_13 =
                object : Migration(12, 13) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        // Create new table with nullable categoryId
                        db.execSQL(
                                """
                    CREATE TABLE Coupon_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        name TEXT NOT NULL, 
                        currentValue REAL NOT NULL, 
                        initialValue REAL NOT NULL, 
                        expirationDate INTEGER NOT NULL, 
                        categoryId INTEGER, 
                        redeemCode TEXT, 
                        isArchived INTEGER NOT NULL DEFAULT 0, 
                        creationMessage TEXT, 
                        FOREIGN KEY(categoryId) REFERENCES Category(id) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """.trimIndent()
                        )

                        // Copy data from old table to new table
                        db.execSQL(
                                """
                    INSERT INTO Coupon_new (id, name, currentValue, initialValue, expirationDate, categoryId, redeemCode, isArchived, creationMessage)
                    SELECT id, name, currentValue, initialValue, expirationDate, categoryId, redeemCode, isArchived, creationMessage FROM Coupon
                """.trimIndent()
                        )

                        // Remove the old table
                        db.execSQL("DROP TABLE Coupon")

                        // Rename new table to old table name
                        db.execSQL("ALTER TABLE Coupon_new RENAME TO Coupon")

                        // Re-create the index
                        db.execSQL(
                                "CREATE INDEX IF NOT EXISTS index_Coupon_categoryId ON Coupon(categoryId)"
                        )
                    }
                }

        private val MIGRATION_13_14 =
                object : Migration(13, 14) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        // Set categoryId to null for coupons that belong to the "General" category
                        // (id = 1)
                        db.execSQL("UPDATE Coupon SET categoryId = NULL WHERE categoryId = 1")
                        // Delete the "General" category
                        db.execSQL("DELETE FROM Category WHERE id = 1")
                    }
                }

        private val MIGRATION_14_15 =
                object : Migration(14, 15) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        // Create new table with the correct schema
                        db.execSQL(
                                """
                    CREATE TABLE coupon_history (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        couponId INTEGER NOT NULL, 
                        action TEXT NOT NULL, 
                        changeSummary TEXT NOT NULL, 
                        timestamp INTEGER NOT NULL DEFAULT 0, 
                        couponState TEXT, 
                        FOREIGN KEY(couponId) REFERENCES Coupon(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """.trimIndent()
                        )

                        // Copy data from old table to new table
                        db.execSQL(
                                """
                    INSERT INTO coupon_history (id, couponId, action, changeSummary, couponState)
                    SELECT id, couponId, action, changeSummary, couponState FROM CouponHistory
                """.trimIndent()
                        )

                        // Remove the old table
                        db.execSQL("DROP TABLE CouponHistory")

                        // Re-create the index
                        db.execSQL(
                                "CREATE INDEX IF NOT EXISTS index_coupon_history_couponId ON coupon_history(couponId)"
                        )
                    }
                }

        private val MIGRATION_15_16 =
                object : Migration(15, 16) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        db.execSQL(
                                "ALTER TABLE Coupon ADD COLUMN isOneTime INTEGER NOT NULL DEFAULT 0"
                        )
                    }
                }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE
                    ?: synchronized(this) {
                        val instance =
                                Room.databaseBuilder(
                                                context.applicationContext,
                                                AppDatabase::class.java,
                                                "coupon_database"
                                        )
                                        .addMigrations(
                                                MIGRATION_12_13,
                                                MIGRATION_13_14,
                                                MIGRATION_14_15,
                                                MIGRATION_15_16
                                        )
                                        .build()
                        INSTANCE = instance
                        instance
                    }
        }
    }
}
