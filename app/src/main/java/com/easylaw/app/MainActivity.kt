package com.easylaw.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.easylaw.app.domain.model.UserInfo
import com.easylaw.app.domain.model.UserSession
import com.easylaw.app.navigation.AppRoute
import com.easylaw.app.navigation.NavRoute
import com.easylaw.app.navigation.NavRoute.bottomItems
import com.easylaw.app.ui.theme.EasyLawTheme
import com.easylaw.app.util.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

private val NAV_BAR_COLOR = Color(0xFFEAEFEF)
private val SELECTED_ICON_COLOR = Color(0xFFD95F1E)
private val UNSELECTED_ICON_COLOR = Color(0xFF797573)
private val DRAWER_BACKGROUND_COLOR = Color.White

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var userSession: UserSession

    @Inject
    lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 앱 시작 시 로컬 저장소에서 유저 정보 불러오기
        lifecycleScope.launch {
            val savedUser = preferenceManager.userData.firstOrNull()
            if (savedUser != null) {
                userSession.setLoginInfo(savedUser)
            } else {
                userSession.sessionClear()
            }
        }

        setContent {
            EasyLawTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                val userInfo by userSession.userInfo.collectAsState()
                // 유저 상태랑 별개로 로딩변수만 따로 감지
                val isInitialized by userSession.isInitialized.collectAsState()

                val hideBarsRoutes = listOf(NavRoute.ONBOARDING, NavRoute.LOGIN, NavRoute.SIGN_UP)

                val startRoute = if (userInfo.id.isNotEmpty()) NavRoute.COMMUNITY else NavRoute.ONBOARDING

                // 세션정보를 가져오는 동안 빈 화면 출력
                if (!isInitialized) {
                    Box(modifier = Modifier.fillMaxSize())
                    return@EasyLawTheme
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    gesturesEnabled = currentRoute !in hideBarsRoutes,
                    drawerContent = {
                        EasylawSideBar(
                            userInfo = userInfo,
                            onLogoutClick = {
                                scope.launch {
                                    userSession.sessionClear()
                                    preferenceManager.sessionClear()

                                    drawerState.close()

                                    if (navController.currentBackStackEntry?.destination?.route != NavRoute.ONBOARDING) {
                                        navController.navigate(NavRoute.ONBOARDING) {
                                            popUpTo(navController.graph.id) {
                                                inclusive = true
                                            }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            },
                        )
                    },
                ) {
                    Scaffold(
                        bottomBar = {
                            if (currentRoute !in hideBarsRoutes) {
                                NavigationBar(
                                    containerColor = NAV_BAR_COLOR,
                                    tonalElevation = 8.dp,
                                ) {
                                    bottomItems.forEach { item ->
                                        val isSelected = currentRoute == item.route
                                        NavigationBarItem(
                                            selected = isSelected,
                                            label = { Text(text = item.title) },
                                            icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                                            colors =
                                                NavigationBarItemDefaults.colors(
                                                    selectedIconColor = SELECTED_ICON_COLOR,
                                                    selectedTextColor = SELECTED_ICON_COLOR,
                                                    unselectedIconColor = UNSELECTED_ICON_COLOR,
                                                    unselectedTextColor = UNSELECTED_ICON_COLOR,
                                                ),
                                            onClick = {
                                                if (currentRoute != item.route) {
                                                    navController.navigate(item.route) {
                                                        popUpTo(navController.graph.findStartDestination().id) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        },
                    ) { innerPadding ->
                        AppRoute(
                            modifier = if (currentRoute in hideBarsRoutes) Modifier else Modifier.padding(innerPadding),
                            navController = navController,
                            startDestination = startRoute,
                        )
                    }
                }
            }
        }
    }
}

private val DRAWER_DIVIDER_COLOR = Color(0xFFEEEEEE)

@Composable
fun EasylawSideBar(
    userInfo: UserInfo,
    onLogoutClick: () -> Unit,
) {
    ModalDrawerSheet(
        drawerContainerColor = DRAWER_BACKGROUND_COLOR,
        modifier = Modifier.width(280.dp),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .padding(20.dp),
        ) {
            // 상단 유저 정보 영역
            Text(
                text = if (userInfo.id.isNotEmpty()) "${userInfo.name}님 환영합니다" else "로그인이 필요합니다",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black,
            )
            Text(
                text = userInfo.email,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
            )

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(thickness = 1.dp, color = DRAWER_DIVIDER_COLOR)
            Spacer(modifier = Modifier.height(12.dp))

            // 메뉴 리스트
            NavigationDrawerItem(
                label = { Text(text = "로그아웃", color = Color.Red) },
                selected = false,
                onClick = onLogoutClick,
                icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color.Red) },
                colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent),
            )
        }
    }
}
