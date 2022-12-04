/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.gui;

import java.text.Format;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class Widget {
    private String name;
    private String dev_id;
    private String channel;
    private String channelTranslated;
    private String type;
    private String query;
    private String range;
    private String title;
    private String description;
    private String unitName;
    private int width;
    private String group;
    private boolean modified;
    private String chartOption;
    private String app_id;
    private String format;
    private String commandType;
    private String role;
    private String unit;
    private String rounding;
    private String icon;
    private String config;
    
    public Widget(){
        width=1;
        modified=false;
    }
    
    public Widget(String userID, String name){
        width=1;
        modified=false;
    }
    

    public String getChartOption() {
        return chartOption;
    }

    public void setChartOption(String chartOption) {
        this.chartOption = chartOption;
    }

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getRounding() {
        return rounding;
    }

    public void setRounding(String rounding) {
        this.rounding = rounding;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getChannelTranslated() {
        return channelTranslated;
    }

    public void setChannelTranslated(String channelTranslated) {
        this.channelTranslated = channelTranslated;
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
     * @return the dev_id
     */
    public String getDev_id() {
        return dev_id;
    }

    /**
     * @param dev_id the dev_id to set
     */
    public void setDev_id(String dev_id) {
        this.dev_id = dev_id;
    }

    /**
     * @return the channel
     */
    public String getChannel() {
        return channel;
    }

    /**
     * @param channel the channel to set
     */
    public void setChannel(String channel) {
        this.channel = channel.replaceAll("\\s", "");
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
     * @return the query
     */
    public String getQuery() {
        if(query==null || query.isEmpty()){
            setQuery("last");
        }
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @return the maximum
     */
    public String getRange() {
        return range;
    }

    /**
     * @param maximum the maximum to set
     */
    public void setRange(String range) {
        this.range = range;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
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
     * @return the unitName
     */
    public String getUnitName() {
        return unitName;
    }

    /**
     * @param unitName the unitName to set
     */
    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        if(width<1||width>4){
            return 1;
        }
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }
    
    public String serialize(){
        return JsonWriter.objectToJson(this);
    }
    
    public static Widget parse(String source){
        return (Widget) JsonReader.jsonToJava(source);
    }

    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(String group) {
        this.group = group;
    }
    
    public void normalize(){
        setChannel(channel);
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

}
