package antifraud.dto;


import antifraud.entity.Transaction;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {

    private Long transactionId;

    @NotNull(message = "Amount cannot be null!")
    @Positive(message = "Amount needs to be greater than zero!")
    private Long amount;

    @NotEmpty(message = "Username cannot be null or empty")
    private String ip;

    @NotEmpty(message = "Username cannot be null or empty")
    private String number;

    @NotEmpty(message = "Region cannot be null or empty")
    private String region;

    @NotNull(message = "Date cannot be null!")
    private String date;

    private String result;

    private String feedback;

    public static TransactionDto toDto(Transaction transaction){

        DateTimeFormatter isoDateTime = DateTimeFormatter.ISO_DATE_TIME;
        String formattedDate = transaction.getDate().format(isoDateTime);

        return new TransactionDto(transaction.getId(),
                transaction.getAmount(),
                transaction.getIp(),
                transaction.getCardNumber(),
                transaction.getRegion().toString(),
                formattedDate,
                transaction.getResult().toString(),
                transaction.getFeedback() != null ? transaction.getFeedback().toString() : "");
    }

}
