/**
 * Copyright (C) Grzegorz Skorupa 2019.
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cedarsoftware.util.io.JsonWriter;

import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.microsite.user.Organization;
import org.cricketmsf.microsite.user.User;
import org.cricketmsf.out.archiver.ZipArchiver;
import org.cricketmsf.out.db.ComparatorIface;
import org.cricketmsf.out.db.H2RemoteDB;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.SqlDBIface;

/**
 *
 * @author greg
 */
public class H2RemoteUserDB extends H2RemoteDB implements SqlDBIface, Adapter {

    private static int MAX_CONNECTIONS = 100;

    @Override
    public void loadProperties(HashMap<String, String> properties, String adapterName) {
        super.loadProperties(properties, adapterName);
    }

    @Override
    public void start() throws KeyValueDBException {
        super.start();
        cp.setMaxConnections(MAX_CONNECTIONS);
    }

    @Override
    public void addTable(String tableName, int maxSize, boolean persistent) throws KeyValueDBException {
        if (!(tableName.equalsIgnoreCase("users") || tableName.equalsIgnoreCase("organizations"))) {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                           // choose Tools | Templates.
        }
        String query;
        StringBuilder sb = new StringBuilder();
        sb.append("create sequence if not exists user_number_seq;");
        sb.append("create sequence if not exists org_number_seq;");
        sb.append("create table if not exists organizations (")
                .append("id bigint default org_number_seq.nextval primary key,")
                .append("name varchar);");
        sb.append("create table if not exists users (")
                .append("uid varchar primary key,")
                .append("type int,")
                .append("email varchar,")
                .append("name varchar,")
                .append("surname varchar,")
                .append("role varchar,")
                .append("secret varchar,")
                .append("password varchar,")
                .append("generalchannel varchar,")
                .append("infochannel varchar,")
                .append("warningchannel varchar,")
                .append("alertchannel varchar,")
                .append("confirmed boolean,")
                .append("unregisterreq boolean,")
                .append("authstatus int,")
                .append("created timestamp,")
                .append("services int,")
                .append("phoneprefix varchar,")
                .append("credits bigint,")
                .append("user_number bigint default user_number_seq.nextval,")
                .append("autologin boolean,")
                .append("language varchar,")
                .append("organization bigint default 0 references organizations);");
        query = sb.toString();
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            boolean updated = pst.executeUpdate() > 0;
            if (!updated) {
                throw new KeyValueDBException(KeyValueDBException.CANNOT_CREATE, "unable to create table " + tableName);
            }
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
        query="insert into organizations (id,name) values (0,'');";
        try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public File getBackupFile() {
        try {
            ZipArchiver archiver = new ZipArchiver("data-", ".zip");
            Map args = new HashMap();
            args.put(JsonWriter.TYPE, true);
            args.put(JsonWriter.PRETTY_PRINT, true);
            Map map;
            map = getAll("users");
            String json = JsonWriter.objectToJson(map, args);
            archiver.addFileContent("users.json", json);
            return archiver.getFile();
        } catch (KeyValueDBException | IOException ex) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
            return null;
        }
    }

