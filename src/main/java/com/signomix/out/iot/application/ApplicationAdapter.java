/*
 * Copyright 2022 Grzegorz Skorupa <g.skorupa at gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.signomix.out.iot.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.signomix.out.db.IotDbDataIface;
import com.signomix.out.iot.ThingsDataException;

import org.cricketmsf.Adapter;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.OutboundAdapter;

/**
 *
 * @author greg
 */
public class ApplicationAdapter extends OutboundAdapter implements Adapter, ApplicationAdapterIface {

    private IotDbDataIface database = null;
    private String helperAdapterName = null;
    private boolean initialized = false;

    private IotDbDataIface getDatabase() {
        return (IotDbDataIface) Kernel.getInstance().getAdaptersMap().get(helperAdapterName);
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        helperAdapterName = properties.get("helper-name");
        Kernel.getInstance().getLogger().print("\thelper-name: " + helperAdapterName);
    }

    @Override
    public Application getApplication(long id) throws ThingsDataException {
        Application app;
        try {
            app = (Application) getDatabase().getApplication(id);
            return app;
        } catch (ThingsDataException | ClassCastException | NullPointerException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }
    
    @Override
    public List getAllApplications() throws ThingsDataException {
        ArrayList<Application> list;
        try {
            list = (ArrayList<Application>) getDatabase().getAllApplications();
            return list;
        } catch (ThingsDataException | ClassCastException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public Application createApplication(Application application) throws ThingsDataException {
        Application app = application;
        Random r = new Random(System.currentTimeMillis());
        try {
            app=getDatabase().addApplication(app);
            return app;
        } catch (ThingsDataException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void modifyApplication(Application app) throws ThingsDataException {
        try {
            getDatabase().updateApplication(app);
        } catch (ThingsDataException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void removeApplication(long appId) throws ThingsDataException {
        try {
            getDatabase().removeApplication(appId);
        } catch (ThingsDataException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e){
            e.printStackTrace();
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public List<Application> getApplications(long organizationId) throws ThingsDataException {
        ArrayList<Application> list;
        try {
            list = (ArrayList<Application>) getDatabase().getApplications(organizationId);
            return list;
        } catch (ThingsDataException | ClassCastException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

}
