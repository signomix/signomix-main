/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.iot;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.cricketmsf.livingdoc.design.BoundedContext;

/**
 * Description
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
@BoundedContext
public class Device {

    public static String GENERIC = "GENERIC";
    public static String TTN = "TTN";
    //public static String TTNCLIENT = "TTNCLIENT";
    public static String GATEWAY = "GATEWAY";
    //public static String ACTUATOR = "ACTUATOR";
    public static String VIRTUAL = "VIRTUAL";
    public static String LORA = "LORA";
    public static String KPN = "KPN";
    public static String EXTERNAL = "EXTERNAL";
    
    public static int UNKNOWN = 0;
    public static int OK = 1;
    public static int FAILURE = 2;

    private String template;

    /**
     * EUI
     */
    private String EUI;  // TTN: devEUI
    private String name; // 
    private String applicationEUI; //TTN: appEUI
    private String applicationID;  //TTn: appID
    private String key;  // TTN: HTTP Integration Authorization request header
    private String userID; //device owner
    private String type;
    private String team;
    private LinkedHashMap channels;
    private String code; // JavaScript data preprocessor code
    private String encoder; // JavaScript to decode LoRa payload
    private String description;
    private long lastSeen;
    private long transmissionInterval;
    private long lastFrame;
    private boolean checkFrames;
    private String pattern; //not used
    private String downlink;
    private String commandScript;
    private String groups;
    private int alertStatus;
    private String deviceID; // TTN: devAddress
    private boolean active;
    private String project;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Double state;
    private long retentionTime;

    //TODO: change uid to uidHex and add validation (is it hex value)
    /**
     * IoT device
     */
    public Device() {
        EUI = "###"; // unique idenitifier
        //uid = ""; // device internal ID ( == not unique device_address in TTN!)
        userID = "";
        type = "GENERIC";
        team = "";
        channels = new LinkedHashMap();
        code = null;
        encoder = null;
        key = null;
        description = "";
        lastSeen = -1;
        transmissionInterval = 0; //10 minutes
        lastFrame = -1;
        checkFrames = true;
        alertStatus = UNKNOWN;
        deviceID = "";
        project = "";
        active = true;
        latitude = 100000d;
        longitude = 100000d;
        altitude = 100000d;
        state = 0d;
    }

    public void print() {
        System.out.println("DEVICE: " + getEUI());
        System.out.println("TEAM: " + getTeam());
        System.out.println("CHANNELS: " + getChannels().keySet().size());
        System.out.println("CODE: " + getCode());
    }

    public boolean isVirtual() {
        return VIRTUAL.equals(getType());
    }

    public boolean userIsTeamMember(String userID) {
        String[] t = team.split(",");
        for (int i = 0; i < t.length; i++) {
            if (userID.equals(t[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create data stored in dynamic (generated, not received from device)
     * channels
     *
     * @return
     */
    HashMap getDynamicData() {
        return null;
    }

    /**
     * @return the EUI
     */
    public String getEUI() {
        return EUI;
    }

    /**
     * @param EUI the EUI to set
     */
    public void setEUI(String EUI) {
        this.EUI = EUI;
    }

    /**
     * @return the userID
     */
    public String getUserID() {
        return userID;
    }

    /**
     * @param userID the userID to set
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the team
     */
    public String getTeam() {
        return team;
    }

    /**
     * @param team the team to set
     */
    public void setTeam(String team) {
        this.team = team;
        if (!this.team.startsWith(",")) {
            this.team = "," + this.team;
        }
        if (!this.team.endsWith(",")) {
            this.team = this.team + ",";
        }
    }

    /**
     * @return the channels
     */
    public HashMap getChannels() {
        return channels;
    }

    public String getChannelsAsString() {
        StringBuilder sb = new StringBuilder();
        channels.keySet().forEach(key -> {
            sb.append(key).append(",");
        });
        String result = sb.toString();
        if (!result.isEmpty()) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * @param channels the channels to set
     */
    public void setChannels(LinkedHashMap channels) {
        this.channels = channels;
    }

    public void setChannels(String channels) {
        this.channels = parseChannels(channels);
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    public String getCodeUnescaped() {
        try {
            return URLDecoder.decode(code, "UTF-8");
        } catch (NullPointerException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        if (code != null) {
            this.code = code.replaceAll("\\+", "%2B");
        } else {
            this.code = "";
        }
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the lastSeen
     */
    public long getLastSeen() {
        return lastSeen;
    }

    /**
     * @param lastSeen the lastSeen to set
     */
    public void setLastSeen(long lastSeen) {
        this.lastSeen = lastSeen;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the transmissionInterval
     */
    public long getTransmissionInterval() {
        return transmissionInterval;
    }

    /**
     * @param transmissionInterval the transmissionInterval to set
     */
    public void setTransmissionInterval(long transmissionInterval) {
        this.transmissionInterval = transmissionInterval;
    }

    /**
     * @return the encoder
     */
    public String getEncoder() {
        return encoder;
    }

    public String getEncoderUnescaped() {
        try {
            return URLDecoder.decode(encoder, "UTF-8");
        } catch (NullPointerException | UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * @param encoder the encoder to set
     */
    public void setEncoder(String encoder) {
        if (encoder != null) {
            this.encoder = encoder.replaceAll("\\+", "%2B");
        } else {
            this.team = team;
            if (!this.team.startsWith(",")) {
                this.team = "," + this.team;
            }
            if (!this.team.endsWith(",")) {
                this.team = this.team + ",";
            }
            this.encoder = "";
        }
    }

    private LinkedHashMap parseChannels(String channelsDeclaration) {
        LinkedHashMap result = new LinkedHashMap();
        if (channelsDeclaration != null) {
            String[] channelArray = channelsDeclaration.split(",");
            for (String tmp : channelArray) {
                Channel c = new Channel();
                c.setName(tmp.trim().toLowerCase());
                result.put(c.getName(), c);
            }
        }
        return result;
    }

    /**
     * @return the lastFrame
     */
    public long getLastFrame() {
        return lastFrame;
    }

    /**
     * @param lastFrame the lastFrame to set
     */
    public void setLastFrame(long lastFrame) {
        this.lastFrame = lastFrame;
    }

    /**
     * @return the checkFrames
     */
    public boolean isCheckFrames() {
        return checkFrames;
    }

    /**
     * @param checkFrames the checkFrames to set
     */
    public void setCheckFrames(boolean checkFrames) {
        this.checkFrames = checkFrames;
    }

    /**
     * @return the template
     */
    public String getTemplate() {
        return template;
    }

    /**
     * @param template the template to set
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * @return the pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * @param pattern the pattern to set
     */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /**
     * @return the downlink
     */
    public String getDownlink() {
        return downlink;
    }

    /**
     * @param downlink the downlink to set
     */
    public void setDownlink(String downlink) {
        this.downlink = downlink;
    }

    /**
     * @return the commandScript
     */
    public String getCommandScript() {
        return commandScript;
    }

    /**
     * @param commandScript the commandScript to set
     */
    public void setCommandScript(String commandScript) {
        this.commandScript = commandScript;
    }

    /**
     * @return the applicationEUI
     */
    public String getApplicationEUI() {
        return applicationEUI;
    }

    /**
     * @param applicationEUI the applicationEUI to set
     */
    public void setApplicationEUI(String applicationEUI) {
        this.applicationEUI = applicationEUI;
    }

    /**
     * @return the applicationID
     */
    public String getApplicationID() {
        return applicationID;
    }

    /**
     * @param applicationID the applicationID to set
     */
    public void setApplicationID(String applicationID) {
        this.applicationID = applicationID;
    }

    /**
     * @return the groups
     */
    public String getGroups() {
        return groups;
    }

    /**
     * @param groupEUI the groups to set
     */
    public void setGroups(String groups) {
        this.groups = groups;
        if (null == groups) {
            this.groups = ",";
            return;
        }
        if (!this.groups.startsWith(",")) {
            this.groups = "," + this.groups;
        }
        if (!this.groups.endsWith(",")) {
            this.groups = this.groups + ",";
        }
    }

    /**
     * @return the alertStatus
     */
    public int getAlertStatus() {
        return alertStatus;
    }

    /**
     * @param alertStatus the alertStatus to set
     */
    public void setAlertStatus(int alertStatus) {
        this.alertStatus = alertStatus;
    }

    /**
     * @return the deviceID
     */
    public String getDeviceID() {
        return deviceID;
    }

    /**
     * @param deviceID the deviceID to set
     */
    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    /**
     * @return the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the project
     */
    public String getProject() {
        return project;
    }

    /**
     * @param project the project to set
     */
    public void setProject(String project) {
        this.project = project;
    }

    /**
     * @return the latitude
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return the altitude
     */
    public Double getAltitude() {
        return altitude;
    }

    /**
     * @param altitude the altitude to set
     */
    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }

    /**
     * @return the state
     */
    public Double getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(Double state) {
        this.state = state;
    }

    /**
     * @return the retentionTime
     */
    public long getRetentionTime() {
        return retentionTime;
    }

    /**
     * @param retentionTime the retentionTime to set
     */
    public void setRetentionTime(long retentionTime) {
        this.retentionTime = retentionTime;
    }

}
