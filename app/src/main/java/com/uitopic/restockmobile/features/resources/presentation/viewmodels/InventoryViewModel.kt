package com.uitopic.restockmobile.features.resources.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uitopic.restockmobile.analytics.RestockAnalytics
import com.uitopic.restockmobile.core.auth.local.TokenManager
import com.uitopic.restockmobile.features.resources.domain.models.Batch
import com.uitopic.restockmobile.features.resources.domain.models.CustomSupply
import com.uitopic.restockmobile.features.resources.domain.models.Supply
import com.uitopic.restockmobile.features.resources.domain.repositories.InventoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: InventoryRepository,
    private val tokenManager: TokenManager,
    private val analytics: RestockAnalytics
) : ViewModel() {

    private val _supplies = MutableStateFlow<List<Supply>>(emptyList())
    val supplies: StateFlow<List<Supply>> = _supplies.asStateFlow()

    private val _customSupplies = MutableStateFlow<List<CustomSupply>>(emptyList())
    val customSupplies: StateFlow<List<CustomSupply>> = _customSupplies.asStateFlow()

    private val _batches = MutableStateFlow<List<Batch>>(emptyList())
    val batches: StateFlow<List<Batch>> = _batches.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _supplies.value = repository.getSupplies()
                _customSupplies.value = repository.getCustomSuppliesByUserId()
                _batches.value = repository.getBatchesByUserId()
            } catch (t: Throwable) {
                // TODO: manejar error (mostrar snackbar o log)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createBatch(batch: Batch) {
        viewModelScope.launch {
            try {
                repository.createBatch(batch)
                _batches.value = repository.getBatches()
            } catch (t: Throwable) {
                _batches.value = _batches.value + batch
            }
        }
    }

    fun updateBatch(updated: Batch) {
        viewModelScope.launch {
            val previous = _batches.value.find { it.id == updated.id }
            try {
                repository.updateBatch(updated)
                trackWasteReportIfStockDecreased(previous, updated)
                _batches.value = repository.getBatches()
            } catch (t: Throwable) {
                trackWasteReportIfStockDecreased(previous, updated)
                _batches.value = _batches.value.map {
                    if (it.id == updated.id) updated else it
                }
            }
        }
    }

    fun deleteBatch(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteBatch(id)
                _batches.value = repository.getBatches()
            } catch (t: Throwable) {
                // Fallback local
                _batches.value = _batches.value.filterNot { it.id == id }
            }
        }
    }

    fun getBatchById(id: String?): Batch? {
        if (id == null) return null
        return _batches.value.find { it.id == id }
    }

    fun addCustomSupply(custom: CustomSupply) {
        viewModelScope.launch {
            try {
                repository.createCustomSupply(custom)
                _customSupplies.value = repository.getCustomSupplies()
            } catch (t: Throwable) {
                _customSupplies.value = _customSupplies.value + custom
            }
        }
    }

    fun updateCustomSupply(updated: CustomSupply) {
        viewModelScope.launch {
            try {
                repository.updateCustomSupply(updated)
                _customSupplies.value = repository.getCustomSupplies()
            } catch (t: Throwable) {
                _customSupplies.value = _customSupplies.value.map {
                    if (it.id == updated.id) updated else it
                }
            }
        }
    }

    fun deleteCustomSupply(custom: CustomSupply) {
        viewModelScope.launch {
            try {
                repository.deleteCustomSupply(custom.id)
                _customSupplies.value = repository.getCustomSupplies()
            } catch (t: Throwable) {
                _customSupplies.value = _customSupplies.value.filterNot { it.id == custom.id }
            }
        }
    }

    fun getCustomSupplyById(id: Int?): CustomSupply? {
        if (id == null) return null
        return _customSupplies.value.find { it.id == id }
    }

    // Función auxiliar para obtener el ID del usuario actual
    fun getCurrentUserId(): Int {
        return tokenManager.getUserId()
    }

    fun getCurrentUserRoleId(): Int {
        return tokenManager.getRoleId()
    }

    fun trackInventoryAlertOpenedIfNeeded(batch: Batch) {
        val alertType = getInventoryAlertType(batch) ?: return
        analytics.trackInventoryAlertOpened(
            alertType = alertType,
            supplyId = batch.customSupply?.supplyId?.toString() ?: batch.id
        )
    }

    fun trackRotationMetricViewed() {
        analytics.trackRotationMetricViewed(
            rotationLevel = "inventory",
            screenName = "InventoryScreen"
        )
    }

    fun trackWasteReportRegistered(
        supplyId: String,
        wasteAmount: Double,
        unit: String
    ) {
        analytics.trackWasteReportRegistered(
            supplyId = supplyId,
            wasteAmount = wasteAmount,
            unit = unit
        )
    }

    private fun trackWasteReportIfStockDecreased(previous: Batch?, updated: Batch) {
        if (previous == null || updated.stock >= previous.stock) return

        trackWasteReportRegistered(
            supplyId = updated.customSupply?.supplyId?.toString() ?: updated.id,
            wasteAmount = previous.stock - updated.stock,
            unit = updated.customSupply?.unit?.abbreviation
                ?: updated.customSupply?.unit?.name
                ?: ""
        )
    }

    private fun getInventoryAlertType(batch: Batch): String? {
        val isLowStock = batch.stock <= (batch.customSupply?.minStock ?: 0)
        val isExpiringSoon = isBatchExpiringSoon(batch)

        return when {
            isLowStock && isExpiringSoon -> "low_stock_and_expiring_soon"
            isLowStock -> "low_stock"
            isExpiringSoon -> "expiring_soon"
            else -> null
        }
    }

    private fun isBatchExpiringSoon(batch: Batch): Boolean {
        val expirationDate = batch.expirationDate
        if (expirationDate.isNullOrBlank() || expirationDate == "9999-12-31") return false

        return try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            parser.isLenient = false
            val expiration = parser.parse(expirationDate) ?: return false
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val warningLimit = today.clone() as Calendar
            warningLimit.add(Calendar.DAY_OF_YEAR, 7)

            !expiration.before(today.time) && !expiration.after(warningLimit.time)
        } catch (t: Throwable) {
            false
        }
    }
}
