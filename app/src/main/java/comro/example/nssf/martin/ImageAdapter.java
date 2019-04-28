package comro.example.nssf.martin;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageAdapter extends PagerAdapter {
    private ArrayList<Integer> images;
    private LayoutInflater inflater;
    private Context context;

    public ImageAdapter (Context context, ArrayList<Integer> images) {
        this.context = context;
        this.images = images;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View myImageLayout = inflater.inflate(R.layout.image_slider_layout, view, false);
        ImageView myImage = myImageLayout.findViewById(R.id.image);
        //myImage.setImageResource(images.get(position));
        Picasso.get()
                .load(images.get(position))
                .fit()
                .centerCrop()
                .into(myImage);
        view.addView(myImageLayout);
        return myImageLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
}
