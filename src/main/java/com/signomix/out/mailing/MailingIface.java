/**
 * Copyright (C) Grzegorz Skorupa 2020.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.mailing;

import org.cricketmsf.microsite.cms.CmsIface;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.user.User;

import com.signomix.out.notification.MessageBrokerIface;

/**
 *
 * @author greg
 */
public interface MailingIface {

    public Object sendMailing(String docUid, String target, UserAdapterIface userAdapter, CmsIface cmsAdapter,
            MessageBrokerIface externalNotificator);

    public Object sendWelcomeDocument(User user, CmsIface cmsAdapter,
            MessageBrokerIface externalNotificator);

}
