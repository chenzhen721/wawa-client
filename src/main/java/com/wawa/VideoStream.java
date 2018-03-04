package com.wawa;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class VideoStream {


    public static void main(String[] args) {

        try {
            File file = new File("D:/output.mp4");
            String pullStream = "ffmpeg -f dshow -i video=\"ICT Camera\" -framerate 5 -video_size 320x240 -pix_fmt yuv420p -c:v libx264 -b:v 100k -bufsize 100k -vprofile baseline -tune zerolatency -f rawvideo -";
            Process process = Runtime.getRuntime().exec(pullStream);
            while(process.isAlive()) {
                System.out.println("alive");
                //System.out.println(process.waitFor());

                StringBuilder sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while((line = reader.readLine()) != null) {

                }
                System.out.println(sb);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}
