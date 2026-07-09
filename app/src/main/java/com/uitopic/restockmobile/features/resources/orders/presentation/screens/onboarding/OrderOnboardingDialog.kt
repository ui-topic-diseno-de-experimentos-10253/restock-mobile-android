package com.uitopic.restockmobile.features.resources.orders.presentation.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.uitopic.restockmobile.ui.theme.RestockmobileTheme
import kotlinx.coroutines.launch

// ─── Data model ──────────────────────────────────────────────────────────────

data class OnboardingPage(
    val icon: ImageVector,
    val badgeIcon: ImageVector? = null,
    val accentColor: Color,
    val title: String,
    val subtitle: String,
    val description: String,
    val tip: String? = null,
    val mockContent: @Composable () -> Unit = {}
)

// ─── Page definitions ────────────────────────────────────────────────────────

@Composable
private fun onboardingPages(): List<OnboardingPage> {
    val primary   = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary  = MaterialTheme.colorScheme.tertiary

    return listOf(
        OnboardingPage(
            icon        = Icons.Default.ShoppingCart,
            accentColor = primary,
            title       = "Pide a tus proveedores",
            subtitle    = "Sistema de órdenes Restock",
            description = "Gestiona fácilmente tus pedidos a proveedores, compara precios y lleva un seguimiento en tiempo real desde un solo lugar.",
            tip         = "¡Ahorra tiempo y reduce costos eligiendo siempre la mejor oferta!"
        ) {
            IntroMockUI()
        },
        OnboardingPage(
            icon        = Icons.Default.Search,
            badgeIcon   = Icons.Default.Inventory,
            accentColor = primary,
            title       = "1. Selecciona insumos",
            subtitle    = "Desde tu inventario registrado",
            description = "Busca cualquier insumo que necesitas reponer. Todos los ingredientes de tu inventario aparecen aquí disponibles para ordenar.",
            tip         = "Puedes filtrar por nombre para encontrar rápidamente lo que buscas."
        ) {
            SearchMockUI()
        },
        OnboardingPage(
            icon        = Icons.Default.Store,
            badgeIcon   = Icons.Default.CompareArrows,
            accentColor = secondary,
            title       = "2. Compara proveedores",
            subtitle    = "Precio, moneda y disponibilidad",
            description = "Todos los proveedores que ofrecen ese insumo aparecerán listados con su precio y stock disponible. Elige uno o varios para combinar tu orden.",
            tip         = "Puedes ordenar por precio para encontrar la oferta más conveniente."
        ) {
            SupplierMockUI()
        },
        OnboardingPage(
            icon        = Icons.Default.Receipt,
            badgeIcon   = Icons.Default.CheckCircle,
            accentColor = primary,
            title       = "3. Revisa y solicita",
            subtitle    = "Confirma cantidades y envía",
            description = "Verifica los insumos agregados, ajusta las cantidades que necesitas y revisa el total antes de enviar la solicitud al proveedor.",
            tip         = "Puedes agregar más insumos de distintos proveedores en una misma orden."
        ) {
            ReviewMockUI()
        },
        OnboardingPage(
            icon        = Icons.Default.LocalShipping,
            badgeIcon   = Icons.Default.Timeline,
            accentColor = tertiary ?: primary,
            title       = "4. Haz seguimiento",
            subtitle    = "Estado de tu orden en tiempo real",
            description = "Una vez enviada, tu orden pasará por distintos estados: Pendiente → Preparando → Despachada → Entregada. Siempre sabrás dónde está.",
            tip         = null
        ) {
            TrackingMockUI()
        }
    )
}

// ─── Mock UI illustrations ────────────────────────────────────────────────────

