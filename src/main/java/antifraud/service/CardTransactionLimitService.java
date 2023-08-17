package antifraud.service;

import antifraud.entity.CardTransactionLimits;
import antifraud.enums.TransactionStatusEnum;
import antifraud.repository.CardTransactionLimitsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static antifraud.enums.TransactionStatusEnum.ALLOWED;
import static antifraud.enums.TransactionStatusEnum.MANUAL_PROCESSING;
import static antifraud.enums.TransactionStatusEnum.PROHIBITED;

@Slf4j
@Service
public class CardTransactionLimitService {

    private final CardTransactionLimitsRepository cardTransactionLimitsRepository;

    public static final double DEFAULT_MAX_ALLOWED = 200d;
    public static final double DEFAULT_MAX_MANUAL = 1500d;

    @Autowired
    public CardTransactionLimitService(CardTransactionLimitsRepository cardTransactionLimitsRepository) {
        this.cardTransactionLimitsRepository = cardTransactionLimitsRepository;
    }

    public Optional<CardTransactionLimits> findCardTransactionLimits(String cardNumber){
        return cardTransactionLimitsRepository.findByCardNumber(cardNumber);
    }

    public List<CardTransactionLimits> findAllCardTransactionLimits(String cardNumber){
        return cardTransactionLimitsRepository.findAllByCardNumber(cardNumber);
    }

    public void updateCardLimitsBasedOnFeedbackAndLastStatus(String cardNumber, Long transactionValue,
                                                             TransactionStatusEnum newFeedback,
                                                             TransactionStatusEnum validationStatus){

        Optional<CardTransactionLimits> cardTransactionLimitOpt = cardTransactionLimitsRepository.findByCardNumber(cardNumber);

        CardTransactionLimits cardTransactionLimit = cardTransactionLimitOpt.orElse(new CardTransactionLimits(cardNumber));

        log.debug("Current transaction max allowed: {}, transaction value: {}",cardTransactionLimit.getMaxAllowed(),transactionValue);
        log.debug("Current transaction max manual: {}, transaction value: {}",cardTransactionLimit.getMaxManual(),transactionValue);

        //feedback X validation status
        if(newFeedback.equals(ALLOWED) && validationStatus.equals(MANUAL_PROCESSING)){
            //increase max allowed
            cardTransactionLimit.setMaxAllowed(
                calculateIncreaseTransactionLimit(cardTransactionLimit.getMaxAllowed(),transactionValue)
            );
            log.debug("Feedback ALLOWED; status: MANUAL_PROCESSING - Increasing MAX ALLOWED!");
        }

        if(newFeedback.equals(ALLOWED) && validationStatus.equals(PROHIBITED)){
            //increase max allowed
            //increase max manual
            cardTransactionLimit.setMaxAllowed(
                calculateIncreaseTransactionLimit(cardTransactionLimit.getMaxAllowed(),transactionValue)
            );
            cardTransactionLimit.setMaxManual(
                calculateIncreaseTransactionLimit(cardTransactionLimit.getMaxManual(),transactionValue)
            );
            log.debug("Feedback ALLOWED; status: PROHIBITED - Increasing MAX ALLOWED and MAX MANUAL!");
        }

        if(newFeedback.equals(MANUAL_PROCESSING) && validationStatus.equals(ALLOWED)){
            //decrease max allowed
            cardTransactionLimit.setMaxAllowed(
                calculateDecreaseTransactionLimit(cardTransactionLimit.getMaxAllowed(),transactionValue)
            );
            log.debug("Feedback MANUAL_PROCESSING; status: ALLOWED - Increasing MAX ALLOWED!");
        }

        if(newFeedback.equals(MANUAL_PROCESSING) && validationStatus.equals(PROHIBITED)){
            //increase max manual
            cardTransactionLimit.setMaxManual(
                calculateIncreaseTransactionLimit(cardTransactionLimit.getMaxManual(),transactionValue)
            );
            log.debug("Feedback MANUAL_PROCESSING; status: PROHIBITED - Increasing MAX MANUAL!");
        }

        if(newFeedback.equals(PROHIBITED) && validationStatus.equals(ALLOWED)){
            //decrease max allowed
            //decrease max manual
            cardTransactionLimit.setMaxAllowed(
                calculateDecreaseTransactionLimit(cardTransactionLimit.getMaxAllowed(),transactionValue)
            );
            cardTransactionLimit.setMaxManual(
                calculateDecreaseTransactionLimit(cardTransactionLimit.getMaxManual(),transactionValue)
            );
            log.debug("Feedback PROHIBITED; status: ALLOWED - Decreasing MAX ALLOWED and MAX MANUAL!");
        }

        if(newFeedback.equals(PROHIBITED) && validationStatus.equals(MANUAL_PROCESSING)){
            //decrease max manual
            cardTransactionLimit.setMaxManual(
                calculateDecreaseTransactionLimit(cardTransactionLimit.getMaxManual(),transactionValue)
            );
            log.debug("Feedback PROHIBITED; status: MANUAL_PROCESSING - Decreasing MAX MANUAL!");
        }

        log.info("Adding a new card transaction limit - {} ",cardTransactionLimit);
        cardTransactionLimitsRepository.save(cardTransactionLimit);
    }

    private double calculateIncreaseTransactionLimit(double currentLimit, double valueFromTransaction){
        double newLimit = 0.8 * currentLimit + 0.2 * valueFromTransaction;
        return Math.ceil(newLimit);
    }

    private double calculateDecreaseTransactionLimit(double currentLimit, double valueFromTransaction){
        double newLimit = 0.8 * currentLimit - 0.2 * valueFromTransaction;
        return Math.ceil(newLimit);
    }
}
