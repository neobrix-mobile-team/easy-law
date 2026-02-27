package com.easylaw.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.easylaw.app.data.repo.AIRepo
import com.easylaw.app.domain.model.UserSession
import com.easylaw.app.navigation.AppRoute
import com.easylaw.app.navigation.navRoute
import com.easylaw.app.navigation.navRoute.bottomItems
import com.easylaw.app.ui.components.CommonIndicator
import com.easylaw.app.ui.components.EasylawSideBar
import com.easylaw.app.ui.components.LanguageBottombar
import com.easylaw.app.ui.theme.EasyLawTheme
import com.easylaw.app.util.PreferenceManager
import com.easylaw.app.viewmodel.RememberMainViewState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

// @Inject 선언은 메서드 안으로 못간다.
class InitDi @Inject constructor(
    val userSession: UserSession,
    val preferenceManager: PreferenceManager,
    val aiManager: AIRepo,
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var initDi: InitDi

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 앱 시작 시 로컬 저장소에서 유저 정보 불러오기
        lifecycleScope.launch {
            val savedUser = initDi.preferenceManager.userData.firstOrNull()
            if (savedUser != null) {
                initDi.userSession.setLoginInfo(savedUser)
            } else {
                initDi.userSession.finishInitialzed()
            }
        }

        setContent {
            EasyLawTheme {

                val mainState = RememberMainViewState()

                val navBackStackEntry by mainState.navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                // 유저 세션 감지
                val userInfo by initDi.userSession.userInfo.collectAsState()
                val isInitialized by initDi.userSession.isInitialized.collectAsState()
                val startRoute = if (userInfo.id.isNotEmpty()) navRoute.community else navRoute.onboarding

                val geminiState by initDi.aiManager.loadingState.collectAsState()

                val hideBarsRoutes = listOf(navRoute.onboarding, navRoute.login, navRoute.signUp)

                // 세션정보를 가져오는 동안 빈 화면 출력
                if (!isInitialized) {
                    Box(modifier = Modifier.fillMaxSize())
                    return@EasyLawTheme
                }

                ModalNavigationDrawer(
                    drawerState = mainState.drawerState,
                    gesturesEnabled = currentRoute !in hideBarsRoutes,
                    drawerContent = {
                        EasylawSideBar(
                            userInfo = userInfo,
                            selectedLanguage = "한국어",
                            onLanguageClick = {
                                mainState.scope.launch {
                                    mainState.drawerState.close()
                                    mainState.showLanguageSheet.value = true
                                }
                            },
                            onLogoutClick = {
                                mainState.scope.launch {
                                    initDi.userSession.sessionClear()
                                    initDi.preferenceManager.sessionClear()
                                    mainState.drawerState.close()

                                    if (mainState.navController.currentBackStackEntry?.destination?.route != navRoute.onboarding) {
                                        mainState.navController.navigate(navRoute.onboarding) {
                                            popUpTo(mainState.navController.graph.id) {
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
                                    containerColor = Color(0xFFEAEFEF),
                                    tonalElevation = 8.dp,
                                ) {
                                    bottomItems.forEach { item ->
                                        val isSelected = currentRoute == item.route
                                        NavigationBarItem(
                                            selected = isSelected,
                                            label = { Text(text = item.title) },
                                            icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color(0xFFD95F1E),
                                                selectedTextColor = Color(0xFFD95F1E),
                                                unselectedIconColor = Color(0xFF797573),
                                                unselectedTextColor = Color(0xFF797573),
                                            ),
                                            onClick = {
                                                if (currentRoute != item.route) {
                                                    mainState.navController.navigate(item.route) {
                                                        popUpTo(mainState.navController.graph.findStartDestination().id) {
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
                            navController = mainState.navController,
                            startDestination = startRoute,
                        )
                    }
                }

                if ( mainState.showLanguageSheet.value) {
                    ModalBottomSheet(
                        onDismissRequest = {  mainState.showLanguageSheet.value = false },
                        sheetState = mainState.sheetState,
                        containerColor = Color.White,
                        dragHandle = { BottomSheetDefaults.DragHandle() }
                    ) {
                        LanguageBottombar(
                            onLanguageSelected = { language ->
                                mainState.scope.launch {
                                    mainState.sheetState.hide()
                                }.invokeOnCompletion {
                                    if (!mainState.sheetState.isVisible) {
                                         mainState.showLanguageSheet.value = false
                                    }
                                }
                            }
                        )
                    }
                }

                // AI 로딩 인디케이터
                if (geminiState.isLoading) {
                    CommonIndicator(geminiState.message)
                }
            }
        }
    }
}