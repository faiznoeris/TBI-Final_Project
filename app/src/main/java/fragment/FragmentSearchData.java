package fragment;

import android.database.Cursor;
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
                data.clear();
                Value = keyword.getText().toString();
                if (Value.equals("")) {
                    Toast.makeText(getContext(), "Keyword kosong!", Toast.LENGTH_SHORT).show();
                } else {
                    Cursor rs = db.getDataSearch(Value);
                    if (rs.moveToFirst()) {
                        while (rs.isAfterLast() == false) {
                            map = new HashMap<>(2);
                            id = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_IDKONTEN));
                            Log.d("SearchData", "Hasil Search, ID = " + id + " | Value Keyword - " + Value);
                            konten = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_KONTEN));
                            judul = rs.getString(rs.getColumnIndex(DBHelper.DATA_COLUMN_JUDUL));

                            String replaced_by_this = WordUtils.capitalize(Value);

                            splitKeyword = konten.split(" ");

                            konten = konten.replaceAll("(?i)"+Value, "<b>" + replaced_by_this + "</b>");
                            konten = konten.replace("\n", "<p></p>");

                            countKeyword=0;

                            for(int i = 0; i < splitKeyword.length; i++){
                                if(StringUtils.containsIgnoreCase(splitKeyword[i], Value)){
                                    countKeyword++;
                                    Log.d("SearchData", "Found keyword - " + splitKeyword[i]);
                                }
                            }

                            map.put("id", id);
                            map.put("content", konten);
                            map.put("title", judul);
                            map.put("countkeyword", String.valueOf(countKeyword));
                            data.add(map);
                            rs.moveToNext();
                        }

                        //sorting most countkeyword
                        Collections.sort(data, new Comparator<Map<String, String>>() {
                            @Override
                            public int compare(Map<String, String> o1, Map<String, String> o2) {
                                int val1 = Integer.parseInt(o1.get("countkeyword"));
                                int val2 = Integer.parseInt(o2.get("countkeyword"));
                                return val1 > val2 ? -1 : (val1 < val2) ? 1 : 0;
                            }
                        });



                        trHeader.setVisibility(View.VISIBLE);
                        listData.setVisibility(View.VISIBLE);

                        SimpleAdapter adapter = new SimpleAdapter(getContext(), data,
                                R.layout.listview_row,
                                new String[]{"id", "content", "title"},
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
                        listData.setAdapter(adapter);
                    } else {
                        Toast.makeText(getContext(), "Data tidak ditemukan!", Toast.LENGTH_SHORT).show();
                    }


                    if (!rs.isClosed()) {
                        rs.close();
                    }
                }
            }
        });

        listData.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String,String> map =(HashMap<String,String>)listData.getItemAtPosition(position);
                String id_click = map.get("title");
                String cKey = map.get("countkeyword");
                Toast.makeText(getContext(), "Judul: " + id_click + " || Jumlah keyword muncul: " + cKey, Toast.LENGTH_SHORT).show();
            }
        });

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
