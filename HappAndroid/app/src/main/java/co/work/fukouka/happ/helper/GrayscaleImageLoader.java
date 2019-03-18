package co.work.fukouka.happ.helper;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.esafirm.imagepicker.features.imageloader.ImageLoader;
import com.esafirm.imagepicker.features.imageloader.ImageType;

public class GrayscaleImageLoader implements ImageLoader{

    @Override
    public void loadImage(String s, ImageView imageView, ImageType imageType) {
        Glide.with(imageView.getContext()).load(s).into(imageView);
    }
}
