package com.example.restea.teatime.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class TeatimeCommentCreationRequest {
    
    @NotBlank(message = "empty content.")
    @Size(max = 100, message = "title is too long.")
    private String content;
}
