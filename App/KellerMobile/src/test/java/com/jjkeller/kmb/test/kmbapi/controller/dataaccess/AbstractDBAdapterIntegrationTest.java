package com.jjkeller.kmb.test.kmbapi.controller.dataaccess;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jjkeller.kmb.test.KMBRoboElectricTestRunner;
import com.jjkeller.kmbapi.controller.dataaccess.AbstractDBAdapter;

import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import java.io.File;

import static junit.framework.Assert.assertEquals;

@RunWith(KMBRoboElectricTestRunner.class)
public class AbstractDBAdapterIntegrationTest {

    /*
        Paths to the databases to be upgraded
     */
    private static final String DATABASE_FILE_PATH_2_71_6290_126 = "./src/test/java/com/jjkeller/kmb/test/kmbapi/controller/dataaccess/kmb_2.71.6290.126.sqlite";
    private static final String DATABASE_FILE_PATH_2_71_6290_124 = "./src/test/java/com/jjkeller/kmb/test/kmbapi/controller/dataaccess/kmb_2.71.6290.124.sqlite";
    private static final String DATABASE_FILE_PATH_2_71_6234_122 = "./src/test/java/com/jjkeller/kmb/test/kmbapi/controller/dataaccess/kmb_2.71.6234.122.sqlite";
    private static final String DATABASE_FILE_PATH_2_64_6190_116 = "./src/test/java/com/jjkeller/kmb/test/kmbapi/controller/dataaccess/kmb_2.64.6190.116.sqlite";
    private static final String DATABASE_FILE_PATH_2_64_6190_114 = "./src/test/java/com/jjkeller/kmb/test/kmbapi/controller/dataaccess/kmb_2.64.6190.114.sqlite";

    /*
        This is the expected schema after a new DB is created, and it should be the same schema for upgrades in the future.
        As long as create statements and upgrade statements exactly match, we shouldn't have to create new schemas for each new version.
     */
    private static final String SCHEMA_FILE_PATH = "./src/test/java/com/jjkeller/kmb/test/kmbapi/controller/dataaccess/schema.txt";
    /*
        This is an extra schema for upgraded DBs that were created in version 2.71.6290.124.
        The create and upgrade statements don't match up with default values, so it needs to be different.

        EmployeeLog and EmployeeLogEldEvent aren't quoted because they weren't renamed. Quotes have been added to the base schema to prevent this in the future.
        EmployeeLogEldEvent.IsReviewed doesn't have "default 0"
        GeotabHOSData.SpeedFromEngine and GeotabHOSData.OdometerFromEngine don't have "default 0"
        LogTeamDriver.KMBUsername doesn't have "DEFAULT ''"
        There are some extra spaces too
     */
    private static final String SCHEMA_FILE_PATH_CREATED_2_71_6290_124 = "./src/test/java/com/jjkeller/kmb/test/kmbapi/controller/dataaccess/schema_2.71.6290.124.txt";
    /*
        This is an extra schema for upgraded DBs that were created in version 2.71.6234.122.
        The create and upgrade statements don't match up with default values, so it needs to be different.

        EmployeeLogWithProvisions.EmployeeLogEldEventKey is at the end instead of in the middle
        LogTeamDriver.KMBUsername won't have "DEFAULT ''"
        There's one extra space too
     */
    private static final String SCHEMA_FILE_PATH_CREATED_2_71_6234_122 = "./src/test/java/com/jjkeller/kmb/test/kmbapi/controller/dataaccess/schema_2.71.6234.122.txt";
    /*
        This is an extra schema for upgraded DBs that were created before version 2.71.6234.122.
        The create and upgrade statements don't match up, so it needs to be different.

        EmployeeLogWithProvisions.EmployeeLogEldEventKey is at the end instead of in the middle
        There's one extra space too
     */
    private static final String SCHEMA_FILE_PATH_CREATED_BEFORE_2_71_6234_122 = "./src/test/java/com/jjkeller/kmb/test/kmbapi/controller/dataaccess/schema_2.64.6190.116.txt";

