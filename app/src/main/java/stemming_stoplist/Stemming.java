package stemming_stoplist;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import faiznoeris.tbitugaspraktek.temubalikinformasi.DBHelper;
import faiznoeris.tbitugaspraktek.temubalikinformasi.R;
import fragment.FragmentStemmingStoplist_2;

/**
 * Created by Vellfire on 20/04/2017.
 */

public class Stemming extends AsyncTask<Void, String, List<Map<String, String>>> {
    private final String TAG_LOG_D = "Stemming";

    String belakang_1, belakang_2, belakang_3, depan_2, depan_3, depan_4, dasar, lowered;
    String[] word;

    FragmentStemmingStoplist_2 fragmentStoplistStemming_2;
    DBHelper db;
    Context context;

    View loadingView;
    View tampilanView;

    Map<String, String> map;
    List<Map<String, String>> data = new ArrayList<>();
    ArrayList<String> id = new ArrayList<String>(), content = new ArrayList<String>(), title = new ArrayList<String>();

    AssetManager am;
    BufferedReader bufferedReader;
    InputStream is;
    String str;

    long startTime, endTime;

    public static int totalWord;
    String str_id,str_content,str_title;
    private Handler progressHandler = new Handler();

    public Stemming(List<Map<String, String>> data, Context context, FragmentStemmingStoplist_2 fragmentStoplistStemming_2, View loadingView, View tampilanView) throws IOException {
        this.fragmentStoplistStemming_2 = fragmentStoplistStemming_2;
        this.context = context;
        this.data = data;
        this.loadingView = loadingView;
        this.tampilanView = tampilanView;
    }

    @Override
    protected void onPreExecute() {
        startTime = System.currentTimeMillis();
    }

