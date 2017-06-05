package fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import faiznoeris.tbitugaspraktek.temubalikinformasi.MainActivity;
import faiznoeris.tbitugaspraktek.temubalikinformasi.R;


/**
 * Created by Vellfire on 02/05/2017.
 */

public class FragmentHitungKata extends Fragment {

    String kalimat;
    String[] kalimat_split;
    int count;

    Button btntampil;
    EditText etkalimat;
    TextView tvhasil,tvanggota;

    Map<String,Integer> hitung_kata = new HashMap<>();

    /*public static FragmentHitungKata newInstance(){
        FragmentHitungKata fragment = new FragmentHitungKata();
        return fragment;
    }*/

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hitungkata, container, false);

        btntampil = (Button) rootView.findViewById(R.id.btnTampil);
        etkalimat = (EditText) rootView.findViewById(R.id.etKalimat);
        tvhasil = (TextView) rootView.findViewById(R.id.tvHasil);
        tvanggota = (TextView) rootView.findViewById(R.id.tvAnggota);

        tvanggota.setText("Anggota Kelompok:\n1. Bayu Andrianto (0021)\n2. Syaeful Hidayat (0025)\n3. Muhammad Faiz Noeris (0027)\n4. Rifdhotul Alfiansyah (0033)\n5. Maskur Al Asad (0035)\n6. Yogi Hendra (0032)\n7. Yosua Sandy Garsa (0034)");

        btntampil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etkalimat.getText().toString().isEmpty()) {
                    Toast.makeText(getContext(), "Ketik kalimat terlebih dahulu.", Toast.LENGTH_SHORT).show();

                } else {
                    kalimat = etkalimat.getText().toString();
                    kalimat_split = kalimat.split(" ");

                    for (int i = 0; i < kalimat_split.length; i++) {
                        if (hitung_kata.containsKey(kalimat_split[i])) {
                            count = hitung_kata.get(kalimat_split[i]);
                            count++;
                            hitung_kata.put(kalimat_split[i], count);
                        } else {
                            hitung_kata.put(kalimat_split[i], 1);
                        }
                    }
                    tvhasil.setText(hitung_kata.toString());
                    hitung_kata.clear();
                }
            }
        });

        ((MainActivity) getActivity()).setActionBarTitle("Hitung Kata");
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    //hide menu
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
