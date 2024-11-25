package com.ssafy.enjoyTrip.domain.attraction.util;

import java.util.regex.Pattern;
import java.util.Set;
import java.util.HashSet;

public class SearchKeywordFilter {
    // 자음/모음만 있는 패턴
    private static final Pattern HANGUL_JAMO_PATTERN = Pattern.compile("^[ㄱ-ㅎㅏ-ㅣ]+$");

    // 특수문자 패턴 (일부 허용할 특수문자는 제외)
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[^가-힣a-zA-Z0-9\\s\\-&]");

    // 최소 검색어 길이
    private static final int MIN_KEYWORD_LENGTH = 2;

    // 욕설 목록 (실제 서비스에서는 더 포괄적인 목록이 필요)
    private static final Set<String> BANNED_WORDS = new HashSet<>() {{
        add("멍청이");
        add("바보");
        add("쓰레기");
        add("시발");
        add("씨발");
        add("꺼져");
        add("지랄");
        add("닥쳐");
        add("병신");
        add("등신");
        add("tlqkf");
        add("wlfkf");
    }};

    public static boolean isValidKeyword(String keyword) {
        if (keyword == null) {
            return false;
        }

        // 키워드 트림 및 소문자 변환
        keyword = keyword.trim().toLowerCase();

        // 최소 길이 체크
        if (keyword.length() < MIN_KEYWORD_LENGTH) {
            return false;
        }

        // 자음/모음만 있는 경우 체크
        if (HANGUL_JAMO_PATTERN.matcher(keyword).matches()) {
            return false;
        }

        // 특수문자 체크
        if (SPECIAL_CHAR_PATTERN.matcher(keyword).find()) {
            return false;
        }

        // 욕설 체크
        for (String bannedWord : BANNED_WORDS) {
            if (keyword.contains(bannedWord)) {
                return false;
            }
        }

        return true;
    }

    public static String sanitizeKeyword(String keyword) {
        if (keyword == null) {
            return "";
        }

        // 앞뒤 공백 제거
        keyword = keyword.trim();

        // 연속된 공백을 하나로 치환
        keyword = keyword.replaceAll("\\s+", " ");

        // 허용되지 않는 특수문자 제거
        keyword = SPECIAL_CHAR_PATTERN.matcher(keyword).replaceAll("");

        return keyword;
    }
}