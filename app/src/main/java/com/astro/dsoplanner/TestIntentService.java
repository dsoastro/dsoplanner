package com.astro.dsoplanner;


import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;


public class TestIntentService extends IntentService {


    public static final int DSO_MAIN = 1;
    public static final int QUERY = 2;
    public static final int DIALOG = 3;
    public static final int SETTINGS_SYSTEM = 4;
    public static final int DB_MANAGER = 5;
    public static final int VDA = 6;
    public static final int GRAPH = 7;

    //actions
    public static final int QUERY_NGCIC = 100;
    public static final int DSO_MAIN_DSO_SELECTION = 101;
    public static final int FINISH = 102;
    public static final int QUERY_MESSIER = 103;
    public static final int QUERY_LOG = 104;
    public static final int QUERY_CALDWELL = 105;
    public static final int QUERY_H400 = 106;
    public static final int QUERY_SAC = 107;
    public static final int QUERY_UGC = 108;
    public static final int QUERY_PGC = 109;
    public static final int QUERY_LDN = 110;
    public static final int QUERY_BARNARD = 111;
    public static final int QUERY_LBN = 112;
    public static final int QUERY_SH2 = 113;
    public static final int QUERY_PK = 114;
    public static final int QUERY_ABELL = 115;
    public static final int QUERY_HCG = 116;
    public static final int QUERY_HAAS = 117;
    public static final int QUERY_WDS = 118;
    public static final int QUERY_COMETS = 119;
    public static final int QUERY_MP = 120;
    public static final int DIALOG_OK = 121;
    public static final int DIALOG_CANCEL = 122;
    public static final int DIALOG_TEXT_TESTTXT = 123;
    public static final int QUERY_EXPORT = 124;
    public static final int SS_OBJECTS_DB = 125;
    public static final int DSO_MAIN_SETTINGS = 126;
    public static final int DB_MANAGER_ADD = 127;
    public static final int DIALOG_TEXT_TEST2 = 128;
    public static final int DB_MANAGER_PRESS_TEST2 = 129;
    public static final int VDA_IMPORT = 130;
    public static final int QUERY_TEST2 = 131;
    public static final int GRAPH_START = 132;
    public static final int GRAPH_LOG = 133;
    public static final int QUERY_ITEM_CLICK = 134;
    public static final int QUERY_IMAGE_TEST = 135;

    public TestIntentService() {
        super("0");
    }

    private static int num = 1;

    public static int getTestNumber() {
        return num++;
    }

