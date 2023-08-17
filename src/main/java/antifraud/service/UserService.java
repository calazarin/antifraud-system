package antifraud.service;

import antifraud.entity.AppUser;
import antifraud.entity.Role;
import antifraud.enums.AccessActionEnum;
import antifraud.exception.*;
import antifraud.repository.RoleRepository;
import antifraud.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static antifraud.enums.UserRoleEnum.*;


@Slf4j
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public AppUser registerNewUser(String name, String username, String password) throws UserExistException {
        log.info("Signing up new user with username {}", username);

        List<AppUser> allUsers = userRepository.findAll();
        blockDuplicatedUsers(username, allUsers);
        AppUser newUser = new AppUser(name, username, passwordEncoder.encode(password));
        List<Role> userRoles = new ArrayList<>();
        if(allUsers.isEmpty()){
            userRoles.add(roleRepository.findByNameIgnoreCase(ADMINISTRATOR.getName()).get());
            newUser.setRoles(userRoles);
            log.info("First user, setting its role to ADMINISTRATOR! user is {}", username);
        } else {
            userRoles.add(roleRepository.findByNameIgnoreCase(MERCHANT.getName()).get());
            newUser.setRoles(userRoles);
            newUser.setAccountNonLocked(false);
            log.info("Not the first user, setting its role to USER! user is {}", username);
        }

        log.info("Registering a new user [name={}, username={}]", name, username);
        AppUser createdUser = this.userRepository.save(newUser);
        return createdUser;
    }

    private void blockDuplicatedUsers(String username, List<AppUser> allUsers)  {
        if(allUsers.size() >= 1 && allUsers.stream().anyMatch(usr -> usr.getUsername().equalsIgnoreCase(username))){
            log.error("User {} already exists!", username);
            throw new UserExistException();
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        Optional<AppUser> user = userRepository.findByUsernameIgnoreCase(username);
        if (user.isPresent()){
            AppUser retrievedUser = user.get();
            log.info("Returning user {} with roles {}", retrievedUser.getUsername(),
                    retrievedUser.getRoles()
                            .stream()
                            .map(Role::getName).
                            collect(Collectors.toList()));
            return retrievedUser;
        } else{
            log.error("Trying to load user by username; not found {}", username);
            throw new UsernameNotFoundException(String.format("Username[%s] not found"));
        }
    }

    public List<AppUser> findAllUsers() {
        List<AppUser> allUsers = userRepository.findAll();
        log.info("Found {} users", allUsers.size());
        return allUsers;
    }

    public void deleteUser(String username) {
        log.info("Deleting user {}", username);
        Optional<AppUser> appUser = userRepository.findByUsernameIgnoreCase(username);
        if(appUser.isPresent()){
            userRepository.delete(appUser.get());
        } else {
            log.error("User {} does not exist; cannot delete it!", username);
            throw new UserNotFoundException();
        }
    }

    public AppUser addNewRole(String username, String roleName) {

        Optional<AppUser> userOpt = userRepository.findByUsernameIgnoreCase(username);
        if(!userOpt.isPresent()){
            log.error("Not possible to update user role as it does not exist - username is {}", username);
            throw new UserNotFoundException();
        }

        log.info("Adding new role {} to user {}", roleName, username);
        if(!roleName.equals(MERCHANT.getShortName()) && !roleName.equals(SUPPORT.getShortName())){
            throw new RoleDoesNotExistException();
        }

        Optional<Role> roleOpt = roleRepository.findByNameIgnoreCase("ROLE_"+roleName);
        if(!roleOpt.isPresent()){
            log.error("Role {} not found!", roleName);
            throw new RoleNotFoundException();
        }

        AppUser user = userOpt.get();
        if(user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_"+roleName.toUpperCase()))){
            log.error("Duplicated role {} to user {]", roleName, username);
            throw new DuplicatedRoleException();
        }

        List<Role> newRoles = new ArrayList<>();
        newRoles.add(roleOpt.get());
        user.setRoles(newRoles);

        log.info("User roles after updating them: {}",
                user.getRoles().stream().map(Role::getName).collect(Collectors.toList()));

        return userRepository.save(user);
    }

    @Transactional
    public void lockAndUnlockUser(String operation, String username) {
        AccessActionEnum actionEnum = validateUserLockAction(operation);
        Optional<AppUser> userOpt = userRepository.findByUsernameIgnoreCase(username);

        if(!userOpt.isPresent()){
            log.error("Not possible to lock/unlock user as it does not exist!", username);
            throw new UserNotFoundException();
        }

        AppUser user = userOpt.get();
        blockLockingAdministrator(user, actionEnum);

        if(actionEnum.equals(AccessActionEnum.LOCK)){
            user.setAccountNonLocked(false);
        }

        if(actionEnum.equals(AccessActionEnum.UNLOCK)){
            user.setAccountNonLocked(true);
        }

        AppUser savedUser = userRepository.save(user);
        log.info("Updated user {} isAccountNonLocked state to {}", username,  savedUser.isAccountNonLocked());
    }

    private AccessActionEnum validateUserLockAction(String action){
        if(action.equalsIgnoreCase("lock")) {
            return AccessActionEnum.LOCK;
        } else if(action.equalsIgnoreCase("unlock")){
            return AccessActionEnum.UNLOCK;
        } else {
            throw new InvalidUserActionException("Invalid user action!");
        }
    }

    private void blockLockingAdministrator(AppUser retrievedUser, AccessActionEnum actionEnum){
        if(retrievedUser.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(ADMINISTRATOR.getName()))
                && actionEnum.equals(AccessActionEnum.LOCK)){
            log.error("Not possible {} an administrator user", actionEnum.getName());
            throw new InvalidUserActionException(String.format("Can't %s the ADMINISTRATOR!", actionEnum.getName()));
        }
    }
}
