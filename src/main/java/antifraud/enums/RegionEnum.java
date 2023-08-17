package antifraud.enums;

import lombok.Getter;

@Getter
public enum RegionEnum {
    EAP("East Asia and Pacific"),
    ECA("Europe and Central Asia"),
    HIC("High-Income countries"),
    LCA("Latin America and the Caribbean"),
    MENA("The Middle East and North Africa"),
    SA("South Asia"),
    SSA("Sub-Saharan Africa");

    private String description;

    RegionEnum(String description){
        this.description = description;
    }

    public static RegionEnum toRegionEnum(String regionAsStr){
        return RegionEnum.valueOf(regionAsStr);
    }
}
