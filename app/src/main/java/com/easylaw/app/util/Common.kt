package com.easylaw.app.util

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object Common {
    fun formatIsoDate(isoString: String): String =
        try {
            // 1. ISO_DATE_TIME 형식(Supabase 기본형)을 읽어들임
            val parsedDate = ZonedDateTime.parse(isoString)
            // 2. 원하는 형식(yyyy-MM-dd)으로 출력
            parsedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (e: Exception) {
            // 파싱 실패 시 원본 혹은 빈 문자열 반환
            isoString.split("T")[0]
        }
}
