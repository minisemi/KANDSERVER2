package Connection;

/**
 * Created by dennisdufback on 2016-03-19.
 */

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.*;

/**
 * Created by dennisdufback on 2016-03-16.
 * http://notetipsblog.com/NoteTipsBlog/articles/java/security/ssl_socket_client_server_2.html
 */
public class SSLRequester {

//    private static final String HOST = "localhost";
    private static final String HOST = "2016-4.itkand.ida.liu.se";
    private static final int PORT = 9001;

    public static void main(String[] args) throws Exception {

        System.setProperty("javax.net.ssl.trustStore",
                "/Users/dennisdufback/Documents/Programmering/certificates/client/clienttruststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "hallonsorbet");

        SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket s = (SSLSocket) sf.createSocket(HOST, PORT);
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
            ioe.printStackTrace();
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