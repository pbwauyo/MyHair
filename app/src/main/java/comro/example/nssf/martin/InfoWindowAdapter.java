package comro.example.nssf.martin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Picasso;

import java.util.zip.Inflater;

public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private Context context;
    private String stylistName, salonName, styleImage;

    public InfoWindowAdapter(Context context, String stylistName, String salonName, String styleImage) {
        this.context = context;
        this.stylistName = stylistName;
        this.salonName = salonName;
        this.styleImage = styleImage;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        LayoutInflater inflater = (LayoutInflater) (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        View view = inflater.inflate(R.layout.custom_info_window,null);

        TextView stylistNameTxt= view.findViewById(R.id.stylist_name_info_box);
        TextView salonNameTxt = view.findViewById(R.id.salon_name_info_box);
        ImageView imageView = view.findViewById(R.id.info_box_image);

        if(!styleImage.equals("")) {
            Picasso.get()
                    .load(styleImage)
                    .fit()
                    .centerCrop()
                    .rotate(90)
                    .into(imageView);
        }

        stylistNameTxt.setText(stylistName);
        salonNameTxt.setText(salonName);

        return view;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
