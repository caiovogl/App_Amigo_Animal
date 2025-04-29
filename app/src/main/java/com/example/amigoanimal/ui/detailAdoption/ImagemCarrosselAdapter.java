package com.example.amigoanimal.ui.detailAdoption;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.amigoanimal.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

public class ImagemCarrosselAdapter extends RecyclerView.Adapter<ImagemCarrosselAdapter.ViewHolder> {

    private List<String> imagens;
    private Context context;

    public ImagemCarrosselAdapter(Context context, List<String> imagens) {
        this.imagens = imagens;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_carrossel_imagem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PhotoView photoView = holder.itemView.findViewById(R.id.imageViewCarrossel);

        Glide.with(context)
                .load(imagens.get(position))
                .into(photoView);
    }

    @Override
    public int getItemCount() {
        return imagens.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewCarrossel);
        }
    }
}
