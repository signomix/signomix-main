/**
 * Copyright (C) Grzegorz Skorupa 2019.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.db;

import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.KeyValueDBIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public interface ShortenerDBIface extends KeyValueDBIface {

    public void putUrl(String path, String target) throws KeyValueDBException;
    public String getTarget(String path) throws KeyValueDBException;
    public void removeUrl(String target) throws KeyValueDBException;
}
