package com.easylaw.app.navigation

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.easylaw.app.R
import com.easylaw.app.ui.screen.LegalSearchRoute
import com.easylaw.app.ui.screen.Login.LoginView
import com.easylaw.app.ui.screen.Login.SignView
import com.easylaw.app.ui.screen.community.CommunityDetailView
import com.easylaw.app.ui.screen.community.CommunityUpdateView
import com.easylaw.app.ui.screen.community.CommunityView
import com.easylaw.app.ui.screen.community.CommunityWriteView
import com.easylaw.app.ui.screen.diagnosis.DiagnosisScreen
import com.easylaw.app.ui.screen.map.MapScreen
import com.easylaw.app.ui.screen.onboarding.OnboardingView
import com.easylaw.app.viewmodel.CommunityDetailViewModel
import com.easylaw.app.viewmodel.CommunityUpdateViewModel
import com.easylaw.app.viewmodel.CommunityViewModel
import com.easylaw.app.viewmodel.CommunityWriteViewModel
import com.easylaw.app.viewmodel.LoginViewModel
import com.easylaw.app.viewmodel.OnboardingViewModel
import com.easylaw.app.viewmodel.SignViewModel

// import com.easylaw.app.viewmodel.CommunityViewModel
// import com.easylaw.app.viewmodel.CommunityWriteViewModel
// import com.easylaw.app.viewmodel.LoginViewModel
// import com.easylaw.app.viewmodel.OnboardingViewModel
// import com.easylaw.app.viewmodel.SignViewModel
// import com.easylaw.app.viewmodel.CommunityDetailViewModel
// import com.easylaw.app.viewmodel.CommunityUpdateViewModel

data class BottomNavItem(
    val route: String,
    @StringRes val titleResId: Int,
    val icon: ImageVector,
)

// 앱의 네비게이션 설정
object NavRoute {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val SIGN_UP = "signUp"
    const val LAW_CONSULT = "lawConsult"
    const val COMMUNITY = "community"
    const val COMMUNITY_WRITE = "communityWrite"
    const val COMMUNITY_UPDATE = "communityUpdate/{updateId}"
    const val SELF = "self"
    const val CAR_CRUSH = "carCrush"
    const val MAP = "map"
    const val COMMUNITY_DETAIL = "communityDetail/{id}"

    val bottomItems =
        listOf(
            BottomNavItem(
                route = COMMUNITY,
                titleResId = R.string.sidebar_menu_community,
                icon = Icons.Default.Share,
            ),
            BottomNavItem(
                route = LAW_CONSULT,
                titleResId = R.string.sidebar_menu_precedent,
                icon = Icons.Default.Gavel,
            ),
            BottomNavItem(
                route = SELF,
                titleResId = R.string.sidebar_menu_self_diagnosis,
                icon = Icons.Default.Check,
            ),
//        BottomNavItem(
//            route = CAR_CRUSH,
//            title = "영상 분석",
//            icon = Icons.Default.Videocam
//        )
            BottomNavItem(
                route = MAP,
                titleResId = R.string.sidebar_menu_nearby,
                icon = Icons.Default.Map,
            ),
        )
}

@Composable
fun AppRoute(
    modifier: Modifier,
    navController: NavHostController,
    startDestination: String = NavRoute.ONBOARDING,
) {
    NavHost(
        navController = navController,
//        startDestination = NavRoute.COMMUNITY,
        startDestination = startDestination,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300),
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300),
            ) + fadeOut(animationSpec = tween(300))
        },
    ) {
        // 온보딩
        composable(route = NavRoute.ONBOARDING) {
            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            OnboardingView(
                viewModel = onboardingViewModel,
                goToLoginView = {
                    navController.navigate(NavRoute.LOGIN) {
                        // popUpTo(NavRoute.ONBOARDING) : onboarding 까지 경로찾기
                        // inclusive : 해당 경로까지 제거
                        popUpTo(NavRoute.ONBOARDING) { inclusive = true }
                    }
                },
            )
        }
        // 로그인
        composable(route = NavRoute.LOGIN) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginView(
                viewModel = loginViewModel,
                goToSignUpView = {
                    navController.navigate(
                        NavRoute.SIGN_UP,
                    )
                },
                goToMainView = {
                    navController.navigate(NavRoute.COMMUNITY) {
                        // navController.graph.id
                        // 이전 스택 전부 지우고 다음 화면 스택만 남긴다.
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
            )
        }
        // 회원가입
        composable(route = NavRoute.SIGN_UP) {
            val signViewModel: SignViewModel = hiltViewModel()
            SignView(
                viewModel = signViewModel,
                goToLoginView = {
                    navController.navigate(NavRoute.LOGIN) {
                        popUpTo(NavRoute.LOGIN) { inclusive = true }
                    }
                },
            )
        }

        // 판례검색 화면
        composable(route = NavRoute.LAW_CONSULT) {
            LegalSearchRoute()
        }
        // 커뮤니티 화면
        composable(
            route = NavRoute.COMMUNITY,
        ) {
            val communityViewModel: CommunityViewModel = hiltViewModel()
            CommunityView(
                modifier = modifier,
                viewModel = communityViewModel,
                communityWrite = {
                    navController.navigate(NavRoute.COMMUNITY_WRITE) {
                        // 연타 시 중복 화면 그리기 방지
                        launchSingleTop = true
                    }
                },
                gotoDetail = { clickedId ->
                    navController.navigate("communityDetail/$clickedId") {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(
            route = "communityDetail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) {
            val communityDetailViewModel: CommunityDetailViewModel = hiltViewModel()
            CommunityDetailView(
                modifier = modifier,
                viewModel = communityDetailViewModel,
                goBack = {
                    navController.popBackStack()
                },
                goUpdate = { updateId ->
                    navController.navigate("communityUpdate/$updateId") {
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(
            route = NavRoute.COMMUNITY_UPDATE,
            arguments = listOf(navArgument("updateId") { type = NavType.LongType }),
        ) {
            val communityUpdateViewModel: CommunityUpdateViewModel = hiltViewModel()
            CommunityUpdateView(
                modifier = modifier,
                viewModel = communityUpdateViewModel,
                goBack = { navController.popBackStack() },
            )
        }
        // 커뮤니티 - 글쓰기
        composable(
            route = NavRoute.COMMUNITY_WRITE,
        ) {
            // 부모의 뷰모델을 가져오고 싶을 떄
            // NavContorller에서 화면 이동시 해당 화면 정본느 기본적으로 BackStackEntry에 저장된다.

//            val parentViewModel = remember(it) {
//                navController.getBackStackEntry(NavRoute.COMMUNITY)
//            }
//            val communityViewModel: CommunityViewModel = hiltViewModel(parentViewModel)

            val communityWriteViewModel: CommunityWriteViewModel = hiltViewModel()
            CommunityWriteView(
                modifier = modifier,
                viewModel = communityWriteViewModel,
                goBack = {
                    navController.popBackStack()
                },
            )
        }
        // 자가진단 화면
        composable(
            route = NavRoute.SELF,
        ) {
            DiagnosisScreen(modifier = modifier)
        }

        composable(
            route = NavRoute.MAP,
        ) {
            MapScreen()
        }
    }
}
