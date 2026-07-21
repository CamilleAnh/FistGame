package com.twinbrother.fruitsort

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val _engine = MutableStateFlow<LevelOneEngine?>(null)
    val engine: StateFlow<LevelOneEngine?> = _engine.asStateFlow()

    // Version counter to force StateFlow re-emission when engine state changes
    // (StateFlow skips emission if the same object reference is assigned)
    private val _stateVersion = MutableStateFlow(0)
    val stateVersion: StateFlow<Int> = _stateVersion.asStateFlow()

    private val _levelId = MutableStateFlow(1)
    val levelId: StateFlow<Int> = _levelId.asStateFlow()

    private val _powerupState = MutableStateFlow(PowerupState())
    val powerupState: StateFlow<PowerupState> = _powerupState.asStateFlow()

    private val _isMagnifyMode = MutableStateFlow(false)
    val isMagnifyMode: StateFlow<Boolean> = _isMagnifyMode.asStateFlow()

    private val _selectedBoxIndex = MutableStateFlow<Int?>(null)
    val selectedBoxIndex: StateFlow<Int?> = _selectedBoxIndex.asStateFlow()

    private val _isHintMode = MutableStateFlow(false)
    val isHintMode: StateFlow<Boolean> = _isHintMode.asStateFlow()

    private val _undoCount = MutableStateFlow(1) // 1 free undo per level
    val undoCount: StateFlow<Int> = _undoCount.asStateFlow()

    data class PowerupState(
        val reroll: Int = 0,
        val reveal: Int = 0,
        val shuffle: Int = 0,
        val freeReroll: Int = 1,
        val freeReveal: Int = 1,
        val freeShuffle: Int = 1,
        val freeHint: Int = 1,
        val freeUndo: Int = 1
    )

    fun initLevel(id: Int, context: android.content.Context) {
        _levelId.value = id
        _engine.value = LevelOneEngine(id)
        _selectedBoxIndex.value = null
        _isMagnifyMode.value = false
        _isHintMode.value = false
        _undoCount.value = 1
        refreshPowerupCounts(context)
    }

    fun resetLevel(context: android.content.Context) {
        val currentId = _levelId.value
        _engine.value = LevelOneEngine(currentId)
        _selectedBoxIndex.value = null
        _isMagnifyMode.value = false
        _isHintMode.value = false
        // Reset free powerups to default (1 each) before refreshing
        _powerupState.value = PowerupState()
        refreshPowerupCounts(context)
    }

    fun refreshPowerupCounts(context: android.content.Context) {
        val current = _powerupState.value
        _powerupState.value = current.copy(
            reroll = current.freeReroll + GoldManager.getRerollCount(context),
            reveal = current.freeReveal + GoldManager.getRevealCount(context),
            shuffle = current.freeShuffle + GoldManager.getShuffleCount(context)
        )
    }

    fun consumeReroll(context: android.content.Context): Boolean {
        val state = _powerupState.value
        if (state.reroll <= 0) return false
        
        if (state.freeReroll > 0) {
            _powerupState.value = state.copy(freeReroll = state.freeReroll - 1)
        } else if (!GoldManager.useReroll(context)) {
            return false
        }
        refreshPowerupCounts(context)
        return true
    }

    fun consumeReveal(context: android.content.Context): Boolean {
        val state = _powerupState.value
        if (state.reveal <= 0) return false
        
        if (state.freeReveal > 0) {
            _powerupState.value = state.copy(freeReveal = state.freeReveal - 1)
        } else if (!GoldManager.useReveal(context)) {
            return false
        }
        refreshPowerupCounts(context)
        return true
    }

    fun consumeShuffle(context: android.content.Context): Boolean {
        val state = _powerupState.value
        if (state.shuffle <= 0) return false
        
        if (state.freeShuffle > 0) {
            _powerupState.value = state.copy(freeShuffle = state.freeShuffle - 1)
        } else if (!GoldManager.useShuffle(context)) {
            return false
        }
        refreshPowerupCounts(context)
        return true
    }

    fun setSelectedBox(index: Int?) {
        _selectedBoxIndex.value = index
    }

    fun setMagnifyMode(enabled: Boolean) {
        _isMagnifyMode.value = enabled
    }

    fun setHintMode(enabled: Boolean) {
        _isHintMode.value = enabled
    }

    fun consumeUndo(context: android.content.Context): Boolean {
        val state = _powerupState.value
        if (state.freeUndo > 0) {
            _powerupState.value = state.copy(freeUndo = state.freeUndo - 1)
            return true
        }
        if (GoldManager.useUndo(context)) {
            refreshPowerupCounts(context)
            return true
        }
        // After free undo & stock, costs gems
        return GoldManager.spendGems(context, GoldManager.GEM_COST_UNDO)
    }

    fun consumeHint(context: android.content.Context): Boolean {
        val state = _powerupState.value
        if (state.freeHint > 0) {
            _powerupState.value = state.copy(freeHint = state.freeHint - 1)
            return true
        }
        if (GoldManager.useHint(context)) {
            refreshPowerupCounts(context)
            return true
        }
        return GoldManager.spendGems(context, GoldManager.GEM_COST_HINT)
    }

    fun triggerStateUpdate() {
        // Increment version counter to notify observers of engine state changes
        _stateVersion.value++
    }
}

