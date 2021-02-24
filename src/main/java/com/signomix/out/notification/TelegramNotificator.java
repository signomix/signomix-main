/**
 * Copyright (C) Grzegorz Skorupa 2019.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.notification;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.exception.AdapterException;
import org.cricketmsf.in.http.Result;
import org.cricketmsf.out.http.HttpClient;
import org.cricketmsf.out.http.Request;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author greg
 */
public class TelegramNotificator extends HttpClient implements NotificationIface, Adapter {

    private String token;
    private boolean ready = false;

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        token = properties.getOrDefault("token", ""); // application token
        if (token.startsWith("$")) {
            token = System.getenv(token.substring(1));
        }

        if (endpointURL.isEmpty() || token.isEmpty()) {
            ready = false;
        } else {
            ready = true;
        }
        Kernel.getInstance().getLogger().print("\ttoken: " + token);
    }

    @Override
    public String send(String userID, String recipient, String nodeName, String message) {
        return send(recipient, nodeName, message);
    }

    @Override
    public String send(String recipient, String nodeName, String message) {
        if (!ready) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "not configured"));
            return "ERROR: not configured";
        }
        String chatID = recipient.substring(recipient.indexOf("@") + 1);

        
        Result r = null;
        Request request;
        try {
            String text = URLEncoder.encode(""+message, "UTF-8");
            request = new Request()
                    .setMethod("GET")
                    .setUrl(endpointURL + "bot" + token + "/sendMessage?chat_id=" + chatID + "&text=" + nodeName + " " + text);
        } catch (UnsupportedEncodingException ex) {
            return "ERROR: " + ex.getMessage();
        }
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
        }
        String json = new String(r.getPayload());
        return "OK";
    }

    public String getChatID(String recipent) {
        Result r = null;
        Request request = new Request()
                .setMethod("GET")
                .setUrl(endpointURL + "bot" + token + "/getUpdates");
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
        }
        String json = new String(r.getPayload());
        JSONObject obj = new JSONObject(json);
        JSONArray arr = obj.getJSONArray("result");
        String chat_id = null;
        JSONObject tmp;
        if (!arr.isEmpty()) {
            for (int i = 0; i < arr.length(); i++) {
                tmp = arr.getJSONObject(i).getJSONObject("message").getJSONObject("chat");
                if (recipent.equals(tmp.getString("username"))) {
                    chat_id = "" + tmp.getLong("id");
                    break;
                }
            }
        }
        return chat_id;
    }

}
