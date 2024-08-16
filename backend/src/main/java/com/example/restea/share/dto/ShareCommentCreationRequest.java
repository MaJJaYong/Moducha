package com.example.restea.share.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class ShareCommentCreationRequest {

    @NotBlank(message = "empty content.")
    @Size(max = 100, message = "content is too long.")
    private String content;
}
