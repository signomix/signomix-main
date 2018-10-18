/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix.out.iot;

import org.cricketmsf.out.db.ComparatorIface;

/**
 * Checks if source origin starts with userId+"~" Returns: 0 - OK 1 - NOK -1 -
 * source is not Event
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class MeasurePseudoComparator implements ComparatorIface {

    @Override
    public int compare(Object source, Object pattern) {
        return 0;
    }
}