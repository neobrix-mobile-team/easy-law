package com.easylaw.app.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 일반 버튼
@Composable
fun CommonButton(
    modifier: Modifier,
    onClick: () -> Unit,
    icon: ImageVector,
    color: Color,
    text: String,
    isEnable: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        enabled = isEnable,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = color,
                disabledContentColor = Color(0xFFD1D9E0),
            ),
        elevation =
            ButtonDefaults.buttonElevation(
                defaultElevation = if (isEnable) 6.dp else 0.dp,
            ),
    ) {
        Icon(icon, contentDescription = null, tint = if (isEnable) Color.White else Color.Gray)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}
