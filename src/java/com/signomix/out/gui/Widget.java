/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.gui;

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
    private String type;
    private String query;
    private String range;
    private String title;
    private String description;
    private String unitName;
    private int width;
    private String group;
    
    public Widget(){
        width=1;
    }
    
    public Widget(String userID, String name){
        width=1;
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

}
