package com.example.restea.teatime.validation;

import com.example.restea.teatime.dto.TeatimeCreationRequest;
import com.example.restea.teatime.dto.TeatimeUpdateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class BroadcastDateAfterEndDateValidator implements
        ConstraintValidator<BroadcastDateAfterEndDate, Object> {
    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        LocalDateTime endDate;
        LocalDateTime broadcastDate;

        if (obj instanceof TeatimeCreationRequest dto) {
            endDate = dto.getEndDate();
            broadcastDate = dto.getBroadcastDate();
        } else if (obj instanceof TeatimeUpdateRequest dto) {
            endDate = dto.getEndDate();
            broadcastDate = dto.getBroadcastDate();
        } else {
            return false;
        }

        if (endDate == null || broadcastDate == null) {
            return true;
        }

        return broadcastDate.isAfter(endDate);
    }
}
