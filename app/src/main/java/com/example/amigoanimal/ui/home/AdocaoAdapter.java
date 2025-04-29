package com.example.amigoanimal.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.amigoanimal.Adocao;
import com.example.amigoanimal.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

public class AdocaoAdapter extends RecyclerView.Adapter<AdocaoAdapter.ViewHolder> {

    private Context context;
    private List<Adocao> listaAdocoes;

    public AdocaoAdapter(Context context, List<Adocao> listaAdocoes) {
        this.context = context;
        this.listaAdocoes = listaAdocoes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_adocao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Adocao adocao = listaAdocoes.get(position);

        if (adocao.getImagens() != null && !adocao.getImagens().isEmpty()) {
            String urlImagem = adocao.getImagens().get(0);

            if (urlImagem != null && !urlImagem.trim().isEmpty()) {
                Glide.with(context)
                        .load(urlImagem)
                        .placeholder(R.drawable.img_logo) // opcional: imagem temporária
                        .into(holder.imageView);
            }
        }

        String nomeAdotante = "Não Adotado";
        if (adocao.getNomeAdotante() != null && !adocao.getNomeAdotante().trim().isEmpty()) {
            nomeAdotante = "Adotante: " + adocao.getNomeAdotante();
        }

        holder.textViewItemAdocao.setText(nomeAdotante + "\n" + adocao.getDataAdocao());


        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("nome_adotante", adocao.getNomeAdotante());
            bundle.putString("email_adotante", adocao.getEmailAdotante());
            bundle.putString("telefone_adotante", adocao.getTelefoneAdotante());
            bundle.putString("dados_animal", adocao.getDadosAnimal());
            bundle.putString("tipo_animal", adocao.getTipoAnimal());
            bundle.putString("data_adocao", adocao.getDataAdocao());
            bundle.putString("data_criacao", adocao.getDataCriacao());
            bundle.putString("profissional", adocao.getProfissional());
            bundle.putString("sexo_animal", adocao.getSexoAnimal());
            bundle.putString("nome_animal", adocao.getNomeAnimal());
            bundle.putString("idade_animal", adocao.getIdadeAnimal());
            bundle.putString("localizacao_adotante", adocao.getLocalizacaoAdotante());
            bundle.putStringArrayList("imagens", new ArrayList<>(adocao.getImagens())); // ou imageUrl se for só uma
            bundle.putStringArrayList("documentos", new ArrayList<>(adocao.getDocumentos()));

            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_nav_home_to_detailFragment, bundle);
        });
    }

    @Override
    public int getItemCount() {
        return listaAdocoes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewItemAdocao;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewItemAdocao);
            textViewItemAdocao = itemView.findViewById(R.id.textViewItemAdocao);
        }
    }
}