    @Test
    public void test_create_db_should_have_valid_schema() throws Exception {
        testCreate(SCHEMA_FILE_PATH);
    }

    @Test
    public void test_upgrade_from_production_version_2_71_6290_126() throws Exception {
        testUpgrade(DATABASE_FILE_PATH_2_71_6290_126, SCHEMA_FILE_PATH);
    }

    @Test
    public void test_upgrade_from_production_version_2_71_6290_124() throws Exception {
        testUpgrade(DATABASE_FILE_PATH_2_71_6290_124, SCHEMA_FILE_PATH_CREATED_2_71_6290_124);
    }

    @Test
    public void test_upgrade_from_production_version_2_71_6234_122() throws Exception {
        testUpgrade(DATABASE_FILE_PATH_2_71_6234_122, SCHEMA_FILE_PATH_CREATED_2_71_6234_122);
    }

    @Test
    public void test_upgrade_from_production_version_2_64_6190_116() throws Exception {
        testUpgrade(DATABASE_FILE_PATH_2_64_6190_116, SCHEMA_FILE_PATH_CREATED_BEFORE_2_71_6234_122);
    }

    @Test
    public void test_upgrade_from_production_version_2_64_6190_114() throws Exception {
        testUpgrade(DATABASE_FILE_PATH_2_64_6190_114, SCHEMA_FILE_PATH_CREATED_BEFORE_2_71_6234_122);
    }

    private void testCreate(String expectedSchemaFilePath) throws Exception {
        String tempDbFile = "./src/test/java/com/jjkeller/kmb/test/kmbapi/controller/dataaccess/test.sqlite";
        String tempDbJournalFile = tempDbFile + "-journal";
        SQLiteDatabase db = null;
        try {
            db = SQLiteDatabase.openDatabase(tempDbFile, null, 0);

            AbstractDBAdapter.DatabaseHelper databaseHelper = AbstractDBAdapter.getHelperForTesting(Robolectric.application);
            databaseHelper.onCreate(db);

            assertSchemas(db, expectedSchemaFilePath);
        } finally {
            close(db);
            FileUtils.fileDelete(tempDbFile);
            FileUtils.fileDelete(tempDbJournalFile);
        }
    }

    private void testUpgrade(String dbFilePath, String expectedSchemaFilePath) throws Exception {
        String tempDbFile = "./src/test/java/com/jjkeller/kmb/test/kmbapi/controller/dataaccess/test.sqlite";
        String tempDbJournalFile = tempDbFile + "-journal";
        SQLiteDatabase db = null;
        try {
            FileUtils.copyFile(new File(dbFilePath), new File(tempDbFile));
            db = SQLiteDatabase.openDatabase(tempDbFile, null, 0);

            AbstractDBAdapter.DatabaseHelper databaseHelper = AbstractDBAdapter.getHelperForTesting(Robolectric.application);
            databaseHelper.onUpgrade(db, db.getVersion(), AbstractDBAdapter.BUILD_VERSION_CURRENT);

            assertSchemas(db, expectedSchemaFilePath);
        } finally {
            close(db);
            FileUtils.fileDelete(tempDbFile);
            FileUtils.fileDelete(tempDbJournalFile);
        }
    }

    private void assertSchemas(SQLiteDatabase db, String expectedSchemaFilePath) throws Exception {
        String expectedSchema = FileUtils.fileRead(expectedSchemaFilePath);
        assertEquals(expectedSchema, getSchema(db));
    }

    private String getSchema(SQLiteDatabase db) {
        String schema = "";
        Cursor cursor = null;
        try {
            String sql =
                    "select sql as schema " +
                    "from sqlite_master " +
                    "where type = 'table' and name not in ('android_metadata', 'sqlite_sequence') " +
                    "order by name";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                schema += cursor.getString(0) + "\n";
            }
        } finally {
            close(cursor);
        }
        return schema;
    }

    private static void close(SQLiteDatabase db) {
        if (db != null) {
            db.close();
        }
    }

    private static void close(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

}
