package com.example.restea.common.util;

import java.io.IOException;
import java.lang.reflect.Type;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

/**
 * Trim 처리를 위한 ControllerAdvice
 * <p>
 *
 * @RequestBody 와 @Trim 이 동시에 붙어있는 Resource 에 대해서 Trim 처리 + 마지막에 붙은 개행문자를 제거
 */
@RestControllerAdvice
public class TrimRequestBodyControllerAdvice implements RequestBodyAdvice {

    /**
     * 이 메소드는 지정된 컨트롤러 메소드 파라미터에 대한 요청 본문 처리가 이 클래스에서 지원되는지 여부를 결정
     *
     * @param methodParameter 메소드 파라미터
     * @param targetType      타겟 타입
     * @param converterType   메시지 컨버터 타입
     * @return @Trim 애너테이션이 있다면 true, 그렇지 않으면 false
     */
    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.hasParameterAnnotation(Trim.class);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                           Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        return inputMessage;
    }

    /**
     * 요청 본문이 읽힌 후, 해당 본문에 대해 트림 처리
     *
     * @param body          요청 본문 객체
     * @param inputMessage  입력 메시지
     * @param parameter     메소드 파라미터
     * @param targetType    타겟 타입
     * @param converterType 컨버터 타입
     * @return 트림 처리된 요청 본문 객체
     */
    @NotNull
    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        body = RequestBodyTrimmer.trim(body);
        return body;
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter,
                                  Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }
}