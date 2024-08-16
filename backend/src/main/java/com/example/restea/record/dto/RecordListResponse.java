package com.example.restea.record.dto;

import com.example.restea.record.entity.Record;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecordListResponse {

    private final Integer recordId;
    private final String title;
    private final String content;
    private LocalDateTime createdDate;

    public static RecordListResponse of(Record record) {
        return RecordListResponse.builder()
                .recordId(record.getId())
                .title(record.getTitle())
                .content(record.getContent())
                .createdDate(record.getCreatedDate())
                .build();
    }
}
