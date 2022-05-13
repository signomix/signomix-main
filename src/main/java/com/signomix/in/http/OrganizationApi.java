/*
 * Copyright 2022 Grzegorz Skorupa <g.skorupa at gmail.com>.
 *
 * Licensed under the MIT License
 *
 */
package com.signomix.in.http;

import org.cricketmsf.Adapter;
import java.util.HashMap;
import org.cricketmsf.Kernel;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.HttpAdapterIface;

public class OrganizationApi extends HttpAdapter implements HttpAdapterIface, Adapter {

    /**
     * This method is executed while adapter is instantiated during the service
     * start.
     * It's used to configure the adapter according to the configuration.
     * 
     * @param properties  map of properties readed from the configuration file
     * @param adapterName name of the adapter set in the configuration file (can be
     *                    different
     *                    from the interface and class name.
     */
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        super.getServiceHooks(adapterName);
        setContext(properties.get("context"));
        Kernel.getInstance().getLogger().print("\tcontext=" + getContext());
    }

}
