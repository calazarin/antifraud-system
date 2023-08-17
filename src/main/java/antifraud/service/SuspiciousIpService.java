package antifraud.service;

import antifraud.entity.SuspiciousIp;
import antifraud.exception.DuplicatedSuspiciousIpException;
import antifraud.exception.InvalidIpAddressException;
import antifraud.exception.SuspiciousIpNotFoundException;
import antifraud.repository.SuspiciousIpRepository;
import com.google.common.net.InetAddresses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SuspiciousIpService {

    private final SuspiciousIpRepository suspiciousIpRepository;

    @Autowired
    public SuspiciousIpService(SuspiciousIpRepository suspiciousIpRepository) {
        this.suspiciousIpRepository = suspiciousIpRepository;
    }

    public SuspiciousIp registerNewSuspiciousIp(String ip) {
        log.info("Adding new suspicious ip {}", ip);

        Optional<SuspiciousIp> suspiciousIpOpt = suspiciousIpRepository.findByIp(ip);
        if(suspiciousIpOpt.isPresent()){
            log.error("Suspicious ip {} already exists!", ip);
            throw new DuplicatedSuspiciousIpException();
        }
        validateIpFormat(ip);
        return suspiciousIpRepository.save(new SuspiciousIp(ip));
    }

    public static void validateIpFormat(String ip){
        if(!InetAddresses.isInetAddress(ip)){
            log.error("IP {} has an invalid format!", ip);
            throw new InvalidIpAddressException();
        }
    }

    public void deleteSuspiciousIp(String ip){
        log.info("Deleting suspicious ip {}", ip);

        validateIpFormat(ip);

        Optional<SuspiciousIp> suspiciousIpOpt = suspiciousIpRepository.findByIp(ip);
        if(suspiciousIpOpt.isEmpty()){
            log.error("Suspicious ip {} does not exist!", ip);
            throw new SuspiciousIpNotFoundException();
        }
        suspiciousIpRepository.delete(suspiciousIpOpt.get());
    }

    public List<SuspiciousIp> retrieveAllSuspiciousIps() {
        return suspiciousIpRepository.findAll();
    }
}
