package stbi;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import faiznoeris.tbitugaspraktek.temubalikinformasi.DBHelper;
import faiznoeris.tbitugaspraktek.temubalikinformasi.R;
import fragment.FragmentSearchData;
import fragment.FragmentShowData;

/**
 * Created by Vellfire on 20/06/2017.
 */

public class AmbilCache extends AsyncTask<String, Void, List<Map<String, String>>> {
    private final String TAG_LOG_D = "AmbilCache";

    String[] query_split;
    //double[] bobotQuery;
    int countTerm = 0, counterLoadingBar = 0, countTotalWordToIndex = 0, index_count, Nterm, id, n, sizestem, jumlahmirip, dotproduct;
    long startTime, endTime;

    double bobot, panjangQuery, idf, panjang, similiarity;

    StringBuilder builder = new StringBuilder();
    String str_id, str_link, str_konten, str_title, query;

    DBHelper db;

    ProgressBar loadingBar;
    View mainView;
    TextView tvInfo;

    FragmentSearchData fragmentSearchData;
    Context context;

    int dataFound = 0;

    Map<String, String> map;
    List<Map<String, String>> data = new ArrayList<>();
    ArrayList<String> id_list = new ArrayList<String>(), content = new ArrayList<String>(), link = new ArrayList<String>(), title = new ArrayList<String>();


    public AmbilCache(Context context, FragmentSearchData fragmentSearchData) {
        this.context = context;
        this.fragmentSearchData = fragmentSearchData;
    }
    @Override
    protected void onPreExecute() {
        counterLoadingBar = 0;
        countTotalWordToIndex = 0;

        db = new DBHelper(context);


        /*loadingBar.setMax(n);
        loadingBar.setVisibility(View.VISIBLE);

        mainView.setVisibility(View.GONE);
        tvInfo.setVisibility(View.VISIBLE);*/

        startTime = System.currentTimeMillis();
    }

    @Override
    protected List<Map<String, String>> doInBackground(String... params) {

        int size = db.isQueryFoundInCache(params[0]);

        if(size > 0){
            Cursor res = db.getCache(params[0]);

            if(res.moveToFirst()){
                while(!res.isAfterLast()){
                    id = res.getInt(res.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));
                    similiarity = res.getInt(res.getColumnIndex(DBHelper.DATA_COLUMN_SIMILIARITY));

                    if(id != 0){
                        Cursor res2 = db.getKonten(id);
                        if(res2.moveToFirst()){
                            while(!res2.isAfterLast()) {
                                str_id = res2.getString(res2.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));
                                str_title = res2.getString(res2.getColumnIndex(DBHelper.DATA_COLUMN_JUDUL));
                                str_konten = res2.getString(res2.getColumnIndex(DBHelper.DATA_COLUMN_KONTEN));
                                str_link = res2.getString(res2.getColumnIndex(DBHelper.DATA_COLUMN_URL));

                                Log.d(TAG_LOG_D, "LINK: " + str_link + "ID: " + str_id);


                                String splitParam[] = params[0].split(" ");

                                for(int i = 0; i < splitParam.length; i++){
                                    String replaced_by_this = WordUtils.capitalize(splitParam[i]);
                                    str_konten = str_konten.replaceAll("(?i)"+splitParam[i], "<b>" + replaced_by_this + "</b>");
                                    str_konten = str_konten.replace("\n", "<p></p>");
                                    //Log.d(TAG_LOG_D, "splitparam: " + splitParam[i].toString());
                                }


                                map = new HashMap<>(2);
                                map.put("id", str_id);
                                map.put("content", str_konten);
                                map.put("title", str_title);
                                map.put("link", str_link);
                                data.add(map);

                                dataFound++;

                                res2.moveToNext();
                            }
                            //send data back to fragmentshow
                        }
                    }else{
                        // data tidak ada
                    }
                    res.moveToNext();
                }
            }
        }else{
            Cursor res = db.getCache(params[0]);

            HitungSimiliarity hitungSimiliarity = new HitungSimiliarity(params[0], context);
            hitungSimiliarity.doTheJob();

            if(res.moveToFirst()) {
                while (!res.isAfterLast()) {
                    id = res.getInt(res.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));
                    similiarity = res.getInt(res.getColumnIndex(DBHelper.DATA_COLUMN_SIMILIARITY));

                    if(id != 0){
                        Cursor res2 = db.getKonten(id);
                        if(res2.moveToFirst()){
                            while(!res2.isAfterLast()) {
                                str_id = res2.getString(res2.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));
                                str_title = res2.getString(res2.getColumnIndex(DBHelper.DATA_COLUMN_JUDUL));
                                str_konten = res2.getString(res2.getColumnIndex(DBHelper.DATA_COLUMN_KONTEN));
                                str_link = res2.getString(res2.getColumnIndex(DBHelper.DATA_COLUMN_URL));

                                Log.d(TAG_LOG_D, "LINK: " + str_link + "ID: " + str_id);

                                String replaced_by_this = WordUtils.capitalize(params[0]);
                                str_konten = str_konten.replaceAll("(?i)"+params[0], "<b>" + replaced_by_this + "</b>");
                                str_konten = str_konten.replace("\n", "<p></p>");


                                map = new HashMap<>(2);
                                map.put("id", str_id);
                                map.put("content", str_konten);
                                map.put("title", str_title);
                                map.put("link", str_link);
                                data.add(map);

                                dataFound++;

                                res2.moveToNext();
                            }
                            //send data back to fragmentshow
                        }
                    }else{
                        // data tidak ada
                    }

                    res.moveToNext();
                }
            }
        }

        Log.d(TAG_LOG_D, "DATA: " + data.toString());
        return data;
    }


    @Override
    protected void onPostExecute(List<Map<String, String>> res) {
        endTime = System.currentTimeMillis();
        Log.d(TAG_LOG_D, "Done, Time spent = " + (endTime - startTime) / 1000 + " seconds");
        ///Log.d(TAG_LOG_D, "Done, " + counterLoadingBar + " data dihitung .");
        Toast.makeText(context, "Found " + dataFound + " data.", Toast.LENGTH_SHORT).show();
        /*loadingBar.setVisibility(View.GONE);
        tvInfo.setVisibility(View.GONE);
        mainView.setVisibility(View.VISIBLE);*/

        SimpleAdapter adapter = new SimpleAdapter(context, data,
                R.layout.listview_row,
                new String[]{"id", "content", "title", "link"},
                new int[]{R.id.tvId,
                        R.id.tvJudul});


        fragmentSearchData.setData(data);
        //fragmentSearchData.setAdapter(adapter);
    }

    @Override
    protected void onCancelled() {
        /*loadingBar.setVisibility(View.GONE);
        tvInfo.setVisibility(View.GONE);
        mainView.setVisibility(View.VISIBLE);*/
    }
}
