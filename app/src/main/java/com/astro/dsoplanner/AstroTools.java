package com.astro.dsoplanner;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.log10;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.astro.dsoplanner.InputDialog.DType;
import com.astro.dsoplanner.base.AstroCatalog;
import com.astro.dsoplanner.base.AstroObject;
import com.astro.dsoplanner.base.ContourObject;
import com.astro.dsoplanner.base.CustomPoint;
import com.astro.dsoplanner.base.Exportable;
import com.astro.dsoplanner.base.HrStar;
import com.astro.dsoplanner.base.NgcicObject;
import com.astro.dsoplanner.base.Planet;
import com.astro.dsoplanner.base.Point;
import com.astro.dsoplanner.database.CrossDb;
import com.astro.dsoplanner.database.CustomDatabase;
import com.astro.dsoplanner.database.Db;
import com.astro.dsoplanner.database.DbListItem;
import com.astro.dsoplanner.database.NgcicDatabase;
import com.astro.dsoplanner.expansion.APKExpansion;
import com.astro.dsoplanner.expansion.FileIn;
import com.astro.dsoplanner.graph.GraphActivity;
import com.astro.dsoplanner.infolist.InfoList;
import com.astro.dsoplanner.infolist.InfoListImpl;
import com.astro.dsoplanner.infolist.InfoListLoader;
import com.astro.dsoplanner.infolist.InfoListLoaderImp;
import com.astro.dsoplanner.infolist.ListHolder;
import com.astro.dsoplanner.util.Holder2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HttpsURLConnection;


/**
 * Class for making astro calculations.
 */
public class AstroTools {


    private static final String BYTE_ARRAY = "byteArray";
    private static final String OBJ_TYPE = "objType";


    private static final String TAG = AstroTools.class.getSimpleName();
    public static final double hStars = -0.5667; //degrees, for stars and planets -34'
    public static final double hSun = -0.8333;    // -50'
    public static final double hMoon = 0.125;
    public static final double hCiv = -7; //end of civil twilighi
    public static final double hNav = -12;//end of navigational twilight
    public static final double hAstro = -18; //end of astronomical twilight


    //http://cdsarc.u-strasbg.fr/viz-bin/ftp-index?VI/42
    private static double[][] conBoundaries = {{0.0000, 360.0000, 88.0000, 84}, {120.0000, 217.5000, 86.5000, 84}, {315.0000, 345.0000, 86.1667, 84}, {270.0000, 315.0000, 86.0000, 84}, {0.0000, 120.0000, 85.0000, 20}, {137.5000, 160.0000, 82.0000, 11}, {0.0000, 75.0000, 80.0000, 20}, {160.0000, 217.5000, 80.0000, 11}, {262.5000, 270.0000, 80.0000, 84}, {302.5000, 315.0000, 80.0000, 34}, {0.0000, 52.6250, 77.0000, 20}, {172.5000, 203.7500, 77.0000, 11}, {248.0000, 262.5000, 75.0000, 84}, {302.5000, 310.0000, 75.0000, 20}, {119.5000, 137.5000, 73.5000, 11}, {137.5000, 170.0000, 73.5000, 34}, {195.0000, 248.0000, 70.0000, 84}, {46.5000, 51.2500, 68.0000, 18}, {306.2500, 310.0000, 67.0000, 34}, {170.0000, 180.0000, 66.5000, 34}, {0.0000, 5.0000, 66.0000, 20}, {210.0000, 235.0000, 66.0000, 84}, {353.7500, 360.0000, 66.0000, 20}, {180.0000, 202.5000, 64.0000, 34}, {202.5000, 216.2500, 63.0000, 34}, {347.5000, 353.7500, 63.0000, 20}, {91.5000, 105.0000, 62.0000, 11}, {300.0000, 306.2500, 61.5000, 34}, {308.0500, 309.0000, 60.9167, 20}, {105.0000, 119.5000, 60.0000, 11}, {119.5000, 126.2500, 60.0000, 83}, {296.5000, 300.0000, 59.5000, 34}, {300.0000, 308.0500, 59.5000, 20}, {343.0000, 347.5000, 59.0833, 20}, {0.0000, 36.5000, 58.5000, 18}, {291.2500, 296.5000, 58.0000, 34}, {25.5000, 28.6250, 57.5000, 18}, {36.5000, 46.5000, 57.0000, 18}, {46.5000, 47.5000, 57.0000, 11}, {334.7500, 343.0000, 56.2500, 20}, {75.0000, 91.5000, 56.0000, 11}, {210.5000, 216.2500, 55.5000, 83}, {216.2500, 291.2500, 55.5000, 34}, {47.5000, 50.0000, 55.0000, 11}, {332.0000, 334.7500, 55.0000, 20}, {309.0000, 329.5000, 54.8333, 20}, {0.0000, 25.5000, 54.0000, 18}, {91.5000, 97.5000, 54.0000, 51}, {181.2500, 202.5000, 53.0000, 83}, {228.7500, 236.2500, 53.0000, 34}, {329.5000, 332.0000, 52.7500, 20}, {50.0000, 75.0000, 52.5000, 11}, {343.0000, 350.0000, 52.5000, 18}, {236.2500, 255.0000, 51.5000, 34}, {30.6250, 37.7500, 50.5000, 63}, {255.0000, 273.5000, 50.5000, 34}, {0.0000, 20.5000, 50.0000, 18}, {20.5000, 25.0000, 50.0000, 63}, {97.5000, 102.0000, 50.0000, 51}, {350.0000, 360.0000, 50.0000, 18}, {202.5000, 210.5000, 48.5000, 83}, {0.0000, 16.7500, 48.0000, 18}, {353.7500, 360.0000, 48.0000, 18}, {272.6250, 273.5000, 47.5000, 40}, {273.5000, 286.2500, 47.5000, 34}, {286.2500, 287.5000, 47.5000, 31}, {25.0000, 30.6250, 47.0000, 63}, {126.2500, 137.5000, 47.0000, 83}, {2.5000, 13.0000, 46.0000, 18}, {180.0000, 181.2500, 45.0000, 83}, {102.0000, 110.5000, 44.5000, 51}, {328.6250, 329.5000, 44.0000, 31}, {328.1250, 328.6250, 43.7500, 31}, {287.5000, 291.0000, 43.5000, 31}, {137.5000, 152.5000, 42.0000, 83}, {152.5000, 161.7500, 40.0000, 83}, {231.5000, 236.2500, 40.0000, 9}, {236.2500, 245.0000, 40.0000, 40}, {138.7500, 143.7500, 39.7500, 51}, {0.0000, 37.7500, 36.7500, 1}, {37.7500, 38.5000, 36.7500, 63}, {290.3750, 291.0000, 36.5000, 52}, {67.5000, 70.3750, 36.0000, 63}, {326.0000, 328.1250, 36.0000, 31}, {328.1250, 330.0000, 36.0000, 45}, {98.0000, 110.5000, 35.5000, 8}, {110.5000, 116.2500, 35.5000, 51}, {0.0000, 30.0000, 35.0000, 1}, {330.0000, 342.2500, 35.0000, 45}, {342.2500, 343.0000, 34.5000, 45}, {343.0000, 352.5000, 34.5000, 1}, {38.5000, 40.7500, 34.0000, 63}, {161.7500, 165.0000, 34.0000, 83}, {180.0000, 185.0000, 34.0000, 13}, {116.2500, 138.7500, 33.5000, 51}, {138.7500, 148.2500, 33.5000, 47}, {10.7500, 21.1250, 33.0000, 1}, {227.7500, 231.5000, 33.0000, 9}, {352.5000, 356.2500, 32.0833, 1}, {185.0000, 198.7500, 32.0000, 13}, {356.2500, 360.0000, 31.3333, 1}, {209.3750, 210.5000, 30.7500, 13}, {36.2500, 40.7500, 30.6667, 80}, {40.7500, 67.5000, 30.6667, 63}, {67.5000, 71.2500, 30.0000, 8}, {272.6250, 290.3750, 30.0000, 52}, {165.0000, 180.0000, 29.0000, 83}, {295.0000, 313.7500, 29.0000, 31}, {71.2500, 88.2500, 28.5000, 8}, {148.2500, 157.5000, 28.5000, 47}, {198.7500, 209.3750, 28.5000, 13}, {0.0000, 1.0000, 28.0000, 1}, {21.1250, 25.0000, 28.0000, 80}, {88.2500, 98.0000, 28.0000, 8}, {118.2500, 120.0000, 28.0000, 38}, {313.7500, 326.0000, 28.0000, 31}, {288.8750, 295.0000, 27.5000, 31}, {28.7500, 36.2500, 27.2500, 80}, {242.5000, 245.0000, 27.0000, 27}, {226.2500, 227.7500, 26.0000, 9}, {227.7500, 242.5000, 26.0000, 27}, {275.5000, 283.0000, 26.0000, 52}, {161.2500, 165.0000, 25.5000, 47}, {283.0000, 288.8750, 25.5000, 52}, {25.0000, 28.7500, 25.0000, 80}, {10.7500, 12.7500, 23.7500, 66}, {157.5000, 161.2500, 23.5000, 47}, {318.7500, 321.2500, 23.5000, 88}, {85.5000, 88.2500, 22.8333, 78}, {1.0000, 2.1250, 22.0000, 1}, {238.7500, 240.5000, 22.0000, 76}, {88.2500, 93.2500, 21.5000, 38}, {297.5000, 303.7500, 21.2500, 88}, {283.0000, 288.7500, 21.0833, 88}, {2.1250, 12.7500, 21.0000, 1}, {303.7500, 308.5000, 20.5000, 88}, {117.1250, 118.2500, 20.0000, 38}, {308.5000, 318.7500, 19.5000, 88}, {288.7500, 297.5000, 19.1667, 88}, {49.2500, 50.5000, 19.0000, 7}, {283.0000, 285.0000, 18.5000, 71}, {85.5000, 86.5000, 18.0000, 60}, {93.2500, 94.6250, 17.5000, 38}, {285.0000, 297.5000, 16.1667, 71}, {74.5000, 80.0000, 16.0000, 78}, {238.7500, 241.2500, 16.0000, 40}, {297.5000, 303.7500, 15.7500, 71}, {69.2500, 74.5000, 15.5000, 78}, {80.0000, 84.0000, 15.5000, 78}, {192.5000, 202.5000, 15.0000, 25}, {258.7500, 273.7500, 14.3333, 40}, {178.0000, 192.5000, 14.0000, 25}, {112.5000, 117.1250, 13.5000, 38}, {251.2500, 258.7500, 12.8333, 40}, {0.0000, 2.1250, 12.5000, 62}, {84.0000, 86.5000, 12.5000, 78}, {105.0000, 112.5000, 12.5000, 38}, {316.7500, 320.0000, 12.5000, 62}, {94.6250, 104.0000, 12.0000, 38}, {273.7500, 283.0000, 12.0000, 40}, {313.1250, 315.7500, 11.8333, 32}, {315.7500, 316.7500, 11.8333, 62}, {172.7500, 178.0000, 11.0000, 46}, {93.6250, 94.6250, 10.0000, 60}, {104.0000, 105.0000, 10.0000, 38}, {117.1250, 118.8750, 10.0000, 12}, {357.5000, 360.0000, 10.0000, 62}, {25.0000, 49.2500, 9.9167, 7}, {302.1250, 304.5000, 8.5000, 32}, {202.5000, 226.2500, 8.0000, 9}, {341.2500, 357.5000, 7.5000, 62}, {118.8750, 138.7500, 7.0000, 12}, {138.7500, 161.2500, 7.0000, 46}, {273.7500, 279.9333, 6.2500, 59}, {279.9333, 283.0000, 6.2500, 5}, {312.5000, 313.1250, 6.0000, 32}, {105.0000, 105.2500, 5.5000, 15}, {273.7500, 276.3750, 4.5000, 76}, {241.2500, 251.2500, 4.0000, 40}, {273.7500, 276.3750, 3.0000, 59}, {322.0000, 325.0000, 2.7500, 62}, {0.0000, 30.0000, 2.0000, 66}, {278.7500, 283.0000, 2.0000, 76}, {304.5000, 312.5000, 2.0000, 32}, {312.5000, 320.0000, 2.0000, 35}, {320.0000, 322.0000, 2.0000, 62}, {330.0000, 341.2500, 2.0000, 62}, {325.0000, 330.0000, 1.7500, 62}, {105.2500, 108.0000, 1.5000, 15}, {53.7500, 69.2500, 0.0000, 78}, {69.2500, 70.0000, 0.0000, 60}, {108.0000, 121.2500, 0.0000, 15}, {220.0000, 226.2500, 0.0000, 86}, {267.5000, 273.7500, 0.0000, 59}, {39.7500, 49.2500, -1.7500, 21}, {49.2500, 53.7500, -1.7500, 78}, {226.2500, 244.0000, -3.2500, 76}, {70.0000, 76.2500, -4.0000, 60}, {87.5000, 93.6250, -4.0000, 60}, {267.5000, 269.5000, -4.0000, 76}, {273.7500, 278.7500, -4.0000, 76}, {278.7500, 283.0000, -4.0000, 5}, {341.2500, 357.5000, -4.0000, 66}, {161.2500, 172.7500, -6.0000, 46}, {172.7500, 177.5000, -6.0000, 86}, {0.0000, 5.0000, -7.0000, 66}, {357.5000, 360.0000, -7.0000, 66}, {213.7500, 220.0000, -8.0000, 86}, {238.7500, 244.0000, -8.0000, 59}, {300.0000, 308.0000, -9.0000, 5}, {320.0000, 328.0000, -9.0000, 4}, {257.5000, 269.5000, -10.0000, 59}, {87.5000, 121.2500, -11.0000, 55}, {73.7500, 76.2500, -11.0000, 36}, {76.2500, 87.5000, -11.0000, 60}, {121.2500, 125.5000, -11.0000, 42}, {143.7500, 161.2500, -11.0000, 77}, {177.5000, 192.5000, -11.0000, 86}, {263.7500, 265.0000, -11.6667, 59}, {283.0000, 300.0000, -12.0333, 5}, {72.5000, 73.7500, -14.5000, 36}, {308.0000, 320.0000, -15.0000, 4}, {257.5000, 273.7500, -16.0000, 76}, {273.7500, 283.0000, -16.0000, 75}, {125.5000, 128.7500, -17.0000, 42}, {244.0000, 245.6250, -18.2500, 59}, {128.7500, 136.2500, -19.0000, 42}, {161.2500, 162.5000, -19.0000, 29}, {244.0000, 245.6250, -19.2500, 73}, {235.0000, 238.7500, -20.0000, 49}, {188.7500, 192.5000, -22.0000, 28}, {192.5000, 213.7500, -22.0000, 86}, {136.2500, 146.2500, -24.0000, 42}, {25.0000, 39.7500, -24.3833, 21}, {39.7500, 56.2500, -24.3833, 36}, {162.5000, 177.5000, -24.5000, 29}, {177.5000, 188.7500, -24.5000, 28}, {213.7500, 223.7500, -24.5000, 49}, {244.0000, 251.2500, -24.5833, 59}, {0.0000, 25.0000, -25.5000, 21}, {320.0000, 328.0000, -25.5000, 16}, {328.0000, 357.5000, -25.5000, 4}, {357.5000, 360.0000, -25.5000, 21}, {146.2500, 153.7500, -26.5000, 42}, {70.5000, 72.5000, -27.2500, 36}, {72.5000, 91.7500, -27.2500, 48}, {300.0000, 320.0000, -28.0000, 16}, {153.7500, 158.7500, -29.1667, 42}, {188.7500, 223.7500, -29.5000, 42}, {223.7500, 235.0000, -29.5000, 49}, {235.0000, 240.0000, -29.5000, 73}, {68.7500, 70.5000, -30.0000, 36}, {251.2500, 264.0000, -30.0000, 59}, {264.0000, 267.5000, -30.0000, 72}, {158.7500, 162.5000, -31.1667, 42}, {91.7500, 110.5000, -33.0000, 14}, {183.7500, 188.7500, -33.0000, 42}, {162.5000, 183.7500, -35.0000, 42}, {52.5000, 56.2500, -36.0000, 37}, {125.5000, 140.5000, -36.7500, 69}, {64.0000, 68.7500, -37.0000, 36}, {267.5000, 287.5000, -37.0000, 72}, {320.0000, 345.0000, -37.0000, 67}, {345.0000, 350.0000, -37.0000, 74}, {45.0000, 52.5000, -39.5833, 37}, {140.5000, 165.0000, -39.7500, 2}, {0.0000, 25.0000, -40.0000, 74}, {25.0000, 45.0000, -40.0000, 37}, {58.0000, 64.0000, -40.0000, 36}, {350.0000, 360.0000, -40.0000, 74}, {212.5000, 223.7500, -42.0000, 19}, {235.0000, 240.0000, -42.0000, 50}, {240.0000, 246.3125, -42.0000, 73}, {72.5000, 75.0000, -43.0000, 10}, {75.0000, 98.7500, -43.0000, 24}, {120.0000, 125.5000, -43.0000, 68}, {51.2500, 58.0000, -44.0000, 36}, {246.3125, 267.5000, -45.5000, 73}, {267.5000, 287.5000, -45.5000, 26}, {287.5000, 305.0000, -45.5000, 72}, {305.0000, 320.0000, -45.5000, 54}, {45.0000, 51.2500, -46.0000, 36}, {67.5000, 72.5000, -46.5000, 10}, {230.0000, 235.0000, -48.0000, 50}, {0.0000, 35.0000, -48.1667, 64}, {40.0000, 45.0000, -49.0000, 36}, {61.2500, 64.0000, -49.0000, 41}, {64.0000, 67.5000, -49.0000, 10}, {320.0000, 330.0000, -50.0000, 39}, {90.0000, 120.0000, -50.7500, 68}, {120.0000, 122.5000, -50.7500, 85}, {36.2500, 40.0000, -51.0000, 36}, {57.5000, 61.2500, -51.0000, 41}, {0.0000, 27.5000, -51.5000, 64}, {90.0000, 92.5000, -52.5000, 17}, {122.5000, 126.7500, -53.0000, 85}, {52.5000, 57.5000, -53.1667, 41}, {57.5000, 60.0000, -53.1667, 33}, {0.0000, 23.7500, -53.5000, 64}, {32.5000, 36.2500, -54.0000, 36}, {67.5000, 75.0000, -54.0000, 65}, {225.7500, 230.0000, -54.0000, 50}, {126.7500, 132.5000, -54.5000, 85}, {92.5000, 97.5000, -55.0000, 17}, {177.5000, 192.5000, -55.0000, 19}, {212.5000, 225.7500, -55.0000, 50}, {225.7500, 230.0000, -55.0000, 57}, {60.0000, 65.0000, -56.5000, 33}, {132.5000, 165.0000, -56.5000, 85}, {165.0000, 168.7500, -56.5000, 19}, {262.5000, 270.0000, -57.0000, 6}, {270.0000, 305.0000, -57.0000, 79}, {330.0000, 350.0000, -57.0000, 39}, {48.0000, 52.5000, -57.5000, 41}, {75.0000, 82.5000, -57.5000, 65}, {97.5000, 102.5000, -58.0000, 17}, {0.0000, 20.0000, -58.5000, 64}, {20.0000, 32.5000, -58.5000, 36}, {350.0000, 360.0000, -58.5000, 64}, {65.0000, 68.7500, -59.0000, 33}, {230.0000, 246.3125, -60.0000, 57}, {305.0000, 320.0000, -60.0000, 44}, {82.5000, 90.0000, -61.0000, 65}, {227.5000, 230.0000, -61.0000, 23}, {246.3125, 248.7500, -61.0000, 6}, {223.7500, 227.5000, -63.5833, 23}, {248.7500, 251.2500, -63.5833, 6}, {90.0000, 102.5000, -64.0000, 65}, {102.5000, 135.5000, -64.0000, 17}, {168.7500, 177.5000, -64.0000, 19}, {177.5000, 192.5000, -64.0000, 30}, {192.5000, 218.0000, -64.0000, 19}, {202.5000, 205.0000, -65.0000, 23}, {251.2500, 252.5000, -65.0000, 6}, {32.5000, 48.0000, -67.5000, 41}, {48.0000, 68.7500, -67.5000, 70}, {221.2500, 223.7500, -67.5000, 23}, {252.5000, 262.5000, -67.5000, 6}, {262.5000, 270.0000, -67.5000, 61}, {330.0000, 350.0000, -67.5000, 82}, {68.7500, 98.7500, -70.0000, 33}, {205.0000, 221.2500, -70.0000, 23}, {221.2500, 255.0000, -70.0000, 81}, {0.0000, 20.0000, -75.0000, 82}, {52.5000, 68.7500, -75.0000, 43}, {98.7500, 135.5000, -75.0000, 87}, {135.5000, 168.7500, -75.0000, 17}, {168.7500, 205.0000, -75.0000, 56}, {270.0000, 320.0000, -75.0000, 61}, {320.0000, 350.0000, -75.0000, 44}, {350.0000, 360.0000, -75.0000, 82}, {11.2500, 20.0000, -76.0000, 82}, {0.0000, 52.5000, -82.5000, 43}, {115.0000, 205.0000, -82.5000, 22}, {205.0000, 270.0000, -82.5000, 3}, {52.5000, 115.0000, -85.0000, 53}, {0.0000, 360.0000, -90.0000, 58},};

