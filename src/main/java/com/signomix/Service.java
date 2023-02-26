/**
 * Copyright (C) Grzegorz Skorupa 2018.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.annotation.EventHook;
import org.cricketmsf.annotation.HttpAdapterHook;
import org.cricketmsf.annotation.PortEventClassHook;
import org.cricketmsf.event.EventMaster;
import org.cricketmsf.exception.EventException;
import org.cricketmsf.exception.InitException;
import org.cricketmsf.in.http.HtmlGenAdapterIface;
import org.cricketmsf.in.http.ParameterMapResult;
import org.cricketmsf.in.http.ResponseCode;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.in.openapi.OpenApiIface;
import org.cricketmsf.in.scheduler.SchedulerIface;
import org.cricketmsf.microsite.cms.CmsIface;
import org.cricketmsf.microsite.cms.Document;
import org.cricketmsf.microsite.in.http.ContentRequestProcessor;
import org.cricketmsf.microsite.out.auth.AuthAdapterIface;
import org.cricketmsf.microsite.out.queue.QueueAdapterIface;
import org.cricketmsf.microsite.out.queue.QueueException;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.out.user.UserException;
import org.cricketmsf.microsite.user.OrganizationAdapterIface;
import org.cricketmsf.microsite.user.User;
import org.cricketmsf.microsite.user.UserEvent;
import org.cricketmsf.out.db.KeyValueDBIface;
import org.cricketmsf.out.file.FileReaderAdapterIface;
import org.cricketmsf.out.log.LoggerAdapterIface;

import com.signomix.common.iot.generic.IotData;
import com.signomix.event.AlertApiEvent;
import com.signomix.event.IotEvent;
import com.signomix.event.MailingApiEvent;
import com.signomix.event.NewDataEvent;
import com.signomix.event.SubscriptionEvent;
import com.signomix.event.UplinkEvent;
import com.signomix.in.http.ActuatorApi;
import com.signomix.in.http.KpnApi;
import com.signomix.in.http.LoRaApi;
import com.signomix.in.http.TtnApi;
import com.signomix.out.auth.AuthLogic;
import com.signomix.out.db.ActuatorCommandsDBIface;
import com.signomix.out.db.IotDbDataIface;
import com.signomix.out.db.ShortenerDBIface;
import com.signomix.out.gui.DashboardAdapterIface;
import com.signomix.out.iot.ActuatorDataIface;
import com.signomix.out.iot.Alert;
import com.signomix.out.iot.ThingsDataException;
import com.signomix.out.iot.ThingsDataIface;
import com.signomix.out.iot.application.ApplicationAdapterIface;
import com.signomix.out.mailing.MailingIface;
import com.signomix.out.notification.MessageBrokerIface;
import com.signomix.out.notification.dto.MessageEnvelope;
import com.signomix.out.script.ScriptingAdapterIface;

/**
 * EchoService
 *
 * @author greg
 */
public class Service extends Kernel {

    public static String SIGNOMIX_TOKEN_NAME = "signomixToken";
    // service parameters
    Invariants invariants = null;

    //Business domain
    DeviceManagementLogicIface deviceLogic = null;

    // adapterClasses
    LoggerAdapterIface logAdapter = null;
    LoggerAdapterIface gdprLogger = null;
    public KeyValueDBIface database = null;
    SchedulerIface scheduler = null;
    HtmlGenAdapterIface htmlAdapter = null;
    FileReaderAdapterIface fileReader = null;

    // optional
    // we don't need to register input adapters:
    // UserApi, AuthApi and other input adapter if we not need to acces them
    // directly from the service
    // CM module
    KeyValueDBIface cmsDatabase = null;
    FileReaderAdapterIface cmsFileReader = null;
    CmsIface cms = null;
    // user module
    KeyValueDBIface userDB = null;
    UserAdapterIface userAdapter = null;
    // organization adapter
    OrganizationAdapterIface organizationAdapter = null;
    // application adapter
    ApplicationAdapterIface applicationAdapter = null;
    // auth module
    KeyValueDBIface authDB = null;
    AuthAdapterIface authAdapter = null;
    // event broker client
    QueueAdapterIface queueAdapter = null;
    KeyValueDBIface queueDB = null;
    // IoT
    ThingsDataIface thingsAdapter = null;
    DashboardAdapterIface dashboardAdapter = null;
    ActuatorApi actuatorApi = null;
    ActuatorDataIface actuatorAdapter = null;

    private IotDbDataIface iotDatabase = null;

    ScriptingAdapterIface scriptingAdapter = null;

    MessageBrokerIface messageBroker = null;
    MailingIface mailingAdapter = null;

    // Integration services
    LoRaApi loraUplinkService = null;
    TtnApi ttnIntegrationService = null;
    KpnApi kpnUplinkService = null;

    // Utils
    ShortenerDBIface shortenerDB = null;
    OpenApiIface apiGenerator = null;

    private static AtomicLong commandIdSeed = null;

