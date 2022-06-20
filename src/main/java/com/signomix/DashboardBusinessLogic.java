/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

import com.cedarsoftware.util.io.JsonReader;
import com.signomix.out.gui.Dashboard;
import com.signomix.out.gui.DashboardException;
import org.cricketmsf.Event;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.StandardResult;
import com.signomix.out.gui.DashboardAdapterIface;
import com.signomix.out.iot.ThingsDataException;
import com.signomix.out.iot.ThingsDataIface;
import org.cricketmsf.Kernel;
import org.cricketmsf.microsite.out.auth.AuthAdapterIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class DashboardBusinessLogic {

    private static DashboardBusinessLogic service;

    public static DashboardBusinessLogic getInstance() {
        if (service == null) {
            service = new DashboardBusinessLogic();
        }
        return service;
    }

    /**
     *
     */
    public Object processEvent(Event event, DashboardAdapterIface dashboardAdapter, ThingsDataIface thingsAdapter, AuthAdapterIface authAdapter)
            throws ThingsDataException, DashboardException {
        //TODO: exception handling - send 400 or 403
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        String userID = request.headers.getFirst("X-user-id");
        String dashboardId = request.pathExt; //
        boolean byName = Boolean.parseBoolean((String) request.parameters.getOrDefault("name", ""));
        if (userID == null || userID.isEmpty()) {
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            result.setData("user not recognized");
            return result;
        }

        try {
            Dashboard d;
            switch (request.method.toUpperCase()) {
                case "GET":
                    if (dashboardId.isEmpty()) {
                        //result.setData(dashboardAdapter.getUserDashboards(userID));
                        result.setData(dashboardAdapter.getUserDashboardsMap(userID));
                    } else {
                        if (byName) {
                            dashboardId=dashboardId.substring(0, dashboardId.length()-1);
                            result.setData(dashboardAdapter.getDashboardByName(userID, dashboardId));
                        } else {
                            result.setData(dashboardAdapter.getDashboard(userID, dashboardId));
                        }
                    }
                    break;
                case "POST":
                    d = deserialize(userID, request);
                    dashboardAdapter.addDashboard(userID, d.normalize(), authAdapter);
                    result.setCode(HttpAdapter.SC_CREATED);
                    result.setData(d);
                    break;
                case "PUT":
                    d = deserialize(userID, request);
                    dashboardAdapter.modifyDashboard(userID, d.normalize(), authAdapter);
                    result.setCode(HttpAdapter.SC_OK);
                    result.setData(d);
                    break;
                case "DELETE":
                    dashboardAdapter.removeDashboard(userID, dashboardId);
                default:
                    break;
            }
        } catch (DashboardException ex) {
            if (ex.getCode() == DashboardException.NOT_AUTHORIZED) {
                result.setCode(HttpAdapter.SC_FORBIDDEN);
            } else {
                result.setCode(ex.getCode());
            }
            result.setMessage(ex.getMessage());
            result.setData(ex.getMessage());

        }
        return result;
    }

    public void removeUserDashboards(String userId, DashboardAdapterIface dashboardAdapter) {
        try {
            dashboardAdapter.removeUserDashboards(userId);
        } catch (DashboardException ex) {
            Kernel.handle(Event.logSevere(this.getClass().getSimpleName(), "cannot remove dashboards of user " + userId));
        }
    }

    private Dashboard deserialize(String userID, RequestObject request) throws DashboardException {
        String jsonString = request.body;
        jsonString
                = "{\"@type\":\"com.signomix.out.gui.Dashboard\","
                + jsonString.substring(jsonString.indexOf("{") + 1);
        //System.out.println(jsonString);
        Dashboard dashboard = null;
        try {
            dashboard = (Dashboard) JsonReader.jsonToJava(jsonString);
        } catch (Exception e) {
            Kernel.handle(Event.logSevere(this.getClass().getSimpleName(), "deserialization problem - check @type declaration"));
            e.printStackTrace();
        }
        if (null == dashboard.getId() || dashboard.getId().isEmpty()) {
            dashboard.setId(PlatformAdministrationModule.getInstance().createEui("S-"));
        }
        return dashboard;
    }

}
