package com.easylaw.app

import android.app.LocaleManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.flow.first
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
    @Inject lateinit var userSession: UserSession

    @Inject lateinit var supabase: SupabaseClient

    @Inject lateinit var preferenceManager: PreferenceManager

//    private var pendingPostId: String? = null
    private var pendingPostId by mutableStateOf<String?>(null)

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM_TEST", "토큰 가져오기 실패", task.exception)
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d("FCM_TEST", "내 폰의 진짜 토큰: $token")
        }

//        Log.d("DeepLink", "intent.data = ${intent.data}")
//        Log.d("DeepLink", "intent.extras = ${intent.extras?.keySet()}")
//
//        try {
//            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
//            for (signature in info.signatures!!) {
//                val md = MessageDigest.getInstance("SHA")
//                md.update(signature.toByteArray())
//                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT))
//            }
//        } catch (e: Exception) {
//            Log.e("KeyHash", e.toString())
//        }

        enableEdgeToEdge()

        if (savedInstanceState == null) {
            val savedLanguage = preferenceManager.languageState.value
            applyLocale(this, savedLanguage)
        }

        pendingPostId = intent.data?.getQueryParameter("postId")
//        Log.d("DeepLink", "pendingPostId = $pendingPostId")

        // 앱 시작 시 로컬 저장소 및 세션 정보 불러오기
        lifecycleScope.launch {
            try {
                val savedUser = preferenceManager.userData.first()
                val currentSupabaseSession = supabase.auth.currentSessionOrNull()

                Log.d("session 유지 확인", "savedUser: $savedUser, currentSupabaseSession: $currentSupabaseSession")

                if (savedUser != null && savedUser.id.isNotEmpty()) {
                    userSession.setLoginInfo(savedUser)
                } else {
                    userSession.sessionClear()
                    preferenceManager.sessionClear()
                    if (currentSupabaseSession != null) supabase.auth.signOut()
//                    supabase.auth.signOut()
                    userSession.setInitialized(true)
                }
            } catch (e: Exception) {
                Log.e("session error", "유저 정보 로드 실패: ${e.message}")
                userSession.setInitialized(true)
            }
//            finally {
//                // 성공/실패 여부와 상관없이 초기화 완료 신호 전달
//                userSession.setInitialized(true)
//            }
        }

        setContent {
            EasyLawTheme {
                // 상태 관찰
                val userInfo by userSession.userInfo.collectAsState()
                Log.d("userinfo", userSession.getUserState().toString())
                val isInitialized by userSession.isInitialized.collectAsState()

                if (!isInitialized) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.White),
                        contentAlignment = Alignment.Center,
                    ) {
                    }
                } else {
                    val state = RememberMainViewState()
                    val navBackStackEntry by state.navController.currentBackStackEntryAsState()
//                    val navController = rememberNavController()
//                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val currentLanguageCode by preferenceManager.languageState.collectAsState()
                    val currentLanguageDisplay = LANGUAGE_DISPLAY_MAP[currentLanguageCode] ?: "한국어"

                    // 로그인 상태에 따른 시작 경로 결정
                    val startRoute = if (userInfo.id.isNotEmpty()) NavRoute.COMMUNITY else NavRoute.ONBOARDING
                    Log.d("시작 위치", startRoute)
//                    LaunchedEffect(navController) {
//                        pendingPostId?.let { postId ->
//                            navController.navigate("communityDetail/$postId")
//                            pendingPostId = null
//                        }
//                    }
                    LaunchedEffect(pendingPostId) {
                        pendingPostId?.let { postId ->
                            state.navController.navigate("communityDetail/$postId") {
                                launchSingleTop = true
                            }
                            pendingPostId = null // 처리가 끝나면 다시 null로!
                        }
                    }

//                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    val hideBarsRoutes =
                        listOf(
                            NavRoute.ONBOARDING,
                            NavRoute.LOGIN,
                            NavRoute.SIGN_UP,
                            NavRoute.COMMUNITY_WRITE,
                            NavRoute.COMMUNITY_DETAIL,
                            NavRoute.COMMUNITY_UPDATE,
                            NavRoute.MAP,
                        )

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
//                                                    Text(
//                                                        text = item.titleResId,
//                                                        fontSize = 11.sp,
//                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
//                                                        letterSpacing = (-0.3).sp,
//                                                    )
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
                            // AppRoute에 패딩과 navController 전달
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingPostId = intent.data?.getQueryParameter("postId")
    }
}
