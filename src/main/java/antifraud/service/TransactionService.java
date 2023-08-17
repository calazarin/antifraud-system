package antifraud.service;

import antifraud.entity.CardTransactionLimits;
import antifraud.entity.StolenCard;
import antifraud.entity.SuspiciousIp;
import antifraud.entity.Transaction;
import antifraud.enums.RegionEnum;
import antifraud.enums.TransactionStatusEnum;
import antifraud.exception.InvalidCardNumberException;
import antifraud.exception.InvalidTransactionFeedback;
import antifraud.exception.NotPossibleToUpdateTransactionLimitsException;
import antifraud.exception.TransactionFeedbackAlreadyExistException;
import antifraud.exception.TransactionNotFoundException;
import antifraud.repository.StolenCardRepository;
import antifraud.repository.SuspiciousIpRepository;
import antifraud.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static antifraud.enums.TransactionStatusEnum.ALLOWED;
import static antifraud.enums.TransactionStatusEnum.INVALID_STATUS;
import static antifraud.enums.TransactionStatusEnum.MANUAL_PROCESSING;
import static antifraud.enums.TransactionStatusEnum.PROHIBITED;
import static antifraud.enums.TransactionStatusEnum.values;
import static antifraud.service.CardTransactionLimitService.DEFAULT_MAX_ALLOWED;
import static antifraud.service.CardTransactionLimitService.DEFAULT_MAX_MANUAL;
import static antifraud.service.StolenCardService.checkLuhn;


@Slf4j
@Service
public class TransactionService {

    private final SuspiciousIpRepository suspiciousIpRepository;
    private final StolenCardRepository stolenCardRepository;
    private final TransactionRepository transactionRepository;
    private final CardTransactionLimitService cardTransactionLimitService;


    @Autowired
    public TransactionService(SuspiciousIpRepository suspiciousIpRepository,
                              StolenCardRepository stolenCardRepository,
                              TransactionRepository transactionRepository,
                              CardTransactionLimitService cardTransactionLimitService) {
        this.suspiciousIpRepository = suspiciousIpRepository;
        this.stolenCardRepository = stolenCardRepository;
        this.transactionRepository = transactionRepository;
        this.cardTransactionLimitService = cardTransactionLimitService;
    }

    public TransactionStatusInfo validateTransaction(Long amount, String ip, String cardNumber,
                                                     RegionEnum region, LocalDateTime date) {

        log.info("Validating transaction - amount {}, ip {}, cardNumber {}, region {} and date {}", amount, ip, cardNumber, region, date);

        SuspiciousIpService.validateIpFormat(ip);

        if(!checkLuhn(cardNumber)){
            log.error("Card number {} is invalid!",cardNumber);
            throw new InvalidCardNumberException();
        }

        TransactionStatusEnum status = INVALID_STATUS;
        List<String> infoList = new ArrayList<>();

        Double defaultMaxAllowLimit = DEFAULT_MAX_ALLOWED;
        Double defaultMaxManualProcessingLimit = DEFAULT_MAX_MANUAL;

        Optional<CardTransactionLimits> cardTransactionLimitOpt = cardTransactionLimitService.findCardTransactionLimits(cardNumber);

        if(cardTransactionLimitOpt.isPresent()){

            CardTransactionLimits cardTransactionLimit = cardTransactionLimitOpt.get();

            log.info("Overriding default limits to validate transaction for card {} - maxAllowed: {}, maxDefault: {}", cardNumber,
                    cardTransactionLimit.getMaxAllowed(), cardTransactionLimit.getMaxManual());

            defaultMaxAllowLimit = cardTransactionLimit.getMaxAllowed();
            defaultMaxManualProcessingLimit = cardTransactionLimit.getMaxManual();
        }

        if(amount <= defaultMaxAllowLimit){
            status = ALLOWED;
            infoList.add("none");
        }

        if (amount > defaultMaxAllowLimit && amount <= defaultMaxManualProcessingLimit){
            status = MANUAL_PROCESSING;
            infoList.add("amount");
        }

        boolean isProhibitedAmount =  amount > defaultMaxManualProcessingLimit;

        boolean isInvalidIp = isSuspiciousIp(ip);
        boolean isInvalidCard =  isIsInvalidCardNumber(cardNumber);

        log.debug("Is suspicious IP? {}", isInvalidIp);
        log.debug("Is a stolen card? {}", isInvalidCard);

        if (isInvalidIp || isInvalidCard || isProhibitedAmount){

            infoList.clear();
            status = PROHIBITED;

            if(isInvalidIp) {
                infoList.add("ip");
            }
            if(isInvalidCard) {
                infoList.add("card-number");
            }
            if(isProhibitedAmount) {
                infoList.add("amount");
            }
        }

        Optional<TransactionStatusEnum> regionStatus = checkIfTransactionIsRegionCorrelated(cardNumber, date, region, infoList);
        if(regionStatus.isPresent()) status = regionStatus.get();

        Optional<TransactionStatusEnum> ipStatus = checkIfTransactionIsIpCorrelated(cardNumber, date, ip, infoList);
        if(ipStatus.isPresent()) status = ipStatus.get();

        addNewTransaction(cardNumber,ip, region, date, status, amount);

        log.info("Returning transaction status as {}", status);
        return new TransactionStatusInfo(status, infoList.stream().sorted().collect(Collectors.joining(", ")));
    }

