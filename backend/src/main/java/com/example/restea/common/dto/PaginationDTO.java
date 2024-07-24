package com.example.restea.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

// pagination에 대한 정보를 담는 DTO
@NoArgsConstructor
@Builder
@Getter
@ToString
public class PaginationDTO {
  private int total;
  private int page;
  private int perPage;

  @Builder
  public PaginationDTO(int total, int page, int perPage) {
    this.total = total;
    this.page = page;
    this.perPage = perPage;
  }

}
