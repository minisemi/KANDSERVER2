package Server_;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by Fredrik on 2016-03-15.
 */
public class Processor {
    private Database database;
    private JsonObject receivedObject, data;
    private JsonArray dataArray;
    private String activity, action, id, response;

    /**
     *
     */

    public Processor() {
        database = new Database();
        response = "";

    }


    /**
     * Process the message received from a client and makes
     * different request depending on what the message asks for
     */
    public String processMessage(String jsonRequest) {
        //Create parser for the request
        JsonParser parser = new JsonParser();
        // Convert Json formatted string to one JsonObject
        receivedObject = (JsonObject) parser.parse(jsonRequest);

        // Assign the request fields of the header
        activity = receivedObject.get("activity").getAsString().toLowerCase();
        action = receivedObject.get("action").getAsString().toLowerCase();
        id = receivedObject.get("sessionid").getAsString().toLowerCase();

        // The data field of the request
        dataArray = (JsonArray) receivedObject.get("data");
        try {
            data = (JsonObject) dataArray.get(0);
        } catch (IndexOutOfBoundsException i) {
            // Data array is empty
        }

        // Manipulating data requires that the user has permission to do so
        // The exception is when the user logs in to the system with nfc
        boolean activeSession;
        // Ignore if it's a login request
        if (activity.equalsIgnoreCase("nfc") || activity.equalsIgnoreCase("pass")) {
            activeSession = true;
        } else {
            activeSession = checkActiveSession();
        }

        if (activeSession) {
            // Different methods for each activity header field
            if (activity.equalsIgnoreCase("nfc")) {
                handleNFC();
            } else if (activity.equalsIgnoreCase("pass")) {
                handlePass();
            } else if (activity.equalsIgnoreCase("map")) {
                handleMap();
            } else if (activity.equalsIgnoreCase("video")) {
                handleVideo();
            } else if (activity.equalsIgnoreCase("contact")) {
                handleContacts();
            } else if (activity.equalsIgnoreCase("file")){
                handleFile();
            } else if (activity.equalsIgnoreCase("vote")) {
                handelVote();
            } else if (activity.equalsIgnoreCase("receive")) {
                handelReceive();
            }else {
                System.out.println("ERROR! Wrong activity asked in processor");
            }
        } else {
            //User isn't active
            response = new Response(false).notActiveResponse();
        }
        return response;
    }

    private void handleFile() {
        response = new Response(true,true).buildNFCResponse();
    }


    private boolean checkActiveSession() {
        boolean active = false;

        // returns 1 or 0
        String query = "SELECT EXISTS (SELECT 1 FROM users WHERE sessionID=\'" + id + "\') AS active";
        try {
            // Get data from database
            JsonObject sqlData = (JsonObject) database.selectData(query).get(0);

            // Grab the users permission level and compare to required permission level
            int access = sqlData.get("active").getAsInt();
            switch (access) {
                case 0:
                    // No access
                    active = false;
                    break;
                case 1:
                    // Access
                    active = true;
                    break;
                default:
                    // Doesn't happen
                    break;
            }
        } catch (SQLException sqle) {
            System.err.println("Error in your SQL query");
            sqle.printStackTrace();
        }
        return active;
    }

    private void handelVote (){

    }

    private void handelReceive (){

    }

    private void handlePass() {
        // Check if both NFCid and password is stored in database
        if (action.equalsIgnoreCase("get_pass")) {
            // Get the user's id and password
            int NFCid = data.get("NFCid").getAsInt();
            String pass = data.get("pass").getAsString();

            // Returns a Name if passwords match, else returns an empty set
            String query = "SELECT Name FROM users WHERE EXISTS (SELECT * FROM users WHERE (NFCid, pass) = (" + NFCid + ", \'" + pass + "\')) AND (NFCid, pass) = (" + NFCid + ", \'" + pass + "\')";

            try {
                // Get data from database and save as JsonArray
                JsonArray sqlResult = database.selectData(query);

                // User entered wrong password!
                if (sqlResult.size() == 0) {
                    // Active, but no access
                    response = new Response(true,false).buildNFCResponse();

                    System.out.println("User seems to have entered wrong password");
                }
                // User exists in the database!
                else {
                    //Add a random sessionID to the database and to client message
                    String sessionID = String.valueOf(UUID.randomUUID());

                    JsonObject nameObj = sqlResult.get(0).getAsJsonObject();
                    String name = nameObj.get("name").getAsString();
                    // Active and access
                    response = new Response(true, true, name, sessionID).buildPassResponse();

                    String addSessionID = "UPDATE users set sessionID = \'" + sessionID + "\' where (NFCid, pass) = (" + NFCid + ", \'" + pass + "\')";
                    database.updateData(addSessionID);
                }

            } catch (SQLException e) {
                System.err.println("Error in your SQL query");
                e.printStackTrace();
            }
        } else {
            System.out.println("ERROR! Wrong action in NFCHandler!");
        }
    }

