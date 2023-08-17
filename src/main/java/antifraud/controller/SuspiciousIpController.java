package antifraud.controller;

import antifraud.dto.StatusDto;
import antifraud.dto.SuspiciousIpDto;
import antifraud.dto.SuspiciousIpStatusDto;
import antifraud.service.SuspiciousIpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SuspiciousIpController {

    private final SuspiciousIpService suspiciousIpService;

    @Autowired
    public SuspiciousIpController(SuspiciousIpService suspiciousIpService) {
        this.suspiciousIpService = suspiciousIpService;
    }

    @PostMapping(value = "/api/antifraud/suspicious-ip", produces = MediaType.APPLICATION_JSON_VALUE)
    public SuspiciousIpStatusDto suspiciousIp(@Valid @RequestBody SuspiciousIpDto suspiciousIpDto){
        return SuspiciousIpStatusDto.toSuspiciousIpStatusDto(suspiciousIpService
                .registerNewSuspiciousIp(suspiciousIpDto.getIp()));
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = "/api/antifraud/suspicious-ip/{ip}", produces = MediaType.APPLICATION_JSON_VALUE)
    public StatusDto deleteSuspiciousIp(@PathVariable String ip){
        suspiciousIpService.deleteSuspiciousIp(ip);
        return new StatusDto(String.format("IP %s successfully removed!", ip));
    }

    @GetMapping(value = "/api/antifraud/suspicious-ip", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SuspiciousIpStatusDto> getSuspiciousIps(){
        return suspiciousIpService.retrieveAllSuspiciousIps().stream()
                .map(SuspiciousIpStatusDto::toSuspiciousIpStatusDto)
                .sorted(Comparator.comparingLong(SuspiciousIpStatusDto::getId))
                .collect(Collectors.toList());
    }
}
