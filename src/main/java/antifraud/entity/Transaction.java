package antifraud.entity;

import antifraud.enums.RegionEnum;
import antifraud.enums.TransactionStatusEnum;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@ToString
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String cardNumber;

    private String ip;

    private RegionEnum region;

    private Long amount;

    private TransactionStatusEnum result;

    private TransactionStatusEnum feedback;

    private LocalDateTime date;

    public Transaction(String cardNumber, String ip, RegionEnum region, LocalDateTime date) {
        this.cardNumber = cardNumber;
        this.ip = ip;
        this.region = region;
        this.date = date;
    }

    public Transaction(String cardNumber, String ip, RegionEnum region, Long amount, TransactionStatusEnum result, LocalDateTime date) {
        this.cardNumber = cardNumber;
        this.ip = ip;
        this.region = region;
        this.amount = amount;
        this.result = result;
        this.date = date;
    }
}