    private void handleContacts() {
        String getPerm = "SELECT Permission FROM users WHERE sessionID=\'" + id + "\'";

        if (action.equalsIgnoreCase("get")) {
            try {
                // Get data from database and save as JsonArray
                JsonArray permData = database.selectData(getPerm);
                JsonObject dataObject = (JsonObject) permData.get(0);

                // Grab the users permission level and gets contact info accordingly
                int userPermission = dataObject.get("permission").getAsInt();
                String query = "";
                switch (userPermission) {
                    case 0:
                        query = "SELECT * FROM View_0";
                        break;
                    case 1:
                        query = "SELECT * FROM View_1";
                        break;
                    case 2:
                        query = "SELECT * FROM contacts";
                        break;
                    default:
                        System.out.println("User has invalid permission");
                }

                JsonArray sqlResult = database.selectData(query);

                response = new Response(true, sqlResult, String.valueOf(userPermission)).buildContactResponse();

            } catch (SQLException sqle) {
                System.err.println("Error in your SQL query");
            }

        } else if (action.equalsIgnoreCase("delete")) {
            String deleteKey = data.get("deletekey").getAsString();
            String query = "DELETE FROM Contacts WHERE ssn = " + deleteKey;
            try {
                database.updateData(query);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }

    }

    /**
     * Handles the NFC requests
     * If a "get" command, returns user info
     */
    private void handleNFC() {
        if (action.equalsIgnoreCase("get_nfc")) {

            // Get the user's NFCid from the (1) data object
            int NFCid = data.get("NFCid").getAsInt();
            // This returns 0 or 1
            String query = "SELECT EXISTS (SELECT * FROM users WHERE NFCid=\'" + NFCid + "\') AS OK";
            try {
                // Get data from database and save as JsonArray
                JsonArray sqlResult = database.selectData(query);
                JsonObject dataObject = (JsonObject) sqlResult.get(0);
                int access = dataObject.get("ok").getAsInt();

                // Give access if the user exists in the database
                if (access == 1) {
                    response = new Response(true,true).buildNFCResponse();
                } else {
                    response = new Response(true,false).buildNFCResponse();
                }
            } catch (SQLException e) {
                System.err.println("Error in your SQL query");
                e.printStackTrace();
            }

        } else {
            System.out.println("ERROR! Wrong action in NFCHandler!");
        }
    }

    /**
     * Handles the Map requests.
     * If it's a get action we return all events to the map
     * If it's a add action we add the new events to the database
     */
    private void handleMap() {
        // Get all markers from the database
        if (action.equalsIgnoreCase("get")) {
            String query = "SELECT * FROM event";
            try {
                // selects all events from the database
                JsonArray sqlResult = database.selectData(query);
                // Adds the result to the servers response
                response = new Response(true, sqlResult).buildMapResponse();
            } catch (SQLException e) {
                System.err.println("Error in your SQL syntax");
                e.printStackTrace();
            }
            // Add markers to the database
        } else if (action.equalsIgnoreCase("add")) {
            String lat, lon, query;
            String event = "";
            query = "INSERT INTO event (lat, lon, event) VALUES ";

            // Loop through all markers
            for (int i = 0; i < dataArray.size(); i++) {
                data = dataArray.get(i).getAsJsonObject();
                lat = data.get("lat").getAsString();
                lon = data.get("lon").getAsString();
                event = data.get("event").getAsString();

                // Don't add the comma if it's the first marker
                if (i != 0) {
                    query += ", ";
                }
                query += "(" + lat + "," + lon + "," + "\'" + event + "\'" + ")";
            }
            // Update the event if it already exists
            query += " ON DUPLICATE KEY UPDATE event = \'" + event + "\'";
            try {
                // try to insert, if we have duplicate coordinates in the db we update the row instead
                database.updateData(query);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // Succeeded
            response = new Response(true, "true").buildSucceededResponse();
//            responseObject.addProperty("successful",true);
            // Delete markers from the list
        } else if (action.equalsIgnoreCase("delete")) {
            String lat, lon, query;

            // Loop through all markers
            query = "DELETE FROM event WHERE (lat,lon) IN (";
            for (int i = 0; i < dataArray.size(); i++) {
                data = dataArray.get(i).getAsJsonObject();
                lat = data.get("lat").getAsString();
                lon = data.get("lon").getAsString();
                if (i != 0) {
                    query += ", ";
                }
                query += "(\'" + lat + "\',\'" + lon + "\')";
            }
            query += ")";
            try {
                database.updateData(query);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // Succeeded
            response = new Response(true, "true").buildSucceededResponse();
//            responseObject.addProperty("successful",true);
        }
    }

    private void handleVideo() {
    }
}

class Response {
    boolean active, access;
    String[] args;
    JsonObject data = new JsonObject();
    JsonObject response = new JsonObject();
    JsonArray dataArray = new JsonArray();

    public Response(boolean active, String... args){
        this.active = active;
        this.args = args;
    }
    public Response(boolean active, JsonArray dataArray, String... args){
        this.active = active;
        this.args = args;
        this.dataArray = dataArray;
    }
    public Response(boolean active, boolean access, String... args){
        this.active = active;
        this.access = access;
        this.args = args;
    }

    public String buildNFCResponse(){
        response.addProperty("active",active);
        response.addProperty("access",access);
        return response.toString();
    }
    public String buildPassResponse(){
        response.addProperty("active",active);
        response.addProperty("access",access);
        data.addProperty("name",args[0]);
        data.addProperty("sessionid",args[1]);
        dataArray.add(data);
        response.add("data",dataArray);
        return response.toString();
    }

    public String buildContactResponse(){
        response.addProperty("active",active);
        response.addProperty("permission",args[0]);
        response.add("data",dataArray);
        return response.toString();
    }

    public String buildMapResponse(){
        response.addProperty("active",active);
        response.add("data",dataArray);
        return response.toString();
    }

    public String buildSucceededResponse(){
        response.addProperty("active",active);
        response.addProperty("succeeded",args[0]);
        return response.toString();
    }

    public String notActiveResponse(){
        response.addProperty("active",false);
        return response.toString();
    }

}
