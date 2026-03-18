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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
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
import com.easylaw.app.viewmodel.LoginViewModel

/**
 * [LoginView]
 *
 * 앱의 사용자 인증을 담당하는 UI 화면입니다.
 * [LoginViewModel]의 상태를 관찰하여 이메일 로그인, 구글 소셜 로그인, 그리고 회원가입 화면으로의 전환을 관리합니다.
 */

@Composable
fun LoginView(
    viewModel: LoginViewModel,
    goToMainView: () -> Unit,
    goToSignUpView: () -> Unit,
) {
    val loginState by viewModel.loginViewState.collectAsState()
    val context = LocalContext.current

    // 전체 배경을 아주 연한 회색으로 설정하여 하얀색 카드가 도드라지게 함
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color(0xFFF9FAFB)),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(24.dp),
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // 1. 상단 타이틀 (그림자로 입체감 부여)
            Text(
                text = stringResource(id = R.string.login_title),
                style =
                    TextStyle(
                        fontSize = 28.sp,
                        lineHeight = 38.sp,
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

            // 2. 메인 입력 카드 (온보딩의 스타일 계승)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 12.dp, // 깊이감 있는 그림자
                border = BorderStroke(1.dp, Color(0xFFEFF1F3)),
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                ) {
                    // 아이디 입력
                    CommonTextField(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(id = R.string.login_id_label),
                        value = loginState.idInput,
                        onValueChange = { viewModel.onChangedIdTextField(it) },
                        placeholder = stringResource(id = R.string.login_id_hint),
                        isError = loginState.isIdError,
                        errorText = if (!loginState.isIdError) "" else stringResource(id = R.string.login_id_error),
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 비밀번호 입력
                    CommonTextField(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(id = R.string.login_pwd_label),
                        value = loginState.pwdInput,
                        onValueChange = { viewModel.onChangedPwdTextField(it) },
                        placeholder = stringResource(id = R.string.login_pwd_hint),
                        isError = loginState.isPwdError,
                        errorText = if (!loginState.isPwdError) "" else stringResource(id = R.string.login_pwd_error),
                        isPassword = true,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 회원가입 링크 (카드 우측 하단 배치)
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        Text(
                            text = stringResource(id = R.string.login_signup_link),
                            modifier = Modifier.clickable { goToSignUpView() },
                            style =
                                TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4E5968),
                                    textDecoration = TextDecoration.Underline,
                                ),
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 로그인 버튼 (입체감 있는 디자인)
                    val isLoginEnabled =
                        loginState.idInput.isNotEmpty() &&
                            loginState.pwdInput.isNotEmpty() &&
                            !loginState.isIdError &&
                            !loginState.isPwdError

                    Button(
                        onClick = { viewModel.login({ goToMainView() }) },
                        enabled = isLoginEnabled,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .graphicsLayer {
                                    // 활성화 시에만 그림자 강조
                                    shadowElevation = if (isLoginEnabled) 8.dp.toPx() else 0f
                                    shape = RoundedCornerShape(16.dp)
                                    clip = true
                                },
                        shape = RoundedCornerShape(16.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3182F6),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFE8F3FF),
                                disabledContentColor = Color(0xFFB0D1FF),
                            ),
                        elevation = null,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.login_btn),
                                style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 3. 구글 로그인 버튼 (하단에 깔끔하게 배치)
            Surface(
                onClick = { viewModel.logInGoogle(context, { goToMainView() }) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp, // 살짝만 띄움
                border = BorderStroke(1.dp, Color(0xFFEFF1F3)),
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    // 여기에 구글 G 로고 이미지를 넣으면 좋습니다.
                    Text(
                        text = stringResource(id = R.string.login_google_btn),
                        style =
                            TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF333D4B),
                            ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 로딩 및 에러 다이얼로그 (기존 유지)
        if (loginState.isLoginLoading) {
            CommonIndicator(title = stringResource(id = R.string.login_loading))
        }

        if (loginState.isLoginError.isNotEmpty()) {
            CommonDialog(
                title = stringResource(id = R.string.login_error_title),
                desc = loginState.isLoginError,
                icon = Icons.Default.Error,
                onConfirm = { viewModel.onLoginErrorClose() },
            )
        }
    }
}
