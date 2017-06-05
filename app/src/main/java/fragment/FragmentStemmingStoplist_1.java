package fragment;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import faiznoeris.tbitugaspraktek.temubalikinformasi.DBHelper;
import faiznoeris.tbitugaspraktek.temubalikinformasi.JSONParsing;
import faiznoeris.tbitugaspraktek.temubalikinformasi.MainActivity;
import faiznoeris.tbitugaspraktek.temubalikinformasi.R;

/**
 * Created by Vellfire on 03/05/2017.
 */

public class FragmentStemmingStoplist_1 extends Fragment {
    private final String TAG_LOD_D = "Stemming-Stoplist-#1";
    private final String[] stopwords_list = {"yang","di","dan","itu","dengan","untuk","tidak","ini","dari","dalam","akan","pada","juga","saya","ke","karena","tersebut","bisa","ada","mereka","lebih","kata",
            "tahun","sudah","atau","saat","oleh","menjadi","orang","ia","telah","adalah", "seperti", "sebagai", "bahwa" ,"dapat","para","harus","namun","kita", "dua","satu", "masih","hari",};
    String[] stoplisting,hasilstop;
    String text;

    Button btntampil;
    EditText etkata;
    TextView tvinfo, tvhasil;
    RadioGroup rb;
    RadioButton rbstem, rbstop;

    DBHelper db;


    AssetManager am;
    BufferedReader bufferedReader;
    InputStream is;
    String str;

