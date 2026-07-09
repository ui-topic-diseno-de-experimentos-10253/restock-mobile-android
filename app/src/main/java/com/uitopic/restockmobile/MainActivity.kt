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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.uitopic.restockmobile.analytics.RestockAnalytics
import com.uitopic.restockmobile.core.auth.local.TokenManager
import com.uitopic.restockmobile.core.notifications.PushNotificationManager
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
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

data class NotificationData(
    val customSupplyId: String?,
    val batchId: String?,
    val type: String?
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenManager: TokenManager

    @Inject
    lateinit var restockAnalytics: RestockAnalytics

    @Inject
    lateinit var pushNotificationManager: PushNotificationManager

    private val pendingNotificationData = MutableStateFlow<NotificationData?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingNotificationData.value = extractNotificationDataFromIntent(intent)

        setContent {
            RestockmobileTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val isLoggedIn = tokenManager.isLoggedIn()
                    LaunchedEffect(isLoggedIn) {
                        if (isLoggedIn) {
                            pushNotificationManager.syncToken()
                        }
                    }

                    val notificationData by pendingNotificationData.collectAsState()

                    RequestNotificationPermission()
                    RestockApp(
                        tokenManager = tokenManager,
                        pendingNotificationData = notificationData,
                        onDeepLinkHandled = {
                            pendingNotificationData.value = null
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val data = extractNotificationDataFromIntent(intent)
        if (data != null) {
            pendingNotificationData.value = data
        }
    }

    private fun extractNotificationDataFromIntent(intent: Intent?): NotificationData? {
        intent?.let {
            val batchId = it.getStringExtra(RestockFirebaseMessagingService.EXTRA_BATCH_ID)
            val customSupplyId = it.getStringExtra(RestockFirebaseMessagingService.EXTRA_CUSTOM_SUPPLY_ID)
            val type = it.getStringExtra(RestockFirebaseMessagingService.EXTRA_TYPE) ?: "stock_low"
            val fromNotification = it.getBooleanExtra(RestockFirebaseMessagingService.EXTRA_FROM_NOTIFICATION, false)

            if (fromNotification || it.hasExtra(RestockFirebaseMessagingService.EXTRA_CUSTOM_SUPPLY_ID) || it.hasExtra(RestockFirebaseMessagingService.EXTRA_BATCH_ID)) {
                val analyticsSupplyId = if (!customSupplyId.isNullOrEmpty()) customSupplyId else (batchId ?: "")
                restockAnalytics.trackNotificationClicked(
                    notificationType = type,
                    supplyId = analyticsSupplyId,
                    timestampMs = System.currentTimeMillis()
                )
                it.removeExtra(RestockFirebaseMessagingService.EXTRA_BATCH_ID)
                it.removeExtra(RestockFirebaseMessagingService.EXTRA_CUSTOM_SUPPLY_ID)
                it.removeExtra(RestockFirebaseMessagingService.EXTRA_TYPE)
                it.removeExtra(RestockFirebaseMessagingService.EXTRA_FROM_NOTIFICATION)
                return NotificationData(customSupplyId, batchId, type)
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
    pendingNotificationData: NotificationData?,
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

    LaunchedEffect(pendingNotificationData, isLoggedIn) {
        if (isLoggedIn && pendingNotificationData != null && subscription != 0) {
            val customSupplyId = pendingNotificationData.customSupplyId
            val batchId = pendingNotificationData.batchId

            if (!customSupplyId.isNullOrEmpty()) {
                navController.navigate("supply_detail/$customSupplyId") {
                    launchSingleTop = true
                }
            } else if (!batchId.isNullOrEmpty()) {
                navController.navigate("inventory_detail/$batchId") {
                    launchSingleTop = true
                }
            } else {
                navController.navigate("inventory") {
                    launchSingleTop = true
                }
            }
            onDeepLinkHandled()
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = isLoggedIn && subscription != 0 && when (currentRoute) {
        HomeRoute.Home.route,
        "inventory",
        "orders",
        "recipes_list",
        "profile_detail" -> true
        else -> false
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                RestockBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
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
                ordersNavGraph(
                    navController = navController,
                    adminRestaurantId = 1,
                    tokenManager = tokenManager
                )
            }
        }
    }
}

enum class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    Home(HomeRoute.Home.route, Icons.Default.Home, "Inicio"),
    Inventory("inventory", Icons.Default.Inventory, "Inventario"),
    Orders("orders", Icons.Default.ShoppingCart, "Pedidos"),
    Recipes("planning", Icons.Default.Restaurant, "Recetas"),
    Profile("profile_graph", Icons.Default.Person, "Perfil")
}

@Composable
fun RestockBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val selectedItem = when (currentRoute) {
        HomeRoute.Home.route -> BottomNavItem.Home
        "inventory" -> BottomNavItem.Inventory
        "orders" -> BottomNavItem.Orders
        "recipes_list" -> BottomNavItem.Recipes
        "profile_detail" -> BottomNavItem.Profile
        else -> null
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), clip = false)
            .height(68.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.values().forEach { item ->
                val isSelected = selectedItem == item
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                    label = "color"
                )
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent,
                    label = "bgColor"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onNavigate(item.route) }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 28.dp)
                            .background(backgroundColor, shape = RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.label,
                        color = contentColor,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