    public static class FileToCopy {

        String dirIn;
        String nameIn;
        String dirOut;
        String nameOut;
        Uri uriOut;
        Uri uriIn;
        Context context;

        /**
         * the same name is used for out file
         *
         * @param dirIn
         * @param nameIn
         * @param dirOut
         */
        public FileToCopy(String dirIn, String nameIn, String dirOut) {
            super();
            this.dirIn = dirIn;
            this.nameIn = nameIn;
            this.dirOut = dirOut;
            nameOut = nameIn;
        }

        /**
         * the separate name is used for out file
         *
         * @param dirIn
         * @param nameIn
         * @param dirOut
         * @param nameOut
         */
        public FileToCopy(String dirIn, String nameIn, String dirOut, String nameOut) {
            super();
            this.dirIn = dirIn;
            this.nameIn = nameIn;
            this.dirOut = dirOut;
            this.nameOut = nameOut;
        }

        /**
         * use for saving files using SAF framework. Save from file system to SAF
         *
         * @param dirIn
         * @param nameIn
         * @param uriOut
         * @param context
         */
        public FileToCopy(String dirIn, String nameIn, Uri uriOut, Context context) {
            super();
            this.dirIn = dirIn;
            this.nameIn = nameIn;
            this.uriOut = uriOut;
            this.context = context;
        }

        /**
         * use for saving files using SAF framework. Save from SAF to file system
         *
         * @param uriIn
         * @param dirOut
         * @param context
         */
        public FileToCopy(Uri uriIn, String dirOut, String nameOut, Context context) {
            super();
            this.uriIn = uriIn;
            this.nameOut = nameOut;
            this.dirOut = dirOut;
            this.context = context;
        }


