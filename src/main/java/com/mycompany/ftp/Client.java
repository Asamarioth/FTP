package com.mycompany.ftp;

import java.io.*;
import java.net.*;
import java.util.*;

class Client {

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        String command = "";

        //Pętla do wprowadzenia adresu serwera i połączenia z nim
        while (true) {

            System.out.println("Wprowadź adres serwera: ");
            String serv_adres = scanner.nextLine();

            try (Socket socket = new Socket(serv_adres, 2137)) {
                //Inicjalizcja strumieni do zapisu i odczytu               
                PrintWriter out = new PrintWriter(
                        socket.getOutputStream(), true);
                BufferedReader in
                        = new BufferedReader(new InputStreamReader(
                                socket.getInputStream()));
                DataInputStream dis = null;
                FileOutputStream fos = null;
                //Pętla menu
                while (true) {
                    System.out.println("1.Wypisz zawartość serwera\n"
                            + "2.Wypisz zawartość klienta\n"
                            + "3.Pobierz plik\n"
                            + "4.Zakończ program");
                    command = scanner.next();
                    switch (command) {
                        case "1":
                            out.println("dir");
                            out.flush();
                            String dirOut = in.readLine();
                            parseAndPrintDirResult(dirOut);
                            System.out.println("--Wpisz dowolny znak aby kontynuować...--");
                            scanner.next();
                            break;
                        case "2":
                            File curDir = new File(".");
                            List<String> files = getAllFiles(curDir);
                            for (String token : files) {
                                System.out.println(token);
                            }
                            System.out.println("--Wpisz dowolny znak aby kontynuować...--");
                            scanner.next();
                            break;
                        case "3":
                            String fileName = "";
                            out.println("dl");
                            out.flush();
                            while (true) {
                                System.out.println("Podaj nazwę pliku: ");
                                fileName= scanner.nextLine();
                                out.println(fileName);

                                if (in.readLine().equals("fnf")) {
                                    System.out.println("Nie znaleziono pliku, spróbuj ponownie");
                                } else {
                                    break;
                                }
                            }
                            dis = new DataInputStream(socket.getInputStream());
                            fos = new FileOutputStream(fileName);
                            byte[] buffer = new byte[4096];
                            int filesize = Integer.valueOf(in.readLine()); // Send file size in separate msg
                            int read = 0;
                            int totalRead = 0;
                            int remaining = filesize;
                            while ((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                                totalRead += read;
                                remaining -= read;
                                System.out.println("read " + totalRead + " bytes.");
                                fos.write(buffer, 0, read);
                            }
                            System.out.println("Pobrano plik");
                            
                            break;
                        case "4":
                            scanner.close();
                            if (fos != null) {
                                fos.close();
                            }
                            if (dis != null) {
                                dis.close();
                            }
                            if (out != null) {
                                out.close();
                            }
                            if (in != null) {
                                in.close();
                            }
                            socket.close();
                            System.exit(0);
                            break;
                        default:
                            System.out.println("Wybrano złą opcję");
                            break;
                    }

                }
            } catch (IOException e) {
                System.out.println("Nieprawidłowy adres serwera lub serwer nie odpowiada!");
            }

        }
    }

    private static void parseAndPrintDirResult(String dir) {
        dir = dir.replace("[", "");
        dir = dir.replace("]", "");
        String[] result = dir.split(",");
        for (String token : result) {
            token = token.replace(",", "");
            token = token.trim();
            System.out.println(token);
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
