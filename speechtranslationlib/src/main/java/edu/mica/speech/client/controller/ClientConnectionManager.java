package edu.mica.speech.client.controller;

/**
 * Created by thinh on 21/03/2017.
 */

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.File;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;

import edu.mica.speech.client.speechprocessor.SpeechProcessor;

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

    public void setSever(String sever) throws UnknownHostException {
        this.sever = sever;
        setIp();
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }
    public HashMap<String,String> translate(String tempFile) throws IOException {
        HashMap<String,String> result = new HashMap<String, String>();
        String resultRecognition = "fail";
        String resultTranslation = "fail";
        File soundefile = new File(tempFile);
        if (isSever) {

            BufferedReader bufferedReader = null;
            try {
                byte [] mybytearray  = new byte [(int)soundefile.length()];
                clientsocket = new Socket(ip, port);
                FileInputStream fis = new FileInputStream(soundefile);
                BufferedInputStream bis = new BufferedInputStream(fis);
                bis.read(mybytearray,0,mybytearray.length);
                // object in used to received data from sever
                in = new DataInputStream(clientsocket.getInputStream());
                // object out used to send data to sever
                out = new DataOutputStream(clientsocket.getOutputStream());
                bufferedReader = new BufferedReader(new InputStreamReader(in));

                //while ((count = fis.read(buffer)) != -1)
                //out.writeInt((int)soundefile.length());
                out.writeInt(mybytearray.length);
                    out.write(mybytearray,0,mybytearray.length);
                out.flush();
                //out.writeUTF("");
                // in.reString received =adUTF();
                resultRecognition = in.readLine();
                resultTranslation = in.readLine();

                System.out.println("Received: " + resultRecognition);
                bufferedReader.close();
                fis.close();
                in.close();
                out.close();
                clientsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    throw  e;

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } finally {
                try {
                    result.put(SpeechProcessor.KEY_RECOGNITION,resultRecognition);
                    result.put(SpeechProcessor.KEY_TRANSATION,resultTranslation);

                    bufferedReader.close();
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

    public HashMap<String,String> translate(List<float[]> allFeatures) throws IOException {
        HashMap<String,String> result = new HashMap<String, String>();
        String resultRecognition = "fail";
        String resultTranslation = "fail";
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
                resultRecognition = bufferedReader.readLine();
                resultTranslation = bufferedReader.readLine();

                result.put(SpeechProcessor.KEY_RECOGNITION,resultRecognition);
                result.put(SpeechProcessor.KEY_TRANSATION,resultTranslation);
                System.out.println("Received: " + resultRecognition);
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

    private void setIp() throws UnknownHostException{
        ip = InetAddress.getByName(sever);
    }
}
