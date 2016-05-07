package Connection;

import java.io.*;
import java.net.*;

/**
 * Created by dennisdufback on 2016-03-16.
 */
public class Requester {

//        private static final String HOST = "localhost";
    private static final String HOST = "2016-4.itkand.ida.liu.se";
    private static final int PORT = 9001;

    public static void main(String[] args){

        Socket s = null;
        try {
            s = new Socket(HOST, PORT);
        } catch (UnknownHostException uhe) {
            System.out.println("Cant connect to server at 9000. Make sure it is running.");
            s = null;
            uhe.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        if (s == null) {
            System.exit(-1);
        }
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));

            out.write("What's up!");
            out.flush();
            System.out.println(in.readLine());
            out.flush();
        } catch (IOException ioe) {
            System.out.println("Exception during communication. Server probably closed connection.");
        } finally {
            try {
                out.close();
                in.close();
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
                out.close();
            }
        }
    }
}