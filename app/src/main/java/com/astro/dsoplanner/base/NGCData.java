package com.astro.dsoplanner.base;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static android.provider.BaseColumns._ID;
import static com.astro.dsoplanner.Constants.A;
import static com.astro.dsoplanner.Constants.B;
import static com.astro.dsoplanner.Constants.CALDWELL;
import static com.astro.dsoplanner.Constants.CONSTEL;
import static com.astro.dsoplanner.Constants.DEC;
import static com.astro.dsoplanner.Constants.HERSHELL;
import static com.astro.dsoplanner.Constants.MAG;
import static com.astro.dsoplanner.Constants.MESSIER;
import static com.astro.dsoplanner.Constants.NAME;
import static com.astro.dsoplanner.Constants.PA;
import static com.astro.dsoplanner.Constants.RA;
import static com.astro.dsoplanner.Constants.TABLE_NAME;
import static com.astro.dsoplanner.Constants.TYPE;

//helper class for ngcic database
public class NGCData extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "ngcic.db" ;
	private static final int DATABASE_VERSION = 1;
	
	
	public NGCData(Context ctx) {
		super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
		}
	@Override
	public void onCreate(SQLiteDatabase db) {
		String s= "";
		s="CREATE TABLE " + TABLE_NAME + " (" + _ID
		 + " INTEGER PRIMARY KEY AUTOINCREMENT, " + NAME
		 + " INTEGER," + RA + " FLOAT,"+ DEC + " FLOAT,"
		 +MAG + " FLOAT,"
		 + A + " FLOAT,"+B + " FLOAT," + CONSTEL+" INTEGER,"+
		 TYPE + " INTEGER,"+PA + " FLOAT,"+MESSIER + " INTEGER,"+
		 CALDWELL + " INTEGER,"+	HERSHELL + " INTEGER);";
		db.execSQL(s);
		 }

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
		    onCreate(db);
	}
}
