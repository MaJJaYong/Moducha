package com.example.restea.common.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.regex.Pattern;

public class RequestBodyTrimmer {
    // 개행 문자를 제거하기 위한 정규 표현식 패턴
    private static final Pattern TRAILING_NEWLINE_PATTERN = Pattern.compile("[\r\n]+$");

    /**
     * 주어진 객체의 문자열 필드에서 앞뒤 공백을 제거
     *
     * @param obj 트림을 적용할 객체
     * @return 트림 처리된 객체
     */
    public static <T> T trim(T obj) {
        if (obj instanceof String) {
            return (T) trimString((String) obj);
        } else {
            return trimObject(obj);
        }
    }

    /**
     * 문자열에서 앞뒤 공백과 개행 문자 제거
     *
     * @param str 트림할 문자열
     * @return 트림된 문자열
     */
    private static String trimString(String str) {
        if (str == null) {
            return null;
        }
        return TRAILING_NEWLINE_PATTERN.matcher(str.trim()).replaceAll("");
    }

    /**
     * 객체의 모든 필드에 대해 트림 처리
     * <p>
     * 이 메소드는 리플렉션을 사용하여 객체의 모든 필드에 접근합니다.
     *
     * @param obj 트림을 적용할 객체
     * @return 트림 처리된 객체
     */
    private static <T> T trimObject(T obj) {
        try {
            Constructor<T> constructor = (Constructor<T>) obj.getClass().getDeclaredConstructor();
            constructor.setAccessible(true); // 생성자가 private일때 Reflection이 접근 불가능하므로 true 설정으로 접근 가능하게...

            T result = constructor.newInstance();
            for (Field field : obj.getClass().getDeclaredFields()) {
                field.setAccessible(true); // 필드가 private일때 Reflection이 접근 가능하도록...
                Object value = field.get(obj);
                if (value != null) {
                    field.set(result, trim(value));
                }
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException("트림 처리에 실패하였습니다.", e);
        }
    }
}