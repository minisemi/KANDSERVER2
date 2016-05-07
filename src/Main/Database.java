package Main;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.sql.*;

/**
 * Created by Fredrik on 2016-02-18.
 */
public class Database {

    Connection conn, backupConn;
    Statement stmt, backupStmt;

    public Database() {}

    // JDBC Driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://db-und.ida.liu.se/itkand_2016_2_1";
    static final String BACKUP_DB_URL = "jdbc:mysql://db-und.ida.liu.se/itkand_2016_2_2";

    //  Main credentials
    static final String USER = "itkand_2016_2_1";
    static final String PASS = "itkand_2016_2_1_24ab";

    // BackupDatabase credentials
    static final String BACKUP_USER = "itkand_2016_2_2";
    static final String BACKUP_PASS = "itkand_2016_2_2_9d2e";



    /**
     * Calls a SELECT SQL query to the database and returns all data from that table
     * @param query SQL query to be sent to the database
     * @return JsonArray containing data from the database
     */
    public JsonArray selectData(String query) throws SQLException {
        JsonArray array;
        System.out.println("QUERY TO PROCESS: " + query);

        // 1. Connect to DataBase
        connect();
        stmt = conn.prepareStatement(query);
        // 2. Execute the SELECT query
        ResultSet resultSet = stmt.executeQuery(query);
        // 3. Convert ResultSet to JsonArray
        array = resultSetToJSON(resultSet);
        // 4. Close resultSet
        resultSet.close();

        // 5. Close database connection
        closeConnection();

        return array;
    }

    /**
     * Performs a UPDATE, INSERT or DELETE query to the database
     * @param query SQL query to be sent to the database
     */
    public void updateData(String query) throws SQLException {
        // 1. Connect to database
        connect();
        connectBackup();
        stmt = conn.createStatement();
        backupStmt = backupConn.createStatement();

        // 2. Perform update/insert/delete
        stmt.executeUpdate(query);
        backupStmt.executeUpdate(query);

        // 3. Close database connection and statement
        closeConnection();
        closeBackupConnection();
    }

    /**
     * Convert a result set into a JSON Array
     * @param resultSet ResultSet to be converted to JsonArray
     * @return a JsonArray
     */
    public static JsonArray resultSetToJSON(ResultSet resultSet) {
        JsonArray jsonArray = new JsonArray();
        Gson gson = new Gson();
        try {
            while (resultSet.next()) {
                int total_rows = resultSet.getMetaData().getColumnCount();
                JsonObject obj = new JsonObject();
                for (int i = 0; i < total_rows; i++) {
                    String columnName = resultSet.getMetaData().getColumnLabel(i + 1).toLowerCase();
                    JsonElement columnValue = gson.toJsonTree(resultSet.getObject(i + 1));

                /*
                Next if block is a hack. In case when in db we have values like price and price1 there's a bug in jdbc -
                both this names are getting stored as price in ResulSet. Therefore when we store second column value,
                we overwrite original value of price. To avoid that, i simply add 1 to be consistent with DB.
                 */
                    if (obj.has(columnName)) {
                        columnName += "1";
                    }
                    obj.add(columnName, columnValue);
                }
                jsonArray.add(obj);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

        return jsonArray;
    }

    /**
     * Connects to Database
     */
    private void connect() throws SQLException {
        try {
            // Register JDBC Driver
            Class.forName(JDBC_DRIVER);

            // Open connection to DB
            System.out.println("Connecting to DB...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void connectBackup() throws SQLException{
        try{
            //Register JDBC Driver
            Class.forName(JDBC_DRIVER);

            //Open connection to Backup DB
            backupConn = DriverManager.getConnection(BACKUP_DB_URL, BACKUP_USER, BACKUP_PASS);
        } catch (ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    /**
     * Closes connection to Database
     */
    private void closeConnection() throws SQLException {
        if (stmt != null)
            stmt.close();
        if (conn != null)
            conn.close();
    }

    private void closeBackupConnection() throws SQLException{
        if(backupStmt != null)
            backupStmt.close();

        if(backupConn != null)
            backupConn.close();
    }
}