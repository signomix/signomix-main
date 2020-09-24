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
    demo: [true|false] //
    release: [ce|commercial|mini]
    
    webCacheSize: the cache size (maximum number of documents/files)
    
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
    superDataRetention:
    superDevicesLimit:
    superNotifications: 
    */

    public Invariants() {
        super();

        put("release", "{{type}}"); //this parameter is changed by the build script
        String releaseType = null;
        try {
            releaseType = (String) get("release");
        } catch (ClassCastException e) {
        }

        boolean demoVersion = Kernel.getInstance().getName().toLowerCase(Locale.getDefault()).contains("demo");
        put("demo", demoVersion);
        put("webCacheSize", 500);

        if ("mini".equalsIgnoreCase(releaseType) || demoVersion) {
            put("maxUsers", 10);
            put("maxDevices", 20); // no limit for total number of registered devices

            put("demoCollectionLimit", 144);
            put("demoCollectionLimitMonthly", 4320);
            put("demoDataRetention", 1); // days
            put("demoDevicesLimit", 1); // user devices
            put("demoNotifications", "SMTP");

            put("freeCollectionLimit", 144);
            put("freeCollectionLimitMonthly", 4320);
            put("freeDataRetention", 1); // days
            put("freeDevicesLimit", 1); // user devices
            put("freeNotifications", "SMTP");

            put("extendedCollectionLimit", 1144); // 6 transmission/hour * 7 days
            put("extendedCollectionLimitMonthly", 4320);
            put("extendedDataRetention", 1); // 1 day
            put("extendedDevicesLimit", 1); // user devices
            put("extendedNotifications", "SMTP");

            put("standardCollectionLimit", 144);
            put("standardCollectionLimitMonthly", 4320);
            put("standardDataRetention", 1); // days
            put("standardDevicesLimit", 1); // user devices
            put("standardNotifications", "SMTP");

            put("primaryCollectionLimit", 144);
            put("primaryCollectionLimitMonthly", 4320);
            put("primaryDataRetention", 1); // days
            put("primaryDevicesLimit", 1); // user devices
            put("primaryNotifications", "SMTP");
        } else if ("commercial".equalsIgnoreCase(releaseType)){
            put("maxUsers", 0);
            put("maxDevices", 0); // no limit for total number of registered devices

            put("demoCollectionLimit", 144);
            put("demoCollectionLimitMonthly", 4320);
            put("demoDataRetention", 1); // 1 day
            put("demoDevicesLimit", 1); // user devices
            put("demoNotifications", "SMTP");

            put("freeCollectionLimit", 288); // 6 transmission/hour * 7 days
            put("freeCollectionLimitMonthly", 8640);
            put("freeDataRetention", 7); // 1 day
            put("freeDevicesLimit", 5); // user devices
            put("freeNotifications", "SMTP,SLACK,PUSHOVER,TELEGRAM,WEBHOOK");

            put("extendedCollectionLimit", 288); // 6 transmission/hour * 7 days
            put("extendedCollectionLimitMonthly", 8640);
            put("extendedDataRetention", 30); // 1 day
            put("extendedDevicesLimit", 10); // user devices
            put("extendedNotifications", "SMTP,SLACK,PUSHOVER,TELEGRAM,WEBHOOK");

            put("standardCollectionLimit", 2160);
            put("standardCollectionLimitMonthly", 64800);
            put("standardDataRetention", 30); // days
            put("standardDevicesLimit", 15); // user devices
            put("standardNotifications", "SMTP,SLACK,PUSHOVER,TELEGRAM,WEBHOOK");

            put("primaryCollectionLimit", 7200);
            put("primaryCollectionLimitMonthly", 216000);
            put("primaryDataRetention", 30); 
            put("primaryDevicesLimit", 50); // user devices
            put("primaryNotifications", "SMTP,PUSHOVER,SMS,SLACK,TELEGRAM,WEBHOOK");

            put("superCollectionLimit", 7200);
            put("superCollectionLimitMonthly", 216000);
            put("superDataRetention", 365); 
            put("superDevicesLimit", 50); // user devices
            put("superNotifications", "SMTP,PUSHOVER,SMS,SLACK,TELEGRAM,WEBHOOK");
        } else { // CE type
            put("maxUsers", 0);
            put("maxDevices", 0); // no limit for total number of registered devices

            put("demoCollectionLimit", 144);
            put("demoCollectionLimitMonthly", 4320);
            put("demoDataRetention", 1); // 1 day
            put("demoDevicesLimit", 1); // user devices
            put("demoNotifications", "SMTP");

            put("freeCollectionLimit", 7200); // 6 transmission/hour * 30 days
            put("demoCollectionLimitMonthly", 216000);
            put("freeDataRetention", 30); // 1 day
            put("freeDevicesLimit", 50); // user devices
            put("freeNotifications", "SMTP,SLACK,PUSHOVER,SMS,TELEGRAM,WEBHOOK");

            put("extendedCollectionLimit", 7200); // 6 transmission/hour * 30 days
            put("extendedCollectionLimitMonthly", 216000);
            put("extendedDataRetention", 30); // days
            put("extendedDevicesLimit", 50); // user devices
            put("extendedNotifications", "SMTP,SLACK,PUSHOVER,SMS,TELEGRAM,WEBHOOK");

            put("standardCollectionLimit", 7200);
            put("standardCollectionLimitMonthly", 216000);
            put("standardDataRetention", 30); // days
            put("standardDevicesLimit", 50); // user devices
            put("standardNotifications", "SMTP,SLACK,PUSHOVER,SMS,TELEGRAM,WEBHOOK");

            put("primaryCollectionLimit", 7200);
            put("primaryCollectionLimitMonthly", 216000);
            put("primaryDataRetention", 30); 
            put("primaryDevicesLimit", 50); // user devices
            put("primaryNotifications", "SMTP,PUSHOVER,SMS,SLACK,TELEGRAM,WEBHOOK");

            put("superCollectionLimit", 7200);
            put("superCollectionLimitMonthly", 216000);
            put("superDataRetention", 365); 
            put("superDevicesLimit", 50); // user devices
            put("superNotifications", "SMTP,PUSHOVER,SMS,SLACK,TELEGRAM,WEBHOOK");
        }
    }

}
