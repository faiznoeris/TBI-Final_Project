package fragment;

import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
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

import background_task.RefreshData;
import faiznoeris.tbitugaspraktek.temubalikinformasi.DBHelper;
import background_task.JSONParsing;
import faiznoeris.tbitugaspraktek.temubalikinformasi.MainActivity;
import faiznoeris.tbitugaspraktek.temubalikinformasi.R;
import stbi.Bobot;
import stbi.Indexing;
import stbi.Stemming;
import stbi.Stoplist;
import stbi.Vektor;

/**
 * Created by Vellfire on 02/05/2017.
 */

public class FragmentShowData extends Fragment {


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
        View rootView = inflater.inflate(R.layout.fragment_showdata, container, false);

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
                AlertDialog diaBox = dialogStemming(str_id, str_content, str_title, str_removedword);
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


        //utama
        db = new DBHelper(getContext());
        if (db.isTBDataUtamaEmpty()) {
            //showProgress(true);
            getPagesAndTotalData getPagesAndTotalData = new getPagesAndTotalData();
            getPagesAndTotalData.execute();
        } else if (!(db.isTBDataUtamaEmpty())) {
            Cursor rs = db.getAllDataUtama();
            if (rs.moveToFirst()) {
                while (rs.isAfterLast() == false) {
                    map = new HashMap<>(2);
                    this.id = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));
                    //Log.d("SearchData", "Hasil Search, ID = " + id + " | Value Keyword - " + Value);
                    konten = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_KONTEN));
                    judul = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_JUDUL));
                    //Log.d("AS", "Judul" + judul);
                    map.put("id", this.id);
                    map.put("content", konten);
                    map.put("title", judul);
                    data.add(map);
                    rs.moveToNext();
                }
            }

            SimpleAdapter adapter = new SimpleAdapter(getContext(), data,
                    R.layout.listview_row,
                    new String[]{"id", "content", "title"},
                    new int[]{R.id.tvId,
                            R.id.tvJudul});

            lvUtama.setAdapter(adapter);
        }


        //stoplist
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
                        R.layout.listview_row,
                        new String[]{"id", "content", "title"},
                        new int[]{R.id.tvId,
                                R.id.tvJudul});

                //setData(data, "");
                setAdapter(adapter, "stoplist");
                counter++;
            }
        }


        //stemming
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
                        R.layout.listview_row,
                        new String[]{"id", "content", "title"},
                        new int[]{R.id.tvId,
                                R.id.tvJudul});

                //setData(data, "");
                setAdapter(adapter, "stemming");
                counter++;
            }
        }

        ((MainActivity) getActivity()).setActionBarTitle("STBI");

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
        item.setVisible(false);
        MenuItem item2 = menu.findItem(R.id.action_searchdata);
        item2.setVisible(false);
        MenuItem item3 = menu.findItem(R.id.action_back);
        item3.setVisible(true);


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
        if (id == R.id.action_refresh) {
            /*if (counter == 2) {
                Toast.makeText(getContext(), "Data sudah terisi semua!", Toast.LENGTH_SHORT).show();
            } else {
                AlertDialog diaBox = dialogStoplist();
                diaBox.show();
                return true;
            }*/
            AlertDialog diaBox = dialogRefresh();
            diaBox.show();
        } else if (id == R.id.action_back) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrame, new FragmentSearchData());
            ft.commit();
        } else if (id == R.id.action_stoplist){
            AlertDialog diaBox = dialogStoplist();
            diaBox.show();
        } else if (id == R.id.action_index){
            if(!db.isTBDataStemmingEmpty()) {
                Indexing indexing = new Indexing(getContext(), FragmentShowData.this, loadingBarHorizontal, mainView, tvInfo);
                indexing.execute();
            }else{
                Toast.makeText(getContext(), "Data stemming masih kosong!", Toast.LENGTH_SHORT).show();
            }
        } else if(id == R.id.action_bobot){
            if(!db.isTBDataIndexEmpty()) {
                Bobot bobot = new Bobot(getContext(), FragmentShowData.this, loadingBarHorizontal, mainView, tvInfo);
                bobot.execute();
            }else{
                Toast.makeText(getContext(), "Data index masih kosong!", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.action_about){
            AlertDialog diaBox = dialogAbout();
            diaBox.show();
        } else if(id == R.id.action_vektor){
            if(!db.isTBDataIndexEmpty()) {
                Vektor bobot = new Vektor(getContext(), FragmentShowData.this, loadingBarHorizontal, mainView, tvInfo);
                bobot.execute();
            }else{
                Toast.makeText(getContext(), "Data index masih kosong!", Toast.LENGTH_SHORT).show();
            }
        }else if(id == R.id.action_showindex){
            if(!db.isTBDataIndexEmpty()) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.mainFrame, new FragmentShowBobot());
                ft.commit();
            }else{
                Toast.makeText(getContext(), "Data index masih kosong!", Toast.LENGTH_SHORT).show();
            }
        }else if(id == R.id.action_showvektor){
            if(!db.isTBDataVektorEmpty()) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.mainFrame, new FragmentShowVektor());
                ft.commit();
            }else{
                Toast.makeText(getContext(), "Data vektor masih kosong!", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private AlertDialog dialogAbout() {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(getContext())
                .setTitle("About")
                .setMessage(Html.fromHtml("Anggota Kelompok:<br><br>1. Bayu Andrianto <b>(0021)</b><br>2. Syaeful Hidayat <b>(0025)</b><br>3. Muhammad Faiz Noeris <b>(0027)</b><br>4. Rifdhotul Alfiansyah <b>(0033)</b><br>5. Maskur Al Asad <b>(0035)</b><br>6. Yogi Hendra <b>(0032)</b><br>7. Yosua Sandy Garsa <b>(0034)</b><br>"))
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        return myQuittingDialogBox;
    }

    private AlertDialog dialogStoplist() {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(getContext())
                .setTitle("Stoplist")
                .setMessage("Lakukan stoplist sekarang?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (counter == 0) {
                            if (db.isTBDataStoplistEmpty()) {
                                //showProgress(true);
                                Stoplist stoplist = new Stoplist(data, getContext(), FragmentShowData.this, loadingBarHorizontal, mainView, tvInfo);
                                stoplist.execute();
                                counter++;
                            }
                        }else{
                            Toast.makeText(getContext(), "Data sudah terisi.", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .create();
        return myQuittingDialogBox;
    }


    private AlertDialog dialogRefresh() {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(getContext())
                .setTitle("Refresh data")
                .setMessage("Lakukan refresh data sekarang?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (!(db.isTBDataUtamaEmpty())) {
                            RefreshData refreshData = new RefreshData(getContext(), loadingBarHorizontal, mainView, tvInfo, FragmentShowData.this);
                            refreshData.execute();
                        }

                    }
                })
                .create();
        return myQuittingDialogBox;
    }

    private AlertDialog dialogStemming(final String id, final String content, final String title, String removedword) {
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
                                data_stemming.clear();
                                map = new HashMap<>(2);

                                map.put("id", id);
                                map.put("content", content);
                                map.put("title", title);

                                data_stemming.add(map);
                                Stemming stemming = new Stemming(data_stemming, getContext(), FragmentShowData.this, loadingBarHorizontal, mainView, tvInfo);
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
            //loadingBarHorizontal.setMax(totalDataNew);
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
                FragmentShowData.totalPages = Integer.parseInt(res.getString("pages"));
                FragmentShowData.totalData = Integer.parseInt(res.getString("count_total"));
                Log.d("ASU", "PAGES post execute" + totalData + " , " + totalPages);
                JSONParsing jsonParsing = new JSONParsing(FragmentShowData.this, getContext(), loadingBarHorizontal, mainView, tvInfo, totalPages, totalData);
                jsonParsing.execute();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
