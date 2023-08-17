package antifraud.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionFeedbackDto {

    @NotNull(message="Transaction id cannot be null")
    private Long transactionId;
    @NotEmpty(message = "Feedback cannot be null or empty")
    private String feedback;
}
