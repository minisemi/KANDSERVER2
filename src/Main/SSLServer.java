package Main;

import javax.net.ssl.*;
import java.io.*;
import java.net.ServerSocket;
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
        int PORT = 9003;

        //SSLServerSocket serverSocket = getConnection(PORT);
        ServerSocket serverSocket = new ServerSocket(PORT);
        //serverSocket.setNeedClientAuth(false);
        System.out.println("Server is up!");
        System.out.println("Waiting for client connection at the port: " + PORT);
        VoteReceiver voteReceiver = new VoteReceiver();

        try {
            while (true) {
                new ConnectionListener(serverSocket.accept(), voteReceiver).start();
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
        VoteReceiver voteReceiver;

        public ConnectionListener(Socket socketValue, VoteReceiver voteReceiver) {
            this.voteReceiver = voteReceiver;
            socket = socketValue;
        }

        public void run() {

            try {
                PrintWriter out = null;
                BufferedReader in;
                Socket outSocket;
                try {
                    System.out.println("Connection received from: " + socket.getInetAddress().getHostAddress());

                    in = new BufferedReader(new InputStreamReader(
                            socket.getInputStream()));

                    String clientMessage = in.readLine();
                    while (!clientMessage.isEmpty()){
                      //  System.out.println("RECEIVED MESSAGE: "+clientMessage);
                        processor = new Processor(clientMessage, voteReceiver, socket);
                        String reply = processor.processMessage();

                            switch (processor.getAction()) {
                                case ("receive"):
                                    break;

                                case ("vote"):
                                  //  System.out.println("Reply to vote server: " + reply);
                                    //System.out.println("IP: "+voteReceiver.getReceiverIP()+ ", PORT: "+ voteReceiver.getReceiverPort());
                                    /*outSocket = new Socket(voteReceiver.getReceiverIP(), voteReceiver.getReceiverPort());
                                    out = new PrintWriter(new OutputStreamWriter(outSocket.getOutputStream()));*/
                                    outSocket = new Socket("2016-4.itkand.ida.liu.se", 9004);
                                    out = new PrintWriter(new OutputStreamWriter(outSocket.getOutputStream()));
                                    out.print(reply);
                                    out.flush();
                                    break;

                                default:
                                    System.out.println("Reply to client: " + reply);
                                    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                                    out.print(reply);
                                    out.flush();
                                    break;
                            }
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