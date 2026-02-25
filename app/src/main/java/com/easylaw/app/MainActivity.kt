package com.easylaw.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.easylaw.app.ui.Route.AppRoute
import com.easylaw.app.ui.Route.navRoute
import com.easylaw.app.ui.Route.navRoute.bottomItems
import com.easylaw.app.ui.theme.EasyLawTheme
import dagger.hilt.android.AndroidEntryPoint
import com.easylaw.app.repository.AIRepo
import com.easylaw.app.ui.components.CommonIndicator
import javax.inject.Inject

/**
 * EasyLaw 메인 액티비티
 * 
 * 앱의 진입점이 되는 액티비티입니다.
 * Jetpack Compose를 사용하여 UI를 구성합니다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var aiManager: AIRepo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EasyLawTheme {

                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                val geminiState by aiManager.loadingState.collectAsState()

                val hideBottomBarRoutes = listOf(navRoute.onboarding, navRoute.login, navRoute.signUp)

                Scaffold(
                    bottomBar = {
                        if(currentRoute !in hideBottomBarRoutes){
                            NavigationBar(
                                containerColor = Color(0xFFEAEFEF),
                                tonalElevation = 8.dp
                            ) {
                                bottomItems.forEach { item ->
                                    val isSelected = currentRoute == item.route

                                    NavigationBarItem(
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color(0xFFD95F1E),
                                            selectedTextColor = Color(0xFFD95F1E),
                                            unselectedIconColor = Color(0xFF797573),
                                            unselectedTextColor = Color(0xFF797573),
                                        ),
                                        selected = isSelected,
                                        label = { Text(text = item.title) },
                                        icon = {
                                            Icon(
                                                imageVector = item.icon,
                                                contentDescription = item.title
                                            )
                                        },
                                        /*

                                        popUpTo(id) : 이동할 떄 id까지만 남기고 중간에 있는건 다 날린다.
                                       보통 초기화면으로 설정

                                        saveState = true,
                                        이동시 해당 화면에 사용된 데이터 유지(필요없다면 false)
                                        restoreState = true
                                        바텀바에서 이동을 해도 데이터 유지

                                        보통 세트로 saveState = true,restoreState = true 두개는 같이 사용

                                        launchSingleTop = true
                                         바텀바 중복 이동 방지

                                         */
                                        onClick = {
                                            if (currentRoute != item.route) {
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    /*
                                                    currentRoute != item.route
                                                    if문에서 중복방지긴 하지만 복잡해짐에 따라 못막을 수도 있으니 설정
                                                     */
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    AppRoute(
                        modifier = if (currentRoute in hideBottomBarRoutes) Modifier else Modifier.padding(innerPadding),
                        navController = navController
                    )

                }
                if (geminiState.isLoading)
                    CommonIndicator(
                        geminiState.message
                    )
            }


        }
    }
}
