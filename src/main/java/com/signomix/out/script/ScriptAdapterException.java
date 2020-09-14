/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.script;

/**
 *
 * @author greg
 */
public class ScriptAdapterException extends Exception {
    
    public static int UNKNOWN = 1000;
    public static int NO_SUCH_METHOD = 1;
    public static int SCRIPT_EXCEPTION = 2;
    
    private String message;
    private int code;
    
    public ScriptAdapterException(int code){
        this.code = code;
        switch (code){
            case 1000:
            default:
                message = "unknown error";
                break;
        }
    }
    
    public ScriptAdapterException(int code, String message){
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
