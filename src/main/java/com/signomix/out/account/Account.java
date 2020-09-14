/*
* Copyright (C) Grzegorz Skorupa 2019.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.account;

import java.util.Date;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class Account {
    private String user;
    private int numberOfDevices;
    private Double balance;
    private Date refillDate;
    private long readsCounter;
    private long writesCounter;

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the numberOfDevices
     */
    public int getNumberOfDevices() {
        return numberOfDevices;
    }

    /**
     * @param numberOfDevices the numberOfDevices to set
     */
    public void setNumberOfDevices(int numberOfDevices) {
        this.numberOfDevices = numberOfDevices;
    }

    /**
     * @return the balance
     */
    public Double getBalance() {
        return balance;
    }

    /**
     * @param balance the balance to set
     */
    public void setBalance(Double balance) {
        this.balance = balance;
    }

    /**
     * @return the refillDate
     */
    public Date getRefillDate() {
        return refillDate;
    }

    /**
     * @param refillDate the refillDate to set
     */
    public void setRefillDate(Date refillDate) {
        this.refillDate = refillDate;
    }

    /**
     * @return the readsCounter
     */
    public long getReadsCounter() {
        return readsCounter;
    }

    /**
     * @param readsCounter the readsCounter to set
     */
    public void setReadsCounter(long readsCounter) {
        this.readsCounter = readsCounter;
    }

    /**
     * @return the writesCounter
     */
    public long getWritesCounter() {
        return writesCounter;
    }

    /**
     * @param writesCounter the writesCounter to set
     */
    public void setWritesCounter(long writesCounter) {
        this.writesCounter = writesCounter;
    }
}
