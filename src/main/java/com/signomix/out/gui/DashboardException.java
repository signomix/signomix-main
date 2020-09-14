/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.gui;

/**
 *
 * @author greg
 */
public class DashboardException extends Exception {
    
    public static int MALFORMED_PATH = 0;
    
    public static int HELPER_NOT_AVAILABLE = 100;
    public static int HELPER_EXCEPTION = 101;
    
    public static int NOT_AUTHORIZED = 403;
    public static int NOT_FOUND = 404;
    public static int ALREADY_EXISTS = 409;
    
    public static int UNKNOWN = 1000;
    
    private String message;
    private int code;
    
    public DashboardException(int code){
        this.code = code;
        switch (code){
            case 1000:
            default:
                message = "unknown error";
                break;
        }
    }
    
    public DashboardException(int code, String message){
        this.code = code;
        this.message = message;
    }
    
    public String getMessage(){
        return message;
    }
    
    public int getCode(){
        return code;
    }
}
