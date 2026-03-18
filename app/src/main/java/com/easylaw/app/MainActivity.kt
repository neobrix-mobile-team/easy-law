package com.easylaw.app

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.easylaw.app.domain.model.UserSession
import com.easylaw.app.navigation.AppRoute
import com.easylaw.app.navigation.NavRoute
import com.easylaw.app.navigation.NavRoute.bottomItems
import com.easylaw.app.ui.components.EasylawSideBar
import com.easylaw.app.ui.components.LanguageBottombar
import com.easylaw.app.ui.theme.EasyLawTheme
import com.easylaw.app.util.PreferenceManager
import com.easylaw.app.viewModel.RememberMainViewState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

private val NAV_BAR_COLOR = Color(0xFFEAEFEF)
private val SELECTED_ICON_COLOR = Color(0xFFD95F1E)
private val UNSELECTED_ICON_COLOR = Color(0xFF797573)
private val DRAWER_BACKGROUND_COLOR = Color.White
private val LANGUAGE_DISPLAY_MAP =
    mapOf(
        "ko" to "한국어",
        "en" to "English",
        "ja" to "日本語",
    )

fun applyLocale(
    context: Context,
    languageCode: String,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        context
            .getSystemService(LocaleManager::class.java)
            .applicationLocales = LocaleList.forLanguageTags(languageCode)
    } else {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageCode),
        )
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var userSession: UserSession

    @Inject
    lateinit var preferenceManager: PreferenceManager

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (savedInstanceState == null) {
            val savedLanguage = preferenceManager.languageState.value
            applyLocale(this, savedLanguage)
        }

        // 앱 시작 시 로컬 저장소에서 유저 정보 불러오기
        if (!userSession.isInitialized.value) {
            lifecycleScope.launch {
                val savedUser = preferenceManager.userData.firstOrNull()
                if (savedUser != null) {
                    userSession.setLoginInfo(savedUser)
                } else {
                    userSession.sessionClear()
                    userSession.finishInitialized()
                }
            }
        }

        setContent {
            EasyLawTheme {
                val state = RememberMainViewState()
                val navBackStackEntry by state.navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val userInfo by userSession.userInfo.collectAsState()
                // 유저 상태랑 별개로 로딩변수만 따로 감지
                val isInitialized by userSession.isInitialized.collectAsState()

                val currentLanguageCode by preferenceManager.languageState.collectAsState()
                val currentLanguageDisplay = LANGUAGE_DISPLAY_MAP[currentLanguageCode] ?: "한국어"

                val hideBarsRoutes =
                    listOf(
                        NavRoute.ONBOARDING,
                        NavRoute.LOGIN,
                        NavRoute.SIGN_UP,
                        NavRoute.MAP,
                    )

                val startRoute = if (userInfo.id.isNotEmpty()) NavRoute.COMMUNITY else NavRoute.ONBOARDING

                // 세션정보를 가져오는 동안 빈 화면 출력
//                if (!isInitialized) {
//                    Box(modifier = Modifier.fillMaxSize())
//                    return@EasyLawTheme
//                }

                ModalNavigationDrawer(
                    drawerState = state.drawerState,
                    gesturesEnabled = currentRoute !in hideBarsRoutes,
                    drawerContent = {
                        EasylawSideBar(
                            userInfo = userInfo,
                            selectedLanguage = currentLanguageDisplay,
                            currentRoute = currentRoute,
                            onLanguageClick = {
                                state.scope.launch {
                                    state.drawerState.close()
                                    state.showLanguageSheet.value = true
                                }
                            },
                            onMenuClick = { route ->
                                state.scope.launch {
                                    state.drawerState.close()
                                    if (currentRoute != route) {
                                        state.navController.navigate(route) {
                                            popUpTo(
                                                state.navController.graph
                                                    .findStartDestination()
                                                    .id,
                                            ) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            },
                            onLogoutClick = {
                                state.scope.launch {
                                    userSession.sessionClear()
                                    preferenceManager.sessionClear()
                                    state.drawerState.close()
                                    if (state.navController.currentBackStackEntry
                                            ?.destination
                                            ?.route != NavRoute.ONBOARDING
                                    ) {
                                        state.navController.navigate(NavRoute.ONBOARDING) {
                                            popUpTo(state.navController.graph.id) {
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
                        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
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
                                            label = {
                                                Text(
                                                    text = stringResource(item.titleResId),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    fontSize = 10.sp,
                                                )
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = item.icon,
                                                    contentDescription = stringResource(item.titleResId),
                                                )
                                            },
                                            colors =
                                                NavigationBarItemDefaults.colors(
                                                    selectedIconColor = SELECTED_ICON_COLOR,
                                                    selectedTextColor = SELECTED_ICON_COLOR,
                                                    unselectedIconColor = UNSELECTED_ICON_COLOR,
                                                    unselectedTextColor = UNSELECTED_ICON_COLOR,
                                                ),
                                            onClick = {
                                                if (currentRoute != item.route) {
                                                    state.navController.navigate(item.route) {
                                                        popUpTo(
                                                            state.navController.graph
                                                                .findStartDestination()
                                                                .id,
                                                        ) {
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
                            navController = state.navController,
                            startDestination = startRoute,
                        )
                    }
                }

                if (state.showLanguageSheet.value) {
                    ModalBottomSheet(
                        onDismissRequest = { state.showLanguageSheet.value = false },
                        sheetState = state.sheetState,
                    ) {
                        LanguageBottombar(
                            currentLanguageCode = currentLanguageCode,
                            onLanguageSelected = { selectedCode ->
                                state.scope.launch {
                                    preferenceManager.saveLanguage(selectedCode)
                                    applyLocale(this@MainActivity, selectedCode)
                                    state.sheetState.hide()
                                    state.showLanguageSheet.value = false
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
