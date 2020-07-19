/**
 * Copyright (C) Grzegorz Skorupa 2020.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.notification;

import java.util.HashMap;
import java.util.Map;
import org.cricketmsf.Adapter;
import org.cricketmsf.exception.AdapterException;
import org.cricketmsf.in.http.Result;
import org.cricketmsf.out.http.HttpClient;
import org.cricketmsf.out.http.Request;

public class WebhookNotificator extends HttpClient implements NotificationIface, Adapter {

    protected HashMap<String, String> statusMap = null;

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
    }

    @Override
    public String send(String userID, String recipient, String nodeName, String message) {
        return send(recipient, nodeName, message);
    }

    @Override
    public String send(String webhookUrl, String deviceEUI, String jsonObject) {
        String data = jsonObject.trim();
        data = "{" + "\"eui\":\"" + deviceEUI + "\"," + jsonObject.substring(1);
        Result r=null;
        Request request = new Request()
                .setMethod("POST")
                .setUrl(webhookUrl)
                .setProperty("Content-type", "application/json")
                .setData(data);
        try {
            r = send(request);
        } catch (AdapterException ex) {
            if (null == r) {
                return "ERROR";
            } else {
                return "ERROR " + r.getCode() + ": " + r.getMessage();
            }
        }
        if (r.getCode() != 200) {
            return "ERROR " + r.getCode() + ": " + r.getMessage();
        } else {
            return "OK";
        }
    }

    @Override
    public void updateStatusItem(String key, String value) {
        statusMap.put(key, value);
    }

    @Override
    public Map<String, String> getStatus(String name) {
        if (statusMap == null) {
            statusMap = new HashMap();
            statusMap.put("name", name);
            statusMap.put("class", getClass().getName());
        }
        return statusMap;
    }

    @Override
    public String getChatID(String recipent) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
