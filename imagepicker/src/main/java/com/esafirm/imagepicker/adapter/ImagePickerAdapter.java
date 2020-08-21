package com.esafirm.imagepicker.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.esafirm.imagepicker.R;
import com.esafirm.imagepicker.features.imageloader.ImageLoader;
import com.esafirm.imagepicker.features.imageloader.ImageType;
import com.esafirm.imagepicker.helper.ImagePickerUtils;
import com.esafirm.imagepicker.listeners.OnImageClickListener;
import com.esafirm.imagepicker.listeners.OnImageSelectedListener;
import com.esafirm.imagepicker.model.Image;

import java.util.ArrayList;
import java.util.List;

public class ImagePickerAdapter extends BaseListAdapter<ImagePickerAdapter.ImageViewHolder> {

    private List<Image> images = new ArrayList<>();
    private List<Image> selectedImages = new ArrayList<>();

    private OnImageClickListener itemClickListener;
    private OnImageSelectedListener imageSelectedListener;

    public ImagePickerAdapter(Context context, ImageLoader imageLoader,
                              List<Image> selectedImages, OnImageClickListener itemClickListener) {
        super(context, imageLoader);
        this.itemClickListener = itemClickListener;

        if (selectedImages != null && !selectedImages.isEmpty()) {
            this.selectedImages.addAll(selectedImages);
        }
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageViewHolder(
                getInflater().inflate(R.layout.ef_imagepicker_item_image, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(ImageViewHolder viewHolder, int position) {

        final Image image = images.get(position);
        final boolean isSelected = isSelected(image);

        getImageLoader().loadImage(
                image,
                viewHolder.imageView,
                ImageType.GALLERY
        );

        boolean showFileTypeIndicator = false;
        String fileTypeLabel = "";
        if (ImagePickerUtils.isGifFormat(image)) {
            fileTypeLabel = getContext().getResources().getString(R.string.ef_gif);
            showFileTypeIndicator = true;
        }
        if (ImagePickerUtils.isVideoFormat(image)) {
            fileTypeLabel = getContext().getResources().getString(R.string.ef_video);
            showFileTypeIndicator = true;
        }
        viewHolder.fileTypeIndicator.setText(fileTypeLabel);
        viewHolder.fileTypeIndicator.setVisibility(showFileTypeIndicator
                ? View.VISIBLE
                : View.GONE);

        viewHolder.alphaView.setAlpha(isSelected ? 0.5f : 0f);

        viewHolder.itemView.setOnClickListener(v -> {
            boolean shouldSelect = itemClickListener.onImageClick(
                    isSelected
            );

            if (isSelected) {
                removeSelectedImage(image);
            } else if (shouldSelect) {
                addSelected(image);
            }
        });

        viewHolder.container.setForeground(isSelected
                ? ContextCompat.getDrawable(getContext(), R.drawable.ef_ic_done_white)
                : null);
    }

    private boolean isSelected(Image image) {
        for (Image selectedImage : selectedImages) {
            if (selectedImage.getPath().equals(image.getPath())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return images.size();
    }


    public void setData(List<Image> images) {
        this.images.clear();
        this.images.addAll(images);
    }

    public void addSelected(final Image image) {
        mutateSelection(() -> {
            selectedImages.add(image);
            int changedPosition = -1;
            for(int i = 0; i < images.size(); i++){
                Image temp = images.get(i);
                if(temp.getPath().equals(image.getPath())){
                    changedPosition = i;
                }
            }

            if(changedPosition >= 0) {
                notifyItemChanged(changedPosition);
            }
        });
    }

    public void removeSelectedImage(final Image image) {
        mutateSelection(() -> {
            selectedImages.remove(image);
            int changedPosition = -1;
            for(int i = 0; i < images.size(); i++){
                Image temp = images.get(i);
                if(temp.getPath().equals(image.getPath())){
                    changedPosition = i;
                }
            }

            if(changedPosition >= 0) {
                notifyItemChanged(changedPosition);
            }
        });
    }

    public void removeAllSelectedSingleClick() {
        mutateSelection(() -> {
            selectedImages.clear();
            notifyDataSetChanged();
        });
    }

    private void mutateSelection(Runnable runnable) {
        runnable.run();
        if (imageSelectedListener != null) {
            imageSelectedListener.onSelectionUpdate(selectedImages);
        }
    }

    public void setImageSelectedListener(OnImageSelectedListener imageSelectedListener) {
        this.imageSelectedListener = imageSelectedListener;
    }

    public Image getItem(int position) {
        return images.get(position);
    }

    public List<Image> getSelectedImages() {
        return selectedImages;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private View alphaView;
        private TextView fileTypeIndicator;
        private FrameLayout container;

        ImageViewHolder(View itemView) {
            super(itemView);

            container = (FrameLayout) itemView;
            imageView = itemView.findViewById(R.id.image_view);
            alphaView = itemView.findViewById(R.id.view_alpha);
            fileTypeIndicator = itemView.findViewById(R.id.ef_item_file_type_indicator);
        }
    }

}
