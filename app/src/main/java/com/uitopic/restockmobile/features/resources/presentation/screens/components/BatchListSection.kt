package com.uitopic.restockmobile.features.resources.presentation.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LayersClear
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
import com.uitopic.restockmobile.features.resources.domain.models.Batch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun getDaysDifference(dateStr: String?): Long {
    if (dateStr == null || dateStr == "9999-12-31") return 999
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val expDate = sdf.parse(dateStr) ?: return 999
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val diffMs = expDate.time - today.time
        diffMs / (1000 * 60 * 60 * 24)
    } catch (e: Exception) {
        999
    }
}

@Composable
fun BatchListSection(
    batches: List<Batch>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBatchClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = MaterialTheme.colorScheme

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Inventory Batches / Lots",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = theme.onBackground
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.Gray) },
            placeholder = { Text("Search ingredient or batch ID...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = theme.primary,
                unfocusedBorderColor = Color(0xFFE0E0E0),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        if (batches.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .height(150.dp),
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
                        imageVector = Icons.Default.LayersClear,
                        contentDescription = null,
                        tint = theme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No active batches registered",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(batches) { batch ->
                    val isNonPerishable = batch.expirationDate == "9999-12-31"
                    val daysLeft = getDaysDifference(batch.expirationDate)
                    val isExpiringSoon = !isNonPerishable && daysLeft in 0..7

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF5F5F5))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = batch.customSupply?.supply?.name ?: "Unnamed Ingredient",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = (-0.2).sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = theme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Batch ID: #${batch.id.take(8)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(theme.primary.copy(alpha = 0.08f), shape = RoundedCornerShape(18.dp))
                                        .clip(RoundedCornerShape(18.dp))
                                        .clickable { onBatchClick(batch.id) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Batch",
                                        tint = theme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = theme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Stock: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${batch.stock} ${batch.customSupply?.unit?.abbreviation ?: ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = theme.onSurface
                                )
                            }

                            HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp)

                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = batch.customSupply?.supply?.category ?: "General",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )

                                val (tagBgColor, tagTextColor, tagText) = when {
                                    isNonPerishable -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Non-perishable")
                                    isExpiringSoon -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Expiring soon (${daysLeft}d)")
                                    else -> Triple(Color(0xFFFFF3E0), Color(0xFFEF6C00), "Expires: ${batch.expirationDate}")
                                }

                                Box(
                                    modifier = Modifier
                                        .background(tagBgColor, shape = RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = tagTextColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = tagText,
                                            color = tagTextColor,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
