package com.easylaw.app.ui.screen.Login

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.easylaw.app.R
import com.easylaw.app.ui.components.CommonButton
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

    Box {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(24.dp),
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = stringResource(id = R.string.login_title),
                style =
                    TextStyle(
                        fontSize = 26.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191F28),
                    ),
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 아이디 입력 필드
            CommonTextField(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(id = R.string.login_id_label),
                value = loginState.idInput,
                onValueChange = { viewModel.onChangedIdTextField(it) },
                placeholder = stringResource(id = R.string.login_id_hint),
                isError = loginState.isIdError,
                errorText = if (!loginState.isIdError) "" else stringResource(id = R.string.login_id_error),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 비밀번호 입력 필드
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    text = stringResource(id = R.string.login_signup_link),
                    modifier = Modifier.clickable { goToSignUpView() },
                    style =
                        TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF191F28),
                            letterSpacing = (-0.5).sp,
                            textDecoration = TextDecoration.Underline,
                        ),
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 로그인 버튼
           // TODO:google 로그인 안됨
            CommonButton(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                text = stringResource(id = R.string.login_btn),
                isEnable = loginState.idInput.isNotEmpty() && loginState.pwdInput.isNotEmpty() && !loginState.isIdError && !loginState.isPwdError,
                icon = Icons.Default.Login,
                onClick = {
                    viewModel.login({ goToMainView() })
                },
                color = Color(0xFF3182F6),
            )

            Spacer(modifier = Modifier.height(25.dp))

            // 구글 로그인 버튼
            CommonButton(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                text = stringResource(id = R.string.login_google_btn),
                isEnable = true,
                onClick = {
                    viewModel.logInGoogle(context, { goToMainView() })
                },
                color = Color(0xff78aef5),
                icon = Icons.Default.Login,
            )
        }

        // 로딩 인디케이터
        if (loginState.isLoginLoading) {
            CommonIndicator(title = stringResource(id = R.string.login_loading))
        }

        // 에러 다이얼로그
        if (loginState.isLoginError.isNotEmpty()) {
            CommonDialog(
                title = stringResource(id = R.string.login_error_title),
                desc = loginState.isLoginError,
                icon = Icons.Default.Error,
                onConfirm = {
                    viewModel.onLoginErrorClose()
                },
            )
        }
    }
}
