package com.easylaw.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.load
import com.github.chrisbanes.photoview.PhotoView

@Composable
fun CommonPreview(
    previewImage: String,
    clickable: () -> Unit,
) {
    if (previewImage.isEmpty()) return
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = true),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(500.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White),
        ) {
            AndroidView(
                factory = { context ->
                    PhotoView(context).apply {
                        load(previewImage)
                    }
                },
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(12.dp),
            )
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { clickable() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp),
                )
            }
        }
    }
}
