package br.com.caelum.casadocodigo.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.mugen.Mugen;
import com.mugen.MugenCallbacks;

import java.io.Serializable;
import java.util.ArrayList;

import br.com.caelum.casadocodigo.R;
import br.com.caelum.casadocodigo.adapter.LivroAdapter;
import br.com.caelum.casadocodigo.adapter.LivroInvertidoAdapter;
import br.com.caelum.casadocodigo.modelo.Livro;
import br.com.caelum.casadocodigo.services.WebClient;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ListaLivrosFragment extends Fragment implements Serializable {

    private FirebaseRemoteConfig mfirebaseRemoteConfig;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mfirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mfirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        mfirebaseRemoteConfig.fetch(15).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                mfirebaseRemoteConfig.activateFetched();
            }
        });
    }


    @BindView(R.id.fragment_lista_livros)
    RecyclerView lista;
    private boolean carregando;
    private ArrayList<Livro> livros;


    public static ListaLivrosFragment com(ArrayList<Livro> livros) {
        ListaLivrosFragment listaLivrosFragment = new ListaLivrosFragment();

        Bundle argumentos = new Bundle();
        argumentos.putSerializable("livros", livros);

        listaLivrosFragment.setArguments(argumentos);

        return listaLivrosFragment;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.fragment_lista_livros, container, false);

        ButterKnife.bind(this, view);

        Bundle arguments = getArguments();
        livros = (ArrayList<Livro>) arguments.getSerializable("livros");


        lista.setAdapter(new LivroAdapter(livros));

        lista.setLayoutManager(new LinearLayoutManager(getContext()));


        Mugen.with(lista, new MugenCallbacks() {
            @Override
            public void onLoadMore() {
                carregando = true;
                new WebClient().buscaLivros(livros.size(), 5);
                Snackbar.make(lista, "Carregando", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public boolean isLoading() {
                return carregando;
            }

            @Override
            public boolean hasLoadedAllItems() {
                return false;
            }
        }).start();


        return view;


    }


    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        ActionBar actionBar = activity.getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(false);

        actionBar.setTitle("Catalogo");
        actionBar.setSubtitle(null);

        if (mfirebaseRemoteConfig.getBoolean("list_type_single_item")){
            lista.setAdapter(new LivroInvertidoAdapter(livros));
        }else {
            lista.setAdapter(new LivroAdapter(livros));
        }



    }

    public void adiciona(ArrayList<Livro> novos) {

        carregando = false;
        livros.addAll(novos);
        lista.getAdapter().notifyDataSetChanged();
    }
}
