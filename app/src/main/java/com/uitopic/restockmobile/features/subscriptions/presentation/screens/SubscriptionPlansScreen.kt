package com.uitopic.restockmobile.features.subscriptions.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uitopic.restockmobile.features.subscriptions.domain.models.SubscriptionPlan
import com.uitopic.restockmobile.ui.theme.RestockmobileTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionPlansScreen(onSubscribeClick: (planType: Int) -> Unit = {}) {
    val theme = MaterialTheme.colorScheme

    val plans = listOf(
        SubscriptionPlan(
            name = "Annual Plan",
            price = "S/. 39.99 / mo",
            features = listOf(
                "Automated ingredient cataloging",
                "Dish recipe planning & margins",
                "Inventory expiration alerts",
                "Automatic low stock notifications",
                "Instant supplier order dispatch"
            ),
            popular = true
        ),
        SubscriptionPlan(
            name = "Semester Plan",
            price = "S/. 49.99 / mo",
            features = listOf(
                "Automated ingredient cataloging",
                "Dish recipe planning & margins",
                "Inventory expiration alerts",
                "Automatic low stock notifications",
                "Instant supplier order dispatch"
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Restaurant Plans", 
                        fontWeight = FontWeight.Black, 
                        style = MaterialTheme.typography.titleMedium
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = theme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Subtle top gradient accent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                theme.primary.copy(alpha = 0.06f),
                                Color.Transparent
                            )
                        )
                    )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                item {
                    // Header Intro
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Optimize Your Kitchen",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Choose a premium plan to unlock full restaurant tools",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                itemsIndexed(plans) { index, plan ->
                    // index 0 = Anual (planType 1), index 1 = Semester (planType 2)
                    val planType = index + 1
                    SubscriptionPlanItem(
                        plan = plan,
                        onSubscribeClick = { onSubscribeClick(planType) }
                    )
                }
            }
        }
    }
}

@Composable
fun SubscriptionPlanItem(
    plan: SubscriptionPlan,
    onSubscribeClick: () -> Unit
) {
    val theme = MaterialTheme.colorScheme

    val cardBorderModifier = if (plan.popular) {
        Modifier.border(2.dp, theme.primary, RoundedCornerShape(24.dp))
    } else {
        Modifier.border(1.dp, Color(0xFFEBEBEB), RoundedCornerShape(24.dp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(cardBorderModifier)
            .shadow(
                elevation = if (plan.popular) 8.dp else 2.dp,
                shape = RoundedCornerShape(24.dp),
                clip = false
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                if (plan.popular) {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Plan Name
                Text(
                    text = plan.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = if (plan.popular) theme.primary else theme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Price display
                val priceParts = plan.price.split(" ")
                val priceAmt = priceParts.firstOrNull() ?: ""
                val pricePeriod = plan.price.replace(priceAmt, "").trim()

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = priceAmt,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = theme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = pricePeriod,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFF1F1F1), thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))

                // Features list
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    plan.features.forEach { feature ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(theme.primary.copy(alpha = 0.08f), shape = RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = theme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = feature,
                                fontSize = 13.sp,
                                color = theme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Subscribe CTA Button
                Button(
                    onClick = onSubscribeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (plan.popular) theme.primary else Color(0xFFF5F5F5),
                        contentColor = if (plan.popular) theme.onPrimary else Color.DarkGray
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (plan.popular) 2.dp else 0.dp)
                ) {
                    Text(
                        text = if (plan.popular) "Get Started Now" else "Choose Plan",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (plan.popular) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            color = theme.primary,
                            shape = RoundedCornerShape(bottomStart = 16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.StarOutline,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "BEST VALUE",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubscriptionPlansScreenPreview() {
    RestockmobileTheme {
        SubscriptionPlansScreen()
    }
}
