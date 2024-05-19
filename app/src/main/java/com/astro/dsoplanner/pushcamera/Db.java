package com.astro.dsoplanner.pushcamera;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;


/**
 * SQLite wrapper
 *
 * @author leonid
 */
public class Db {
    private static final String FILE = "dbtmp.txt";

    String dbname;
    String dir;
    PrintWriter pw;
    boolean begin = false;

    /**
     * @param dbname
     * @param dir    - with /
     */
    public Db(String dir, String dbname) {
        this.dbname = dbname;
        this.dir = dir;
    }

    /**
     * start transaction
     */
    public void start() throws Exception {
        if (begin) throw new Exception("began already");
        begin = true;
        pw = new PrintWriter(new FileOutputStream(new File(dir, FILE)));


    }

    public List<String[]> end(int width) throws Exception {
        if (!begin) throw new Exception("not begun");
        begin = false;

        pw.close();

        return exec(".read " + dir + FILE, width);
    }

    public List<String[]> end() throws Exception {
        return end(-1);
    }

    /**
     * @param sql
     * @param width - number of params in output to throw exception if wrong
     *              use -1 to skip control
     * @return
     */
    public List<String[]> exec(String sql, int width) throws Exception {
        String[] command = new String[]{"sqlite3", dir + dbname, sql};
        Runtime runTime = Runtime.getRuntime();
        Process process = runTime.exec(command);
        List<String> list = getStringList(process, "");

        List<String[]> l = new ArrayList<String[]>();
        for (String s : list) {
            String[] arr = mysplit(s);//s.split("\\|");
            l.add(arr);
        }
        return l;
    }

    public List<String[]> exec(String sql) throws Exception {
        return exec(sql, -1);
    }

    public void cd(String dir) throws Exception {
        String[] command = new String[]{"cd", dir};
        Runtime runTime = Runtime.getRuntime();
        Process process = runTime.exec(command);
        List<String> list = getStringList(process, "");
    }

    public void addCommand(String sql) throws Exception {
        if (!begin) throw new Exception("not begun");
        pw.println(sql);
    }

    private String[] mysplit(String s) {
        List<String> list = new ArrayList<String>();
        String s2 = "";
        for (char c : s.toCharArray()) {
            if (c == '|') {
                list.add(s2);
                s2 = "";
            } else
                s2 += c;
        }
        list.add(s2);
        String[] arr = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            arr[i] = list.get(i);
        }
        return arr;
    }

    private List<String> getStringList(Process process, String line) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String s;
        List<String> list = new ArrayList<String>();

        while ((s = reader.readLine()) != null) {
            list.add(s);
        }

        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        while ((s = errorReader.readLine()) != null) {
            System.out.println(s + " " + line);
        }
        reader.close();
        errorReader.close();
        return list;
    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

}
