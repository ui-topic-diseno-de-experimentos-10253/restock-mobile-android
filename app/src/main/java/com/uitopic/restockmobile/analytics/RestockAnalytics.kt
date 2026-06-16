package com.uitopic.restockmobile.analytics

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestockAnalytics @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
) {
    init {
        firebaseAnalytics.setAnalyticsCollectionEnabled(true)
    }

    fun trackFirstOrderCompleted(
        success: Boolean,
        durationSeconds: Long,
        userRole: String
    ) {
        logEvent(
            AnalyticsEvent.FIRST_ORDER_COMPLETED,
            Bundle().apply {
                putLong(AnalyticsParam.SUCCESS, if (success) 1L else 0L)
                putLong(AnalyticsParam.DURATION_SECONDS, durationSeconds)
                putString(AnalyticsParam.USER_ROLE, userRole)
            }
        )
    }

    fun trackInventoryAlertOpened(
        alertType: String,
        supplyId: String
    ) {
        logEvent(
            AnalyticsEvent.INVENTORY_ALERT_OPENED,
            Bundle().apply {
                putString(AnalyticsParam.ALERT_TYPE, alertType)
                putString(AnalyticsParam.SUPPLY_ID, supplyId)
            }
        )
    }

    fun trackPurchaseOrderCreated(
        correctedLines: Int,
        totalLines: Int
    ) {
        logEvent(
            AnalyticsEvent.PURCHASE_ORDER_CREATED,
            Bundle().apply {
                putLong(AnalyticsParam.CORRECTED_LINES, correctedLines.toLong())
                putLong(AnalyticsParam.TOTAL_LINES, totalLines.toLong())
            }
        )
    }

    fun trackRotationMetricViewed(
        rotationLevel: String,
        screenName: String
    ) {
        logEvent(
            AnalyticsEvent.ROTATION_METRIC_VIEWED,
            Bundle().apply {
                putString(AnalyticsParam.ROTATION_LEVEL, rotationLevel)
                putString(AnalyticsParam.SCREEN_NAME, screenName)
            }
        )
    }

    fun trackWasteReportRegistered(
        supplyId: String,
        wasteAmount: Double,
        unit: String
    ) {
        logEvent(
            AnalyticsEvent.WASTE_REPORT_REGISTERED,
            Bundle().apply {
                putString(AnalyticsParam.SUPPLY_ID, supplyId)
                putDouble(AnalyticsParam.WASTE_AMOUNT, wasteAmount)
                putString(AnalyticsParam.UNIT, unit)
            }
        )
    }

    private fun logEvent(name: String, params: Bundle) {
        Log.d("RestockAnalytics", "Logging event: $name params=$params")
        firebaseAnalytics.logEvent(name, params)
    }
}
