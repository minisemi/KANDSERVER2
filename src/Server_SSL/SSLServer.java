package Server_SSL;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by dennisdufback on 2016-03-16.
 */
public class SSLServer {

    public static void main(String[] args) throws Exception {
        int PORT = 9001;

        SSLServerSocket serverSocket = getConnection(PORT);
        serverSocket.setNeedClientAuth(true);
        System.out.println("Server_ is up!");
        System.out.println("Waiting for client connection at the port: " + PORT);

        try {
            while (true) {
                new ConnectionListener(serverSocket.accept()).start();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }
    public static SSLServerSocket getConnection(int port) throws IOException{
        String trustStorePass = "password";
        String keyStorePass = "password";

        try{
            // Load server private key
            KeyStore serverKeys = KeyStore.getInstance("JKS");
            serverKeys.load(new FileInputStream("/srv/DENO/certs/server.jks"), keyStorePass.toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(serverKeys, keyStorePass.toCharArray());

            // Setup trust store
            KeyStore trustKeys = KeyStore.getInstance("JKS");
            trustKeys.load(new FileInputStream("/srv/DENO/certs/servertruststore.jks"), trustStorePass.toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustKeys);

            //Context
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            SSLServerSocketFactory factory = context.getServerSocketFactory();
            return (SSLServerSocket)factory.createServerSocket(port);

        }catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////                                                        ////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    static class ConnectionListener extends Thread {

        private Socket socket;
        Processor processor;

        public ConnectionListener(Socket socketValue) {
            socket = socketValue;
        }

        public void run() {

            try {
                PrintWriter out = null;
                BufferedReader in;
                try {
                    System.out.println("Connection received from: " + socket.getInetAddress().getHostAddress());

                    in = new BufferedReader(new InputStreamReader(
                            socket.getInputStream()));
                    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

                    String clientMessage = in.readLine();
                    while (!clientMessage.isEmpty()){
                        processor = new Processor(clientMessage);
                        String reply = processor.processMessage();
                        System.out.println("Reply to client: " + reply);
                        out.print(reply);
                        out.flush();
                        clientMessage = "";
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();

                }
                if (out != null) {
                    out.close();
                }
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(SSLServer.class.getName()).log(
                        Level.SEVERE, null, ex);
            }
        }
    }
}