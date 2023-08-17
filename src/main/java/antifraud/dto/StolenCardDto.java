package antifraud.dto;

import antifraud.entity.StolenCard;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StolenCardDto {

    private Long id;

    @NotEmpty(message = "Card number is mandatory!")
    private String number;

    public static StolenCardDto toDto(StolenCard stolenCard){
        return new StolenCardDto(stolenCard.getId(), stolenCard.getNumber());
    }
}
