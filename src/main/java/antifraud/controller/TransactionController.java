package antifraud.controller;

import antifraud.dto.TransactionDto;
import antifraud.dto.TransactionFeedbackDto;
import antifraud.dto.TransactionStatusDto;
import antifraud.entity.Transaction;
import antifraud.enums.RegionEnum;
import antifraud.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping(value = "/api/antifraud/transaction")
    public TransactionStatusDto antiFraudTransaction(@Valid @RequestBody TransactionDto transactionDto){

        var transactionStatus = transactionService.validateTransaction(
                transactionDto.getAmount(), transactionDto.getIp(), transactionDto.getNumber(),
                RegionEnum.toRegionEnum(transactionDto.getRegion()), validateInputDate(transactionDto.getDate()));

        return TransactionStatusDto.toStatusDto(transactionStatus.getStatus().name(),
                transactionStatus.getInfo());
    }

    @PutMapping(value = "/api/antifraud/transaction", produces = MediaType.APPLICATION_JSON_VALUE)
    public TransactionDto updateTransactionFeedback(@RequestBody TransactionFeedbackDto transactionFeedbackDto){
        Transaction returnedTransaction = transactionService.updateTransactionFeedback(transactionFeedbackDto.getTransactionId(),
                transactionFeedbackDto.getFeedback());
        return TransactionDto.toDto(returnedTransaction);
    }

    @GetMapping(value = "/api/antifraud/history", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TransactionDto> getTransactionHistory(){
        return transactionService.retrieveTransactionHistory()
                .stream()
                .sorted(Comparator.comparingLong(Transaction::getId))
                .map(TransactionDto::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping(value = "/api/antifraud/history/{number}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TransactionDto> getTransactionHistory(@PathVariable String number){
        return transactionService.retrieveTransactionHistoryForACardNumber(number)
                .stream()
                .sorted(Comparator.comparingLong(Transaction::getId))
                .map(TransactionDto::toDto)
                .collect(Collectors.toList());
    }

    private LocalDateTime validateInputDate(String dateTimeStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime parsedDate = LocalDateTime.parse(dateTimeStr, dateTimeFormatter);
        return parsedDate;
    }

}
