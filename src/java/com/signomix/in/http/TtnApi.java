/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.in.http;

import org.cricketmsf.Adapter;
import java.util.HashMap;
import org.cricketmsf.Kernel;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.HttpAdapterIface;
import org.cricketmsf.livingdoc.architecture.HexagonalAdapter;

/**
 * TTN REST API
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
@HexagonalAdapter
public class TtnApi extends HttpAdapter implements HttpAdapterIface, Adapter {
    
    private boolean dumpRequest = false;
    private boolean authorizationRequired = true;
    
    /**
     * This method is executed while adapter is instantiated during the service start.
     * It's used to configure the adapter according to the configuration.
     * 
     * @param properties    map of properties read from the configuration file
     * @param adapterName   name of the adapter set in the configuration file (can be different
     *  from the interface and class name.
     */
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        super.getServiceHooks(adapterName);
        setContext(properties.get("context"));
        Kernel.getInstance().getLogger().print("\tcontext=" + getContext());
        setExtendedResponse(properties.getOrDefault("extended-response","false"));
        Kernel.getInstance().getLogger().print("\textended-response=" + isExtendedResponse());
        setDateFormat(properties.get("date-format"));
        Kernel.getInstance().getLogger().print("\tdate-format: " + getDateFormat());
        
        dumpRequest = "true".equalsIgnoreCase(properties.getOrDefault("dump-request", "false"));
        Kernel.getInstance().getLogger().print("\tdump-request: " + dumpRequest);
        properties.put("dump-request", ""+dumpRequest);
        
        authorizationRequired = "true".equalsIgnoreCase(properties.getOrDefault("authorization-required", "true"));
        Kernel.getInstance().getLogger().print("\tauthorization-required: " + authorizationRequired);
        properties.put("authorization-required", ""+authorizationRequired);
    }

}
