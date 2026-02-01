package com.aerofit.india.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aerofit.india.ui.theme.CardBackground
import com.aerofit.india.ui.theme.TextPrimary

@Composable
fun AqiGauge(
    aqiValue: Int,
    aqiColor: Color,
    pollutant: String,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(200.dp)
            .background(CardBackground, shape = CircleShape)
            .border(8.dp, aqiColor, CircleShape)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Text(
                text = aqiValue.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "AQI",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pollutant,
                fontSize = 14.sp,
                color = aqiColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
