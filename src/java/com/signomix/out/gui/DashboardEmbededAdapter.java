/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.gui;

import com.signomix.iot.IotEvent;
import com.signomix.out.db.IotDatabaseIface;
import com.signomix.out.iot.ThingsDataException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.microsite.out.auth.AuthAdapterIface;
import org.cricketmsf.microsite.out.auth.AuthException;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.out.OutboundAdapter;
import org.cricketmsf.out.db.KeyValueDBIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class DashboardEmbededAdapter extends OutboundAdapter implements Adapter, DashboardAdapterIface {

    private String helperAdapterName;
    private String userAdapterName;
    private KeyValueDBIface database = null;
    private UserAdapterIface userAdapter = null;

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        helperAdapterName = properties.get("helper-name");
        Kernel.getInstance().getLogger().print("\thelper-name: " + helperAdapterName);
        userAdapterName = properties.get("helper-name2");
        Kernel.getInstance().getLogger().print("\thelper-name2: " + userAdapterName);
    }

    private KeyValueDBIface getDatabase() {
        if (database == null) {
            database = (KeyValueDBIface) Kernel.getInstance().getAdaptersMap().get(helperAdapterName);
        }
        return database;
    }
    
    private IotDatabaseIface getIotDB() {
        return (IotDatabaseIface) Kernel.getInstance().getAdaptersMap().get(helperAdapterName);
    }

    private UserAdapterIface getUserAdapter() {
        if (userAdapter == null) {
            userAdapter = (UserAdapterIface) Kernel.getInstance().getAdaptersMap().get(userAdapterName);
        }
        return userAdapter;
    }

    @Override
    public void addDashboard(String userID, Dashboard dashboard, AuthAdapterIface authAdapter) throws DashboardException {
        if (!userID.equals(dashboard.getUserID())) {
            throw new DashboardException(DashboardException.NOT_AUTHORIZED, "user IDs not match");
        }
        try {
            if (getIotDB().isDashboardRegistered(dashboard.getId())) {
                throw new DashboardException(DashboardException.ALREADY_EXISTS, "this dashboard ID already exists");
            }
            if (dashboard.isShared()) {
                dashboard.setSharedToken(createSharedToken(userID, dashboard.getId(), authAdapter));
                Kernel.handle(new IotEvent(IotEvent.DASHBOARD_SHARED, this, dashboard.getSharedToken()));
            }
            //getDatabase().put("dashboards", dashboard.getId(), dashboard);
            getIotDB().addDashboard(dashboard);
        } catch (ThingsDataException ex) {
            throw new DashboardException(DashboardException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public void modifyDashboard(String userID, Dashboard dashboard, AuthAdapterIface authAdapter) throws DashboardException {
        if (!userID.equals(dashboard.getUserID())) {
            throw new DashboardException(DashboardException.NOT_AUTHORIZED, "user IDs not match");
        }
        Dashboard original;
        try {
            //original = (Dashboard) getDatabase().get("dashboards", dashboard.getId());
            original = getIotDB().getDashboard(dashboard.getId());
            //if (!getDatabase().containsKey("dashboards", dashboard.getId())) {
            if (original == null) {
                throw new DashboardException(DashboardException.NOT_FOUND, "dashboard ID not found");
            }
            //TODO: przepisać parametry z original
            //if(dashboard.isShared()==null){
            //    dashboard.setShared(original.isShared());
            //}
            
            if (original.isShared() && !dashboard.isShared()) {
                Kernel.getInstance().handle((Event)new IotEvent(IotEvent.DASHBOARD_UNSHARED, this, original.getSharedToken()));
            } else if (dashboard.isShared() && !original.isShared()) {
                dashboard.setSharedToken(createSharedToken(userID, dashboard.getId(), authAdapter));
                Kernel.getInstance().handle((Event)new IotEvent(IotEvent.DASHBOARD_SHARED, this, dashboard.getSharedToken()));
            } else if(dashboard.isShared() && original.isShared()){
                dashboard.setSharedToken(original.getSharedToken());
            }
            //getDatabase().put("dashboards", dashboard.getId(), dashboard);
            getIotDB().updateDashboard(dashboard);
        } catch (ThingsDataException |ClassCastException | NullPointerException ex) {
            throw new DashboardException(DashboardException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public void removeDashboard(String userID, String dashboardID) throws DashboardException {
        try {
            Dashboard d = getIotDB().getDashboard(userID, dashboardID);
            getIotDB().removeDashboard(userID, dashboardID);
            if (d!=null && d.isShared()) {
                Kernel.handle(new IotEvent(IotEvent.DASHBOARD_REMOVED, this, d.getSharedToken()));
            }
        } catch (NullPointerException | ThingsDataException ex) {
            throw new DashboardException(DashboardException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public Dashboard getDashboard(String userId, String dashboardID) throws DashboardException {
        try {
            return getIotDB().getDashboard(userId, dashboardID);
        } catch (ThingsDataException ex) {
            throw new DashboardException(DashboardException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public List<Dashboard> getUserDashboards(String userID) throws DashboardException {
        try {
            return getIotDB().getUserDashboards(userID, true);
            /*
            try {
            Map map = getUserDashboardsMap(userID);
            ArrayList list = new ArrayList();
            Iterator itr = map.values().iterator();
            Dashboard d;
            while (itr.hasNext()) {
            d = (Dashboard) itr.next();
            //if (d.getUserID().equals(userID)) {
            list.add(d);
            //}
            }
            return list;
            } catch (NullPointerException | DashboardException ex) {
            throw new DashboardException(DashboardException.HELPER_EXCEPTION, ex.getMessage());
            }
            */
        } catch (ThingsDataException ex) {
            throw new DashboardException(DashboardException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public boolean isAuthorized(String userID, String dashboardID) throws DashboardException {
        try {
            return getIotDB().isAuthorized(userID, dashboardID);
            /*
            //System.out.println("isAuthorized: " + userID);
            try {
            Dashboard dashboard = (Dashboard) getDatabase().get("dashboards", dashboardID);
            if (dashboard != null) {
            if (dashboard.getUserID().equals(userID) || dashboard.isShared()) {
            return true;
            } else {
            return dashboard.isTeamMember(userID);
            }
            } else {
            return false;
            }
            } catch (KeyValueDBException ex) {
            throw new DashboardException(DashboardException.HELPER_EXCEPTION, ex.getMessage());
            }
            */
        } catch (ThingsDataException ex) {
            throw new DashboardException(DashboardException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public void removeUserDashboards(String userID) throws DashboardException {
        try {
            getIotDB().removeUserDashboards(userID);
            /*
            List dashboards = getUserDashboards(userID);
            Dashboard d;
            for (int i = 0; i < dashboards.size(); i++) {
            d = (Dashboard) dashboards.get(i);
            removeDashboard(userID, d.getId());
            }
            */
        } catch (ThingsDataException ex) {
            throw new DashboardException(DashboardException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

    @Override
    public Map<String, Dashboard> getUserDashboardsMap(String userID) throws DashboardException {
        //TODO: team allowed to use dashboard
        HashMap<String, Dashboard> map = new HashMap();
        List<Dashboard> list = getUserDashboards(userID);
        for(int i =0;i<list.size();i++){
            map.put(list.get(i).getId(),list.get(i));
        }
        return map;
        /*
        try {
            Map<String, Dashboard> map = getDatabase().getAll("dashboards");
            HashMap result = new HashMap();
            map.entrySet().stream().filter(
                    (entry) -> (userID.equals(entry.getValue().getUserID()) || entry.getValue().isTeamMember(userID))
            ).forEachOrdered(
                    (entry) -> {
                        result.put(entry.getKey(), entry.getValue());
                    }
            );
            return result;
        } catch (NullPointerException | KeyValueDBException ex) {
            throw new DashboardException(DashboardException.HELPER_EXCEPTION, ex.getMessage());
        }
        */
    }

    private String createSharedToken(String userID, String dashboardID, AuthAdapterIface authAdapter) throws DashboardException {
        try {
            return authAdapter.createPermanentToken("public", userID, true, dashboardID).getToken();
        } catch (AuthException ex) {
            throw new DashboardException(DashboardException.HELPER_EXCEPTION, ex.getMessage());
        }
    }

}
