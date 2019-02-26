package comro.example.nssf.martin;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import comro.example.nssf.martin.dataModels.Style;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    Context context;
    ArrayList<Style> styles;
    StorageReference storageReference;

    public MyAdapter(Context c , ArrayList<Style> p, StorageReference storageReference)
    {
        context = c;
        styles = p;
        this.storageReference = storageReference;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.recycler_view_row,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

        String stylistId = styles.get(position).getId();
        String styleName = styles.get(position).getName();
        String fileExtension = styles.get(position).getSalonId();

        holder.name.setText(styleName);
        holder.gender.setText(styles.get(position).getGender());
        holder.cost.setText(styles.get(position).getCost());

        StorageReference storageReference = firebaseStorage.getReferenceFromUrl("gs://prototype-5625e.appspot.com/").child(stylistId).child("style_images").child(styleName.concat("." + fileExtension));




//        GlideApp.with(context)
//                .load(storageReference.child(styles.get(position).getName()))
//                .into(holder.image);

//        Log.d("image url2", storageReference.child(styles.get(position).getName()).getDownloadUrl().toString());

        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(holder.image);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "image download failed", Toast.LENGTH_LONG).show();
            }
        });

                //Log.d("image url", styles.get(position).getimage());

    }

    @Override
    public int getItemCount() {
        return styles.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView name, gender, cost;
        ImageView image;


        public MyViewHolder(View itemView) {
            super(itemView);
            name =  itemView.findViewById(R.id.name);
            gender = itemView.findViewById(R.id.gender);
            cost = itemView.findViewById(R.id.cost);
            image = itemView.findViewById(R.id.style_image);

//            btn = (Button) itemView.findViewById(R.id.checkDetails);
        }
//        public void onClick(final int position)
//        {
//            btn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Toast.makeText(context, position+" is clicked", Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
    }
}

