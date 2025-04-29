package com.example.amigoanimal.ui.create;

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


import com.example.amigoanimal.R;
import com.example.amigoanimal.databinding.FragmentCreateBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CreateFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri uri = null;
    private List<Uri> images,documents;
    private String usuarioID, nomeProfissional;
    private ActivityResultLauncher<String> pickImage, pickDocument;
    private Switch animalType, sexoAnimalSwitch;
    private RadioGroup radioGroupIdade;
    private EditText editName, editEmail, editPhone, editAnimalData, editAdoptionDate, editNomeAnimal, LocalizacaoAdotante;
    private Button button_addImage, button_addDocuments, button_save;
    private LinearLayout create_images, create_documents;

    private FragmentCreateBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CreateViewModel createViewModel =
                new ViewModelProvider(this).get(CreateViewModel.class);

        binding = FragmentCreateBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        images = new ArrayList<>();
        documents = new ArrayList<>();


        IniciarComponentes();

        IniciarSeletorData();

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

    private void IniciarComponentes(){
        editName = binding.editName;
        editEmail = binding.editEmail;
        editPhone = binding.editPhone;
        editAnimalData = binding.editAnimalData;
        editAdoptionDate = binding.editAdoptionDate;
        button_addImage = binding.buttonAddImage;
        button_addDocuments = binding.buttonAddDocuments;
        button_save = binding.buttonSave;
        create_images = binding.createImages;
        create_documents = binding.createDocuments;
        animalType = binding.animalType;
        sexoAnimalSwitch = binding.sexoAnimal;
        radioGroupIdade = binding.radioGroupIdade;
        LocalizacaoAdotante = binding.editLocalizaoAdotante;
        editNomeAnimal = binding.editNomeAnimal;

        usuarioID = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        db.collection("Usuarios").document(usuarioID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                nomeProfissional = documentSnapshot.getString("nome");
            }
        });
    }

    private void IniciarSeletorData(){
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String currentDateTime = format.format(Calendar.getInstance().getTime());
        editAdoptionDate.setText(currentDateTime);

        editAdoptionDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view, year, month, dayOfMonth) -> {
                        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                                (timeView, hourOfDay, minute) -> {

                                    Calendar selectedDateTime = Calendar.getInstance();
                                    selectedDateTime.set(year, month, dayOfMonth, hourOfDay, minute);

                                    String formatted = format.format(selectedDateTime.getTime());
                                    editAdoptionDate.setText(formatted);

                                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

                        timePickerDialog.show();

                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

            datePickerDialog.show();
        });
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

        return result;
    }

    private void ExcluirView(LinearLayout base,View view){
        base.removeView(view);
        base.invalidate();
        base.requestLayout();
    }

    private void SalvarImagem(Uri uriNovo){
        if(uriNovo != null) {
            View imgAdd = getLayoutInflater().inflate(R.layout.item_image, create_images, false);
            ImageView imageView = imgAdd.findViewById(R.id.imageView);
            imageView.setImageURI(uriNovo);
            create_images.addView(imgAdd);
            images.add(uriNovo);
            imgAdd.findViewById(R.id.image_close).setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   ExcluirView(create_images,imgAdd);
                   images.remove(uriNovo);
               }
            });
            create_images.invalidate();
            create_images.requestLayout();
        }else{
            Snackbar snackbar = Snackbar.make(binding.getRoot(), "Erro ao carregar a imagem", Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(Color.WHITE);
            snackbar.setTextColor(Color.BLACK);
            snackbar.show();
        }
    }

    private void SalvarDocumentos(Uri uriNovo){
        if(uriNovo != null) {
            View documentAdd = getLayoutInflater().inflate(R.layout.item_document, create_documents, false);
            TextView nomeDoc = documentAdd.findViewById(R.id.nomeDocumento);
            nomeDoc.setText(getFileNameFromUri(uriNovo));
            create_documents.addView(documentAdd);
            documents.add(uriNovo);
            documentAdd.findViewById(R.id.image_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExcluirView(create_documents,documentAdd);
                    documents.remove(uriNovo);
                }
            });
            create_documents.invalidate();
            create_documents.requestLayout();
        }else{
            Snackbar snackbar = Snackbar.make(binding.getRoot(), "Erro ao carregar o documento", Snackbar.LENGTH_SHORT);
            snackbar.setBackgroundTint(Color.WHITE);
            snackbar.setTextColor(Color.BLACK);
            snackbar.show();
        }
    }

    private void Salvar() {
        button_save.setEnabled(false);
        button_save.setText("Salvando...");

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

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss", Locale.getDefault());
        String dataFormatada = dateFormat.format(Calendar.getInstance().getTime());

        SalvarArquivosComUrls(dataFormatada, nomeAdotante, emailAdotante, telefoneAdotante, dadosAnimal, tipoAnimal, dataAdocao, dataFormatada, nomeAnimal, sexoAnimal, idadeAnimal, localizacaoAdotante);
    }

    private void SalvarArquivosComUrls(String pastaBase, String nome, String email, String telefone,
                                       String dadosAnimal, String tipoAnimal, String dataAdocao, String dataCriacao,
                                        String nomeAnimal, String sexoAnimal, String idadeAnimal, String localizacaoAdotante) {

        List<String> urlsImagens = new ArrayList<>();
        List<String> urlsDocumentos = new ArrayList<>();

        List<Uri> todasUris = new ArrayList<>();
        todasUris.addAll(images);
        todasUris.addAll(documents);

        Map<Uri, String> caminhoPorUri = new HashMap<>();
        for (Uri uri : images) {
            String caminho = pastaBase + "/imagens/" + getFileNameFromUri(uri);
            caminhoPorUri.put(uri, caminho);
        }
        for (Uri uri : documents) {
            String caminho = pastaBase + "/documentos/" + getFileNameFromUri(uri);
            caminhoPorUri.put(uri, caminho);
        }

        // Contador para saber quando todos os uploads acabaram
        final int totalUploads = todasUris.size();
        final int[] uploadsFinalizados = {0};

        for (Uri uri : todasUris) {
            String caminho = caminhoPorUri.get(uri);
            storage.getReference().child(caminho).putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storage.getReference().child(caminho).getDownloadUrl()
                                .addOnSuccessListener(uriDownload -> {
                                    if (caminho.contains("imagens")) {
                                        urlsImagens.add(uriDownload.toString());
                                    } else {
                                        urlsDocumentos.add(uriDownload.toString());
                                    }

                                    uploadsFinalizados[0]++;
                                    if (uploadsFinalizados[0] == totalUploads) {
                                        // Todos os uploads terminaram, agora salva no Firestore
                                        SalvarNoFirestore(pastaBase, nome, email, telefone, dadosAnimal, tipoAnimal, dataAdocao, urlsImagens, urlsDocumentos, dataCriacao, nomeAnimal, sexoAnimal, idadeAnimal, localizacaoAdotante);
                                    }
                                });
                    })
                    .addOnFailureListener(e -> Log.d("upload_error", "Erro ao subir " + caminho + ": " + e.getMessage()));
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
        dados.put("profissional", nomeProfissional);

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
                .set(dados)
                .addOnSuccessListener(createunused -> {Log.d("db", "Dados salvos com sucesso!");

                    Snackbar snackbar = Snackbar.make(binding.getRoot(), "Cadastro realizado com sucesso!", Snackbar.LENGTH_LONG);
                    snackbar.setBackgroundTint(Color.GREEN);
                    snackbar.setTextColor(Color.WHITE);
                    snackbar.show();


                    editName.setText("");
                    editEmail.setText("");
                    editPhone.setText("");
                    editAnimalData.setText("");
                    LocalizacaoAdotante.setText("");
                    editNomeAnimal.setText("");

                    SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    editAdoptionDate.setText(format.format(Calendar.getInstance().getTime()));

                    animalType.setChecked(false);


                    create_images.removeAllViews();
                    create_documents.removeAllViews();
                    images.clear();
                    documents.clear();


                    button_save.setEnabled(true);
                    button_save.setText("Cadastrar");
                })
                .addOnFailureListener(e ->{ Log.d("db_error", "Erro ao salvar dados: " + e.getMessage());

                    button_save.setEnabled(true);
                    button_save.setText("Cadastrar");});
    }

    private ArrayList<String> SepararString(String texto){
        texto = texto.replace(",", "").toLowerCase();
        ArrayList<String> textoSeparado = new ArrayList<>(Arrays.asList(texto.split(" ")));
        return textoSeparado;
    }
}