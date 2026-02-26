package com.easylaw.app.ui.screen.onboarding

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easylaw.app.R
import com.easylaw.app.ui.components.CommonButton
import com.easylaw.app.viewmodel.OnboardingViewModel

/**
 * [OnboardingView]
 * * 앱 최초 진입 사용자를 위한 3단계 온보딩 프로세스를 구현한 화면입니다.
 * [OnboardingViewModel]의 상태에 따라 환영 인사, 역할 선택, 권한 안내 화면을 순차적으로 렌더링합니다.
 * * 주요 특징:
 * 실시간 언어 최적화: 사용자가 역할을 선택하는 즉시 앱의 Locale을 변경(ko/en)하고 Activity를 재실행(recreate)하여 언어 환경을 동기화합니다.
 */

@Composable
fun OnboardingView(
    viewModel: OnboardingViewModel,
    goToLoginView: () -> Unit,
) {
    val onboardingViewState by viewModel.onboardingViewState.collectAsState()

    // 기기의 뒤로가기 버튼 동작 제어, 컴포즈 내장 함수
    BackHandler(enabled = onboardingViewState.currentStep > 1) {
        viewModel.previousStep()
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding() // 상단바 공간 확보
                .navigationBarsPadding() // 하단바 공간 확보
                .padding(24.dp),
    ) {
        LinearProgressIndicator(
            progress = onboardingViewState.currentStep / 3f,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
            color = Color(0xFF3182F6),
            trackColor = Color(0xFFF2F4F6),
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )

        Spacer(modifier = Modifier.height(48.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (onboardingViewState.currentStep) {
                1 -> WelcomeView()
                2 ->
                    RoleSelectionView(
                        selectedRole = onboardingViewState.user_role,
                        onRoleSelected = { viewModel.selectRole(it) },
                    )
                3 -> FinalView()
            }
        }

        val isNextEnabled =
            when (onboardingViewState.currentStep) {
                2 -> onboardingViewState.user_role.isNotEmpty()
                else -> true
            }

        CommonButton(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            onClick = {
                if (onboardingViewState.currentStep == 3) {
//                    viewModel.completeOnboarding()
                    goToLoginView()
                } else {
                    viewModel.nextStep()
                }
            },
            color = Color(0xFF3182F6),
            isEnable = isNextEnabled,
            text =
                if (onboardingViewState.currentStep == 3) {
                    stringResource(
                        id = R.string.btn_start,
                    )
                } else {
                    stringResource(id = R.string.btn_next)
                },
            icon =
                if (onboardingViewState.currentStep == 3) {
                    Icons.Default.Check
                } else {
                    Icons.Default.ArrowForward
                },
        )
    }
}

@Composable
fun WelcomeView() {
    Column {
        Text(
            text = stringResource(id = R.string.onboarding_welcome_title),
            style =
                TextStyle(
                    fontSize = 26.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191F28),
                ),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(id = R.string.onboarding_welcome_subtitle),
            fontSize = 16.sp,
            color = Color(0xFF4E5968),
        )
    }
}

@Composable
fun RoleSelectionView(
    selectedRole: String,
    onRoleSelected: (String) -> Unit,
) {
    val context = LocalContext.current
//    val roles = listOf("일반 사용자예요.", "I am a foreign resident in Korea.")
    val roleKOR = stringResource(id = R.string.btn_korean)
    val roleENG = stringResource(id = R.string.btn_foreigner)
    val roles = listOf(roleKOR, roleENG)

    Column {
        Text(
            text = stringResource(id = R.string.onboarding_ask_help),
            style =
                TextStyle(
                    fontSize = 26.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191F28),
                ),
        )
        Spacer(modifier = Modifier.height(32.dp))

        roles.forEach { role ->
            val isSelected = selectedRole == role

            Surface(
                onClick = {
                    onRoleSelected(role)

                    val currentLang = context.resources.configuration.locale.language

                    if (role == roleENG && currentLang != "en") {
                        setLocale(context, "en")
                        (context as? Activity)?.recreate()
                    } else if (role == roleKOR && currentLang != "ko") {
                        setLocale(context, "ko")
                        (context as? Activity)?.recreate()
                    }
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) Color(0xFFE8F3FF) else Color(0xFFF9FAFB),
                border = if (isSelected) BorderStroke(1.5.dp, Color(0xFF3182F6)) else null,
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = role,
                        modifier = Modifier.weight(1f),
                        style =
                            TextStyle(
                                fontSize = 18.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color(0xFF3182F6) else Color(0xFF4E5968),
                            ),
                    )
//                    if (isSelected) {
//                        Icon(
//                            imageVector = Icons.Default.Check,
//                            contentDescription = null,
//                            tint = Color(0xFF3182F6)
//                        )
//                    }
                }
            }
        }
    }
}

@Composable
fun FinalView() {
    Column {
        Text(
            text = stringResource(id = R.string.permission_title),
//            text = "꼭 필요한 권한만\n사용할 때 여쭤볼게요.",
            style = TextStyle(fontSize = 26.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(id = R.string.permission_subtitle),
            fontSize = 16.sp,
            color = Color(0xFF4E5968),
        )

        Spacer(modifier = Modifier.height(40.dp))

        PermissionItem(
            icon = Icons.Default.CameraAlt,
            title = stringResource(id = R.string.permission_camera_title),
            desc = stringResource(id = R.string.permission_camera_desc),
        )
        PermissionItem(
            icon = Icons.Default.PhotoLibrary,
            title = stringResource(id = R.string.permission_storage_title),
            desc = stringResource(id = R.string.permission_storage_desc),
        )
    }
}

@Composable
fun PermissionItem(
    icon: ImageVector,
    title: String,
    desc: String,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFFF2F4F6),
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.padding(12.dp),
                tint = Color(0xFF4E5968),
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = desc, fontSize = 14.sp, color = Color(0xFF8B95A1))
        }
    }
}

fun setLocale(
    context: Context,
    lang: String,
) {
    val locale = java.util.Locale(lang)
    java.util.Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
}
