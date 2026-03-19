package com.easylaw.app.ui.screen.Login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easylaw.app.R
import com.easylaw.app.ui.components.CommonDialog
import com.easylaw.app.ui.components.CommonIndicator
import com.easylaw.app.ui.components.CommonTextField
import com.easylaw.app.viewModel.SignViewModel

/**
 * [SignView]
 * * 서비스의 회원가입 화면을 구성하는 컴포저블 함수입니다.
 * [SignViewModel]의 상태([SignViewState])를 관찰하여 UI를 갱신하며, 사용자 입력을 ViewModel로 전달합니다.
 * * 주요 설계 포인트:
 * 1. 상태 중심 UI: 입력 필드, 버튼의 활성화 상태, 다이얼로그 노출 여부가 모두 ViewModel의 단일 상태에 의존합니다.
 * 2. 반응형 유효성 검사: 사용자가 입력하는 즉시 에러 텍스트와 버튼 활성 상태가 업데이트되어 직관적인 UX를 제공합니다.
 * 3. 사용자 피드백: 로딩 중에는 인디케이터를, 결과에 따라 성공/실패 팝업을 띄워 프로세스 상태를 명확히 전달합니다.
 */

@Composable
fun SignView(
    viewModel: SignViewModel,
    goToLoginView: () -> Unit,
) {
    val scrollState = rememberScrollState()
    val signState by viewModel.signViewState.collectAsState()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB)) // 배경색 통일
                .statusBarsPadding()
                .navigationBarsPadding(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(scrollState),
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 1. 타이틀 영역
            Text(
                text = stringResource(id = R.string.sign_title),
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

            Spacer(modifier = Modifier.height(32.dp))

            // 2. 메인 입력 카드 (입체감 있는 디자인)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 10.dp,
                border = BorderStroke(1.dp, Color(0xFFEFF1F3)),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                ) {
                    // 이름 입력
                    CommonTextField(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(id = R.string.sign_name_label),
                        value = signState.name,
                        onValueChange = { viewModel.onNameChanged(it) },
                        placeholder = stringResource(id = R.string.sign_name_hint),
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 이메일 입력
                    CommonTextField(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(id = R.string.sign_email_label),
                        value = signState.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        placeholder = stringResource(id = R.string.sign_email_hint),
                        isError = signState.isEmailError,
                        errorText = if (!signState.isEmailError) "" else stringResource(id = R.string.sign_email_error),
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 비밀번호 입력
                    CommonTextField(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(id = R.string.sign_pwd_label),
                        value = signState.password,
                        onValueChange = { viewModel.onPasswordChanged(it) },
                        placeholder = stringResource(id = R.string.sign_pwd_hint),
                        isPassword = true,
                        isError = signState.isPasswordError,
                        errorText = if (!signState.isPasswordError) "" else stringResource(id = R.string.sign_pwd_error),
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 비밀번호 확인
                    CommonTextField(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(id = R.string.sign_pwd_confirm_label),
                        value = signState.passwordConfirm,
                        onValueChange = { viewModel.onPasswordConfirmChanged(it) },
                        placeholder = "",
                        isPassword = true,
                        isError = signState.isPasswordConfirmError,
                        errorText = if (!signState.isPasswordConfirmError) "" else stringResource(id = R.string.sign_pwd_confirm_error),
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // 가입하기 버튼
                    val isSignEnabled =
                        signState.name.isNotEmpty() &&
                            signState.email.isNotEmpty() &&
                            signState.password.isNotEmpty() &&
                            signState.passwordConfirm.isNotEmpty() &&
                            !signState.isEmailError &&
                            !signState.isPasswordError &&
                            !signState.isPasswordConfirmError

                    Button(
                        onClick = { viewModel.signUp() },
                        enabled = isSignEnabled,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .graphicsLayer {
                                    shadowElevation = if (isSignEnabled) 8.dp.toPx() else 0f
                                    shape = RoundedCornerShape(16.dp)
                                    clip = true
                                },
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3182F6),
                                disabledContainerColor = Color(0xFFE8F3FF),
                                disabledContentColor = Color(0xFF3182F6).copy(alpha = 0.5f),
                            ),
                        elevation = null,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.sign_btn),
                                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "이미 계정이 있으신가요?",
                    style = TextStyle(fontSize = 14.sp, color = Color(0xFF6B7684)),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.login_btn), // "로그인"
                    modifier = Modifier.clickable { goToLoginView() },
                    style =
                        TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3182F6),
                            textDecoration = TextDecoration.Underline,
                        ),
                )
            }
        }

        // 로딩, 성공, 에러 다이얼로그 (기존 로직 유지)
        if (signState.isSignLoading) {
            CommonIndicator(title = stringResource(id = R.string.sign_loading))
        }

        if (signState.isSignSuccess) {
            CommonDialog(
                title = "회원가입 성공",
                desc = "이메일 인증을 진행해 주세요.",
                icon = Icons.Default.CheckCircle,
                onConfirm = { goToLoginView() },
            )
        }

        if (signState.isSignError.isNotEmpty()) {
            CommonDialog(
                title = stringResource(id = R.string.sign_error_title),
                desc = signState.isSignError,
                icon = Icons.Default.Error,
                onConfirm = { viewModel.onSignErrorClose() },
            )
        }
    }
}
