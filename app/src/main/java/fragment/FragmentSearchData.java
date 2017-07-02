package fragment;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
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

/**
 * Created by Vellfire on 31/05/2017.
 */

public class FragmentSearchData extends Fragment {
    Button search;
    ListView listData;
    EditText keyword;
    TableRow trHeader;

    DBHelper db;

    String id, konten, judul, Value;

    int countKeyword = 0;
    String[] splitKeyword;

    Map<String, String> map;
    List<Map<String, String>> data = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_searchdata, container, false);

        search = (Button) rootView.findViewById(R.id.btnSearch);
        listData = (ListView) rootView.findViewById(R.id.listData);
        keyword = (EditText) rootView.findViewById(R.id.etKeyword);
        trHeader = (TableRow) rootView.findViewById(R.id.trKonten);

        db = new DBHelper(getContext());

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Value = keyword.getText().toString();
                if (Value.equals("")) {
                    Toast.makeText(getContext(), "Keyword kosong!", Toast.LENGTH_SHORT).show();
                }else {
                    AmbilCache ambilCache = new AmbilCache(getContext(), FragmentSearchData.this);
                    ambilCache.execute(keyword.getText().toString());
                }
            }
        });

        listData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String,String> map =(HashMap<String,String>)listData.getItemAtPosition(position);
                //String id_click = map.get("title");
                //String cKey = map.get("countkeyword");
                String link = map.get("link");
               // Toast.makeText(getContext(), "Judul: " + id_click + " || Jumlah keyword muncul: " + cKey, Toast.LENGTH_SHORT).show();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(browserIntent);

                Toast.makeText(getContext(), link + " | " + map.get("title"), Toast.LENGTH_SHORT).show();
            }
        });

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
        MenuItem item=menu.findItem(R.id.action_getdata);
        item.setVisible(true);
        MenuItem item2=menu.findItem(R.id.action_searchdata);
        item2.setVisible(false);
        MenuItem item3=menu.findItem(R.id.action_back);
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_getdata) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrame, new FragmentShowData());
            ft.commit();
        }
        return super.onOptionsItemSelected(item);
    }
}
