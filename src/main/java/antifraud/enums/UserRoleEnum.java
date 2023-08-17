package antifraud.enums;

import lombok.Getter;

@Getter
public enum UserRoleEnum {
    ADMINISTRATOR("ROLE_ADMINISTRATOR", "ADMINISTRATOR"),
    MERCHANT("ROLE_MERCHANT","MERCHANT"),
    SUPPORT("ROLE_SUPPORT","SUPPORT");

    private String name;

    private String shortName;

    UserRoleEnum(String name, String shortName){
        this.name = name;
        this.shortName = shortName;
    }
}
