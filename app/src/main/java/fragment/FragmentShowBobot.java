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

import background_task.JSONParsing;
import background_task.RefreshData;
import faiznoeris.tbitugaspraktek.temubalikinformasi.DBHelper;
import faiznoeris.tbitugaspraktek.temubalikinformasi.MainActivity;
import faiznoeris.tbitugaspraktek.temubalikinformasi.R;
import stbi.Bobot;
import stbi.Indexing;
import stbi.Stemming;
import stbi.Stoplist;
import stbi.Vektor;

/**
 * Created by Vellfire on 01/07/2017.
 */

public class FragmentShowBobot extends Fragment {


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
    Map<String, String> map;

    String id, term, count, bobot;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_showbobot, container, false);

        data.clear();

        lvUtama = (ListView) rootView.findViewById(R.id.listView);
        mainView = rootView.findViewById(R.id.tampilan);
        loadingView = rootView.findViewById(R.id.progressCircle);
        loadingBarHorizontal = (ProgressBar) rootView.findViewById(R.id.progress);

        /*lvUtama.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) lvUtama.getItemAtPosition(position);
                String id_click = map.get("title");
                Toast.makeText(getContext(), id_click, Toast.LENGTH_SHORT).show();
            }
        });*/


        //utama
        db = new DBHelper(getContext());

        Cursor rs = db.getAllDataIndex();
        if (rs.moveToFirst()) {
            while (rs.isAfterLast() == false) {
                map = new HashMap<>(2);
                id = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));
                term = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_TERM));
                count = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_COUNTINDEX));
                bobot = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_BOBOT));
                map.put("id", id);
                map.put("term", term);
                map.put("count", count);
                map.put("bobot", bobot);
                data.add(map);
                rs.moveToNext();
            }
        }

        SimpleAdapter adapter = new SimpleAdapter(getContext(), data,
                R.layout.listview_row_bobot,
                new String[]{"id", "term", "count","bobot"},
                new int[]{R.id.tvId,
                        R.id.tvTerm,
                        R.id.tvCount,
                        R.id.tvBobot});

        lvUtama.setAdapter(adapter);


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
        MenuItem item4 = menu.findItem(R.id.action_refresh);
        item4.setVisible(false);
        MenuItem item5 = menu.findItem(R.id.action_bobot);
        item5.setVisible(false);
        MenuItem item6 = menu.findItem(R.id.action_index);
        item6.setVisible(false);
        MenuItem item7 = menu.findItem(R.id.action_about);
        item7.setVisible(false);
        MenuItem item8 = menu.findItem(R.id.action_stoplist);
        item8.setVisible(false);
        MenuItem item9 = menu.findItem(R.id.action_vektor);
        item9.setVisible(false);
        MenuItem item10 = menu.findItem(R.id.action_showindex);
        item10.setVisible(false);
        MenuItem item11 = menu.findItem(R.id.action_showvektor);
        item11.setVisible(false);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_back) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrame, new FragmentShowData());
            ft.commit();
        }
        return super.onOptionsItemSelected(item);
    }

}
