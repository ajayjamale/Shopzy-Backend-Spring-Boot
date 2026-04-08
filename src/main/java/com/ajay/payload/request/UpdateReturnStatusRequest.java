package com.ajay.payload.request;

import com.ajay.domains.ReturnStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateReturnStatusRequest {
    @NotNull
    private ReturnStatus status;
    private String adminComment;
}

