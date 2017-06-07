package fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import faiznoeris.tbitugaspraktek.temubalikinformasi.DBHelper;
import faiznoeris.tbitugaspraktek.temubalikinformasi.JSONParsing;
import faiznoeris.tbitugaspraktek.temubalikinformasi.MainActivity;
import faiznoeris.tbitugaspraktek.temubalikinformasi.R;
import stemming_stoplist.Stemming;
import stemming_stoplist.Stoplist;

/**
 * Created by Vellfire on 02/05/2017.
 */

public class FragmentStemmingStoplist_2 extends Fragment {


    int counter = 0;

    ListView lvUtama, lvStoplist, lvStemming;
    View loadingView;
    View mainView;
    ProgressBar loadingBarHorizontal;
    TextView tvInfo;

    public static int totalData = 0;
    public static int totalPages = 0;

    DBHelper db;

    List<Map<String, String>> data = new ArrayList<>();
    List<Map<String, String>> data_stoplist = new ArrayList<>();
    List<Map<String, String>> data_stemming = new ArrayList<>();
    //List<Map<String, String>> dataUtama = new ArrayList<>();
    Map<String, String> map;

    String id, konten, judul;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stemmingstoplist_2, container, false);

        data.clear();

        lvUtama = (ListView) rootView.findViewById(R.id.listView);
        lvStoplist = (ListView) rootView.findViewById(R.id.listViewStoplist);
        lvStemming = (ListView) rootView.findViewById(R.id.listViewStemming);
        mainView = rootView.findViewById(R.id.tampilan);
        loadingView = rootView.findViewById(R.id.progressCircle);
        loadingBarHorizontal = (ProgressBar) rootView.findViewById(R.id.progress);
        tvInfo = (TextView) rootView.findViewById(R.id.progressinfo);

        lvUtama.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) lvUtama.getItemAtPosition(position);
                String id_click = map.get("title");
                Toast.makeText(getContext(), id_click, Toast.LENGTH_SHORT).show();
            }
        });
        lvStoplist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) lvStoplist.getItemAtPosition(position);
                String str_id = map.get("id");
                String str_content = map.get("content");
                String str_title = map.get("title");
                String str_removedword = map.get("removedword");

                //Toast.makeText(getContext(), id_click, Toast.LENGTH_LONG).show();
                AlertDialog diaBox = AskStemming(str_id, str_content, str_title, str_removedword);
                diaBox.show();
            }
        });
        lvStemming.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //HashMap<String,String> map =(HashMap<String,String>)lvUtama.getItemAtPosition(position);
                //String id_click = map.get("removedword");
                //Toast.makeText(getContext(), id_click, Toast.LENGTH_LONG).show();
            }
        });


        db = new DBHelper(getContext());
        if (db.isTBDataUtamaEmpty()) {
            //showProgress(true);
            getPagesAndTotalData getPagesAndTotalData = new getPagesAndTotalData();
            getPagesAndTotalData.execute();
            //Log.d("ASU", "PAGES , DATA = " + totalData + " " + totalPages);

        } else if (!(db.isTBDataUtamaEmpty())) {
            //CheckData checkData = new CheckData(getContext(), loadingBarHorizontal, mainView, tvInfo, this);
            //checkData.execute();
            Cursor rs = db.getAllDataUtama();
            if (rs.moveToFirst()) {
                while (rs.isAfterLast() == false) {
                    map = new HashMap<>(2);
                    id = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));
                    //Log.d("SearchData", "Hasil Search, ID = " + id + " | Value Keyword - " + Value);
                    konten = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_KONTEN));
                    judul = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_JUDUL));
                    //Log.d("AS", "Judul" + judul);
                    map.put("id", id);
                    map.put("content", konten);
                    map.put("title", judul);
                    data.add(map);
                    rs.moveToNext();
                }
            }

            SimpleAdapter adapter = new SimpleAdapter(getContext(), data,
                    R.layout.row,
                    new String[]{"id", "content", "title"},
                    new int[]{R.id.tvId,
                            R.id.tvJudul});

            lvUtama.setAdapter(adapter);

            //setData(data, "");
        }

        if (!(db.isTBDataStoplistEmpty())) {
            Cursor rs = db.getAllDataStoplist();
            if (rs.moveToFirst()) {
                while (rs.isAfterLast() == false) {
                    map = new HashMap<>(2);
                    id = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));
                    konten = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_KONTEN));
                    judul = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_JUDUL));
                    //Log.d("AS", "Judul" + judul);
                    map.put("id", id);
                    map.put("content", konten);
                    map.put("title", judul);
                    map.put("removedword", rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_REMOVEDWORD)));
                    data_stoplist.add(map);
                    rs.moveToNext();
                }
                SimpleAdapter adapter = new SimpleAdapter(getContext(), data_stoplist,
                        R.layout.row,
                        new String[]{"id", "content", "title"},
                        new int[]{R.id.tvId,
                                R.id.tvJudul});

                //setData(data, "");
                setAdapter(adapter, "stoplist");
                counter++;
            }
        }

        if (!(db.isTBDataStemmingEmpty())) {
            Cursor rs = db.getAllDataStemming();
            if (rs.moveToFirst()) {
                while (rs.isAfterLast() == false) {
                    map = new HashMap<>(2);
                    id = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));
                    konten = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_KONTEN));
                    judul = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_JUDUL));
                    //Log.d("AS", "Judul" + judul);
                    map.put("id", id);
                    map.put("content", konten);
                    map.put("title", judul);
                    //map.put("removedword", rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_REMOVEDWORD)));
                    data_stemming.add(map);
                    rs.moveToNext();
                }
                SimpleAdapter adapter = new SimpleAdapter(getContext(), data_stemming,
                        R.layout.row,
                        new String[]{"id", "content", "title"},
                        new int[]{R.id.tvId,
                                R.id.tvJudul});

                //setData(data, "");
                setAdapter(adapter, "stemming");
                counter++;
            }
        }

        ((MainActivity) getActivity()).setActionBarTitle("Stemming dan Stoplist #2");

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_getdata);
        item.setVisible(true);
        MenuItem item2 = menu.findItem(R.id.action_searchdata);
        item2.setVisible(true);
        MenuItem item3 = menu.findItem(R.id.action_back);
        item3.setVisible(false);
    }

    public void setAdapter(SimpleAdapter adapter, String operasi) {
        if (operasi.equals("utama")) {
            lvUtama.setAdapter(adapter);
        } else if (operasi.equals("stoplist")) {
            lvStoplist.setAdapter(adapter);
        } else if (operasi.equals("stemming")) {
            lvStemming.setAdapter(adapter);
        }
    }

    public void setData(List<Map<String, String>> data, String operasi) {
        /*if(operasi.equals("utama")){
            dataUtama = data;
        }else {*/
        this.data = data;
        //}
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_getdata) {
            if (counter == 2) {
                Toast.makeText(getContext(), "Data sudah terisi semua!", Toast.LENGTH_SHORT).show();
            } else {
                AlertDialog diaBox = AskOption();
                diaBox.show();
                return true;
            }
        } else if (id == R.id.action_searchdata) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrame, new FragmentSearchData());
            ft.commit();
        }
        return super.onOptionsItemSelected(item);
    }

    private AlertDialog AskOption() {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(getContext())
                .setTitle("Add Data")
                .setMessage("Isi data sekarang?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (counter == 0) {
                            if (db.isTBDataStoplistEmpty()) {
                                showProgress(true);
                                Stoplist stoplist = new Stoplist(data, getContext(), FragmentStemmingStoplist_2.this, mainView, loadingView);
                                stoplist.execute();
                                counter++;
                            }



                        }/* else if (counter == 1) {
                            try {
                                showProgress(true);
                                Stemming stemming = new Stemming(data, getContext(), FragmentStemmingStoplist_2.this, mainView, loadingView);
                                stemming.execute();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            counter++;
                            Toast.makeText(getContext(), "Stemming hanya bisa dilakukan 1 judul sekali, klik item pada listview stoplist.", Toast.LENGTH_SHORT).show();
                        }*/else{
                            Toast.makeText(getContext(), "Data sudah terisi.", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .create();
        return myQuittingDialogBox;
    }


    private AlertDialog AskStemming(final String id, final String content, final String title, String removedword) {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(getContext())
                .setTitle("Stemming")
                .setMessage(Html.fromHtml("<b>Removed Word:</b> "+ removedword))


                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            if (!(db.isDataStemmingExist(id))) {
                                showProgress(true);
                                map = new HashMap<>(2);

                                map.put("id", id);
                                map.put("content", content);
                                map.put("title", title);

                                data_stemming.add(map);
                                Stemming stemming = new Stemming(data_stemming, getContext(), FragmentStemmingStoplist_2.this, mainView, loadingView);
                                stemming.execute();
                                counter++;
                            }else{
                                Toast.makeText(getContext(), "Data sudah distem.", Toast.LENGTH_SHORT).show();
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Terjadi error.", Toast.LENGTH_SHORT).show();
                        }
                        //Toast.makeText(getContext(), "ngentiaw", Toast.LENGTH_SHORT).show();
                    }
                })
                .create();
        return myQuittingDialogBox;

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mainView.setVisibility(show ? View.GONE : View.VISIBLE);
            mainView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mainView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
            loadingView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            loadingView.setVisibility(show ? View.VISIBLE : View.GONE);
            mainView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public class getPagesAndTotalData extends AsyncTask<Void, Void, JSONObject> {
        URL link;
        DBHelper db;
        HttpURLConnection conn;
        InputStream inputStream;
        BufferedReader bufferedReader;
        StringBuffer stringBuffer;
        JSONObject parentObject, e;
        String WEBSITE_ADDRESS = "http://www.hirupmotekar.com/?json=1";
        String line = "";

        @Override
        protected void onPreExecute() {


            mainView.setVisibility(View.GONE);
            //loadingBarHorizontal.setMax(totalData);
            loadingBarHorizontal.setVisibility(View.VISIBLE);
            tvInfo.setVisibility(View.VISIBLE);

            //startTime = System.currentTimeMillis();
        }


        @Override
        protected JSONObject doInBackground(Void... params) {

            db = new DBHelper(getContext());

            try {
                link = new URL(WEBSITE_ADDRESS);
                conn = (HttpURLConnection) link.openConnection();
                conn.connect();

                inputStream = conn.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                stringBuffer = new StringBuffer();

                //masukkan json kedalam stringbuffer
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }
                bufferedReader.close();

                //finalJson = stringBuffer.toString();
                parentObject = new JSONObject(stringBuffer.toString());

                Log.d("ASU", "PAGES " + parentObject.getString("pages"));
            } catch (Exception er) {
                er.printStackTrace();
            }
            return parentObject;
        }


        @Override
        protected void onPostExecute(JSONObject res) {
            try {
                FragmentStemmingStoplist_2.totalPages = Integer.parseInt(res.getString("pages"));
                FragmentStemmingStoplist_2.totalData = Integer.parseInt(res.getString("count_total"));
                Log.d("ASU", "PAGES post execute" + totalData + " , " + totalPages);
                JSONParsing jsonParsing = new JSONParsing(FragmentStemmingStoplist_2.this, getContext(), loadingBarHorizontal, mainView, tvInfo, totalPages, totalData);
                jsonParsing.execute();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
