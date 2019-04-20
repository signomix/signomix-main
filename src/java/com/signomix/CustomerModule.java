/**
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
*/
package com.signomix;

import com.signomix.out.notification.EmailSenderIface;
import org.cricketmsf.microsite.user.*;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.microsite.out.auth.AuthAdapterIface;
import org.cricketmsf.microsite.out.auth.AuthException;
import org.cricketmsf.microsite.out.auth.Token;
import org.cricketmsf.microsite.out.user.UserAdapterIface;
import org.cricketmsf.microsite.out.user.UserException;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class CustomerModule {

    private static CustomerModule self;

    public static CustomerModule getInstance() {
        if (self == null) {
            self = new CustomerModule();
        }
        return self;
    }

    /**
     * Creates temporary token and sends e-mail including link to reset
     * password;
     *
     * @param event
     * @param userAdapter
     * @return
     */
    public Object handleResetRequest(
            Event event,
            String userID,
            String resetPassEmail,
            UserAdapterIface userAdapter,
            AuthAdapterIface authAdapter,
            EmailSenderIface emailSender) {

        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        if (userID == null || userID.isEmpty()) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            return result;
        }
        try {
            User user = userAdapter.get(userID);
            if (user == null) {
                result.setCode(HttpAdapter.SC_FORBIDDEN);
                return result;
            }
            String email = user.getEmail();
            if (!resetPassEmail.equalsIgnoreCase(email)) {
                result.setCode(HttpAdapter.SC_FORBIDDEN);
                return result;
            }

            // create link
            Token token = authAdapter.createPermanentToken(userID, userID, false, null);
            Kernel.getInstance().dispatchEvent(new UserEvent(UserEvent.USER_RESET_PASSWORD, token.getToken() + ":" + email));

        } catch (NullPointerException | UserException | AuthException e) {
            //e.printStackTrace();
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
        }
        return result;
    }

    public Object handlePermanentLinkRequest(
            Event event,
            UserAdapterIface userAdapter,
            AuthAdapterIface authAdapter,
            EmailSenderIface emailSender) {

        RequestObject request = event.getRequest();
        String userID = request.headers.getFirst("X-issuer-id");
        String link = ""+event.getPayload();
        String publicUserID = "public";
        StandardResult result = new StandardResult();
        try {
            User user = userAdapter.get(userID);
            User publicUser = userAdapter.get(publicUserID);
            if (user == null) {
                result.setCode(HttpAdapter.SC_FORBIDDEN);
                return result;
            }
            if (publicUser == null) {
                result.setCode(HttpAdapter.SC_BAD_REQUEST);
                return result;
            }
            String email = user.getEmail();

            // create link
            Token token = authAdapter.createPermanentToken(publicUserID, userID, true, null);
            link=link.concat(token.getToken());
            Kernel.getInstance().dispatchEvent(new UserEvent(UserEvent.USER_NEW_PERMALINK, link));
            result.setData(link);
        } catch (NullPointerException | UserException | AuthException e) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
        }
        return result;
    }
    
    public Object handleDeleteLinkRequest(Event event){
        RequestObject request = event.getRequest();
        String userID = request.headers.getFirst("X-issuer-id");
        String link = ""+event.getPayload();
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_NOT_IMPLEMENTED);
        return null;
    }
}
