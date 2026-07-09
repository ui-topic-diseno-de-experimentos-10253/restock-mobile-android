package com.uitopic.restockmobile.features.resources.presentation.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uitopic.restockmobile.features.resources.domain.models.CustomSupply

@Composable
fun SupplyCatalogSection(
    supplies: List<CustomSupply>,
    onAddSupplyClick: () -> Unit,
    onViewSupplyDetails: (CustomSupply) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = MaterialTheme.colorScheme

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "My Ingredients",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = theme.onBackground
            )
            Button(
                onClick = onAddSupplyClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = theme.primary,
                    contentColor = theme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add", fontWeight = FontWeight.Bold)
            }
        }

        if (supplies.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F1F1))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = null,
                        tint = theme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No ingredients registered",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(supplies) { custom ->
                    SupplyCard(custom, onViewSupplyDetails)
                }
            }
        }
    }
}

@Composable
private fun SupplyCard(
    custom: CustomSupply,
    onViewSupplyDetails: (CustomSupply) -> Unit
) {
    val theme = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .width(200.dp)
            .height(180.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Name
                Text(
                    text = custom.supply?.name ?: "Unnamed Ingredient",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = theme.onSurface
                )

                // Category and Unit Chips Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(theme.primary.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = custom.unit.abbreviation,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = theme.primary
                            )
                        )
                    }

                    val category = custom.supply?.category
                    if (!category.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .weight(1f, fill = false)
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Range Stock target
                Text(
                    text = "Stock target: ${custom.minStock} - ${custom.maxStock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Bottom row: Price and details button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "S/. ${String.format("%.2f", custom.price)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = theme.primary
                    )
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(theme.primary.copy(alpha = 0.08f), shape = RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onViewSupplyDetails(custom) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Details",
                        tint = theme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