    ArrayList<String> katadasar = new ArrayList<String>();
    /*public static FragmentStemmingStoplist_1 newInstance() {
        FragmentStemmingStoplist_1 fragment = new FragmentStemmingStoplist_1();
        return fragment;
    }*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stemmingstoplist_1, container, false);



        btntampil = (Button) rootView.findViewById(R.id.btnTampil);
        etkata = (EditText) rootView.findViewById(R.id.etKata);
        tvinfo = (TextView) rootView.findViewById(R.id.tvInfo);
        tvhasil = (TextView) rootView.findViewById(R.id.tvHasil);
        rb = (RadioGroup) rootView.findViewById(R.id.rGroup);
        rbstem = (RadioButton) rootView.findViewById(R.id.rbStem);
        rbstop = (RadioButton) rootView.findViewById(R.id.rbStop);

        tvinfo.setText("Pilih menu untuk melakukan Stemming atau Stoplisting pada radiobutton dibawah.");

        rb.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(rbstem.isChecked()){
                    tvinfo.setText("Tulislah sebuah kata pada kolom text dibawah kemudian tekan tombol go untuk mendapatkan hasil stemming.");
                }else{
                    tvinfo.setText("Tulislah sebuah kalimat pada kolom dibawah kemudian tekan tombol go untuk mendapatkan hasil stoplisting.");
                }
            }
        });

        btntampil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text = etkata.getText().toString();

                if(rbstem.isChecked() && text.contains(" ")){
                    Toast.makeText(getContext(), "Dalam proses stemming hanya bisa menginputkan satu kata", Toast.LENGTH_SHORT).show();
                }else if(rbstop.isChecked() && !(text.isEmpty())){
                    stoplisting = text.split(" ");

                    for(int i = 0; i < stoplisting.length; i++){
                        for(int k = 0; k < stopwords_list.length; k++) {
                            if (stoplisting[i].equalsIgnoreCase(stopwords_list[k])) {
                                stoplisting[i] = "";
                                Log.d(TAG_LOD_D, "Stoplisting - Kata yang dihilangkan: " + stopwords_list[k]);
                            }
                        }
                    }
                    StringBuilder builder = new StringBuilder();
                    for(String s : stoplisting) {
                        builder.append(s + " ");
                    }
                    tvhasil.setText(builder);
                }else if(rbstem.isChecked() && !(text.isEmpty())){

// ==========no db
                    db = new DBHelper(getContext());
                    /*am = getContext().getAssets();

                    try {
                        is = am.open("kata_dasar_indo.txt");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    bufferedReader = new BufferedReader(new InputStreamReader(is));

                    try {
                        while ((str = bufferedReader.readLine()) != null) {
                            katadasar.add(str);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
// ==========

                    Log.d(TAG_LOD_D, "Stemming - Sebelum: " + text);
                    //Langkah 1 - Penghapusan Partikel
                    //for(int i = 0; i < katadasar.size(); i++) {
                        if (db.isKataDasar(text) == false && !(text.length() < 4)) {
                            if (text.substring(text.length() - 3).equalsIgnoreCase("kah") || text.substring(text.length() - 3).equalsIgnoreCase("lah")
                                    || text.substring(text.length() - 3).equalsIgnoreCase("pun")) {
                                text = text.substring(0, text.length() - 3);
                            }
                        }
                        tvhasil.setText("Langkah 1 - Hapus Partikel: " + text);


                        //Langkah 2 - Penghapusan Possesive Pronouns
                        if (db.isKataDasar(text) == false && !(text.length() < 4)) {
                            if (text.substring(text.length() - 2).equalsIgnoreCase("ku") || text.substring(text.length() - 2).equalsIgnoreCase("mu")) {
                                text = text.substring(0, text.length() - 2);
                                System.out.println(text);
                            } else if (text.substring(text.length() - 3).equalsIgnoreCase("nya")) {
                                text = text.substring(0, text.length() - 3);
                            }
                        }
                        tvhasil.setText(tvhasil.getText() + "\nLangkah 2 - Hapus Possesive Pronouns: " + text);

                        //Langkah 3 - Hapus Awalan Pertama
                        if (db.isKataDasar(text) == false && !(text.length() < 4)) {
                            if (text.substring(0, 4).equalsIgnoreCase("meng")) {
                                if (text.substring(4, 5).equalsIgnoreCase("a")) {
                                    text = "K" + text.substring(4, text.length());
                                } else {
                                    text = text.substring(4, text.length());
                                }
                            } else if (text.substring(0, 4).equalsIgnoreCase("meny")) {
                                if (text.substring(4, 5).equalsIgnoreCase("a")) {
                                    text = "S" + text.substring(4, text.length());
                                } else if (text.substring(4, 5).equalsIgnoreCase("u")) {
                                    text = "C" + text.substring(4, text.length());
                                }
                            } else if (text.substring(0, 3).equalsIgnoreCase("men")) {
                                text = text.substring(3, text.length());
                            } else if (text.substring(0, 3).equalsIgnoreCase("mem")) {
                                if (text.substring(3, 4).equalsIgnoreCase("a") || text.substring(3, 4).equalsIgnoreCase("i") || text.substring(3, 4).equalsIgnoreCase("u")
                                        || text.substring(3, 4).equalsIgnoreCase("e") || text.substring(3, 4).equalsIgnoreCase("o")) {
                                    text = "M" + text.substring(3, text.length());
                                } else {
                                    text = text.substring(3, text.length());
                                }
                            } else if (text.substring(0, 2).equalsIgnoreCase("me")) {
                                text = text.substring(2, text.length());
                            } else if (text.substring(0, 4).equalsIgnoreCase("peng")) {
                                text = text.substring(4, text.length());
                            } else if (text.substring(0, 4).equalsIgnoreCase("peny")) {
                                text = "S" + text.substring(4, text.length());
                            } else if (text.substring(0, 3).equalsIgnoreCase("pen")) {
                                text = text.substring(3, text.length());
                            } else if (text.substring(0, 3).equalsIgnoreCase("pem")) {
                                text = "P" + text.substring(3, text.length());
                            } else if (text.substring(0, 2).equalsIgnoreCase("di")) {
                                text = text.substring(2, text.length());
                            } else if (text.substring(0, 3).equalsIgnoreCase("ter")) {
                                text = text.substring(3, text.length());
                            } else if (text.substring(0, 2).equalsIgnoreCase("ke")) {
                                text = text.substring(2, text.length());
                            }
                        }
                        tvhasil.setText(tvhasil.getText() + "\nLangkah 2 - Hapus Awalan Pertama: " + text);

                        //Langkah 4 - Hapus Awalan Kedua
                        if (db.isKataDasar(text) == false && !(text.length() < 4)) {
                            if (text.substring(0, 3).equalsIgnoreCase("ber")) {
                                text = text.substring(3, text.length());
                            } else if (text.substring(0, 3).equalsIgnoreCase("bel")) {
                                text = text.substring(3, text.length());
                            } else if (text.substring(0, 2).equalsIgnoreCase("be")) {
                                text = text.substring(2, text.length());
                            } else if (text.substring(0, 3).equalsIgnoreCase("per")) {
                                text = text.substring(3, text.length());
                            } else if (text.substring(0, 3).equalsIgnoreCase("pel")) {
                                text = text.substring(3, text.length());
                            } else if (text.substring(0, 2).equalsIgnoreCase("pe")) {
                                text = text.substring(2, text.length());
                            }
                        }
                        tvhasil.setText(tvhasil.getText() + "\nLangkah 4 - Hapus Awalan Kedua: " + text);

                        //Langkah 5 - Hapus Akhiran
                        if (db.isKataDasar(text) == false && !(text.length() < 4)) {
                            if (text.substring(text.length() - 3).equalsIgnoreCase("kan")) {
                                text = text.substring(0, text.length() - 3);
                            } else if (text.substring(text.length() - 2).equalsIgnoreCase("an")) {
                                text = text.substring(0, text.length() - 2);
                            } else if (text.substring(text.length() - 1).equalsIgnoreCase("i")) {
                                text = text.substring(0, text.length() - 1);
                            }
                        }
                        tvhasil.setText(tvhasil.getText() + "\nLangkah 5 - Hapus Akhiran: " + text);
                        Log.d(TAG_LOD_D, "Stemming - Sesudah: " + text);
                    //}
                }else if(!(rbstem.isChecked()) && !(rbstop.isChecked())){
                    Toast.makeText(getContext(), "Silahkan pilih menu untuk melakukan stemming atau stoplisting pada radiobutton diatas", Toast.LENGTH_SHORT).show();
                }else if((rbstem.isChecked() || rbstop.isChecked()) && text.equals("")){
                    Toast.makeText(getContext(), "Tolong masukkan sebuah kata/kalimat sebelum melakukan proses stemming / stoplisting", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ((MainActivity) getActivity()).setActionBarTitle("Stemming dan Stoplist #1");

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
        MenuItem item2=menu.findItem(R.id.action_searchdata);
        MenuItem item3=menu.findItem(R.id.action_back);
        item.setVisible(false);
        item2.setVisible(false);
        item3.setVisible(false);
    }
}
