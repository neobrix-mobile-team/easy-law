package com.easylaw.app.ui.screen.onboarding

import android.Manifest
import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.easylaw.app.R
import com.easylaw.app.ui.components.CommonButton
import com.easylaw.app.viewModel.OnboardingViewModel

@Composable
fun OnboardingView(
    viewModel: OnboardingViewModel,
    goToLoginView: () -> Unit,
) {
    val onboardingViewState by viewModel.onboardingViewState.collectAsState()

    // 뒤로가기 제어
    BackHandler(enabled = onboardingViewState.currentStep > 1) {
        viewModel.previousStep()
    }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
    ) {
        // 상단 프로그레스 바
        LinearProgressIndicator(
            progress = { onboardingViewState.currentStep / 3f },
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

        // 메인 컨텐츠 영역
        Box(modifier = Modifier.weight(1f)) {
            when (onboardingViewState.currentStep) {
                1 -> WelcomeView()
                2 ->
                    RoleSelectionView(
                        selectedRole = onboardingViewState.userRole,
                        onRoleSelected = { viewModel.selectRole(it) },
                    )

                3 ->
                    FinalView(
                        goToLoginView = goToLoginView,
                        onRequiredInformation = { viewModel.onRequiredInformation() },
                        isPrivacyAgreed = onboardingViewState.isPrivacyAgreed,
                        isOpen = onboardingViewState.isOpen,
                        closeRequiredInformation = { viewModel.closeRequiredInformation() },
                        agreeRequiredInformation = { viewModel.agreeRequiredInformation() },
//                    isPrivacyAgreed = onboardingViewState.isPrivacyAgreed,
//                    onRequiredInformation = {isAgreed ->
//                        viewModel.onCheckedChange(
//                            isAgreed
//                        )
//                    },
                    )
            }
        }

        if (onboardingViewState.currentStep != 3) {
            val isNextEnabled =
                when (onboardingViewState.currentStep) {
                    2 -> onboardingViewState.userRole.isNotEmpty()
                    else -> true
                }

            CommonButton(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                onClick = { viewModel.nextStep() },
                color = Color(0xFF3182F6),
                isEnable = isNextEnabled,
                text = stringResource(id = R.string.btn_next),
                icon = Icons.AutoMirrored.Filled.ArrowForward,
            )
        }
    }
}

@Composable
fun WelcomeView() {
    // 화면 전체 배경색 (약간의 회색조를 주어 하얀색 카드가 돋보이게 함)
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB))
                .padding(24.dp), // 화면 전체 여백
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(20.dp)) // 상단 여백

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start, // 텍스트는 왼쪽 정렬
            ) {
                Text(
                    text = stringResource(id = R.string.onboarding_welcome_title),
                    style =
                        TextStyle(
                            fontSize = 28.sp, // 크기를 살짝 키워 강조
                            lineHeight = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191F28),
                            // 텍스트에 아주 은은한 그림자를 주어 입체감 부여
                            shadow =
                                Shadow(
                                    color = Color.Black.copy(alpha = 0.1f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f,
                                ),
                        ),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "복잡한 일들을 혼자서 고민 하지 마세요.",
//                    text = stringResource(id = R.string.onboarding_welcome_subtitle),
                    style =
                        TextStyle(
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = Color(0xFF4E5968),
                        ),
                )
            }

            Spacer(modifier = Modifier.height(40.dp)) // 텍스트와 이미지 사이 여백

            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f) // 남은 공간을 꽉 채움
                        .graphicsLayer {
                        },
                shape = RoundedCornerShape(24.dp), // 부드러운 모서리
                color = Color.White,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, Color(0xFFEFF1F3)), // 아주 연한 테두리
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = Color(0xFF3182F6).copy(alpha = 0.2f), // 연한 파란색
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp)) // 이미지와 버튼 사이 여백

            Spacer(modifier = Modifier.height(20.dp)) // 최하단 여백
        }
    }
}

