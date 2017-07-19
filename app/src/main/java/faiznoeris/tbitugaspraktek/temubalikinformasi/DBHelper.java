package faiznoeris.tbitugaspraktek.temubalikinformasi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Vellfire on 01/05/2017.
 */

public class DBHelper extends SQLiteOpenHelper {

    //private String cek_kata, id, katadasar, konten;
    public static final int KATADASAR_MAX = 28524;
    private final static String DATABASE_PATH = "/data/data/faiznoeris.tbitugaspraktek.temubalikinformasi/databases/";
    private final Context dbContext;
    //context.getApplicationInfo().dataDir + "/databases/"

    private static final String DATABASE_NAME = "TBI-withcache(last).db";
    private static final String KATADASAR_TABLE_NAME = "tb_katadasar";
    private static final String KATADASAR_COLUMN_ID = "id";
    private static final String KATADASAR_COLUMN_KATADASAR = "katadasar";

    private static final String DATA_STOPLIST_TABLE_NAME = "tb_data_stoplist";
    private static final String DATA_STEMMING_TABLE_NAME = "tb_data_stemming";
    private static final String DATA_UTAMA_TABLE_NAME = "tb_data_utama";
    private static final String DATA_INDEX_TABLE_NAME = "tb_index";
    private static final String DATA_VEKTOR_TABLE_NAME = "tb_vektor";
    private static final String DATA_CACHE_TABLE_NAME = "tb_cache";

    public static final String DATA_COLUMN_ID = "id";
    public static final String DATA_COLUMN_IDKONTEN = "idkonten";
    public static final String DATA_COLUMN_KONTEN = "konten";
    public static final String DATA_COLUMN_JUDUL = "judul";
    public static final String DATA_COLUMN_URL = "link_url";
    public static final String DATA_COLUMN_COUNTINDEX = "count_index";
    public static final String DATA_COLUMN_REMOVEDWORD = "removedword";
    public static final String DATA_COLUMN_TERM = "term";
    public static final String DATA_COLUMN_BOBOT = "bobot";
    public static final String DATA_COLUMN_PANJANGVEKTOR = "panjang";
    public static final String DATA_COLUMN_SIMILIARITY = "similiarity";
    public static final String DATA_COLUMN_QUERY = "query";

    private static final String QUERY_CHECK_TB_KATADASAR_SIZE = "SELECT COUNT(*) FROM " + KATADASAR_TABLE_NAME;
    private static final String QUERY_CHECK_TB_DATAUTAMA_SIZE = "SELECT COUNT(*) FROM " + DATA_UTAMA_TABLE_NAME;
    private static final String QUERY_CHECK_TB_DATASTOPLIST_SIZE = "SELECT COUNT(*) FROM " + DATA_STOPLIST_TABLE_NAME;
    private static final String QUERY_CHECK_TB_DATASTEMMING_SIZE = "SELECT COUNT(*) FROM " + DATA_STEMMING_TABLE_NAME;
    private static final String QUERY_CHECK_TB_DATAINDEX_SIZE = "SELECT COUNT(*) FROM " + DATA_INDEX_TABLE_NAME;
    private static final String QUERY_CHECK_TB_DATAVEKTOR_SIZE = "SELECT COUNT(*) FROM " + DATA_VEKTOR_TABLE_NAME;
    private static final String QUERY_CHECK_TB_DATACACHE_SIZE = "SELECT COUNT(*) FROM " + DATA_CACHE_TABLE_NAME;

