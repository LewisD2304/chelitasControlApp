package com.dlcode.chelasapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dlcode.chelasapp.repository.BusinessRepository

class BusinessViewModelFactory(private val repository: BusinessRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BusinessViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BusinessViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
