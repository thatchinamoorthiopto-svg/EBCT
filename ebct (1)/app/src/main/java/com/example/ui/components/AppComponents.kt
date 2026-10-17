package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandGreen
import com.example.ui.theme.BrandGreenDark
import com.example.ui.theme.BrandGreenSurface
import com.example.ui.theme.BrandNavy

@Composable
fun TariffStatusBadge(
    state: String = "Tamil Nadu",
    isVerified: Boolean = true,
    effectiveDate: String = "01-Jul-2024",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isVerified) BrandGreenSurface else Color(0xFFFEF3C7))
            .border(
                width = 1.dp,
                color = if (isVerified) BrandGreenDark.copy(alpha = 0.3f) else Color(0xFFF59E0B),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = if (isVerified) "Verified Tariff" else "Unverified Tariff",
                tint = if (isVerified) BrandGreenDark else Color(0xFFB45309),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isVerified) "$state Official Tariff (Eff. $effectiveDate)" else "$state Tariff: Unverified",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isVerified) BrandGreenDark else Color(0xFF92400E)
                )
            )
        }
    }
}

@Composable
fun DisclaimerCard(
    modifier: Modifier = Modifier,
    text: String = "This amount is an estimate based on the meter readings entered and the applicable tariff rules. Your actual electricity bill may differ."
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Disclaimer",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 1.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun ElectricIconBadge(
    modifier: Modifier = Modifier,
    backgroundColor: Color = BrandGreenSurface,
    iconTint: Color = BrandGreenDark
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "Electricity Icon",
            tint = iconTint,
            modifier = Modifier.size(26.dp)
        )
    }
}
