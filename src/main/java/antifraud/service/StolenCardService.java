package antifraud.service;

import antifraud.entity.StolenCard;
import antifraud.exception.DuplicatedStolenCardException;
import antifraud.exception.InvalidCardNumberException;
import antifraud.exception.StolenCardNotFoundException;
import antifraud.repository.StolenCardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class StolenCardService {

    private final StolenCardRepository stolenCardRepository;

    @Autowired
    public StolenCardService(StolenCardRepository stolenCardRepository) {
        this.stolenCardRepository = stolenCardRepository;
    }

    public StolenCard registerNewStolenCard(String number) {

        Optional<StolenCard> stolenCardOpt = stolenCardRepository.findByNumber(number);
        if(stolenCardOpt.isPresent()){
            log.error("Card number {} already exist!",number);
            throw new DuplicatedStolenCardException();
        }

        if(!checkLuhn(number)){
            log.error("Card number {} is invalid!",number);
            throw new InvalidCardNumberException();
        }

        StolenCard stolenCard = new StolenCard();
        stolenCard.setNumber(number);
        log.info("Registering a new stolen card {}", number);
        return stolenCardRepository.save(stolenCard);
    }

    public void deleteStolenCard(String cardNumber){

        if(!checkLuhn(cardNumber)){
            log.error("Card number {} is invalid!",cardNumber);
            throw new InvalidCardNumberException();
        }

        Optional<StolenCard> stolenCardOpt = stolenCardRepository.findByNumber(cardNumber);
        if(stolenCardOpt.isEmpty()){
            log.error("Card number {} does not exist!",cardNumber);
            throw new StolenCardNotFoundException();
        }

        log.info("Deleting stolen card {}", cardNumber);
        stolenCardRepository.delete(stolenCardOpt.get());
    }

    public static boolean checkLuhn(String cardNumber){
        int nDigits = cardNumber.length();
        int nSum = 0;
        boolean isSecond = false;
        for (int i = nDigits - 1; i >= 0; i--){
            int d = cardNumber.charAt(i) - '0';
            if (isSecond) {
                d = d * 2;
            }
            nSum += d / 10;
            nSum += d % 10;
            isSecond = !isSecond;
        }
        return (nSum % 10 == 0);
    }

    public List<StolenCard> findAll() {
        return stolenCardRepository.findAll();
    }
}
