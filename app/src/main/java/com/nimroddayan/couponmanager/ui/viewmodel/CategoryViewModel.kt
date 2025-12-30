package com.nimroddayan.couponmanager.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nimroddayan.couponmanager.data.db.CategoryDao
import com.nimroddayan.couponmanager.data.model.Category
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(private val categoryDao: CategoryDao) : ViewModel() {
    val allCategories: StateFlow<List<Category>> = categoryDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insert(category: Category) = viewModelScope.launch {
        categoryDao.insert(category)
    }

    fun clearAll() = viewModelScope.launch {
        categoryDao.clearAll()
    }
}
