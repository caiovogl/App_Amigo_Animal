package com.example.amigoanimal.ui.detailAdoption;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.amigoanimal.R;

import java.util.List;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    private Context context;
    private List<String> documentos;

    public DocumentAdapter(Context context, List<String> documentos) {
        this.context = context;
        this.documentos = documentos;
    }

    @NonNull
    @Override
    public DocumentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_detalhe_documento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentAdapter.ViewHolder holder, int position) {
        String url = documentos.get(position);
        String nomeDocumento = Uri.parse(url).getLastPathSegment();
        if (nomeDocumento != null && nomeDocumento.contains("/")) {
            nomeDocumento = nomeDocumento.substring(nomeDocumento.lastIndexOf("/") + 1);
        }
        holder.textDocumentoNome.setText(nomeDocumento);

        holder.iconDownload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return documentos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textDocumentoNome;
        ImageView iconDownload;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDocumentoNome = itemView.findViewById(R.id.textDocumentoNome);
            iconDownload = itemView.findViewById(R.id.iconDownload);
        }
    }
}

