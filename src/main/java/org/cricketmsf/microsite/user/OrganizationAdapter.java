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
package org.cricketmsf.microsite.user;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cricketmsf.Adapter;
import org.cricketmsf.Kernel;
import org.cricketmsf.microsite.out.user.UserException;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.KeyValueDBIface;

/**
 *
 * @author greg
 */
public class OrganizationAdapter extends OutboundAdapter implements Adapter, OrganizationAdapterIface {

    private KeyValueDBIface database = null;
    private String helperAdapterName = null;
    private boolean initialized = false;

    private KeyValueDBIface getDatabase() {
        if (database == null) {
            try {
                database = (KeyValueDBIface) Kernel.getInstance().getAdaptersMap().get(helperAdapterName);
            } catch (Exception e) {
            }
        }
        return database;
    }

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        helperAdapterName = properties.get("helper-name");
        Kernel.getInstance().getLogger().print("\thelper-name: " + helperAdapterName);
    }

    @Override
    public Organization getOrganization(long id) throws UserException {
        Organization org;
        try {
            org = (Organization) getDatabase().get("organizations", ""+id);
            return org;
        } catch (KeyValueDBException | ClassCastException | NullPointerException e) {
            throw new UserException(UserException.HELPER_EXCEPTION, e.getMessage());
        }
    }
    
    @Override
    public Map getAllOrganizations() throws UserException {
        HashMap<String, Organization> map;
        try {
            map = (HashMap<String, Organization>) getDatabase().getAll("organizations");
            return map;
        } catch (KeyValueDBException | ClassCastException e) {
            throw new UserException(UserException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public Organization createOrganization(Organization organization) throws UserException {
        Organization org = organization;
        Random r = new Random(System.currentTimeMillis());
        try {
            if (getDatabase().containsKey("organizations", ""+org.id)) {
                throw new UserException(UserException.USER_ALREADY_EXISTS, "cannot register");
            }
            getDatabase().put("organizations", ""+org.id, org);
            return getOrganization(org.id);
        } catch (KeyValueDBException e) {
            throw new UserException(UserException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void modifyOrganization(Organization org) throws UserException {
        try {
            if(!getDatabase().containsKey("organizations", ""+org.id)){
                throw new UserException(UserException.UNKNOWN_USER, "organization not found");
            }
            getDatabase().put("organizations", ""+org.id, org);
        } catch (KeyValueDBException e) {
            throw new UserException(UserException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void removeOrganization(Organization org) throws UserException {
        try {
            getDatabase().remove("organizations", ""+org.id);
            //TODO: event to remove user's data
        } catch (KeyValueDBException e) {
            throw new UserException(UserException.HELPER_EXCEPTION, e.getMessage());
        } catch (Exception e){
            e.printStackTrace();
            throw new UserException(UserException.HELPER_EXCEPTION, e.getMessage());
        }
    }

}