    // port getters
    public MessageBrokerIface getMessageBroker(){
        return messageBroker;
    }


    
    /**
     * Returns next unique identifier for command (IotEvent).
     *
     * @return next unique identifier
     */
    public synchronized long getCommandId(String deviceEui) {
        // TODO: max value policy: 2,4,8 bytes (unsigned)
        short commandIdBytes = 0;
        try {
            commandIdBytes = Short.parseShort((String) getProperties().getOrDefault("command_id_size", "0"));
        } catch (Exception e) {
        }
        // default is long type (8 bytes)
        if(commandIdBytes==0){
            return getEventId();
        }
        if (null == commandIdSeed) {
            long seed = 0;
            try {
                if (null == deviceEui) {
                    seed = getActuatorCommandsDatabase().getMaxCommandId();
                } else {
                    seed = getActuatorCommandsDatabase().getMaxCommandId(deviceEui);
                }
            } catch (ThingsDataException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            switch (commandIdBytes) {
                case 2:
                    if (seed >= 65535L) {
                        seed = 0;
                    }
                    break;
                case 4:
                    if (seed >= 4294967295L) {
                        seed = 0;
                    }
                    break;
                default: //default per device ID is 8 bytes
                    if (seed == Long.MAX_VALUE) {
                        seed = 0;
                    }
            }
            commandIdSeed = new AtomicLong(seed);
        }
        long value = commandIdSeed.get();
        long newValue;
        switch (commandIdBytes) {
            case 2:
                if (value >= 65535L) {
                    newValue = 1;
                } else {
                    newValue = value + 1;
                }
                break;
            case 4:
                if (value >= 4294967295L) {
                    newValue = 1;
                } else {
                    newValue = value + 1;
                }
                break;
            default:
                if (value == Long.MAX_VALUE) {
                    newValue = 1;
                } else {
                    newValue = value + 1;
                }
        }
        commandIdSeed.set(newValue);
        return newValue;
    }

    public Service() {
        super();
    }

    public IotDbDataIface getIotDatabase() {
        return iotDatabase;
    }

    public ActuatorCommandsDBIface getActuatorDatabase() {
        return (ActuatorCommandsDBIface) iotDatabase;
    }

    public ActuatorCommandsDBIface getActuatorCommandsDatabase() {
        return (ActuatorCommandsDBIface) iotDatabase;
    }

    @Override
    public void getAdapters() {
        // standard Cricket adapters
        logAdapter = (LoggerAdapterIface) getRegistered("logger");
        gdprLogger = (LoggerAdapterIface) getRegistered("GdprLogger");
        database = (KeyValueDBIface) getRegistered("database");
        scheduler = (SchedulerIface) getRegistered("scheduler");
        htmlAdapter = (HtmlGenAdapterIface) getRegistered("WwwService");
        fileReader = (FileReaderAdapterIface) getRegistered("FileReader");
        // business domain
        deviceLogic = (DeviceManagementLogicIface) getRegistered("DeviceLogic");
        // cms
        cmsFileReader = (FileReaderAdapterIface) getRegistered("CmsFileReader");
        cmsDatabase = (KeyValueDBIface) getRegistered("cmsDB");
        cms = (CmsIface) getRegistered("cms");
        // user
        userAdapter = (UserAdapterIface) getRegistered("userAdapter");
        userDB = (KeyValueDBIface) getRegistered("userDB");
        // organization
        organizationAdapter = (OrganizationAdapterIface) getRegistered("organizationAdapter");
        // application
        applicationAdapter = (ApplicationAdapterIface) getRegistered("applicationAdapter");
        // auth
        authAdapter = (AuthAdapterIface) getRegistered("authAdapter");
        authDB = (KeyValueDBIface) getRegistered("authDB");
        // queue
        queueDB = (KeyValueDBIface) getRegistered("queueDB");
        queueAdapter = (QueueAdapterIface) getRegistered("queueAdapter");
        // IoT
        thingsAdapter = (ThingsDataIface) getRegistered("iotAdapter");
        dashboardAdapter = (DashboardAdapterIface) getRegistered("dashboardAdapter");
        actuatorApi = (ActuatorApi) getRegistered("ActuatorService");
        actuatorAdapter = (ActuatorDataIface) getRegistered("actuatorAdapter");
        scriptingAdapter = (ScriptingAdapterIface) getRegistered("scriptingAdapter");
        iotDatabase = (IotDbDataIface) getRegistered("IotDatabase");
        loraUplinkService = (LoRaApi) getRegistered("LoRaUplinkService");
        ttnIntegrationService = (TtnApi) getRegistered("TtnIntegrationService");
        kpnUplinkService = (KpnApi) getRegistered("KpnUplinkService");
        //
        messageBroker = (MessageBrokerIface) getRegistered("MessageBroker");
        mailingAdapter = (MailingIface) getRegistered("MailingService");
        shortenerDB = (ShortenerDBIface) getRegistered("ShortenerDB");
        apiGenerator = (OpenApiIface) getRegistered("OpenApi");
    }

    @Override
    public void runInitTasks() {
        try {
            super.runInitTasks();
            EventMaster.registerEventCategories(new Event().getCategories(), Event.class.getName());
            EventMaster.registerEventCategories(new UserEvent().getCategories(), UserEvent.class.getName());
            EventMaster.registerEventCategories(new IotEvent().getCategories(), IotEvent.class.getName());
            EventMaster.registerEventCategories(new Alert().getCategories(), Alert.class.getName());
        } catch (EventException | InitException ex) {
            ex.printStackTrace();
            shutdown();
        }
        // read the OS variable to get the service URL
        String urlEnvName = (String) getProperties().get("SRVC_URL_ENV_VARIABLE");
        if (null != urlEnvName) {
            try {
                String url = System.getenv(urlEnvName);
                if (null != url) {
                    getProperties().put("serviceurl", url);
                }
            } catch (Exception e) {
            }
        }
        invariants = new Invariants();
        PlatformAdministrationModule.getInstance().initDatabases(database, userDB, authDB,
                shortenerDB, getIotDatabase());
        // PlatformAdministrationModule.getInstance().readPlatformConfig(database);
        Kernel.getInstance().handleEvent(
                new Event(
                        this.getClass().getSimpleName(),
                        Event.CATEGORY_GENERIC,
                        "EMAIL_ADMIN_STARTUP",
                        "+30s",
                        "Signomix service has been started."));

        apiGenerator.init(this);
        setInitialized(true);
    }

    @Override
    public void runFinalTasks() {
        /*
         * // CLI adapter doesn't start automaticaly as other inbound adapters
         * if (cli != null) {
         * cli.start();
         * }
         */
    }

    /**
     * Executed when the Service is started in "not service" mode
     */
    @Override
    public void runOnce() {
        super.runOnce();
        handleEvent(Event.logInfo("Service.runOnce()", "executed"));
    }

    @Override
    public void shutdown() {
        String subject = "Signomix - shutdown";
        String text = "Signomix service is going down.";
        if (null != messageBroker) {
            MessageEnvelope message = new MessageEnvelope();
            message.message = text;
            message.subject = subject;
            message.type = MessageEnvelope.ADMIN_EMAIL;
            User user = new User();
            user.setEmail((String) getProperties().getOrDefault("admin-notification-email", ""));
            message.user = user;
            messageBroker.send(message);
        }
        super.shutdown();
    }

    /**
     * Event dispatcher method. Depending on the event category, Service and
     * QueueAdapte configurations dispatches event to Scheduler, QueAdapter or
     * Kernel handler method.
     *
     * @param event Event object to dispatch
     * @return
     */
    @Override
    public Object handleEvent(Event event) {
        if (queueAdapter != null && queueAdapter.isHandling(event.getCategory())) {
            try {
                queueAdapter.send(event);
            } catch (QueueException ex) {
                handleEvent(Event.logSevere(this.getClass().getSimpleName(), ex.getMessage()));
            }
            return null;
        }
        if (scheduler != null && event.getTimePoint() != null) {
            scheduler.handleEvent(event);
            return null;
        }
        return super.handleEvent(event);
    }

    @HttpAdapterHook(adapterName = "goto", requestMethod = "GET")
    public Object goToShortcut(Event event) {
        return UrlShortener.getInstance().processRequest(event, shortenerDB);
    }

    @HttpAdapterHook(adapterName = "goto", requestMethod = "POST")
    public Object updateShortcut(Event event) {
        return UrlShortener.getInstance().processRequest(event, shortenerDB);
    }

    /**
     * Process requests from simple web server implementation given by
     * HtmlGenAdapter access web web resources
     *
     * @param event
     * @return ParameterMapResult with the file content as a byte array
     */
    @HttpAdapterHook(adapterName = "WwwService", requestMethod = "GET")
    public Object wwwGet(Event event) {
        ArrayList<String> cookies = null;
        try {
            event.getRequest().headers.keySet().forEach(key -> {
                if (key.equalsIgnoreCase("Cookie")) {
                    event.getRequest().headers.get(key).forEach(value -> {
                        cookies.add(key);
                    });
                }

            });
            // cookies.forEach(value -> System.out.println("Cookie: " + value));
        } catch (Exception e) {
        }

        ParameterMapResult result = new ParameterMapResult();
        String userID = null;
        try {
            // TODO: to nie jest optymalne rozwiązanie
            Kernel.getInstance().dispatchEvent(Event.logWarning(this, "GET WWW: " + event.getRequest().pathExt));
            dispatchEvent(Event.logFinest(this.getClass().getSimpleName(), event.getRequest().uri));
            String language = event.getRequestParameter("language");
            if (language == null || language.isEmpty()) {
                language = "en";
            } else if (language.endsWith("/")) {
                language = language.substring(0, language.length() - 1);
            }

            try {
                String cacheName = "webcache_" + language;
                result = (ParameterMapResult) cms
                        .getFile(event.getRequest(), htmlAdapter.useCache() ? database : null, cacheName, language);

                if (ResponseCode.NOT_FOUND == result.getCode()) {
                    if (event.getRequest().pathExt.endsWith(".html")) {
                        // TODO: configurable index file params
                        // RequestObject request = processRequest(event.getRequest(), ".html",
                        // "index_pl.html");
                        RequestObject request = processRequest(event.getRequest(), ".html", "index.html");
                        result = (ParameterMapResult) fileReader
                                .getFile(request, htmlAdapter.useCache() ? database : null, "webcache_en");
                    }
                }

                if (ResponseCode.NOT_FOUND == result.getCode()) {
                    Kernel.getInstance()
                            .dispatchEvent(Event.logWarning(this, "404 WWW: " + event.getRequest().pathExt));
                    return result;
                } else if (ResponseCode.NOT_FOUND != result.getCode()
                        && ("".equals(event.getRequest().pathExt) || event.getRequest().pathExt.endsWith("/")
                                || event.getRequest().pathExt.endsWith(".html"))) {
                    // ((HashMap) result.getData()).put("serviceurl",
                    // getProperties().get("serviceurl"));
                    userID = event.getRequest().headers.getFirst("X-user-id");
                    HashMap rd = (HashMap) result.getData();
                    rd.put("serviceurl", getProperties().get("serviceurl"));
                    rd.put("defaultLanguage", getProperties().get("default-language"));
                    rd.put("gaTrackingID", getProperties().get("ga-tracking-id"));
                    rd.put("token", event.getRequestParameter("tid")); // fake tokens doesn't pass SecurityFilter
                    rd.put("shared", event.getRequestParameter("tid")); // niepusty tid może być permanentnym tokenem
                                                                        // ale może też być fałszywy
                    rd.put("user", userID);
                    rd.put("environmentName", getName());
                    rd.put("distroType", (String) invariants.get("release"));
                    rd.put("javaversion", System.getProperty("java.version"));
                    List<String> roles = event.getRequest().headers.get("X-user-role");
                    if (roles != null) {
                        StringBuilder sb = new StringBuilder("[");
                        for (int i = 0; i < roles.size(); i++) {
                            if (i > 0) {
                                sb.append(",");
                            }
                            sb.append("'").append(roles.get(i)).append("'");
                        }
                        sb.append("]");
                        rd.put("roles", sb.toString());
                    } else {
                        rd.put("roles", "[]");
                    }
                    // TODO: caching policy
                    result.setMaxAge(120);
                    if (null != userID && !userID.isEmpty()) {
                        result.setHeader("X-user-id", userID);
                    } else {
                        result.setHeader("X-user-id", "guest");
                    }
                    // if (null != cookies && cookies.get(0).indexOf(SIGNOMIX_TOKEN_NAME) >= 0) {
                    // result.setHeader("Cookie", cookies.get(0));
                    // }
                }
            } catch (Exception e) {
                Kernel.getInstance().dispatchEvent(Event.logWarning(this, "500 WWW: " + event.getRequest().pathExt));
                e.printStackTrace();
                result = new ParameterMapResult();
                result.setCode(ResponseCode.INTERNAL_SERVER_ERROR);
                return result;
            }
            if ("HEAD".equalsIgnoreCase(event.getRequest().method)) {
                byte[] empty = {};
                result.setPayload(empty);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setCode(ResponseCode.INTERNAL_SERVER_ERROR);
        }
        return result;
    }

    /**
     * Modify request pathExt basic on adapter configuration for CMS/Website
     * systems
     *
     * @param originalRequest
     * @param indexFileExt
     * @param indexFileName
     * @return
     */
    private RequestObject processRequest(RequestObject originalRequest, String indexFileExt, String indexFileName) {
        RequestObject request = originalRequest;
        String[] pathElements = request.uri.split("/");
        if (pathElements.length == 0) {
            return request;
        }
        StringBuilder sb = new StringBuilder();
        if (pathElements[pathElements.length - 1].endsWith(indexFileExt)) {
            if (!pathElements[pathElements.length - 1].equals(indexFileName)) {
                for (int i = 0; i < pathElements.length - 1; i++) {
                    sb.append(pathElements[i]).append("/");
                }
                request.pathExt = sb.toString();
            }
        }
        return request;
    }

    @PortEventClassHook(className = "MailingApiEvent", procedureName = "send")
    public Object handleMailingSend(MailingApiEvent event) {
        return mailingAdapter.sendMailing(event.getData().get("documentId"), event.getData().get("target"), userAdapter,
                cms, messageBroker);
    }

    @PortEventClassHook(className = "AlertApiEvent", procedureName = "get")
    public Object handleAlertGet(AlertApiEvent event) {
        return AlertModule.getInstance().getAlerts(event.userId, thingsAdapter);
    }

    @PortEventClassHook(className = "AlertApiEvent", procedureName = "delete")
    public Object handleAlertDelete(AlertApiEvent event) {
        if (!"*".equals(event.alertId)) {
            return AlertModule.getInstance().removeAlert(event.userId, event.alertId, thingsAdapter);
        } else {
            return AlertModule.getInstance().removeAll(event.userId, thingsAdapter);
        }
    }

    @HttpAdapterHook(adapterName = "DashboardService", requestMethod = "OPTIONS")
    public Object dashboardServiceOptions(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(ResponseCode.OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "DashboardService", requestMethod = "GET")
    public Object dashboardServiceGet(Event event) {
        try {
            return new DashboardBusinessLogic().getInstance().processEvent(event, dashboardAdapter, thingsAdapter,
                    authAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @HttpAdapterHook(adapterName = "DashboardService", requestMethod = "POST")
    public Object dashboardServicePost(Event event) {
        try {
            return new DashboardBusinessLogic().getInstance().processEvent(event, dashboardAdapter, thingsAdapter,
                    authAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @HttpAdapterHook(adapterName = "DashboardService", requestMethod = "PUT")
    public Object dashboardServicePut(Event event) {
        try {
            return new DashboardBusinessLogic().getInstance().processEvent(event, dashboardAdapter, thingsAdapter,
                    authAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @HttpAdapterHook(adapterName = "DashboardService", requestMethod = "DELETE")
    public Object dashboardServiceDelete(Event event) {
        try {
            return new DashboardBusinessLogic().getInstance().processEvent(event, dashboardAdapter, thingsAdapter,
                    authAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @HttpAdapterHook(adapterName = "DeviceService", requestMethod = "OPTIONS")
    public Object deviceServiceOptions(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(ResponseCode.OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "DeviceService", requestMethod = "GET")
    public Object deviceServiceGet(Event event) {
        try{
        StandardResult result = (StandardResult) deviceLogic.processDeviceEvent(event, thingsAdapter,
                userAdapter, organizationAdapter, PlatformAdministrationModule.getInstance());
        return result;
        }catch(Exception e){
            e.printStackTrace();
            return null;    
        }
    }

    @HttpAdapterHook(adapterName = "DeviceService", requestMethod = "POST")
    public Object deviceServicePost(Event event) {
        StandardResult result = (StandardResult) deviceLogic.processDeviceEvent(event, thingsAdapter,
                userAdapter, organizationAdapter, PlatformAdministrationModule.getInstance());
        return result;
    }

    @HttpAdapterHook(adapterName = "DeviceService", requestMethod = "PUT")
    public Object deviceServicePut(Event event) {
        StandardResult result = (StandardResult) deviceLogic.processDeviceEvent(event, thingsAdapter,
                userAdapter, organizationAdapter, PlatformAdministrationModule.getInstance());
        return result;
    }

    @HttpAdapterHook(adapterName = "DeviceService", requestMethod = "DELETE")
    public Object deviceServiceDelete(Event event) {
        StandardResult result = (StandardResult) deviceLogic.processDeviceEvent(event, thingsAdapter,
                userAdapter, organizationAdapter, PlatformAdministrationModule.getInstance());
        return result;
    }

    @HttpAdapterHook(adapterName = "GroupPublicationService", requestMethod = "OPTIONS")
    public Object groupPublicationServiceOptions(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(ResponseCode.OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "GroupPublicationService", requestMethod = "GET")
    public Object groupPublicationServiceGet(Event event) {
        return deviceLogic.processGroupPublicationEvent(event, thingsAdapter, userAdapter,
        organizationAdapter, PlatformAdministrationModule.getInstance());
    }

    @HttpAdapterHook(adapterName = "GroupService", requestMethod = "OPTIONS")
    public Object groupServiceOptions(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(ResponseCode.OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "GroupService", requestMethod = "GET")
    public Object groupServiceGet(Event event) {
        return deviceLogic.processGroupEvent(event, thingsAdapter, userAdapter,
                organizationAdapter, PlatformAdministrationModule.getInstance());
    }

    @HttpAdapterHook(adapterName = "GroupService", requestMethod = "POST")
    public Object groupServicePost(Event event) {
        return deviceLogic.processGroupEvent(event, thingsAdapter, userAdapter,organizationAdapter, 
                PlatformAdministrationModule.getInstance());
    }

    @HttpAdapterHook(adapterName = "GroupService", requestMethod = "PUT")
    public Object groupServicePut(Event event) {
        return deviceLogic.processGroupEvent(event, thingsAdapter, userAdapter,organizationAdapter, 
                PlatformAdministrationModule.getInstance());
    }

    @HttpAdapterHook(adapterName = "GroupService", requestMethod = "DELETE")
    public Object groupServiceDelete(Event event) {
        return deviceLogic.processGroupEvent(event, thingsAdapter, userAdapter,organizationAdapter, 
                PlatformAdministrationModule.getInstance());
    }

    @HttpAdapterHook(adapterName = "TemplateService", requestMethod = "OPTIONS")
    public Object templateServiceCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(ResponseCode.OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "TemplateService", requestMethod = "*")
    public Object templateServiceHandle(Event event) {
        return deviceLogic.processTemplateEvent(event, thingsAdapter, userAdapter,organizationAdapter, 
                PlatformAdministrationModule.getInstance());
    }

    @HttpAdapterHook(adapterName = "TtnIntegrationService", requestMethod = "OPTIONS")
    public Object ttnDataCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(ResponseCode.OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "TtnIntegrationService", requestMethod = "*")
    public Object ttnDataAdd(Event event) {
        try {
            return DeviceIntegrationModule.getInstance().processTtnRequest(event, thingsAdapter, userAdapter,
                    scriptingAdapter, ttnIntegrationService);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @HttpAdapterHook(adapterName = "Ttn3IntegrationService", requestMethod = "OPTIONS")
    public Object ttn3DataCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(ResponseCode.OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "Ttn3IntegrationService", requestMethod = "*")
    public Object ttn3DataAdd(Event event) {
        try {
            return DeviceIntegrationModule.getInstance().processTtn3Request(event, thingsAdapter, userAdapter,
                    scriptingAdapter, ttnIntegrationService);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @PortEventClassHook(className = "UplinkEvent", procedureName = "processData")
    public Object handleChirpstackUplink(UplinkEvent requestEvent) {
        IotData data = (IotData) requestEvent.getOriginalEvent().getPayload();
        String info = "RECEIVED: application=%1$s, devEUI=%2$s, data=%3$s";
        try {
            return DeviceIntegrationModule.getInstance().processChirpstackRequest(data, thingsAdapter, userAdapter,
                    scriptingAdapter, ttnIntegrationService);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @PortEventClassHook(className = "NewDataEvent", procedureName = "processData")
    public Object handleIotData(NewDataEvent requestEvent) {
        IotData data = (IotData) requestEvent.getOriginalEvent().getPayload();
        try {
            return DeviceIntegrationModule.getInstance().processGenericRequest(data, thingsAdapter, userAdapter,
                    scriptingAdapter, ttnIntegrationService, getActuatorDatabase());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @HttpAdapterHook(adapterName = "ActuatorService", requestMethod = "OPTIONS")
    public Object actuatorCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(ResponseCode.OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "ActuatorService", requestMethod = "*")
    public Object actuatorHandle(Event event) {
        return ActuatorModule.getInstance().processRequest(event, actuatorApi, thingsAdapter,
                (ActuatorCommandsDBIface) getActuatorCommandsDatabase(), scriptingAdapter);
    }

    @HttpAdapterHook(adapterName = "LoRaUplinkService", requestMethod = "*")
    public Object LoRaUplinkHandle(Event event) {
        return DeviceIntegrationModule.getInstance().processLoRaRequest(event, thingsAdapter, userAdapter,
                scriptingAdapter, loraUplinkService);
    }

    @HttpAdapterHook(adapterName = "LoRaJoinService", requestMethod = "*")
    public Object LoRaJoinHandle(Event event) {
        return LoRaBusinessLogic.getInstance().processLoRaRequest(event);
    }

    @HttpAdapterHook(adapterName = "LoRaAckService", requestMethod = "*")
    public Object LoRaAckHandle(Event event) {
        return LoRaBusinessLogic.getInstance().processLoRaRequest(event);
    }

    @HttpAdapterHook(adapterName = "LoRaErrorService", requestMethod = "*")
    public Object LoRaErrorHandle(Event event) {
        return LoRaBusinessLogic.getInstance().processLoRaRequest(event);
    }

    @HttpAdapterHook(adapterName = "KpnUplinkService", requestMethod = "*")
    public Object KpnUplinkHandle(Event event) {
        return DeviceIntegrationModule.getInstance().processKpnRequest(event, thingsAdapter, userAdapter,
                scriptingAdapter, kpnUplinkService);
    }

    @HttpAdapterHook(adapterName = "RecoveryService", requestMethod = "OPTIONS")
    public Object recoveryCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(ResponseCode.OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "RecoveryService", requestMethod = "POST")
    public Object recoveryHandle(Event event) {
        String resetPassEmail = event.getRequestParameter("resetpass");
        String userName = event.getRequestParameter("name");
        return CustomerModule.getInstance().handleResetRequest(event, userName, resetPassEmail, userAdapter,
                authAdapter);
    }

    @HttpAdapterHook(adapterName = "UserService", requestMethod = "OPTIONS")
    public Object userCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(ResponseCode.OK);
        return result;
    }

    /**
     * Return user data
     *
     * @param event
     * @return
     */
    @HttpAdapterHook(adapterName = "UserService", requestMethod = "GET")
    public Object userGet(Event event) {
        return UserModule.getInstance().handleGetRequest(event, userAdapter);
    }

    @HttpAdapterHook(adapterName = "UserService", requestMethod = "POST")
    public Object userAdd(Event event) {
        try{
        boolean withConfirmation = "true"
                .equalsIgnoreCase((String) getProperties().getOrDefault("user-confirm", "false"));
        return UserModule.getInstance().handleRegisterRequest(event, userAdapter, withConfirmation, authAdapter);
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Modify user data or sends password reset link
     *
     * @param event
     * @return
     */
    @HttpAdapterHook(adapterName = "UserService", requestMethod = "PUT")
    public Object userUpdate(Event event) {
        String resetPassEmail = event.getRequestParameter("resetpass");
        try {
            if (resetPassEmail == null || resetPassEmail.isEmpty()) {
                return UserModule.getInstance().handleUpdateRequest(event, userAdapter, authAdapter);
            } else {
                String userName = event.getRequestParameter("name");
                return CustomerModule.getInstance().handleResetRequest(event, userName, resetPassEmail, userAdapter,
                        authAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Set user as waiting for removal
     *
     * @param event
     * @return
     */
    @HttpAdapterHook(adapterName = "UserService", requestMethod = "DELETE")
    public Object userDelete(Event event) {
        boolean withConfirmation = "true"
                .equalsIgnoreCase((String) getProperties().getOrDefault("user-confirm", "false"));
        return UserModule.getInstance().handleDeleteRequest(event, userAdapter, withConfirmation);
    }

    @HttpAdapterHook(adapterName = "OrganizationService", requestMethod = "OPTIONS")
    public Object organizationCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(ResponseCode.OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "OrganizationService", requestMethod = "GET")
    public Object organizationGet(Event event) {
        return OrganizationModule.getInstance().handleGetRequest(event, organizationAdapter);
    }

    @HttpAdapterHook(adapterName = "OrganizationService", requestMethod = "POST")
    public Object organizationAdd(Event event) {
        try {
            return OrganizationModule.getInstance().handleCreateRequest(event, organizationAdapter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @HttpAdapterHook(adapterName = "OrganizationService", requestMethod = "PUT")
    public Object organizationUpdate(Event event) {
        try {
            return OrganizationModule.getInstance().handleUpdateRequest(event, organizationAdapter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @HttpAdapterHook(adapterName = "OrganizationService", requestMethod = "DELETE")
    public Object organizationDelete(Event event) {
        return OrganizationModule.getInstance().handleDeleteRequest(event, organizationAdapter);
    }

    @HttpAdapterHook(adapterName = "ApplicationService", requestMethod = "OPTIONS")
    public Object applicationCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(ResponseCode.OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "ApplicationService", requestMethod = "GET")
    public Object applicationGet(Event event) {
        return ApplicationModule.getInstance().handleGetRequest(event, applicationAdapter);
    }

    @HttpAdapterHook(adapterName = "ApplicationService", requestMethod = "POST")
    public Object applicationAdd(Event event) {
        try {
            return ApplicationModule.getInstance().handleAddApplication(event, applicationAdapter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @HttpAdapterHook(adapterName = "ApplicationService", requestMethod = "PUT")
    public Object applicationUpdate(Event event) {
        try {
            return ApplicationModule.getInstance().handleUpdateRequest(event, applicationAdapter);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @HttpAdapterHook(adapterName = "ApplicationService", requestMethod = "DELETE")
    public Object applicationDelete(Event event) {
        return ApplicationModule.getInstance().handleDeleteRequest(event, applicationAdapter);
    }

    @HttpAdapterHook(adapterName = "AuthService", requestMethod = "OPTIONS")
    public Object authCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(ResponseCode.OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "AuthService", requestMethod = "POST")
    public Object authLogin(Event event) {
        System.out.println("LOGIN");
        StandardResult result = (StandardResult) AuthLogic.getInstance().login(event, authAdapter);
        if (result.getCode() == ResponseCode.OK) {
            Kernel.getInstance().dispatchEvent(new IotEvent(IotEvent.PLATFORM_MONITORING, "login"));
        } else {
            Kernel.getInstance().dispatchEvent(new IotEvent(IotEvent.PLATFORM_MONITORING, "login_error"));
        }
        return result;
    }

    @HttpAdapterHook(adapterName = "AuthService", requestMethod = "DELETE")
    public Object authLogout(Event event) {
        return AuthLogic.getInstance().logout(event, authAdapter);
    }

    @HttpAdapterHook(adapterName = "AuthService", requestMethod = "GET")
    public Object authCheck(Event event) {
        Kernel.getInstance().dispatchEvent(Event.logInfo(this.getClass().getSimpleName(), "CHECK TOKEN"));
        return AuthLogic.getInstance().check(event, authAdapter);
    }

    @HttpAdapterHook(adapterName = "AuthService", requestMethod = "PUT")
    public Object authRefresh(Event event) {
        return AuthLogic.getInstance().refreshToken(event, authAdapter);
    }

    @HttpAdapterHook(adapterName = "ConfirmationService", requestMethod = "GET")
    public Object userConfirm(Event event) {
        StandardResult result = new StandardResult();
        result.setCode(ResponseCode.FORBIDDEN);
        try {
            String key = event.getRequestParameter("key");
            try {
                if (authAdapter.checkToken(key)) {
                    User user = authAdapter.getUser(key);
                    if (user.getStatus() == User.IS_REGISTERING && user.getConfirmString().equals(key)) {
                        user.setConfirmed(true);
                        userAdapter.modify(user);
                        result.setCode(200);
                        // TODO: build default html page or redirect
                        String pageContent = "Registration confirmed.<br>You can go to <a href=/app/#!login>login page</a> and sign in.";
                        result.setFileExtension("html");
                        result.setHeader("Content-type", "text/html");
                        result.setPayload(pageContent.getBytes());
                    }
                } else {
                    result.setCode(401);
                    String pageContent = "Oops, something has gone wrong: confirmation token not found . We cannot confirm your <a href=/>Signomix</a> registration. Please contact support.";
                    result.setFileExtension("html");
                    result.setHeader("Content-type", "text/html");
                    result.setPayload(pageContent.getBytes());
                }
            } catch (UserException ex) {
                Kernel.getInstance().dispatchEvent(
                        Event.logWarning(this.getClass().getSimpleName(), "confirmation error " + ex.getMessage()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @PortEventClassHook(className = "SubscriptionEvent", procedureName = "start")
    public Object handleSubscriptionStart(SubscriptionEvent event) {
        return UserModule.getInstance().handleSubscribeRequest(event, userAdapter, false);
    }

    @PortEventClassHook(className = "SubscriptionEvent", procedureName = "end")
    public Object handleSubscriptionEnd(SubscriptionEvent event) {
        return UserModule.getInstance().handleUnsubscribeRequest(event, userAdapter, false);
    }

    @HttpAdapterHook(adapterName = "SubscriptionConfirmationService", requestMethod = "GET")
    public Object subscriptionConfirm(Event event) {
        StandardResult result = new StandardResult();
        result.setCode(ResponseCode.FORBIDDEN);
        try {
            String key = event.getRequestParameter("key");
            try {
                if (authAdapter.checkToken(key)) {
                    User user = authAdapter.getUser(key);
                    if (user.getStatus() == User.IS_REGISTERING && user.getConfirmString().equals(key)) {
                        user.setConfirmed(true);
                        userAdapter.modify(user);
                        result.setCode(200);
                        // TODO: build default html page or redirect
                        String pageContent = "Subscription confirmed.<br>Thank you for using <a href='/'>Signomix</a>.";
                        result.setFileExtension("html");
                        result.setHeader("Content-type", "text/html");
                        result.setPayload(pageContent.getBytes());
                    }
                } else {
                    result.setCode(401);
                    String pageContent = "Oops, something has gone wrong: confirmation token not found . Your subscription cannot be confirmed.";
                    result.setFileExtension("html");
                    result.setHeader("Content-type", "text/html");
                    result.setPayload(pageContent.getBytes());
                }
            } catch (UserException ex) {
                Kernel.getInstance().dispatchEvent(
                        Event.logWarning(this.getClass().getSimpleName(), "confirmation error " + ex.getMessage()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    @HttpAdapterHook(adapterName = "ContentService", requestMethod = "OPTIONS")
    public Object contentCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(ResponseCode.OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "ContentService", requestMethod = "GET")
    public Object contentGetPublished(Event event) {
        StandardResult result = null;
        try {
            result = (StandardResult) new ContentRequestProcessor().processGetPublished(event, cms);
            if (ResponseCode.NOT_FOUND == result.getCode()) {
                String path = event.getRequest().pathExt;
                if (null != path) {
                    String filePath = fileReader.getRootPath() + path;
                    Document doc = new Document();
                    doc.setTitle("");
                    doc.setSummary("");
                    doc.setAuthor("");
                    doc.setContent(new String(fileReader.readFile(filePath)));
                    result.setData(doc);
                    result.setCode(ResponseCode.OK);
                }
                Kernel.getInstance().dispatchEvent(Event.logWarning(this, "missing in CMS so read form disk " + path));
            }
        } catch (Exception e) {
            result = new StandardResult();
            result.setCode(ResponseCode.NOT_FOUND);
        }
        return result;
    }

    @HttpAdapterHook(adapterName = "ContentManager", requestMethod = "OPTIONS")
    public Object contentServiceCors(Event requestEvent) {
        StandardResult result = new StandardResult();
        result.setCode(ResponseCode.OK);
        return result;
    }

    @HttpAdapterHook(adapterName = "ContentManager", requestMethod = "*")
    public Object contentServiceHandle(Event event) {
        return new ContentRequestProcessor().processRequest(event, cms, null);
    }

    @HttpAdapterHook(adapterName = "SystemService", requestMethod = "*")
    public Object systemServiceHandle(Event event) {
        return new PlatformAdministrationModule().handleRestEvent(event);
    }

    @EventHook(eventCategory = Event.CATEGORY_LOG)
    public void logEvent(Event event) {
        try {
            logAdapter.log(event);
            /* if (event.getType().equals(Event.LOG_SEVERE)) {
                String subject = "Signomix - error";
                String text = event.toString();
                if (null != messageBroker) {
                    MessageEnvelope message = new MessageEnvelope();
                    message.message = text;
                    message.subject = subject;
                    message.type = MessageEnvelope.ADMIN_EMAIL;
                    User user = new User();
                    user.setEmail((String) getProperties().getOrDefault("admin-notification-email", ""));
                    message.user = user;
                    messageBroker.send(message);
                }
            } */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHook(eventCategory = Event.CATEGORY_HTTP_LOG)
    public void logHttpEvent(Event event) {
        try {
            logAdapter.log(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHook(eventCategory = UserEvent.CATEGORY_USER)
    public void processUserEvent(Event event) {
        try {
            if (event.getTimePoint() != null) {
                scheduler.handleEvent(event);
                return;
            }
            UserEventHandler.handleEvent(
                    this,
                    event,
                    userAdapter,
                    gdprLogger,
                    authAdapter,
                    thingsAdapter,
                    dashboardAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @EventHook(eventCategory = IotEvent.CATEGORY_IOT)
    public void processIotEvent(Event event) {
        try {
            IotEventHandler.handleEvent(
                    this,
                    event,
                    scheduler,
                    userAdapter,
                    thingsAdapter,
                    dashboardAdapter,
                    authAdapter,
                    scriptingAdapter,
                    messageBroker,
                    getIotDatabase());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles system events
     *
     * @param event event object to process
     */
    @EventHook(eventCategory = Event.CATEGORY_GENERIC)
    public void processSystemEvent(Event event) {
        try {
            SystemEventHandler.handleEvent(
                    this,
                    event,
                    database,
                    cmsDatabase,
                    userAdapter,
                    userDB,
                    authAdapter,
                    authDB,
                    actuatorAdapter,
                    thingsAdapter,
                    dashboardAdapter,
                    scriptingAdapter,
                    messageBroker,
                    getIotDatabase());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles all event categories not processed by other handler methods
     *
     * @param event event object to process
     */
    @EventHook(eventCategory = "*")
    public void processEvent(Event event) {
        try {
            if (event.getTimePoint() != null) {
                scheduler.handleEvent(event);
            } else {
                handleEvent(Event.logWarning("Don't know how to handle category " + event.getCategory(),
                        event.getPayload().toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
