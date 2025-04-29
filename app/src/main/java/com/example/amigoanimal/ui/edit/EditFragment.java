package com.example.amigoanimal.ui.edit;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.amigoanimal.R;
import com.example.amigoanimal.databinding.FragmentCreateBinding;
import com.example.amigoanimal.databinding.FragmentEditBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri uri = null;
    private List<Uri> images,documents;
    private ArrayList<String> imagensAntigas, documentosAntigos;
    private ActivityResultLauncher<String> pickImage, pickDocument;
    private Switch animalType, sexoAnimalSwitch;
    private RadioGroup radioGroupIdade;
    private EditText editName, editEmail, editPhone, editAnimalData, editAdoptionDate, editNomeAnimal, LocalizacaoAdotante;
    private Button button_addImage, button_addDocuments, button_save;
    private LinearLayout edit_images, edit_documents;
    private String data_criacao;

    private FragmentEditBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        EditViewModel editViewModel =
                new ViewModelProvider(this).get(EditViewModel.class);

        binding = FragmentEditBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        images = new ArrayList<>();
        documents = new ArrayList<>();

        Bundle args = getArguments();

        IniciarComponentes(args);

        pickImage = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    for (Uri uri : uris) {
                        SalvarImagem(uri);
                    }
                    if (!uris.isEmpty()) {
                        this.uri = uris.get(0);
                    }
                }
        );

        pickDocument = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    for (Uri uri: uris){
                        SalvarDocumentos(uri);
                    }
                    if (!uris.isEmpty()) {
                        this.uri = uris.get(0);
                    }
                }
        );

        button_addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage.launch("image/*");
            }
        });

        button_addDocuments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDocument.launch("*/*");
            }
        });

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Salvar();
            }
        });



        return root;
    }

    private void IniciarComponentes(Bundle args){
        editName = binding.editName;
        editEmail = binding.editEmail;
        editPhone = binding.editPhone;
        editAnimalData = binding.editAnimalData;
        editAdoptionDate = binding.editAdoptionDate;
        button_addImage = binding.buttonAddImage;
        button_addDocuments = binding.buttonAddDocuments;
        button_save = binding.buttonSave;
        edit_images = binding.createImages;
        edit_documents = binding.createDocuments;
        animalType = binding.animalType;
        sexoAnimalSwitch = binding.sexoAnimal;
        radioGroupIdade = binding.radioGroupIdade;
        LocalizacaoAdotante = binding.editLocalizaoAdotante;
        editNomeAnimal = binding.editNomeAnimal;
        data_criacao = args.getString("data_criacao");

        imagensAntigas = new ArrayList<>();
        documentosAntigos = new ArrayList<>();

        editName.setText(args.getString("nome_adotante"));
        editEmail.setText(args.getString("email_adotante"));
        editPhone.setText(args.getString("telefone_adotante"));
        editAnimalData.setText(args.getString("dados_animal"));
        editAdoptionDate.setText(args.getString("data_adocao"));
        LocalizacaoAdotante.setText(args.getString("localizacao_adotante"));
        editNomeAnimal.setText(args.getString("nome_animal"));

        boolean tipoAnimal = false;
        if(args.getString("tipo_animal") != null && args.getString("tipo_animal").equals("gato")){
            tipoAnimal = true;
        }
        animalType.setChecked(tipoAnimal);

        boolean sexoAnimal = false;
        if(args.getString("sexo_animal") != null && args.getString("sexo_animal").equals("Fêmea")){
            sexoAnimal = true;
        }
        sexoAnimalSwitch.setChecked(sexoAnimal);

        int idadeAnimal = 0;
        if(args.getString("idade_animal") != null) {
            ArrayList<View> views = new ArrayList<>();
            radioGroupIdade.findViewsWithText(views, args.getString("idade_animal"), View.FIND_VIEWS_WITH_TEXT);
            if (!views.isEmpty()) {
                idadeAnimal = views.get(0).getId();
            }
        }
        radioGroupIdade.check(idadeAnimal);

        ArrayList<String> imagens = args.getStringArrayList("imagens");
        if(imagens != null) {
            for (String imagem : imagens) {
                SalvarImagemUrl(imagem);
            }
        }

        ArrayList<String> documentos = args.getStringArrayList("documentos");
        if(documentos != null) {
            for (String documento : documentos) {
                SalvarDocumentosUrl(documento);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        if (result != null && result.contains("/")) {
            result = result.substring(result.lastIndexOf("/") + 1);
        }

        return result;
    }

    private void ExcluirView(LinearLayout base,View view){
        base.removeView(view);
        base.invalidate();
        base.requestLayout();
    }

    private void SalvarImagem(Uri uriNovo){
        if(uriNovo != null) {
            View imgAdd = getLayoutInflater().inflate(R.layout.item_image, edit_images, false);
            ImageView imageView = imgAdd.findViewById(R.id.imageView);
            imageView.setImageURI(uriNovo);
            edit_images.addView(imgAdd);
            images.add(uriNovo);
            imgAdd.findViewById(R.id.image_close).setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   ExcluirView(edit_images,imgAdd);
                   images.remove(uriNovo);
               }
            });
            edit_images.invalidate();
            edit_images.requestLayout();
        }else{
            Snackbar snackbar = Snackbar.make(binding.getRoot(), "Erro ao carregar a imagem", Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(Color.WHITE);
            snackbar.setTextColor(Color.BLACK);
            snackbar.show();
        }
    }

    private void SalvarImagemUrl(String url){
        View imgAdd = getLayoutInflater().inflate(R.layout.item_image, edit_images, false);
        ImageView imageView = imgAdd.findViewById(R.id.imageView);
        Glide.with(requireContext()).load(url).into(imageView);
        edit_images.addView(imgAdd);
        Log.d("teste",url);
        imagensAntigas.add(url);
        imgAdd.findViewById(R.id.image_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExcluirView(edit_images,imgAdd);
                imagensAntigas.remove(url);

                StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                ref.delete().addOnSuccessListener(aVoid -> {
                    Log.d("STORAGE", "Imagem removida do storage com sucesso.");
                }).addOnFailureListener(e -> {
                    Log.d("STORAGE", "Erro ao remover imagem do storage: " + e.getMessage());
                });
            }
        });
    }

    private void SalvarDocumentosUrl(String url){
        View documentAdd = getLayoutInflater().inflate(R.layout.item_document, edit_documents, false);
        TextView nomeDoc = documentAdd.findViewById(R.id.nomeDocumento);
        nomeDoc.setText(getFileNameFromUri(Uri.parse(url)));
        edit_documents.addView(documentAdd);
        documentosAntigos.add(url);
        documentAdd.findViewById(R.id.image_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExcluirView(edit_documents,documentAdd);
                documentosAntigos.remove(url);

                StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl(url);
                ref.delete().addOnSuccessListener(aVoid -> {
                    Log.d("STORAGE", "Imagem removida do storage com sucesso.");
                }).addOnFailureListener(e -> {
                    Log.d("STORAGE", "Erro ao remover imagem do storage: " + e.getMessage());
                });
            }
        });
    }

    private void SalvarDocumentos(Uri uriNovo){
        if(uriNovo != null) {
            View documentAdd = getLayoutInflater().inflate(R.layout.item_document, edit_documents, false);
            TextView nomeDoc = documentAdd.findViewById(R.id.nomeDocumento);
            nomeDoc.setText(getFileNameFromUri(uriNovo));
            edit_documents.addView(documentAdd);
            documents.add(uriNovo);
            documentAdd.findViewById(R.id.image_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExcluirView(edit_documents,documentAdd);
                    documents.remove(uriNovo);
                }
            });
            edit_documents.invalidate();
            edit_documents.requestLayout();
        }else{
            Snackbar snackbar = Snackbar.make(binding.getRoot(), "Erro ao carregar o documento", Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(Color.WHITE);
            snackbar.setTextColor(Color.BLACK);
            snackbar.show();
        }
    }

    private void Salvar() {
        button_save.setEnabled(false);
        button_save.setText("Atualizando...");

        String nomeAdotante = editName.getText().toString();
        String emailAdotante = editEmail.getText().toString();
        String telefoneAdotante = editPhone.getText().toString();
        String dadosAnimal = editAnimalData.getText().toString();
        String tipoAnimal = animalType.isChecked() ? "gato" : "cachorro";
        String dataAdocao = editAdoptionDate.getText().toString();
        String localizacaoAdotante = LocalizacaoAdotante.getText().toString();
        String nomeAnimal = editNomeAnimal.getText().toString();
        String sexoAnimal = sexoAnimalSwitch.isChecked() ? "Fêmea" : "Macho";
        String idadeAnimal = "";
        int selectedId = radioGroupIdade.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton radioButton = radioGroupIdade.findViewById(selectedId);
            idadeAnimal = radioButton.getText().toString();
        }

        Log.d("teste",data_criacao);

        SalvarArquivosComUrls(data_criacao, nomeAdotante, emailAdotante, telefoneAdotante, dadosAnimal, tipoAnimal, dataAdocao, data_criacao, nomeAnimal, sexoAnimal, idadeAnimal, localizacaoAdotante);
    }

    private void SalvarArquivosComUrls(String pastaBase, String nome, String email, String telefone,
                                       String dadosAnimal, String tipoAnimal, String dataAdocao, String dataCriacao,
                                        String nomeAnimal, String sexoAnimal, String idadeAnimal, String localizacaoAdotante) {

        List<String> urlsImagens = new ArrayList<>();
        List<String> urlsDocumentos = new ArrayList<>();

        if (getArguments() != null) {
            if (imagensAntigas != null) urlsImagens.addAll(imagensAntigas);
            if (documentosAntigos != null) urlsDocumentos.addAll(documentosAntigos);
        }

        List<Uri> todasUris = new ArrayList<>();



        for (Uri uri : images) {
            if ("content".equals(uri.getScheme())) {
                todasUris.add(uri);
            }
        }
        for (Uri uri : documents) {
            if ("content".equals(uri.getScheme())) {
                todasUris.add(uri);
            }
        }


        Map<Uri, String> caminhoPorUri = new HashMap<>();
        for (Uri uri : images) {
            if ("content".equals(uri.getScheme())) {
                String caminho = pastaBase + "/imagens/" + getFileNameFromUri(uri);
                caminhoPorUri.put(uri, caminho);
            }
        }
        for (Uri uri : documents) {
            if ("content".equals(uri.getScheme())) {
                String caminho = pastaBase + "/documentos/" + getFileNameFromUri(uri);
                caminhoPorUri.put(uri, caminho);
            }
        }

        final int totalUploads = todasUris.size();
        final int[] uploadsFinalizados = {0};

        Log.d("DEBUG", "docId: " + totalUploads);

        for (Uri uri : todasUris) {
            String caminho = caminhoPorUri.get(uri);
            storage.getReference().child(caminho).putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storage.getReference().child(caminho).getDownloadUrl()
                                .addOnSuccessListener(uriDownload -> {
                                    Log.d("caminhos",caminho);
                                    if (caminho.contains("imagens")) {
                                        urlsImagens.add(uriDownload.toString());
                                    } else {
                                        urlsDocumentos.add(uriDownload.toString());
                                    }

                                    uploadsFinalizados[0]++;
                                    if (uploadsFinalizados[0] == totalUploads) {
                                        Log.d("UPLOAD", "Upload sucesso: " + caminho);
                                        SalvarNoFirestore(pastaBase, nome, email, telefone, dadosAnimal, tipoAnimal, dataAdocao, urlsImagens, urlsDocumentos, dataCriacao, nomeAnimal, sexoAnimal, idadeAnimal, localizacaoAdotante);
                                    }
                                }).addOnFailureListener(e -> Log.d("download_error", "Erro ao subir " + caminho + ": " + e.getMessage()));;
                    })
                    .addOnFailureListener(e -> Log.d("upload_error", "Erro ao subir " + caminho + ": " + e.getMessage()));
        }

        if (todasUris.isEmpty()) {
            SalvarNoFirestore(pastaBase, nome, email, telefone, dadosAnimal, tipoAnimal, dataAdocao, urlsImagens, urlsDocumentos, dataCriacao, nomeAnimal, sexoAnimal, idadeAnimal, localizacaoAdotante);
        }
    }

    private void SalvarNoFirestore(String docId, String nome, String email, String telefone,
                                   String dadosAnimal, String tipoAnimal, String dataAdocao,
                                   List<String> imagensUrls, List<String> documentosUrls, String dataCriacao,
                                   String nomeAnimal, String sexoAnimal, String idadeAnimal, String localizacaoAdotante) {

        Map<String, Object> dados = new HashMap<>();
        dados.put("nome_adotante", nome);
        dados.put("email_adotante", email);
        dados.put("telefone_adotante", telefone);
        dados.put("dados_animal", dadosAnimal);
        dados.put("tipo_animal", tipoAnimal);
        dados.put("data_adocao", dataAdocao);
        dados.put("imagens", imagensUrls);
        dados.put("documentos", documentosUrls);
        dados.put("data_criacao", dataCriacao);
        dados.put("nome_animal", nomeAnimal);
        dados.put("sexo_animal", sexoAnimal);
        dados.put("idade_animal", idadeAnimal);
        dados.put("localizacao_adotante", localizacaoAdotante);

        ArrayList<String> pesquisa = new ArrayList<>();
        pesquisa.addAll(SepararString(nomeAnimal));
        pesquisa.addAll(SepararString(localizacaoAdotante));
        pesquisa.add(sexoAnimal);
        pesquisa.add(idadeAnimal);
        pesquisa.add(email);
        pesquisa.addAll(SepararString(telefone));
        pesquisa.addAll(SepararString(dadosAnimal));
        pesquisa.addAll(SepararString(nome));
        pesquisa.add(tipoAnimal);

        dados.put("pesquisa", pesquisa);


        db.collection("animaisAdotados").document(docId)
                .update(dados)
                .addOnSuccessListener(createunused -> {Log.d("db", "Dados atualizados com sucesso!");

                    Snackbar snackbar = Snackbar.make(binding.getRoot(), "Cadastro atualizado com sucesso!", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(Color.GREEN);
                    snackbar.setTextColor(Color.WHITE);
                    snackbar.show();


                    button_save.setEnabled(true);
                    button_save.setText("Editar");


                })
                .addOnFailureListener(e ->{ Log.d("db_error", "Erro ao salvar dados: " + e.getMessage());

                    button_save.setEnabled(true);
                    button_save.setText("Editar");});
    }

    private ArrayList<String> SepararString(String texto){
        texto = texto.replace(",", "").toLowerCase();
        ArrayList<String> textoSeparado = new ArrayList<>(Arrays.asList(texto.split(" ")));
        return textoSeparado;
    }
}