/**
 * Copyright (C) Grzegorz Skorupa 2020.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.notification;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import com.signomix.common.iot.Device;

import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.OutboundAdapter;

/**
 *
 * @author greg
 */
public class CommandWebHookClient extends OutboundAdapter implements CommandWebHookIface, Adapter {

    private boolean ready = false;
    private String endpointUrl = null;
    private boolean printResponse = false;

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        endpointUrl = properties.getOrDefault("url", "");
        this.properties.put("url", endpointUrl);
        Kernel.getLogger().printIndented("url=" + endpointUrl);
        printResponse = Boolean.parseBoolean(properties.getOrDefault("print-response", "false"));
        this.properties.put("print-response", "" + printResponse);
        Kernel.getLogger().printIndented("print-response=" + printResponse);
        if (endpointUrl.isEmpty()) {
            ready = false;
        } else {
            ready = true;
        }
    }

    @Override
    public boolean send(Device device, String payload, boolean hexRepresentation) {
        boolean isGlobalWebhook = false;
        String deviceEUI = device.getEUI();
        String url = device.getDownlink();
        String deviceKey = device.getKey();

        if (null == url || url.isBlank()) {
            url = endpointUrl;

        }

        if (printResponse) {
            if (isGlobalWebhook) {
                System.out.println("SENDING TO DEVICE WEBHOOK: " + deviceEUI + " " + url);
            } else {
                System.out.println("SENDING TO GLOBAL WEBHOOK: " + deviceEUI + " " + url);
            }
            System.out.println("REQUEST PAYLOAD:" + payload);
            System.out.println("HEX: " + hexRepresentation);
        }
        if (null == url || url.isBlank()) {
            return false;

        }
        String data;
        if (hexRepresentation) {
            data = payload.trim();
        } else {
            data = new String(Base64.getDecoder().decode(payload)).trim();
        }
        if (data.startsWith("{")) {
            return sendJson(url, deviceEUI, deviceKey, data);
        } else {
            return sendForm(url, deviceEUI, deviceKey, data);
        }
    }

    private boolean sendForm(String downlink, String deviceEUI, String deviceKey, String payload) {
        if (!ready) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "not configured"));
            return false;
        }
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // form parameters
        Map<Object, Object> data = new HashMap<>();
        data.put("payload", payload);

        HttpRequest request = HttpRequest.newBuilder()
                .POST(ofFormData(data))
                .uri(URI.create(downlink))
                .setHeader("User-Agent", "Signomix CE") // add request header
                .setHeader("X-device-eui", deviceEUI)
                .setHeader("Authorization", deviceKey)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (printResponse) {
                System.out.println("RESP CODE:" + response.statusCode());
                System.out.println("RESP BODY:" + response.body());
            }
            return response.statusCode() == 200;
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        return false;

    }

    private boolean sendJson(String downlink, String deviceEUI, String deviceKey, String json) {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        // add json header
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .uri(URI.create(downlink))
                .setHeader("User-Agent", "Signomix CE") // add request header
                .setHeader("X-device-eui", deviceEUI)
                .setHeader("Authorization", deviceKey)
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (printResponse) {
                System.out.println("RESP CODE:" + response.statusCode());
                System.out.println("RESP BODY:" + response.body());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public static HttpRequest.BodyPublisher ofFormData(Map<Object, Object> data) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

}
