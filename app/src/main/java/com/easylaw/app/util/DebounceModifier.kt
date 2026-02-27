package com.easylaw.app.util

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
inline fun Modifier.debouncedClickable(
    debounceTime: Long = 500L,
    crossinline onClick: () -> Unit,
): Modifier {
    val lastClickTime = remember { mutableLongStateOf(0L) }
    return this.clickable {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime.longValue > debounceTime) {
            lastClickTime.longValue = currentTime
            onClick()
        }
    }
}