    private Optional<TransactionStatusEnum> checkIfTransactionIsIpCorrelated(String cardNumber, LocalDateTime transactionDate, String ip, List<String> infoList){
        Optional<TransactionStatusEnum> status = Optional.empty();

        LocalDateTime dateTimeFilterLowerLimit = transactionDate.plusHours(-1);
        log.debug("Transaction date time filter lower limit {}", dateTimeFilterLowerLimit);

        List<Transaction> distinctIpTransactionsLastHour = transactionRepository.findDistinctTransactionsFilteredByCardNumberIpAndDate(cardNumber,
                dateTimeFilterLowerLimit, transactionDate, ip);

        long distinctIpTransactionsLastHourCounter = distinctIpTransactionsLastHour
                .stream()
                .filter(distinctByKey(Transaction::getIp)).count();
        log.warn("DistinctIpTransactionsLastHourCounter is {} ", distinctIpTransactionsLastHourCounter);

        String ipCorrelationInfo = "ip-correlation";
        String noneInfo = "none";
        if(distinctIpTransactionsLastHourCounter >= 3){
            status = Optional.of(PROHIBITED);
            if(infoList.contains(noneInfo)) infoList.clear();
            infoList.add(ipCorrelationInfo);

        } else if (distinctIpTransactionsLastHourCounter == 2) {
            status = Optional.of(MANUAL_PROCESSING);
            if(infoList.contains(noneInfo)) infoList.clear();
            infoList.add(ipCorrelationInfo);
        }
        return status;
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private Optional<TransactionStatusEnum> checkIfTransactionIsRegionCorrelated(String cardNumber, LocalDateTime transactionDate, RegionEnum region, List<String> infoList){
        Optional<TransactionStatusEnum> status = Optional.empty();

        LocalDateTime dateTimeFilterLowerLimit = transactionDate.plusHours(-1);
        log.debug("Transaction date time filter lower limit {}", dateTimeFilterLowerLimit);

        List<Transaction> distinctRegionTransactionsLastHour = transactionRepository.findDistinctTransactionsFilteredByCardNumberRegionAndDate(cardNumber,
                dateTimeFilterLowerLimit, transactionDate, region);

        long distinctRegionTransactionsLastHourCounter = distinctRegionTransactionsLastHour.size();
        log.warn("DistinctRegionTransactionsLastHourCounter is {} ", distinctRegionTransactionsLastHourCounter);

        String regionCorrelationInfo = "region-correlation";
        String noneInfo = "none";
        if(distinctRegionTransactionsLastHourCounter > 3){
            status = Optional.of(PROHIBITED);
            if(infoList.contains(noneInfo)) infoList.clear();
            infoList.add(regionCorrelationInfo);

        } else if (distinctRegionTransactionsLastHourCounter == 3) {
            status = Optional.of(MANUAL_PROCESSING);
            if(infoList.contains(noneInfo)) infoList.clear();
            infoList.add(regionCorrelationInfo);
        }
        return status;
    }

    private void addNewTransaction(String cardNumber, String ip, RegionEnum region, LocalDateTime date,
                                   TransactionStatusEnum status,Long amount){
        Transaction newTransaction = new Transaction(cardNumber,ip, region, amount, status,
                LocalDateTime.parse(date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        log.info("Adding new transaction {}", newTransaction);
        transactionRepository.save(newTransaction);
    }

    private boolean isSuspiciousIp(String ipAddress){
        Optional<SuspiciousIp> suspiciousIpOpt = suspiciousIpRepository.findByIp(ipAddress);
        return suspiciousIpOpt.isPresent();
    }

    private boolean isIsInvalidCardNumber(String cardNumber){
        Optional<StolenCard> stolenCardOpt = stolenCardRepository.findByNumber(cardNumber);
        return stolenCardOpt.isPresent();
    }

    @Transactional
    public Transaction updateTransactionFeedback(Long transactionId, String feedback) {

        validateProvidedFeedback(feedback);
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);

        if(transactionOpt.isEmpty()){
            log.error("Transaction with id {} not found!", transactionId);
            throw new TransactionNotFoundException();
        }

        Transaction transaction = transactionOpt.get();

        if(transaction.getFeedback() != null) {
            throw new TransactionFeedbackAlreadyExistException();
        }

        TransactionStatusEnum newFeedback = TransactionStatusEnum.valueOf(feedback);
        transaction.setFeedback(newFeedback);

        TransactionStatusEnum validationStatus = transaction.getResult();

        if(newFeedback.equals(validationStatus)){
            throw new NotPossibleToUpdateTransactionLimitsException();
        }

        log.info("Updating transaction id {} with feedback to {}", transactionId,feedback);
        Transaction updatedTransaction = transactionRepository.save(transaction);

        cardTransactionLimitService.updateCardLimitsBasedOnFeedbackAndLastStatus(transaction.getCardNumber(),
                transaction.getAmount(), newFeedback, transaction.getResult());

        return updatedTransaction;
    }

    private void validateProvidedFeedback(String feedback) {
        if(!Arrays.stream(values()).map(Enum::toString).toList().contains(feedback)){
            log.error("Invalid feedback provided {}", feedback);
            throw new InvalidTransactionFeedback();
        }
    }

    public List<Transaction> retrieveTransactionHistory() {
        return transactionRepository.findAll();
    }

    public List<Transaction> retrieveTransactionHistoryForACardNumber(String cardNumber) {

        boolean checkLuhn = checkLuhn(cardNumber);
        if(!checkLuhn){
            log.error("Card number {} is invalid!",cardNumber);
            throw new InvalidCardNumberException();
        }

        log.debug("Retrieving all transactions for card number {}",cardNumber);
        List<Transaction> allTransactionByCardNumber = transactionRepository.findAllByCardNumber(cardNumber);
        if(allTransactionByCardNumber.isEmpty()){
            throw new TransactionNotFoundException();
        }
        return allTransactionByCardNumber;
    }

    public static class TransactionStatusInfo{
        private final TransactionStatusEnum status;
        private final String info;

        public TransactionStatusInfo(TransactionStatusEnum status, String info) {
            this.status = status;
            this.info = info;
        }

        public TransactionStatusEnum getStatus() {
            return status;
        }

        public String getInfo() {
            return info;
        }
    }

}
