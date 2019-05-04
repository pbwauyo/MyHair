package comro.example.nssf.martin.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
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
import comro.example.nssf.martin.dataModels.Booking;
import de.hdodenhof.circleimageview.CircleImageView;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.ViewHolder> {
    private ArrayList<Booking> bookings;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public BookingsAdapter(ArrayList<Booking> bookings, Context context, OnItemClickListener onItemClickListener){
        this.bookings = bookings;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.bookings_row, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.nameTxt.setText(bookings.get(i).getCustomerName());
        viewHolder.styleNameTxt.setText(bookings.get(i).getStyleName());

        String imageUrl = bookings.get(i).getStyleImage();
        if(!imageUrl.equals("")){
            Picasso.get()
                    .load(imageUrl)
                    .centerCrop()
                    .fit()
                    .into(viewHolder.imageView);
        }

        if(bookings.get(i).getStatus().equals("accepted")){
            viewHolder.acceptBtn.setText(context.getString(R.string.chat));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                viewHolder.acceptBtn.setBackground(context.getResources().getDrawable(R.drawable.chat_button));
            }

            viewHolder.declineBtn.setText(context.getString(R.string.call));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                viewHolder.declineBtn.setBackground(context.getResources().getDrawable(R.drawable.call_button));
            }
        }
    }

    @Override
    public int getItemCount() {
        if(bookings.size()!=0){
            return bookings.size();
        }
        else {
            return 0;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView nameTxt, styleNameTxt;
        private CircleImageView imageView;
        private Button acceptBtn, declineBtn;


        ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTxt = itemView.findViewById(R.id.name);
            styleNameTxt = itemView.findViewById(R.id.style_name);
            imageView = itemView.findViewById(R.id.profile_pic);
            acceptBtn = itemView.findViewById(R.id.accept_btn);
            declineBtn = itemView.findViewById(R.id.decline_btn);

            acceptBtn.setOnClickListener(this);
            declineBtn.setOnClickListener(this);
        }

        @SuppressLint("NewApi")
        @Override
        public void onClick(View v) {
            Button btn = (Button) v;
            int id = v.getId();
            switch (id){
                case R.id.accept_btn:
                    if(btn.getText().equals("ACCEPT")){
                        btn.setText(context.getString(R.string.chat));
                        btn.setBackground(context.getResources().getDrawable(R.drawable.chat_button));
                        declineBtn.setText(context.getString(R.string.call));
                        declineBtn.setBackground(context.getResources().getDrawable(R.drawable.call_button));
                        onItemClickListener.onAcceptButtonClick(v, getAdapterPosition());
                    }
                    else if (btn.getText().equals("CHAT")){
                        onItemClickListener.onChatButtonClick(v, getAdapterPosition());
                    }
                    break;
                case R.id.decline_btn:
                    if(btn.getText().equals("DECLINE")){
                        onItemClickListener.onDeclineButtonClick(v, getAdapterPosition());
                    }
                    else if (btn.getText().equals("CALL")){
                        onItemClickListener.onCallButtonClick(v, getAdapterPosition());
                    }
                    break;
            }
        }
    }

    public interface OnItemClickListener {
        void onAcceptButtonClick(View view, int position);
        void onDeclineButtonClick(View view, int position);
        void onChatButtonClick(View view, int position);
        void onCallButtonClick(View view, int position);
    }
}
