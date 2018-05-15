import java.io.File;
import java.sql.*;

@SuppressWarnings("Duplicates")
class Database {
    private static Connection connection;
    private static PreparedStatement qCheckUserExists;
    private static PreparedStatement qCreateUser;
    private static PreparedStatement qDeleteUser;
    private static PreparedStatement qSetLoginState;
    private static PreparedStatement qGetLoginState;
    private static PreparedStatement qSetUsername;
    private static PreparedStatement qGetUsername;
    private static PreparedStatement qSetSessionToken;
    private static PreparedStatement qGetSessionToken;
    private static PreparedStatement qDeleteSessionToken;
    private static PreparedStatement qSetActionType;
    private static PreparedStatement qGetActionType;


    static {
        try {
            boolean dbExists = new File("database.sqlite").exists();
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:database.sqlite");
            connection.setAutoCommit(true);
            if (!dbExists) {
                Statement statement = connection.createStatement();
                statement.execute("CREATE TABLE users\n" +
                                          "(\n" +
                                          "  id       INTEGER NOT NULL PRIMARY KEY,\n" +
                                          "  state    INTEGER,\n" +
                                          "  username TEXT,\n" +
                                          "  sessionToken    TEXT,\n" +
                                          "  actionType     INTEGER\n" +
                                          ");");
                System.err.println("Database was missing. Created one.");
            }
            qCheckUserExists = connection.prepareStatement("SELECT count(id) FROM users WHERE id = ?");
            qCreateUser = connection.prepareStatement("INSERT INTO users (id, state, username, sessionToken, actionType) VALUES (?, 0, '', '', 0);");
            qDeleteUser = connection.prepareStatement("DELETE FROM users WHERE id = ?;");
            qSetLoginState = connection.prepareStatement("UPDATE users SET state = ? WHERE id = ?;");
            qGetLoginState = connection.prepareStatement("SELECT state FROM users WHERE id = ?;");
            qSetUsername = connection.prepareStatement("UPDATE users SET username = ? WHERE id = ?;");
            qGetUsername = connection.prepareStatement("SELECT username FROM users WHERE id = ?");
            qSetSessionToken = connection.prepareStatement("UPDATE users SET sessionToken = ? WHERE id = ?;");
            qGetSessionToken = connection.prepareStatement("SELECT sessionToken FROM users WHERE id = ?");
            qDeleteSessionToken = connection.prepareStatement("UPDATE users SET sessionToken = '' WHERE id = ?");
            qSetActionType = connection.prepareStatement("UPDATE users SET actionType = ? WHERE id = ?;");
            qGetActionType = connection.prepareStatement("SELECT actionType FROM users WHERE id = ?");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    static int getLoginState(Integer id) {
        try {
            qCheckUserExists.setInt(1, id);
            ResultSet userExistsResultSet = qCheckUserExists.executeQuery();
            if (!userExistsResultSet.first() || userExistsResultSet.getInt(1) == 0) {
                qCreateUser.setInt(1, id);
                qCreateUser.executeUpdate();
            }
            qGetLoginState.setInt(1, id);
            ResultSet loginStateResultSet = qGetLoginState.executeQuery();
            loginStateResultSet.first();
            return loginStateResultSet.getInt("state");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    static void setUsername(Integer id, String username) {
        try {
            qSetUsername.setString(1, username);
            qSetUsername.setInt(2, id);
            qSetUsername.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static String getUsername(Integer id) {
        try {
            qGetUsername.setInt(1, id);
            ResultSet resultSet = qGetUsername.executeQuery();
            if (resultSet.first()) {
                return resultSet.getString("username");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void setSessionToken(Integer id, String sessionToken) {
        try {
            qSetSessionToken.setString(1, sessionToken);
            qSetSessionToken.setInt(2, id);
            qSetSessionToken.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static String getSessionToken(Integer id) {
        try {
            qGetSessionToken.setInt(1, id);
            ResultSet resultSet = qGetSessionToken.executeQuery();
            if (resultSet.first()) {
                return resultSet.getString("sessionToken");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    static void invalidateSessionToken(Integer id) {
        setLoginState(id, 1);
        try {
            qDeleteSessionToken.setInt(1, id);
            qDeleteSessionToken.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void setLoginState(Integer id, int state) {
        try {
            qSetLoginState.setInt(1, state);
            qSetLoginState.setInt(2, id);
            qSetLoginState.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void setType(Integer id, ActionType type) {
        try {
            qSetActionType.setInt(1, type.toInt());
            qSetActionType.setInt(2, id);
            qSetActionType.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static ActionType getType(Integer id) {
        try {
            qGetActionType.setInt(1, id);
            ResultSet resultSet = qGetActionType.executeQuery();
            if (resultSet.first()) {
                return ActionType.fromInt(resultSet.getInt("actionType"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ActionType.UNSET;
    }

    static void forget(Integer id) {
        try {
            qDeleteUser.setInt(1, id);
            qDeleteUser.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void close() throws SQLException {
        connection.commit();
        connection.close();
    }
}
