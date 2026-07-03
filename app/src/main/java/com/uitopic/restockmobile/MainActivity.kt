package com.uitopic.restockmobile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.uitopic.restockmobile.analytics.RestockAnalytics
import com.uitopic.restockmobile.core.auth.local.TokenManager
import com.uitopic.restockmobile.core.notifications.RestockFirebaseMessagingService
import com.uitopic.restockmobile.features.auth.presentation.navigation.authNavGraph
import com.uitopic.restockmobile.features.home.presentation.navigation.HomeRoute
import com.uitopic.restockmobile.features.home.presentation.navigation.homeNavGraph
import com.uitopic.restockmobile.features.monitoring.presentation.navigation.monitoringNavGraph
import com.uitopic.restockmobile.features.planning.presentation.navigation.planningNavGraph
import com.uitopic.restockmobile.features.profiles.presentation.navigation.profileNavGraph
import com.uitopic.restockmobile.features.resources.orders.presentation.navigation.ordersNavGraph
import com.uitopic.restockmobile.features.resources.presentation.navigation.inventoryNavGraph
import com.uitopic.restockmobile.features.subscriptions.presentation.navigation.SubscriptionRoute
import com.uitopic.restockmobile.features.subscriptions.presentation.navigation.subscriptionNavGraph
import com.uitopic.restockmobile.ui.theme.RestockmobileTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var restockAnalytics: RestockAnalytics

    private var initialSupplyId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialSupplyId = extractSupplyIdFromIntent(intent)

        setContent {
            RestockmobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var pendingSupplyId by remember { mutableStateOf(initialSupplyId) }

                    RequestNotificationPermission()
                    RestockApp(
                        tokenManager = tokenManager,
                        pendingSupplyId = pendingSupplyId,
                        onDeepLinkHandled = {
                            pendingSupplyId = null
                            initialSupplyId = null
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val supplyId = extractSupplyIdFromIntent(intent)
        if (!supplyId.isNullOrEmpty()) {
            initialSupplyId = supplyId
        }
    }

    private fun extractSupplyIdFromIntent(intent: Intent?): String? {
        intent?.let {
            val supplyId = it.getStringExtra(RestockFirebaseMessagingService.EXTRA_SUPPLY_ID)
            val notificationType = it.getStringExtra(RestockFirebaseMessagingService.EXTRA_NOTIFICATION_TYPE) ?: "stock_low"
            val fromNotification = it.getBooleanExtra(RestockFirebaseMessagingService.EXTRA_FROM_NOTIFICATION, false)

            if (!supplyId.isNullOrEmpty() && (fromNotification || it.hasExtra(RestockFirebaseMessagingService.EXTRA_SUPPLY_ID))) {
                restockAnalytics.trackNotificationClicked(
                    notificationType = notificationType,
                    supplyId = supplyId,
                    timestampMs = System.currentTimeMillis()
                )
                it.removeExtra(RestockFirebaseMessagingService.EXTRA_SUPPLY_ID)
                it.removeExtra(RestockFirebaseMessagingService.EXTRA_FROM_NOTIFICATION)
                return supplyId
            }
        }
        return null
    }
}

@Composable
fun RequestNotificationPermission() {
    val context = LocalContext.current
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = {}
        )
        LaunchedEffect(Unit) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(permission)
            }
        }
    }
}

@Composable
fun RestockApp(
    tokenManager: TokenManager,
    pendingSupplyId: String?,
    onDeepLinkHandled: () -> Unit
) {
    val navController = rememberNavController()

    val isLoggedIn = tokenManager.isLoggedIn()
    val subscription = if (isLoggedIn) tokenManager.getSubscription() else 0

    val startDestination = if (isLoggedIn) {
        if (subscription == 0) {
            SubscriptionRoute.SubscriptionGraph
        } else {
            HomeRoute.Home.route
        }
    } else {
        "auth_graph"
    }

    LaunchedEffect(pendingSupplyId, isLoggedIn) {
        if (isLoggedIn && !pendingSupplyId.isNullOrEmpty() && subscription != 0) {
            navController.navigate("supply_detail/$pendingSupplyId") {
                launchSingleTop = true
            }
            onDeepLinkHandled()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Graph
        authNavGraph(
            navController = navController,
            onAuthSuccess = { sub ->
                if (sub == 0) {
                    navController.navigate(SubscriptionRoute.SubscriptionGraph) {
                        popUpTo("auth_graph") { inclusive = true }
                    }
                } else {
                    navController.navigate(HomeRoute.Home.route) {
                        popUpTo("auth_graph") { inclusive = true }
                    }
                }
            }
        )

        // Subscription Graph
        subscriptionNavGraph(
            navController = navController,
            onSubscriptionComplete = {
                navController.navigate(HomeRoute.Home.route) {
                    popUpTo(SubscriptionRoute.SubscriptionGraph) { inclusive = true }
                }
            }
        )

        // Home Screen
        homeNavGraph(navController)
        // Monitoring Graph (Sales)
        monitoringNavGraph(navController)
        // Profile Graph
        profileNavGraph(
            navController = navController,
            onAccountDeleted = {
                navController.navigate("auth_graph") {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
        // Inventory
        inventoryNavGraph(navController)
        // Planning (Recipes)
        planningNavGraph(navController)

        // Resources - Orders
        ordersNavGraph(navController, 1)
    }
}
