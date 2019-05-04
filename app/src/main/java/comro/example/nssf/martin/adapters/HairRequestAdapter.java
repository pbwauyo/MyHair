package comro.example.nssf.martin.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import comro.example.nssf.martin.R;
import comro.example.nssf.martin.dataModels.HairRequest;
import de.hdodenhof.circleimageview.CircleImageView;

public class HairRequestAdapter extends RecyclerView.Adapter <HairRequestAdapter.ViewHolder> {
    private ArrayList <HairRequest> hairRequests;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public HairRequestAdapter(ArrayList <HairRequest> hairRequests, Context context, OnItemClickListener onItemClickListener){
        this.hairRequests = hairRequests;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.requests_row, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.stylistName.setText(hairRequests.get(i).getStylistName());
        viewHolder.styleName.setText(hairRequests.get(i).getStyleName());

        if(!hairRequests.get(i).getStyleImageUrl().equals("")){
            Picasso.get()
                    .load(hairRequests.get(i).getStyleImageUrl())
                    .centerCrop()
                    .fit()
                    .into(viewHolder.imageView);
        }

        if (hairRequests.get(i).getStatus().equals("accepted")){
            viewHolder.declinedBtn.setText(context.getString(R.string.chat));
            viewHolder.declinedBtn.setBackground(context.getResources().getDrawable(R.drawable.chat_button));

            viewHolder.deleteBtn.setText(context.getString(R.string.call));
            viewHolder.deleteBtn.setBackground(context.getResources().getDrawable(R.drawable.call_button));
        }
    }

    @Override
    public int getItemCount() {
        if(hairRequests.size()!=0){
            return hairRequests.size();
        }
        else {
            return 0;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private CircleImageView imageView;
        private TextView stylistName, styleName;
        private Button declinedBtn, deleteBtn;

        ViewHolder(View itemView){
            super(itemView);
            imageView = itemView.findViewById(R.id.style_image);
            stylistName = itemView.findViewById(R.id.stylist_name);
            styleName = itemView.findViewById(R.id.style_name);
            declinedBtn = itemView.findViewById(R.id.declined_btn);
            deleteBtn = itemView.findViewById(R.id.delete_btn);

            declinedBtn.setOnClickListener(this);
            deleteBtn.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Button button = (Button) v;
            int id = v.getId();
            switch (id){
                case R.id.delete_btn:
                    if(button.getText().equals(context.getString(R.string.call))){
                        onItemClickListener.onCallButtonClick(v, getAdapterPosition());
                    }
                    else if (button.getText().equals("DELETE")){
                        onItemClickListener.onDeleteButtonClick(v, getAdapterPosition());
                    }
                    break;
                case R.id.declined_btn:
                    if(button.getText().equals("CHAT")){
                        onItemClickListener.onChatButtonClick(v, getAdapterPosition());
                    }
                    break;
            }
        }
    }

    public interface OnItemClickListener{
        void onChatButtonClick(View view, int position);
        void onDeleteButtonClick(View view, int position);
        void onCallButtonClick(View view, int position);
    }
}
