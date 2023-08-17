package antifraud.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionStatusDto {
    private String result;
    private String info;
    public static TransactionStatusDto toStatusDto(String status, String info){
        return new TransactionStatusDto(status, info);
    }
}
