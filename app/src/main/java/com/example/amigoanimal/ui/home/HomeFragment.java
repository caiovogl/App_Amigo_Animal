package com.example.amigoanimal.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amigoanimal.Adocao;
import com.example.amigoanimal.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Adocao> listaAdocoes = new ArrayList<>();
    private EditText editSearch;
    private AdocaoAdapter adapter;
    private String pesquisa = "";
    private boolean isLoading = false;
    private DocumentSnapshot lastVisible;
    private static final int LIMIT = 10;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewAdocoes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AdocaoAdapter(requireContext(), listaAdocoes);
        recyclerView.setAdapter(adapter);

        listaAdocoes.clear();

        IniciarComponentes(view);

        editSearch.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                Drawable drawableEnd = editSearch.getCompoundDrawables()[2];
                if (drawableEnd != null) {
                    int drawableWidth = drawableEnd.getBounds().width();
                    if (event.getRawX() >= (editSearch.getRight() - drawableWidth - editSearch.getPaddingRight())) {
                        pesquisa = editSearch.getText().toString();
                        listaAdocoes.clear();
                        lastVisible = null;
                        adapter.notifyDataSetChanged();
                        carregarAdocoes();
                        v.performClick();

                        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);
                        return true;
                    }
                }
            }
            return false;


        });

        editSearch.setOnEditorActionListener((v, actionId, event) -> {
            pesquisa = editSearch.getText().toString();

            listaAdocoes.clear();
            adapter.notifyDataSetChanged();
            lastVisible = null;

            carregarAdocoes();


            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editSearch.getWindowToken(), 0);

            return true;
        });

        // Adicionando um TextWatcher para a pesquisa, para capturar alterações em tempo real
        /*editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                pesquisa = editable.toString();
                listaAdocoes.clear();
                lastVisible = null;
                adapter.notifyDataSetChanged();
                carregarAdocoes();
            }
        });*/

        carregarAdocoes();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == listaAdocoes.size() - 1) {
                    carregarMaisCadastros();
                }
            }
        });

        return view;
    }

    public void IniciarComponentes(View view){
        editSearch = view.findViewById(R.id.editSearch);
    }

    private void carregarAdocoes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        isLoading = true;

        Query query = db.collection("animaisAdotados")
                .orderBy("pesquisa")
                .limit(LIMIT);

        if (!pesquisa.isEmpty()) {
            query = query.whereArrayContains("pesquisa", pesquisa.toLowerCase());
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                lastVisible = queryDocumentSnapshots.getDocuments()
                        .get(queryDocumentSnapshots.size() - 1);

                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Adocao adocao = doc.toObject(Adocao.class);
                    if (adocao != null && adocao.getImagens() != null && !adocao.getImagens().isEmpty()) {
                        listaAdocoes.add(adocao);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            isLoading = false;
        });
    }

    private void carregarMaisCadastros() {
        isLoading = true;
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Query query = db.collection("animaisAdotados")
                .orderBy("pesquisa")
                .whereArrayContains("pesquisa", pesquisa.toLowerCase())
                .limit(LIMIT);

        if (lastVisible != null) {
            query = query.startAfter(lastVisible);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                lastVisible = queryDocumentSnapshots.getDocuments()
                        .get(queryDocumentSnapshots.size() - 1);

                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    Adocao adocao = doc.toObject(Adocao.class);
                    if (adocao != null && adocao.getImagens() != null && !adocao.getImagens().isEmpty()) {
                        listaAdocoes.add(adocao);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            isLoading = false;
        });
    }

}
