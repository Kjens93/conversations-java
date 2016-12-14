package io.github.kjens93.conversations.communications;

import com.google.common.base.Throwables;
import io.github.kjens93.conversations.messages.MessageID;
import lombok.extern.java.Log;

import java.sql.*;
import java.util.logging.Level;

/**
 * Created by kjensen on 12/13/16.
 */
@Log
final class MessageIDFactory {

    private final int processID;
    private final Connection conn;

    MessageIDFactory(int processID) {
        this.processID = processID;
        try {
            Class.forName(org.sqlite.JDBC.class.getName());
            conn = DriverManager.getConnection("jdbc:sqlite:messages.db");
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not connect to sqlite database: [messages.db]", e);
            Throwables.propagate(e);
            throw new RuntimeException("Unexpected", e);
        }
        createTableIfNotExists();
        log.log(Level.FINE,"Opened messages database successfully");
    }

    public MessageID nextMessageID() {
        short next = getAndIncrement();
        return new MessageID(processID, next);
    }

    private void createTableIfNotExists() {
        String sql;
        try(Statement stmt = conn.createStatement()) {
            sql = "CREATE TABLE IF NOT EXISTS mids (id INT PRIMARY KEY NOT NULL, curr SMALLINT NOT NULL);";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Exception while attempting to create table: [mids]");
            throw new RuntimeException(e);
        }
    }

    private synchronized short getAndIncrement() {
        short current;
        String sql;
        ResultSet res;
        try(Statement stmt = conn.createStatement()) {
            sql = "SELECT curr FROM mids WHERE id=" + processID + ";";
            res = stmt.executeQuery(sql);
            if(res.next()) {
                current = res.getShort("curr");
                current++;
                if(current == Short.MAX_VALUE || current < 0) {
                    current = 1;
                    log.log(Level.FINE, "Reset message ID to [1] for process [" + processID + "].");
                }
                sql = "UPDATE mids SET curr=" + current + " WHERE id=" + processID + ";";
                stmt.executeUpdate(sql);
                log.log(Level.FINER, "Set message ID to [" + current + "] for process [" + processID + "].");
                return current;
            }
            else {
                sql = "INSERT INTO mids (id, curr) VALUES(" + processID + ", 1);";
                log.log(Level.FINE, "Reset message ID to [1] for process [" + processID + "].");
                stmt.executeUpdate(sql);
                return 1;
            }
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Exception while attempting to increment message id for process:" + processID);
            throw new RuntimeException(e);
        }
    }

}
