/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

import java.util.List;
import java.util.Map;

import com.signomix.out.iot.ThingsDataException;
import com.signomix.out.iot.application.Application;
import com.signomix.out.iot.application.ApplicationAdapterIface;

import org.cricketmsf.Event;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.StandardResult;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class ApplicationModule {

    private static ApplicationModule self;

    public static ApplicationModule getInstance() {
        if (self == null) {
            self = new ApplicationModule();
        }
        return self;
    }

    private boolean isAdmin(RequestObject request) {
        List<String> requesterRoles = request.headers.get("X-user-role");
        // String requesterRole = request.headers.getFirst("X-user-role");
        boolean admin = false;
        for (int i = 0; i < requesterRoles.size(); i++) {
            if ("admin".equals(requesterRoles.get(i))) {
                admin = true;
                break;
            }
        }
        return admin;
    }

    public Object handleGetRequest(Event event, ApplicationAdapterIface applicationAdapter) {
        System.out.println("Application - handleGetRequest");
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        if (!isAdmin(request)) {
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            return result;
        }
        String uid = request.pathExt;
        long appNumber = -1;
        try {
            appNumber = Long.parseLong(uid);
        } catch (ClassCastException | NumberFormatException|NullPointerException e) {
        }

        try {
            if (appNumber==-1) {
                List m = applicationAdapter.getAllApplications();
                result.setData(m);
            } else {
                Application u = (Application) applicationAdapter.getApplication(appNumber);
                result.setData(u);
            }
        } catch (ThingsDataException e) {
            e.printStackTrace();
            result.setCode(HttpAdapter.SC_NOT_FOUND);
        }
        return result;
    }

    public Object handleAddApplication(Event event, ApplicationAdapterIface applicationAdapter) {
        RequestObject request = event.getRequest();
        String uid = request.pathExt;
        StandardResult result = new StandardResult();
        if (!isAdmin(request)) {
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            return result;
        }
        if (uid == null || uid.contains("/")) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            return result;
        }
        try {
            String name = event.getRequestParameter("name");
            String config = event.getRequestParameter("config");
            long orgId=Long.parseLong(event.getRequestParameter("organization"));
            long orgVersion;
            try{
                orgVersion=Long.parseLong(event.getRequestParameter("version"));
            }catch(Exception e){
                orgVersion=parseOrgVersion(event.getRequestParameter("version"));
            }
            Application app = new Application(null, orgId, orgVersion, name, config);
            app=applicationAdapter.createApplication(app);
            result.setCode(HttpAdapter.SC_OK);
            result.setData(app.id);
        } catch (ThingsDataException ex) {
            //Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), ex.getMessage()));
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
        }
        return result;
    }

    public Object handleDeleteRequest(Event event, ApplicationAdapterIface applicationAdapter) {
        // TODO: check requester rights
        // only admin can do this and user status must be IS_UNREGISTERING
        RequestObject request = event.getRequest();
        String uid = request.pathExt;
        StandardResult result = new StandardResult();
        if (!isAdmin(request)) {
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            return result;
        }
        if (uid == null || uid.contains("/")) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            return result;
        }
        try {
            long id = Long.parseLong(uid);
            Application tmpApp = applicationAdapter.getApplication(id);
            applicationAdapter.removeApplication(tmpApp.id);
            // Kernel.getInstance()
            // .dispatchEvent(new UserEvent(UserEvent.USER_DELETED, tmpUser.getNumber() + "
            // " + tmpUser.getUid()));
            result.setCode(HttpAdapter.SC_OK);
            result.setData(uid);
        } catch (ThingsDataException e) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
        }
        return result;
    }

    public Object handleUpdateRequest(Event event, ApplicationAdapterIface applicationAdapter) {
        // TODO: check requester rights
        // only admin can set: role or type differ than default
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        if (!isAdmin(request)) {
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            return result;
        }
        String uid = request.pathExt;
        if (uid == null || uid.contains("/")) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            return result;
        }
        try {
            long id = Long.parseLong(uid);
            Application app = applicationAdapter.getApplication(id);
            if (app == null) {
                result.setCode(HttpAdapter.SC_NOT_FOUND);
                result.setData("application not found");
                return result;
            }
            String name = event.getRequestParameter("name");
            String config = event.getRequestParameter("config");
            long orgVersion;
            try{
                orgVersion=Long.parseLong(event.getRequestParameter("version"));
            }catch(Exception e){
                orgVersion=parseOrgVersion(event.getRequestParameter("version"));
            }
            app.name=name;
            app.config=config;
            app.version=orgVersion;
            applicationAdapter.modifyApplication(app);
            // fire event
            // Kernel.getInstance().dispatchEvent(new ThingsDataEvent(IotEvent.USER_UPDATED,
            // user.getUid()));
            result.setCode(HttpAdapter.SC_OK);
            result.setData(app);
        } catch (NullPointerException | ThingsDataException e) {
            e.printStackTrace();
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
        }
        return result;
    }

    private long parseOrgVersion(String version){
        long orgVersion=0;

        return orgVersion;
    }

}
