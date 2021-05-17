package com.mycompany.ftp;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

class Server {

    public static void main(String[] args) {
        ServerSocket server = null;

        try {
            int numer = 0;
            server = new ServerSocket(2137);
            server.setReuseAddress(true);
            System.out.println("Serwer wystartował " + server.getLocalSocketAddress());
            while (true) {
                Socket client = server.accept();
                ClientHandler clientSock
                        = new ClientHandler(client);
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ClientHandler implements Runnable {

        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            PrintWriter out = null;
            BufferedReader in = null;
            DataOutputStream dos = null;
            FileInputStream fis = null;
            DataInputStream dis = null;
            FileOutputStream fos = null;
            try {
                out = new PrintWriter(
                        clientSocket.getOutputStream(), true);
                in = new BufferedReader(
                        new InputStreamReader(
                                clientSocket.getInputStream()));

                String line;
                while (true) {

                    line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    switch (line) {
                        case "dir":
                            File curDir = new File(".");
                            List<String> dir = getAllFiles(curDir);
                            out.flush();
                            out.println(dir);
                            break;
                        case "dl":
                            //Pobranie nazwy pliku od klienta i sprawdzenie czy jest na serwerze
                            Boolean filePresent;
                            String fileName;
                            while (true) {
                                fileName = in.readLine();
                                File curentDir = new File(".");
                                filePresent = false;
                                File[] filesList = curentDir.listFiles();
                                for (File temp : filesList) {
                                    if (temp.getName().equals(fileName)) {
                                        filePresent = true;
                                        break;
                                    }
                                }
                                if (!filePresent) {
                                    out.println("fnf");
                                } else {
                                    break;
                                }
                            }
                            out.println("ff");
                            System.out.println("Wysyłanie pliku");
                            File fileToSend = new File(fileName);
                            out.println(fileToSend.length());
                            dos = new DataOutputStream(clientSocket.getOutputStream());
                            fis = new FileInputStream(fileToSend);
                            byte[] buffer = new byte[4096];
                             {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            while (fis.read(buffer) > 0) {
                                    dos.write(buffer);
      

 
                            }
                            //out.flush();
                            fis.close();
                            System.out.println("Ukończono wysyłanie");

                            break;

                        case "rec":
                            //otrzymywanie pliku od klienta
                            dis = new DataInputStream(clientSocket.getInputStream());
                            String fileNameRec = in.readLine();
                            System.out.println("Otrzymano nowy plik: " + fileNameRec);
                            fos = new FileOutputStream(fileNameRec);
                            byte[] bufferRec = new byte[4096];
                            long filesize = Integer.valueOf(in.readLine());
                            System.out.println(filesize);
                            int read = 0;
                            int totalRead = 0;
                            long remaining = filesize;
                            while ((read = dis.read(bufferRec)) > 0) {
                                totalRead += read;
                                remaining -= read;
                                fos.write(bufferRec, 0, read);

                                if (totalRead >= filesize) {
                                    break;
                                }
                            }
                            System.out.println("Pobrano plik");
                            fos.close();
                            break;
                        default:
                            System.out.println("Nierozpoznana komenda");
                            break;
                    }
                }

            } catch (IOException e) {
                System.out.println("Zerwano połączenie");
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (dos != null) {
                        dos.close();
                    }
                    if (fis != null) {
                        fis.close();
                    }
                    if (dis != null) {
                        dis.close();
                    }
                    if (in != null) {
                        in.close();
                        System.out.println("Zakończono obsługę klienta " + clientSocket.getLocalAddress());
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private static List<String> getAllFiles(File curDir) {
            List<String> output = new LinkedList<String>();
            File[] filesList = curDir.listFiles();
            for (File f : filesList) {
                if (f.isDirectory()) {
                    //output.add(f.getName());
                }
                if (f.isFile()) {
                    String size = "";
                    if (f.length() < 1024) {
                        size = f.length() + "B";
                    } else if (f.length() < (1024 * 1024)) {
                        size = f.length() / 1024 + "kB";
                    } else if (f.length() < (1024 * 1024 * 1024)) {
                        size = f.length() / (1024 * 1024) + "mB";
                    }
                    output.add(f.getName() + " " + size);
                }
            }
            return output;
        }
    }
}
