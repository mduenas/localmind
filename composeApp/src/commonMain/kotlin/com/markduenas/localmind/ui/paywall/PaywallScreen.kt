package com.markduenas.localmind.ui.paywall

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.markduenas.localmind.billing.BillingProduct
import com.markduenas.localmind.billing.ProductIds
import com.markduenas.localmind.billing.ProductType

@Composable
fun PaywallContent(
    products: List<BillingProduct>,
    purchaseInProgress: Boolean,
    restoreInProgress: Boolean,
    onPurchase: (String) -> Unit,
    onRestore: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Upgrade to Premium",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Unlock the full power of LocalMind",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))

        // Feature list
        val features = listOf(
            "On-device AI parsing with LLM",
            "Export tasks as JSON",
            "Priority support & future features",
        )
        features.forEach { feature ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = feature,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // Purchase buttons
        val lifetime = products.find { it.id == ProductIds.PREMIUM_LIFETIME }
        val monthly = products.find { it.id == ProductIds.PREMIUM_MONTHLY }

        if (lifetime != null) {
            Button(
                onClick = { onPurchase(lifetime.id) },
                enabled = !purchaseInProgress && !restoreInProgress,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (purchaseInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Lifetime — ${lifetime.formattedPrice}")
                }
            }
        }

        if (monthly != null) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { onPurchase(monthly.id) },
                enabled = !purchaseInProgress && !restoreInProgress,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Monthly — ${monthly.formattedPrice}/mo")
            }
        }

        // Fallback if no products loaded yet
        if (lifetime == null && monthly == null) {
            Button(
                onClick = { onPurchase(ProductIds.PREMIUM_LIFETIME) },
                enabled = !purchaseInProgress && !restoreInProgress,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (purchaseInProgress) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text("Purchase Premium")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(
            onClick = onRestore,
            enabled = !purchaseInProgress && !restoreInProgress,
        ) {
            if (restoreInProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Restore Purchases")
            }
        }

        TextButton(onClick = onDismiss) {
            Text("Maybe Later")
        }
    }
}
