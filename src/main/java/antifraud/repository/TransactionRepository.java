package antifraud.repository;

import antifraud.entity.Transaction;
import antifraud.enums.RegionEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t WHERE t.cardNumber = :cardNumber AND t.date >= :lowDateLimit AND t.date <= :upperDateLimit AND t.region != :region")
    List<Transaction> findDistinctTransactionsFilteredByCardNumberRegionAndDate(String cardNumber,  LocalDateTime lowDateLimit, LocalDateTime upperDateLimit, RegionEnum region);

    @Query("SELECT t FROM Transaction t WHERE t.cardNumber = :cardNumber AND t.date >= :lowDateLimit AND t.date <= :upperDateLimit AND t.ip != :ip")
    List<Transaction> findDistinctTransactionsFilteredByCardNumberIpAndDate(String cardNumber, LocalDateTime lowDateLimit, LocalDateTime upperDateLimit, String ip);

    List<Transaction> findAllByCardNumber(String cardNumber);
}
