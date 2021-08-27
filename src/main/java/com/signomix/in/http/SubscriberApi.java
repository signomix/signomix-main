/*
 * Copyright 2020 Grzegorz Skorupa <g.skorupa at gmail.com>.
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
package com.signomix.in.http;

import com.signomix.event.SubscriptionEvent;
import org.cricketmsf.Adapter;
import java.util.HashMap;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.event.ProcedureCall;
import org.cricketmsf.in.http.HttpAdapterIface;
import org.cricketmsf.in.http.HttpPortedAdapter;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class SubscriberApi extends HttpPortedAdapter implements HttpAdapterIface, Adapter {

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
        Kernel.getInstance().getLogger().print("\tcontext=" + getContext());
    }

    @Override
    protected ProcedureCall preprocess(RequestObject request, long l) {
        switch (request.method) {
            case "POST":
                return preprocessPost(request);
            case "GET":
                return preprocessGet(request);
            case "OPTIONS":
                return ProcedureCall.respond(200, null);
            default:
                HashMap<String, Object> err = new HashMap<>();
                err.put("code", 405); //code<100 || code >1000
                err.put("message", String.format("method %1s not allowed", request.method));
                return ProcedureCall.respond(405, err);
        }
    }

    private ProcedureCall preprocessPost(RequestObject request) {

        String userName = (String) request.parameters.get("name");
        String subscriptionName = (String) request.parameters.get("subscription");
        String userEmail = (String) request.parameters.get("email");
        String preferredLanguage = (String) request.parameters.get("language");
        String confirmed = (String) request.parameters.get("confirmed");
        String redirectSubscribed = (String) request.parameters.getOrDefault("r", "/");
        if (null == subscriptionName || subscriptionName.isEmpty()
                || null == userEmail || userEmail.isEmpty()
                || null == preferredLanguage || preferredLanguage.isEmpty()) {
            HashMap<String, Object> err = new HashMap<>();
            err.put("code", 400); //code<100 || code >1000
            err.put("message", "wrong parameters");
            return ProcedureCall.respond(400, err);
        }
        //subscribe
        SubscriptionEvent event = new SubscriptionEvent(null, userName, userEmail, subscriptionName, preferredLanguage, true);
        Kernel.getInstance().getEventProcessingResult(
                event,
                "start"
        );
        return ProcedureCall.respond(302, redirectSubscribed);
    }

    private ProcedureCall preprocessGet(RequestObject request) {

        String userUid = (String) request.parameters.get("id");
        String redirectUnsubscribed = (String) request.parameters.getOrDefault("r", "/");
        if (null == userUid || userUid.isEmpty()) {
            HashMap<String, Object> err = new HashMap<>();
            err.put("code", 400); //code<100 || code >1000
            err.put("message", "wrong parameters");
            return ProcedureCall.respond(400, err);
        }
        //unsubscribe
        SubscriptionEvent event = new SubscriptionEvent(userUid, null, null, null, null, false);
        Kernel.getInstance().getEventProcessingResult(
                event,
                "end"
        );
        return ProcedureCall.respond(302, redirectUnsubscribed);
    }

}
