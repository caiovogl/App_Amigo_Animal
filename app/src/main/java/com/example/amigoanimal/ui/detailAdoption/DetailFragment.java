package com.example.amigoanimal.ui.detailAdoption;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.amigoanimal.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DetailFragment extends Fragment {

    private TextView textNomeAdotante, textEmailAdotante, textTelefoneAdotante, textLocalizacaoAdotante;
    private TextView textDadosAnimal, textTipoAnimal, textDataAdocao, textProfissional, textSexoAnimal, textNomeAnimal, textIdadeAnimal;
    private Button buttonDelete, buttonEdit;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    public DetailFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textNomeAdotante = view.findViewById(R.id.textNomeAdotante);
        textEmailAdotante = view.findViewById(R.id.textEmailAdotante);
        textTelefoneAdotante = view.findViewById(R.id.textTelefoneAdotante);
        textDadosAnimal = view.findViewById(R.id.textDadosAnimal);
        textTipoAnimal = view.findViewById(R.id.textTipoAnimal);
        textDataAdocao = view.findViewById(R.id.textDataAdocao);
        textProfissional = view.findViewById(R.id.textProfissional);
        textSexoAnimal = view.findViewById(R.id.textSexoAnimal);
        textNomeAnimal = view.findViewById(R.id.textNomeAnimal);
        textIdadeAnimal = view.findViewById(R.id.textIdadeAnimal);
        textLocalizacaoAdotante = view.findViewById(R.id.textLocalizacaoAdotante);
        buttonDelete = view.findViewById(R.id.buttonDelete);
        buttonEdit = view.findViewById(R.id.buttonEdit);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        Bundle args = getArguments();
        if (args != null) {
            textNomeAdotante.setText("Nome: " + (args.getString("nome_adotante") == "" ? "Não informado" : args.getString("nome_adotante", "Não informado")));
            textEmailAdotante.setText("Email: " + (args.getString("email_adotante") == "" ? "Não informado" : args.getString("email_adotante", "Não informado")));
            textTelefoneAdotante.setText("Telefone: " + (args.getString("telefone_adotante") == "" ? "Não informado" : args.getString("telefone_adotante", "Não informado")));
            textDadosAnimal.setText("Descrição: " + args.getString("dados_animal", "Não informado"));
            textTipoAnimal.setText("Tipo: " + args.getString("tipo_animal", "Não informado"));
            textDataAdocao.setText("Data da Adoção: " + args.getString("data_adocao", "Não informado"));
            textProfissional.setText(args.getString("profissional", "Não informado"));
            textSexoAnimal.setText("Sexo: " + args.getString("sexo_animal", "Não informado"));
            textNomeAnimal.setText("Nome: " + args.getString("nome_animal", "Não informado"));
            textIdadeAnimal.setText("Idade: " + args.getString("idade_animal", "Não informado"));
            textLocalizacaoAdotante.setText("Localização: " + (args.getString("localizacao_adotante") == "" ? "Não informado" : args.getString("localizacao_adotante", "Não informado")));

            ArrayList<String> imagens = args.getStringArrayList("imagens");
            ViewPager2 viewPager = view.findViewById(R.id.viewPagerImagens);
            WormDotsIndicator dotsIndicator = view.findViewById(R.id.dotsIndicator);
            ImagemCarrosselAdapter carouselAdapter = new ImagemCarrosselAdapter(requireContext(), imagens);
            viewPager.setAdapter(carouselAdapter);
            viewPager.setClipToPadding(false);
            viewPager.setClipChildren(false);
            viewPager.setOffscreenPageLimit(3);
            viewPager.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
            dotsIndicator.setViewPager2(viewPager);

        }

        RecyclerView recyclerViewDocumentos = view.findViewById(R.id.recyclerViewDocumentos);
        if (args.containsKey("documentos")) {
            ArrayList<String> documentos = args.getStringArrayList("documentos");
            if (documentos != null && !documentos.isEmpty()) {
                DocumentAdapter docAdapter = new DocumentAdapter(requireContext(), documentos);
                recyclerViewDocumentos.setAdapter(docAdapter);
                recyclerViewDocumentos.setLayoutManager(new LinearLayoutManager(requireContext()));
            }
        }

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(buttonDelete.getText() == "Confirmar"){
                    String data = args.getString("data_criacao", "Não informado");
                    db.collection("animaisAdotados").document(data).delete();
                    deletarPastaCompleta(data);


                    Bundle bundle = new Bundle();

                    NavController navController = Navigation.findNavController(v);
                    navController.navigate(R.id.action_nav_detail_to_homeFragment, bundle);
                }else{
                    buttonDelete.setText("Confirmar");
                    buttonDelete.setBackgroundColor(Color.RED);
                }
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.action_nav_detail_to_editFragment, args);
            }
        });

    }

    public static void deletarPastaCompleta(String pastaPath) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference pastaRef = storage.getReference().child(pastaPath);

        pastaRef.listAll()
                .addOnSuccessListener(listResult -> {
                    for (StorageReference item : listResult.getItems()) {
                        item.delete()
                                .addOnSuccessListener(aVoid -> Log.d("FirebaseStorage", "Arquivo deletado: " + item.getName()))
                                .addOnFailureListener(e -> Log.e("FirebaseStorage", "Erro ao deletar: " + item.getName(), e));
                    }

                    for (StorageReference subPasta : listResult.getPrefixes()) {
                        deletarPastaCompleta(subPasta.getPath()); // chamada recursiva
                    }
                })
                .addOnFailureListener(e -> Log.e("FirebaseStorage", "Erro ao listar a pasta: " + pastaPath, e));
    }
}
