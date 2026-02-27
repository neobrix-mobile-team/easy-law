package com.easylaw.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LanguageBottombar(onLanguageSelected: (String) -> Unit) {
    // 선택 가능한 언어 리스트
    val languages =
        listOf(
            "한국어" to "ko",
            "English" to "en",
            "日本語" to "ja",
        )

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 8.dp, bottom = 24.dp),
    ) {
        // 상단 타이틀
        Text(
            text = "언어 선택",
            style =
                TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191F28),
                ),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )

        // 언어 목록 리스트
        languages.forEach { (name, code) ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected(name) }
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = name,
                    style =
                        TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF333D4B),
                        ),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
