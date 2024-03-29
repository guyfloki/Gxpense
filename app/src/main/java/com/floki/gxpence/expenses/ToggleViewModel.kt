package com.floki.gxpence.expenses

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ToggleViewModel : ViewModel() {
    private val _isXPCardVisible = MutableStateFlow(true)
    val isXPCardVisible: StateFlow<Boolean> = _isXPCardVisible

    fun switch() {
        _isXPCardVisible.value = !_isXPCardVisible.value
    }
}

