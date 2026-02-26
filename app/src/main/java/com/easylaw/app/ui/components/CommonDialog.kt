package com.easylaw.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CommonDialog(
    title: String,
    desc: String,
    icon: ImageVector,
    onConfirm: () -> Unit,
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = { }) {
        androidx.compose.material3.Surface(
            shape =
                androidx.compose.foundation.shape
                    .RoundedCornerShape(24.dp),
            // 토스 특유의 깊은 곡률
            color = Color.White,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                Icon(
                    imageVector = icon,
                    contentDescription = "Success",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFF3182F6),
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style =
                        TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191F28), // 토스 텍스트 컬러
                        ),
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = desc,
                    textAlign = TextAlign.Center,
                    style =
                        TextStyle(
                            fontSize = 15.sp,
                            color = Color(0xFF4E5968), // 토스 서브 텍스트 컬러
                            lineHeight = 22.sp,
                        ),
                )

                Spacer(modifier = Modifier.height(32.dp))

                androidx.compose.material3.Button(
                    onClick = onConfirm,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                    shape =
                        androidx.compose.foundation.shape
                            .RoundedCornerShape(14.dp),
                    colors =
                        androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3182F6), // 토스 블루
                        ),
                ) {
                    Text(text = "확인", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
