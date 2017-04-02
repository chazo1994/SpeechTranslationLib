package edu.mica.speech.client.controller;

/**
 * Created by thinh on 21/03/2017.
 */

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class ClientConnectionManager {
    private final String micaServer = "192.168.75.74";
    private final int micaPort = 9875;
    // port on sever
    private int port = micaPort;
    // host name or ip address
    private String sever = micaServer;
    private Socket clientsocket;
    private InetAddress ip;
    private boolean isSever = true;
    private DataInputStream in;
    private DataOutputStream out;

    public ClientConnectionManager() throws UnknownHostException {
        setIp();
    }

    public ClientConnectionManager(String host) throws UnknownHostException {
        sever = host;
        setIp();
    }

    public ClientConnectionManager(String host, int port) throws UnknownHostException{
        this.port = port;
        sever = host;
        setIp();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getSever() {
        return sever;
    }

    public void setSever(String sever) {
        this.sever = sever;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public String translate(List<float[]> allFeatures) throws IOException {
        String result = "fail";
        if (isSever) {

            BufferedReader bufferedReader;
            PrintWriter printWriter;
            try {
                clientsocket = new Socket(ip, port);
                // object in used to received data from sever
                in = new DataInputStream(clientsocket.getInputStream());
                // object out used to send data to sever
                out = new DataOutputStream(clientsocket.getOutputStream());

                bufferedReader = new BufferedReader(new InputStreamReader(in));
                printWriter = new PrintWriter(out,true);
                int numberFeatureLine = allFeatures.size();
                printWriter.println(String.valueOf(numberFeatureLine));
                for(float[] feature: allFeatures){
                    String line = "";
                    for(float value: feature){
                        line = line + " " + value;
                    }
                    line = line.trim();
                    printWriter.println(line);
                }
                printWriter.println("end");
                //out.writeUTF("");
                //String received = in.readUTF();
                result = bufferedReader.readLine();
                System.out.println("Received: " + result);
                bufferedReader.close();
                printWriter.close();

            } catch (IOException e) {
                e.printStackTrace();
                try {
                    throw  e;

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } finally {
                try {


                    in.close();
                    out.close();
                    clientsocket.close();

                    return result;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        } else {
            System.out.println("failed to resolve ip or host");
        }
        return null;
    }

    private void setIp() {
        try {
            ip = InetAddress.getByName(sever);
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            isSever = false;
            System.out.println("failed to resolve ip or host");
            e.printStackTrace();
        }
    }
}
