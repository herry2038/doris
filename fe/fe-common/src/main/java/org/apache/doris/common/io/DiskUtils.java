package org.apache.doris.common.io;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiskUtils {
    public static class Df {
        public String filesystem;
        public long blocks;
        /** 已使用 */
        public long used;
        /** 剩余可用 */
        public long available;
        /** 已使用率， 0-100的整数值 */
        public int useRate;
        /** 挂载目录， '/'表示挂载在根目录 */
        public String mountedOn;
    }

    public static Df df(String dir) {
        String os = System.getProperty("os.name");
        if ( os.startsWith("Windows") ) {
            Df df = new Df();
            df.available = Long.MAX_VALUE;
            return df;
        }

        Process process;
        try {
            process = Runtime.getRuntime().exec("df " + dir);
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Filesystem      1K-blocks       Used Available Use% Mounted on
            // /dev/sdc       5814186096 5814169712         0 100% /home/spy-sd/sdc
            String titleLine = reader.readLine();
            String dataLine = reader.readLine();
            if ( titleLine == null || dataLine == null ) {
                return null;
            }

            List<String> titles = getTitles(titleLine);
            String[] values = dataLine.split("\\s+");
            Df df = new Df();
            for(int i = 0; i < titles.size(); i ++) {
                String title = titles.get(i);
                switch (title.toLowerCase()) {
                    case "filesystem":
                        df.filesystem = values[i];
                        break;
                    case "1k-blocks":
                        df.blocks = Long.parseLong(values[i]);
                        break;
                    case "used":
                        df.used = Long.parseLong(values[i]);
                        break;
                    case "available":
                        df.available = Long.parseLong(values[i]);
                    case "use%":
                        df.useRate = Integer.parseInt(values[i].replace("%", ""));
                        break;
                    case "mountedon":
                        df.mountedOn = values[i];
                        break;
                }
            }
            return df;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static List<String> getTitles(String titlesLine) {
        List<String> titles = new ArrayList<>();
        String[] titleArray = titlesLine.split("\\s+");
        for(String title: titleArray) {
            if(title.equalsIgnoreCase("on")) {
                if(!titles.isEmpty()) {
                    int lastIdx = titles.size() - 1;
                    titles.set(lastIdx, titles.get(lastIdx) + "On");
                }
            } else {
                titles.add(title);
            }
        }
        return titles;
    }

}
