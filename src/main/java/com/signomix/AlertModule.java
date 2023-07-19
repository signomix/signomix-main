/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix;

import org.cricketmsf.Event;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.StandardResult;
import com.signomix.out.iot.ThingsDataException;
import com.signomix.out.iot.ThingsDataIface;
import java.util.List;
import org.cricketmsf.Kernel;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class AlertModule {

    private static AlertModule service;

    public static AlertModule getInstance() {
        if (service == null) {
            service = new AlertModule();
        }
        return service;
    }

    public Object getAlertsCount(String userId, ThingsDataIface thingsAdapter) {
        StandardResult result = new StandardResult();
        if (userId != null) {
            try {
                Integer count = thingsAdapter.getAlertsCount(userId);
                result.setData(count);
            } catch (ThingsDataException ex) {
                Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), ex.getMessage()));
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                result.setMessage(ex.getMessage());
            }
        }
        return result;
    }

    public Object getAlerts(String userId, String limit, String offset, ThingsDataIface thingsAdapter) {
        StandardResult result = new StandardResult();
        Integer reqLimit = null;
        Integer reqOffset = null;
        try {
            if (limit != null) {
                reqLimit = Integer.parseInt(limit);
            }
            if (offset != null) {
                reqOffset = Integer.parseInt(offset);
            }
        } catch (Exception e) {
        }
        if (userId != null) {
            try {
                List l = thingsAdapter.getAlerts(userId, reqLimit, reqOffset);
                result.setData(l);
            } catch (ThingsDataException ex) {
                Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), ex.getMessage()));
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                result.setMessage(ex.getMessage());
            }
        }
        return result;
    }

    /**
     *
     */
    @Deprecated
    public Object getAlerts(Event event, ThingsDataIface thingsAdapter) {
        RequestObject request = event.getRequest();
        String userID = request.headers.getFirst("X-user-id");
        StandardResult result = new StandardResult();
        if (userID != null) {
            try {
                List l = thingsAdapter.getAlerts(userID, null, null);
                result.setData(l);
            } catch (ThingsDataException ex) {
                Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), ex.getMessage()));
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                result.setMessage(ex.getMessage());
            }
        }
        return result;
    }

    public Object removeAlert(Event event, ThingsDataIface thingsAdapter) {
        // TODO: access rights
        RequestObject request = event.getRequest();
        String userID = request.headers.getFirst("X-user-id");
        String alertId = request.pathExt;
        StandardResult result = new StandardResult();
        try {
            // TODO: authorization
            thingsAdapter.removeAlert(Long.parseLong(alertId));
            result.setData("OK");
        } catch (NumberFormatException | ThingsDataException ex) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), ex.getMessage()));
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            result.setMessage(ex.getMessage());
        }
        return result;
    }

    public Object removeAlert(String userId, String alertId, ThingsDataIface thingsAdapter) {
        // TODO: access rights
        StandardResult result = new StandardResult();
        try {
            // TODO: authorization
            thingsAdapter.removeAlert(Long.parseLong(alertId));
            result.setData("OK");
        } catch (NumberFormatException | ThingsDataException ex) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), ex.getMessage()));
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            result.setMessage(ex.getMessage());
        }
        return result;
    }

    public Object removeAll(String userId, ThingsDataIface thingsAdapter) {
        StandardResult result = new StandardResult();
        try {
            thingsAdapter.removeUserAlerts(userId);
            result.setData("OK");
        } catch (ThingsDataException ex) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), ex.getMessage()));
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            result.setMessage(ex.getMessage());
        }
        return result;
    }

    public void putAlert(Event event, ThingsDataIface thingsAdapter) {
        try {
            thingsAdapter.saveAlert(event);
        } catch (ThingsDataException ex) {
            Kernel.getInstance().dispatchEvent(Event.logSevere(this.getClass().getSimpleName(), ex.getMessage()));
        }
    }

}
