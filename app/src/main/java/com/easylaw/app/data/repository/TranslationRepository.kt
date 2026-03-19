package com.easylaw.app.data.repository

interface TranslationRepository {
    suspend fun translateTitles(
        texts: List<String>,
        targetLang: String,
    ): Map<String, String>
}

object PrecedentLabelTranslation {
    // 언어 코드 → (한국어 원문 → 번역문) 맵
    private val categoryMap =
        mapOf(
            "EN" to
                mapOf(
                    "민사" to "Civil",
                    "형사" to "Criminal",
                    "행정" to "Administrative",
                    "일반행정" to "General Administrative",
                    "특허" to "Patent",
                    "가사" to "Family",
                    "헌법" to "Constitutional",
                    "조세" to "Tax",
                    "노동" to "Labor",
                    "선거" to "Election",
                    "기타" to "Other",
                ),
            "JA" to
                mapOf(
                    "민사" to "民事",
                    "형사" to "刑事",
                    "행정" to "行政",
                    "일반행정" to "一般行政",
                    "특허" to "特許",
                    "가사" to "家事",
                    "헌법" to "憲法",
                    "조세" to "租税",
                    "노동" to "労働",
                    "선거" to "選挙",
                    "기타" to "その他",
                ),
        )

    private val courtMap =
        mapOf(
            "EN" to
                mapOf(
                    "대법원" to "Supreme Court",
                    "고등법원" to "High Court",
                    "서울고등법원" to "Seoul High Court",
                    "부산고등법원" to "Busan High Court",
                    "대구고등법원" to "Daegu High Court",
                    "광주고등법원" to "Gwangju High Court",
                    "대전고등법원" to "Daejeon High Court",
                    "지방법원" to "District Court",
                    "서울중앙지방법원" to "Seoul Central District Court",
                    "헌법재판소" to "Constitutional Court",
                    "특허법원" to "Patent Court",
                    "행정법원" to "Administrative Court",
                    "가정법원" to "Family Court",
                ),
            "JA" to
                mapOf(
                    "대법원" to "最高裁判所",
                    "고등법원" to "高等裁判所",
                    "서울고등법원" to "ソウル高等裁判所",
                    "부산고등법원" to "釜山高等裁判所",
                    "대구고등법원" to "大邱高等裁判所",
                    "광주고등법원" to "光州高等裁判所",
                    "대전고등법원" to "大田高等裁判所",
                    "지방법원" to "地方裁判所",
                    "서울중앙지방법원" to "ソウル中央地方裁判所",
                    "헌법재판소" to "憲法裁判所",
                    "특허법원" to "特許裁判所",
                    "행정법원" to "行政裁判所",
                    "가정법원" to "家庭裁判所",
                ),
        )

    private val judgmentTypeMap =
        mapOf(
            "EN" to
                mapOf(
                    "판결" to "Judgment",
                    "결정" to "Decision",
                    "명령" to "Order",
                    "선고" to "Verdict",
                ),
            "JA" to
                mapOf(
                    "판결" to "判決",
                    "결정" to "決定",
                    "명령" to "命令",
                    "선고" to "宣告",
                ),
        )

    fun translateCategory(
        korean: String,
        targetLang: String,
    ): String = categoryMap[targetLang]?.get(korean) ?: korean

    fun translateCourt(
        korean: String,
        targetLang: String,
    ): String = courtMap[targetLang]?.get(korean) ?: korean

    fun translateJudgmentType(
        korean: String,
        targetLang: String,
    ): String = judgmentTypeMap[targetLang]?.get(korean) ?: korean

    /** 언어 코드(ko/en/ja) → DeepL target_lang 코드("EN"/"JA") 변환 */
    fun toDeepLLang(appLang: String): String? =
        when (appLang) {
            "en" -> "EN"
            "ja" -> "JA"
            else -> null // 한국어는 번역 불필요
        }
}
