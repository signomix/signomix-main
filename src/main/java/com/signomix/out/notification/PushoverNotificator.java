/**
 * Copyright (C) Grzegorz Skorupa 2018.
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
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.out.http.OutboundHttpAdapter;
import org.cricketmsf.out.http.Request;

/**
 *
 * @author greg
 */
public class PushoverNotificator extends OutboundHttpAdapter implements NotificationIface, Adapter {

    //private String url;
    private String token;
    boolean ready = false;

    public void loadProperties(HashMap<String, String> properties, String adapterName) {

        super.loadProperties(properties, adapterName);
        //url = properties.getOrDefault("url", "");  //https://api.pushover.net/1/messages.json
        token = properties.getOrDefault("token", ""); // application token
        if (token.startsWith("$")) {
            token = System.getenv(token.substring(1));
        }

        if (endpointURL.isEmpty() || token.isEmpty()) {
            ready = false;
        } else {
            ready = true;
        }
        //Kernel.getInstance().getLogger().print("\turl: " + url);
        Kernel.getInstance().getLogger().print("\ttoken: " + token);
    }

    @Override
    public String send(String userID, String recipient, String nodeName, String message) {
        return send(recipient, nodeName, message);
    }

    public String send(String recipient, String nodeName, String message) {
        if (!ready) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "not configured"));
            return "ERROR: not configured";
        }

        StandardResult result = new StandardResult();
        Request r = new Request();
        r.properties.put("Content-Type", "application/x-www-form-urlencoded");
        r.method = "POST";
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("token=").append(token).append("&");
            sb.append("user=").append(recipient).append("&");
            sb.append("message=").append(URLEncoder.encode(nodeName + ": " + message, "UTF-8"));
            r.setData(sb.toString());
            result = (StandardResult) send(r, false);
        } catch (AdapterException | UnsupportedEncodingException e) {
            return "ERROR: " + e.getMessage();
        }
        if (result.getCode() == 200) {
            return new String(result.getPayload());
        } else {
            return "ERROR: " + result.getCode() + " " + result.getPayload();
        }
    }

    @Override
    public String getChatID(String recipent) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
