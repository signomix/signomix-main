package com.signomix.in.http.formatter.kanarek;

import java.util.ArrayList;

/**
 *
 * @author greg
 */
public class KanarekStationDto {

    public Long id=null; //required
    public String href=null;
    public String owner;
    public Boolean indoor=null;
    public Boolean ignore=null;
    public String name;
    public String country; // eg. "PL"
    public Double lat=null; //required
    public Double lon=null; //required
    public ArrayList<KanarekValue> values;

    public KanarekStationDto() {
        values = new ArrayList<>();
    }
}
