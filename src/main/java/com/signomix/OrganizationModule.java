/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

import java.util.List;
import java.util.Map;

import org.cricketmsf.Event;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.microsite.out.user.UserException;
import org.cricketmsf.microsite.user.Organization;
import org.cricketmsf.microsite.user.OrganizationAdapterIface;
import org.cricketmsf.microsite.user.UserBusinessLogic;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class OrganizationModule extends UserBusinessLogic {

    private static OrganizationModule self;

    public static OrganizationModule getInstance() {
        if (self == null) {
            self = new OrganizationModule();
        }
        return self;
    }

    private boolean isAdmin(RequestObject request) {
        List<String> requesterRoles = request.headers.get("X-user-role");
        boolean admin = false;
        for (int i = 0; i < requesterRoles.size(); i++) {
            if ("admin".equals(requesterRoles.get(i))) {
                admin = true;
                break;
            }
        }
        return admin;
    }

    public Object handleGetRequest(Event event, OrganizationAdapterIface organizationAdapter) {
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        if(!isAdmin(request)){
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            return result;
        }

        String uid = request.pathExt;
        long id = -1;
        try {
            if (null != uid && !uid.isEmpty()) {
                id = Long.parseLong(uid);
            }
        } catch (ClassCastException | NumberFormatException e) {
            result.setCode(404);
            return result;
        }
        try {
            if (uid.isEmpty()) {
                Map m = organizationAdapter.getAllOrganizations();
                result.setData(m);
            } else {
                Organization org = (Organization) organizationAdapter.getOrganization(id);
                result.setData(org);
            }
        } catch (UserException e) {
            result.setCode(HttpAdapter.SC_NOT_FOUND);
        }
        return result;
    }

    public Object handleCreateRequest(Event event, OrganizationAdapterIface organizationAdapter) {
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        if(!isAdmin(request)){
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            return result;
        }

        String uid = request.pathExt;
        if (uid != null && !uid.isEmpty()) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            return result;
        }
        try {
            Organization newOrg = new Organization(null, event.getRequestParameter("name"));
            newOrg = organizationAdapter.createOrganization(newOrg);
            result.setData(newOrg.id);
        } catch (UserException e) {
            if (e.getCode() == UserException.USER_ALREADY_EXISTS) {
                result.setCode(HttpAdapter.SC_CONFLICT);
            } else {
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
            }
            result.setMessage(e.getMessage());
        } catch (NullPointerException e) {
            e.printStackTrace();
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            result.setMessage(e.getMessage());
        }
        return result;
    }

    public Object handleDeleteRequest(Event event, OrganizationAdapterIface organizationAdapter) {
        // TODO: check requester rights
        // only admin can do this and user status must be IS_UNREGISTERING
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        if(!isAdmin(request)){
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            return result;
        }
        String uid = request.pathExt;
        long id;
        if (uid == null) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            return result;
        } else {
            try {
                id = Long.parseLong(uid);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                return result;
            }
        }
        try {
            Organization tmpOrg = organizationAdapter.getOrganization(id);
            if(null==tmpOrg){
                result.setCode(HttpAdapter.SC_NOT_FOUND);
                return result;    
            }
            organizationAdapter.removeOrganization(tmpOrg);
            result.setCode(HttpAdapter.SC_OK);
            result.setData("" + id);
        } catch (UserException e) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
        }
        return result;
    }

    public Object handleUpdateRequest(Event event, OrganizationAdapterIface organizationAdapter) {
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        if(!isAdmin(request)){
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            return result;
        }
        String uid = request.pathExt;
        long id;
        if (uid == null || uid.contains("/")) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            return result;
        } else {
            try {
                id = Long.parseLong(uid);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                return result;
            }
        }
        try {
            Organization org = organizationAdapter.getOrganization(id);
            if (org == null) {
                result.setCode(HttpAdapter.SC_NOT_FOUND);
                result.setData("organization not found");
                return result;
            }
            String name = event.getRequestParameter("name");
            if (null != name && !name.isEmpty()) {
                org.name = name;
                organizationAdapter.modifyOrganization(org);
                result.setCode(HttpAdapter.SC_OK);
                result.setData(org);
            } else {
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
            }
        } catch (NullPointerException | UserException e) {
            e.printStackTrace();
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
        }
        return result;
    }

}
