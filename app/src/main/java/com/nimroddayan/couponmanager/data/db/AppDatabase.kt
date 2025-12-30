package com.nimroddayan.couponmanager.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.nimroddayan.couponmanager.data.model.Category
import com.nimroddayan.couponmanager.data.model.Coupon

@Database(entities = [Coupon::class, Category::class], version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun couponDao(): CouponDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "coupon_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
