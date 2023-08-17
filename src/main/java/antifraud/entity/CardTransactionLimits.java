package antifraud.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import static antifraud.service.CardTransactionLimitService.DEFAULT_MAX_ALLOWED;
import static antifraud.service.CardTransactionLimitService.DEFAULT_MAX_MANUAL;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@ToString
public class CardTransactionLimits {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String cardNumber;

    private Double maxAllowed;

    private Double maxManual;

    public CardTransactionLimits(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Long getId() {
        return id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public Double getMaxManual(){
        if(maxManual == null) return DEFAULT_MAX_MANUAL;
        return maxManual;
    }

    public Double getMaxAllowed(){
        if(maxAllowed == null) return DEFAULT_MAX_ALLOWED;
        return maxAllowed;
    }
}
