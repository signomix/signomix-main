/**
 * Copyright (C) Grzegorz Skorupa 2020.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.mailing;

import com.signomix.out.notification.ExternalNotificatorIface;

import org.cricketmsf.microsite.cms.CmsIface;
import org.cricketmsf.microsite.out.notification.EmailSenderIface;
import org.cricketmsf.microsite.out.user.UserAdapterIface;

/**
 *
 * @author greg
 */
public interface MailingIface {

    public Object sendMailing(String docUid, String target, UserAdapterIface userAdapter, CmsIface cmsAdapter,
            EmailSenderIface emailSender, ExternalNotificatorIface externalNotificator);

}
