package com.example.restea.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaginationAndSortingDto {

    @NotBlank
    private String sort = "latest"; // 기본값 설정

    @NotNull
    @Positive
    private Integer page; // 페이지 번호

    @NotNull
    @Positive
    private Integer perPage; // 페이지 당 항목 수

}