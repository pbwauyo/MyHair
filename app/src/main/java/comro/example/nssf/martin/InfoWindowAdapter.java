package comro.example.nssf.martin;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.zip.Inflater;

import de.hdodenhof.circleimageview.CircleImageView;

public class InfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
    private Context context;
    private String stylistName, salonName, styleImage;
    private CircleImageView imageView;
    private ProgressBar progressBar;

    public InfoWindowAdapter(Context context, String stylistName, String salonName, String styleImage) {
        this.context = context;
        this.stylistName = stylistName;
        this.salonName = salonName;
        this.styleImage = styleImage;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        LayoutInflater inflater = (LayoutInflater) (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        View view = inflater.inflate(R.layout.custom_info_window,null);

        TextView stylistNameTxt= view.findViewById(R.id.stylist_name_info_box);
        TextView salonNameTxt = view.findViewById(R.id.salon_name_info_box);
        imageView = view.findViewById(R.id.info_box_image);
        progressBar = view.findViewById(R.id.arrowProgressBar);

        Picasso.get()
                .load(styleImage)
                .fit()
                .centerCrop()
                //.rotate(90)
                .into(imageView, new MarkerCallback(marker));

        stylistNameTxt.setText(stylistName);
        salonNameTxt.setText(salonName);

        return view;
    }

    static class MarkerCallback implements Callback{
        Marker marker = null;

        MarkerCallback(Marker marker){
            this.marker = marker;
        }

        @Override
        public void onSuccess() {
            if(marker != null && marker.isInfoWindowShown()){
                marker.hideInfoWindow();
                marker.showInfoWindow();
            }
        }

        @Override
        public void onError(Exception e) {
            Log.d("Error",  "failed to load image into info window: " + e.getMessage());
        }
    }
}
