package antifraud.controller;

import antifraud.dto.StatusDto;
import antifraud.dto.StolenCardDto;
import antifraud.service.StolenCardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class StolenCardController {

    private final StolenCardService stolenCardService;

    public StolenCardController(StolenCardService stolenCardService) {
        this.stolenCardService = stolenCardService;
    }

    @PostMapping(value = "/api/antifraud/stolencard", produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE)
    public StolenCardDto addNewStolenCard(@Valid @RequestBody StolenCardDto stolenCardDto){
        return StolenCardDto.toDto(stolenCardService
                .registerNewStolenCard(stolenCardDto.getNumber()));
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = "/api/antifraud/stolencard/{number}", produces = MediaType.APPLICATION_JSON_VALUE)
    public StatusDto deleteStolenCard(@PathVariable String number){
        stolenCardService.deleteStolenCard(number);
        return new StatusDto(String.format("Card %s successfully removed!", number));
    }

    @GetMapping(path = "/api/antifraud/stolencard", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StolenCardDto> findAll(){
        return stolenCardService.findAll().stream()
                .map(StolenCardDto::toDto)
                .sorted(Comparator.comparingLong(StolenCardDto::getId))
                .collect(Collectors.toList());
    }
}
