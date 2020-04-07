package com.labs.myapplication;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.List;

import static com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView.ORIENTATION_USE_EXIF;

public class ViewPagerAdapter extends PagerAdapter {
    private Activity activity;
    private List<String> uriImages;
    private LayoutInflater inflater;

    ViewPagerAdapter(Activity activity, List<String> images) {
        this.activity = activity;
        this.uriImages = images;
    }

    @Override
    public int getCount() {
        return uriImages.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        inflater = (LayoutInflater) activity.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View itemView = inflater.inflate(R.layout.viewpager_item, container,false);

        SubsamplingScaleImageView imageView = itemView.findViewById(R.id.imageView);

        DisplayMetrics dis = new DisplayMetrics();

        activity.getWindowManager().getDefaultDisplay().getMetrics(dis);

        imageView.setMinimumHeight(dis.heightPixels);
        imageView.setMinimumWidth(dis.widthPixels);
        imageView.setOrientation(ORIENTATION_USE_EXIF);
        imageView.setImage(ImageSource.uri(uriImages.get(position)));

        if(imageView.getParent() != null) {
            ((ViewGroup)imageView.getParent()).removeView(imageView);
        }

        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }
}
