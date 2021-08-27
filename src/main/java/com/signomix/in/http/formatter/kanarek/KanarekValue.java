package com.signomix.in.http.formatter.kanarek;

/**
 *
 * @author greg
 */
public class KanarekValue {

    public static final transient String PM1 = "PM1";
    public static final transient String PM10 = "PM10";
    public static final transient String PM25 = "PM25";
    public static final transient String TEMPERATURE = "Temperature";
    public static final transient String PRESSURE = "Pressure";
    public static final transient String HUMIDITY = "Humidity";
    public static final transient String NO2 = "NO2";
    public static final transient String O3 = "03";
    public static final transient String SO2 = "SO2";
    public static final transient String C6H6 = "C6H6";
    
    public String type;
    public double v;
    public long t;
    /*
    public Long v_sampling; //optional, average period, if used
    public Long v_avg; //optional
    public Long avg_sampling; //optional
    */
    public KanarekValue(){
    }
    
    public KanarekValue(String type, double value, long timestamp){
        //
        this.v=value;
        this.t=timestamp;
        switch(type.toUpperCase()){
            case "TEMPERATURE":
                this.type=TEMPERATURE;
                break;
            case "PRESSURE":
            case "BAROMETRIC_PRESSURE":
            case "BAROMETRIC-PRESSURE":
                this.type=PRESSURE;
                break;
            case "PM10":
            case "PM_10":
            case "PM-10":
            case "PM100":
                this.type=PM10;
                break;
            case "PM25":
            case "PM_25":
            case "PM-25":
            case "PM2_5":
                this.type=PM25;
                break;
            case "HUMIDITY":
            case "RELATIVE_HUMIDITY":
            case "RELATIVE-HUMIDITY":
                this.type=HUMIDITY;
                break;
            default:
                this.type=null;
        }
    }

}
