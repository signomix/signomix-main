/**
 * Copyright (C) Grzegorz Skorupa 2019.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.db;

import com.signomix.mailing.Mailing;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.KeyValueDBIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public interface MailingDBIface extends KeyValueDBIface {

    public void putMailing(Long id, Mailing mailing) throws KeyValueDBException;
    public Mailing getMailing(Long id) throws KeyValueDBException;
    public void removeMailing(Long id) throws KeyValueDBException;
}
