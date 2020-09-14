/*
* Copyright (C) Grzegorz Skorupa 2019.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix;

import com.signomix.out.db.ShortenerDBIface;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.RequestObject;
import org.cricketmsf.in.http.StandardResult;
import org.cricketmsf.in.http.HttpAdapter;
import org.cricketmsf.out.db.KeyValueDBException;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class UrlShortener {

    private static UrlShortener logic;

    public static UrlShortener getInstance() {
        if (logic == null) {
            logic = new UrlShortener();
        }
        return logic;
    }

    public Object processRequest(
            Event event,
            ShortenerDBIface shortenerDB
    ) {
        RequestObject request = event.getRequest();
        StandardResult result = new StandardResult();
        String path = request.pathExt;
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        switch (request.method.toUpperCase()) {
            case "GET":
                result = processGet(path, shortenerDB);
                break;
            case "POST":
                result = processPost((String) request.parameters.getOrDefault("target", ""), shortenerDB);
                break;
            default:
                result.setCode(HttpAdapter.SC_METHOD_NOT_ALLOWED);
        }
        return result;
    }

    private StandardResult processGet(String path, ShortenerDBIface shortenerDB) {
        StandardResult result = new StandardResult();
        try {
            result.setCode((HttpAdapter.SC_MOVED_PERMANENTLY));
            String target = shortenerDB.getTarget(path);
            if (target.isEmpty()) {
                result.setHeader("Location", "/");
            } else {
                result.setHeader("Location", target);
            }
        } catch (KeyValueDBException ex) {
            result.setCode(HttpAdapter.SC_NOT_FOUND);
        }
        return result;
    }

    private StandardResult processPost(String target, ShortenerDBIface shortenerDB) {
        StandardResult result = new StandardResult();
        try {
            String source = Long.toString(Kernel.getEventId(), 36);
            shortenerDB.putUrl(source, target);
            result.setPayload(source.getBytes());
            result.setHeader("Content-Type", "text/plain");
        } catch (KeyValueDBException e) {
            result.setCode(HttpAdapter.SC_BAD_REQUEST);
            result.setMessage(e.getMessage());
        }
        return result;
    }
}
