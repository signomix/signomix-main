/*
 * Copyright 2017 Grzegorz Skorupa <g.skorupa at gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cricketmsf.microsite.auth;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.microsite.out.auth.AuthAdapterIface;
import org.cricketmsf.microsite.out.auth.AuthException;
import org.cricketmsf.microsite.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is default filter used to check required request conditions. Does
 * nothing. Could be used as a starting point to implement required filter.
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class SecurityFilter extends Filter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);

    private static final String PERMANENT_TOKEN_PREFIX = "~~";
    private static final String SIGNOMIX_TOKEN_NAME = "signomixToken";

    private String[] restrictedPost = null;
    private String[] restrictedPut = null;
    private String[] restrictedGet = null;
    private String[] restrictedDelete = null;

    private boolean authRequired = false;

    @Override
    public String description() {
        return "Default security filter";
    }

    private void initialize() {
        ArrayList<String> aPost = new ArrayList<>();
        ArrayList<String> aPut = new ArrayList<>();
        ArrayList<String> aGet = new ArrayList<>();
        ArrayList<String> aDelete = new ArrayList<>();

        String restr = (String) Kernel.getInstance().getProperties().getOrDefault("restricted-resources", "");
        if (!restr.isEmpty()) {
            String r[] = restr.split(" ");
            String tmpPath;
            String tmpMethod;
            for (String r1 : r) {
                if (r1.isEmpty()) {
                    continue;
                }
                String[] r2 = r1.split("\\@");
                tmpMethod = r2[0];
                tmpPath = r2[1];
                switch (tmpMethod) {
                    case "*":
                        aPost.add(tmpPath);
                        aPut.add(tmpPath);
                        aGet.add(tmpPath);
                        aDelete.add(tmpPath);
                        authRequired = true;
                        break;
                    case "POST":
                        aPost.add(tmpPath);
                        authRequired = true;
                        break;
                    case "PUT":
                        aPut.add(tmpPath);
                        authRequired = true;
                        break;
                    case "GET":
                        aGet.add(tmpPath);
                        authRequired = true;
                        break;
                    case "DELETE":
                        aDelete.add(tmpPath);
                        authRequired = true;
                        break;
                }
            }
            if (aPost.size() > 0) {
                restrictedPost = new String[aPost.size()];
                restrictedPost = aPost.toArray(restrictedPost);
            } else {
                restrictedPost = new String[0];
            }
            if (aPut.size() > 0) {
                restrictedPut = new String[aPut.size()];
                restrictedPut = aPut.toArray(restrictedPut);
            } else {
                restrictedPut = new String[0];
            }
            if (aGet.size() > 0) {
                restrictedGet = new String[aGet.size()];
                restrictedGet = aGet.toArray(restrictedGet);
            } else {
                restrictedGet = new String[0];
            }
            if (aDelete.size() > 0) {
                restrictedDelete = new String[aDelete.size()];
                restrictedDelete = aDelete.toArray(restrictedDelete);
            } else {
                restrictedDelete = new String[0];
            }
        }
    }

    private boolean isRestrictedPath(String method, String path) {
        if (restrictedPost == null) {
            initialize();
        }
        if (authRequired) {
            switch (method) {
                case "GET":
                    if (restrictedGet != null) {
                        for (String restrictedGet1 : restrictedGet) {
                            if (path.startsWith(restrictedGet1)) {
                                return true;
                            }
                        }
                    }
                    break;
                case "POST":
                    if (restrictedPost != null) {
                        for (String restrictedPost1 : restrictedPost) {
                            if (path.startsWith(restrictedPost1)) {
                                return true;
                            }
                        }
                    }
                    break;
                case "PUT":
                    if (restrictedPut != null) {
                        for (String restrictedPut1 : restrictedPut) {
                            if (path.startsWith(restrictedPut1)) {
                                return true;
                            }
                        }
                    }
                    break;
                case "DELETE":
                    if (restrictedDelete != null) {
                        for (String restrictedDelete1 : restrictedDelete) {
                            if (path.startsWith(restrictedDelete1)) {
                                return true;
                            }
                        }
                    }
                    break;
            }
        }
        return false;
    }

    /**
     * Does request analysis
     *
     * @param exchange request object
     * @return
     */
    public SecurityFilterResult checkRequest(HttpExchange exchange) {

        String tokenID = null;
        String cookieValue = null;
        User user = null;
        User issuer = null;
        String cookieString = exchange.getRequestHeaders().getFirst("Cookie");
        if (null != cookieString && !cookieString.isBlank()) {
            String[] cookies = cookieString.split(";");
            String[] cookie;
            for (int i = 0; i < cookies.length; i++) {
                cookie = cookies[i].split("=");
                if (SIGNOMIX_TOKEN_NAME.equals(cookie[0].trim())) {
                    cookieValue = cookie[1];
                    logger.debug("signomixToken={}", cookieValue);
                }
            }
        }
        String path = exchange.getRequestURI().getPath();
        boolean authorizationNotRequired = true;
        try {
            authorizationNotRequired = !isRestrictedPath(exchange.getRequestMethod(), path);
        } catch (Exception e) {
            e.printStackTrace();
            Kernel.getInstance().dispatchEvent(Event.logFine(this.getClass().getSimpleName(), e.getMessage()));
        }
        Map parameters = (Map) exchange.getAttribute("parameters");
        SecurityFilterResult result = new SecurityFilterResult();
        result.user = null;
        result.issuer = null;
        String appKey=(String)parameters.get("appkey");
        if(null==appKey){
            appKey=exchange.getRequestHeaders().getFirst("X-app-key");
        }
        if(null!=appKey){
            String configuredAppKey=(String)Kernel.getInstance().properties.getOrDefault("application_key","");
            if(!configuredAppKey.isEmpty() && appKey.equalsIgnoreCase(configuredAppKey)){
                result.user=new User();
                result.user.setUid("externalService");
                result.user.setRole("admin,redactor");
                result.user.setType(User.APPLICATION);
                return result;
            }
        }

        if (authorizationNotRequired) {
            String inParamsToken = null;
            try {
                if (parameters != null) {
                    inParamsToken = (String) parameters.get("tid");
                    if (null != inParamsToken) {
                        if (inParamsToken.endsWith("/")) {
                            inParamsToken = inParamsToken.substring(0, inParamsToken.length() - 1);
                        }
                        logger.debug("tokenFromParams={}", tokenID);
                        result.user = getUser(inParamsToken, true);
                        result.issuer = getIssuer(inParamsToken);
                        // Kernel.getInstance().dispatchEvent(Event.logFine(this.getClass().getSimpleName(),
                        // "FOUND IP TOKEN " + inParamsToken + " FOR " + result.user.getUid() + " by " +
                        // result.issuer.getUid()));
                    } else {
                        tokenID = cookieValue;
                        logger.debug("tokenFromCookie={}", tokenID);
                        try {
                            user = getUser(tokenID, tokenID.startsWith(PERMANENT_TOKEN_PREFIX));
                            if ("public".equalsIgnoreCase(user.getUid())) {
                                issuer = getIssuer(tokenID);
                            }
                            result.user = user;
                            result.issuer = issuer;
                            result.code = 200;
                            return result;
                        } catch (Exception e) {
                        }
                    }
                }
            } catch (NullPointerException e) {
            } catch (AuthException e) {
                Kernel.getInstance().dispatchEvent(Event.logFine(this.getClass().getSimpleName(),
                        "AUTH PROBLEM " + e.getCode() + " " + e.getMessage())); // eg. expired token
            }
            result.code = 200;
            result.message = "";
            return result;
        }

        tokenID = exchange.getRequestHeaders().getFirst("Authentication");
        if (tokenID == null || tokenID.isEmpty()) {
            try {
                if (null != parameters) {
                    tokenID = (String) parameters.getOrDefault("tid", "");
                    if (tokenID.endsWith("/")) {
                        tokenID = tokenID.substring(0, tokenID.length() - 1);
                    }
                }
                if (null == tokenID || tokenID.isEmpty()) {
                    URI uri = exchange.getRequestURI();
                    String query = uri.getQuery();
                    if (null != query) {
                        int idx = query.indexOf("tid=");
                        if (idx >= 0) {
                            tokenID = exchange.getRequestURI().getQuery().substring(idx + 4);
                            int pos = tokenID.indexOf("&");
                            if (pos > 0) {
                                tokenID = tokenID.substring(0, pos);
                            }
                        }
                    }
                }
                if (tokenID != null && tokenID.endsWith("/")) {
                    tokenID = tokenID.substring(0, tokenID.length() - 1);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Kernel.getInstance().dispatchEvent(Event.logFine(this.getClass().getSimpleName(), e.getMessage()));
            }
        }
        if (null == tokenID || tokenID.isEmpty()) {
            tokenID = cookieValue;
            logger.debug("tokenFromCookie={}", tokenID);
        }
        try {
            user = getUser(tokenID, tokenID.startsWith(PERMANENT_TOKEN_PREFIX));
            if ("public".equalsIgnoreCase(user.getUid())) {
                issuer = getIssuer(tokenID);
            }
        } catch (Exception e) {
            result.code = 403;
            result.message = e.getMessage() + " - request blocked by security filter\r\n";
            return result;
        }
        logger.debug("user={}", user.getUid());
        result.user = user;
        result.issuer = issuer;
        result.code = 200;

        return result;
    }

    private User getUser(String token, boolean permanentToken) throws AuthException {
        // ask dedicated adapter
        AuthAdapterIface authAdapter = (AuthAdapterIface) Kernel.getInstance().getAdaptersMap()
                .getOrDefault("authAdapter", null);
        if (authAdapter != null) {
            return authAdapter.getUser(token, permanentToken);
        } else {
            return null;
        }
    }

    private User getIssuer(String token) throws AuthException {
        // ask dedicated adapter
        AuthAdapterIface authAdapter = (AuthAdapterIface) Kernel.getInstance().getAdaptersMap()
                .getOrDefault("authAdapter", null);
        if (authAdapter != null) {
            return authAdapter.getIssuer(token);
        } else {
            return null;
        }
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain)
            throws IOException {
        try {
            SecurityFilterResult result = null;
            try {
                result = checkRequest(exchange);
            } catch (Exception e) {
                e.printStackTrace();
                String message=e.getMessage();
                    if(null==message) {
                        message="";
                    }
                exchange.sendResponseHeaders(400, message.length());
                exchange.getResponseBody().write(message.getBytes());
                exchange.getResponseBody().close();
                exchange.close();
            }
            if (result.code != 200) {
                if (result.message == null) {
                    result.message = "authentication error";
                }
                exchange.sendResponseHeaders(result.code, result.message.length());
                exchange.getResponseBody().write(result.message.getBytes());
                exchange.getResponseBody().close();
                exchange.close();
            } else {
                try {
                    if (result.user != null) {
                        chain.doFilter(new Exchange(exchange, result.user, result.issuer));
                    } else {
                        chain.doFilter(exchange);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    String message=e.getMessage();
                    if(null==message) {
                        message="";
                    }
                    exchange.sendResponseHeaders(400, message.length());
                    exchange.getResponseBody().write(message.getBytes());
                    exchange.getResponseBody().close();
                    exchange.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