@Composable
fun RoleSelectionView(
    selectedRole: String,
    onRoleSelected: (String) -> Unit,
) {
    val context = LocalContext.current
    val roleKOR = stringResource(id = R.string.btn_korean)
    val roleENG = stringResource(id = R.string.btn_foreigner)
    val roles = listOf(Pair("ko", roleKOR), Pair("en", roleENG))

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "내국인 또는 외국인 여부에 따라 최적화된 정보를 제공해 드려요.",
//            text = stringResource(id = R.string.onboarding_ask_help),
            style =
                TextStyle(
                    fontSize = 26.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF191F28),
                    shadow =
                        Shadow(
                            color = Color.Black.copy(alpha = 0.05f),
                            offset = Offset(2f, 2f),
                            blurRadius = 4f,
                        ),
                ),
        )

        Spacer(modifier = Modifier.height(40.dp))

        roles.forEach { role ->
            val isSelected = selectedRole == role.first

            val scale by animateFloatAsState(if (isSelected) 1.02f else 1f, label = "scale")
            val elevation by animateDpAsState(if (isSelected) 8.dp else 2.dp, label = "elevation")

            Surface(
                onClick = {
                    onRoleSelected(role.first)
                    if (role.first == "en") {
                        setLocale(context, "en")
                    } else {
                        setLocale(context, "ko")
                    }
                    (context as? Activity)?.recreate()
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp) // 간격을 넓혀 입체감 강조
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) Color.White else Color(0xFFF9FAFB),
                shadowElevation = elevation,
                border = if (isSelected) BorderStroke(2.dp, Color(0xFF3182F6)) else BorderStroke(1.dp, Color(0xFFEFF1F3)),
            ) {
                Row(
                    modifier =
                        Modifier
                            .padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (role.first == "ko") "🇰🇷" else "🌐",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(end = 16.dp),
                    )

                    Text(
                        text = role.second,
                        modifier = Modifier.weight(1f),
                        style =
                            TextStyle(
                                fontSize = 20.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) Color(0xFF3182F6) else Color(0xFF4E5968),
                            ),
                    )

                    Box(
                        modifier =
                            Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFF3182F6) else Color(0xFFE5E8EB))
                                .padding(6.dp),
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FinalView(
    goToLoginView: () -> Unit,
    isPrivacyAgreed: Boolean = false,
    onRequiredInformation: () -> Unit,
    isOpen: Boolean = false,
    closeRequiredInformation: () -> Unit,
    agreeRequiredInformation: () -> Unit,
) {
    val context = LocalContext.current

    // 1. 권한 상태 관리
    var isCameraGranted by remember { mutableStateOf(checkPermission(context, Manifest.permission.CAMERA)) }
    var isStorageGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkPermission(context, Manifest.permission.READ_MEDIA_IMAGES)
            } else {
                checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)
            },
        )
    }

    val isAllReady = isCameraGranted && isStorageGranted && isPrivacyAgreed

    // 2. 개별 권한 요청 런처 설정
    val cameraLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted -> isCameraGranted = isGranted }

    val storageLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted -> isStorageGranted = isGranted }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "서비스 이용에 필요한 권한 설정이 필요합니다.",
//            text = stringResource(id = R.string.permission_title),
            style = TextStyle(fontSize = 26.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
            color = Color.Black,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "원활한 앱 이용을 위해\n다음 권한들에 대한 동의가 필요합니다.",
//            text = stringResource(id = R.string.permission_subtitle),
            fontSize = 16.sp,
            color = Color(0xFF4E5968),
        )

        Spacer(modifier = Modifier.height(24.dp))

        PermissionItem(
            icon = Icons.Default.CameraAlt,
            title = stringResource(id = R.string.permission_camera_title),
            desc = stringResource(id = R.string.permission_camera_desc),
            isGranted = isCameraGranted,
            onClick = {
                if (!isCameraGranted) {
                    cameraLauncher.launch(Manifest.permission.CAMERA)
                }
            },
        )
        Spacer(modifier = Modifier.height(8.dp))
        PermissionItem(
            icon = Icons.Default.PhotoLibrary,
            title = stringResource(id = R.string.permission_storage_title),
            desc = stringResource(id = R.string.permission_storage_desc),
            isGranted = isStorageGranted,
            onClick = {
                if (!isStorageGranted) {
                    val permission =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                    storageLauncher.launch(permission)
                }
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 개인정보 동의 아이템
        PrivacyConsentItem(
            title = "개인정보 수집 및 이용 동의 (필수)",
            desc = "서비스 이용을 위해 약관 확인 및 동의가 필요합니다.",
            isPrivacyAgreed = isPrivacyAgreed,
            onRequiredInformation = { onRequiredInformation() },
        )

        Spacer(modifier = Modifier.weight(1f))

        // 최종 시작하기 버튼 (모든 항목 완료 시에만 활성화)
        Button(
            onClick = { if (isAllReady) goToLoginView() },
            enabled = isAllReady,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = if (isAllReady) Color(0xFF3182F6) else Color(0xFFE8F3FF),
                    contentColor = if (isAllReady) Color.White else Color(0xFF3182F6),
                ),
        ) {
            Text(
                text = if (isAllReady) "시작하기" else "권한 설정을 해주세요.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }

    if (isOpen) {
        AlertDialog(
            onDismissRequest = { closeRequiredInformation() },
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF2F8FF),
                        modifier = Modifier.size(56.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF3182F6),
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "서비스 이용 동의",
                        style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191F28)),
                    )
                }
            },
            text = {
                Surface(
                    color = Color(0xFFF9FAFB),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text =
                            """
                            1. 수집 항목: 기기 ID, 앱 이용 기록
                            2. 이용 목적: 서비스 품질 개선 및 사용자 맞춤형 콘텐츠 제공
                            3. 보유 기간: 회원 탈퇴 시 또는 법정 의무 보유 기간까지
                            * 동의를 거부하실 수 있으나, 서비스 이용이 제한될 수 있습니다.
                            """.trimIndent(),
                        style = TextStyle(fontSize = 14.sp, lineHeight = 22.sp, color = Color(0xFF4E5968)),
                        modifier = Modifier.padding(16.dp),
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { agreeRequiredInformation() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3182F6)),
                    elevation = null,
                ) {
                    Text("확인하고 동의하기", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { closeRequiredInformation() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("다음에 할게요", color = Color(0xFF8B95A1))
                }
            },
        )
    }
}

