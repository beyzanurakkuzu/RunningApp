package com.beyzaakkuzu.runningtracker.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.beyzaakkuzu.runningtracker.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepository
):ViewModel() {

}