    @Override
    public void put(String tableName, String key, Object o) throws KeyValueDBException {
        if (tableName.equals("users")) {
            try {
                putUser(tableName, key, (User) o);
            } catch (ClassCastException e) {
                throw new KeyValueDBException(KeyValueDBException.UNKNOWN, "object is not a User");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (tableName.equals("organizations")) {
            try {
                putOrganization((Organization) o);
            } catch (ClassCastException e) {
                throw new KeyValueDBException(KeyValueDBException.UNKNOWN, "object is not a User");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new KeyValueDBException(KeyValueDBException.CANNOT_CREATE, "unsupported table " + tableName);
        }
    }

    private void putUser(String tableName, String key, User user) throws KeyValueDBException {
        String query = "merge into ?? (uid,type,email,name,surname,role,secret,password,generalchannel,infochannel,warningchannel,alertchannel,confirmed,unregisterreq,authstatus,created,services,phoneprefix,credits,autologin,language,organization) key (uid) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        query = query.replaceFirst("\\?\\?", tableName);
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            // uid,type,email,name,surname,role,secret,password,generalchannel,infochannel,warningchannel,alertchannel,confirmed,unregisterreq,authstatus,created,user_number,services,phoneprefix,credits,autologin
            pstmt.setString(1, user.getUid());
            pstmt.setInt(2, user.getType());
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getName());
            pstmt.setString(5, user.getSurname());
            pstmt.setString(6, user.getRole());
            pstmt.setString(7, user.getConfirmString());
            pstmt.setString(8, user.getPassword());
            pstmt.setString(9, user.getGeneralNotificationChannel());
            pstmt.setString(10, user.getInfoNotificationChannel());
            pstmt.setString(11, user.getWarningNotificationChannel());
            pstmt.setString(12, user.getAlertNotificationChannel());
            pstmt.setBoolean(13, user.isConfirmed());
            pstmt.setBoolean(14, user.isUnregisterRequested());
            pstmt.setInt(15, user.getStatus());
            pstmt.setTimestamp(16, new Timestamp(user.getCreatedAt()));
            pstmt.setInt(17, user.getServices());
            pstmt.setString(18, user.getPhonePrefix());
            pstmt.setLong(19, user.getCredits());
            pstmt.setBoolean(20, user.isAutologin());
            pstmt.setString(21, user.getPreferredLanguage());
            pstmt.setLong(22, user.getOrganization());
            int updated = pstmt.executeUpdate();
            // check?
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void putOrganization(Organization organization) throws KeyValueDBException {
        if (null == organization.id || organization.id==-1) {
            String query = "insert into organizations (name) values (?)";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
                // uid,type,email,name,surname,role,secret,password,generalchannel,infochannel,warningchannel,alertchannel,confirmed,unregisterreq,authstatus,created,user_number,services,phoneprefix,credits,autologin
                pstmt.setString(1, organization.name);
                int updated = pstmt.executeUpdate();
                // check?
            } catch (SQLException e) {
                throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String query = "merge into organizations (id,name) key (id) values (?,?)";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
                // uid,type,email,name,surname,role,secret,password,generalchannel,infochannel,warningchannel,alertchannel,confirmed,unregisterreq,authstatus,created,user_number,services,phoneprefix,credits,autologin
                pstmt.setLong(1, organization.id);
                pstmt.setString(2, organization.name);
                int updated = pstmt.executeUpdate();
                // check?
            } catch (SQLException e) {
                e.printStackTrace();
                throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public Object get(String tableName, String key) throws KeyValueDBException {
        return get(tableName, key, null);
    }

    @Override
    public Object get(String tableName, String key, Object o) throws KeyValueDBException {
        if (tableName.equals("users")) {
            return getUser(tableName, key, o);
        } else if (tableName.equals("organizations")) {
            return getOrganization(key);
        } else {
            return null;
        }
    }

    @Override
    public Map getAll(String tableName) throws KeyValueDBException {
        // TODO: nie używać, zastąpić konkretnymi search'ami
        if (tableName.equals("users")) {
            HashMap<String, User> map = new HashMap<>();
            String query = "select uid,type,email,name,surname,role,secret,password,generalchannel,infochannel,warningchannel,alertchannel,confirmed,unregisterreq,authstatus,created,user_number,services,phoneprefix,credits,autologin,language,organization from users order by uid asc";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    map.put(rs.getString(1), buildUser(rs));
                }
            } catch (SQLException e) {
                throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
            }
            return map;
        } else if (tableName.equals("organizations")) {
            HashMap<String, Organization> map = new HashMap<>();
            String query = "select id,name from organizations order by id asc";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    map.put("" + rs.getLong(1), buildOrganization(rs));
                }
                return map;
            } catch (SQLException e) {
                throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
            }
        }
        return new HashMap<String, Object>();
    }

    @Override
    public boolean containsKey(String tableName, String key) throws KeyValueDBException {
        String query;
        if (tableName.equals("users")) {
            query = "select uid from " + tableName + " where uid=?";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
                pstmt.setString(1, key);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return true;
                }
            } catch (SQLException e) {
                throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
            }
        } else if (tableName.equals("organizations")) {
            query = "select id from " + tableName + " where id=?";
            try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
                long id = Long.parseLong(key);
                pstmt.setLong(1, id);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return true;
                }
            } catch (NumberFormatException e) {
                throw new KeyValueDBException(KeyValueDBException.UNKNOWN, e.getMessage());
            } catch (SQLException e) {
                throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
            }
        } else {
            throw new KeyValueDBException(KeyValueDBException.TABLE_NOT_EXISTS, "unsupported table " + tableName);
        }

        return false;
    }

    @Override
    public boolean remove(String tableName, String key) throws KeyValueDBException {
        String query = "delete from ?? where uid = ?".replaceFirst("\\?\\?", tableName);
        boolean updated = false;
        if (tableName.equals("users")) {
            // query;
            try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
                pst.setString(1, key);
                updated = pst.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
            }
        } else if (tableName.equals("organizations")) {
            query = "delete from organizations where id=?";
            try (Connection conn = getConnection(); PreparedStatement pst = conn.prepareStatement(query);) {
                long id = Long.parseLong(key);
                pst.setLong(1, id);
                updated = pst.executeUpdate() > 0;
            } catch (NumberFormatException e) {
                throw new KeyValueDBException(KeyValueDBException.UNKNOWN, e.getMessage());
            } catch (SQLException e) {
                throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
            }

        } else {
            throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods,
                                                                           // choose Tools | Templates.
        }
        return updated;
    }

    @Override
    public List search(String tableName, String statement, Object[] parameters) throws KeyValueDBException {
        ArrayList<User> result = new ArrayList<>();
        String query = "select uid,type,email,name,surname,role,secret,password,generalchannel,infochannel,warningchannel,alertchannel,confirmed,unregisterreq,authstatus,created,user_number,services,phoneprefix,credits,autologin,language,organization from "
                + tableName + " where user_number=?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            // uid,type,email,name,surname,role,secret,password,generalchannel,infochannel,warningchannel,alertchannel,confirmed,unregisterreq,authstatus,created,user_number,services,phoneprefix,credits,autologin
            pstmt.setLong(1, (Long) parameters[0]);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                result.add(buildUser(rs));
            }
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
        return result;
    }

    @Override
    public List search(String tableName, ComparatorIface ci, Object o) throws KeyValueDBException {
        throw new UnsupportedOperationException("Not supported yet."); // To change body of generated methods, choose
                                                                       // Tools | Templates.
    }

    User buildUser(ResultSet rs) throws SQLException {
        // uid,type,email,name,surname,role,secret,password,generalchannel,infochannel,warningchannel,alertchannel,confirmed,unregisterreq,authstatus,created,number,services,phoneprefix,credits,autologin
        User user = new User();
        user.setUid(rs.getString(1));
        user.setType(rs.getInt(2));
        user.setEmail(rs.getString(3));
        user.setName(rs.getString(4));
        user.setSurname(rs.getString(5));
        user.setRole(rs.getString(6));
        user.setConfirmString(rs.getString(7));
        user.setPassword(rs.getString(8));
        user.setGeneralNotificationChannel(rs.getString(9));
        user.setInfoNotificationChannel(rs.getString(10));
        user.setWarningNotificationChannel(rs.getString(11));
        user.setAlertNotificationChannel(rs.getString(12));
        user.setConfirmed(rs.getBoolean(13));
        user.setUnregisterRequested(rs.getBoolean(14));
        user.setStatus(rs.getInt(15));
        user.setCreatedAt(rs.getTimestamp(16).getTime());
        user.setNumber(rs.getLong(17));
        user.setServices(rs.getInt(18));
        user.setPhonePrefix(rs.getString(19));
        user.setCredits(rs.getLong(20));
        user.setAutologin(rs.getBoolean(21));
        user.setPreferredLanguage(rs.getString(22));
        user.setOrganization(rs.getLong(23));
        return user;
    }

    Organization buildOrganization(ResultSet rs) throws SQLException {
        // uid,type,email,name,surname,role,secret,password,generalchannel,infochannel,warningchannel,alertchannel,confirmed,unregisterreq,authstatus,created,number,services,phoneprefix,credits,autologin
        Organization org = new Organization(rs.getLong(1), rs.getString(2));
        return org;
    }

    private Object getUser(String tableName, String key, Object defaultResult) throws KeyValueDBException {
        User user = null;
        String query = "select uid,type,email,name,surname,role,secret,password,generalchannel,infochannel,warningchannel,alertchannel,confirmed,unregisterreq,authstatus,created,user_number,services,phoneprefix,credits,autologin,language,organization from "
                + tableName + " where uid=?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            // uid,type,email,name,surname,role,secret,password,generalchannel,infochannel,warningchannel,alertchannel,confirmed,unregisterreq,authstatus,created,user_number,services,phoneprefix,credits,autologin
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                user = buildUser(rs);
            }
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
        if (user == null) {
            return defaultResult;
        } else {
            return user;
        }
    }

    private Object getOrganization(String key) throws KeyValueDBException {
        Organization org = null;
        String query = "select id,name from organizations where id=?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(query);) {
            long id = Long.parseLong(key);
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                org = buildOrganization(rs);
            }
        } catch (NumberFormatException e) {
            throw new KeyValueDBException(KeyValueDBException.UNKNOWN, e.getMessage());
        } catch (SQLException e) {
            throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
        return org;
    }

    @Override
    protected void updateStructureTo(Connection conn, int versionNumber) throws KeyValueDBException {
        String query = "";
        switch (versionNumber) {
            case 2:
                query = "create sequence if not exists user_number_seq; alter table users add user_number bigint default user_number_seq.nextval;";
                break;
        }
        try {
            PreparedStatement pst;
            pst = conn.prepareStatement(query);
            pst.executeUpdate();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
            // throw new KeyValueDBException(e.getErrorCode(), e.getMessage());
        }
    }

}
