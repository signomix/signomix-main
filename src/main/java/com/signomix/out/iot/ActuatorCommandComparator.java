/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.iot;

import org.cricketmsf.Event;
import org.cricketmsf.out.db.ComparatorIface;

/**
 * Checks if source origin is equal to source origin 
 * Returns: 0==OK,  1==NOK, -1==NPE or ClassCastException
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class ActuatorCommandComparator implements ComparatorIface {

    @Override
    public int compare(Object source, Object pattern) {
        try{
        if(((Event)pattern).getOrigin().equals(((Event)source).getOrigin())){
            return 0;
        }else{
            return 1;
        }
        }catch(NullPointerException | ClassCastException e){
            return -1;
        }
    }
}