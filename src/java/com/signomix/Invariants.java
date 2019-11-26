/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix;

import java.util.HashMap;
import java.util.Locale;
import org.cricketmsf.Kernel;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class Invariants extends HashMap {

    /* 
    Configuration parameters:
    demo: [true|false]
    release: [standard|mini]
    webCacheSize: rozmiar cache (lczba dokumentów/plików w cache)
    
    maxUsers:
    maxDevices:
    responseLimit:
    demoCollectionLimit:
    demoDataRetention:
    demoDevicesLimit:
    demoNotifications: 
    freeCollectionLimit:
    freeDataRetention:
    freeDevicesLimit:
    freeNotifications: 
    primaryCollectionLimit:
    primaryDataRetention:
    primaryDevicesLimit:
    primaryNotifications: 
    */

    public Invariants() {
        super();

        put("release", "standard"); //this parameter is changed by the build script
        String releaseType = null;
        try {
            releaseType = (String) get("release");
        } catch (ClassCastException e) {
        }

        boolean demoVersion = Kernel.getInstance().getName().toLowerCase(Locale.getDefault()).contains("demo");
        put("demo", demoVersion);
        put("webCacheSize", 500);

        if ("mini".equalsIgnoreCase(releaseType)) {
            put("maxUsers", 10);
            put("maxDevices", 20); // no limit for total number of registered devices

            put("demoCollectionLimit", 144);
            put("demoDataRetention", 1); // days
            put("demoDevicesLimit", 3); // user devices
            put("demoNotifications", "SMTP");

            put("freeCollectionLimit", 4320);
            put("freeDataRetention", 30); // days
            put("freeDevicesLimit", 20); // user devices
            put("freeNotifications", "SMTP,SLACK,PUSHOVER,TELEGRAM");

            put("standardCollectionLimit", 4320);
            put("standardDataRetention", 30); // days
            put("standardDevicesLimit", 20); // user devices
            put("standardNotifications", "SMTP,SLACK,PUSHOVER,TELEGRAM");

            put("primaryCollectionLimit", 4320);
            put("primaryDataRetention", 30); // days
            put("primaryDevicesLimit", 20); // user devices
            put("primaryNotifications", "SMTP,SLACK,PUSHOVER,TELEGRAM");
        } else if (demoVersion) {
            put("maxUsers", 10);
            put("maxDevices", 0); // no limit for total number of registered devices

            put("demoCollectionLimit", 144);
            put("demoDataRetention", 1); // 1 day
            put("demoDevicesLimit", 3); // user devices
            put("demoNotifications", "SMTP,SLACK,PUSHOVER,TELEGRAM");

            put("freeCollectionLimit", 144);
            put("freeDataRetention", 1); // 1 day
            put("freeDevicesLimit", 3); // user devices
            put("freeNotifications", "SMTP,SLACK,PUSHOVER,TELEGRAM");

            put("standardCollectionLimit", 144);
            put("standardDataRetention", 1); // days
            put("standardDevicesLimit", 3); // user devices
            put("standardNotifications", "SMTP,SLACK,PUSHOVER,TELEGRAM");

            put("primaryCollectionLimit", 144);
            put("primaryDataRetention", 1); // 1 day
            put("primaryDevicesLimit", 3); // user devices
            put("primaryNotifications", "SMTP,SLACK,PUSHOVER,TELEGRAM");
        } else {
            put("maxUsers", 1000);
            put("maxDevices", 0); // no limit for total number of registered devices

            put("demoCollectionLimit", 144);
            put("demoDataRetention", 1); // 1 day
            put("demoDevicesLimit", 1); // user devices
            put("demoNotifications", "SMTP,SLACK,PUSHOVER,TELEGRAM");

            put("freeCollectionLimit", 1008); // 6 transmission/hour * 7 days
            put("freeDataRetention", 7); // 1 day
            put("freeDevicesLimit", 5); // user devices
            put("freeNotifications", "SMTP,SLACK,PUSHOVER,TELEGRAM");

            put("standardCollectionLimit", 4320);
            put("standardDataRetention", 30); // days
            put("standardDevicesLimit", 15); // user devices
            put("standardNotifications", "SMTP,SLACK,PUSHOVER,TELEGRAM");

            put("primaryCollectionLimit", 4320);
            put("primaryDataRetention", 30); 
            put("primaryDevicesLimit", 50); // user devices
            put("primaryNotifications", "SMTP,PUSHOVER,SMS,SLACK,TELEGRAM");
        }
    }

}
