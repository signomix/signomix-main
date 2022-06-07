package com.signomix.out.iot.application;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

public class Application {
    public Long id;
    public Long organization;
    public Long version;
    public String name;
    public String configuration;

    public Application(Long id, Long organization, Long version, String name, String config) {
        this.id = id;
        this.organization = organization;
        this.version = version;
        this.name = name;
        this.configuration = config;
    }

    /**
     * Updates configuration by adding new parameters and changing the value of existing parameters.
     * @param newParameters
     */
    public void updateConfigParemeters(HashMap<String, Object> newParameters) {
        try {
            Map args1 = new HashMap();
            args1.put(JsonReader.USE_MAPS, true);
            JsonReader jr = new JsonReader();
            Map map = (Map) JsonReader.jsonToJava(this.configuration, args1);
            Iterator<String> it = map.keySet().iterator();
            String key;
            while (it.hasNext()) {
                key = it.next();
                map.put(key, map.get(key));
            }
            Map args2 = new HashMap();
            args2.put(JsonWriter.TYPE, false);
            this.configuration = JsonWriter.objectToJson(map, args2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
