package com.easylaw.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easylaw.app.R
import com.easylaw.app.domain.model.UserInfo
import com.easylaw.app.navigation.NavRoute
import com.easylaw.app.util.Common

private data class SidebarMenuItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val groupLabel: String? = null,
)

@Composable
fun EasylawSideBar(
    userInfo: UserInfo,
    selectedLanguage: String = stringResource(R.string.lang_korean),
    currentRoute: String? = null,
    onLanguageClick: () -> Unit, // 언어 선택창
    onMenuClick: (route: String) -> Unit,
    onLogoutClick: () -> Unit, // 로그아웃
) {
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val appVersion = Common.getAppVersion(context)

    val sidebarMenuItems =
        listOf(
            SidebarMenuItem(NavRoute.COMMUNITY, stringResource(R.string.sidebar_menu_community), Icons.Default.Share, groupLabel = stringResource(R.string.sidebar_group_community)),
            SidebarMenuItem(NavRoute.LAW_CONSULT, stringResource(R.string.sidebar_menu_precedent), Icons.Default.Gavel, groupLabel = stringResource(R.string.sidebar_group_legal)),
            SidebarMenuItem(NavRoute.SELF, stringResource(R.string.sidebar_menu_self_diagnosis), Icons.Default.Check),
            SidebarMenuItem(NavRoute.MAP, stringResource(R.string.sidebar_menu_nearby), Icons.Default.Map),
        )

    ModalDrawerSheet(
        drawerContainerColor = colorScheme.surface,
        modifier = Modifier.width(300.dp),
        drawerShape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 40.dp),
        ) {
            Text(
                text = if (userInfo.name.isNotEmpty()) stringResource(R.string.sidebar_greeting, userInfo.name) else stringResource(R.string.sidebar_guest),
                style =
                    TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorScheme.onSurface, // 기본 텍스트
                    ),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userInfo.email.ifEmpty { "easylaw@example.com" },
                style = TextStyle(fontSize = 14.sp, color = colorScheme.onSurfaceVariant),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                onClick = onLanguageClick,
                color = colorScheme.surfaceVariant,
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
                        tint = colorScheme.tertiary,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedLanguage,
                        style =
                            TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant,
                            ),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            HorizontalDivider(thickness = 1.dp, color = colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            // ── 메뉴 리스트 ───────────────────────────────────
            sidebarMenuItems.forEach { item ->

                // 그룹 구분선 + 라벨
                item.groupLabel?.let { label ->
                    if (item != sidebarMenuItems.first { it.groupLabel != null }) {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(thickness = 1.dp, color = colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Text(
                        text = label,
                        style =
                            TextStyle(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colorScheme.onSurfaceVariant, // Soft Grey
                            ),
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                    )
                }

                val isSelected = currentRoute == item.route

                NavigationDrawerItem(
                    label = {
                        Text(
                            text = item.label,
                            style =
                                TextStyle(
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                ),
                        )
                    },
                    selected = isSelected,
                    onClick = { onMenuClick(item.route) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    colors =
                        NavigationDrawerItemDefaults.colors(
                            // 선택 상태: primary(Deep Blue) 계열
                            selectedContainerColor = colorScheme.primaryContainer,
                            selectedIconColor = colorScheme.primary,
                            selectedTextColor = colorScheme.primary,
                            // 미선택 상태: secondary(Soft Grey) 계열
                            unselectedContainerColor = colorScheme.surface,
                            unselectedIconColor = colorScheme.onSurfaceVariant,
                            unselectedTextColor = colorScheme.onSurface,
                        ),
                    modifier = Modifier.height(48.dp),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── 로그아웃 ──────────────────────────────────────
            HorizontalDivider(thickness = 1.dp, color = colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            NavigationDrawerItem(
                label = {
                    Text(
                        text = stringResource(R.string.logout),
                        style =
                            TextStyle(
                                fontSize = 14.sp,
                                color = colorScheme.onSurfaceVariant,
                            ),
                    )
                },
                selected = false,
                onClick = onLogoutClick,
                icon = {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                },
                colors =
                    NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = colorScheme.surface,
                        unselectedIconColor = colorScheme.onSurfaceVariant,
                        unselectedTextColor = colorScheme.onSurfaceVariant,
                    ),
            )
            Text(
                text = "Version $appVersion",
                style =
                    TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = colorScheme.onSurfaceVariant.copy(alpha = 0.6f), // 약간 흐리게 처리
                    ),
                modifier = Modifier.padding(start = 12.dp, bottom = 8.dp),
            )
        }
    }
}
