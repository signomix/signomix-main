/**
 * Copyright (C) Grzegorz Skorupa 2020.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.in.http;

import com.signomix.Service;
import com.signomix.event.MailingApiEvent;
import java.util.Arrays;
import org.cricketmsf.Adapter;
import java.util.HashMap;
import java.util.List;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.event.ProcedureCall;
import org.cricketmsf.in.http.HttpAdapterIface;
import org.cricketmsf.in.http.HttpPortedAdapter;
import org.cricketmsf.in.openapi.Operation;
import org.cricketmsf.in.openapi.Parameter;
import org.cricketmsf.in.openapi.ParameterLocation;
import org.cricketmsf.in.openapi.Response;
import org.cricketmsf.in.openapi.Schema;
import org.cricketmsf.in.openapi.SchemaType;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.out.user.UserException;
import org.cricketmsf.microsite.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST API for mailing system
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class MailingApi extends HttpPortedAdapter implements HttpAdapterIface, Adapter {

    private static final Logger logger = LoggerFactory.getLogger(MailingApi.class);
    private String[] authorizedUsers;

    /**
     * This method is executed while adapter is instantiated during the service
     * start. It's used to configure the adapter according to the configuration.
     *
     * @param properties  map of properties readed from the configuration file
     * @param adapterName name of the adapter set in the configuration file (can
     *                    be different from the interface and class name.
     */
    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
        super.getServiceHooks(adapterName);
        setContext(properties.get("context"));
        logger.info("context=" + getContext());
        setExtendedResponse(properties.getOrDefault("extended-response", "false"));
        logger.info("extended-response=" + isExtendedResponse());
        authorizedUsers = properties.getOrDefault("authorized", "").split(",");
        logger.info("\tauthorized=" + properties.getOrDefault("authorized", ""));
    }

    @Override
    protected ProcedureCall preprocess(RequestObject request, long l) {
        switch (request.method) {
            case "POST":
                return preprocessPost(request);
            case "DELETE":
                return preprocessDelete(request);
            case "OPTIONS":
                return ProcedureCall.respond(200, null);
            default:
                HashMap<String, Object> err = new HashMap<>();
                err.put("code", 405); // code<100 || code >1000
                err.put("message", String.format("method %1s not allowed", request.method));
                return ProcedureCall.respond(405, err);
        }
    }

    private ProcedureCall preprocessPost(RequestObject request) {
        String userId = request.headers.getFirst("X-user-id");
        if (null == userId || userId.isEmpty()) {
            HashMap<String, Object> err = new HashMap<>();
            err.put("code", 403); // code<100 || code >1000
            err.put("message", "not authenticated");
            return ProcedureCall.respond(400, err);
        }
        UserAdapterIface userAdapter = (UserAdapterIface) ((Service) Kernel.getInstance()).adaptersMap
                .get("userAdapter");
        boolean authorized = false;
        try {
            User sender = userAdapter.get(userId);
            if (null != sender) {
                if (null != sender && sender.hasRole("redactor")) {
                    if (null != sender && Arrays.asList(authorizedUsers).contains(sender.getUid())) {
                        authorized = true;
                    } else {
                        logger.warn("User is not authorized: "+userId);
                    }
                } else {
                    logger.warn("User is not redactor: "+userId);
                }
            }else{
                logger.warn("User not found: "+userId);
            }
        } catch (UserException ex) {
            ex.printStackTrace();
        }
        if (!authorized) {
            HashMap<String, Object> err = new HashMap<>();
            err.put("code", 400); // code<100 || code >1000
            err.put("message", "not authorized");
            return ProcedureCall.respond(400, err);
        }
        String documentId = (String) request.parameters.get("doc");
        String target = (String) request.parameters.get("target");

        if (null == documentId || documentId.isEmpty()) {
            HashMap<String, Object> err = new HashMap<>();
            err.put("code", 400); // code<100 || code >1000
            err.put("message", "doc parameter not found");
            return ProcedureCall.respond(400, err);
        }
        String[] tList = { "all", "users", "subscribers", "test" };
        List<String> validTargets = Arrays.asList(tList);
        if (null == target || !validTargets.contains(target)) {
            HashMap<String, Object> err = new HashMap<>();
            err.put("code", 400); // code<100 || code >1000
            err.put("message", "target parameter not found or not valid (all|users|subscribers|test)");
            return ProcedureCall.respond(400, err);
        }
        // mailing should be run in new thread (timePoint, 201 response code)
        return ProcedureCall.forward(new MailingApiEvent(documentId, target).timePoint("+1s"), "send", 201);
    }

    private ProcedureCall preprocessDelete(RequestObject request) {
        HashMap<String, Object> err = new HashMap<>();
        err.put("code", 400); // code<100 || code >1000
        err.put("message", "not implemented");
        return ProcedureCall.respond(400, err);
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
                                new Schema(SchemaType.string)))
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
                                new Schema(SchemaType.string)))
                .parameter(
                        new Parameter(
                                "alertId",
                                ParameterLocation.path,
                                true,
                                "alert ID to delete or * to delete all"))
                .pathModifier("/{alertId}")
                .response(new Response("200").content("text/plain").description("deletion successfull"))
                .response(new Response("400").description("invalid request parameters"))
                .response(new Response("401").description("session expired"))
                .response(new Response("403").description("not authorized"));
        addOperationConfig(op);
    }

}