    private static final String QUERY_GET_ALL_DATA_UTAMA = "select * from " + DATA_UTAMA_TABLE_NAME;
    private static final String QUERY_GET_ALL_DATA_STOPLIST = "SELECT * FROM " + DATA_STOPLIST_TABLE_NAME;
    private static final String QUERY_GET_ALL_DATA_STEMMING = "SELECT * FROM " + DATA_STEMMING_TABLE_NAME;
    private static final String QUERY_GET_ALL_DATA_VEKTOR = "SELECT * FROM " + DATA_VEKTOR_TABLE_NAME;
    private static final String QUERY_GET_ALL_DATA_INDEX = "SELECT * FROM " + DATA_INDEX_TABLE_NAME;
    private static final String QUERY_GET_ALL_DATA_CACHE = "SELECT * FROM " + DATA_CACHE_TABLE_NAME;

/*
    private String QUERY_CHECK_KATADASAR = "SELECT * FROM " + KATADASAR_TABLE_NAME + " WHERE " + KATADASAR_COLUMN_KATADASAR + "='" + cek_kata + "'";
    private String QUERY_CHECK_KATADASAR_EXIST = "SELECT * FROM " + KATADASAR_TABLE_NAME + " WHERE " + KATADASAR_COLUMN_KATADASAR + " = '" + katadasar + "'";

    private String QUERY_CHECK_DATA_UTAMA_EXIST = "SELECT * FROM " + DATA_UTAMA_TABLE_NAME + " WHERE " + DATA_COLUMN_IDKONTEN + "=" + id + "";
    private String QUERY_CHECK_DATA_STOPLIST_EXIST = "SELECT * FROM " + DATA_STOPLIST_TABLE_NAME + " WHERE " + DATA_COLUMN_IDKONTEN + "=" + id + "";
    private String QUERY_CHECK_DATA_STEMMING_EXIST = "SELECT * FROM " + DATA_STEMMING_TABLE_NAME + " WHERE " + DATA_COLUMN_IDKONTEN + "=" + id + "";
*/


//    private String QUERY_GET_SEARCH_DATA = "select * from "+DATA_UTAMA_TABLE_NAME+" where konten LIKE '%" + konten + "%'";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.dbContext = context;
        // checking database and open it if exists
        if (checkDataBase(context)) {
            openDataBase();
        } else {
            try {
                this.getReadableDatabase();
                copyDataBase();
                this.close();
                openDataBase();

            } catch (IOException e) {
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(context, "Initial database is created", Toast.LENGTH_LONG).show();
        }
    }

