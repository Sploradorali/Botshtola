package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBInteraction {
	/* User table */

    /**
     * Creates a new user record in the embedded database mapped to a XIVDB
     * character ID
     *
     * @param userId
     * @param charId
     * @return
     */
    public static int mapUserToChar(long userId, long charId) {
        String select = "select * from users where userId = ?";
        String insert = "insert into users values(?, ?)";
        String update = "update users set charid = ? where userid = ?";

        PreparedStatement selectStmt = null;
        PreparedStatement insertStmt = null;
        PreparedStatement updateStmt = null;

        try (Connection con = DBInitialization.getConnection()) {
            selectStmt = con.prepareStatement(select);
            selectStmt.setLong(1, userId);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                updateStmt = con.prepareStatement(update);
                updateStmt.setLong(1, charId);
                updateStmt.setLong(2, userId);
                updateStmt.executeUpdate();
                return 0;
            } else {
                insertStmt = con.prepareStatement(insert);
                insertStmt.setLong(1, userId);
                insertStmt.setLong(2, charId);
                insertStmt.executeUpdate();
                return 1;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Retrieves character information using information stored in the embedded database
     *
     * @param userId
     * @return
     */
    public static long getUserCharacter(long userId) {
        String select = "select charid from users where userid = ?";
        PreparedStatement selectStmt = null;

        try (Connection con = DBInitialization.getConnection()) {
            selectStmt = con.prepareStatement(select);
            selectStmt.setLong(1, userId);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                long charId = rs.getLong("charid");
                return charId;
            } else {
                return 0;
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Sets a user's character record from the embedded DB to 0
     *
     * @param userId
     */
    public static void deleteUserCharacter(long userId) {
        String update = "update users set charid = 0 where userid = ?";
        PreparedStatement updateStmt = null;

        try (Connection con = DBInitialization.getConnection()) {
            updateStmt = con.prepareStatement(update);
            updateStmt.setLong(1, userId);
            updateStmt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void mapEventToUser(long userId, int eventId) {
        String insert = "insert into userevents values(?, ?)";
        PreparedStatement insertStmt = null;

        try (Connection con = DBInitialization.getConnection()) {
            insertStmt = con.prepareStatement(insert);
            insertStmt.setLong(1, userId);
            insertStmt.setInt(2, eventId);
            insertStmt.executeUpdate();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
