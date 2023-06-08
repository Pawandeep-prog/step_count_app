package com.programminghut.stepcountapp.viewmodels

import android.database.Observable
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StepCountViewModel: ViewModel() {

    val stepCount = ObservableField<String>()

    init {
        stepCount.set("steps count: reading")
    }
}