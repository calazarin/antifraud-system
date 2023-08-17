package antifraud.service;

import antifraud.entity.Role;
import antifraud.enums.UserRoleEnum;
import antifraud.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class DataLoader implements ApplicationListener<ContextRefreshedEvent> {

    boolean alreadySetup = false;

    private final RoleRepository roleRepository;

    @Autowired
    public DataLoader(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    private void createRoles() {
        try {
            for (UserRoleEnum role : UserRoleEnum.values()) {
                if(!roleRepository.findByNameIgnoreCase(role.getName()).isPresent()){
                    roleRepository.save(new Role(role.getName(), role.getShortName()));
                }
            }
        } catch (Exception ex){
            log.error("Error to execute data loader and populate user roles", ex);
        }
    }

    @Transactional
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        createRoles();
        alreadySetup = true;
    }
}
