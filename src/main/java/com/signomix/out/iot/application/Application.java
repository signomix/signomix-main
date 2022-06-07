package com.signomix.out.iot.application;

import java.util.HashMap;

public class Application {
    public Long id;
    public Long organization;
    public Long version;
    public String name;
    public String configuration;

    public Application(Long id, Long organization, Long version, String name, String config){
        this.id=id;
        this.organization=organization;
        this.version=version;
        this.name=name;
        this.configuration=config;
    }

    public void updateConfigParemeters(HashMap<String, Object> newParameters){
        //TODO
    }
}
