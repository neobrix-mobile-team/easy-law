package com.easylaw.app.ui.Route

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
import com.easylaw.app.ui.screen.Login.LoginViewModel
import com.easylaw.app.ui.screen.Login.SignView
import com.easylaw.app.ui.screen.Login.SignViewModel
import com.easylaw.app.ui.screen.Self.SelfView
import com.easylaw.app.ui.screen.Self.SelfViewModel
import com.easylaw.app.ui.screen.community.CommunityView
import com.easylaw.app.ui.screen.community.CommunityViewModel
import com.easylaw.app.ui.screen.onboarding.OnboardingView
import com.easylaw.app.ui.screen.onboarding.OnboardingViewModel

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
)

// 앱의 네비게이션 설정
object navRoute {
    const val onboarding = "onboarding"
    const val login = "login"
    const val signUp = "signUp"
    const val lawConsult = "lawConsult"
    const val community = "community"
    const val self = "self"
    const val carCrush = "carCrush"

    val bottomItems =
        listOf(
            BottomNavItem(
                route = community,
                title = "커뮤니티",
                icon = Icons.Default.Share,
            ),
            BottomNavItem(
                route = lawConsult,
                title = "판례검색",
                icon = Icons.Default.Gavel,
            ),
            BottomNavItem(
                route = self,
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
) {
    NavHost(
        navController = navController,
//        startDestination = navRoute.community,
        startDestination = navRoute.onboarding,
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
        composable(route = navRoute.onboarding) {
            val onboardingViewModel: OnboardingViewModel = hiltViewModel()
            OnboardingView(
                viewModel = onboardingViewModel,
                goToLoginView = {
                    navController.navigate(navRoute.login) {
                        // popUpTo(navRoute.onboarding) : onboarding 까지 경로찾기
                        // inclusive : 해당 경로까지 제거
                        popUpTo(navRoute.onboarding) { inclusive = true }
                    }
                },
            )
        }
        // 로그인
        composable(route = navRoute.login) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginView(
                viewModel = loginViewModel,
                goToSignUpView = {
                    navController.navigate(
                        navRoute.signUp,
                    )
                },
                goToMainView = {
                    navController.navigate(navRoute.community) {
                        popUpTo(navRoute.login) { inclusive = true }
                    }
                },
            )
        }
        // 회원가입
        composable(route = navRoute.signUp) {
            val signViewModel: SignViewModel = hiltViewModel()
            SignView(
                viewModel = signViewModel,
                goToLoginView = {
                    navController.navigate(navRoute.login) {
                        popUpTo(navRoute.login) { inclusive = true }
                    }
                },
            )
        }

        // 판례검색 화면
        composable(route = navRoute.lawConsult) {
//            val lawConsultViewModel: LawConsultViewModel = hiltViewModel()
//            LawConsultView(
//                modifier = modifier,
//                viewModel = lawConsultViewModel,
//            )
            LegalSearchRoute()
        }
        // 커뮤니티 화면
        composable(
            route = navRoute.community,
        ) {
            val communityViewModel: CommunityViewModel = hiltViewModel()
            CommunityView(
                modifier = modifier,
                viewModel = communityViewModel,
            )
        }
        // 자가진단 화면
        composable(
            route = navRoute.self,
        ) {
            val selfViewModel: SelfViewModel = hiltViewModel()
            SelfView(
                modifier = modifier,
                viewModel = selfViewModel,
            )
        }
    }
}
