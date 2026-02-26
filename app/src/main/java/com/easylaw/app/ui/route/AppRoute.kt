package com.easylaw.app.ui.route

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.easylaw.app.ui.screen.LegalSearchRoute
import com.easylaw.app.ui.screen.Login.LoginView
import com.easylaw.app.ui.screen.Login.SignView
import com.easylaw.app.ui.screen.Self.SelfView
import com.easylaw.app.ui.screen.community.CommunityView
import com.easylaw.app.ui.screen.onboarding.OnboardingView
import com.easylaw.app.viewModel.LoginViewModel
import com.easylaw.app.viewModel.OnboardingViewModel
import com.easylaw.app.viewModel.SignViewModel
import com.easylaw.app.viewmodel.CommunityViewModel
import com.easylaw.app.viewmodel.SelfViewModel

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
)

// 앱의 네비게이션 설정
object NavRoute {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val SIGNUP = "signUp"
    const val LAW_CONSULT = "lawConsult"
    const val COMMUNITY = "community"
    const val SELF = "self"
    const val CAR_CRUSH = "carCrush"

    val bottomItems =
        listOf(
            BottomNavItem(
                route = COMMUNITY,
                title = "커뮤니티",
                icon = Icons.Default.Share,
            ),
            BottomNavItem(
                route = LAW_CONSULT,
                title = "판례검색",
                icon = Icons.Default.Gavel,
            ),
            BottomNavItem(
                route = SELF,
                title = "자가진단",
                icon = Icons.Default.Check,
            ),
//        BottomNavItem(
//            route = carCrush,
//            title = "영상 분석",
//            icon = Icons.Default.Videocam
//        )
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
//        startDestination = navRoute.community,
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
                        // popUpTo(navRoute.onboarding) : onboarding 까지 경로찾기
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
                        NavRoute.SIGNUP,
                    )
                },
                goToMainView = {
                    navController.navigate(NavRoute.COMMUNITY) {
                        popUpTo(NavRoute.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        // 회원가입
        composable(route = NavRoute.SIGNUP) {
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
//            val lawConsultViewModel: LawConsultViewModel = hiltViewModel()
//            LawConsultView(
//                modifier = modifier,
//                viewModel = lawConsultViewModel,
//            )
            LegalSearchRoute()
        }
        // 커뮤니티 화면
        composable(route = NavRoute.COMMUNITY) {
            val communityViewModel: CommunityViewModel = hiltViewModel()
            CommunityView(
                modifier = modifier,
                viewModel = communityViewModel,
            )
        }
        // 자가진단 화면
        composable(route = NavRoute.SELF) {
            val selfViewModel: SelfViewModel = hiltViewModel()
            SelfView(
                modifier = modifier,
                viewModel = selfViewModel,
            )
        }
    }
}
