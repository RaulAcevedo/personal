package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.jjkeller.kmbapi.controller.utility.ErrorLogHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class LocationDBAdapter {

    protected static final String TAG = "LocationDBAdapter";

    protected static final String DATABASE_NAME = "locations";

    // NOTE: the intent of the BUILD_VERSION constants is to identify when the location database
    //		 changes.  When an updated location database is placed in the assets folder, the User_Version
    //		 property on that database should be updated.  The Build_Version should be updated to
    //		 match the User_Version set in the database.  When the version number is updated that
    // 		 will trigger the upgrade logic which will delete the existing database and copy the new
    // 		 database over.
    protected static final int BUILD_VERSION_000 = 1;
    protected static final int BUILD_VERSION_001 = 6701;    // 2018.05.07

    public static int BUILD_VERSION_CURRENT = BUILD_VERSION_001;
    private static final String LOCATION_DB_VERSION = "location_db_ver";

    private static final String SQL_SELECT_CLOSEST_MATCH_LIST = "select *, LatDiff + LonDiff as TotalDiff from (select *, abs(Latitude - ?) as LatDiff, abs(Longitude - ?) as LonDiff from [GPSLocations]) as Distance order by TotalDiff limit 20";

    private static final String CITY = "City";
    private static final String STATE = "State";
    private static final String LATITUDE = "Latitude";
    private static final String LONGITUDE = "Longitude";
    private static final String POPULATION = "Population";
    private static final String COUNTRY = "Country";

    private DatabaseHelper _dbHelper;
    private SQLiteDatabase _database;

    private static String getDatabasePath(Context ctx) {
        return ctx.getDatabasePath(DATABASE_NAME).getPath();
    }

    private class DatabaseHelper extends SQLiteOpenHelper {
        private Context _ctx;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, BUILD_VERSION_CURRENT);
            _ctx = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // NOTE:  When copying a database from the Assets folder, cannot have any code
            // in the onCreate method.  If so, after copying the database over, the tables
            // in the database will not exist.
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // NOTE: The Locations database is pre-populated with data so upgrading should just delete and re-create.
            // Deleting the DB cannot be done in onUpgrade (because DB is already open at that point), so instead we handle
            // the upgrade logic in the open() method below
        }

        protected void createDatabase() {
            try {
                copyDatabase();

                // Update the Location DB version we store in SharedPreferences and use to determine when to upgrade
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(_ctx);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(LOCATION_DB_VERSION, BUILD_VERSION_CURRENT);
                editor.commit();
            } catch (IOException e) {
                Log.e(TAG, "Failed to create the location database", e);
                ErrorLogHelper.RecordException(e);
            }
        }

        private void copyDatabase() throws IOException {
            // Open local db as the input stream
            InputStream input = _ctx.getAssets().open(DATABASE_NAME);

            String outFileName = getDatabasePath(_ctx);

            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
            output.close();
            input.close();
        }
    }

    /**
     * Opens the database
     *
     * @param ctx The context
     * @return An opened database
     * @throws IOException
     * @throws SQLException
     */
    public LocationDBAdapter open(Context ctx) throws IOException, SQLException {
        if (_dbHelper == null)
            _dbHelper = new DatabaseHelper(ctx);

        String dbFileName = getDatabasePath(ctx);

        File file = new File(dbFileName);
        if (!file.exists())
            _dbHelper.createDatabase();

        // If current build version is different than version stored in preferences, delete and re-create DB
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        int locationDbVersion = sharedPreferences.getInt(LOCATION_DB_VERSION, 1);
        if (locationDbVersion != BUILD_VERSION_CURRENT) {
            file.delete();
            _dbHelper.createDatabase();
        }

        _database = _dbHelper.getWritableDatabase();
        return this;
    }

    /**
     * Closes the database
     */
    public void close() {
        if (_database != null) _database.close();
        if (_dbHelper != null) _dbHelper.close();
    }

    public List<LocationDBLocation> FetchList(Context ctx, double lat, double lon) {
        Cursor cursorData;
        List<LocationDBLocation> list = null;

        try {
            this.open(ctx);

            String[] args;
            args = new String[] { Double.toString(lat), Double.toString(lon) };

            cursorData = _database.rawQuery(SQL_SELECT_CLOSEST_MATCH_LIST, args);
            list = this.CreateListFromCursor(cursorData);
        } catch (Throwable e) {
            Log.e(TAG, "Failed to fetch the list from  the location database", e);
            ErrorLogHelper.RecordException(ctx, e);
        }

        this.close();
        return list;
    }

    private List<LocationDBLocation> CreateListFromCursor(Cursor cursorData) {
        List<LocationDBLocation> objList = new ArrayList<>();

        if (cursorData != null && cursorData.moveToFirst()) {
            while (!cursorData.isAfterLast()) {
                LocationDBLocation obj = new LocationDBLocation();
                obj.setCity(cursorData.getString(cursorData.getColumnIndex(CITY)));
                obj.setState(cursorData.getString(cursorData.getColumnIndex(STATE)));
                obj.setLatitude(cursorData.getDouble(cursorData.getColumnIndex(LATITUDE)));
                obj.setLongitude(cursorData.getDouble(cursorData.getColumnIndex(LONGITUDE)));
                if (!cursorData.isNull(cursorData.getColumnIndex(POPULATION))) {
                    obj.setPopulation(cursorData.getInt(cursorData.getColumnIndex(POPULATION)));
                }
                obj.setCountry(cursorData.getString(cursorData.getColumnIndex(COUNTRY)));

                objList.add(obj);
                cursorData.moveToNext();
            }
        }

        return objList;
    }

    public class LocationDBLocation {
        private String city;
        private String state;
        private double latitude;
        private double longitude;
        private Integer population;
        private String country;

        public String getCity() {
            return this.city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return this.state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public double getLatitude() {
            return this.latitude;
        }

        public void setLatitude(double lat) {
            this.latitude = lat;
        }

        public double getLongitude() {
            return this.longitude;
        }

        public void setLongitude(double lon) {
            this.longitude = lon;
        }

        public Integer getPopulation() {
            return this.population;
        }

        public void setPopulation(Integer pop) {
            this.population = pop;
        }

        public String getCountry() {
            return this.country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }
}
