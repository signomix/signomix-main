/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.script;

import com.signomix.event.IotEvent;
import com.signomix.out.iot.ChannelData;
import com.signomix.out.iot.Device;
import com.signomix.out.iot.ThingsDataIface;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.cricketmsf.Adapter;
import org.cricketmsf.out.OutboundAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.cricketmsf.Kernel;

/**
 *
 * @author greg
 */
public class NashornScriptingAdapter extends OutboundAdapter implements Adapter, ScriptingAdapterIface {

    private ScriptEngineManager manager;
    private ScriptEngine engine;
    private String scriptTemplate;
    private String scriptLocation;
    private String decoderEnvelope;
    private String decoderEnvelopeLocation;
    private String helperName;
    private ThingsDataIface thingsAdapter;

    /**
     * This method is executed while adapter is instantiated during the service
     * start. It's used to configure the adapter according to the configuration.
     *
     * @param properties map of properties readed from the configuration file
     * @param adapterName name of the adapter set in the configuration file (can
     * be different from the interface and class name.
     */
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        manager = new ScriptEngineManager();
        engine = manager.getEngineByName("nashorn");
        scriptLocation = properties.get("script-file");
        Kernel.getInstance().getLogger().print("\tscript-file: " + scriptLocation);
        decoderEnvelopeLocation = properties.get("decoder-envelope-location");
        Kernel.getInstance().getLogger().print("\tdecoder-envelope-location: " + decoderEnvelopeLocation);
        helperName = properties.get("helper-name");
        Kernel.getInstance().getLogger().print("\thelper-name: " + helperName);
        scriptTemplate = readScript(scriptLocation);
        decoderEnvelope = readScript(decoderEnvelopeLocation);
    }

    private ThingsDataIface getThingsAdapter() {
        if (thingsAdapter == null) {
            thingsAdapter = (ThingsDataIface) Kernel.getInstance().getAdaptersMap().get(helperName);
        }
        return thingsAdapter;
    }
    
    @Override
    public ScriptResult processData(ArrayList<ChannelData> values, Device device, 
            long dataTimestamp, Double latitude, Double longitude, Double altitude, Double state, 
            int alert, String command, String requestData) throws ScriptAdapterException {
        return processData(values, device.getCodeUnescaped(), device.getEUI(), device.getUserID(), 
                dataTimestamp, latitude, longitude, altitude, device.getState(), device.getAlertStatus(),
                device.getLatitude(), device.getLongitude(), device.getAltitude(), command, requestData);
    }

    @Override
    public ScriptResult processData(ArrayList<ChannelData> values, String deviceScript,
            String deviceID, String userID, long dataTimestamp,
            Double latitude, Double longitude, Double altitude,
            Double state, int alert, Double devLatitude, Double devLongitude, Double devAltitude,
            String command, String requestData) throws ScriptAdapterException {
        Invocable invocable;
        ScriptResult result = new ScriptResult();
        if (values == null) {
            return result;
        }
        ChannelClient channelReader = new ChannelClient(userID, deviceID, getThingsAdapter());
        try {
            engine.eval(deviceScript != null ? merge(scriptTemplate, deviceScript) : scriptTemplate);
            invocable = (Invocable) engine;
            result = (ScriptResult) invocable.invokeFunction("processData", deviceID, values, channelReader, userID, dataTimestamp, latitude, longitude, altitude, state, alert,
                    devLatitude, devLongitude, devAltitude, command, requestData);
        } catch (NoSuchMethodException e) {
            fireEvent(2, userID + "\t" + deviceID, e.getMessage());
            throw new ScriptAdapterException(ScriptAdapterException.NO_SUCH_METHOD, "NashornScriptingAdapter.no_such_method " + e.getMessage());
        } catch (ScriptException e) {
            fireEvent(2, userID + "\t" + deviceID, e.getMessage());
            throw new ScriptAdapterException(ScriptAdapterException.SCRIPT_EXCEPTION, "NashornScriptingAdapter.script_exception " + e.getMessage());
        }
        return result;
    }

    @Override
    public ScriptResult processRawData(String requestBody, String deviceScript, String deviceID, String userID, long dataTimestamp) throws ScriptAdapterException {
        Invocable invocable;
        ScriptResult result = new ScriptResult();
        if (requestBody == null) {
            return result;
        }
        ChannelClient channelReader = new ChannelClient(userID, deviceID, getThingsAdapter());
        try {
            engine.eval(deviceScript != null ? merge(scriptTemplate, deviceScript) : scriptTemplate);
            invocable = (Invocable) engine;
            result = (ScriptResult) invocable.invokeFunction("processRawData", deviceID, requestBody, channelReader, userID, dataTimestamp);
        } catch (NoSuchMethodException e) {
            fireEvent(2, userID + "\t" + deviceID, e.getMessage());
            throw new ScriptAdapterException(ScriptAdapterException.NO_SUCH_METHOD, "NashornScriptingAdapter.no_such_method " + e.getMessage());
        } catch (ScriptException e) {
            fireEvent(2, userID + "\t" + deviceID, e.getMessage());
            throw new ScriptAdapterException(ScriptAdapterException.SCRIPT_EXCEPTION, "NashornScriptingAdapter.script_exception " + e.getMessage());
        }
        return result;
    }

    @Override
    public ArrayList<ChannelData> decodeData(byte[] data, String script, String deviceId, long timestamp, String userID) throws ScriptAdapterException {
        Invocable invocable;
        ArrayList<ChannelData> list = new ArrayList<>();
        try {
            engine.eval(script != null ? merge(decoderEnvelope, script) : decoderEnvelope);
            invocable = (Invocable) engine;
            list = (ArrayList) invocable.invokeFunction("decodeData", deviceId, data, timestamp);
        } catch (NoSuchMethodException e) {
            fireEvent(1, userID + "\t" + deviceId, e.getMessage());
            throw new ScriptAdapterException(ScriptAdapterException.NO_SUCH_METHOD, e.getMessage());
        } catch (ScriptException e) {
            fireEvent(1, userID + "\t" + deviceId, e.getMessage());
            throw new ScriptAdapterException(ScriptAdapterException.SCRIPT_EXCEPTION, e.getMessage());
        }
        return list;
    }

    @Override
    public ArrayList<ChannelData> decodeHexData(String hexadecimalPayload, String script, String deviceId, long timestamp, String userID) throws ScriptAdapterException {
        Invocable invocable;
        ArrayList<ChannelData> list = new ArrayList<>();
        try {
            engine.eval(script != null ? merge(decoderEnvelope, script) : decoderEnvelope);
            invocable = (Invocable) engine;
            list = (ArrayList) invocable.invokeFunction("decodeHexData", deviceId, hexadecimalPayload, timestamp);
        } catch (NoSuchMethodException e) {
            fireEvent(1, userID + "\t" + deviceId, e.getMessage());
            throw new ScriptAdapterException(ScriptAdapterException.NO_SUCH_METHOD, e.getMessage());
        } catch (ScriptException e) {
            fireEvent(1, userID + "\t" + deviceId, e.getMessage());
            throw new ScriptAdapterException(ScriptAdapterException.SCRIPT_EXCEPTION, e.getMessage());
        }
        return list;
    }

    String merge(String template, String deviceScript) {
        //Kernel.getInstance().dispatchEvent(Event.logSevere(this, "device script template not available"));
        String res = template.replaceAll("//injectedCode", deviceScript);
        return res;
    }

    /**
     * Reads script from file
     *
     * @param path the file location
     * @return script content
     */
    public String readScript(String path) {
        File file = new File(path);
        byte[] bytes = new byte[(int) file.length()];
        String result;
        InputStream input = null;
        try {
            int totalBytesRead = 0;
            input = new BufferedInputStream(new FileInputStream(file));
            while (totalBytesRead < bytes.length) {
                int bytesRemaining = bytes.length - totalBytesRead;
                int bytesRead = input.read(bytes, totalBytesRead, bytesRemaining);
                if (bytesRead > 0) {
                    totalBytesRead = totalBytesRead + bytesRead;
                }
            }
            result = new String(bytes);
            /*
         the above style is a little bit tricky: it places bytes into the 'bytes' array; 
         'bytes' is an output parameter;
         the while loop usually has a single iteration only.
             */

        } catch (Exception e) {
            Kernel.getInstance().getLogger().print("WARNING: " + e.getClass().getName() + " " + e.getMessage()+". Reading from the classpath.");
            if (path.lastIndexOf("/") > -1) {
                path = path.substring(path.lastIndexOf("/") + 1);
            }
            InputStream resource = getClass().getClassLoader().getResourceAsStream(path);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(resource, "UTF-8"))) {
                result = br.lines().collect(Collectors.joining(System.lineSeparator()));
            } catch (IOException ex) {
                Kernel.getInstance().getLogger().print("ERROR: " + e.getClass().getName() + " " + e.getMessage());
                return null;
            }
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                }
            }
        }
        return result;
    }

    private void fireEvent(int source, String origin, String message) {
        IotEvent ev = new IotEvent();
        ev.setOrigin(origin);
        if (source == 1) {
            ev.setPayload("Decoder script (1): " + message);
        } else {
            ev.setPayload("Data processor script (1): " + message);
        }
        ev.setType(IotEvent.GENERAL);
        Kernel.getInstance().dispatchEvent(ev);
    }



}
