/**
 * Copyright (C) Grzegorz Skorupa 2020.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.in.http;

import com.cedarsoftware.util.io.JsonObject;
import com.cedarsoftware.util.io.JsonReader;
import com.signomix.event.NewDataEvent;
import com.signomix.iot.IotData;
import com.signomix.iot.generic.IotData2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.event.ProcedureCall;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.HttpPortedAdapter;

public class IotApi extends HttpPortedAdapter {

    private boolean authorizationRequired = false;
    private boolean ignoreServiceResponseCode = false;

    public IotApi() {
        super();
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        setContext(properties.get("context"));
        authorizationRequired = Boolean.parseBoolean(properties.get("authorization-required"));
        setProperty("authorization-required", "" + authorizationRequired);
        ignoreServiceResponseCode = Boolean.parseBoolean(properties.get("overwrite-resp-code"));
        setProperty("overwrite-resp-code", "" + ignoreServiceResponseCode);
    }

    /**
     * Transforming request data to Event type required by the service adapter.
     *
     * @param request
     * @param rootEventId
     * @return Wrapper object for the event and the port procedure name.
     */
    protected ProcedureCall preprocess(RequestObject request, long rootEventId) {
        // validation and translation 
        String method = request.method;
        if ("POST".equalsIgnoreCase(method)) {
            return preprocessPost(request, rootEventId);
        } else if ("OPTIONS".equalsIgnoreCase(method)) {
            return ProcedureCall.respond(200, "OK");
        } else {
            return ProcedureCall.respond(HttpAdapter.SC_METHOD_NOT_ALLOWED, "error");
        }
    }

    private ProcedureCall preprocessPost(RequestObject request, long rootEventId) {
        String errorMessage = "";
        ProcedureCall result = null;
        IotData2 iotData = null;
        String dataString = request.body;
        String jsonString = null;
        boolean authProblem = false;

        String authKey = request.headers.getFirst("Authorization");
        if (authorizationRequired && (null == authKey || authKey.isBlank())) {
            errorMessage = "no authorization header";
            authProblem = true;
        } else {
            if (dataString == null) {
                dataString = ((String) request.parameters.getOrDefault("data", "")).trim();
            }
            if (dataString.isEmpty()) {
                iotData = parseIotData(request.parameters);
                dataString = buildParamString(request.parameters);
            } else {
                StringBuilder sb = new StringBuilder(dataString.trim());
                sb.insert(1, "{\"@type\":\"com.signomix.iot.IotData2\",");
                jsonString = sb.toString();
                try {
                    iotData = (IotData2) JsonReader.jsonToJava(jsonString);
                } catch (Exception e) {
                    errorMessage = e.getMessage();
                    Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), "deserialization problem from " + request.clientIp + " " + jsonString));
                }
            }
        }

        if (errorMessage.isEmpty()) {
            iotData.prepareIotValues();
            Event ev = new Event(this.getName(), request);
            ev.setRootEventId(rootEventId);
            ev.setPayload(new IotData(iotData)
                    .authRequired(authorizationRequired)
                    .authKey(request.headers.getFirst("Authorization"))
                    .serializedData(null != jsonString ? jsonString : dataString)
            );
            NewDataEvent event = new NewDataEvent(ev);
            result = ProcedureCall.forward(event, "processData", ignoreServiceResponseCode ? 200 : 0);
        } else {
            if (authProblem) {
                if (ignoreServiceResponseCode) {
                    result = ProcedureCall.respond(200, errorMessage);
                } else {
                    result = ProcedureCall.respond(401, errorMessage);
                }
            } else {
                if (ignoreServiceResponseCode) {
                    result = ProcedureCall.respond(200, errorMessage);
                } else {
                    result = ProcedureCall.respond(400, errorMessage);
                }
            }
        }
        return result;
    }

    private IotData2 parseIotData(Map<String, Object> parameters) {
        IotData2 data = new IotData2();
        data.dev_eui = null;
        data.timestamp = "" + System.currentTimeMillis();
        data.payload_fields = new ArrayList<>();
        HashMap<String, String> map;
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = (String) entry.getValue();
            if ("eui".equalsIgnoreCase(key)) {
                data.dev_eui = value;
                System.out.println("dev_eui:" + data.dev_eui);
            } else if ("timestamp".equalsIgnoreCase(key)) {
                data.timestamp = value;
            } else if ("authkey".equalsIgnoreCase(key)) {
                data.authKey = value;
            } else if ("clienttitle".equalsIgnoreCase(key)) {
                data.clientname = value;
            } else if ("payload".equalsIgnoreCase(key)) {
                data.payload = value;
            } else {
                map = new HashMap<>();
                map.put("name", key);
                map.put("value", value);
                data.payload_fields.add(map);
                System.out.println(key + ":" + value);
            }
            System.out.println("timestamp:" + data.timestamp);
        }
        if (null == data.dev_eui || data.payload_fields.isEmpty()) {
            System.out.println("ERROR: " + data.dev_eui + "," + data.payload_fields);
            return null;
        }
        data.normalize();
        return data;
    }

    private String buildParamString(Map<String, Object> parameters) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = (String) entry.getValue();
            result.append(entry.getKey()).append("=").append((String) entry.getValue()).append("\r\n");
        }
        return result.toString();
    }

    private IotData2 processObject(JsonObject o) {
        IotData2 data = new IotData2();
        /*
        Uplink iotData = new Uplink();
        iotData.setAdr((boolean) o.get("adr"));
        iotData.setApplicationID((String) o.get("applicationID"));
        iotData.setApplicationName((String) o.get("applicationName"));
        iotData.setData((String) o.get("data"));
        iotData.setDevEUI((String) o.get("devEUI"));
        iotData.setDeviceName((String) o.get("deviceName"));
        iotData.setfCnt((long) o.get("fCnt"));
        iotData.setfPort((long) o.get("fPort"));

        //tx
        JsonObject txObj = (JsonObject) o.get("txInfo");
        TxInfo txInfo = new TxInfo();
        txInfo.setDr((long) txObj.get("dr"));
        txInfo.setFrequency((long) txObj.get("frequency"));
        iotData.setTxInfo(txInfo);

        //rx
        ArrayList<RxInfo> rxList = new ArrayList<>();
        //List<JsonObject> objList = (List) o.get("rxInfo");
        Object[] jo = (Object[]) o.get("rxInfo");
        JsonObject rxObj, locObj;
        RxInfo rxInfo;
        Location loc;

        for (int i = 0; i < jo.length; i++) {
            rxObj = (JsonObject) jo[i];
            rxInfo = new RxInfo();
            rxInfo.setGatewayID((String) rxObj.get("gatewayID"));
            rxInfo.setUplinkID((String) rxObj.get("uplinkID"));
            rxInfo.setName((String) rxObj.get("name"));
            rxInfo.setRssi((long) rxObj.get("rssi"));
            rxInfo.setLoRaSNR((long) rxObj.get("loRaSNR"));
            locObj = (JsonObject) rxObj.get("location");
            loc = new Location();
            loc.setLatitude((Double) locObj.get("latitude"));
            loc.setLongitude((Double) locObj.get("longitude"));
            loc.setAltitude((long) locObj.get("altitude"));
            rxInfo.setLocation(loc);
            rxList.add(rxInfo);
        }
        iotData.setRxInfo(rxList);

        //data
        JsonObject data = (JsonObject) o.get("object");
        JsonObject dataMap;
        JsonObject dataFieldsMap;
        Iterator it = data.keySet().iterator();
        Iterator it2, it3;
        String dataName;
        String dataIndex, dataField;
        Object dataValue;
        while (it.hasNext()) {
            dataName = (String) it.next();
            dataMap = (JsonObject) data.get(dataName);
            it2 = dataMap.keySet().iterator();
            while (it2.hasNext()) {
                dataIndex = (String) it2.next();
                dataValue = dataMap.get(dataIndex);
                if (dataValue instanceof Double) {
                    iotData.addField(dataName + "_" + dataIndex, (Double) dataValue);
                } else {
                    dataFieldsMap = (JsonObject) dataValue;
                    it3 = dataFieldsMap.keySet().iterator();
                    while (it3.hasNext()) {
                        dataField = (String) it3.next();
                        dataValue = dataFieldsMap.get(dataField);
                        iotData.addField(dataName + "_" + dataIndex + "_" + dataField, (Double) dataValue);
                    }
                }
            }
        }
        return iotData;
         */
        return data;
    }

}
