package com.example.restea.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Getter
public class NicknameUpdateRequest {

    @NotBlank(message = "닉네임은 빈문자열이거나 null일 수 없습니다.")
    @Size(min = 2, max = 12, message = "닉네임은 2~12자의 길이를 가져야 합니다.")
    @Pattern(
            regexp = "^(?!\\d+$)(?!.*\\s{2,})(?=.*[가-힣a-zA-Z])[가-힣a-zA-Z\\d ]+$",
            message = "닉네임은 한글, 영어를 포함할 수 있으며, 숫자는 단독으로 사용될 수 없습니다. 공백은 최대 1개만 허용됩니다."
    )

    private String nickname;
}
