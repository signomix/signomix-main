/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix;

import com.signomix.out.db.ActuatorCommandsDBIface;
import com.signomix.out.db.IotDataStorageIface;
import com.signomix.out.db.IotDatabaseIface;
import com.signomix.out.gui.Dashboard;
import com.signomix.out.gui.DashboardAdapterIface;
import com.signomix.out.gui.DashboardException;
import com.signomix.out.gui.Widget;
import com.signomix.out.iot.ActuatorDataIface;
import com.signomix.out.iot.Channel;
import com.signomix.out.iot.Device;
import com.signomix.out.iot.DeviceTemplate;
import com.signomix.out.iot.ThingsDataException;
import org.cricketmsf.Event;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.StandardResult;
import com.signomix.out.iot.ThingsDataIface;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import org.cricketmsf.Kernel;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.scheduler.SchedulerIface;
import org.cricketmsf.microsite.out.auth.AuthAdapterIface;
import org.cricketmsf.microsite.out.auth.Token;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.out.user.UserException;
import org.cricketmsf.microsite.user.HashMaker;
import org.cricketmsf.microsite.user.User;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.KeyValueDBIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class PlatformAdministrationModule {
    
    public static final int ERR_PAYMENT_REQUIRED = 402;

    private static PlatformAdministrationModule module;
    private Invariants platformConfig = null;
    private String backupFolder = null;
    private boolean backupDaily = false;
    private final String ADMIN = "admin";

    private boolean hasAccessRights(String userID, List<String> roles) {
        if (userID == null || userID.isEmpty()) {
            return false;
        }
        return roles.contains(ADMIN);
    }

    /**
     * Returns the class instance
     *
     * @return PlatformAdministrationModule instance object
     */
    public static PlatformAdministrationModule getInstance() {
        if (module == null) {
            module = new PlatformAdministrationModule();
        }
        return module;
    }

    public Invariants getPlatformConfig() {
        return platformConfig;
    }

    public void readPlatformConfig(KeyValueDBIface database) {
        try {
            platformConfig = (Invariants) database.get("signomix", "platformlimits");
        } catch (ClassCastException | KeyValueDBException ex) {
            Kernel.handle(Event.logSevere(this.getClass().getSimpleName(), ex.getMessage()));
        }
    }

    /**
     * Process API requests related to platform administration
     *
     * @param event HTTP request encapsulated in Event object
     * @return Result object encapsulating HTTP response
     */
    public Object handleRestEvent(Event event) {
        RequestObject request = event.getRequest();
        String method = request.method;
        String moduleName = request.pathExt;
        StandardResult result = new StandardResult();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            result.setCode(HttpAdapter.SC_OK);
        }
        String userID = request.headers.getFirst("X-user-id");
        List<String> roles = request.headers.get("X-user-role");
        if (!hasAccessRights(userID, roles)) {
            result.setCode(HttpAdapter.SC_FORBIDDEN);
            return result;
        }
        if ("GET".equalsIgnoreCase(method)) {
            switch (moduleName.toLowerCase()) {
                case "status":
                    result = getServiceInfo();
                    break;
                case "config":
                    result.setData(platformConfig);
                    break;
                case "dbclean":
                    // we want to run database maintenance in separated thread, so we need to fire event
                    Kernel.getInstance().dispatchEvent(
                            new Event(
                                    this.getClass().getSimpleName(),
                                    Event.CATEGORY_GENERIC,
                                    "CLEAR_DATA",
                                    "+5s",
                                    event.getRequestParameter("category") + "," + event.getRequestParameter("type")
                            )
                    );
                    result.setCode(HttpAdapter.SC_ACCEPTED);
                    break;
                case "shutdown":
                    result.setCode(HttpAdapter.SC_ACCEPTED);
                    result.setData("the service will be stopped within few seconds");
                    Kernel.getInstance().handleEvent(
                            new Event(
                                    this.getClass().getSimpleName(),
                                    Event.CATEGORY_GENERIC,
                                    "SHUTDOWN",
                                    "+5s",
                                    ""
                            )
                    );
                    break;
                default:
                    result.setCode(HttpAdapter.SC_BAD_REQUEST);
            }
        } else {
            result.setCode(HttpAdapter.SC_METHOD_NOT_ALLOWED);
        }
        return result;
    }

    private StandardResult getServiceInfo() {
        StandardResult result = new StandardResult();
        result.setData(Kernel.getInstance().reportStatus());
        return result;
    }

    /**
     * Creates required database structure and default objects
     *
     * @param database
     * @param userDB
     * @param authDB
     * @param thingsDB
     * @param iotDataDB
     * @param actuatorCommandsDB
     */
    public void initDatabases(
            KeyValueDBIface database,
            KeyValueDBIface userDB,
            KeyValueDBIface authDB,
            IotDatabaseIface thingsDB,
            IotDataStorageIface iotDataDB,
            ActuatorCommandsDBIface actuatorCommandsDB
    ) {

        // SYSTEM key parameters and limits
        try {
            database.addTable("signomix", 5, true);
        } catch (KeyValueDBException e) {
            Kernel.handle(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
        }
        try {
            Invariants platformLimits = new Invariants();
            database.put("signomix", "platformlimits", platformLimits);
        } catch (KeyValueDBException e) {
            Kernel.handle(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
        }

        readPlatformConfig(database);
        backupFolder = (String) Kernel.getInstance().getProperties().get("backup-folder");
        try {
            backupDaily = Boolean.parseBoolean((String) Kernel.getInstance().getProperties().get("backup-daily"));
        } catch (ClassCastException e) {
        }
        if (backupFolder == null) {
            Kernel.handle(Event.logSevere(this, "Kernel parameter \"backup-folder\" not configured"));
        }
        if (!backupFolder.endsWith(System.getProperty("file.separator"))) {
            backupFolder = backupFolder.concat(System.getProperty("file.separator"));
        }
        // web moduleName CACHE
        /*try {
            database.addTable("webcache", (int) getPlatformConfig().get("webCacheSize"), false);
        } catch (ClassCastException | KeyValueDBException e) {
            Kernel.handle(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
        }*/
        // web moduleName CACHE
        try {
            database.addTable("webcache_pl", (int) getPlatformConfig().get("webCacheSize"), false);
        } catch (ClassCastException | KeyValueDBException e) {
            Kernel.getInstance().dispatchEvent(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
        }
        try {
            database.addTable("webcache_en", (int) getPlatformConfig().get("webCacheSize"), false);
        } catch (ClassCastException | KeyValueDBException e) {
            Kernel.getInstance().dispatchEvent(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
        }
        try {
            database.addTable("webcache_fr", (int) getPlatformConfig().get("webCacheSize"), false);
        } catch (ClassCastException | KeyValueDBException e) {
            Kernel.getInstance().dispatchEvent(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
        }
        try {
            database.addTable("webcache_it", (int) getPlatformConfig().get("webCacheSize"), false);
        } catch (ClassCastException | KeyValueDBException e) {
            Kernel.getInstance().dispatchEvent(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
        }

        // USERS DB
        try {
            userDB.addTable("users", (int) platformConfig.get("maxUsers"), true);
        } catch (KeyValueDBException e) {
            Kernel.handle(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
        }
        try {
            String initialAdminEmail = (String) Kernel.getInstance().getProperties().getOrDefault("initial-admin-email", "");
            String initialAdminPassword = (String) Kernel.getInstance().getProperties().getOrDefault("initial-admin-secret", "");
            if (initialAdminEmail.isEmpty() || initialAdminPassword.isEmpty()) {
                Kernel.handle(Event.logSevere(this.getClass().getSimpleName(), "initial-admin-email or initial-admin-secret properties not set. Stop the server now!"));
            }
            User newUser;
            //create admin account
            if (!userDB.containsKey("users", "admin")) {
                newUser = new User();
                newUser.setUid("admin");
                newUser.setEmail(initialAdminEmail);
                newUser.setType(User.OWNER);
                newUser.setRole("admin,redactor");
                newUser.setPassword(HashMaker.md5Java(initialAdminPassword));
                Random r = new Random(System.currentTimeMillis());
                newUser.setConfirmString(Base64.getEncoder().withoutPadding().encodeToString(("" + r.nextLong()).getBytes()));
                // no confirmation necessary for initial admin account
                newUser.setConfirmed(true);
                userDB.put("users", newUser.getUid(), newUser);
            }
            //create test account
            if (!userDB.containsKey("users", "tester1")) {
                newUser = new User();
                newUser.setUid("tester1");
                newUser.setEmail(initialAdminEmail);
                newUser.setType(User.FREE);
                newUser.setRole("user");
                newUser.setPassword(HashMaker.md5Java("signomix"));
                newUser.setConfirmString("6022140857");
                // no confirmation necessary for test account
                newUser.setConfirmed(true);
                userDB.put("users", newUser.getUid(), newUser);
            }
            //create user demo
            if (Kernel.getInstance().getName().toLowerCase(Locale.getDefault()).contains("demo")) {
                if (!userDB.containsKey("users", "demo")) {
                    newUser = new User();
                    newUser.setUid("demo");
                    newUser.setEmail(initialAdminEmail);
                    newUser.setType(User.DEMO);
                    newUser.setRole("user");
                    newUser.setPassword(HashMaker.md5Java("demo"));
                    newUser.setConfirmString("1234567890");
                    // no confirmation necessary for test account
                    newUser.setConfirmed(true);
                    userDB.put("users", newUser.getUid(), newUser);
                }
            }

            if (!userDB.containsKey("users", "public")) {
                //create user public
                newUser = new User();
                newUser.setUid("public");
                newUser.setEmail("");
                newUser.setType(User.READONLY);
                newUser.setRole("guest");
                newUser.setConfirmed(true);
                userDB.put("users", newUser.getUid(), newUser);
            }

        } catch (ClassCastException | KeyValueDBException e) {
            Kernel.handle(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // AUTH / IDM
        try {
            authDB.addTable("tokens", 2 * (int) platformConfig.get("maxUsers"), false);
        } catch (ClassCastException | KeyValueDBException e) {
            Kernel.handle(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
        }
        try {
            authDB.addTable("ptokens", (int) platformConfig.get("primaryDevicesLimit") * (int) platformConfig.get("maxUsers"), true); //permanent tokens used to share dashboard
        } catch (ClassCastException | KeyValueDBException e) {
            Kernel.handle(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
        }

        // IoT
        //TODO: configurable number of devices
        if (thingsDB != null) {
            try {
                thingsDB.addTable("devicetemplates", 100, true);
                thingsDB.addTable("devices", 2 * (int) platformConfig.get("primaryDevicesLimit") * (int) platformConfig.get("maxUsers"), true);
                thingsDB.addTable("dashboards", 3 * (int) platformConfig.get("primaryDevicesLimit") * (int) platformConfig.get("maxUsers"), true);
                thingsDB.addTable("alerts", 100 * (int) platformConfig.get("maxUsers"), true);
                iotDataDB.addTable("devicedata", 1000 * (int) platformConfig.get("primaryDevicesLimit") * (int) platformConfig.get("maxUsers"), true);
                iotDataDB.addTable("devicechannels", 2 * (int) platformConfig.get("primaryDevicesLimit") * (int) platformConfig.get("maxUsers"), true);
                //thingsDB.addTable("widgets", 1000, true);
                //templates
                LinkedHashMap<String, Channel> channels;
                Channel ch;

                DeviceTemplate template = new DeviceTemplate();
                template.setType(Device.TTN);
                template.setEUI("SGMTH01");
                template.setApplicationEUI("");
                channels = new LinkedHashMap<>();
                channels.put("temperature", new Channel("temperature"));
                channels.put("humidity", new Channel("humidity"));
                channels.put("battery", new Channel("battery"));
                template.setChannels(channels);
                template.setCommandScript("");
                template.setDescription("Example device build on Arduino Pro Mini and DTH11/DHT22 sensor.");
                template.setPattern("");
                template.setProducer("community");
                thingsDB.addDeviceTemplate(template); //demo device

                Device device = new Device();
                device.setType(Device.GENERIC);
                device.setName("emulator");
                device.setEUI("IOT-EMULATOR");
                //device.setUid(device.getEUI());
                device.setTeam("admin");
                device.setUserID("tester1");

                channels = new LinkedHashMap<>();
                channels.put("temperature", new Channel("temperature"));
                channels.put("humidity", new Channel("humidity"));
                channels.put("latitude", new Channel("latitude"));
                channels.put("longitude", new Channel("longitude"));
                device.setChannels(channels);

                Random r = new Random(System.currentTimeMillis());
                device.setKey("6022140857");
                //thingsDB.put("devices", device.getEUI(), device);
                thingsDB.putDevice(device);
                iotDataDB.updateDeviceChannels(device, null);

                Dashboard dashboard = new Dashboard();
                dashboard.setName("emulator");
                dashboard.setId("tester1~emulator");
                dashboard.setTitle("Data from iot-emulator");
                dashboard.setUserID("tester1");
                dashboard.setShared(false);
                dashboard.setTeam("admin");

                Widget widget = new Widget();
                widget.setName("e-temperature");
                widget.setDescription("Displays last collected temperature form iot-emulator device");
                widget.setTitle("Temperature (JSON)");
                widget.setType("raw");
                widget.setDev_id("IOT-EMULATOR");
                widget.setChannel("temperature");
                widget.setQuery("last");
                widget.setRange("");

                Widget widget2 = new Widget();
                widget2.setName("e-info");
                widget2.setDescription("The dashboard shows the most recent data registered by the emulator of IoT device.");
                widget2.setTitle("Info");
                widget2.setType("text");
                widget2.setDev_id("");
                widget2.setChannel("");
                widget2.setQuery("");
                widget2.setRange("");

                Widget widget3 = new Widget();
                widget3.setName("e-humidity");
                widget3.setDescription("Displays last collected humidity form iot-emulator device");
                widget3.setTitle("Humidity");
                widget3.setType("symbol");
                widget3.setDev_id("IOT-EMULATOR");
                widget3.setChannel("humidity");
                widget3.setUnitName("%");
                widget3.setQuery("last");
                widget3.setRange("<25");

                Widget widget5 = new Widget();
                widget5.setName("e-Temp");
                widget5.setDescription("Temperature");
                widget5.setTitle("Temperature");
                widget5.setType("symbol");
                widget5.setDev_id("IOT-EMULATOR");
                widget5.setChannel("temperature");
                widget5.setUnitName("&deg;C");
                widget5.setQuery("last");
                widget5.setRange("<-10>35:<0>25");

                Widget widget1 = new Widget();
                widget1.setName("e-humidity2");
                widget1.setDescription("Displays last collected humidity form iot-emulator device");
                widget1.setTitle("Humidity");
                widget1.setType("line");
                widget1.setDev_id("IOT-EMULATOR");
                widget1.setChannel("humidity");
                widget1.setUnitName("%");
                widget1.setQuery("last 10");
                widget1.setRange("");
                widget1.setWidth(4);

                dashboard.addWidget(widget2);
                dashboard.addWidget(widget3);
                dashboard.addWidget(widget5);
                dashboard.addWidget(widget);
                dashboard.addWidget(widget1);

                //thingsDB.put("dashboards", dashboard.getId(), dashboard);
                thingsDB.addDashboard(dashboard);
                //thingsDB.put("widgets", widget.getId(), widget);
            } catch (ClassCastException | KeyValueDBException | ThingsDataException e) {
                //e.printStackTrace();
                Kernel.handle(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // IoT actuator commands (storing events with actuator commands)
        if (actuatorCommandsDB != null) {
            try {
                actuatorCommandsDB.addTable("commands", (int) platformConfig.get("primaryDevicesLimit") * (int) platformConfig.get("maxUsers"), true);
            } catch (ClassCastException | KeyValueDBException e) {
                Kernel.handle(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
            }
            try {
                actuatorCommandsDB.addTable("commandslog", (int) platformConfig.get("primaryDevicesLimit") * (int) platformConfig.get("maxUsers") * 100, true);
            } catch (ClassCastException | KeyValueDBException e) {
                Kernel.handle(Event.logInfo(getClass().getSimpleName(), e.getMessage()));
            }
        }
    }

    /**
     * Creates events that should be fired on the Service start.
     */
    public void initScheduledTasks(SchedulerIface scheduler) {
        String initialTasks = scheduler.getProperty("init");
        String[] params;
        String[] tasks;
        if (initialTasks != null && !initialTasks.isEmpty()) {
            tasks = initialTasks.split(";");
            for (int i = 0; i < tasks.length; i++) {
                params = tasks[i].split(",");
                if (params.length == 6) {
                    scheduler.handleEvent(
                            new Event(params[1], params[2], params[3], params[4], params[5]).putName(params[0]), false, true);
                }
            }
        }
    }

    public void buildDefaultDashboard(String deviceId, ThingsDataIface thingsAdapter, DashboardAdapterIface dashboardAdapter, AuthAdapterIface authAdapter) {
        Device device = null;
        try {
            device = thingsAdapter.getDevice(deviceId);
        } catch (ThingsDataException ex) {
            Kernel.handle(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
        }
        if (device == null) {
            Kernel.handle(Event.logSevere(this.getClass().getSimpleName(), "device " + deviceId + " not found"));
            return;
        }

        Dashboard dashboard = new Dashboard(device.getUserID(), deviceId);
        Widget widget;
        Channel chnl;
        HashMap channels = device.getChannels();
        Iterator itr = channels.keySet().iterator();
        while (itr.hasNext()) {
            chnl = (Channel) channels.get(itr.next());
            widget = new Widget();
            widget.setName(chnl.getName());
            widget.setTitle(chnl.getName());
            widget.setType("symbol");
            widget.setDev_id(deviceId);
            widget.setChannel(chnl.getName());
            widget.setQuery("");
            widget.setRange("");
            widget.setDescription("");
            widget.setUnitName(guessChannelUnit(chnl.getName()));
            dashboard.addWidget(widget);
        }
        dashboard.setName(deviceId);
        dashboard.setId(device.getUserID() + "~" + deviceId);
        dashboard.setTitle(deviceId);
        dashboard.setUserID(device.getUserID());
        dashboard.setShared(false);
        dashboard.setTeam("");
        try {
            dashboardAdapter.addDashboard(device.getUserID(), dashboard, authAdapter);
        } catch (DashboardException ex) {
            Kernel.handle(Event.logSevere(this.getClass().getSimpleName(), ex.getMessage()));
        }
    }

    /**
     * Runs backup for all databases
     *
     * @param database
     * @param userDB
     * @param authDB
     * @param cmsDB
     * @param thingsDB
     * @param iotDataDB
     * @param actuatorCommandsDB
     */
    public void backupDatabases(
            KeyValueDBIface database,
            KeyValueDBIface userDB,
            KeyValueDBIface authDB,
            KeyValueDBIface cmsDB,
            IotDatabaseIface thingsDB,
            IotDataStorageIface iotDataDB,
            ActuatorCommandsDBIface actuatorCommandsDB
    ) {
        String prefix = backupDaily ? getDateString() : "";
        try {
            database.backup(backupFolder + prefix + database.getBackupFileName());
        } catch (KeyValueDBException ex) {
            Kernel.handle(Event.logSevere(this, "backup error - " + ex.getMessage()));
        }
        try {
            cmsDB.backup(backupFolder + prefix + cmsDB.getBackupFileName());
        } catch (KeyValueDBException ex) {
            Kernel.handle(Event.logSevere(this, "backup error - " + ex.getMessage()));
        }
        try {
            userDB.backup(backupFolder + prefix + userDB.getBackupFileName());
        } catch (KeyValueDBException ex) {
            Kernel.handle(Event.logSevere(this, "backup error - " + ex.getMessage()));
        }
        try {
            authDB.backup(backupFolder + prefix + authDB.getBackupFileName());
        } catch (KeyValueDBException ex) {
            Kernel.handle(Event.logSevere(this, "backup error - " + ex.getMessage()));
        }
        try {
            thingsDB.backup(backupFolder + prefix + thingsDB.getBackupFileName());
        } catch (KeyValueDBException ex) {
            Kernel.handle(Event.logSevere(this, "backup error - " + ex.getMessage()));
        }
        try {
            iotDataDB.backup(backupFolder + prefix + iotDataDB.getBackupFileName());
        } catch (KeyValueDBException ex) {
            Kernel.handle(Event.logSevere(this, "backup error - " + ex.getMessage()));
        }
        try {
            actuatorCommandsDB.backup(backupFolder + prefix + actuatorCommandsDB.getBackupFileName());
        } catch (KeyValueDBException ex) {
            Kernel.handle(Event.logSevere(this, "backup error - " + ex.getMessage()));
        }
        //TODO: scheduler
        //TODO: queue DB
        Kernel.handle(Event.logInfo(this, "database backup done"));
    }

    public void clearUserData(String userId) {
        Kernel.handle(Event.logWarning(this.getClass().getSimpleName(), "method clearUserData not implemented"));
        //devices, channels, dashboards, alerts
    }

    public void clearData(
            boolean demoMode,
            String dataCategory,
            String userType,
            UserAdapterIface userAdapter,
            ThingsDataIface thingsAdapter,
            AuthAdapterIface authAdapter,
            KeyValueDBIface database,
            DashboardAdapterIface dashboardAdapter,
            ActuatorDataIface actuatorAdapter) {

        //System.out.println("CLEARDATA:" + dataCategory + "," + userType);
        clearExpiredTokens(database);
        if(demoMode){
            //clearAllUsersData(userType, dataCategory, userAdapter, thingsAdapter, dashboardAdapter, actuatorAdapter);
        }
        clearNotConfirmed(userAdapter);
        //clearOldData(demoMode, userAdapter, thingsAdapter, actuatorAdapter);
        Kernel.getInstance().dispatchEvent(Event.logInfo(this, "Clearing data done."));
    }

    public void checkDevicesLimit(User user, int actualValue) throws PlatformException{
        int limit = 0;
        switch(user.getType()){
            case User.DEMO:
                limit = (int)getPlatformConfig().get("demoDevicesLimit");
                break;
            case User.FREE:
                limit = (int)getPlatformConfig().get("freeDevicesLimit");
                break;
            case User.USER:
                limit = (int)getPlatformConfig().get("standardDevicesLimit");
                break;
            case User.OWNER:
            case User.PRIMARY:
                limit = (int)getPlatformConfig().get("primaryDevicesLimit");
                break;
            case User.READONLY:
                limit = 0;
                break;
        }
        if(actualValue>=limit){
            throw new PlatformException(PlatformException.TOO_MANY_USER_DEVICES, "too many devices");
        }
    }
    
    private String guessChannelUnit(String channelName) {
        String unitName = "";
        switch (channelName.toUpperCase()) {
            case "TEMPERATURE":
                unitName = "&deg;C";
                break;
            case "HUMIDITY":
            case "MOISTURE":
                unitName = "%";
                break;
            case "PERCENTAGE":
            case "PERCENT":
            case "BATTERY":
            case "CHARGE":
                unitName = "%";
                break;
            case "VOLTAGE":
                unitName = "V";
                break;
            case "CURRENT":
                unitName = "A";
                break;
            case "SPEED":
                unitName = "m/s";
                break;
            case "PRESSURE":
                unitName = "Pa";
                break;
            case "ILLUMINANCE":
                unitName = "lx";
                break;
            case "MASS":
            case "WEIGHT":
                unitName = "kg";
                break;
            case "DISTANCE":
            case "LENGTH":
            case "WIDTH":
            case "HEIGTH":
                unitName = "m";
                break;
        }
        return unitName;
    }

    private String getDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-");
        return sdf.format(new Date());
    }

    /**
     * Clear all expired tokens and permanent tokens
     */
    private void clearExpiredTokens(KeyValueDBIface db) {
        Map tokens;
        Token t;
        Iterator it;
        try {
            tokens = db.getAll("tokens");
            it=tokens.keySet().iterator();
            while(it.hasNext()){
                t=(Token)tokens.get(it.next());
                if(!t.isValid()){
                    try{
                    db.remove("tokens", t.getToken());
                    }catch(KeyValueDBException e){
                        e.printStackTrace();
                    }
                }
            }
        } catch (KeyValueDBException ex) {
            ex.printStackTrace();
        }
        try {
            tokens = db.getAll("ptokens");
            it=tokens.keySet().iterator();
            while(it.hasNext()){
                t=(Token)tokens.get(it.next());
                if(!t.isValid()){
                    try{
                    db.remove("ptokens", t.getToken());
                    }catch(KeyValueDBException e){
                        e.printStackTrace();
                    }
                }
            }
        } catch (KeyValueDBException ex) {
            ex.printStackTrace();
        }
    }

    private void clearAllUsersData(String userType, String dataCategory, UserAdapterIface userAdapter, 
            ThingsDataIface thingsAdapter, DashboardAdapterIface dashboardAdapter, ActuatorDataIface actuatorAdapter) {
        //clear user data (cascade: alerts, devices, dashboards, users)
        int userTypeToRemove = -1;
        switch (userType.toUpperCase()) {
            case "USER":
                userTypeToRemove = User.USER;
                break;
            case "DEMO":
                userTypeToRemove = User.DEMO;
                break;
            case "READONLY":
                userTypeToRemove = User.READONLY;
                break;
            case "FREE":
                userTypeToRemove = User.FREE;
                break;
            case "APPLICATION":
                userTypeToRemove = User.APPLICATION;
                break;
            case "PRIMARY":
                userTypeToRemove = User.PRIMARY;
                break;
            default:
                userTypeToRemove = -1;
        }
        if (userTypeToRemove >= 0) {
            try {
                Map users = userAdapter.getAll();
                List<Device> devices;
                Iterator it = users.keySet().iterator();
                String uid;
                while (it.hasNext()) {
                    uid = (String) it.next();
                    if (userAdapter.get(uid).getType() == userTypeToRemove) {
                        if ("ALL".equalsIgnoreCase(dataCategory) || "ALERTS".equalsIgnoreCase(dataCategory)) {
                            thingsAdapter.removeUserAlerts(uid);
                        }
                        if ("ALL".equalsIgnoreCase(dataCategory) || "DASHBOARDS".equalsIgnoreCase(dataCategory)) {
                            dashboardAdapter.removeUserDashboards(uid);
                        }
                        devices = thingsAdapter.getUserDevices(uid,false);
                        if ("ALL".equalsIgnoreCase(dataCategory) || "CHANNELS".equalsIgnoreCase(dataCategory) || "DEVICES".equalsIgnoreCase(dataCategory)) {
                            for (int j = 0; j < devices.size(); j++) {
                                thingsAdapter.removeAllChannels(devices.get(j).getEUI());
                            }
                        }
                        if ("ALL".equalsIgnoreCase(dataCategory) || "DEVICES".equalsIgnoreCase(dataCategory)) {
                            thingsAdapter.removeAllDevices(uid);
                        }
                        if ("ALL".equalsIgnoreCase(dataCategory) || "COMMANDS".equalsIgnoreCase(dataCategory)) {
                            actuatorAdapter.removeAllCommands(uid);
                        }
                    }
                }
            } catch (UserException | ThingsDataException | DashboardException ex) {
                ex.printStackTrace();
            }
        }

    }

    private void clearNotConfirmed(UserAdapterIface userAdapter) {
        try {
            Map users = userAdapter.getAll();
            long TWO_DAYS = 48 * 3600 * 1000;
            Iterator it = users.keySet().iterator();
            String uid;
            while (it.hasNext()) {
                uid = (String) it.next();
                //remove users when registration is not confirmed after two days
                if (!((User) users.get(uid)).isConfirmed() && (System.currentTimeMillis() - ((User) users.get(uid)).getCreatedAt() > TWO_DAYS)) {
                    userAdapter.remove((String) uid);
                }
            }
        } catch (UserException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Removes user's channel data, alerts
     * @param demoMode
     * @param userAdapter
     * @param thingsAdapter
     * @param dashboardAdapter 
     */
    private void clearOldData(boolean demoMode, UserAdapterIface userAdapter, ThingsDataIface thingsAdapter, ActuatorDataIface actuatorAdapter) {
        // data retention
        // how long data is kept in signomix depends on user userType
        int ONE_DAY = 24 * 3600 * 1000;
        try {
            long freeRetention = ONE_DAY * (int) getPlatformConfig().get("freeDataRetention");
            long standardRetention = ONE_DAY * (int) getPlatformConfig().get("standardDataRetention");
            long primaryRetention = ONE_DAY * (int) getPlatformConfig().get("primaryDataRetention");
            Map users = userAdapter.getAll();
            List<Device> devices;
            Iterator it = users.keySet().iterator();
            String uid;
            long tooOldPoint = System.currentTimeMillis() - 2 * ONE_DAY;
            long tooOldPointFree = System.currentTimeMillis() - freeRetention;
            long tooOldPointStandard = System.currentTimeMillis() - standardRetention;
            long tooOldPointPrimary = System.currentTimeMillis() - primaryRetention;
            while (it.hasNext()) {
                uid = (String) it.next();
                if (!demoMode) {
                    switch(userAdapter.get(uid).getType()){
                        case User.OWNER:
                        case User.PRIMARY:
                            tooOldPoint = tooOldPointPrimary;
                            break;
                        case User.USER:
                            tooOldPoint = tooOldPointStandard;
                            break;
                        default:
                            tooOldPoint = tooOldPointFree;
                    }
                }
                thingsAdapter.removeUserAlerts(uid, tooOldPoint);
                devices = thingsAdapter.getUserDevices(uid,false);
                for (int j = 0; j < devices.size(); j++) {
                    thingsAdapter.clearAllChannels(devices.get(j).getEUI(), tooOldPoint);
                    try{
                        actuatorAdapter.clearAllCommands(devices.get(j).getEUI(), tooOldPoint);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        } catch (ClassCastException | UserException | ThingsDataException ex) {
            ex.printStackTrace();
        }
    }
}
