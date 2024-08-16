package com.example.restea.share.dto;

import com.example.restea.common.dto.ImageRequest;
import com.example.restea.share.entity.ShareBoard;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class ShareUpdateRequest implements ImageRequest {

    @NotBlank(message = "empty title.")
    @Size(max = 50, message = "title is too long.")
    private String title;

    @NotBlank(message = "empty content.")
    private String content;

    @NotNull(message = "empty endDate.")
    @Future(message = "endDate must be future.")
    private LocalDateTime endDate;

    @NotNull(message = "empty maxParticipants.")
    @Min(value = 1, message = "maxParticipants must be greater than 0.")
    @Max(value = 100, message = "maxParticipants must be less than 100.")
    private Integer maxParticipants;

    private List<String> images;

    public ShareBoard toEntity() {
        return ShareBoard.builder()
                .title(title)
                .content(content)
                .endDate(endDate)
                .maxParticipants(maxParticipants)
                .build();
    }

}
