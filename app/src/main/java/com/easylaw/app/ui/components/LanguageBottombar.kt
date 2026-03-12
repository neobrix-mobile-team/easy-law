package com.easylaw.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easylaw.app.R

private val SELECTED_COLOR = Color(0xFFD95F1E)
private val DIVIDER_COLOR = Color(0xFFF2F4F6)

private val LANGUAGES =
    listOf(
        "ko" to "한국어",
        "en" to "English",
        "ja" to "日本語",
    )

@Composable
fun LanguageBottombar(
    currentLanguageCode: String = "ko",
    onLanguageSelected: (code: String) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(top = 8.dp, bottom = 24.dp),
    ) {
        // 상단 타이틀
        Text(
            text = stringResource(R.string.lang_select_title),
            style =
                TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191F28),
                ),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
        HorizontalDivider(thickness = 1.dp, color = DIVIDER_COLOR)

        LANGUAGES.forEach { (code, name) ->
            val isSelected = code == currentLanguageCode
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable { onLanguageSelected(code) }
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = name,
                    style =
                        TextStyle(
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SELECTED_COLOR else Color(0xFF333D4B),
                        ),
                    modifier = Modifier.weight(1f),
                )
                // 현재 선택된 언어 체크마크
                if (isSelected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.lang_selected_desc),
                        tint = SELECTED_COLOR,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            if (code != LANGUAGES.last().first) {
                HorizontalDivider(thickness = 1.dp, color = DIVIDER_COLOR, modifier = Modifier.padding(horizontal = 24.dp))
            }
        }
    }
}
