package com.example.restea.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaginationAndSearchDto extends PaginationAndSortingDto {

    private String searchBy; // 검색 기준 (제목: title, 작성자: writer, 내용: content)

    private String keyword;  // 검색 내용

}
