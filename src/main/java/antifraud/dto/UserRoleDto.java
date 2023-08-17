package antifraud.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRoleDto {

    @NotEmpty(message = "Username cannot be null or empty")
    private String username;
    @NotEmpty(message = "Role cannot be null or empty")
    private String role;

}
