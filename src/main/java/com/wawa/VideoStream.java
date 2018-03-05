package com.wawa;

import com.wawa.common.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class VideoStream {


    public static void main(String[] args) {

        try {
            File file = new File("D:/output.mp4");
            String pullStream = "ffmpeg -f dshow -i video=\"e2eSoft VCam\" -r 1 -framerate 10 -video_size 160x120 -pix_fmt yuv420p -c:v libx264 -b:v 2k -bufsize 1k -vprofile baseline -tune zerolatency -f rawvideo -";
            Process process = Runtime.getRuntime().exec(pullStream);
            while(process.isAlive()) {
                System.out.println("alive");
                //System.out.println(process.waitFor());

                /*StringBuilder sb = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while((line = reader.readLine()) != null) {

                }
                System.out.println(sb);*/
                writeByteToHex(process.getInputStream());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void writeByteToHex(InputStream input) {
        byte[] tmp = new byte[2048];
        try {
            int len;
            while ((len = input.read(tmp, 0, tmp.length)) > -1) {
                System.out.println(StringUtils.bytes2HexString(tmp, len));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
