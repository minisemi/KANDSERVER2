package Server_;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Fredrik on 2016-03-16.
 *
 * Server_ class running a multithreaded server
 * Accepts multiple client connections
 */

public class Server {


    public static void main(String[] args) throws Exception {
        int PORT = 9001;

        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("Server is up!");
        System.out.println("Waiting for client connection at the port: " + PORT);

        try {
            while (true) {
                new ConnectionListener(serverSocket.accept()).start();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        System.out.println("closing server socket...");
        serverSocket.close();

    }

    static class ConnectionListener extends Thread {

        private Socket socket;
        private Processor processor;
        public boolean isFile;

        private int ret = 0;
        private int offset = 0;
        private int fileSize = 20971520;

        public ConnectionListener(Socket socketValue) {
            socket = socketValue;
        }

        public void run() {

            try {
                PrintWriter out = null;
                BufferedReader bin;
                InputStream in;
                try {
                    System.out.println("Connection received from: " + socket.getInetAddress().getHostAddress());

                    in = socket.getInputStream();
                    bin = new BufferedReader(new InputStreamReader(in));

                    if(isFile){
                        processFile(in);
                    } else {
                        processJson(bin);
                    }
                    isFile = false;
                } catch (IOException ioe) {
                    ioe.printStackTrace();

                }
                if (out != null) {
                    out.close();
                }
                socket.close();
            } catch (IOException ex) {
            }
        }
        private void processJson(BufferedReader in){
            try {
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                String clientMessage = in.readLine();

                processor = new Processor();
                String reply = processor.processMessage(clientMessage);
                System.out.println("Reply to client: " + reply);
                out.print(reply);
                out.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void processFile(InputStream in){

            try {
                byte[] buffer = new byte[fileSize];

                OutputStream oS = new FileOutputStream("/srv/DENO/Received files/theFile");
                BufferedOutputStream bOS = new BufferedOutputStream(oS);

                System.out.println("Receiving...");

                //following lines read the input slide file byte by byte
                while((ret = in.read(buffer,offset,(buffer.length - offset))) > 0){
                    offset += ret;
                    if(offset > buffer.length){
                        break;
                    }
                }

                bOS.write(buffer,0,offset);
                bOS.flush();
                bOS.close();
                in.close();

                System.out.println("DONE!");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}