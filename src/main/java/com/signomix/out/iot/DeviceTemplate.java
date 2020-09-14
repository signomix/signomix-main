/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.iot;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class DeviceTemplate {
    
    private String eui; //product type
    private String appid;
    private String appeui;
    private String type;
    private String channels;
    private String code;
    private String decoder;
    private String description;
    private int interval;
    private String pattern; //required fields
    private String commandScript;
    private String producer; //producer name

    //TODO: change uid to uidHex and add validation (is it hex value)
    public DeviceTemplate() {
        super();
    }

    /**
     * @return the producer
     */
    public String getProducer() {
        return producer;
    }

    /**
     * @param producer the producer to set
     */
    public void setProducer(String producer) {
        this.producer = producer;
    }

    /**
     * @return the eui
     */
    public String getEui() {
        return eui;
    }

    /**
     * @param eui the eui to set
     */
    public void setEui(String eui) {
        this.eui = eui;
    }

    /**
     * @return the appid
     */
    public String getAppid() {
        return appid;
    }

    /**
     * @param appid the appid to set
     */
    public void setAppid(String appid) {
        this.appid = appid;
    }

    /**
     * @return the appeui
     */
    public String getAppeui() {
        return appeui;
    }

    /**
     * @param appeui the appeui to set
     */
    public void setAppeui(String appeui) {
        this.appeui = appeui;
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
     * @return the channels
     */
    public String getChannels() {
        return channels;
    }

    /**
     * @param channels the channels to set
     */
    public void setChannels(String channels) {
        this.channels = channels;
    }

    /**
     * @return the code
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return the decoder
     */
    public String getDecoder() {
        return decoder;
    }

    /**
     * @param decoder the decoder to set
     */
    public void setDecoder(String decoder) {
        this.decoder = decoder;
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
     * @return the interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * @param interval the interval to set
     */
    public void setInterval(int interval) {
        this.interval = interval;
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

}
