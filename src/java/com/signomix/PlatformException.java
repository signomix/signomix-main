/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix;

/**
 *
 * @author greg
 */
public class PlatformException extends Exception {
    
    public static int TOO_MANY_USER_DEVICES = 1;

    public static int UNKNOWN = 1000;
    
    private String message;
    private int code;
    
    public PlatformException(int code){
        this.code = code;
        switch (code){
            case 1000:
            default:
                message = "unknown error";
                break;
        }
    }
    
    public PlatformException(int code, String message){
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
