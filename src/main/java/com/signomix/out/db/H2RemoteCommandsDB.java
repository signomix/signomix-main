/*
* Copyright (C) Grzegorz Skorupa 2018.
* Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */
package com.signomix.out.db;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;
import com.signomix.out.iot.ThingsDataException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.cricketmsf.Adapter;
import org.cricketmsf.Event;
import org.cricketmsf.Kernel;
import org.cricketmsf.out.archiver.ZipArchiver;
import org.cricketmsf.out.db.H2RemoteDB;
import org.cricketmsf.out.db.KeyValueDBException;
import org.cricketmsf.out.db.SqlDBIface;

/**
 *
 * @author Grzegorz Skorupa <g.skorupa at gmail.com>
 */
public class H2RemoteCommandsDB extends H2RemoteDB implements SqlDBIface, ActuatorCommandsDBIface, Adapter {

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
        String query;
        String indexQuery = null;
        StringBuilder sb = new StringBuilder();
        switch (tableName) {
            case "commands":
                sb.append("create table commands (")
                        .append("id bigint,")
                        .append("category varchar,")
                        .append("type varchar,")
                        .append("origin varchar,")
                        .append("payload varchar,")
                        .append("createdat bigint);");
                indexQuery = "create index idxcommands on commands(origin);";
                break;
            case "commandslog":
                sb.append("create table commandslog (")
                        .append("id bigint,")
                        .append("category varchar,")
                        .append("type varchar,")
                        .append("origin varchar,")
                        .append("payload varchar,")
                        .append("createdat bigint);");
                indexQuery = "create index idxcommandslog on commandslog(origin);";
                break;
            default:
                throw new KeyValueDBException(KeyValueDBException.CANNOT_CREATE, "unable to create table " + tableName);
        }
        query = sb.toString();
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.executeUpdate();
            if (indexQuery != null) {
                PreparedStatement pst2 = conn.prepareStatement(indexQuery);
                pst2.executeUpdate();
                pst2.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            map = getAll("commands");
            String json = JsonWriter.objectToJson(map, args);
            archiver.addFileContent("commands.json", json);
            map = getAll("commandslog");
            json = JsonWriter.objectToJson(map, args);
            archiver.addFileContent("commandslog.json", json);
            return archiver.getFile();
        } catch (KeyValueDBException | IOException ex) {
            Kernel.getInstance().dispatchEvent(Event.logWarning(this.getClass().getSimpleName(), ex.getMessage()));
            return null;
        }
    }
    
    private void loadBackupFile(String fileName){
        ZipArchiver archiver;
        try {
            Path filePath = Path.of(fileName);
	    String json = Files.readString(filePath);
            switch(fileName) {
                case "commands.json":
                    //HashMap<String,Object>map=(HashMap)getAllCommands();
                    HashMap<String,Object> map = (HashMap)JsonReader.jsonToJava(json);
                    Iterator<String> it=map.keySet().iterator();
                    while(it.hasNext()){
                        String eui=it.next();
                        Event ev=(Event)map.get(eui);
                        putDeviceCommand(eui, ev);
                    }
                    break;
                case "connamdslog.json":
                    break;
            }
        } catch (IOException|ThingsDataException ex) {
            Logger.getLogger(H2RemoteCommandsDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    @Override
    public void putDeviceCommand(String deviceEUI, Event commandEvent) throws ThingsDataException {
        String query = "insert into commands (id,category,type,origin,payload,createdat) values (?,?,?,?,?,?);";
        //String query2 = "update commands set id=?,payload=?,createdat=? where category=? and type=? and origin=?";
        String query2 = "merge into commands key (category,type,origin) values (?,?,?,?,?,?)";
        String command = (String) commandEvent.getPayload();
        boolean overwriteMode = false;
        if (command.startsWith("&")) {
            overwriteMode = false;
        } else if (command.startsWith("#")) {
            query = query2;
            overwriteMode = true;
        }
        command = command.substring(1);
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setLong(1, commandEvent.getId());
            pst.setString(2, commandEvent.getCategory());
            pst.setString(3, commandEvent.getType());
            pst.setString(4, deviceEUI);
            pst.setString(5, command);
            pst.setLong(6, commandEvent.getCreatedAt());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(e.getErrorCode(), e.getMessage());
        }
        /*if (!overwriteMode) {
            try (Connection conn = getConnection()) {
                PreparedStatement pst;
                pst = conn.prepareStatement(query);
                pst.setLong(1, commandEvent.getId());
                pst.setString(2, commandEvent.getCategory());
                pst.setString(3, commandEvent.getType());
                pst.setString(4, deviceEUI);
                pst.setString(5, (String) commandEvent.getPayload());
                pst.setLong(6, commandEvent.getCreatedAt());
                pst.executeUpdate();
                pst.close();
                conn.close();
            } catch (SQLException e) {
                throw new ThingsDataException(e.getErrorCode(), e.getMessage());
            }
        } else {
            try (Connection conn = getConnection()) {
                PreparedStatement pst;
                pst = conn.prepareStatement(query2);
                pst.setLong(1, commandEvent.getId());
                pst.setString(2, (String) commandEvent.getPayload());
                pst.setLong(3, commandEvent.getCreatedAt());
                pst.setString(4, commandEvent.getCategory());
                pst.setString(5, commandEvent.getType());
                pst.setString(6, commandEvent.getOrigin());
                pst.executeUpdate();
                pst.close();
                conn.close();
            } catch (SQLException e) {
                throw new ThingsDataException(e.getErrorCode(), e.getMessage());
            }
        }*/
    }

    @Override
    public Event getFirstCommand(String deviceEUI) throws ThingsDataException {
        String query = "select id,category,type,payload,createdat from commands where origin like ? order by createdat limit 1";
        Event result = null;
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, "%@" + deviceEUI);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                result = new Event(deviceEUI, rs.getString(2), rs.getString(3), null, rs.getString(4));
                result.setId(rs.getLong(1));
                result.setCreatedAt(rs.getLong(5));
            }
            return result;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public Event previewDeviceCommand(String deviceEUI, Event commandEvent) throws ThingsDataException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clearAllCommands(String deviceEUI, long checkPoint) throws ThingsDataException {
        String query = "delete from commands where origin like ? and createdat<?";
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, "%@" + deviceEUI);
            pst.setLong(2, checkPoint);
            //pst.setTimestamp(2, new java.sql.Timestamp(checkPoint));
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public void removeAllCommands(String deviceEUI) throws ThingsDataException {
        String query = "delete from commands where origin like ?";
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, "%@" + deviceEUI);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public List<Event> getAllCommands(String deviceEUI) throws ThingsDataException {
        String query = "select id,category,type,payload,createdat from commands where origin like ? order by createdat";
        ArrayList<Event> result = new ArrayList<>();
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, "%@" + deviceEUI);
            ResultSet rs = pst.executeQuery();
            Event ev;
            while (rs.next()) {
                ev = new Event(deviceEUI, rs.getString(2), rs.getString(3), null, rs.getString(4));
                ev.setId(rs.getLong(1));
                ev.setCreatedAt(rs.getLong(5));
                result.add(ev.clone());
            }
            return result;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void putCommandLog(String deviceEUI, Event commandEvent) throws ThingsDataException {
        String query = "insert into commandslog (id,category,type,origin,payload,createdat) values (?,?,?,?,?,?);";
        String command = (String) commandEvent.getPayload();
        if (command.startsWith("#") || command.startsWith("&")) {
            command = command.substring(1);
        }
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setLong(1, commandEvent.getId());
            pst.setString(2, commandEvent.getCategory());
            pst.setString(3, commandEvent.getType());
            pst.setString(4, deviceEUI);
            pst.setString(5, command);
            pst.setLong(6, commandEvent.getCreatedAt());
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(e.getErrorCode(), e.getMessage());
        }
    }

    @Override
    public void clearAllLogs(String deviceEUI, long checkPoint) throws ThingsDataException {
        String query = "delete from commandslog where origin like ? and createdat<?";
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, "%@" + deviceEUI);
            pst.setLong(2, checkPoint);
            //pst.setTimestamp(2, new java.sql.Timestamp(checkPoint));
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public void removeAllLogs(String deviceEUI) throws ThingsDataException {
        String query = "delete from commandslog where origin like ?";
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, "%@" + deviceEUI);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public List<Event> getAllLogs(String deviceEUI) throws ThingsDataException {
        String query = "select id,category,type,payload,createdat from commandslog where origin like ? order by createdat";
        ArrayList<Event> result = new ArrayList<>();
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setString(1, "%@" + deviceEUI);
            ResultSet rs = pst.executeQuery();
            Event ev;
            while (rs.next()) {
                ev = new Event(deviceEUI, rs.getString(2), rs.getString(3), null, rs.getString(4));
                ev.setId(rs.getLong(1));
                ev.setCreatedAt(rs.getLong(5));
                result.add(ev.clone());
            }
            return result;
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.HELPER_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void removeCommand(long id) throws ThingsDataException {
        String query = "delete from commands where id=?";
        try ( Connection conn = getConnection();  PreparedStatement pst = conn.prepareStatement(query);) {
            pst.setLong(1, id);
            pst.executeUpdate();
        } catch (SQLException e) {
            throw new ThingsDataException(ThingsDataException.BAD_REQUEST, e.getMessage());
        }
    }

}
