package antifraud.dto;

import antifraud.entity.SuspiciousIp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SuspiciousIpStatusDto {
    private Long id;
    private String ip;

    public static SuspiciousIpStatusDto toSuspiciousIpStatusDto(SuspiciousIp suspiciousIp) {
        return new SuspiciousIpStatusDto(suspiciousIp.getId(), suspiciousIp.getIp());
    }
}
