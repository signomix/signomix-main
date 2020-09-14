/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.in.http;

import com.signomix.event.AlertApiEvent;
import org.cricketmsf.Adapter;
import java.util.HashMap;
import org.cricketmsf.RequestObject;
import org.cricketmsf.event.ProcedureCall;
import org.cricketmsf.in.http.HttpAdapterIface;
import org.cricketmsf.in.http.HttpPortedAdapter;
import org.cricketmsf.in.openapi.Operation;
import org.cricketmsf.in.openapi.Parameter;
import org.cricketmsf.in.openapi.ParameterLocation;
import org.cricketmsf.in.openapi.Response;
import org.cricketmsf.in.openapi.Schema;
import org.cricketmsf.in.openapi.SchemaFormat;
import org.cricketmsf.in.openapi.SchemaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST API for messaging system
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class AlertApi2 extends HttpPortedAdapter implements HttpAdapterIface, Adapter {

    private static final Logger logger = LoggerFactory.getLogger(AlertApi2.class);

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
        super.loadProperties(properties, adapterName);
        super.getServiceHooks(adapterName);
        setContext(properties.get("context"));
        logger.info("context=" + getContext());
        setExtendedResponse(properties.getOrDefault("extended-response", "false"));
        logger.info("extended-response=" + isExtendedResponse());
        setDateFormat(properties.get("date-format"));
        logger.info("date-format: " + getDateFormat());
    }

    @Override
    protected ProcedureCall preprocess(RequestObject request, long l) {
        switch (request.method) {
            case "GET":
                return preprocessGet(request);
            case "DELETE":
                return preprocessDelete(request);
            case "OPTIONS":
                return ProcedureCall.respond(200, null);
            default:
                HashMap<String, Object> err = new HashMap<>();
                err.put("code", 405); //code<100 || code >1000
                err.put("message", String.format("method %1s not allowed", request.method));
                return ProcedureCall.respond(405, err);
        }
    }

    private ProcedureCall preprocessGet(RequestObject request) {
        String userId = request.headers.getFirst("X-user-id");
        if (userId.isEmpty()) {
            HashMap<String, Object> err = new HashMap<>();
            err.put("code", 400); //code<100 || code >1000
            err.put("message", "user ID must be defined");
            return ProcedureCall.respond(400, err);
        }
        return ProcedureCall.forward(new AlertApiEvent("", userId), "get");
    }

    private ProcedureCall preprocessDelete(RequestObject request) {
        String alertId = request.pathExt;
        String userId = request.headers.getFirst("X-user-id");
        if (alertId.isEmpty()) {
            HashMap<String, Object> err = new HashMap<>();
            err.put("code", 400); //code<100 || code >1000
            err.put("message", "alert ID must be set");
            return ProcedureCall.respond(400, err);
        }
        if (userId.isEmpty()) {
            HashMap<String, Object> err = new HashMap<>();
            err.put("code", 400); //code<100 || code >1000
            err.put("message", "user ID must be defined");
            return ProcedureCall.respond(400, err);
        }
        return ProcedureCall.forward(new AlertApiEvent(alertId, userId), "delete");
    }

    /**
     * The method provides API documentation for this adapter.
     */
    @Override
    public void defineApi() {
        // GET request definition
        Operation op = new Operation("GET")
                .tag("notifications")
                .description("get usrer notifications")
                .summary("get notifications")
                .parameter(
                        new Parameter(
                                "Authentication",
                                ParameterLocation.header,
                                true,
                                "Session key",
                                new Schema(SchemaType.string)
                        )
                )
                .response(new Response("200").content("application/json").description("response"))
                .response(new Response("401").description("session expired"))
                .response(new Response("403").description("not authorized"));
        addOperationConfig(op);

        // DELETE request definition
        op = new Operation("DELETE")
                .tag("notifications")
                .description("removing selected or all user notifications")
                .summary("remove notifications")
                .parameter(
                        new Parameter(
                                "Authentication",
                                ParameterLocation.header,
                                true,
                                "Session key",
                                new Schema(SchemaType.string)
                        )
                )
                .parameter(
                        new Parameter(
                                "alertId",
                                ParameterLocation.path,
                                true,
                                "alert ID to delete or * to delete all")
                )
                .pathModifier("/{alertId}")
                .response(new Response("200").content("text/plain").description("deletion successfull"))
                .response(new Response("400").description("invalid request parameters"))
                .response(new Response("401").description("session expired"))
                .response(new Response("403").description("not authorized"));
        addOperationConfig(op);
    }

}
