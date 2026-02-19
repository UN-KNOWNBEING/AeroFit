package com.aerofit.india.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.aerofit.india.data.local.UserDao
import com.aerofit.india.domain.usecase.AssessRunningSuitabilityUseCase
import com.aerofit.india.domain.usecase.GetAqiForCurrentLocationUseCase

class ViewModelFactory(
    private val getAqiUseCase: GetAqiForCurrentLocationUseCase,
    private val assessSuitabilityUseCase: AssessRunningSuitabilityUseCase,
    private val userDao: UserDao,
    private val context: Context // Added context here
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                getAqiUseCase,
                assessSuitabilityUseCase,
                userDao,
                context // Pass context to the ViewModel
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}