/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package org.cricketmsf.microsite.out.queue;

/**
 *
 * @author greg
 */
public class QueueException extends Exception {
    
    public static int UNKNOWN = 1000;
    
    private String message;
    private int code;
    
    public QueueException(int code){
        this.code = code;
        switch (code){
            case 1000:
            default:
                message = "unknown error";
                break;
        }
    }
    
    public QueueException(int code, String message){
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
