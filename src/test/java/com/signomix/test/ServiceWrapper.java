package com.signomix.test;

import org.cricketmsf.Kernel;
import org.cricketmsf.Runner;

/**
 *
 * @author greg
 */
public class ServiceWrapper {

    private static Kernel service;

    public static void setup() {
        System.out.println("@setup");
        String[] args = {"-r", "-s", "SignomixService", "-c", "/home/greg/workspace/signomix/src/other/environment/dev/cricket.json"};
        service = Runner.getService(args);
        while (!service.isInitialized()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("service is running");
    }

    public static void shutdown() {
        System.out.println("@shutdown");
        service.shutdown();
    }
}