    @Override
    protected List<Map<String, String>> doInBackground(Void... params) {
        db = new DBHelper(context);

        for (Map<String, String> tempmap : data) {
            for (Map.Entry<String, String> entry : tempmap.entrySet()) {
                if (entry.getKey().equals("content") && !(entry.getValue() == null)) {
                    content.add(entry.getValue());
                } else if(entry.getKey().equals("id") && !(entry.getValue() == null)){
                    id.add(entry.getValue());
                } else if(entry.getKey().equals("title") && !(entry.getValue() == null)){
                    title.add(entry.getValue());
                }
            }
        }

        try {
            for (int i = 0; i < content.size(); i++) {
                word = content.get(i).split(" ");
                for (int j = 0; j < word.length; j++) {
                    totalWord++;
                    if (word[j].length() < 4) {
                        continue;
                    }

                    lowered = word[j].toLowerCase();
                    belakang_1 = word[j].substring(word[j].length() - 1);
                    belakang_2 = word[j].substring(word[j].length() - 2);
                    belakang_3 = word[j].substring(word[j].length() - 3);
                    if (word[j].length() > 2) {
                        depan_2 = word[j].substring(0, 2);
                        depan_3 = word[j].substring(0, 3);
                        depan_4 = word[j].substring(0, 4);
                    }



                    Log.d(TAG_LOG_D, "Sebelum stem: " + lowered + " - " + String.valueOf(db.isKataDasar(lowered)));

                    //Langkah 1 - Penghapusan Partikel
                    if (db.isKataDasar(lowered) == false) {
                        if (belakang_3.equalsIgnoreCase("kah") || belakang_3.equalsIgnoreCase("lah") || belakang_3.equalsIgnoreCase("pun")) {
                            word[j] = word[j].substring(0, word[j].length() - 3);
                        }
                    }

                    if (word[j].length() < 4) {
                        continue;
                    }

                    //Langkah 2 - Penghapusan Possesive Pronouns
                    if (db.isKataDasar(word[j]) == false) {
                        if (word[j].length() > 4) {
                            if (belakang_2.equalsIgnoreCase("ku") || belakang_2.equalsIgnoreCase("mu")) {
                                word[j] = word[j].substring(0, word[j].length() - 2);
                            } else if (belakang_3.equalsIgnoreCase("nya")) {
                                word[j] = word[j].substring(0, word[j].length() - 3);
                            }
                        }
                    }

                    if (word[j].length() < 4) {
                        continue;
                    }

                    //Langkah 3 - Hapus Awalan Pertama
                    if (db.isKataDasar(word[j]) == false) {
                        if (depan_4.equalsIgnoreCase("meng")) {
                            if (word[j].substring(4, 5).equals("a")) {
                                word[j] = "K" + word[j].substring(4, word[j].length());
                            } else {
                                word[j] = word[j].substring(4, word[j].length());
                            }
                        } else if (depan_4.equalsIgnoreCase("meny")) {
                            // || word[j].substring(4).equals("usul") || word[j].substring(4).equals("usu")
                            if (word[j].substring(4, 5).equalsIgnoreCase("a")) {
                                word[j] = "S" + word[j].substring(4, word[j].length());
                            } else if (word[j].substring(4, 5).equalsIgnoreCase("u")) {
                                word[j] = "C" + word[j].substring(4, word[j].length());
                            }
                        } else if (depan_3.equalsIgnoreCase("men")) {
                            word[j] = word[j].substring(3, word[j].length());
                        } else if (depan_3.equalsIgnoreCase("mem")) {
                            if (word[j].substring(3, 4).equalsIgnoreCase("a") || word[j].substring(3, 4).equalsIgnoreCase("i") || word[j].substring(3, 4).equalsIgnoreCase("u")
                                    || word[j].substring(3, 4).equalsIgnoreCase("e") || word[j].substring(3, 4).equalsIgnoreCase("o")) {
                                word[j] = "M" + word[j].substring(3, word[j].length());
                            } else {
                                word[j] = word[j].substring(3, word[j].length());
                            }
                        } else if (depan_2.equalsIgnoreCase("me")) {
                            word[j] = word[j].substring(2, word[j].length());
                        } else if (depan_4.equalsIgnoreCase("peng")) {
                            word[j] = word[j].substring(4, word[j].length());
                        } else if (depan_4.equalsIgnoreCase("peny")) {
                            word[j] = "S" + word[j].substring(4, word[j].length());
                        } else if (depan_3.equalsIgnoreCase("pen")) {
                            word[j] = word[j].substring(3, word[j].length());
                        } else if (depan_3.equalsIgnoreCase("pem")) {
                            word[j] = "P" + word[j].substring(3, word[j].length());
                        } else if (depan_2.equalsIgnoreCase("di")) {
                            word[j] = word[j].substring(2, word[j].length());
                        } else if (depan_3.equalsIgnoreCase("ter")) {
                            word[j] = word[j].substring(3, word[j].length());
                        } else if (depan_2.equalsIgnoreCase("ke")) {
                            word[j] = word[j].substring(2, word[j].length());
                        }
                    }

                    if (word[j].length() < 4) {
                        continue;
                    }

                    //Langkah 4 - Hapus Awalan Kedua
                    if (db.isKataDasar(word[j]) == false) {
                        if (depan_3.equalsIgnoreCase("ber")) {
                            word[j] = word[j].substring(3, word[j].length());
                        } else if (depan_3.equalsIgnoreCase("bel")) {
                            word[j] = word[j].substring(3, word[j].length());
                        } else if (depan_2.equalsIgnoreCase("be")) {
                            word[j] = word[j].substring(2, word[j].length());
                        } else if (depan_3.equalsIgnoreCase("per")) {
                            word[j] = word[j].substring(3, word[j].length());
                        } else if (depan_3.equalsIgnoreCase("pel")) {
                            word[j] = word[j].substring(3, word[j].length());
                        } else if (depan_2.equalsIgnoreCase("pe")) {
                            word[j] = word[j].substring(2, word[j].length());
                        }
                    }

                    if (word[j].length() < 4) {
                        continue;
                    }

                    //Langkah 5 - Hapus Akhiran
                    if (db.isKataDasar(word[j]) == false) {
                        if (belakang_3.equalsIgnoreCase("kan")) {
                            word[j] = word[j].substring(0, word[j].length() - 3);
                        } else if (belakang_2.equalsIgnoreCase("an")) {
                            word[j] = word[j].substring(0, word[j].length() - 2);
                        } else if (belakang_1.equalsIgnoreCase("i")) {
                            word[j] = word[j].substring(0, word[j].length() - 1);
                        }
                    }
                    Log.d(TAG_LOG_D, "Hasil stem: " + word[j]);
                }
                //penyatuan kembali kata-kata yang distem
                StringBuilder builder = new StringBuilder();
                for (String s : word) {
                    builder.append(s + " ");
                }
                Log.d(TAG_LOG_D, "Kalimat akhir: " + builder.toString());
                content.set(i, builder.toString());
            }
        }catch (Exception e){
            e.printStackTrace();
            progressHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Terjadi error!", Toast.LENGTH_SHORT).show();
                }
            });
            endTime = System.currentTimeMillis();
            Log.d(TAG_LOG_D, "ERROR, Time spent = " + (endTime-startTime)/1000 + " seconds | Total Words Stemmed = " + totalWord);
        }

        //memasukkan data kedalam list
        data.clear();
        for(int i = 0; i < content.size(); i++){
            str_id = id.get(i);
            str_content = content.get(i);
            str_title = title.get(i);
            map = new HashMap<>(2);
            map.put("id",str_id);
            map.put("content",str_content);
            map.put("title",str_title);
            data.add(map);
            if (!(db.isDataStemmingExist(str_id))) {
                if (db.addDataStemming(str_id, str_content, str_title)) {
                    //counter++;
                    Log.d(TAG_LOG_D, "Data Stemming Inserted. (" + str_id + ", " + str_title + ")");
                }
            }
            Log.d(TAG_LOG_D, "Data Stemming added - " + str_id);
        }

        return data;
    }

    @Override
    protected void onPostExecute(List<Map<String, String>> maps) {
        endTime = System.currentTimeMillis();
        Log.d(TAG_LOG_D, "Done, Time spent = " + (endTime-startTime)/1000 + " seconds | Total Words Stemmed = " + totalWord);
        showProgress(true);
        //set adapter
        SimpleAdapter adapter = new SimpleAdapter(context, maps,
                R.layout.row,
                new String[]{"id", "content"},
                new int[]{R.id.tvId,
                        R.id.tvJudul});

        Toast.makeText(context, "Task done in " + (endTime-startTime)/1000 + " seconds", Toast.LENGTH_SHORT).show();

        fragmentStoplistStemming_2.setData(data, "");
        fragmentStoplistStemming_2.setAdapter(adapter, "stemming");
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

            tampilanView.setVisibility(show ? View.GONE : View.VISIBLE);
            tampilanView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tampilanView.setVisibility(show ? View.GONE : View.VISIBLE);
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
            tampilanView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
