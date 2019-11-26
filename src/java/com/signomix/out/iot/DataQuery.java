/**
 * Copyright (C) Grzegorz Skorupa 2019.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.iot;

/**
 *
 * @author greg
 */
public class DataQuery {

    private int limit;
    private int average;
    private String channelName;
    private boolean timeseries;
    private String project;
    private Double newValue;
    private String group;
    private Double state;

    public DataQuery() {
        limit = 1;
        average = 0;
        channelName = null;
        timeseries = false;
        project = null;
        newValue = null;
        group = null;
        state = null;
    }

    public static DataQuery parse(String query) throws DataQueryException {
        //TODO: in case of number format exception - log SEVERE event
        DataQuery dq = new DataQuery();
        String q = query.trim().toLowerCase();
        if (q.equalsIgnoreCase("last")) {
            q = "last 1";
        }
        String[] params = q.split(" ");
        for (int i = 0; i < params.length;) {
            switch (params[i]) {
                case "last":
                    dq.setLimit(Integer.parseInt(params[i + 1]));
                    i = i + 2;
                    break;
                case "average":
                    dq.setAverage(Integer.parseInt(params[i + 1]));
                    if (params.length > i + 2) {
                        try {
                            dq.setNewValue(Double.parseDouble(params[i + 2]));
                            i = i + 3;
                        } catch (NumberFormatException ex) {
                            i = i + 2;
                        }
                    } else {
                        i = i + 2;
                    }
                    break;
                case "project":
                    dq.setProject(params[i + 1]);
                    i = i + 2;
                    break;
                case "state": {
                    try {
                        dq.setState(Double.parseDouble(params[i + 1]));
                    } catch (NumberFormatException e) {
                        //TODO:inform user about wrong query selector
                    }
                    i = i + 2;
                    break;
                }
                case "timeseries":
                case "csv.timeseries":
                    dq.setTimeseries(true);
                    i = i + 1;
                    break;
                case "channel":
                    dq.setChannelName(params[i + 1]);
                    i = i + 2;
                    break;
                case "group":
                    dq.setGroup(params[i + 1]);
                    i = i + 2;
                    break;
                case "new": {
                    try {
                        Double n = Double.parseDouble(params[i + 1]);
                        dq.setNewValue(n);
                    } catch (NumberFormatException e) {
                        //TODO:inform user about wrong query selector
                    }
                    i = i + 2;
                    break;
                }
                default:
                    throw new DataQueryException(DataQueryException.PARSING_EXCEPTION, "unrecognized word " + params[i]);
            }
        }
        if (dq.getAverage() > 0) {
            dq.setLimit(dq.getAverage());
        }
        return dq;
    }

    /**
     * @return the limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * @param limit the limit to set
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * @return the average
     */
    public int getAverage() {
        return average;
    }

    /**
     * @param average the average to set
     */
    public void setAverage(int average) {
        this.average = average;
    }

    /**
     * @return the channelName
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * @param channelName the channelName to set
     */
    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    /**
     * @return the timeseries
     */
    public boolean isTimeseries() {
        return timeseries;
    }

    /**
     * @param timeseries the timeseries to set
     */
    public void setTimeseries(boolean timeseries) {
        this.timeseries = timeseries;
    }

    /**
     * @return the project
     */
    public String getProject() {
        return project;
    }

    /**
     * @param project the project to set
     */
    public void setProject(String project) {
        this.project = project;
    }

    /**
     * @return the newValue
     */
    public Double getNewValue() {
        return newValue;
    }

    /**
     * @param newValue the newValue to set
     */
    public void setNewValue(Double newValue) {
        this.newValue = newValue;
    }

    /**
     * @return the group
     */
    public String getGroup() {
        return group;
    }

    /**
     * @param group the group to set
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * @return the state
     */
    public Double getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(Double state) {
        this.state = state;
    }

}
