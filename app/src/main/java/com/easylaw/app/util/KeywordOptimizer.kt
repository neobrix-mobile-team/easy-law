package com.easylaw.app.util

import javax.inject.Inject

class KeywordOptimizer
    @Inject
    constructor() {
        // 한글, 영문, 숫자로만 이루어진 단어 검사 (특수문자 제외)
        private val singleWordRegex = Regex("^[가-힣a-zA-Z0-9]+$")

        // 즉, 입력값이 "다", "요", "까", "죠" 등 서술어 어미나
        // "은", "는", "이", "가", "을", "를" 등 조사로 끝나는 문장 제외
        private val sentenceEndingRegex = Regex(".*(다|요|까|죠|은|는|이|가|을|를)$")

        // true -> 제미나이 호출 생략
        fun shouldBypassGemini(
            situation: String,
            details: String,
        ): Boolean {
            if (details.isNotBlank()) {
                return false
            }

            val trimmedSituation = situation.trim()

            // 띄어쓰기 유무 검사
            if (trimmedSituation.contains(" ")) {
                return false
            }

            // 종결 어미 및 조사 검사
            if (sentenceEndingRegex.matches(trimmedSituation)) {
                return false
            }

            // 단일 명사 키워드 검사
            if (trimmedSituation.length <= 8 && singleWordRegex.matches(trimmedSituation)) {
                return true
            }

            return false
        }
    }
