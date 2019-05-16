/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.in.http;

import com.signomix.out.iot.ChannelData;
import java.io.UnsupportedEncodingException;
import org.cricketmsf.Adapter;
import java.util.HashMap;
import java.util.List;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.in.http.HttpAdapter;
import static org.cricketmsf.in.http.HttpAdapter.CSV;
import static org.cricketmsf.in.http.HttpAdapter.JSON;
import static org.cricketmsf.in.http.HttpAdapter.TEXT;
import static org.cricketmsf.in.http.HttpAdapter.XML;
import org.cricketmsf.in.http.HttpAdapterIface;
import org.cricketmsf.in.http.JsonFormatter;
import org.cricketmsf.in.http.Result;
import org.cricketmsf.in.http.TxtFormatter;
import org.cricketmsf.in.http.XmlFormatter;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class ThingsApi extends HttpAdapter implements HttpAdapterIface, Adapter {

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
        super.getServiceHooks(adapterName);
        setContext(properties.get("context"));
        Kernel.getInstance().getLogger().print("\tcontext=" + getContext());
        setExtendedResponse(properties.getOrDefault("extended-response", "false"));
        Kernel.getInstance().getLogger().print("\textended-response=" + isExtendedResponse());
        setDateFormat(properties.get("date-format"));
        Kernel.getInstance().getLogger().print("\tdate-format: " + getDateFormat());
    }

    public byte[] formatResponse(String type, Result result) {
        byte[] r = {};
        String formattedResponse = "";
        switch (type) {
            case JSON:
                formattedResponse = JsonFormatter.getInstance().format(true, isExtendedResponse() ? result : result.getData());
                break;
            case XML:
                //TODO: extended response is not possible because of "java.util.List is an interface, and JAXB can't handle interfaces"
                formattedResponse = XmlFormatter.getInstance().format(true, result.getData());
                break;
            case CSV:
                // formats only Result.getData() object.
                // TODO: concat all list items
                try {
                    formattedResponse = format(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case TEXT:
                // formats only Result.getData() object
                formattedResponse = TxtFormatter.getInstance().format(result);
                break;
            default:
                formattedResponse = JsonFormatter.getInstance().format(true, result);
                break;
        }

        try {
            r = formattedResponse.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Kernel.getInstance().dispatchEvent(Event.logSevere("HttpAdapter", e.getMessage()));
        }
        return r;
    }

    private String format(Result source) {

        StringBuilder sb = new StringBuilder();
        if (false) {
            sb.append(ChannelData.getCsvHeaderLine(true));
        }
        if (((List) source.getData()).isEmpty()) {
            return "";
        } else if (((List) source.getData()).get(0) instanceof List) {
            List<List> devices = (List) source.getData();
            for (List row : devices) {
                if (row.get(0) instanceof ChannelData) {
                    for (Object record : row) {
                        sb.append(((ChannelData) record).toCsv(",", true)).append("\r\n");
                    }
                } else {
                    for (Object column : row) {
                        sb.append("\"").append(column.toString()).append("\",");
                    }
                    sb.deleteCharAt(sb.length() - 1);
                    sb.append("\r\n");
                }
            }
        } /*
        else if (((List) source.getData()).get(0) instanceof ChannelData) {
            ArrayList<ChannelData> list = (ArrayList) source.getData();
            for (ChannelData record : list) {
                sb.append(record.toCsv(",",true)).append("\r\n");
            }
        } 
         */ else {
        }
        return sb.toString();
    }

    @Override
    public String setResponseType(String acceptedResponseType, String fileExt) {
        if (".csv".equalsIgnoreCase(fileExt)) {
            return "text/csv";
        } else {
            return super.setResponseType(acceptedResponseType, fileExt);
        }
    }

}