        /**
         * @return true if operation is successful
         */
        public boolean copy() {
            OutputStream fos = null;
            InputStream fis = null;
            try {
                if (uriIn == null) {
                    File in = new File(dirIn, nameIn);
                    if (in.exists()) fis = new BufferedInputStream(new FileInputStream(in));

                } else {
                    fis = new BufferedInputStream(context.getContentResolver().openInputStream(uriIn));
                }

                if (uriOut == null) {
                    File out = new File(dirOut, nameOut);
                    fos = new BufferedOutputStream(new FileOutputStream(out));
                } else {
                    fos = new BufferedOutputStream(context.getContentResolver().openOutputStream(uriOut));
                }

                //transfer bytes from the input stream to the output stream
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }


            } catch (Exception e) {
                Log.d(TAG, "exception=" + e);
                Log.d(TAG, getStackTrace(e));
                return false;
            } finally {
                try {
                    //Close the streams
                    fos.flush();
                    fos.close();
                    fis.close();
                } catch (Exception e) {
                    Log.d(TAG, "exception=" + e);
                }
            }
            return true;

        }
    }


    public static boolean copyFile(String from, String to) {
        OutputStream fos = null;
        InputStream fis = null;
        try {
            File in = new File(from);
            //	Log.d(TAG,"input file "+in.exists());
            if (in.exists()) {

                File out = new File(to);
                //		Log.d(TAG,"output file "+out.exists());

                fis = new BufferedInputStream(new FileInputStream(in));
                fos = new BufferedOutputStream(new FileOutputStream(out));
                //transfer bytes from the inputfile to the outputfile
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, length);
                }

            } else return false;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                //Close the streams
                fos.flush();
                fos.close();
                fis.close();
            } catch (Exception e) {
                Log.d(TAG, "exception=" + e);
            }
        }
        return true;
    }


    /**
     * the names of the prefs that are copied from sd to internal storage when restoring data
     *
     * @return
     */
    public static String[] getMyImportantPrefs() {
        String[] names = new String[]{Constants.WHATSNEW_FLAG, Constants.QUERY_CHECK_BOXES, Constants.QUERY_HEAD_CHECK_BOX, Constants.QSTART_DIALOG_FLAG, Constants.YALE_MAG, Constants.TYCHO_MAG, Constants.UCAC_MAG, Constants.PGC_MAG, Constants.NGC_MAG, Constants.UGC_MAG, Constants.REAL_TIME_DIALOG_CHOICE};
        return names;
    }

    /**
     * use this to print backups of shpref
     *
     * @param context
     * @param f
     */
    public static void printShPrefBackup(Context context, File f) {
        InputStream in = null;
        InfoList list = new InfoListImpl("", ShItem.class);
        try {
            in = new FileInputStream(f);
            InfoListLoader loader = new InfoListLoaderImp(in);
            list.load(loader);
            for (Object o : list) {
                Log.d(TAG, "ShItem=" + o);
            }
        } catch (Exception e) {
            Log.d(TAG, "e=" + e);
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
        }

    }

    //used for returning record of ra/dec.  better use generics in the future instead
    public static class RaDecRec {
        public double ra;
        public double dec;

        public RaDecRec(double ra, double dec) {
            this.ra = ra;
            this.dec = dec;
        }

        @Override
        public String toString() {
            return "RaDecRec [ra=" + ra + ", dec=" + dec + "]";
        }

    }

    public static Double getRaDecValue(String s) { //eg 12 23 36, null stands for error
        double v = 0;
        boolean error = false;
        try {
            v = Double.valueOf(s);
        } catch (Exception ex) {
            error = true;
        }
        if (!error) return v;
        else {
            String[] sarr = s.split("[^0-9+-\\.]");    //any numeric char
            //noinspection ImplicitArrayToString
            for (String sa : sarr) {
                Log.d(TAG, "sarr s=" + sa);
            }

            List<Double> l = new ArrayList<Double>();
            boolean error1 = false;
            try {
                for (String sa : sarr)
                    if (sa.length() > 0) l.add(Double.valueOf(sa));
            } catch (Exception ex) {
                error1 = true;
            }
            if (!error1) { //convert DMS->D
                v = 0;
                int len = min(3, l.size());
                int sign = 1;

                for (int i = 0; i < len; i++) {
                    switch (i) {
                        case 0:
                            v = l.get(i);
                            if (v < 0) sign = -1;
                            break;
                        case 1:
                            v = v + l.get(i) / 60f * sign;
                            break;
                        case 2:
                            v = v + l.get(i) / 3600f * sign;
                            break;
                    }
                }
                return v;
            }

        }
        return null;

    }

    //used for returning transit info
    public static class TransitRec {
        public Calendar tRise;
        public Calendar tTransit;
        public Calendar tSetting;

        public TransitRec(Calendar tRise, Calendar tTransit, Calendar tSetting) {
            this.tRise = tRise;
            this.tTransit = tTransit;
            this.tSetting = tSetting;
        }
    }

    /**
     * Universal tool for permission request.
     * Do not forget to provide for callback method ActivityCompat.OnRequestPermissionsResultCallback in calling activity which is called by android permission dialog.
     * If android permission dialog is not called, "always" runnable is run both if you cancel the explanation dialog or allow it.
     * The method correctly deal with night mode.
     *
     * @param activity
     * @param alwaysRun      runnable which runs both if permission is granted and not
     * @param permissions
     * @param requestCode
     * @param redmodeWarning if false do not show any dialog and android permission request in red mode. if true show the dialog, but add red mode warning
     */
    public static void askForPermissionsOrRun(Activity activity, Runnable alwaysRun, String[] permissions, String explanation, int requestCode, boolean redmodeWarning) {


        Log.d(TAG, "requestUser");
        boolean attempts_pass = false;

        for (String permission : permissions) {
            int perm_attempts = SettingsActivity.getSharedPreferences(activity).getInt(Constants.PERMISSIONS_BASE + permission, Global.max_permission_attempts);

            Log.d(TAG, permission + perm_attempts);

            if (perm_attempts > 0) {
                attempts_pass = true;
                perm_attempts--;
                SettingsActivity.putSharedPreferences(Constants.PERMISSIONS_BASE + permission, perm_attempts, activity);

            }

        }
        //if all permission are requested max_permission_attempts do not ask them
        if (!attempts_pass) {
            alwaysRun.run();
            return;
        }


        if (SettingsActivity.getNightMode() && !redmodeWarning) {
            alwaysRun.run();
            return;
        }

        boolean all_granted = true;
        for (String permission : permissions) {
            int perm = ActivityCompat.checkSelfPermission(activity, permission);
            Log.d(TAG, permission + " granted " + perm);
            if (perm != PackageManager.PERMISSION_GRANTED) {
                all_granted = false;
                break;
            }
        }

        if (!all_granted) {
            String warning = activity.getString(R.string.permission_red_mode);
            String text = explanation;
            if (SettingsActivity.getNightMode() && redmodeWarning) {
                text += warning;
            }

            InputDialog d = AstroTools.getPermissionDialog(activity, text, permissions, alwaysRun, requestCode);
            if (activity instanceof ParentActivity) {
                ((ParentActivity) activity).registerDialog(d).show();
            } else if (activity instanceof ParentListActivity) {
                ((ParentListActivity) activity).registerDialog(d).show();
            } else if (activity instanceof ParentPreferenceActivity) {
                ((ParentPreferenceActivity) activity).registerDialog(d).show();
            }

        } else {
            alwaysRun.run();
        }
    }

    /**
     * Get dialog for permission explanation, suggesting user to accept permission and
     * calling original android dialog for permission setting
     *
     * @param activity
     * @param explanation
     * @param permissions
     * @return
     */
    public static InputDialog getPermissionDialog(final Activity activity, String explanation, final String[] permissions, final Runnable no, final int requestCode) {

        InputDialog dimp = new InputDialog(activity);
        dimp.setMessage(explanation);

        dimp.setPositiveButton(activity.getString(R.string.ok), new InputDialog.OnButtonListener() {
            public void onClick(String value) {
                ActivityCompat.requestPermissions(activity, permissions, requestCode);
                //activity.requestPermissions(permissions, requestCode);

            }
        });
        dimp.setNegativeButton(activity.getString(R.string.cancel), new InputDialog.OnButtonListener() {
            public void onClick(String value) {

                if (no != null) no.run();
            }
        });
        return dimp;
    }

    public static void initOnWriteExternalStoragePermissionSet(Context context, String[] permissions, int[] grantResults) {
        for (int i = 0; i < permissions.length && i < grantResults.length; i++) {
            if (permissions[i].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                new Init(context, null).run();
                break;
            }
        }
    }

    public static InputDialog getDialog(Context context, String title, final Runnable r) {
        InputDialog dimp = new InputDialog(context);
        dimp.setMessage(title);

        dimp.setPositiveButton(context.getString(R.string.ok), new InputDialog.OnButtonListener() {
            public void onClick(String value) {
                r.run();
            }
        });
        dimp.setNegativeButton(context.getString(R.string.cancel));

        return dimp;


    }

    public static InputDialog getDialogNoCancel(Context context, String title, final Runnable r) {
        InputDialog dimp = new InputDialog(context);
        dimp.setMessage(title);

        dimp.setPositiveButton(context.getString(R.string.ok), new InputDialog.OnButtonListener() {
            public void onClick(String value) {
                r.run();
            }
        });


        return dimp;
    }

    public static InputDialog getDialog(Context context, String title, final Runnable r, final Runnable r2) {
        InputDialog dimp = new InputDialog(context);
        dimp.setMessage(title);
        dimp.setPositiveButton(context.getString(R.string.ok), new InputDialog.OnButtonListener() {
            public void onClick(String v) {
                r.run();
            }
        });
        dimp.setNegativeButton(context.getString(R.string.cancel), new InputDialog.OnButtonListener() {
            public void onClick(String v) {
                r2.run();
            }
        });

        return dimp;
    }

    public static interface NameDialogRunnable {
        public void setName(String name);

        public void run();
    }

    public static InputDialog getNameDialog(Context context, String title, final NameDialogRunnable r) {
        return getNameDialog(context, title, "", r);

    }

    /**
     * @param context
     * @param title
     * @param initValue
     * @param r         - runnable on OK
     * @return
     */
    public static InputDialog getNameDialog(Context context, String title, String initValue, final NameDialogRunnable r) {
        return getNameDialog(context, title, initValue, null, false, r, null, null);
    }

    public static InputDialog getNameDialog(Context context, String title, String initValue, String hint, boolean phone_keys, final NameDialogRunnable r, String majorTitle) {
        return getNameDialog(context, title, initValue, hint, phone_keys, r, null, majorTitle);
    }

    /**
     * @param context
     * @param title
     * @param initValue
     * @param hint       use null for empty hint
     * @param phone_keys whether to use phone keyboard
     * @param rPos       - runnable on ok
     * @param rNeg       - runnable on cancel
     * @return
     */
    public static InputDialog getNameDialog(Context context, String title, String initValue, String hint, boolean phone_keys, final NameDialogRunnable rPos, final NameDialogRunnable rNeg, String majorTitle) {
        InputDialog dimp = new InputDialog(context);
        if (majorTitle != null) dimp.setTitle(majorTitle);
        dimp.setMessage(title);
        dimp.setType(InputDialog.DType.INPUT_STRING);
        dimp.setValue(initValue);
        dimp.setHint(hint);
        if (phone_keys) {
            dimp.setType(DType.INPUT_NUMBER);
        }

        dimp.setPositiveButton(context.getString(R.string.ok), new InputDialog.OnButtonListener() {
            public void onClick(String v) {
                rPos.setName(v);
                rPos.run();
            }
        });
        dimp.setNegativeButton(context.getString(R.string.cancel), new InputDialog.OnButtonListener() {
            public void onClick(String v) {
                if (rNeg != null) {
                    rNeg.setName(v);
                    rNeg.run();
                }
            }
        });

        return dimp;
    }

    public static double getNormalisedRa(double ra) {
        return AstroTools.normalise24(ra);
    }

    public static byte getConstellation(double ra, double dec) {
        int i = 0;
        RaDecRec rec = precession(ra, dec, 1875, 1, 1);
        double ra75 = getNormalisedRa(rec.ra) * 360 / 24;//precessed to 1875, the equinox of the boundaries
        double dec75 = rec.dec;
        while (i < conBoundaries.length) {
            if (dec75 >= conBoundaries[i][2] && ra75 >= conBoundaries[i][0] && ra75 < conBoundaries[i][1])
                return (byte) conBoundaries[i][3];
            i++;
        }
        return 0;
    }


    /**
     * returns rise,setting,transit time so that the closest transit after the beginning of the day
     * is calculated
     *
     * @param p
     * @param c
     * @param alt
     * @return
     */
    public static TransitRec getRiseSetting(Point p, Calendar c, double alt) {//Tr -seconds of rising,Ttr - seconds of transit,Ts of setting
        //somehow not works for moon for south lattitude, check why
        //do try catch block for calendar
        boolean error = false;

        double h0 = alt;
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);
        Calendar c1 = (Calendar) c.clone();
        c1.set(y, m, d, 0, 0, 0);
        double sdbeg = sdTime(c1); //local sidereal time
        Log.d(TAG, "sdbeg=" + sdbeg);


        boolean planet = false;

        //if planet recalculate its coords as the planet moves during the day
        if (p instanceof Planet) {
            planet = true;
            ((Planet) p).setRaDec(c1); //planet moves, this may impact calculations. Thus we calculate the coords on the date

        }


        double lon = 0;//Settings.getLongitude(Global.context);
        double lat = SettingsActivity.getLattitude();
        double sdtr = p.getRa() - lon * 24 / 360; //sidereal time of transit

        while (sdtr < sdbeg) {
            sdtr += 24;
        }
        while (sdtr >= sdbeg + 24) {
            sdtr -= 24;
        }
        //Log.d(TAG,"sdtr="+sdtr);
        double dist = (sdtr - sdbeg) / 1.0027379 * 3600;//in UTC seconds
        // Log.d(TAG,"dist="+dist);
        double dl = (sin(h0 * PI / 180) - sin(lat * PI / 180) * sin(p.getDec() * PI / 180)) / (cos(lat * PI / 180) * cos(p.getDec() * PI / 180));
        Log.d(TAG, "dl=" + dl);
        if (abs(dl) > 1) error = true; //never rising or setting


        long Tb = c1.getTimeInMillis() / 1000;
        long Ttr = Tb + (int) (dist);
        long Tr = 0;
        long Ts = 0;

        boolean moon = false;

        boolean flag = false;

        if (!error) {
            dl = abs(3600 * 12 / PI / 1.0027379 * acos(dl));  //dist from transit to rise/setting
            Tr = Ttr - (int) (dl);
            Ts = Ttr + (int) (dl);
            if (Tr < Tb) { //if rise before the beginning of the day, then move to the next day
                int sday = (int) (3600 * 24 / 1.0027379);
                Tr = Tr + sday;
                Ttr = Ttr + sday;
                Ts = Ts + sday;
            }

            int i = 1;

            if (planet) //iterate for moon/sun as they move a lot
                if ((((Planet) p).getPlanetType() == Planet.PlanetType.Moon) || (((Planet) p).getPlanetType() == Planet.PlanetType.Sun))
                    moon = true;
            do {

                i++;
                c1.setTimeInMillis(Tr * 1000);
                if (planet) {
                    ((Planet) p).setRaDec(c1);//make coordinates preciser
                }

                double sdr = sdTime(c1);
                double h = Altitude(sdr, lat, p.getRa(), p.getDec());
                double han = sdr + lon * 24 / 360 - p.ra;//hour angle
                double delta = 1 / (cos(p.getDec() * PI / 180) * cos(lat * PI / 180) * sin(han * PI / 12)) * 3600 * (h - h0) * 12 / 180 / 1.0027379;
                Tr = Tr + (int) (delta);
                //to iterate for moon
                flag = false;
                if (moon) if ((abs(h - h0) > 0.01) && (i < 10)) flag = true;//continue iterating
            } while (flag);

            i = 1;
            do {

                i++;
                c1.setTimeInMillis(Ts * 1000);
                if (planet) {
                    ((Planet) p).setRaDec(c1);//make coordinates preciser
                }

                double sdr = sdTime(c1);
                double h = Altitude(sdr, lat, p.getRa(), p.getDec());
                double han = sdr + lon * 24 / 360 - p.getRa();//hour angle
                double delta = 1 / (cos(p.getDec() * PI / 180) * cos(lat * PI / 180) * sin(han * PI / 12)) * 3600 * (h - h0) * 12 / 180 / 1.0027379;
                Ts = Ts + (int) (delta);
                //to iterate for moon
                flag = false;
                if (moon) if ((abs(h - h0) > 0.01) && (i < 10)) flag = true;//continue iterating
            } while (flag);
        }
        int i = 1;
        do {
            i++;
            c1.setTimeInMillis(Ttr * 1000);
            if (planet) {
                ((Planet) p).setRaDec(c1);//make coordinates preciser
            }

            double sdr = sdTime(c1);
            double han = sdr + lon * 24 / 360 - p.getRa();//hour angle

            han = AstroTools.normalise24(han);
            if (han > 12) han = han - 24;
            double delta = -3600 * han / 1.0027379;
            Ttr = Ttr + (int) (delta);
            //  Log.d(TAG,"Ttr after delta="+Ttr);
            double az = Azimuth(sdr, lat, p.getRa(), p.getDec());

            flag = false;
            if (moon) if ((abs(az - 180) > 0.01) && (i < 10)) flag = true;//continue iterating
        } while (flag);

        Calendar ct = Calendar.getInstance();
        ct.setTimeInMillis(Ttr * 1000);
        Calendar cr = null;
        Calendar cs = null;
        if (!error) {
            cr = Calendar.getInstance();
            cr.setTimeInMillis(Tr * 1000);

            cs = Calendar.getInstance();
            cs.setTimeInMillis(Ts * 1000);

        }
        return new TransitRec(cr, ct, cs);

    }

    /**
     * @param ra
     * @param dec
     * @param jdinit   - epoch of given ra/dec
     * @param jdtarget - target epoch
     * @return
     */
    public static RaDecRec getRaDecExact(double ra, double dec, double jdinit, double jdtarget) {


        //p.126

        double T = (jdinit - 2451545.) / 36525;

        double t = (jdtarget - jdinit) / 36525;

        double a0 = (2306.2181 + 1.39656 * T - 0.000139 * T * T) * t;
        double a1 = a0 + (0.30188 - 0.000344 * T) * t * t + 0.017998 * t * t * t;
        a1 = a1 / 3600;
        double a2 = a0 + (1.09468 + 0.000066 * T) * t * t + 0.018203 * t * t * t;
        a2 = a2 / 3600;
        double a3 = (2004.3109 - 0.85330 * T - 0.000217 * T * T) * t - (0.42665 + 0.000217 * T) * t * t - 0.041833 * t * t * t;
        a3 = a3 / 3600;

        double A = cos(dec * PI / 180) * sin((ra) * PI / 12 + a1 * PI / 180);

        double B = cos(a3 * PI / 180) * cos(dec * PI / 180) * cos((ra) * PI / 12 + a1 * PI / 180) - sin(a3 * PI / 180) * sin(dec * PI / 180);
        double C = sin(a3 * PI / 180) * cos(dec * PI / 180) * cos((ra) * PI / 12 + a1 * PI / 180) + cos(a3 * PI / 180) * sin(dec * PI / 180);

        double ra1 = (Math.atan2(A, B)) * 12 / PI + a2 * 12 / 180;
        double dec1 = Math.asin(C) * 180 / PI;
        return new RaDecRec(ra1, dec1);
    }


    public static RaDecRec getRaDecExact(double ra, double dec, Calendar cinit, Calendar ctarget) {
        int y = cinit.get(Calendar.YEAR);
        int month = cinit.get(Calendar.MONTH) + 1;
        int d = cinit.get(Calendar.DAY_OF_MONTH);
        int h = cinit.get(Calendar.HOUR_OF_DAY);
        int min = cinit.get(Calendar.MINUTE);

        //target date
        int yt = ctarget.get(Calendar.YEAR);
        int montht = ctarget.get(Calendar.MONTH) + 1;
        int dt = ctarget.get(Calendar.DAY_OF_MONTH);
        int ht = ctarget.get(Calendar.HOUR_OF_DAY);
        int mint = ctarget.get(Calendar.MINUTE);

        //p.126
        double jd0 = JD(y, month, d, h, min);
        double jd = JD(yt, montht, dt, ht, mint);
        return getRaDecExact(ra, dec, jd0, jd);
    }

    public static RaDecRec precession(double ra, double dec, Calendar c) {

        double jdinit = JD(2000, 1, 1, 12, 0);
        double jdtarget = JD(c);
        return getRaDecExact(ra, dec, jdinit, jdtarget);

    }

    public static RaDecRec approximatePrecession(double ra, double dec, Calendar c) {

        double dist = JD(c) - JD(2000, 1, 1, 0, 0);
        double m = 3.07496 + 0.00186 * dist / 365 / 100;  //in seconds
        double n = 1.33621 - 0.00057 * dist / 365 / 100;  //in seconds
        ra = ra + (m + n * sin(ra * PI / 12) * tan(dec * PI / 180)) * dist / 365 / 3600;       //precession

        n = 20.0431 - 0.0085 * dist / 365 / 100;  //in seconds
        dec = dec + n * cos(ra * PI / 12) * dist / 365 / 3600;
        return new RaDecRec(ra, dec);


    }

    public static Point precession(Point p, Calendar c) {
        RaDecRec r = precession(p.getRa(), p.getDec(), c);
        return new Point(r.ra, r.dec);
    }

    /**
     * get 2000 ra/dec from current
     *
     * @param ra  current epoch ra
     * @param dec current epoch dec
     * @param c   current epoch
     * @return
     */

    public static RaDecRec get2000RaDec(double ra, double dec, Calendar c) {//get 2000 ra/dec from current
        double jdc = JD(c);
        double jd2000 = 2451545;
        RaDecRec rec = AstroTools.getRaDecExact(ra, dec, jdc, jd2000);
        return rec;
    }

    public static RaDecRec precession(double ra, double dec, int y, int month, int d) {
        double jdinit = JD(2000, 1, 1, 12, 0);
        double jdtarget = JD(y, month, d, 12, 0);
        return getRaDecExact(ra, dec, jdinit, jdtarget);
    }

    public static double JD(int y, int m, double d, int h, int min) {//Julian day

        double y1 = y;
        double m1 = m;
        if ((m == 1) || (m == 2)) {
            y1 = y - 1;
            m1 = m + 12;
        }
        double a = (int) (y1 / 100);
        double b = (2.0 - a + (int) (a / 4.0));
        double r = (((int) (365.25 * (y1 + 4716))) + ((int) (30.6001 * (m1 + 1))) + d + h / 24f + min / (24 * 60f) + b - 1524.5);
        return r;
    }

    public static double JD(Calendar c) {//Julian day
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH) + 1;
        int d = c.get(Calendar.DAY_OF_MONTH);
        int h = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        return JD(y, m, d, h, min);
    }

    /**
     *  local sidereal time
     */
    public static double sdTime(int y, int m, int d, int h, int min) {

        double lon = SettingsActivity.getLongitude();
        Calendar c = Calendar.getInstance();
        double t = (JD(y, m, d, 0, 0) - 2451545) / 36525;
        double r = 6 * 3600.0 + 41 * 60.0 + 50.54841;
        r = r + 8640184.8 * t + 0.093104 * t * t - (6.2e-06) * t * t * t;
        r = r + 1.0027379 * (h * 3600.0 + 60.0 * min - c.get(Calendar.ZONE_OFFSET) / 1000 - c.get(Calendar.DST_OFFSET) / 1000); //in seconds
        double r1 = 0;
        if (r > 0) r1 = r - ((int) (r / 86400)) * 86400;
        else r1 = r - ((int) (r / 86400) - 1) * 86400;

        r = r1 / 3600 + lon * 12 / 180;
        r = AstroTools.normalise24(r);
        return r;
    }

    /**
     *  local sidereal time
     */
    public static double sdTime(Calendar c) {
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH) + 1;
        int d = c.get(Calendar.DAY_OF_MONTH);
        int h = c.get(Calendar.HOUR_OF_DAY);
        int min = c.get(Calendar.MINUTE);
        int sec = c.get(Calendar.SECOND);
        double lon = SettingsActivity.getLongitude();

        double t = (JD(y, m, d, 0, 0) - 2451545) / 36525;
        double r = 6 * 3600.0 + 41 * 60.0 + 50.54841;
        r = r + 8640184.8 * t + 0.093104 * t * t - (6.2 * pow(10, -6)) * t * t * t;
        r = r + 1.0027379 * (h * 3600.0 + 60.0 * min + sec - c.get(Calendar.ZONE_OFFSET) / 1000 - c.get(Calendar.DST_OFFSET) / 1000); //in seconds
        double r1 = 0;
        if (r > 0) r1 = r - ((int) (r / 86400.0)) * 86400.0;
        else r1 = r - ((int) (r / 86400) - 1) * 86400.0;

        r = r1 / 3600.0 + lon * 12.0 / 180;
        r = AstroTools.normalise24(r);
        return r;
    }

