/*
* Copyright (C) Grzegorz Skorupa 2019.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import org.cricketmsf.Adapter;
import org.cricketmsf.out.db.H2EmbededDB;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.SqlDBIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class H2ShortenerDB extends H2EmbededDB implements SqlDBIface, ShortenerDBIface, Adapter {

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
    }

    @Override
    public void addTable(String tableName, int maxSize, boolean persistent) throws KeyValueDBException {

        String query;
        String indexQuery = null;
        StringBuilder sb = new StringBuilder();
        switch (tableName) {
            case "urls":
                sb.append("create table urls (")
                        .append("source varchar,")
                        .append("target varchar primary key)");
                indexQuery = "create index idxurls on urls(source);";
                break;
            default:
                throw new KeyValueDBException(KeyValueDBException.CANNOT_CREATE, "unable to create table " + tableName);
        }
        query = sb.toString();
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.executeUpdate();
            pst.close();
            if (indexQuery != null) {
                PreparedStatement pst2 = conn.prepareStatement(indexQuery);
                pst2.executeUpdate();
                pst2.close();
            }
            conn.close();
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public void removeUrl(String target) throws KeyValueDBException {
        String query = "delete from urls where target=?";
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, target);
            pst.executeUpdate();
            pst.close();
            conn.close();
        } catch (SQLException e) {
            throw new KeyValueDBException(KeyValueDBException.CANNOT_DELETE, e.getMessage());
        }
    }

    @Override
    public void putUrl(String path, String target) throws KeyValueDBException {
        String query = "merge into urls key(target) values (?,?);";
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, path);
            pst.setString(2, target);
            pst.executeUpdate();
            pst.close();
            conn.close();
        } catch (SQLException e) {
            throw new KeyValueDBException(KeyValueDBException.CANNOT_WRITE, e.getMessage());
        }
    }

    @Override
    public String getTarget(String path) throws KeyValueDBException {
        String query = "select target from urls where source=?";
        String target = "";
        try (Connection conn = getConnection()) {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.setString(1, path);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                target=rs.getString(1);
            }
            pst.close();
            conn.close();
            return target;
        } catch (SQLException e) {
            throw new KeyValueDBException(KeyValueDBException.UNKNOWN, e.getMessage());
        }
    }

}
