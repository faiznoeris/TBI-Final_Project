package fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import faiznoeris.tbitugaspraktek.temubalikinformasi.DBHelper;
import faiznoeris.tbitugaspraktek.temubalikinformasi.MainActivity;
import faiznoeris.tbitugaspraktek.temubalikinformasi.R;

/**
 * Created by Vellfire on 06/07/2017.
 */

public class FragmentShowCache extends Fragment{


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

    String id, query, similiarity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_showcache, container, false);

        data.clear();

        lvUtama = (ListView) rootView.findViewById(R.id.listView);
        mainView = rootView.findViewById(R.id.tampilan);
        loadingView = rootView.findViewById(R.id.progressCircle);
        loadingBarHorizontal = (ProgressBar) rootView.findViewById(R.id.progress);

        //utama
        db = new DBHelper(getContext());

        Cursor rs = db.getAllDataCache();
        if (rs.moveToFirst()) {
            while (rs.isAfterLast() == false) {
                map = new HashMap<>(2);
                id = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));
                query = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_QUERY));
                similiarity = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_SIMILIARITY));
                map.put("id", id);
                map.put("query", query);
                map.put("similiarity", similiarity);
                data.add(map);
                rs.moveToNext();
            }
        }

        SimpleAdapter adapter = new SimpleAdapter(getContext(), data,
                R.layout.listview_row_cache,
                new String[]{"id", "query", "similiarity"},
                new int[]{R.id.tvId,
                        R.id.tvQuery,
                        R.id.tvSimiliarity});

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
        MenuItem item12 = menu.findItem(R.id.action_showcache);
        item12.setVisible(false);
        MenuItem item13 = menu.findItem(R.id.action_clearcache);
        item13.setVisible(false);
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
