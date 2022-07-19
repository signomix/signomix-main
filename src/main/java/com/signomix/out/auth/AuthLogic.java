package com.signomix.out.auth;

import com.signomix.Service;

import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.microsite.auth.AuthBusinessLogic;
import org.cricketmsf.microsite.out.auth.AuthAdapterIface;
import org.cricketmsf.microsite.out.auth.AuthException;
import org.cricketmsf.microsite.user.User;

public class AuthLogic extends AuthBusinessLogic {

    private static AuthLogic self;
    private String PERMANENT_TOKEN_PREFIX = "~~";

    public static AuthLogic getInstance() {
        if (self == null) {
            self = new AuthLogic();
        }
        return self;
    }

    @Override
    public Object check(Event event, AuthAdapterIface authAdapter) {
        RequestObject request = event.getRequest();
        String tokenValue = (String) request.parameters.getOrDefault("tid", "");
        Kernel.getInstance()
                .dispatchEvent(Event.logInfo(this.getClass().getSimpleName(), "Check token: " + tokenValue));
        StandardResult result = new StandardResult();
        result.setCode(HttpAdapter.SC_FORBIDDEN);
        try {
            Object withData = request.parameters.get("data");
            Kernel.getInstance()
                    .dispatchEvent(Event.logInfo(this.getClass().getSimpleName(), "with data: " + withData));
            if (null != withData) {
                String appKey = (String) Service.getInstance().getProperties().getOrDefault("application_key", "");
                String requestAppKey = (String) request.parameters.getOrDefault("appkey", "defaultValue");
                if(null==requestAppKey){
                    requestAppKey=request.headers.getFirst("X-app-key");
                }
                if (!appKey.equals(requestAppKey)) {
                    Kernel.getInstance().dispatchEvent(Event.logInfo(this.getClass().getSimpleName(),
                            "Wrong app key " + appKey + "!=" + requestAppKey));
                    return result;
                }
                String userUid;
                String issuerUid;
                String roles;
                User user = authAdapter.getUser(tokenValue, tokenValue.startsWith(PERMANENT_TOKEN_PREFIX));
                if (null != user) {
                    TokenData tokenData = new TokenData();
                    tokenData.user = user.getUid();
                    tokenData.role = user.getRole();
                    tokenData.token = tokenValue;
                    if ("public".equalsIgnoreCase(user.getUid())) {
                        User issuUser = authAdapter.getIssuer(tokenValue);
                        tokenData.issuer = null != issuUser ? issuUser.getUid() : "";
                    } else {
                        tokenData.issuer = "";
                    }
                    result.setData(tokenData);
                    result.setCode(HttpAdapter.SC_OK);
                } else {
                    // forbidden
                }
            } else {
                if (authAdapter.checkToken(tokenValue)) {
                    result.setCode(HttpAdapter.SC_OK);
                }
            }
        } catch (AuthException ex) {
            ex.printStackTrace();
            Kernel.getInstance().dispatchEvent(Event.logInfo(this.getClass().getSimpleName(), ex.getMessage()));
            if (ex.getCode() == AuthException.EXPIRED) {
                result.setCode(401);
            }
        }
        return result;
    }

}
