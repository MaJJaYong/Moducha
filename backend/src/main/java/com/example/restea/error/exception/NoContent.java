package com.example.restea.error.exception;

import java.nio.charset.Charset;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;

public final class NoContent extends HttpStatusCodeException {
    private NoContent(String statusText, HttpHeaders headers, byte[] body, @Nullable Charset charset) {
        super(HttpStatus.NO_CONTENT, statusText, headers, body, charset);
    }

    private NoContent(String message, String statusText, HttpHeaders headers, byte[] body, @Nullable Charset charset) {
        super(message, HttpStatus.NO_CONTENT, statusText, headers, body, charset);
    }

}
