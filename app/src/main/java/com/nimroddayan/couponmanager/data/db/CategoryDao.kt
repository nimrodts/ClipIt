package com.nimroddayan.couponmanager.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.nimroddayan.couponmanager.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category ORDER BY name ASC")
    fun getAll(): Flow<List<Category>>

    @Insert
    suspend fun insert(category: Category): Long

    @Query("DELETE FROM category")
    suspend fun clearAll()
}