// this is for visibility calculation, this is the author of idea http://www.uv.es/jrtorres/visib.html

    private static double[][] VisArr = {{-0.376911, -0.331451, -0.268183, -0.198177, -0.123815, -0.042444, 0.049842, 0.159558, 0.293354, 0.455739, 0.64999, 0.880777, 1.15584, 1.482228, 1.855923, 2.266949, 2.676, 2.776553, 2.930431, 3.163406, 3.464285, 3.821092, 4.221, 4.61}, {-1.806444, -1.774717, -1.734482, -1.685101, -1.625192, -1.552937, -1.465538, -1.358054, -1.22564, -1.067267, -0.884068, -0.668706, -0.395243, -0.041944, 0.345791, 0.696, 1.088, 1.206474, 1.382138, 1.610655, 1.903381, 2.256431, 2.632, 3.066}, {-2.336825, -2.333688, -2.331011, -2.313951, -2.279119, -2.229734, -2.165945, -2.081004, -1.967364, -1.818647, -1.629165, -1.39667, -1.126426, -0.824314, -0.492425, -0.131546, 0.206, 0.346695, 0.535251, 0.770789, 1.033812, 1.326535, 1.699, 2.132}, {-2.460093, -2.460781, -2.460521, -2.457228, -2.446245, -2.421422, -2.376266, -2.303572, -2.196521, -2.053094, -1.874059, -1.661059, -1.417576, -1.147481, -0.856105, -0.550963, -0.321, -0.13774, 0.032813, 0.253144, 0.494252, 0.760529, 1.132, 1.585}, {-2.546943, -2.546521, -2.546681, -2.548051, -2.546321, -2.534318, -2.504717, -2.449928, -2.363052, -2.24449, -2.098851, -1.92837, -1.729969, -1.502126, -1.266128, -1.056193, -0.88, -0.736142, -0.560534, -0.3895, -0.203342, 0.0172, 0.286, 0.652}, {-2.560968, -2.560729, -2.560819, -2.561529, -2.55969, -2.55013, -2.526875, -2.482261, -2.40917, -2.308296, -2.184839, -2.041067, -1.872722, -1.676828, -1.472054, -1.28921, -1.137, -0.9964, -0.8606, -0.703, -0.525901, -0.2992, -0.051, 0.241}, {-2.565971, -2.565756, -2.565837, -2.56546, -2.56457, -2.555202, -2.533309, -2.49371, -2.431807, -2.349087, -2.250534, -2.137519, -2.003406, -1.842016, -1.662449, -1.482659, -1.362, -1.2439, -1.1187, -0.968133, -0.8288, -0.6394, -0.408, -0.121}, {-2.565971, -2.565756, -2.565837, -2.56546, -2.56457, -2.555202, -2.533309, -2.49371, -2.431807, -2.349087, -2.256, -2.163, -2.054, -1.916, -1.737, -1.525, -1.43, -1.352, -1.194, -1.036, -0.96, -0.854, -0.623, -0.244}};
    private static double[] AngleArr = {-0.2255, 0.5563, 0.9859, 1.2601, 1.7419, 2.0828, 2.5563, 2.883};


    //lst=local sidereal time
    public static double Azimuth(double lst, double lat, double ra, double dec) {  //in grad
        double le = cos(dec * PI / 180) * cos(-(lst - ra) * PI / 12);
        double me = cos(dec * PI / 180) * sin(-(lst - ra) * PI / 12);
        double ne = sin(dec * PI / 180);

        double lh = cos(lat * PI / 180 - PI / 2) * le + sin(lat * PI / 180 - PI / 2) * ne;
        double mh = me;
        double a = -atan2(mh, lh) * 180 / PI + 180;
        return a;
    }

    /**
     * @param lst local sidereal time
     * @param lat
     * @param ra
     * @param dec
     * @return
     */
    public static double Altitude(double lst, double lat, double ra, double dec) { //input parameters in grad, output in grad
        double sgr1 = lst * PI / 12;
        double lat1 = lat * PI / 180;

        double ra1 = ra * PI / 12;
        double dec1 = dec * PI / 180;
        double alt = asin(sin(lat1) * sin(dec1) + cos(lat1) * cos(dec1) * cos(sgr1 - ra1)) * 180 / PI;
        return alt;
    }

    /**
     * @param lst local sidereal time
     * @param lat latitude
     * @param ra
     * @param dec
     * @return az alt in grad
     */
    public static Holder2<Double, Double> getAzAlt(double lst, double lat, double ra, double dec) {
        Holder2<Double, Double> res = new Holder2<Double, Double>(0., 0.);
        double RAD = PI / 180;
        double RAH = PI / 12;
        double d = dec * RAD;
        double l = lat * RAD;
        double r = ra * RAH;
        double s = lst * RAH;

        double le = cos(d) * cos(-(s - r));
        double me = cos(d) * sin(-(s - r));
        double ne = sin(d);

        double lh = cos(l - PI / 2) * le + sin(l - PI / 2) * ne;
        double mh = me;

        res.x = -atan2(mh, lh) / RAD + 180;
        res.y = asin(sin(l) * sin(d) + cos(l) * cos(d) * cos(s - r)) / RAD;

        return res;
    }

    public static double SBB(double LM) {     //background SB, for visibility calculation
        double sb;
        if (LM > 8.5) sb = 27;
        else {
            if (LM >= 5.11) sb = 25.25435 - 3.1308976 * LM + 0.3913035 * LM * LM;
            else {
                if (LM < -3.12) sb = 1.0897 * LM + 8.3839;
                else sb = 8.5001927 + 1.1721614 * LM + 0.000010686334 * exp(log(LM) * 8);
            }
        }
        return sb;
    }

    public static double NOT_VISIBLE = -0.1;

    //for visibility calculation
    public static double LC0(double a, double b, double mag, double magnification) {
        double SBobj = mag + 2.5 * log10(a * b * PI / 4) + 8.89; //object surface brigtness per arcsec
        double dark = -5 * log10(sqrt(SettingsActivity.getTPassthr() / 100) * SettingsActivity.getTAperture() / magnification / 7.5);

        double SBBack = SBB(SettingsActivity.getLM());

        double lc0 = -0.4 * (SBobj - SBBack);
        return lc0;
    }

    //for visibility calculation
    public static double LogC(double a, double b, double mag, double magnification, int type) { //returns true if within limits, angle - object dimension

        if ((type == AstroObject.PN || type == AstroObject.MINOR_PLANET || type == AstroObject.DoubleStar) && Double.isNaN(a))
            a = 0;
        if ((type == AstroObject.PN || type == AstroObject.MINOR_PLANET || type == AstroObject.DoubleStar) && Double.isNaN(b))
            b = 0;

        if (Double.isNaN(mag) || Double.isNaN(a) || Double.isNaN(b)) return Double.NaN;

        if (a < 1e-5) {
            a = 144 / 60. / SettingsActivity.getTAperture();//airy disc
        }

        if (b < 1e-5) {
            b = 144 / 60. / SettingsActivity.getTAperture();
        }


        double angle = 0;
        if (a < b) angle = log10(a * magnification);
        else angle = log10(b * magnification); //apparent angle after magnification

        double SBobj = mag + 2.5 * log10(a * b * PI / 4) + 8.89; //object surface brigtness per arcsec
        double dark = -5 * log10(sqrt(SettingsActivity.getTPassthr() / 100) * SettingsActivity.getTAperture() / magnification / 7.5);
        double SBBack = SBB(SettingsActivity.getLM());
        double SBApp = SBBack + dark;
        double lc0 = -0.4 * (SBobj - SBBack);
        int k = -1;
        for (int j = 0; j < 6; j++)
            if ((angle >= AngleArr[j]) && (angle <= AngleArr[j + 1])) k = j;

        if (angle > AngleArr[6]) k = 6;                //za granitsey dannyh
        if (angle < AngleArr[0]) k = 0;
        double lc = 0;
        if ((k != -1) && (SBApp < 27) && (SBApp > 4) && (mag != 0) && (a != 0) && (b != 0)) {
            int l = (int) (SBApp) - 4;
            if (k != 6) {
                double SB0 = VisArr[k][l] + (VisArr[k][l + 1] - VisArr[k][l]) * (SBApp - l - 4);     //granitsy diapazona interpolyatsii
                double SB1 = VisArr[k + 1][l] + (VisArr[k + 1][l + 1] - VisArr[k + 1][l]) * (SBApp - l - 4);
                lc = SB0 + (SB1 - SB0) * (angle - AngleArr[k]) / (AngleArr[k + 1] - AngleArr[k]);     //Log of critical contrast
            } else lc = VisArr[k][l] + (VisArr[k][l + 1] - VisArr[k][l]) * (SBApp - l - 4);


            double vis = lc0 - lc; //visibility log c0 -log C
            return transformLogC(vis);
        } else return NOT_VISIBLE;//transformLogC(-10);


    }


    /**
     * use it for creating check alt context only once to make a quicker search
     *
     * @author leonid
     */
    public static class CheckAltContext {
        long start;
        long end;
        double lat;
        double minAlt;

        public CheckAltContext() {
            start = SettingsActivity.getSharedPreferences(Global.getAppContext()).getLong(Constants.START_OBSERVATION_TIME, 0);
            end = SettingsActivity.getSharedPreferences(Global.getAppContext()).getLong(Constants.END_OBSERVATION_TIME, 0);
            lat = SettingsActivity.getLattitude();
            minAlt = SettingsActivity.getMinAlt();
        }

    }

    /**
     * checks if the object is above minimum altitude within the observation range
     *
     * @param ra
     * @param dec
     * @param checkContext
     * @return
     */
    public static boolean CheckAlt(double ra, double dec, CheckAltContext checkContext) {//true if the object is visible between LSTstart and LSTfin
        Calendar sc = Calendar.getInstance();
        long start = checkContext.start;//Settings1243.getSharedPreferences(Global.getAppContext()).getLong(Constants.START_OBSERVATION_TIME, 0);
        sc.setTimeInMillis(start);

        Calendar ec = Calendar.getInstance();
        long end = checkContext.end;//Settings1243.getSharedPreferences(Global.getAppContext()).getLong(Constants.END_OBSERVATION_TIME, 0);
        ec.setTimeInMillis(end);

        double LSTfin = AstroTools.sdTime(ec);
        double LSTstart = AstroTools.sdTime(sc);
        double ste;
        double lat = checkContext.lat;//Settings1243.getLattitude();
        if (LSTfin < LSTstart) ste = LSTfin + 24;
        else ste = LSTfin;
        double sttime = LSTstart;
        boolean visible = false;
        boolean flag;
        do {
            if (Altitude(sttime, lat, ra, dec) > checkContext.minAlt)    //Settings1243.getMinAlt()
                visible = true;
            // Log.d(TAG,"Alt="+Altitude(sttime,lat,ra,dec));
            sttime = sttime + 0.5;
            flag = (sttime < ste);
            if (visible) flag = false;
        } while (flag);
        return visible;
    }


    public static boolean CheckVisibility(double a, double b, double mag, double detLimit, int emptyRule, int type) {

        boolean visibility = false;
        double DL = detLimit;
        double magnification;

        for (int i = 0; i < SettingsActivity.getEPsNumber(); i++) {
            double epfocus = SettingsActivity.getEpFocus(i);
            if (SettingsActivity.isCCD(epfocus)) //CCD
                continue;
            magnification = SettingsActivity.getTFocus() / epfocus;
            double logC;


            logC = LogC(a, b, mag, magnification, type);
            if (Double.isNaN(logC)) {
                switch (emptyRule) {
                    case SearchRules.EMPTY_RULE_INCLUDE:
                        return true;
                    case SearchRules.EMPTY_RULE_EXCLUDE:
                        visibility = visibility || false;
                        break;
                    case SearchRules.EMPTY_RULE_ZERO:
                        visibility = detLimit <= 0;
                        break;

                    case SearchRules.EMPTY_RULE_MAX:
                        return true;
                }
            } else {
                if (logC >= DL) return true;
            }

        }

        return visibility;
    }

    /**
     * @param lc
     * @return transform -0.1...0.5 into 0...5
     */
    private static double transformLogC(double lc) {
        double y = (lc + 0.1) * 5 / 0.6;

        if (y < 0) y = NOT_VISIBLE;
        if (y > 5) y = 5;
        return y;
    }

    public static double getMaxVisibility(double a, double b, double mag, int type) {
        double vis;
        double maxvis = -100;

        for (int i = 0; i < SettingsActivity.getEPsNumber(); i++) {
            double epfocus = SettingsActivity.getEpFocus(i);
            if (SettingsActivity.isCCD(epfocus)) //CCD
                continue;
            vis = LogC(a, b, mag, SettingsActivity.getTFocus() / epfocus, type);
            if (vis > maxvis) maxvis = vis;
        }
        return maxvis;
    }

    public static double MoonFraction(Calendar c) {

        Planet sun = new Planet(Planet.PlanetType.Sun, c);
        Planet moon = new Planet(Planet.PlanetType.MoonNoParallax, c);

        double cosangle = -(sin(sun.dec * PI / 180) * sin(moon.dec * PI / 180) + cos(sun.dec * PI / 180) * cos(moon.dec * PI / 180) * cos((sun.ra - moon.ra) * PI / 12));
        return (1 + cosangle) / 2;

    }
    //SAND: converts double to deg/min/sec

    public static DMS d2dms(double x) {
        DMS e = new DMS();
        e.s = (int) Math.round(x * 3600);
        e.d = e.s / 3600;
        e.s = e.s % 3600;
        if (e.s < 0) e.s = -e.s; //abs
        e.m = e.s / 60;
        e.s %= 60;

        return e;
    }

    public static String getGradString(double value) {
        DMS dms = AstroTools.d2dms(value);
        return "" + dms.d + " " + (dms.m < 10 ? "0" : "") + dms.m + " " + (dms.s < 10 ? "0" : "") + dms.s;
    }

    public static Holder2<Double, Double> solveQe(double a, double b, double c) {//ax*x+bx+c=0
        double det = b * b - 4 * a * c;
        double detsqrt = sqrt(det);
        if (det < 0) return null;
        return new Holder2<Double, Double>((-b - detsqrt) / (2 * a), (-b + detsqrt) / (2 * a));

    }

    public static int getInteger(String s, int def, int min, int max) {
        try {
            int i = Integer.parseInt(s);
            if (i < min) return min;
            if (i > max) return max;
            return i;
        } catch (Exception e) {
            return def;
        }
    }

    public static double getDouble(String s, double def, double min, double max) {
        try {
            double d = Double.parseDouble(s);
            if (d < min) return min;
            if (d > max) return max;
            return d;
        } catch (Exception e) {
            return def;
        }
    }

    public static boolean invokeSearchActivity(final Activity p) {
        startSearchDialogNew(p);
        return false;
    }

    public static final int SR_CODE = 3;//code for calling search result activity from graph

    private static void startSearchDialogNew(final Activity p) {
        final InputDialog d = new InputDialog(p);
        d.setTitle(p.getString(R.string.dso_search));
        d.insertLayout(R.layout.search_dialog_new);

        final boolean calledfromgraph = p instanceof GraphActivity;


        final EditText et = (EditText) d.findViewById(R.id.sd_et);


        et.setText(SettingsActivity.getStringFromSharedPreferences(p, Constants.SEARCH_STRING, ""));//prefs.getString(Constants.SEARCH_STRING,""));


        d.setNegativeButton(p.getString(R.string.cancel));
        d.setPositiveButton(p.getString(R.string.search), new InputDialog.OnButtonListener() {
            public void onClick(String value) {
                //form a compatible call
                Intent a = new Intent(p, SearchResultActivity.class);
                a.setAction(Intent.ACTION_SEARCH);
                a.putExtra(SearchManager.QUERY, et.getText().toString().trim());
                a.putExtra(Constants.RETURN_TO_GRAPH, calledfromgraph);
                SettingsActivity.putSharedPreferences(Constants.SEARCH_STRING, et.getText().toString(), p);
                if (calledfromgraph) {
                    p.startActivityForResult(a, SR_CODE);
                } else p.startActivity(a);

            }
        });
        d.show();
    }

    /**
     * @param p
     * @param p true if called from graph, false otherwise
     */
    private static void startSearchDialog(final Activity p) {
        final InputDialog d = new InputDialog(p);
        d.setTitle(p.getString(R.string.dso_search));
        d.setHelp(R.string.search_desc);
        d.insertLayout(R.layout.search_dialog);

        final boolean calledfromgraph = p instanceof GraphActivity;

        final Button db_btn = (Button) d.findViewById(R.id.sd_db_btn);
        final EditText et = (EditText) d.findViewById(R.id.sd_et);
        final CheckBox ch = (CheckBox) d.findViewById(R.id.sd_ch);

        //current search prefs
        SharedPreferences prefs = SettingsActivity.getSharedPreferences(p);//p.getSharedPreferences(Constants.PREFS,Context.MODE_PRIVATE);
        int db_num = prefs.getInt(Constants.SEARCH_DB, -1);

        ch.setChecked(prefs.getBoolean(Constants.SEARCH_FIELDS, true));
        et.setText(SettingsActivity.getStringFromSharedPreferences(p, Constants.SEARCH_STRING, ""));//prefs.getString(Constants.SEARCH_STRING,""));


        //make an array for list
        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
        final String[] db_names = new String[iL.getCount() + 2];
        db_names[0] = p.getString(R.string.all);
        int i = 1;
        int pos = 0;//if db_num==-1 or does not exist pos is left 0
        for (Object o : iL) {
            DbListItem item = (DbListItem) o;
            db_names[i] = item.dbName;
            if (item.cat == db_num) pos = i;
            i++;
        }
        db_names[i] = p.getString(R.string.notes);
        if (db_num == -2)//notes
            pos = i;
        db_btn.setTag(pos);//passing beginning pos
        db_btn.setText(db_names[pos]);

        db_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                InputDialog d = new InputDialog(p);
                d.setValue(String.valueOf((Integer) db_btn.getTag()));
                d.setTitle(p.getString(R.string.select_database_to_search_in));
                d.setListItems(db_names, new InputDialog.OnButtonListener() {
                    public void onClick(String value) {
                        int which = AstroTools.getInteger(value, 0, -1, 10000);
                        db_btn.setText(db_names[which]);
                        db_btn.setTag(which);
                    }
                });
                d.show();
            }
        });

        d.setPositiveButton(p.getString(R.string.search), new InputDialog.OnButtonListener() {
            public void onClick(String value) {
                //form a compatible call
                Intent a = new Intent(p, SearchResultActivity.class);
                a.setAction(Intent.ACTION_SEARCH);
                a.putExtra(SearchManager.QUERY, et.getText().toString());
                a.putExtra(Constants.RETURN_TO_GRAPH, calledfromgraph);

                SettingsActivity.putSharedPreferences(Constants.SEARCH_FIELDS, ch.isChecked(), p);
                SettingsActivity.putSharedPreferences(Constants.SEARCH_STRING, et.getText().toString(), p);

                int pos = (Integer) db_btn.getTag();
                if (pos == 0) {//ALL
                    SettingsActivity.putSharedPreferences(Constants.SEARCH_DB, -1, p);
                } else if (pos == db_names.length - 1) {//Notes
                    if (ImportDatabaseIntentService.isBeingImported(Constants.NOTE_DATABASE_NAME)) {
                        InputDialog.message(p, Global.DB_IMPORT_RUNNING, 0).show();
                        return;
                    }
                    SettingsActivity.putSharedPreferences(Constants.SEARCH_DB, -2, p);
                } else {
                    InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
                    DbListItem item = (DbListItem) iL.get(pos - 1);
                    if (ImportDatabaseIntentService.isBeingImported(item.dbFileName)) {
                        InputDialog.message(p, Global.DB_IMPORT_RUNNING, 0).show();
                        return;
                    }
                    SettingsActivity.putSharedPreferences(Constants.SEARCH_DB, item.cat, p);
                }
                if (calledfromgraph) {
                    p.startActivityForResult(a, SR_CODE);
                } else p.startActivity(a);

            }
        });
        d.show();
    }

    /**
     * for contour objects short byte representation is used
     *
     * @param e
     * @return
     */
    public static Bundle getBundleFromExportable(Exportable e) {
        Bundle bn = new Bundle();
        bn.putInt(OBJ_TYPE, e.getClassTypeId());
        if (e instanceof ContourObject)
            bn.putByteArray(BYTE_ARRAY, ((ContourObject) e).getShortByteRepresentation());
        else bn.putByteArray(BYTE_ARRAY, e.getByteRepresentation());
        return bn;
    }

    public static Exportable getExportableFromBundle(Bundle b) {
        int objType = b.getInt(OBJ_TYPE);
        Log.d(TAG, "obj type=" + objType);
        byte[] objrep = b.getByteArray(BYTE_ARRAY);
        if (objrep != null) {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(objrep));
            Object o = ObjectInflater.getInflater().inflate(objType, dis);
            Log.d(TAG, "o=" + o);
            if (o instanceof Exportable) return (Exportable) o;
            else return null;
        } else return null;

    }

    public static String doubleToGrad(double d, char ch1, char ch2) {
        int sign;
        if (d < 0) sign = -1;
        else sign = 1;
        d = abs(d);
        int grad = (int) d;
        double min = (d - grad) * 60;
        String s = makeNumber(grad) + ch1;
        if (min < 10) s = s + "0" + String.format(Locale.US, "%.1f", min) + ch2;
        else s = s + String.format(Locale.US, "%.1f", min) + ch2;
        if (sign < 0) s = "-" + s;
        return s;

    }

    private static String makeNumber(int i) {
        String s = String.valueOf(i);
        if (i < 10) s = "0" + s;
        return s;
    }

    /**
     * @param f   use only simple zip files without internal directory structure!!!
     * @param dir
     * @throws IOException
     */
    public static void expandZipFile(File f, String dir) throws IOException {
        Log.d(TAG, "inside zip expand");
        ZipInputStream zis = null;
        OutputStream output = null;
        try {
            ZipEntry ze;
            zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(f)));
            while ((ze = zis.getNextEntry()) != null) {

                String filename = ze.getName();
                File dest = new File(dir, filename);
                output = new BufferedOutputStream(new FileOutputStream(dest));

                byte[] buffer = new byte[1024];
                int count;
                while ((count = zis.read(buffer)) != -1) {
                    output.write(buffer, 0, count);
                }

                Log.d(TAG, "zip filename=" + filename);

            }
        } finally {
            zis.close();
            output.close();
        }
    }

    /**
     * @param catid
     * @return position in DB_LIST, -1 if not found
     */
    public static int findPosByCatId(int catid) {
        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
        Iterator it = iL.iterator();
        int pos = -1;
        for (; it.hasNext(); ) {
            DbListItem dbitem = (DbListItem) it.next();
            if (dbitem.cat == catid) return pos + 1;
            pos++;
        }
        return -1;
    }

    public static DbListItem findItemByCatId(int catid) {
        InfoList iL = ListHolder.getListHolder().get(InfoList.DB_LIST);
        Iterator<?> it = iL.iterator();
        int pos = 0;
        for (; it.hasNext(); ) {
            DbListItem dbitem = (DbListItem) it.next();
            if (dbitem.cat == catid) return dbitem;
            pos++;
        }
        return null;
    }

    /**
     * @return free space on sd card
     */

    public static long getSdCardAvailableSpace() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        int availBlocks = stat.getAvailableBlocks();
        int blockSize = stat.getBlockSize();
        long free_memory = (long) availBlocks * (long) blockSize;

        return free_memory;
    }

    public static boolean isExternalStoragePermissionGranted(Activity activity) {
        int perm = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return (perm == PackageManager.PERMISSION_GRANTED);
    }

    public static final int EXT_STORAGE_WRITABLE = 1;
    public static final int EXT_STORAGE_READABLE = 2;


    /**
     * show relevant message for ext storage being not available on UI thread
     *
     * @param type    EXT_STORAGE_WRITABLE, EXT_STORAGE_READABLE
     * @param context should be activity context, not application context for dialogs to work!!!
     */
    public static void showExtStorageNotAvailableMessage(int type, Activity context) {
        ErrorHandler eh = getExtStorageNotAvailableMessage(type, context);
        if (eh != null) eh.showError(context);
    }

    public static ErrorHandler getExtStorageNotAvailableMessage(int type, Context context) {
        String message = "";
        switch (type) {
            case EXT_STORAGE_WRITABLE:
                message = context.getString(R.string.external_storage_not_available_for_writing_);
                break;
            case EXT_STORAGE_READABLE:
                message = context.getString(R.string.external_storage_not_available_for_reading_);
                break;
            default:
                return null;
        }
        ErrorHandler eh = new ErrorHandler(ErrorHandler.IO_ERROR, message);
        return eh;
    }


    /**
     * Calendar basis
     * i.e. month=usual month-1
     *
     * @author leonid
     */
    static class Date {
        int year;
        int month;
        int day;
        int hour;
        int minute;
        int second;

        @Override
        public String toString() {
            return "Date [year=" + year + ", month=" + month + ", day=" + day + ", hour=" + hour + ", minute=" + minute + ", second=" + second + "]";
        }

        public Date(int year, int month, int day, int hour, int minute, int second) {

            this.year = year;
            this.month = month;
            this.day = day;
            this.hour = hour;
            this.minute = minute;
            this.second = second;
        }

        public Calendar getCalendar() {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day, hour, minute, second);
            return c;
        }

        /**
         * @param c - calendar to be set to class fields
         */
        public void changeCalendar(Calendar c) {
            c.set(year, month, day, hour, minute, second);
        }

    }


    public static Date getDateFromCalendar(Calendar c) {
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        return new Date(year, month, day, hour, minute, second);
    }

    /**
     * @param ms - calendar in milliseconds
     * @return
     */
    public static Calendar getCalendarFromMs(Long ms) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(ms);
        return c;
    }

    /**
     * @return default time saved in shared prefs. Former Global.cal
     */
    public static Calendar getDefaultTime(Context context) {
        Long time = SettingsActivity.getSharedPreferences(context).getLong(Constants.GLOBAL_CALENDAR, 0);
        return getCalendarFromMs(time);
    }

    public static void setDefaultTime(Calendar c, Context context) {
        SettingsActivity.putSharedPreferences(Constants.GLOBAL_CALENDAR, c.getTimeInMillis(), context);
    }

    /**
     * @return true if civil twilight is over
     */
    public static boolean isNightModeRequired() {
        Calendar c = Calendar.getInstance();
        Planet sun = new Planet(Planet.PlanetType.Sun, c);
        double lst = AstroTools.sdTime(c);
        double lat = SettingsActivity.getLattitude();
        double alt = AstroTools.Altitude(lst, lat, sun.getRa(), sun.getDec());
        Log.d(TAG, "alt=" + alt);
        if (alt < AstroTools.hCiv) return true;
        else return false;
    }

    /**
     * activity is finished if change in nightmode is required!
     *
     * @param a
     * @param activity number of activity with NightChangeTracker
     * @return true if change required
     */
    public static boolean changeNightModeIfNeeded(Activity a, boolean activityNightMode, int activity) {
        boolean change = NightModeChangeTracker.get(activity);//night mode was changed in Graph/Settings
        if (change) NightModeChangeTracker.clear(activity);
        boolean restart = false;


        if (SettingsActivity.isAutoNightModeOn()) {

            boolean nightModeRequired = AstroTools.isNightModeRequired();
            if (activityNightMode != nightModeRequired) {
                restart = true;
                SettingsActivity.forceNightMode(nightModeRequired);
            }

        }
        if (!restart) {
            restart = change;
        }

        if (restart) {

            Intent intent = a.getIntent();
            a.overridePendingTransition(0, 0);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            a.finish();
            a.overridePendingTransition(0, 0);
            a.startActivity(intent);
            return true;
        }
        return false;
    }

    /**
     * helper method
     *
     * @return the time of the last geo update
     */
    public static String getLastUpdateDateTimeString(Context context) {
        long time = SettingsActivity.getSharedPreferences(context).getLong(Constants.GEO_LAST_UPDATE, -1);
        String timeString = "";
        if (time != -1) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(time);
            timeString = DetailsActivity.makeShortDateTimeString(c);
        }
        return timeString;
    }

    private final static long AUTO_LOCATION_INTERVAL = 1000 * 60 * 60 * 2;//2 hour

    /**
     * helper method for starting autolocation if an hour hass passed since last start
     *
     * @param context
     */
    public static void startAutoLocationIfNeeded(Context context) {
        if (SettingsActivity.getAutoLoc() && doesLocProviderExist(context)) {
            long time = SettingsActivity.getSharedPreferences(context).getLong(Constants.GEO_LAST_START, -1);
            long timenow = Calendar.getInstance().getTimeInMillis();

            if (time == -1 || timenow - time > AUTO_LOCATION_INTERVAL) {//time since the last start is greater than 1 hour
                if (AutoLocation.isStarted()) AutoLocation.stopAll();
                AutoLocation.start(context);
            }
        }
    }

    public static String getStackTrace(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        e.printStackTrace(ps);
        return baos.toString();
    }

    public static String getStackTrace(Throwable e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        //	Log.d(TAG,"exception="+e);
        e.printStackTrace(ps);
        return baos.toString();
    }

    /**
     * @param soapString
     * @param method
     * @return null if method is not found
     */
    public static String getValue(String soapString, String method) {
        int start = soapString.indexOf("<" + method + ">");
        if (start == -1) return null;
        int end = soapString.indexOf("</" + method + ">");
        if (end == -1) return null;
        String parameter = soapString.substring(start, end).replaceAll("<" + method + ">", "");
        parameter = parameter.trim();
        return parameter;
    }


    /**
     * @param s
     * @param map - W,E or N,S to sign (1,-1)
     * @return
     */
    private static Double getLatLonValue(String s, Map<String, Integer> map) { //eg w12 23 36, null stands for error
        String prefix = "";


        double v = 0;
        boolean error = false;
        try {
            v = Double.valueOf(s);
        } catch (NumberFormatException ex) {
            error = true;
        }
        if (!error) return v;
        else {
            String[] sarr = s.split(" ");    //any numeric char
            List<Double> l = new ArrayList<Double>();
            boolean error1 = false;
            try {
                int i = 0;
                for (String sa : sarr) {
                    if (sa.length() > 0) {
                        if (i == 0) {
                            prefix = "" + sa.charAt(0);//prefix at the beginning
                            if (sa.length() > 1)
                                l.add(Double.valueOf(sa.substring(1, sa.length())));
                        } else l.add(Double.valueOf(sa));
                        i++;

                    }
                }
            } catch (Exception ex) {
                error1 = true;
            }
            if (!error1) { //convert DMS->D
                v = 0;
                int len = min(3, l.size());
                int sign = 1;

                for (int i = 0; i < len; i++) {
                    switch (i) {
                        case 0:
                            v = l.get(i);
                            if (v < 0) sign = -1;
                            break;
                        case 1:
                            v = v + l.get(i) / 60f * sign;
                            break;
                        case 2:
                            v = v + l.get(i) / 3600f * sign;
                            break;
                    }
                }
                Integer si = map.get(prefix.toUpperCase(Locale.US));
                if (si == null) return null;
                v = v * si;
                return v;
            }

        }
        return null;

    }

    /**
     * @param lonStr
     * @return null for wrong grammar and range
     */
    public static Double convertLongitude(String lonStr) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("E", 1);
        map.put("W", -1);
        Double lon = getLatLonValue(lonStr, map);
        if (lon == null) return null;
        if (lon > 180 || lon < -180) return null;
        return lon;
    }

    /**
     * @param latStr
     * @return null for wrong grammar and range
     */
    public static Double convertLattitude(String latStr) {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("N", 1);
        map.put("S", -1);
        Double lat = getLatLonValue(latStr, map);
        if (lat == null) return null;
        if (lat > 180 || lat < -180) return null;
        return lat;
    }

    public static String getLatString(double lat) {
        String s = DetailsActivity.doubleToGrad(lat, '\u00B0', (char) 39);
        if (lat < 0) {
            s = s.substring(1, s.length());
            s = "S" + s;
        } else {
            s = "N" + s;
        }
        return s;
    }

    public static String getLonString(double lon) {
        String s = DetailsActivity.doubleToGrad(lon, '\u00B0', (char) 39);
        if (lon < 0) {
            s = s.substring(1, s.length());
            s = "W" + s;
        } else {
            s = "E" + s;
        }
        return s;
    }


    /**
     * return the exis
     * the file is made on the fly and kept in tmp directory
     *
     * @param ngc
     * @return
     */
    public static String getNgcPic(int ngc) {
        int pos = findNgcPic(ngc);
        if (pos == -1) return null;
        InfoList ilist = ListHolder.getListHolder().get(InfoList.NGC_PIC_LIST);
        NgcPicListItem item = (NgcPicListItem) ilist.get(pos);
        String picname = item.getName();
        File f = new File(Global.tmpPath, picname);
        if (f.exists()) return f.getAbsolutePath();

        clearTmpDirIfNecessary();

        long pointer = item.pos;
        long size = item.size;
        RandomAccessFile db = null;
        try {
            String name = APKExpansion.getExpPath(Global.getAppContext(), Global.mainVersion);
            Log.d(TAG, "exp pack name=" + name);
            db = new RandomAccessFile(name, "r");
            db.seek(pointer);
            FileIn fin = new FileIn(db, picname, size, Global.tmpPath);
            fin.read();
            return new File(Global.tmpPath, picname).getAbsolutePath();
        } catch (Exception e) {
            return null;

        } finally {
            try {
                db.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * @param ngc ngc number
     * @return position in the ngc pic list or -1 if this des not exist
     */
    public static int findNgcPic(int ngc) {
        InfoList ilist = ListHolder.getListHolder().get(InfoList.NGC_PIC_LIST);
        try {
            int low = 0;
            int high = ilist.getCount() - 1;
            int i = 0;
            while (abs(high - low) > 1 && i++ < 20) {
                NgcPicListItem lowitem = (NgcPicListItem) ilist.get(low);
                NgcPicListItem highitem = (NgcPicListItem) ilist.get(high);

                int lowval = lowitem.getValue();
                int highval = highitem.getValue();

                if (lowval == -1) return -1;
                if (highval == -1) return -1;

                if (lowval == ngc) return low;
                if (highval == ngc) return high;

                int middle = (low + high) / 2;
                NgcPicListItem middleitem = (NgcPicListItem) ilist.get(middle);
                int middleval = middleitem.getValue();
                if (middleval == ngc) return middle;
                if (middleval > ngc) high = middle;
                else low = middle;
            }
            NgcPicListItem lowitem = (NgcPicListItem) ilist.get(low);
            NgcPicListItem highitem = (NgcPicListItem) ilist.get(high);

            int lowval = lowitem.getValue();
            int highval = highitem.getValue();
            if (lowval == ngc) return low;
            if (highval == ngc) return high;
            return -1;

        } catch (Exception e) {
            return -1;
        }

    }

    /**
     * removing 20 files after reaching 100
     */
    private static void clearTmpDirIfNecessary() {
        File[] list = new File(Global.tmpPath).listFiles();
        if (list == null) return;
        if (list.length > 100) {
            for (int i = 0; i < 20; i++) {
                list[i].delete();
            }
        }
        return;
    }


    /**
     * helper class for finding bright star
     *
     * @author leonid
     */
    private static class bstar {
        int bayer;
        int con;
        int hr;

        public bstar(int bayer, int con, int hr) {
            super();
            this.bayer = bayer;
            this.con = con;
            this.hr = hr;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof bstar) {
                bstar obj = (bstar) o;
                if (bayer == obj.bayer && con == obj.con && hr == obj.hr) return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return 17 * 17 * con + 13 * bayer + hr;
        }
    }

    private static Map<bstar, String> bmap = new HashMap<bstar, String>();

    static {
        bmap.put(new bstar(1, 8, 1708), "Capella");
        bmap.put(new bstar(2, 8, 2088), "Menkalinan");
        bmap.put(new bstar(5, 72, 6879), "Kaus Australis");
        bmap.put(new bstar(1, 64, 99), "Ankaa");
        bmap.put(new bstar(2, 1, 337), "Mirach");
        bmap.put(new bstar(1, 15, 2943), "Procyon");
        bmap.put(new bstar(1, 9, 5340), "Arcturus");
        bmap.put(new bstar(1, 1, 15), "Alpheratz");
        bmap.put(new bstar(3, 1, 603), "Almach");
        bmap.put(new bstar(5, 9, 5506), "Izar");
        bmap.put(new bstar(4, 73, 5953), "Dschubba");
        bmap.put(new bstar(5, 73, 6241), "Wei");
        bmap.put(new bstar(18, 72, 7121), "Nunki");
        bmap.put(new bstar(1, 73, 6134), "Antares");
        bmap.put(new bstar(2, 73, 5984), "Acrab");
        bmap.put(new bstar(1, 18, 168), "Schedar");
        bmap.put(new bstar(11, 73, 6527), "Shaula");
        bmap.put(new bstar(3, 18, 264), "Tsih");
        bmap.put(new bstar(10, 73, 6580), "Girtab");
        bmap.put(new bstar(3, 38, 2421), "Alhena");
        bmap.put(new bstar(2, 30, 4853), "Mimosa");
        bmap.put(new bstar(2, 18, 21), "Caph");
        bmap.put(new bstar(3, 30, 4763), "Gacrux");
        bmap.put(new bstar(2, 38, 2990), "Pollux");
        bmap.put(new bstar(3, 46, 4057), "Algieba");
        bmap.put(new bstar(8, 73, 6553), "Sargas");
        bmap.put(new bstar(2, 46, 4534), "Denebola");
        bmap.put(new bstar(1, 38, 2891), "Castor");
        bmap.put(new bstar(5, 31, 7949), "Gienah");
        bmap.put(new bstar(1, 17, 2326), "Canopus");
        bmap.put(new bstar(2, 17, 3685), "Miaplacidus");
        bmap.put(new bstar(1, 39, 8425), "Alnair");
        bmap.put(new bstar(1, 31, 7924), "Deneb");
        bmap.put(new bstar(5, 17, 3307), "Avior");
        bmap.put(new bstar(3, 31, 7796), "Sadr");
        bmap.put(new bstar(1, 46, 3982), "Regulus");
        bmap.put(new bstar(9, 17, 3699), "Aspidiske");
        bmap.put(new bstar(1, 67, 8728), "Fomalhaut");
        bmap.put(new bstar(1, 81, 6217), "Atria");
        bmap.put(new bstar(1, 20, 8162), "Alderamin");
        bmap.put(new bstar(2, 84, 5563), "Kochab");
        bmap.put(new bstar(1, 52, 7001), "Vega");
        bmap.put(new bstar(1, 60, 2061), "Betelgeuse");
        bmap.put(new bstar(1, 61, 7790), "Peacock");
        bmap.put(new bstar(2, 60, 1713), "Rigel");
        bmap.put(new bstar(1, 84, 424), "Polaris");
        bmap.put(new bstar(3, 60, 1790), "Bellatrix");
        bmap.put(new bstar(1, 36, 472), "Achernar");
        bmap.put(new bstar(4, 60, 1852), "Mintaka");
        bmap.put(new bstar(5, 60, 1903), "Alnilam");
        bmap.put(new bstar(7, 59, 6378), "Sabik");
        bmap.put(new bstar(6, 60, 1948), "Alnitak");
        bmap.put(new bstar(6, 68, 3165), "Naos");
        bmap.put(new bstar(10, 60, 2004), "Saiph");
        bmap.put(new bstar(3, 19, 4819), "Muhlifain");
        bmap.put(new bstar(1, 19, 5459), "Rigil Kent");
        bmap.put(new bstar(1, 27, 5793), "Alphecca");
        bmap.put(new bstar(1, 5, 7557), "Altair");
        bmap.put(new bstar(2, 19, 5267), "Agena");
        bmap.put(new bstar(3, 83, 4554), "Phecda");
        bmap.put(new bstar(1, 59, 6556), "Rasalhague");
        bmap.put(new bstar(1, 62, 8781), "Markab");
        bmap.put(new bstar(2, 83, 4295), "Merak");
        bmap.put(new bstar(1, 83, 4301), "Dubhe");
        bmap.put(new bstar(7, 83, 5191), "Benetnasch");
        bmap.put(new bstar(6, 83, 5054), "Mizar");
        bmap.put(new bstar(5, 83, 4905), "Alioth");
        bmap.put(new bstar(7, 19, 5440), "Marfikent");
        bmap.put(new bstar(8, 19, 5288), "Menkent");
        bmap.put(new bstar(1, 50, 5469), "Men");
        bmap.put(new bstar(1, 14, 2491), "Sirius");
        bmap.put(new bstar(1, 42, 3748), "Alphard");
        bmap.put(new bstar(1, 30, 4730), "Acrux");
        bmap.put(new bstar(3, 34, 6705), "Eltanin");
        bmap.put(new bstar(1, 86, 5056), "Spica");
        bmap.put(new bstar(5, 62, 8308), "Enif");
        bmap.put(new bstar(2, 78, 1791), "El Nath");
        bmap.put(new bstar(2, 62, 8775), "Scheat");
        bmap.put(new bstar(2, 21, 188), "Deneb Kaitos");
        bmap.put(new bstar(1, 78, 1457), "Aldebaran");
        bmap.put(new bstar(4, 85, 3485), "KooShe");
        bmap.put(new bstar(3, 85, 3207), "Suhail");
        bmap.put(new bstar(2, 63, 936), "Algol");
        bmap.put(new bstar(7, 14, 2827), "Aludra");
        bmap.put(new bstar(1, 63, 1017), "Mirfak");
        bmap.put(new bstar(4, 14, 2693), "Wezen");
        bmap.put(new bstar(5, 14, 2618), "Adara");
        bmap.put(new bstar(10, 85, 3734), "Markeb");
        bmap.put(new bstar(2, 14, 2294), "Mirzam");
        bmap.put(new bstar(1, 7, 617), "Hamal");

    }

    /**
     * @param bayer
     * @param con
     * @return null if there is not one
     */

    public static String getBrightStarName(int bayer, int con, int hr) {
        String name = bmap.get(new bstar(bayer, con, hr));
        return name;
    }


    /**
     * @param name in the format HR1
     * @return null if no star found
     */
    public static HrStar getHrStar(String name) {
        try {
            return Global.databaseHr[StarData.ConvHrToRow(Integer.parseInt(name.replace("HR", "").replace(" ", "")))];
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param name
     * @return null if there not one
     */
    public static HrStar findFlamsteedStar(String name) {
        for (HrStar star : Global.databaseHr) {
            String fname = star.getFlamsteedName();
            if (fname != null) {
                fname = fname.toUpperCase();
                if (name.toUpperCase(Locale.US).equals(fname)) return new HrStar(star);
            }

        }
        return null;
    }


    /**
     * 360 basis
     *
     * @param an
     * @return
     */
    public static double normalise(double an) {
        an = an - Math.floor(an / 360) * 360;
        if (an >= 0 && an < 360) return an;
        return 0;
    }

    public static double normalise24(double an) {
        an = an - Math.floor(an / 24) * 24;
        if (an >= 0 && an < 24) return an;
        return 0;
    }

    public static double normalise90(double an) {
        if (an >= -90 && an <= 90) return an;
        return 0;
    }

    private static float dx;
    private static float dy;

    public static void setLabelPathRotation() {
        CustomPoint p2 = new CustomPoint(Point.getAzCenter() + 1, Point.getAltCenter(), "");
        p2.setXY();
        p2.setDisplayXY();
        dx = p2.xd - Point.getWidth() / 2;
        dy = p2.yd - Point.getHeight() / 2;
    }

    /**
     * Path from start to the screen end
     * The second point gives direction
     *
     * @param x1
     * @param y1
     * @return
     */
    public static Path getLabelPath(float x1, float y1) {
        //y=a+bx
        return getLabelPath(x1, y1, new Path());

    }

    /**
     * Path from start to the screen end
     * The second point gives direction
     * path - clean path, to save on object creation
     *
     * @param x1
     * @param y1
     * @return
     */
    public static Path getLabelPath(float x1, float y1, Path path) {
        //y=a+bx

        float x2 = x1 + dx;
        float y2 = y1 + dy;
        float width = Point.getWidth();

        path.moveTo(x1, y1);
        if (Math.abs(x1 - x2) < 1e-3) {
            if (y1 > y2) {
                path.lineTo(x1, y1 - width);
            } else path.lineTo(x1, y1 + width);
            return path;
        }
        if (x2 > x1) path.lineTo(x1 + width, y1 + (y2 - y1) / (x2 - x1) * width);
        else path.lineTo(x1 - width, y1 - (y2 - y1) / (x2 - x1) * width);
        return path;

    }

    private static Map<Integer, String> commap = new HashMap<Integer, String>();

    static {
        commap.put(7654, "43617373696f706569612053616c742d616e642d5065707065723b204f63746f6265722053616c742d616e642d506570706572");
        commap.put(2447, "427574746572666c7920436c7573746572");
        commap.put(6334, "436174277320506177204e6562756c61");
        commap.put(6811, "486f6c6520696e206120436c7573746572");
        commap.put(7662, "426c756520536e6f7762616c6c204e6562756c61");
        commap.put(281, "5061634d616e204e6562756c61");
        commap.put(4622, "43656e74617572757320436861696e");
        commap.put(3532, "46697265666c7920506172747920436c7573746572");
        commap.put(4616, "43656e74617572757320436861696e");
        commap.put(5218, "4b65656e616e27732053797374656d");
        commap.put(4889, "436f6d6120436c7573746572");
        commap.put(5216, "4b65656e616e27732053797374656d");
        commap.put(4631, "48657272696e672047616c61787920284e6562756c61293b205768616c652047616c617879");
        commap.put(5236, "536f75746865726e2050696e776865656c2047616c617879");
        commap.put(6537, "52656420537069646572204e6562756c61");
        commap.put(4039, "416e74656e6e61652047616c61786965733b2052696e67205461696c2047616c6178696573");
        commap.put(2239, "526f7365747465204e6562756c61");
        commap.put(6543, "436174e280997320457965204e6562756c61");
        commap.put(4038, "416e74656e6e61652047616c61786965733b2052696e67205461696c2047616c6178696573");
        commap.put(2237, "526f7365747465204e6562756c61");
        commap.put(6302, "427567204e6562756c61");
        commap.put(5194, "4c6f726420526f7373652773204e6562756c61202847616c617879293b205175657374696f6e204d61726b3b20576869726c706f6f6c2047616c617879");
        commap.put(4650, "43656e74617572757320436861696e");
        commap.put(5457, "50696e776865656c2047616c617879");
        commap.put(4661, "43656e74617572757320436861696e");
        commap.put(7635, "427562626c65204e6562756c61");
        commap.put(6826, "426c696e6b696e67204e6562756c61");
        commap.put(12177, "4561676c65204e6562756c613b2053656167756c6c204e6562756c61");
        commap.put(4656, "43726f776261722047616c617879");
        commap.put(6822, "4261726e617264e28099732047616c617879");
        commap.put(4666, "537570657277696e642d47616c617879");
        commap.put(6818, "4c6974746c652047656d");
        commap.put(7088, "426178656e64616c6c2773204e6562756c61");
        commap.put(2264, "4368726973746d6173205472656520436c75737465723b20436f6e65204e6562756c612028436f6e7573204e2e29");
        commap.put(884, "436869205065727365693b20446f75626c6520436c75737465723b205065727365757320446f75626c6520436c7573746572");
        commap.put(1039, "53706972616c20436c7573746572");
        commap.put(12602, "536f75746865726e20506c6569616465733b20546865746120436172696e6165");
        commap.put(2261, "487562626c65e2809973205661726961626c65");
        commap.put(891, "53696c76657220536c697665722047616c617879");
        commap.put(5907, "53706c696e7465722047616c617879");
        commap.put(1598, "4361726166652047726f7570");
        commap.put(7129, "536d616c6c20436c7573746572204e6562756c61");
        commap.put(1595, "4361726166652047726f7570");
        commap.put(869, "446f75626c6520436c75737465723b2068205065727365693b205065727365757320446f75626c6520436c7573746572");
        commap.put(6853, "4170706c6520436f7265204e6562756c613b20446961626c6f204e6562756c613b20446f75626c652d4865616465642053686f743b2044756d6262656c6c204e6562756c61");
        commap.put(6369, "4c6974746c652047686f7374204e6562756c61");
        commap.put(4438, "45796573");
        commap.put(5128, "43656e74617572757320413b2048616d6275726765722047616c617879");
        commap.put(6905, "426c756520466c617368204e6562756c61");
        commap.put(7317, "5374657068616e2773205175696e746574");
        commap.put(2537, "42656172205061772028436c6177292047616c617879");
        commap.put(6603, "44656c6c6520436175737469636865");
        commap.put(4435, "45796573");
        commap.put(7319, "5374657068616e2773205175696e746574");
        commap.put(1068, "43657475732041");
        commap.put(7318, "5374657068616e2773205175696e746574");
        commap.put(6357, "4c6f6273746572204e6562756c613b2057617220616e64205065616365204e6562756c61");
        commap.put(7320, "5374657068616e2773205175696e746574");
        commap.put(380, "50697363657320436c6f7564");
        commap.put(383, "50697363657320436c6f7564");
        commap.put(382, "50697363657320436c6f7564");
        commap.put(3034, "426f646527732047616c617869657320284e6562756c6165293b2043696761722047616c6178793b2055727361204d616a6f722041");
        commap.put(6888, "4372657363656e74204e6562756c61");
        commap.put(6618, "436865636b6d61726b204e6562756c613b20486f72736573686f65204e6562756c613b204c6f6273746572204e6562756c613b204f6d656761204e6562756c613b205377616e204e6562756c61");
        commap.put(598, "547269616e67756c756d2047616c6178793b20547269616e67756c756d2050696e776865656c2047616c617879");
        commap.put(3242, "434253204579653b20457965204e6562756c613b2047686f7374206f66204a757069746572");
        commap.put(10405, "466c616d696e672053746172206e6562756c61");
        commap.put(1316, "466f726e61782041");
        commap.put(3031, "426f646527732047616c6178792c20426f64652773204e6562756c61");
        commap.put(6885, "32302056756c706563756c616520436c7573746572");
        commap.put(7293, "53756e666c6f776572204e6562756c613b2048656c6978204e6562756c61");
        commap.put(6913, "436f6f6c696e6720546f776572");
        commap.put(2070, "333020446f72616475733b204c6f6f706564204e6562756c613b2054727565204c6f7665727327204b6e6f74");
        commap.put(14703, "4561676c65204e6562756c613b205374617220517565656e204e6562756c61");
        commap.put(4535, "4c6f73742047616c6178793b204d634c656973682773204f626a656374");
        commap.put(404, "4d697261636827732047686f7374");
        commap.put(6205, "48657263756c657320476c6f62756c617220436c7573746572");
        commap.put(2323, "48656172742d53686170656420436c7573746572");
        commap.put(6451, "546f6d205468756d6220436c7573746572");
        commap.put(4526, "4c6f73742047616c617879");
        commap.put(2024, "466c616d65204e6562756c613b204f72696f6e20423b2054616e6b20747261636b73");
        commap.put(385, "50697363657320436c6f7564");
        commap.put(384, "50697363657320436c6f7564");
        commap.put(4755, "4b617070612043727563697320436c7573746572");
        commap.put(2099, "4175726967612053616c742d616e642d5065707065723b204a616e756172792053616c742d616e642d506570706572");
        commap.put(5055, "53756e666c6f7765722067616c617879");
        commap.put(11805, "4865617274204e6562756c61");
        commap.put(3195, "47616d6d61204c656f6e69732047726f7570");
        commap.put(651, "4170706c6520436f7265204e6562756c613b2042617262656c6c204e6562756c613b20427574746572666c79204e6562756c613b20436f726b204e6562756c613b204c6974746c652044756d6262656c6c204e6562756c61");
        commap.put(6405, "427574746572666c7920436c7573746572");
        commap.put(650, "4170706c6520436f7265204e6562756c613b2042617262656c6c204e6562756c613b20427574746572666c79204e6562756c613b20436f726b204e6562756c613b204c6974746c652044756d6262656c6c204e6562756c61");
        commap.put(6705, "4a756c792053616c742d616e642d5065707065723b2053637574756d2053616c742d616e642d5065707065723b2057696c64204475636b20436c7573746572");
        commap.put(5866, "5370696e646c652047616c617879");
        commap.put(7252, "41746f6d20466f722050656163652047616c617879");
        commap.put(3190, "47616d6d61204c656f6e69732047726f7570");
        commap.put(6169, "4d75204e6f726d616520436c7573746572");
        commap.put(1535, "436c656f7061747261277320457965");
        commap.put(3187, "47616d6d61204c656f6e69732047726f7570");
        commap.put(3185, "47616d6d61204c656f6e69732047726f7570");
        commap.put(14406, "526574696e61204e6562756c61");
        commap.put(2080, "47686f73742048656164204e6562756c61");
        commap.put(6960, "436972727573204e6562756c613b204379676e7573204c6f6f703b2046696c616d656e74617279204e6562756c613b204c6163652d776f726b204e6562756c613b205665696c204e6562756c61");
        commap.put(2359, "4475636b204e6562756c613b2054686f7227732048656c6d6574");
        commap.put(4254, "436f6d612050696e776865656c2047616c6178793b2053742e204b6174686572696e65277320576865656c3b20566972676f20436c75737465722050696e776865656c2047616c2e");
        commap.put(10349, "4d65726f7065204e6562756c612050617274");
        commap.put(2362, "4d65786963616e204a756d70696e6720537461723b2054617520434d6120636c7573746572");
        commap.put(4486, "566972676f2041");
        commap.put(6979, "466c656d696e67277320547269616e67756c6172204e2e3b205069636b6572696e67277320547269616e67756c6172204e2e");
        commap.put(6741, "5068616e746f6d2053747265616b");
        commap.put(14628, "507261776e204e6562756c61");
        commap.put(1435, "4d65726f7065204e6562756c613b2054656d70656c2773204e6562756c61");
        commap.put(1432, "506c6569616465733b20536576656e2053697374657273");
        commap.put(1982, "4465204d616972616e2773204e6562756c61");
        commap.put(1976, "4f72696f6e204e6562756c61");
        commap.put(2685, "50616e63616b65");
        commap.put(4594, "536f6d627265726f2047616c617879");
        commap.put(6720, "52696e67204e6562756c61");
        commap.put(6995, "436972727573204e6562756c613b204379676e7573204c6f6f703b204e6574776f726b204e6562756c613b205665696c204e6562756c61");
        commap.put(6992, "436972727573204e6562756c613b204379676e7573204c6f6f703b204e6574776f726b204e6562756c613b205665696c204e6562756c61");
        commap.put(3628, "4c656f20547269706c6574");
        commap.put(4826, "426c61636b204579652047616c6178793b20536c656570696e67204265617574792047616c617879");
        commap.put(1952, "43726162204e6562756c613b205461757275732041");
        commap.put(3627, "4c656f20547269706c6574");
        commap.put(6514, "547269666964204e6562756c61");
        commap.put(457, "447261676f6e666c7920436c75737465723b20455420436c75737465723b204f776c20436c75737465723b205068692043617373696f706569616520436c7573746572");
        commap.put(6729, "5220437241204e6562756c61");
        commap.put(3623, "4c656f20547269706c6574");
        commap.put(7000, "4e6f72746820416d6572696361204e6562756c61");
        commap.put(12948, "52756e6e696e6720436869636b656e204e6562756c61");
        commap.put(1960, "50696e776865656c20436c7573746572");
        commap.put(12944, "52756e6e696e6720436869636b656e204e6562756c61");
        commap.put(2392, "436c6f776e204e6562756c612c20436c6f776e2046616365204e2e3b2045736b696d6f204e6562756c61");
        commap.put(6523, "4c61676f6f6e204e6562756c61");
        commap.put(7009, "53617475726e204e6562756c61");
        commap.put(14593, "5768697465204579656420506561");
        commap.put(6231, "5461626c65206f662053636f7270697573");
        commap.put(4568, "5369616d657365205477696e73");
        commap.put(3132, "45696768742d627572737420506c616e65746172793b20536f75746865726e2052696e67204e6562756c61");
        commap.put(4567, "5369616d657365205477696e73");
        commap.put(14603, "52686f204f7068697563686920436f6d706c6578");
        commap.put(6475, "50746f6c656d79277320436c7573746572");
        commap.put(15146, "436f636f6f6e204e6562756c61");
        commap.put(14604, "52686f204f7068697563686920436f6d706c6578");
        commap.put(7023, "5265666c656374696e67204e6562756c61");
        commap.put(225, "5361696c626f617420436c7573746572");
        commap.put(2169, "2733372720436c75737465723b20274c452720436c7573746572");
        commap.put(224, "416e64726f6d6564612047616c617879");
        commap.put(11848, "536f756c204e6562756c61");
        commap.put(253, "5363756c70746f722047616c6178793b2053696c76657220436f696e2047616c617879");
        commap.put(1922, "537461726669736820436c7573746572");
        commap.put(2419, "496e74657267616c61637469632057616e64657265722f5472616d70");
        commap.put(6210, "547572746c6520506c616e6574617279204e6562756c61");
        commap.put(2632, "4265656869766520436c75737465723b204d616e6765723b205072616573657065");
        commap.put(2155, "43617665204e6562756c61");
        commap.put(3587, "4f776c204e6562756c61");
    }

    /**
     * ref - ngc (of commap)
     */
    public static Map<Integer, Integer> commapdb = new HashMap<Integer, Integer>();

    public static boolean isComMapDbEmpty() {
        return (commapdb.size() == 0);
    }

    /**
     * ref - ngc (of commap in AstroTools)
     */
    public static void fillComMapDb(Context context) {
        Log.d(TAG, "start");
        Db db = new Db(context, "ngcic.db");
        try {
            db.open();
            for (int i : commap.keySet()) {
                Cursor cursor = db.rawQuery("select ref from ngcic where name=" + i);
                if (cursor.moveToNext()) {
                    int ref = cursor.getInt(0);
                    commapdb.put(ref, i);
                }
            }

        } catch (Exception e) {
            Log.d(TAG, "e=" + e);
        } finally {
            try {
                db.close();

            } catch (Exception e) {
            }
        }
        Log.d(TAG, "end");
    }


    /**
     * @param ngc
     * @return
     */
    public static String getCommonName(int ngc) {
        String s = commap.get(ngc);
        if (s == null) return null;
        return SettingsActivity.getStringFromHexRepresentation(s);
    }

    /**
     * partial search
     *
     * @param name
     * @return
     */
    public static List<String> searchCommonNames(String name) {
        List<String> list = new ArrayList<String>();
        String query = name.toUpperCase();
        if (name.length() < Global.COMMON_NAME_MIN_LENGTH_FOR_EXT_SEARCH) return list;
        for (Map.Entry<Integer, String> e : commap.entrySet()) {
            String cname = SettingsActivity.getStringFromHexRepresentation(e.getValue()).toUpperCase();
            if (cname.contains(query)) list.add(NgcicObject.getNgcIcName(e.getKey()));
        }
        return list;
    }

    /**
     * @param obj
     * @return common name based on ref field, or null if there is none
     */
    public static String getCommonNameFromRef(AstroObject obj) {
        if (obj.ref == 0) return null;
        Integer ngc = commapdb.get(obj.ref);
        if (ngc == null) return null;
        return getCommonName(ngc);
    }


    public static void logr(String s) {
        //MyExTracker.saveInfo2(Global.log,s,null);
    }

    public static void logd(String s) {
        //MyExTracker.saveInfo(Global.log,s,null);
    }

    public static void logd2(String s) {

    }

    public static void resolveService(Context context, Intent intent) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentServices(intent, 0);
        if (list == null) {
            MyExTracker.saveInfo2(Global.log, "null list", null);
            return;
        }
        if (list.size() == 0) {
            MyExTracker.saveInfo2(Global.log, "empty list", null);
        }
        for (ResolveInfo i : list) {
            MyExTracker.saveInfo2(Global.log, "" + i + " " + i.describeContents(), null);
        }
    }


    private static String getCorrectPKName(String s) {
        if (!s.matches("PK[0-9]+[\\+\\-][0-9]+.*")) return null;
        Pattern p = Pattern.compile("PK[0-9]+");
        Matcher m = p.matcher(s);
        String s2 = "";
        String s3;
        if (m.find()) {
            s2 = s.substring(m.start(), m.end());
            if (s2.length() < 5) {
                int count = 5 - s2.length();
                s3 = s2.replace("PK", "");
                for (int i = 0; i < count; i++) {
                    s3 = "0" + s3;

                }
                s3 = "PK" + s3;
            } else s3 = s2;
        } else return null;

        String s4 = s.replace(s2, "");
        if (!(s4.charAt(0) == '+' || s4.charAt(0) == '-')) return null;
        s3 = s3 + s4.charAt(0);
        s4 = s4.substring(1, s4.length());
        p = Pattern.compile("[0-9]+");
        m = p.matcher(s4);

        if (m.find()) {
            int st = m.start();
            int e = m.end();
            String s5 = s4.substring(st, e);
            if (s5.length() < 2) {
                s5 = "0" + s5;
            }
            s4 = s3 + s5 + s4.substring(e, s4.length());
        } else return null;

        return s4;

    }

    /**
     * key=catalog abbreviation in Sky Safari or Sky Tools
     * value=full catalog name
     */
    static Map<String, String> catmap = new HashMap<String, String>();

    static {
        catmap.put("ACO", "ABELL");//Sky Tools. Abell Galaxy Cluster (ACO)
        catmap.put("HCG", "HICKSON"); //Sky Tools. Designations from the Hickson Compact Groups are entered by following Hickson or HCG by a number.
        catmap.put("ARO", "ABELL"); //Sky Tools
        catmap.put("MKN", "MARKARIAN");//Sky Tools

    }

    /**
     * key=catalog in usual form
     * value=catalog name in SAC
     */
    static Map<String, String> sacmap = new HashMap<String, String>();

    static {
        sacmap.put("BARKHATOVA", "BARK");
        sacmap.put("BERKELEY", "BERK");
        sacmap.put("BERNES", "BE");
        sacmap.put("BIURAKAN", "BIUR");
        sacmap.put("CEDERBLAD", "CED");
        sacmap.put("COLLINDER", "CR");
        sacmap.put("DOLIDZE", "DO");
        sacmap.put("DUNLOP", "DUN");
        sacmap.put("FEINSTEIN", "FEIN");
        sacmap.put("HENIZE", "HE");
        sacmap.put("HOLMBERG", "HO");
        sacmap.put("HUMASON", "HU");
        sacmap.put("ISKUDARIAN", "ISK");
        sacmap.put("JONCKHEERE", "J");
        sacmap.put("KOHOUTEK", "K");
        sacmap.put("KRASNOGORSKAJA", "KR");
        sacmap.put("LACAILLE", "LAC");
        sacmap.put("MERRILL", "ME");
        sacmap.put("MARKARIAN", "MRK");
        sacmap.put("MELOTTE", "MEL");
        sacmap.put("MINKOWSKI", "M");
        sacmap.put("PALOMAR", "PAL");
        sacmap.put("RUPRECHT", "RU");
        sacmap.put("SANDQVIST", "SA");
        sacmap.put("SHARPLESS", "SH");
        sacmap.put("STEPHENSON", "STEPH");
        sacmap.put("TERZAN", "TER");
        sacmap.put("TONANTZINTLA", "TON");
        sacmap.put("TRUMPLER", "TR");
        sacmap.put("VYSSOTSKY", "VY");
        sacmap.put("ZWICKY", "ZWG");
        sacmap.put("DOL-DZIM", "DODZ");
        sacmap.put("VANDENBERGH-HAGEN", "VDB-HA");
    }


    /**
     * @param mapi    contains pairs DB / NUMBER
     * @param crossdb whether to search in cross database, use true for direct call.
     *                false is used in recursion???
     * @return
     */
    public static AstroObject getObject(Map<String, String> mapi, Context context, ErrorHandler eh, boolean crossdb) {
        class Request {
            AstroCatalog cat;
            String sqlreq;

            public Request(AstroCatalog cat, String sqlreq) {
                super();
                this.cat = cat;
                this.sqlreq = sqlreq;
            }

        }

        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String, String> e : mapi.entrySet()) {
            String key = e.getKey().toUpperCase();
            if (key.equals("LEDA")) key = "PGC";
            String value = e.getValue();
            map.put(key, value);
        }


        Log.d(TAG, "getObject");
        List<Request> list = new ArrayList<Request>();
        for (Map.Entry<String, String> e : map.entrySet()) {
            Log.d(TAG, e.getKey() + " " + e.getValue());
        }

        if (map.containsKey("HR")) {
            String value = map.get("HR");
            AstroObject obj = AstroTools.getHrStar("HR" + value);
            if (obj != null) return obj;
        }
        //looking for hr from cross search where we get the whole name
        for (Map.Entry<String, String> e : map.entrySet()) {

            if (e.getKey().matches("HR[0-9]+")) {

                AstroObject obj = AstroTools.getHrStar(e.getKey());
                if (obj != null) return obj;
            }
        }
        //looking for flamsteed
        for (Map.Entry<String, String> e : map.entrySet()) {
            //flamsteed
            if (e.getKey().matches("[0-9]+") && e.getValue().matches("[a-zA-Z]+")) {

                AstroObject obj = AstroTools.findFlamsteedStar(e.getKey() + " " + e.getValue());
                if (obj != null) return obj;
            }
        }

        if (map.containsKey("NGC")) {
            String str = map.get("NGC");
            String req = Constants.NAME1 + "=" + "'NGC" + str + "'";
            Request r = new Request(new NgcicDatabase(context), req);
            list.add(r);
        }
        if (map.containsKey("IC")) {
            String str = map.get("IC");
            String req = Constants.NAME1 + "=" + "'IC" + str + "'";//(10000+num);
            Request r = new Request(new NgcicDatabase(context), req);
            list.add(r);
        }
        String[] keys = new String[]{"LDN", "LBN", "UGC", "WDS", "BARNARD", "PGC", "PK", "SH2", "M"};
        boolean flag = false;//if the key is in the keys
        boolean pgcflag = false;
        for (String key : keys) {
            if (map.containsKey(key)) {
                flag = true;
                String req;
                String name = "";
                if (key.equals("BARNARD")) name = "B" + map.get(key);
                else if (key.equals("PK")) {
                    String s2 = getCorrectPKName("PK" + map.get(key));
                    if (s2 != null) name = s2;
                    else name = map.get(key);
                } else if (key.equals("WDS")) {
                    name = map.get(key);
                } else {
                    name = key + map.get(key);

                }
                req = CustomDatabase.NAME1 + CustomDatabase.LIKE + "'" + name + "'" + CustomDatabase.OR + CustomDatabase.NAME2 + CustomDatabase.LIKE + "'" + name + "'";//Constants.NAME1+"="+key+map.get(key);

                int cat = 0;
                if (key.equals("LDN")) cat = AstroCatalog.DNLYNDS;
                else if (key.equals("LBN")) cat = AstroCatalog.BNLYNDS;
                else if (key.equals("UGC")) cat = AstroCatalog.UGC;
                else if (key.equals("WDS")) cat = AstroCatalog.WDS;
                else if (key.equals("BARNARD")) cat = AstroCatalog.DNBARNARD;
                else if (key.equals("PGC")) {
                    cat = AstroCatalog.PGC;
                    pgcflag = true;
                } else if (key.equals("PK")) cat = AstroCatalog.PK;
                else if (key.equals("SH2")) cat = AstroCatalog.SH2;
                else if (key.equals("M")) cat = AstroCatalog.MESSIER;


                AstroCatalog db = SearchRules.getCatalog(cat, context);
                if (db != null) {
                    Request r = new Request(db, req);
                    list.add(r);
                }
            }
        }


        //for all looking in SAC
        for (Map.Entry<String, String> e : map.entrySet()) {
            String name = catmap.get(e.getKey());//there is a key in abbreviation catalog
            if (name == null) name = e.getKey();
            String sacname = sacmap.get(name);
            if (sacname == null) sacname = name;
            sacname += e.getValue();

            AstroCatalog db = SearchRules.getCatalog(AstroCatalog.SAC, context);
            if (db != null) {
                String req = CustomDatabase.NAME1 + CustomDatabase.LIKE + "'" + sacname.replace("'", "''") + "'" + CustomDatabase.OR + CustomDatabase.NAME2 + CustomDatabase.LIKE + "'" + sacname.replace("'", "''") + "'";//Constants.NAME1+"="+key+map.get(key);

                Request r = new Request(db, req);
                list.add(r);
            }
        }
        //for all looking in MISC
        for (Map.Entry<String, String> e : map.entrySet()) {

            AstroCatalog db = SearchRules.getCatalog(AstroCatalog.MISC, context);

            String search = e.getKey() + e.getValue();
            if (db != null) {
                String req = CustomDatabase.NAME1 + CustomDatabase.LIKE + "'" + search.replace("'", "''") + "'" + CustomDatabase.OR + CustomDatabase.NAME2 + CustomDatabase.LIKE + "'" + search.replace("'", "''") + "'";//Constants.NAME1+"="+key+map.get(key);

                Request r = new Request(db, req);
                list.add(r);
            }
        }

        //for all looking in HAAS
        for (Map.Entry<String, String> e : map.entrySet()) {
            AstroCatalog db = SearchRules.getCatalog(AstroCatalog.HAAS, context);

            String search = e.getKey() + e.getValue();
            if (db != null) {
                String req = CustomDatabase.NAME1 + CustomDatabase.LIKE + "'" + search.replace("'", "''") + "'" + CustomDatabase.OR + CustomDatabase.NAME2 + CustomDatabase.LIKE + "'" + search.replace("'", "''") + "'";//Constants.NAME1+"="+key+map.get(key);

                Request r = new Request(db, req);
                list.add(r);
            }
        }


        //returning the first successfull request
        for (Request r : list) {

            r.cat.open(eh);
            if (eh.hasError()) {

                continue;
            }
            List<AstroObject> list2 = r.cat.search(r.sqlreq);
            r.cat.close();
            if (list2.size() > 0) {
                return list2.get(0);
            }

        }

        if (crossdb) {
            for (Map.Entry<String, String> e : map.entrySet()) {
                String name = e.getKey() + e.getValue();
                List<String> list2 = CrossDb.searchName(name, context);

                if (e.getKey().equals("PN") && !"".equals(e.getValue())) {
                    list2.addAll(CrossDb.searchName(e.getValue(), context));
                }
                Map<String, String> map2 = new HashMap<String, String>();
                List<String> keys2 = new ArrayList<String>();
                for (String s : keys) {
                    keys2.add(s);
                }
                keys2.add("NGC");
                keys2.add("IC");
                for (String s : list2) {
                    Log.d(TAG, "crossdb, s=" + s + " where name=" + name);
                    //breaking names from cross search into key - value pairs
                    for (String keystr : keys2) {
                        if (s.startsWith(keystr)) {
                            String s2 = s.replace(keystr, "");
                            map2.put(keystr, s2);
                            //found=true;
                            break;
                        }
                    }
                    //put the whole name to search in SAC as well
                    map2.put(s, "");

                }
                Log.d(TAG, "map2=" + map2);
                /*
                 * map2 like
                 * map2={WDS22288-0001=, BD-00 4365B=, HD213051=, STF2909=, ADS15971=, TYC5226-1605-2=, WDS=22288-0001}

                 */

                if (map2.size() > 0) {
                    AstroObject obj = getObject(map2, context, eh, false);
                    Log.d(TAG, "object from cross search=" + obj);
                    if (obj != null) return obj;
                }
            }
        }

        if (pgcflag) {
            String numstr = map.get("PGC");
            int id = AstroTools.getInteger(numstr, -1, 0, Integer.MAX_VALUE);
            if (id != -1) {
                AstroObject obj = SearchResultActivity.findPGCinStarChartLayer(id);
                if (obj != null) return obj;
            }

        }
        if (map.containsKey("TYC")) {
            String name = map.get("TYC");
            if (name.toUpperCase().matches("[0-9]+[\\-][0-9]+[\\-][0-9]+")) {
                AstroObject obj = SearchResultActivity.findTYCinStarChartLayer("TYC" + name, context);
                return obj;
            }
        }

        eh.addError(ErrorHandler.UNRECOGNISED_OBJECT, "");
        return null;

    }


    /**
     * @param ra1
     * @param dec1
     * @param ra2
     * @param dec2
     * @return distance in degrees
     */
    public static double getDst(double ra1, double dec1, double ra2, double dec2) {
        double cosd = sin(dec1 * PI / 180) * sin(dec2 * PI / 180) + cos(dec1 * PI / 180) * cos(dec2 * PI / 180) * cos((ra1 - ra2) * PI / 12);
        if (cosd <= 1) return Math.acos(cosd) * 180 / PI;
        else return 0;

    }

    /**
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return distance on earth surface in meters
     */
    public static double getEarthDst(double lat1, double lon1, double lat2, double lon2) {
        double a = getDst(lon1 * 24 / 360, lat1, lon2 * 24 / 360, lat2);
        return (2 * PI * 6371000 * a / 360);
    }

    private static double prev_lat = 0;
    private static double prev_lon = 0;
    private static String prev_loc_name = null;

    private static void savePrevLocation(double lat, double lon, String name) {
        prev_lat = lat;
        prev_lon = lon;
        prev_loc_name = name;
    }

    /**
     * @param lat
     * @param lon
     * @return location name where distance from it is less than 100 meters or null otherwise
     */
    public static String getLocationName(double lat, double lon, Context context) {

        class data {
            double lat;
            double lon;
            String name;
            double dst;

            public data(double lat, double lon, String name, double dst) {
                super();
                this.lat = lat;
                this.lon = lon;
                this.name = name;
                this.dst = dst;
            }
        }

        if (prev_loc_name != null) {
            if (getEarthDst(lat, lon, prev_lat, prev_lon) < 1000) return prev_loc_name;
        }

        InfoList list = ListHolder.getListHolder().get(InfoList.LOCATION_LIST);
        for (Object o : list) {
            LocationItem item = (LocationItem) o;
            double la = item.lat;
            double lo = item.lon;
            if (getEarthDst(lat, lon, la, lo) < 5000) {
                savePrevLocation(lat, lon, item.name);
                return item.name;
            }
        }
        try {
            Db db = new Db(context, Constants.LOCATIONS_DB);
            db.open();
            Cursor cursor = db.rawQuery("select city,lat,lon,country from list2");
            List<data> list2 = new ArrayList<data>();
            while (cursor.moveToNext()) {
                double la = cursor.getDouble(1);
                double lo = cursor.getDouble(2);
                String name = cursor.getString(0) + ", " + cursor.getString(3);
                double dst = getEarthDst(lat, lon, la, lo);

                if (dst < 20000) {
                    list2.add(new data(la, lo, name, dst));
                }
            }
            cursor.close();
            db.close();
            int pos = -1;
            double min = 1000000;

            for (int i = 0; i < list2.size(); i++) {
                data d = list2.get(i);
                if (d.dst < min) {
                    min = d.dst;
                    pos = i;
                }
            }
            if (pos != -1) {
                String name = list2.get(pos).name;
                savePrevLocation(lat, lon, name);
                return name;
            }
        } catch (Exception e) {

        }
        return null;

    }

    interface CheckBoxDialogActionable {
        public void act(String[] names);
    }

    public static Dialog getCheckBoxDialog(String[] names, CheckBoxDialogActionable act, Context context) {
        InputDialog d = new InputDialog(context);

        d.insertLayout(R.layout.input_dialog_ch_layout);
        LinearLayout ll = (LinearLayout) d.findViewById(R.id.ll_input_dialog_ch_layout1);
        ll.setVisibility(View.GONE);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        d.setTitle(context.getString(R.string.select_database_to_search_in));
        return d;
    }

    /**
     * looks for a star with name like 23and, alphaand etc
     *
     * @param name
     * @return empty list if not found
     */
    public static List<AstroObject> searchHrStar(String name, Context context) {
        List<AstroObject> list = searchHrCrossDb(name.replace(" ", ""), context);
        if (list.size() == 0) {
            list.addAll(searchHrCrossDb(name, context)); //for names like Rigil Kent which have a sapce between words
        }

        return list;
    }

    private static List<AstroObject> searchHrCrossDb(String name, Context context) {
        List<AstroObject> list = new ArrayList<AstroObject>();
        Db db = new Db(context, Constants.SQL_DATABASE_HRCROSS_DB);
        try {
            db.open();
            String sql = "select id from cross where name='" + name.toUpperCase(Locale.US).replace("'", "''") + "'";

            Log.d(TAG, "sql=" + sql);
            Cursor cursor = db.rawQuery(sql);
            while (cursor.moveToNext()) {
                int id = cursor.getInt(0);
                Log.d(TAG, "id=" + id);
                Cursor cursor2 = db.rawQuery("select name from cross where id=" + id);
                while (cursor2.moveToNext()) {
                    String name2 = cursor2.getString(0);
                    Log.d(TAG, "name2=" + name2);
                    if (name2 != null && name2.matches("HR[0-9]+")) {
                        HrStar star = getHrStar(name2);
                        Log.d(TAG, "star=" + star);
                        if (star != null) list.add(star);
                    }
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "e=" + e);
        } finally {
            try {
                db.close();
            } catch (Exception e) {
            }
        }
        return list;
    }

    /**
     * map of contours ngcnum - id in the list
     */
    private static Map<Integer, Integer> mapcon = new HashMap<Integer, Integer>();

    static {
        mapcon.put(6114, 0);
        mapcon.put(6205, 1);
        mapcon.put(7523, 2);
        mapcon.put(1931, 3);
        mapcon.put(1782, 4);
        mapcon.put(7354, 5);
        mapcon.put(7355, 6);
        mapcon.put(7358, 7);
        mapcon.put(7360, 8);
        mapcon.put(7361, 9);
        mapcon.put(7363, 10);
        mapcon.put(1887, 11);
        mapcon.put(1888, 12);
        mapcon.put(1928, 13);
        mapcon.put(1929, 14);
        mapcon.put(1932, 15);
        mapcon.put(2098, 16);
        mapcon.put(2100, 17);
        mapcon.put(261, 18);
        mapcon.put(599, 19);
        mapcon.put(1335, 20);
        mapcon.put(1846, 21);
        mapcon.put(6836, 22);
        mapcon.put(1845, 23);
        mapcon.put(1823, 24);
        mapcon.put(6122, 25);
        mapcon.put(6567, 26);
        mapcon.put(7520, 27);
        mapcon.put(6430, 28);
        mapcon.put(1394, 29);
        mapcon.put(2202, 30);
        mapcon.put(9015, 31);
        mapcon.put(9117, 32);
        mapcon.put(6212, 33);
        mapcon.put(7358, 34);
    }

    public static Integer getContourNumber(int ngcnum) {
        return mapcon.get(ngcnum);
    }

    public static boolean doesBtExist() {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) return false;
            else return true;
        } catch (Exception e) {
            Log.d(TAG, "BT permission?", e);
            return false;
        }
    }

    public static boolean doesMicExist(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_MICROPHONE);
    }

    public static boolean doesLocProviderExist(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION);
    }

    public static String downloadTextViaHttps(String aURL) {
        URL url;
        BufferedReader br = null;
        try {
            url = new URL(aURL);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            InputStream in = con.getInputStream();
            br = new BufferedReader(new InputStreamReader(in));
            String s;
            String result = "";
            while ((s = br.readLine()) != null) {
                Log.d(TAG, "s=" + s);
                result += s + "\n";


            }
            return result;

        } catch (Exception e) {
            Log.d(TAG, "e=" + e);
            return null;
        } finally {
            try {
                br.close();
            } catch (Exception e) {
            }
        }
    }

    static long t0 = System.nanoTime();

    public static void t(String s) {
        Log.i("T>", "\n" + (System.nanoTime() - t0) + " " + s);
        t0 = System.nanoTime();
    }

    /**
     * trim() implementation that keep \0 char
     *
     * @param s
     * @return
     */
    public static String trim(String s) {
        if (s.length() == 0) return "";
        int i1 = 0;
        int i2 = 0;
        for (i1 = 0; i1 < s.length(); i1++) {
            char c = s.charAt(i1);
            if (c > '\u0020' || c == '\u0000') {
                break;
            }
        }
        for (i2 = s.length() - 1; i2 >= 0; i2--) {
            char c = s.charAt(i2);
            if (c > '\u0020' || c == '\u0000') {
                break;
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = i1; i <= i2; i++) {
            sb.append(s.charAt(i));
        }
        return sb.toString();
    }
}