    private void copyDataBase() throws IOException {
        InputStream myInput = dbContext.getAssets().open(DATABASE_NAME);
        String outFileName = DATABASE_PATH + DATABASE_NAME;
        OutputStream myOutput = new FileOutputStream(outFileName);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    public void openDataBase() throws SQLException {
        String dbPath = DATABASE_PATH + DATABASE_NAME;
        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    private boolean checkDataBase(Context context) {
        SQLiteDatabase checkDB = null;
        boolean exist = false;
        try {
            String dbPath = DATABASE_PATH + DATABASE_NAME;
            File file = context.getDatabasePath(DATABASE_NAME);
            //file.setWritable(true);
            //checkDB = SQLiteDatabase.openDatabase(file.getAbsolutePath(), null,
            //        SQLiteDatabase.OPEN_READONLY);
            return file.exists();
        } catch (SQLiteException e) {
            e.printStackTrace();
        }

        /*if (checkDB != null) {
            exist = true;
            checkDB.close();
        }*/
        return exist;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        /*if (checkDataBase()) {
            openDataBase();
        } else
        {
            try {
                this.getReadableDatabase();
                copyDataBase();
                this.close();
                openDataBase();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
        db.execSQL(
                "create table if not exists " + KATADASAR_TABLE_NAME + " (" + KATADASAR_COLUMN_ID + " integer primary key, " + KATADASAR_COLUMN_KATADASAR + " text)"
        );
        db.execSQL(
                "create table if not exists " + DATA_UTAMA_TABLE_NAME + " (" + DATA_COLUMN_ID + " integer primary key," + DATA_COLUMN_IDKONTEN + " integer, " + DATA_COLUMN_KONTEN + " text, " + DATA_COLUMN_JUDUL + " text)"
        );
        db.execSQL(
                "create table if not exists " + DATA_STOPLIST_TABLE_NAME + " (" + DATA_COLUMN_ID + " integer primary key," + DATA_COLUMN_IDKONTEN + " integer, " + DATA_COLUMN_KONTEN + " text, " + DATA_COLUMN_JUDUL + " text, " + DATA_COLUMN_REMOVEDWORD + " text)"
        );
        db.execSQL(
                "create table if not exists " + DATA_STEMMING_TABLE_NAME + " (" + DATA_COLUMN_ID + " integer primary key," + DATA_COLUMN_IDKONTEN + " integer, " + DATA_COLUMN_KONTEN + " text, " + DATA_COLUMN_JUDUL + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DATA_UTAMA_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DATA_STOPLIST_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DATA_STEMMING_TABLE_NAME);
        onCreate(db);
    }


//  CHECK DATA


    //cek apakah tabel katadasar sudah terisi atau belum
    public boolean isTBKataDasarEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery(QUERY_CHECK_TB_KATADASAR_SIZE, null);
            res.moveToFirst();
            int count = res.getInt(0);
            if (count == KATADASAR_MAX) {
                res.close();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return true;
    }

    //cek apakah tabel katadasar sudah terisi atau belum
    public boolean isTBDataUtamaEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery(QUERY_CHECK_TB_DATAUTAMA_SIZE, null);
            res.moveToFirst();
            int count = res.getInt(0);
            if (count > 0) {
                res.close();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return true;
    }

    //cek apakah tabel katadasar sudah terisi atau belum
    public boolean isTBDataStoplistEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery(QUERY_CHECK_TB_DATASTOPLIST_SIZE, null);
            res.moveToFirst();
            int count = res.getInt(0);
            if (count > 0) {
                res.close();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return true;
    }

    public boolean isTBDataStemmingEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery(QUERY_CHECK_TB_DATASTEMMING_SIZE, null);
            res.moveToFirst();
            int count = res.getInt(0);
            if (count > 0) {
                res.close();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return true;
    }

    public boolean isTBDataIndexEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery(QUERY_CHECK_TB_DATAINDEX_SIZE, null);
            res.moveToFirst();
            int count = res.getInt(0);
            if (count > 0) {
                res.close();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return true;
    }

    public boolean isTBDataVektorEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery(QUERY_CHECK_TB_DATAVEKTOR_SIZE, null);
            res.moveToFirst();
            int count = res.getInt(0);
            if (count > 0) {
                res.close();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return true;
    }

    public boolean isTBDataCacheEmpty() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery(QUERY_CHECK_TB_DATACACHE_SIZE, null);
            res.moveToFirst();
            int count = res.getInt(0);
            if (count > 0) {
                res.close();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return true;
    }

    public boolean isDataUtamaExist(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = null;
        try {
            //this.id = id;
            res = db.rawQuery("SELECT * FROM " + DATA_UTAMA_TABLE_NAME + " WHERE " + DATA_COLUMN_IDKONTEN + "=" + id + "", null);
            if (res.moveToFirst()) {
                res.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return false;
    }

    public boolean isDataStoplistExist(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = null;
        try {
            //this.id = id;
            res = db.rawQuery("SELECT * FROM " + DATA_STOPLIST_TABLE_NAME + " WHERE " + DATA_COLUMN_IDKONTEN + "=" + id + "", null);
            if (res.moveToFirst()) {
                res.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return false;
    }

    public boolean isDataStemmingExist(String id) {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = null;
        try {
            //this.id = id;
            res = db.rawQuery("SELECT * FROM " + DATA_STEMMING_TABLE_NAME + " WHERE " + DATA_COLUMN_IDKONTEN + "=" + id , null);
            if (res.moveToFirst()) {
                res.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return false;
    }

    public boolean isKataDasarExist(String katadasar) {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = null;
        try {
            //this.katadasar = katadasar;
            res = db.rawQuery("SELECT * FROM " + KATADASAR_TABLE_NAME + " WHERE " + KATADASAR_COLUMN_KATADASAR + " = '" + katadasar + "'", null);
            if (res.moveToFirst()) {
                res.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return false;
    }

    //cek apakah kata sudah merupakan kata dasar / belum
    public boolean isKataDasar(String cek_kata) {

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = null;
        try {
            //this.cek_kata = cek_kata;
            res = db.rawQuery("SELECT * FROM " + KATADASAR_TABLE_NAME + " WHERE " + KATADASAR_COLUMN_KATADASAR + " LIKE '%" + cek_kata + "%'", null);
            res.moveToFirst();
            int count = res.getCount();
            if (count > 0) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            res.close();
            db.close();
        }
        return false;
    }


//  GETTING DATA


    public Cursor getDataSearch(String konten) {
        //this.konten = konten;
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor res = db.rawQuery("SELECT * FROM " + DATA_STEMMING_TABLE_NAME + " WHERE " + DATA_COLUMN_KONTEN + " LIKE '%" + konten + "%'", null);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Cursor getAllDataUtama() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor res = db.rawQuery(QUERY_GET_ALL_DATA_UTAMA, null);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Cursor getAllDataStoplist() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor res = db.rawQuery(QUERY_GET_ALL_DATA_STOPLIST, null);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Cursor getAllDataStemming() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor res = db.rawQuery(QUERY_GET_ALL_DATA_STEMMING, null);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Cursor getAllDataCache() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor res = db.rawQuery(QUERY_GET_ALL_DATA_CACHE, null);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public int getSizeDataUtama() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery(QUERY_GET_ALL_DATA_UTAMA, null);
            res.moveToFirst();
            return res.getCount();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return 0;
    }

    public int getSizeDataIndex() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery(QUERY_GET_ALL_DATA_INDEX, null);
            res.moveToFirst();
            return res.getCount();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return 0;
    }


    public int getSizeDataStemming() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery(QUERY_GET_ALL_DATA_STEMMING, null);
            res.moveToFirst();
            return (int) DatabaseUtils.queryNumEntries(db, DATA_STEMMING_TABLE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return 0;
    }

    public int getSizeDataVektor() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery(QUERY_GET_ALL_DATA_VEKTOR, null);
            res.moveToFirst();
            return (int) DatabaseUtils.queryNumEntries(db, DATA_STEMMING_TABLE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return 0;
    }

//  ADDING DATA


    //masukkan kata dasar ke dalam table
    public boolean addKataDasar(String katadasar) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = null;
        try {
            contentValues = new ContentValues();
            contentValues.put(KATADASAR_COLUMN_KATADASAR, katadasar);
            db.insert(KATADASAR_TABLE_NAME, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return true;
    }

    //masukkan kata dasar ke dalam table
    public boolean addDataUtama(String id, String konten, String title, String url) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = null;
        try {
            contentValues = new ContentValues();
            contentValues.put(DATA_COLUMN_IDKONTEN, Integer.parseInt(id));
            contentValues.put(DATA_COLUMN_KONTEN, konten);
            contentValues.put(DATA_COLUMN_JUDUL, title);
            contentValues.put(DATA_COLUMN_URL, url);
            db.insert(DATA_UTAMA_TABLE_NAME, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return true;
    }

    //masukkan kata dasar ke dalam table
    public boolean addDataStoplist(String id, String konten, String title, String removedword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = null;
        try {
            contentValues = new ContentValues();
            contentValues.put(DATA_COLUMN_IDKONTEN, Integer.parseInt(id));
            contentValues.put(DATA_COLUMN_KONTEN, konten);
            contentValues.put(DATA_COLUMN_JUDUL, title);
            contentValues.put(DATA_COLUMN_REMOVEDWORD, removedword);
            db.insert(DATA_STOPLIST_TABLE_NAME, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return true;
    }

    //masukkan kata dasar ke dalam table
    public boolean addDataStemming(String id, String konten, String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = null;
        try {
            contentValues = new ContentValues();
            contentValues.put(DATA_COLUMN_IDKONTEN, Integer.parseInt(id));
            contentValues.put(DATA_COLUMN_KONTEN, konten);
            contentValues.put(DATA_COLUMN_JUDUL, title);
            db.insert(DATA_STEMMING_TABLE_NAME, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return true;
    }


    //  ADDING DATA

    public boolean updateDataUtama(String idkonten, String judul, String konten) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        try {
            if (judul == "") {
                contentValues.put(DATA_COLUMN_KONTEN, konten);
                db.update(DATA_UTAMA_TABLE_NAME, contentValues, DATA_COLUMN_IDKONTEN + " = " + Integer.parseInt(idkonten), null);
                return true;
            } else {
                contentValues.put(DATA_COLUMN_JUDUL, judul);
                db.update(DATA_UTAMA_TABLE_NAME, contentValues, DATA_COLUMN_IDKONTEN + " = " + Integer.parseInt(idkonten), null);
                return true;
            }
            /*res = db.rawQuery("UPDATE " + DATA_UTAMA_TABLE_NAME
                        + " SET " + DATA_COLUMN_JUDUL + " = '" + judul + "' , " + DATA_COLUMN_KONTEN + " = '" + konten + "'"
                        + " WHERE " + DATA_COLUMN_IDKONTEN + " = " + idkonten, null);
            */
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //db.close();
        }
        return false;
    }

    //  REWMOVE DATA

    public int delDataStoplist(String idkonten) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(DATA_STOPLIST_TABLE_NAME, DATA_COLUMN_IDKONTEN + " = " + idkonten, null);
    }

