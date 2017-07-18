package fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import faiznoeris.tbitugaspraktek.temubalikinformasi.DBHelper;
import faiznoeris.tbitugaspraktek.temubalikinformasi.MainActivity;
import faiznoeris.tbitugaspraktek.temubalikinformasi.R;
import stbi.AmbilCache;
import stbi.Bobot;
import stbi.Indexing;
import stbi.Stoplist;
import stbi.Vektor;

/**
 * Created by Vellfire on 31/05/2017.
 */

public class FragmentSearchData extends Fragment {
    Button search;
    ListView listData;
    EditText keyword;
    TableRow trHeader;
    ProgressBar loadingBarHorizontal;
    TextView tvInfo;
    View mainView;

    DBHelper db;

    String id, konten, judul, Value;

    int countKeyword = 0;
    String[] splitKeyword;

    Map<String, String> map;
    List<Map<String, String>> data = new ArrayList<>();
    List<Map<String, String>> data_tostoplist = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_searchdata, container, false);

        search = (Button) rootView.findViewById(R.id.btnSearch);
        listData = (ListView) rootView.findViewById(R.id.listData);
        keyword = (EditText) rootView.findViewById(R.id.etKeyword);
        trHeader = (TableRow) rootView.findViewById(R.id.trKonten);
        loadingBarHorizontal = (ProgressBar) rootView.findViewById(R.id.progress);
        tvInfo = (TextView) rootView.findViewById(R.id.progressinfo);
        mainView = rootView.findViewById(R.id.tampilan);

        db = new DBHelper(getContext());

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Value = keyword.getText().toString();
                if (Value.equals("")) {
                    Toast.makeText(getContext(), "Keyword kosong!", Toast.LENGTH_SHORT).show();
                } else {
                    AmbilCache ambilCache = new AmbilCache(getContext(), FragmentSearchData.this);
                    ambilCache.execute(keyword.getText().toString());
                }
            }
        });

        listData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> map = (HashMap<String, String>) listData.getItemAtPosition(position);
                //String id_click = map.get("title");
                //String cKey = map.get("countkeyword");
                String link = map.get("link");
                // Toast.makeText(getContext(), "Judul: " + id_click + " || Jumlah keyword muncul: " + cKey, Toast.LENGTH_SHORT).show();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(browserIntent);

                Toast.makeText(getContext(), link + " | " + map.get("title"), Toast.LENGTH_SHORT).show();
            }
        });

        data_tostoplist.clear();
        if (!(db.isTBDataUtamaEmpty())) {
            Cursor rs = db.getAllDataUtama();
            if (rs.moveToFirst()) {
                while (rs.isAfterLast() == false) {
                    map = new HashMap<>(2);
                    id = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));
                    konten = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_KONTEN));
                    judul = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_JUDUL));
                    if(id == "116"){
                        continue;
                    }
                    //Log.d("AS", "Judul" + judul);
                    map.put("id", id);
                    map.put("content", konten);
                    map.put("title", judul);
                    //map.put("removedword", rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_REMOVEDWORD)));
                    data_tostoplist.add(map);
                    rs.moveToNext();
                }
            }
            rs.close();
        }


        ((MainActivity) getActivity()).setActionBarTitle("STBI");

        return rootView;
    }

    public void setData(List<Map<String, String>> data) {

        this.data = data;

        SimpleAdapter adapter = new SimpleAdapter(getContext(), data,
                R.layout.listview_row,
                new String[]{"id", "content", "title", "link"},
                new int[]{R.id.tvId,
                        R.id.tvJudul});

        SimpleAdapter.ViewBinder binder = new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object object, String value) {
                if (view instanceof TextView) {
                    ((TextView) view).setText(Html.fromHtml(value));
                    return true;
                }

                return false;
            }
        };
        adapter.setViewBinder(binder);


        trHeader.setVisibility(View.VISIBLE);
        listData.setVisibility(View.VISIBLE);
        listData.setAdapter(adapter);
        //Toast.makeText(getContext(), "NEW: " + this.data.toString(), Toast.LENGTH_SHORT).show();
    }

    public void setAdapter(SimpleAdapter adapter) {
        listData.setAdapter(adapter);
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
        item2.setVisible(false);
        MenuItem item3 = menu.findItem(R.id.action_back);
        item3.setVisible(false);
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
        MenuItem item14 = menu.findItem(R.id.action_clearstem);
        item14.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_getdata) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrame, new FragmentShowData());
            ft.commit();
        } else if (id == R.id.action_prosesall) {
            /*FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrame, new FragmentShowData());
            ft.commit();*/

            //add parameter to check if its a proses all
            //delete tb cache first
            AlertDialog diaBox = dialogProsesAll();
            diaBox.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private AlertDialog dialogProsesAll() {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(getContext())
                .setTitle("Proses")
                .setMessage("Proses semua data sekarang?")
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        db.clearTbCache();
                        /*if (!db.isTBDataIndexEmpty()) {
                            Bobot bobot = new Bobot(getContext(), null, loadingBarHorizontal, mainView, tvInfo, FragmentSearchData.this);
                            bobot.execute();
                        }*/
                        /*
                        if (!db.isTBDataIndexEmpty()) {

                            Vektor vektor = new Vektor(getContext(), null, loadingBarHorizontal, mainView, tvInfo);
                            vektor.execute();
                        }
                        */
                        /*if (!db.isTBDataStemmingEmpty()) {
                            Indexing indexing = new Indexing(getContext(), null, loadingBarHorizontal, mainView, tvInfo, FragmentSearchData.this);
                            indexing.execute();
                        }*/

                        Stoplist stoplist = new Stoplist(data_tostoplist, getContext(), null, loadingBarHorizontal, mainView, tvInfo, FragmentSearchData.this);
                        stoplist.execute();

                        /*
                        if (!db.isTBDataStemmingEmpty()) {
                            Indexing indexing = new Indexing(getContext(), null, loadingBarHorizontal, mainView, tvInfo);
                            indexing.execute();
                        }
                        if (!db.isTBDataIndexEmpty()) {
                            Bobot bobot = new Bobot(getContext(), null, loadingBarHorizontal, mainView, tvInfo);
                            bobot.execute();
                        }
                        if (!db.isTBDataIndexEmpty()) {
                            Vektor bobot = new Vektor(getContext(), null, loadingBarHorizontal, mainView, tvInfo);
                            bobot.execute();
                        }
                        */
                    }
                })
                .create();
        return myQuittingDialogBox;
    }
}