@Composable
fun PermissionItem(
    icon: ImageVector,
    title: String,
    desc: String,
    isGranted: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isGranted) Color(0xFFF2F8FF) else Color(0xFFF9FAFB),
        border = if (isGranted) BorderStroke(1.dp, Color(0xFFD0E5FF)) else null,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp), // PrivacyConsentItem과 동일한 간격
    ) {
        Row(
            modifier =
                Modifier
                    .padding(16.dp),
            // 내부 여유 공간 통일
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 좌측 아이콘 영역
            Surface(
                shape = CircleShape,
                color = if (isGranted) Color(0xFF3182F6) else Color(0xFFE5E8EB),
                modifier = Modifier.size(24.dp), // 권한 아이콘은 체크보다 약간 크게 설정
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.Check else icon,
                    contentDescription = null,
                    modifier = Modifier.padding(4.dp),
                    tint = Color.White, // 강조를 위해 흰색으로 통일
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 중앙 텍스트 영역
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style =
                        TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isGranted) Color(0xFF3182F6) else Color(0xFF333D4B),
                        ),
                )
                Text(
                    text = desc,
                    style =
                        TextStyle(
                            fontSize = 13.sp,
                            color = Color(0xFF6B7684),
                            lineHeight = 18.sp,
                        ),
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            // 우측 상태 표시 (선택 사항: 허용됨 텍스트 혹은 화살표)
            if (isGranted) {
                Text(
                    text = "허용됨",
                    style =
                        TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3182F6),
                        ),
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFFADB5BD),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
fun PrivacyConsentItem(
    title: String,
    desc: String,
    isPrivacyAgreed: Boolean,
    onRequiredInformation: () -> Unit,
) {
    Surface(
        onClick = onRequiredInformation,
        shape = RoundedCornerShape(16.dp), // 모서리를 둥글게
        color = if (isPrivacyAgreed) Color(0xFFF2F8FF) else Color(0xFFF9FAFB), // 상태에 따른 미세한 배경 변화
        border = if (isPrivacyAgreed) BorderStroke(1.dp, Color(0xFFD0E5FF)) else null,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp), // 아이템 간 간격
    ) {
        Row(
            modifier =
                Modifier
                    .padding(16.dp),
            // 내부 여유 공간 확대
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 좌측 체크 아이콘 영역
            Surface(
                shape = CircleShape,
                color = if (isPrivacyAgreed) Color(0xFF3182F6) else Color(0xFFE5E8EB),
                modifier = Modifier.size(24.dp), // 크기를 조금 줄여서 더 세련되게
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.padding(4.dp),
                    tint = Color.White, // 체크 아이콘은 항상 흰색으로 강조
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 중앙 텍스트 영역
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style =
                            TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isPrivacyAgreed) Color(0xFF3182F6) else Color(0xFF333D4B),
                            ),
                    )
                }
                if (desc.isNotEmpty()) {
                    Text(
                        text = desc,
                        style =
                            TextStyle(
                                fontSize = 13.sp,
                                color = Color(0xFF6B7684),
                                lineHeight = 18.sp,
                            ),
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }

            if (isPrivacyAgreed) {
                Text(
                    text = "허용됨",
                    style =
                        TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3182F6),
                        ),
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFFADB5BD),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

fun checkPermission(
    context: Context,
    permission: String,
): Boolean =
    androidx.core.content.ContextCompat.checkSelfPermission(
        context,
        permission,
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

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
