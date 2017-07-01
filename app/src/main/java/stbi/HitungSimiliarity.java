package stbi;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.regex.Matcher;

import faiznoeris.tbitugaspraktek.temubalikinformasi.DBHelper;
import fragment.FragmentShowData;

/**
 * Created by Vellfire on 20/06/2017.
 */

public class HitungSimiliarity{
    //CHANGE THIS CLASS TO CLASS BIASA
    private final String TAG_LOG_D = "HitungSimiliarity";

    String[] query_split;
    //double[] bobotQuery;
    int countTerm = 0, counterLoadingBar = 0, countTotalWordToIndex = 0, index_count, Nterm, id, n, sizestem, jumlahmirip, dotproduct;
    long startTime, endTime;

    double bobot, panjangQuery, idf, panjang, similiarity;

    StringBuilder builder = new StringBuilder();
    String str_id, str_term, str_count, str_removedword, query;

    DBHelper db;

    ProgressBar loadingBar;
    View mainView;
    TextView tvInfo;

    FragmentShowData fragmentShowData;
    Context context;

    //Map<String, String> map;
    ArrayList<Double> bobotQuery = new ArrayList<Double>();

    public HitungSimiliarity(String query, Context context) {
        this.query = query;
        this.context = context;
    }

    protected void doTheJob(){
        //query = params[0];
        db = new DBHelper(context);
        n = db.getSizeDataVektor();
        query_split = query.split(" ");

        panjangQuery = 0;

        for(int i = 0; i < query_split.length; i++){
            Nterm = db.countQueryInIndex(query_split[i]);

            idf = Math.log(n/Nterm);


            Log.d(TAG_LOG_D, "NTERM: " + Nterm + " IDF: " + idf + " N: " + n);
            bobotQuery.add(i, idf);
            panjangQuery += idf * idf;
        }

        panjangQuery = Math.sqrt(panjangQuery);
        jumlahmirip = 0;

        Cursor res = db.getAllDataVektor();
        if(res.moveToFirst()){
            while (!res.isAfterLast()){
                dotproduct = 0;

                id = res.getInt(res.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));
                panjang = res.getInt(res.getColumnIndex(DBHelper.DATA_COLUMN_PANJANGVEKTOR));

                Log.d(TAG_LOG_D, "ID: " + id);
                Cursor res2 = db.getKontenFromIndex(id);
                if(res2.moveToFirst()) {
                    while (!res2.isAfterLast()) {
                        str_term = res2.getString(res2.getColumnIndex(DBHelper.DATA_COLUMN_TERM));
                        bobot = res2.getInt(res2.getColumnIndex(DBHelper.DATA_COLUMN_BOBOT));



                        for(int i = 0; i < query_split.length; i++){

                            if(str_term.equalsIgnoreCase(query_split[i])){
                                Log.d(TAG_LOG_D, "TERM: " + str_term + " | SPLITQUERY: " + query_split[i] +" ID: " + id);
                                dotproduct += bobot * bobotQuery.get(i);
                                Log.d(TAG_LOG_D, "BOBOT: " + bobot + " BOBOTQUERY: " + bobotQuery.get(i));
                            }
                        }

                        res2.moveToNext();
                    }
                }

                Log.d(TAG_LOG_D, "DOTPRODUCT: " + dotproduct);
                if(dotproduct > 0){
                    similiarity = dotproduct / (panjangQuery * panjang);

                    if(db.addTbVektor(query, id, similiarity)){
                        jumlahmirip++;
                    }


                }

                res.moveToNext();
            }

            Log.d(TAG_LOG_D, "MIRIP: " + jumlahmirip);

            if (jumlahmirip == 0) {
                if(db.addTbVektor(query,id,0)){
                    //
                }
            }
        }

    }
}
