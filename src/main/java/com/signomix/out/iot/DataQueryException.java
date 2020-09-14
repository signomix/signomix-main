/**
* Copyright (C) Grzegorz Skorupa 2019
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.iot;

/**
 *
 * @author greg
 */
public class DataQueryException extends Exception {
    
    public static int UNKNOWN = 1000;
    public static int PARSING_EXCEPTION = 2;
    
    private String message;
    private int code;
    
    public DataQueryException(int code){
        this.code = code;
        switch (code){
            case 2:
                message="syntax error";
                break;
            case 1000:
            default:
                message = "unknown error";
                break;
        }
    }
    
    public DataQueryException(int code, String message){
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
