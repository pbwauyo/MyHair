package comro.example.nssf.martin.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import comro.example.nssf.martin.R;
import comro.example.nssf.martin.dataModels.Style;
import de.hdodenhof.circleimageview.CircleImageView;

public class StylesAdapter extends RecyclerView.Adapter<StylesAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Style> arrayList;
    private OnItemClickListener onItemClickListener;

    public StylesAdapter(Context context, ArrayList<Style> arrayList, OnItemClickListener onItemClickListener){
        this.context = context;
        this.arrayList = arrayList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.style_row, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        //viewHolder.styleImageView.setVisibility(View.GONE);
       // viewHolder.progressBar.setVisibility(View.VISIBLE);
        viewHolder.styleNameTxt.setText(arrayList.get(i).getName());
        viewHolder.styleCostTxt.setText(arrayList.get(i).getCost());

        Picasso.get()
                .load(arrayList.get(i).getimage())
                .fit()
                .centerCrop()
                //.rotate(90)
                .into(viewHolder.styleImageView);

    }

    @Override
    public int getItemCount() {
        if(arrayList.size() != 0){
            return arrayList.size();
        }
        return 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView styleNameTxt, styleCostTxt;
        private CircleImageView styleImageView;
        private Button requestBtn;
        private ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            styleNameTxt = itemView.findViewById(R.id.style_name);
            styleCostTxt = itemView.findViewById(R.id.style_cost);
            styleImageView = itemView.findViewById(R.id.style_image_url);
            requestBtn = itemView.findViewById(R.id.request_btn);
            progressBar = itemView.findViewById(R.id.arrowProgressBar);

            requestBtn.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onRequestButtonClick(v, getAdapterPosition());
        }
    }

    public interface OnItemClickListener {
        void onRequestButtonClick(View view, int position);
    }

}
