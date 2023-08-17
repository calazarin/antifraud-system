package antifraud.repository;

import antifraud.entity.CardTransactionLimits;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardTransactionLimitsRepository extends JpaRepository<CardTransactionLimits, Long> {
    Optional<CardTransactionLimits> findByCardNumber(String cardNumber);

    List<CardTransactionLimits> findAllByCardNumber(String cardNumber);
}
