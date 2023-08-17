package antifraud.controller;

import antifraud.dto.AccessActionDto;
import antifraud.dto.StatusDto;
import antifraud.dto.UserDto;
import antifraud.dto.UserRoleDto;
import antifraud.entity.AppUser;
import antifraud.enums.AccessActionEnum;
import antifraud.exception.UserExistException;
import antifraud.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(path = "/api/auth/user", consumes = MediaType.APPLICATION_JSON_VALUE
            , produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto registerNewUser(@Valid @RequestBody UserDto userDto) throws UserExistException {
        return UserDto.toDto(userService.registerNewUser(userDto.getName(),
                userDto.getUsername(), userDto.getPassword()));
    }

    @GetMapping(path = "/api/auth/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<UserDto> retrieveUsers(){
        List<UserDto> result = userService.findAllUsers().stream()
                .map(user -> UserDto.toDto(user))
                .sorted(Comparator.comparingLong(UserDto::getId))
                .collect(Collectors.toList());
        log.info("All users: {}", result);
        return result;
    }

    @DeleteMapping(path = "/api/auth/user/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
    public StatusDto deleteUser(@PathVariable String username){
        userService.deleteUser(username);
        return new StatusDto(username, "Deleted successfully!");
    }

    @PutMapping(path = "/api/auth/role", consumes = MediaType.APPLICATION_JSON_VALUE)
    public UserDto addNewRole(@RequestBody UserRoleDto roleDto){
        AppUser updatedUser = userService.addNewRole(roleDto.getUsername(), roleDto.getRole());
        return UserDto.toDto(updatedUser);
    }

    @PutMapping(path = "/api/auth/access", consumes = MediaType.APPLICATION_JSON_VALUE)
    public StatusDto lockAndUnlockUser(@Valid @RequestBody AccessActionDto actionDto){
        userService.lockAndUnlockUser(actionDto.getOperation(), actionDto.getUsername());

        String action = actionDto.getOperation().equalsIgnoreCase(AccessActionEnum.LOCK.name()) ?
                AccessActionEnum.LOCK.getStatus() : AccessActionEnum.UNLOCK.getStatus();

        return new StatusDto(String.format("User %s %s!", actionDto.getUsername().toLowerCase(), action));
    }
}