@Composable
private fun IntroMockUI() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(
                Icons.Default.Inventory to "Insumos",
                Icons.Default.Store     to "Proveedores",
                Icons.Default.Receipt   to "Orden",
                Icons.Default.Done      to "¡Listo!"
            ).forEachIndexed { index, (icon, label) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape  = CircleShape,
                        color  = MaterialTheme.colorScheme.primary.copy(alpha = if (index == 3) 1f else 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint   = if (index == 3) MaterialTheme.colorScheme.onPrimary
                                     else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                }
                if (index < 3) {
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchMockUI() {
    val items = listOf("🌾 Arroz (25 kg)", "🫒 Aceite vegetal (5 L)", "🧅 Cebolla (10 kg)")
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(12.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "Buscar insumos...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items.forEach { item ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item, style = MaterialTheme.typography.bodySmall)
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SupplierMockUI() {
    val suppliers = listOf(
        Triple("Proveedor Lima",  "S/ 45.00", true),
        Triple("Proveedor Norte", "S/ 42.50", false),
        Triple("Agro Sur SAC",   "S/ 48.00", false),
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Arroz (25 kg) — elige proveedor",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        suppliers.forEachIndexed { index, (name, price, best) ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp)),
                color = if (best) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Store,
                            contentDescription = null,
                            tint = if (best) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(name, style = MaterialTheme.typography.bodySmall, fontWeight = if (best) FontWeight.SemiBold else FontWeight.Normal)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            price,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (best) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Checkbox(
                            checked = index == 1,
                            onCheckedChange = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewMockUI() {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        val lineItems = listOf(
            Triple("🌾 Arroz (25 kg)", "10 unid.", "S/ 425"),
            Triple("🫒 Aceite (5 L)",  "5 unid.",  "S/ 225"),
        )
        lineItems.forEach { (name, qty, total) ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(qty, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                    Text(total, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text("S/ 650", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun TrackingMockUI() {
    val steps = listOf(
        Triple("Pendiente",   Icons.Default.Schedule,      true),
        Triple("Preparando",  Icons.Default.Inventory,     true),
        Triple("Despachada",  Icons.Default.LocalShipping, false),
        Triple("Entregada",   Icons.Default.Done,          false),
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, (label, icon, done) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = when {
                        done  -> MaterialTheme.colorScheme.primary
                        index == 2 -> MaterialTheme.colorScheme.primaryContainer
                        else  -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = when {
                            done  -> MaterialTheme.colorScheme.onPrimary
                            index == 2 -> MaterialTheme.colorScheme.primary
                            else  -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, textAlign = TextAlign.Center)
            }
            if (index < steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(
                            if (done) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.outline
                        )
                )
            }
        }
    }
}

// ─── Page indicator dots ──────────────────────────────────────────────────────

@Composable
private fun PagerDotsIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier           = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment  = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isActive) 24.dp else 8.dp,
                animationSpec = tween(300),
                label = "dot_width_$index"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    )
            )
        }
    }
}

// ─── Main dialog ─────────────────────────────────────────────────────────────

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OrderOnboardingDialog(
    onDismiss: () -> Unit
) {
    val pages       = onboardingPages()
    val pagerState  = rememberPagerState(pageCount = { pages.size })
    val scope       = rememberCoroutineScope()
    val isLastPage  = pagerState.currentPage == pages.lastIndex

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress      = true,
            dismissOnClickOutside   = false
        )
    ) {
        Surface(
            modifier      = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape         = RoundedCornerShape(28.dp),
            color         = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // ── Gradient accent header ─────────────────────────────────
                val currentPage = pages[pagerState.currentPage]
                val accentColor = currentPage.accentColor

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.85f),
                                    accentColor.copy(alpha = 0.35f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                currentPage.icon,
                                contentDescription = null,
                                tint     = Color.White,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                        currentPage.badgeIcon?.let { badgeIcon ->
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    badgeIcon,
                                    contentDescription = null,
                                    tint     = accentColor,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }
                        }
                    }
                }

                // ── Content ────────────────────────────────────────────────
                HorizontalPager(
                    state    = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { pageIndex ->
                    val page = pages[pageIndex]
                    Column(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        verticalArrangement   = Arrangement.spacedBy(12.dp)
                    ) {
                        // Chip subtitle
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = page.accentColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text     = page.subtitle,
                                style    = MaterialTheme.typography.labelSmall,
                                color    = page.accentColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        // Title
                        Text(
                            text       = page.title,
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )

                        // Description
                        Text(
                            text  = page.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )

                        // Mock illustration
                        page.mockContent()

                        // Tip card
                        page.tip?.let { tip ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape    = RoundedCornerShape(12.dp),
                                color    = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint     = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text  = tip,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Footer: dots + buttons ─────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    PagerDotsIndicator(
                        pageCount   = pages.size,
                        currentPage = pagerState.currentPage
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!isLastPage) {
                            TextButton(onClick = onDismiss) {
                                Text("Omitir")
                            }
                        }

                        Button(
                            onClick = {
                                if (isLastPage) {
                                    onDismiss()
                                } else {
                                    scope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isLastPage) "¡Entendido!" else "Siguiente")
                            if (!isLastPage) {
                                Spacer(Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun OrderOnboardingDialogPreview() {
    RestockmobileTheme {
        OrderOnboardingDialog(onDismiss = {})
    }
}
