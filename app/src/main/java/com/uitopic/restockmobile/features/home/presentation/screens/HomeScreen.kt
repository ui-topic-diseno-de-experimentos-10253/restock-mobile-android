// features/home/presentation/HomeScreen.kt
package com.uitopic.restockmobile.features.home.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.uitopic.restockmobile.features.home.presentation.components.QuickActionCard
import com.uitopic.restockmobile.features.home.presentation.components.RestockScaffold
import com.uitopic.restockmobile.features.home.presentation.viewmodels.HomeViewModel
import com.uitopic.restockmobile.features.resources.presentation.viewmodels.InventoryViewModel
import com.uitopic.restockmobile.features.planning.presentation.viewmodels.RecipesViewModel
import com.uitopic.restockmobile.features.planning.presentation.states.RecipeUiState
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    inventoryViewModel: InventoryViewModel = hiltViewModel(),
    recipesViewModel: RecipesViewModel = hiltViewModel(),
    userAvatar: String = "",
    onNavigateToProfile: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToSales: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val userName = viewModel.getUsername()
    val userEmail = viewModel.userEmail

    // Fetch live statistics
    LaunchedEffect(Unit) {
        inventoryViewModel.loadAll()
        recipesViewModel.loadRecipes()
    }

    val customSupplies by inventoryViewModel.customSupplies.collectAsState()
    val batches by inventoryViewModel.batches.collectAsState()
    val recipesState by recipesViewModel.uiState.collectAsState()

    val recipesCount = when (val state = recipesState) {
        is RecipeUiState.Success -> state.recipes.size
        else -> 0
    }

    RestockScaffold(
        title = "Restock Restaurants",
        userName = userName,
        userEmail = userEmail,
        userAvatar = userAvatar,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToInventory = onNavigateToInventory,
        onNavigateToRecipes = onNavigateToRecipes,
        onNavigateToSales = onNavigateToSales,
        onNavigateToHome = {},
        onLogout = onLogout,
        onNavigateToOrders = onNavigateToOrders
    ) { innerPadding ->
        HomeContent(
            modifier = Modifier.padding(innerPadding),
            userName = userName,
            suppliesCount = customSupplies.size,
            batchesCount = batches.size,
            recipesCount = recipesCount,
            onNavigateToRecipes = onNavigateToRecipes,
            onNavigateToInventory = onNavigateToInventory,
            onNavigateToSales = onNavigateToSales,
            onNavigateToOrders = onNavigateToOrders
        )
    }
}

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    userName: String,
    suppliesCount: Int,
    batchesCount: Int,
    recipesCount: Int,
    onNavigateToRecipes: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToSales: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    val theme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    // Dynamic greeting based on hour of day
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good morning"
            in 12..18 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Welcome and Header
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$greeting,",
                style = MaterialTheme.typography.titleMedium,
                color = theme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                color = theme.onBackground
            )
        }

        // Live stats panel card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = theme.primaryContainer.copy(alpha = 0.15f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Kitchen Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = theme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(value = "$suppliesCount", label = "Ingredients", modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                            .align(Alignment.CenterVertically)
                            .background(theme.onPrimaryContainer.copy(alpha = 0.15f))
                    )
                    StatItem(value = "$batchesCount", label = "Lots/Batches", modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                            .align(Alignment.CenterVertically)
                            .background(theme.onPrimaryContainer.copy(alpha = 0.15f))
                    )
                    StatItem(value = "$recipesCount", label = "Recipes", modifier = Modifier.weight(1f))
                }
            }
        }

        // Quick Actions section
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Operations Grid",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.Inventory,
                    title = "Stock & Items",
                    description = "Analyze kitchen shelf life",
                    onClick = onNavigateToInventory,
                    modifier = Modifier.weight(1f)
                )

                QuickActionCard(
                    icon = Icons.Default.ShoppingCart,
                    title = "Supplies Order",
                    description = "Request restock items",
                    onClick = onNavigateToOrders,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.Restaurant,
                    title = "Menu Planner",
                    description = "Dishes ingredient recipes",
                    onClick = onNavigateToRecipes,
                    modifier = Modifier.weight(1f)
                )

                QuickActionCard(
                    icon = Icons.Outlined.PointOfSale,
                    title = "Register Sales",
                    description = "Deduct inventory stocks",
                    onClick = onNavigateToSales,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Restaurant context promo card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = theme.primary.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Kitchen,
                        contentDescription = null,
                        tint = theme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Smart Restaurant Planning",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Register recipe sales to automatically deduct stock levels in real time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
    }
}
