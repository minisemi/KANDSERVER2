package Main;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.scene.control.RadioButton;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
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
    FileEncryption decryptionMix;
    FileEncryption decryptionReceiver;
    File in1;
    File in2;
    File encrypted1;
    File encrypted1_1;
    File encrypted2;
    File encrypted2_1;
    File decrypted1;
    File decrypted1_1;
    File rsaPublicKeyReceiver;
    File rsaPublicKeyMix;
    File encryptedAesKeyReceiver;
    File rsaPrivateKeyMix;
    File encryptedAesKeyMix;
    File rsaPrivateKeyReceiver;
    FileEncryption encryption;
    Socket socket;
    String receiverIP;
    int receiverPort;
    VoteReceiver votereceiver;
    FileUtils fileUtils;

    /**
     *
     */

    public Processor(String jsonRequest, VoteReceiver voteReceiver) {
        database = new Database();
        response = "";
        this.votereceiver = voteReceiver;

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
    }


    /**
     * Process the message received from a client and makes
     * different request depending on what the message asks for
     */
    public String processMessage() {

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
            } else if (activity.equalsIgnoreCase("vote")){
                handleVote();
            } else if (activity.equalsIgnoreCase("receive")){
                handleReceive();
            } else {
                System.out.println("ERROR! Wrong activity asked in processor");
            }
        } else {
            //User isn't active
            response = new Response(false).notActiveResponse();
        }
        return response;
    }

    private void handleVote (){
        System.out.println("JSONMESSAGE: "+data.toString());
        //String encryptedMessage = data.get("message").toString();
        //String encrypedAesKey = data.get("aeskey").toString();
        fileUtils = new FileUtils();
        //String [] parts = jsonMessage.split("\"?,?\"[a-z]*\":\"");
        //JsonParser parser = new JsonParser();
        //JsonObject jo = (JsonObject) parser.parse(encryptedMessage);
        //String activity = parts [1];


            encrypted1 = new File("/srvakf/srvakf2/KANDSERVER2/encrypted1.txt");
            decrypted1 = new File("/srvakf/srvakf2/KANDSERVER2/decrypted1.txt");
            encryptedAesKeyMix = new File("/srvakf/srvakf2/KANDSERVER2/encryptedAesKeyMix.txt");
            rsaPrivateKeyMix = new File("/srvakf/srvakf2/KANDSERVER2/privateSender.der");
            rsaPublicKeyMix = new File("/srvakf/srvakf2/KANDSERVER2/publicSender.der");
            rsaPublicKeyReceiver = new File("/srvakf/srvakf2/KANDSERVER2/publicReceiver.der");
            encryptedAesKeyReceiver = new File("/srvakf/srvakf2/KANDSERVER2/encryptedAesKeyReceiver.txt");
            encrypted1_1 = new File("/srvakf/srvakf2/KANDSERVER2/encrypted1_1.txt");
            in1 = new File("/srvakf/srvakf2/KANDSERVER2/In1.txt");
            //String encryptedAesKey = parts [3].replace("\"}","");
            //System.out.println("AESKEY: "+encryptedAesKey);

            try {
                encryption = new FileEncryption();
                encryption.makeKey();
                encryptMix(encrypted1, encrypted1_1, encryptClient(in1, encrypted1));

               /* BufferedWriter writer = new BufferedWriter(new FileWriter(encryptedAesKeyMix, false ));

            writer.write(encryptedAesKey);
            writer.close();
                System.out.println("AESKEY: "+fileUtils.readFileToString(encryptedAesKeyMix));
            encryption.loadKey(encryptedAesKeyMix, rsaPrivateKeyMix);
                String encryptedMessage = jo.get("message").toString();
                //String encryptedMessage = parts [2];
                System.out.println(encryptedMessage);
                writer = new BufferedWriter(new FileWriter(encrypted1, false ));
                writer.write(encryptedMessage);
                writer.close();
            encryption.decrypt(encrypted1, decrypted1);
                jsonMessage = encryption.getJsonString(decrypted1).replace("RANDOM_SALT", "");
                receiverIP = votereceiver.getReceiverIP();
                receiverPort = votereceiver.getReceiverPort();
                System.out.println(jsonMessage);
                System.out.println(receiverIP);
                System.out.println(receiverPort);
                //ClientSender clientSender =
                //        new ClientSender(mClientInfo, jsonMessage, receiverIP, receiverPort);
                //clientSender.start();*/
                String jsonMessage = "hej";
                response = new Response(true, jsonMessage).buildVoteResponse();



       // } catch (IOException i){
         //   i.printStackTrace();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }

    }

    private void handleReceive (){
        receiverIP = socket.getInetAddress().getHostAddress();
        receiverPort = socket.getPort();
        System.out.println(receiverIP);
        System.out.println(receiverPort);
        votereceiver.setReceiverIP(receiverIP);
        votereceiver.setReceiverPort(receiverPort);


    }

    public String getAction () {
        return action;
    }

    public String encrypt (File in, File out, File rsaPublicKey, File encryptedAesKey) {
        try {

            String encryptedText1 = fileUtils.readFileToString(in);


            encryption.saveKey(encryptedAesKey, rsaPublicKey);
            encryption.encrypt(in, out);


            String encryptedText = fileUtils.readFileToString(out);

            //BufferedReader brM1 = new BufferedReader(new InputStreamReader(new FileInputStream(out)));


            return encryptedText;
        } catch (GeneralSecurityException e){
            e.printStackTrace();
        } catch (IOException i){
            i.printStackTrace();
        }
        return null;
    }

    public void decryptMix (File in, File out){
        try {
            String encryptedText1 = fileUtils.readFileToString(in);;

            decryptionMix.loadKey(encryptedAesKeyMix, rsaPrivateKeyMix);
            decryptionMix.decrypt(in, out);

            String encryptedText = fileUtils.readFileToString(out);;

        } catch (IOException i){
            i.printStackTrace();
        } catch (GeneralSecurityException e){
            e.printStackTrace();
        }
    }

    public void decryptReceiver (File in, File out){
        try {
            String encryptedText1 = fileUtils.readFileToString(in).replace("RANDOM_SALT","");
            //JsonParser parser = new JsonParser();
            //JsonObject jo = (JsonObject)parser.parse(encryptedText1);
            String [] parts = encryptedText1.split("\"?,?\"[a-z]*\":\"");
            //String encryptedMessage = jo.get("message").getAsString().toLowerCase();
            String encryptedMessage = parts[2];
            String [] encryptedKeyParts = parts [3].split("\"}");
            //String encryptedKey = jo.get("aeskey").getAsString().toLowerCase();
            String encryptedKey = parts[3].replace("\"}", "");
            BufferedWriter writer = new BufferedWriter(new FileWriter(encryptedAesKeyReceiver, false /*append*/));
            writer.write(encryptedKey);
            writer.close();
            decryptionReceiver.loadKey(encryptedAesKeyReceiver, rsaPrivateKeyReceiver);
            writer = new BufferedWriter(new FileWriter(in, false /*append*/));
            writer.write(encryptedMessage);
            writer.close();
            decryptionReceiver.decrypt(in, out);
            String encryptedText = fileUtils.readFileToString(out);
        } catch (IOException i){
            i.printStackTrace();
        } catch (GeneralSecurityException e){
            e.printStackTrace();
        }
    }

    public String encryptClient(File vote, File encryptedVote){

        String encryptedText = encrypt(vote, encryptedVote, rsaPublicKeyReceiver, encryptedAesKeyReceiver);
        String encryptedAesKey = encryption.getEncryptedAesKey(encryptedAesKeyReceiver);
        //String jsonString = "{\"address\":\"345.2534.64.65\",\"message\":\""+encryptedText+"\",\"aeskey\":\""+encryptedAesKey+"\"}";
        JsonObject params = new JsonObject();
        params.addProperty("address", "534.23.5");
        params.addProperty("message", encryptedText);
        params.addProperty("aeskey", encryptedAesKey);
        return params.toString();
    }

    public String encryptMix (File in, File out, String jsonString) {

        try{

            String encryptedClientText = "RANDOM_SALT" + jsonString;
            BufferedWriter writer = new BufferedWriter(new FileWriter(in, false /*append*/));
            writer.write(encryptedClientText);
            writer.close();

            String encryptedText = encrypt(in, out, rsaPublicKeyMix, encryptedAesKeyMix);
            String encryptedAesKey = encryption.getEncryptedAesKey(encryptedAesKeyMix);
            //String jsonStringMix = "{\"activity\":\"vote\",\"message\":\""+encryptedText+"\",\"aeskey\":\""+encryptedAesKey+"\"}";

            JsonObject paramsMix = new JsonObject();
            paramsMix.addProperty("activity", "vote");
            paramsMix.addProperty("message", encryptedText);
            paramsMix.addProperty("aeskey", encryptedAesKey);
            return paramsMix.toString();

        }catch (IOException e) {
            e.printStackTrace();
        }
        return "";

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

        String query = "SELECT phonenr FROM users WHERE sessionID = '" + id + "'";
        try {
            JsonArray sqlResult = database.selectData(query);
            JsonObject dataObject = (JsonObject) sqlResult.get(0);
            String myNumer = dataObject.get("phonenr").getAsString();
            String toContact = data.get("tocall").getAsString();

            String callQuery = "SELECT readyToCall FROM users where phonenr = '" + toContact + "'";
            boolean ready;
            do {
                JsonArray callResult = database.selectData(callQuery);
                System.out.println("ARRAY: " + callResult);
                JsonObject callObject = (JsonObject) callResult.get(0);
                ready = callObject.get("readytocall").getAsBoolean();

                System.out.println("Active: " + callResult);
                if (!ready) {
                    Thread.sleep(3000);
                }
            } while(!ready);

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    public String buildVoteResponse(){
        response.addProperty("active", active);
        response.addProperty("message", args[0]);
        System.out.println("RESPONSE TO CLIENT: "+response.toString());
        return response.toString();
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