    public int delDataStemming(String idkonten) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(DATA_STEMMING_TABLE_NAME, DATA_COLUMN_IDKONTEN + " = " + idkonten, null);
    }

    //  INDEXING

    public int getCountIndex(String term, String idkonten) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res;
        try {
            res = db.rawQuery("SELECT count_index FROM " + DATA_INDEX_TABLE_NAME + " WHERE " + DATA_COLUMN_IDKONTEN + " = " + idkonten + " AND " + DATA_COLUMN_TERM + " = '" + term + "'", null);
            res.moveToFirst();
            if(res.getCount() > 0) {
                return res.getInt(res.getColumnIndex(DATA_COLUMN_COUNTINDEX));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean updateTbIndex_Indexing(int count, String term, String idkonten) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put(DATA_COLUMN_COUNTINDEX, count);
            db.update(DATA_INDEX_TABLE_NAME, contentValues, DATA_COLUMN_IDKONTEN + " = " + idkonten + " AND " + DATA_COLUMN_TERM + " = '" + term + "'", null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addTbIndex_Indexing(String term, String idkonten) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = null;
        try {
            contentValues = new ContentValues();
            contentValues.put(DATA_COLUMN_IDKONTEN, Integer.parseInt(idkonten));
            contentValues.put(DATA_COLUMN_TERM, term);
            contentValues.put(DATA_COLUMN_COUNTINDEX, 1);
            contentValues.put(DATA_COLUMN_BOBOT, 0);
            db.insert(DATA_INDEX_TABLE_NAME, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return true;
    }

    public boolean clearTbIndexing() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(DATA_INDEX_TABLE_NAME, null,null);
            db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + DATA_INDEX_TABLE_NAME + "'"); //reset auto increment
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return false;
    }

    public boolean clearTbCache() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(DATA_CACHE_TABLE_NAME, null,null);
            db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + DATA_CACHE_TABLE_NAME + "'"); //reset auto increment
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return false;
    }

    public boolean clearTbStem() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(DATA_STEMMING_TABLE_NAME, null,null);
            db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + DATA_STEMMING_TABLE_NAME + "'"); //reset auto increment
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return false;
    }

    public boolean clearTbStop() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(DATA_STOPLIST_TABLE_NAME, null,null);
            db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + DATA_STOPLIST_TABLE_NAME + "'"); //reset auto increment
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return false;
    }


    // BOBOT

    public int getTotalDataIndex() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery("SELECT COUNT(DISTINCT " + DATA_COLUMN_IDKONTEN + ") as N FROM " + DATA_INDEX_TABLE_NAME , null);
            res.moveToFirst();
            //return (int) DatabaseUtils.queryNumEntries(db, DATA_INDEX_TABLE_NAME);
            return res.getInt(res.getColumnIndex("N"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return 0;
    }



    public Cursor getAllDataIndex() {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            //Cursor res = db.rawQuery("SELECT * FROM " + DATA_INDEX_TABLE_NAME + " ORDER BY " + DATA_COLUMN_ID, null);

            Cursor res = db.rawQuery(QUERY_GET_ALL_DATA_INDEX, null);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }



    public int getWordFreq(String term) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery("SELECT Count(*) as N FROM " + DATA_INDEX_TABLE_NAME + " WHERE " + DATA_COLUMN_TERM + " = '" + term + "'" , null);
            res.moveToFirst();
            //if(res.getCount() > 0) {
                return res.getInt(res.getColumnIndex("N"));
            //}
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return 0;
    }

    public boolean updateTbIndex_Bobot(double bobot, int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        try {
            contentValues.put(DATA_COLUMN_BOBOT, bobot);
            db.update(DATA_INDEX_TABLE_NAME, contentValues, DATA_COLUMN_ID + " = " + id, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } /*finally {
            //db.close();
        }*/
        return false;
    }

    // VEKTOR

    public boolean clearTbVektor() {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(DATA_VEKTOR_TABLE_NAME, null,null);
            db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + DATA_VEKTOR_TABLE_NAME + "'"); //reset auto increment
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return false;
    }

    public Cursor getAllDataVektor() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery("SELECT * FROM "+DATA_VEKTOR_TABLE_NAME+" ORDER BY "+ DATA_COLUMN_IDKONTEN, null);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //db.close();
        }
        return null;
    }

    public Cursor getBobotFromIndex(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery("SELECT " + DATA_COLUMN_BOBOT + " FROM " + DATA_INDEX_TABLE_NAME + " WHERE " + DATA_COLUMN_IDKONTEN + " = " + id , null);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //db.close();
        }
        return null;
    }


    public boolean addTbVektor(int id, double panjangvektor) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = null;
        try {
            contentValues = new ContentValues();
            contentValues.put(DATA_COLUMN_IDKONTEN, id);
            contentValues.put(DATA_COLUMN_PANJANGVEKTOR, panjangvektor);
            db.insert(DATA_VEKTOR_TABLE_NAME, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return true;
    }

    //HITUNG SIMILIRAITY

    public int countQueryInIndex(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery("SELECT Count(*) as N from " + DATA_INDEX_TABLE_NAME + " WHERE " + DATA_COLUMN_TERM + " = '" + query + "'", null);
            res.moveToFirst();
            //if(res.getCount() > 0) {
                return res.getInt(res.getColumnIndex("N"));
            //}
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return 0;
    }

    public Cursor getKontenFromIndex(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery("SELECT * FROM "+DATA_INDEX_TABLE_NAME+" WHERE "+DATA_COLUMN_IDKONTEN+" = " + id, null);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //db.close();
        }
        return null;
    }

    public boolean addTbCache(String query, int id, double sim) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = null;
        try {
            contentValues = new ContentValues();
            if(sim != 0) {
                contentValues.put(DATA_COLUMN_IDKONTEN, id);
                contentValues.put(DATA_COLUMN_QUERY, query);
                contentValues.put(DATA_COLUMN_SIMILIARITY, sim);
            }else{
                contentValues.put(DATA_COLUMN_IDKONTEN, id);
                contentValues.put(DATA_COLUMN_QUERY, "0");
                contentValues.put(DATA_COLUMN_SIMILIARITY, 0);
            }
            db.insert(DATA_CACHE_TABLE_NAME, null, contentValues);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return true;
    }


    //AMBIOL CACHE

    public Cursor getCache(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery("SELECT * FROM "+DATA_CACHE_TABLE_NAME+" WHERE "+DATA_COLUMN_QUERY+" = '"+query+"' ORDER BY "+DATA_COLUMN_SIMILIARITY+" DESC", null);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //db.close();
        }
        return null;
    }

    public int isQueryFoundInCache(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery("SELECT Count(*) as N FROM "+DATA_CACHE_TABLE_NAME+" WHERE "+DATA_COLUMN_QUERY+" = '"+query+"' ORDER BY "+DATA_COLUMN_SIMILIARITY+" DESC", null);
            res.moveToFirst();
            //if(res.getCount() > 0) {
            return res.getInt(res.getColumnIndex("N"));
            //}
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        return 0;
    }

    public Cursor getKonten(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = null;
        try {
            res = db.rawQuery("SELECT * FROM "+DATA_UTAMA_TABLE_NAME+" WHERE "+DATA_COLUMN_IDKONTEN+" = "  + id, null);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //db.close();
        }
        return null;
    }

}