package antifraud.dto;

import antifraud.entity.AppUser;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {

    private Long id;

    @NotEmpty(message = "Name cannot be null or empty")
    private String name;

    @NotEmpty(message = "Username cannot be null or empty")
    private String username;

    @NotEmpty(message = "Password cannot be null or empty")
    @Size(min=12, message = "The password length must be at least 12 chars!")
    private String password;

    private String role;

    public UserDto(Long id, String name, String username, String role) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.role = role;
    }

    public static UserDto toDto(AppUser appUser){
        return new UserDto(appUser.getId(), appUser.getName(), appUser.getUsername(),
                appUser.getRoles().stream()
                        .map(r -> r.getShortName())
                        .findAny().get());
    }
}
