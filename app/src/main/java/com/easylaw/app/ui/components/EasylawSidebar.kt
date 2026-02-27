package com.easylaw.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easylaw.app.domain.model.UserInfo

@Composable
fun EasylawSideBar(
    userInfo: UserInfo,
    selectedLanguage: String = "한국어", // 임시
    onLanguageClick: () -> Unit, // 언어 선택창
    onLogoutClick: () -> Unit, // 로그아웃
) {
    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        modifier = Modifier.width(300.dp),
        drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (userInfo.name.isNotEmpty()) "${userInfo.name}님" else "사용자님",
                    style =
                        TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191F28),
                        ),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = userInfo.email.ifEmpty { "easylaw@example.com" },
                    style = TextStyle(fontSize = 14.sp, color = Color(0xFF8B95A1)),
                )

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    onClick = onLanguageClick,
                    color = Color(0xFFF2F4F6),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF4E5968),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedLanguage,
                            style =
                                TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4E5968),
                                ),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF8B95A1),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF2F4F6))
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Share, // 커뮤니티 아이콘
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color(0xFF8B95A1),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "커뮤니티",
                    style =
                        TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8B95A1),
                        ),
                )
            }

            NavigationDrawerItem(
                label = {
                    Text(
                        text = "내가 쓴 글",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    )
                },
                selected = false,
                onClick = {},
                icon = { Icon(Icons.Default.Description, contentDescription = null) },
                colors =
                    NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedIconColor = Color(0xFF4E5968),
                        unselectedTextColor = Color(0xFF333D4B),
                    ),
                modifier = Modifier.height(48.dp),
            )

            Spacer(modifier = Modifier.weight(1f))

            NavigationDrawerItem(
                label = { Text(text = "로그아웃", fontSize = 14.sp) },
                selected = false,
                onClick = onLogoutClick,
                icon = {
                    Icon(
                        Icons.Default.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
                colors =
                    NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = Color.Transparent,
                        unselectedIconColor = Color(0xFFB0B8C1),
                        unselectedTextColor = Color(0xFFB0B8C1),
                    ),
            )
        }
    }
}
