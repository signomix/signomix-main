package org.cricketmsf.microsite.user;

public class Organization {
    public Long id;
    public String code;
    public String name;
    public String description;

    public Organization(Long id, String code, String name, String description) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
    }
}
