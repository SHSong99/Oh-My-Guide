package com.ohmyguide.app.util

object KoreanRomanizer {

    private val CHOSEONG = arrayOf(
        "G", "GG", "N", "D", "DD", "R", "M", "B", "BB",
        "S", "SS", "", "J", "JJ", "Ch", "K", "T", "P", "H",
    )
    private val JUNGSEONG = arrayOf(
        "a", "ae", "ya", "yae", "eo", "e", "yeo", "ye", "o",
        "wa", "wae", "oe", "yo", "u", "wo", "we", "wi", "yu",
        "eu", "ui", "i",
    )
    private val JONGSEONG = arrayOf(
        "", "k", "k", "k", "n", "n", "n", "t", "l",
        "l", "l", "l", "l", "l", "l", "l", "m", "p",
        "p", "t", "t", "t", "t", "ng", "t", "t", "k", "t", "p", "t",
    )

    // 종성 뒤에 모음이 올 때 연음 처리용
    private val JONGSEONG_LINKED = arrayOf(
        "", "g", "kk", "gs", "n", "nj", "nh", "d", "r",
        "lg", "lm", "lb", "ls", "lt", "lp", "lh", "m", "b",
        "bs", "s", "ss", "ng", "j", "ch", "k", "t", "p", "h",
    )

    fun romanize(text: String): String {
        val sb = StringBuilder()
        val chars = text.toCharArray()

        for (i in chars.indices) {
            val c = chars[i]
            if (c.code in 0xAC00..0xD7A3) {
                val syllable = c.code - 0xAC00
                val cho = syllable / (21 * 28)
                val jung = (syllable % (21 * 28)) / 28
                val jong = syllable % 28

                // 초성
                val choStr = CHOSEONG[cho]
                val isWordStart = i == 0 || !chars[i - 1].isHangul()
                if (choStr.isNotEmpty()) {
                    if (isWordStart || sb.isEmpty() || sb.last() == ' ') {
                        sb.append(choStr[0].uppercaseChar())
                        if (choStr.length > 1) sb.append(choStr.substring(1).lowercase())
                    } else {
                        sb.append(choStr.lowercase())
                    }
                }

                // 중성
                sb.append(JUNGSEONG[jung])

                // 종성
                if (jong != 0) {
                    val nextIsVowel = i + 1 < chars.size && chars[i + 1].isHangulVowelStart()
                    if (nextIsVowel) {
                        sb.append(JONGSEONG_LINKED[jong])
                    } else {
                        sb.append(JONGSEONG[jong])
                    }
                }
            } else if (c == ' ') {
                sb.append(' ')
            } else {
                sb.append(c)
            }
        }

        return capitalizeWords(sb.toString())
    }

    private fun capitalizeWords(text: String): String {
        return text.split(' ').joinToString(" ") { word ->
            if (word.isNotEmpty()) {
                word[0].uppercaseChar() + word.substring(1)
            } else ""
        }
    }

    private fun Char.isHangul(): Boolean = code in 0xAC00..0xD7A3

    private fun Char.isHangulVowelStart(): Boolean {
        if (!isHangul()) return false
        val syllable = code - 0xAC00
        val cho = syllable / (21 * 28)
        // ㅇ(이응) = index 11 → 초성이 없는 것처럼 발음
        return cho == 11
    }
}
