/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.utility;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public interface CounterIface {
    /**
     * Adds new value to inputs counter
     * @param delta value to add
     */
    public void addInputs(long delta);
    
    /**
     * Adds new value to outputs counter
     * @param delta value to add
     */
    public void addOutputs(long delta);

    /**
     * Adds new value to inputs or outputs counter depending on the parameter sign
     * @param delta value to add
     */
    public void add(long delta);
    
    /**
     * Returns actual value of the incoming counter
     * @return counter value
     */
    public long getInputs();
    
    /**
     * Returns actual value of the exiting counter
     * @return counter value
     */
    public long getOutputs();
    
    /**
     * Returns number of actual visitors calculated as getIncoming()-getExiting()+getCorrection()
     * @return number of current visitors
     */
    public long get();
    
    /**
     * Reset counters and set new correction value
     * @param newCorrection new correction value
     */
    public void resetAndSet(long newCorrection);
    
    /**
     * Set correction value
     * 
     * @param newCorrecton new correction value
     */
    public void setCorrection(long newCorrecton);
    
    /**
     * Returns correction value
     * 
     * @return correction value
     */
    public long getCorrection();
}
