package com.easylaw.app.viewmodel

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.CoroutineScope
import kotlin.Boolean

/*
MainActivity에 흩어져 있을 법한 여러 가지 UI 제어 변수로 묶어서 관리하는 코드
 */

data class MainViewState
    @OptIn(ExperimentalMaterial3Api::class)
    constructor(
        val navController: NavHostController,
        val drawerState: DrawerState,
        val sheetState: SheetState,
        val scope: CoroutineScope,
        val showLanguageSheet: MutableState<Boolean>,
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RememberMainViewState(
    navController: NavHostController = rememberNavController(),
    drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed),
    scope: CoroutineScope = rememberCoroutineScope(),
    sheetState: SheetState = rememberModalBottomSheetState(),
    showLanguageSheet: MutableState<Boolean> = remember { mutableStateOf(false) },
) = remember(navController, drawerState, sheetState, scope) {
    MainViewState(navController, drawerState, sheetState, scope, showLanguageSheet)
}