    private static void clearTestNumber() {
        num = 1;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!Global.TEST_MODE)
            return;
        try {
            clearTestNumber();
            initTestQuery();
            //1
            testQuery(QUERY_NGCIC, 11890);
            //2
            testQuery(QUERY_MESSIER, 110);
            //3
            testQuery(QUERY_CALDWELL, 111);
            //4
            testQuery(QUERY_H400, 398);
            //5
            testQuery(QUERY_SAC, 10054);
            //6
            testQuery(QUERY_UGC, 12934);
            //7
            testQuery(QUERY_PGC, 15000);
            //8
            testQuery(QUERY_LDN, 1787);
            //9
            testQuery(QUERY_BARNARD, 349);
            //10
            testQuery(QUERY_LBN, 1103);
            //11
            testQuery(QUERY_SH2, 313);
            //12
            testQuery(QUERY_PK, 1510);
            //13
            testQuery(QUERY_ABELL, 2712);
            //14
            testQuery(QUERY_HCG, 100);
            //15
            testQuery(QUERY_HAAS, 2271);
            //16
            testQuery(QUERY_WDS, 15000);
            //17
            testQuery(QUERY_COMETS, 727); //this test fails as the number of comets is different each time. Just make sure in the logs that it is around 800
            //18
            testQuery(QUERY_MP, 10000);
            //19
            testCustomDb();
            SettingsActivity.saveCatalogSelectionPrefs(getApplicationContext(), new HashSet<Integer>(), SettingsActivity.DSO_SELECTION);

            initTestQuery();
            testGraph();
            testImage();
        } catch (Exception e) {

        }
    }

    /**
     * @throws InterruptedException
     */
    private void initTestQuery() throws InterruptedException {
        Thread.sleep(3000);

        Context context = getApplicationContext();
        //id>0
        SettingsActivity.putSharedPreferences(Constants.SRA_ACTIVE_SEARCH_REQUEST, 0, context);
        SharedPreferences sh = PreferenceManager.getDefaultSharedPreferences(context);
        //advanced search
        sh.edit().putString(context.getString(R.string.select_search_type2), "1").commit();

        //make sure that dso selection list is not empty
        sh.edit().putBoolean(context.getString(R.string.select_catalog_messier), true).commit();
        SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, getApplicationContext());
        sendMessage(DSO_MAIN, DSO_MAIN_DSO_SELECTION, 0);
        Thread.sleep(15000);
        sendMessage(QUERY, FINISH, 0);
        Thread.sleep(2000);
    }

    /**
     * starting from dso selection
     *
     * @throws InterruptedException
     */
    private void testCustomDb() throws InterruptedException {
        //exporting barnard.db
        testQuery(QUERY_BARNARD, 349);
        sendMessage(DSO_MAIN, DSO_MAIN_DSO_SELECTION, 0);
        Thread.sleep(2000);

        sendMessage(QUERY, QUERY_EXPORT, 0);
        Thread.sleep(2000);

        sendMessage(DIALOG, DIALOG_TEXT_TESTTXT, 0);
        Thread.sleep(2000);

        sendMessage(DIALOG, DIALOG_OK, 0);
        Thread.sleep(5000);

        sendMessage(QUERY, FINISH, 0);
        Thread.sleep(2000);

        sendMessage(DSO_MAIN, DSO_MAIN_SETTINGS, 0);
        Thread.sleep(2000);
        sendMessage(SETTINGS_SYSTEM, SS_OBJECTS_DB, 0);
        Thread.sleep(2000);
        sendMessage(DB_MANAGER, DB_MANAGER_ADD, 0);
        Thread.sleep(2000);
        sendMessage(DIALOG, DIALOG_TEXT_TEST2, 0);
        Thread.sleep(2000);
        sendMessage(DIALOG, DIALOG_OK, 0);
        Thread.sleep(2000);
        sendMessage(DIALOG, DIALOG_CANCEL, 0);
        Thread.sleep(2000);
        sendMessage(DB_MANAGER, DB_MANAGER_PRESS_TEST2, 0);
        Thread.sleep(2000);
        sendMessage(VDA, VDA_IMPORT, 0);
        Thread.sleep(2000);
        sendMessage(DIALOG, DIALOG_OK, 0);
        Thread.sleep(45000);
        sendMessage(DB_MANAGER, FINISH, 0);
        Thread.sleep(2000);
        sendMessage(SETTINGS_SYSTEM, FINISH, 0);
        Thread.sleep(2000);
        testQuery(QUERY_TEST2, 349);
    }

    /**
     * Testing DSO selection databases
     *
     * @param message
     * @param count
     * @throws InterruptedException
     */
    private void testQuery(int message, int count) throws InterruptedException {
        SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, getApplicationContext());
        sendMessage(DSO_MAIN, DSO_MAIN_DSO_SELECTION, 0);
        Thread.sleep(2000);

        sendMessage(QUERY, message, 0);
        Thread.sleep(15000);
        sendMessage(QUERY, QUERY_LOG, count);
        sendMessage(QUERY, FINISH, 0);
        Thread.sleep(2000);
    }

    /**
     * assume that initTestQuery already run
     *
     * @throws InterruptedException
     */
    private void testGraph() throws InterruptedException {

        SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, getApplicationContext());
        sendMessage(DSO_MAIN, DSO_MAIN_DSO_SELECTION, 0);
        Thread.sleep(2000);

        sendMessage(QUERY, QUERY_MESSIER, 0);
        Thread.sleep(5000);
        SettingsActivity.setAutoTimeUpdating(false);
        sendMessage(QUERY, QUERY_ITEM_CLICK, 0);
        Thread.sleep(2000);
        sendMessage(GRAPH, GRAPH_START, 0);
        Thread.sleep(10000);
        sendMessage(GRAPH, GRAPH_LOG, 0);
        Thread.sleep(2000);
        sendMessage(GRAPH, FINISH, 0);
        Thread.sleep(2000);
        sendMessage(QUERY, FINISH, 0);
        Thread.sleep(2000);
    }

    /**
     * assume that initTestQuery already run
     *
     * @throws InterruptedException
     */
    private void testImage() throws InterruptedException {
        SettingsActivity.putSharedPreferences(Constants.QUERY_UPDATE, true, getApplicationContext());
        sendMessage(DSO_MAIN, DSO_MAIN_DSO_SELECTION, 0);
        Thread.sleep(2000);

        sendMessage(QUERY, QUERY_MESSIER, 0);
        Thread.sleep(5000);
        sendMessage(QUERY, QUERY_IMAGE_TEST, 0);
        Thread.sleep(2000);
        sendMessage(QUERY, FINISH, 0);
        Thread.sleep(2000);


    }

    private void sendMessage(int activity, int action, int param) {
        Intent intent = new Intent(Constants.TEST_BROADCAST);
        intent.putExtra(Constants.TEST_ACTIVITY, activity);
        intent.putExtra(Constants.TEST_ACTIVITY_ACTION, action);
        intent.putExtra(Constants.TEST_ACTIVITY_PARAM, param);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public static void print(String s) {
        File f = new File(Global.exportImportPath, "testing.txt");
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(new FileOutputStream(f, true));
            pw.println(s);

        } catch (Exception e) {
        } finally {
            try {
                pw.close();
            } catch (Exception e) {
            }
        }

    }

}
