package services;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connector {

    private final String CONNECTIONSTRING = "jdbc:sqlserver://localhost:1433;database=PLAYLIST;";
    private final String USERNAME = "bacardi";
    private final String PASSWORD = "sql";

    public Connection getConnection() throws SQLException{
        return DriverManager.getConnection(CONNECTIONSTRING, this.USERNAME, this.PASSWORD);
    }

}
