package comro.example.nssf.martin.stylist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import comro.example.nssf.martin.R;

public class Stylist extends AppCompatActivity implements View.OnClickListener{
    Button registerStyleTxt, editStyleTxt, viewBookngsTxt, viewHistoryTxt, cancelBookingTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stylist);
        registerStyleTxt = findViewById(R.id.registerStyle);
        editStyleTxt = findViewById(R.id.editStyle);
        viewBookngsTxt = findViewById(R.id.viewBookings);
        viewHistoryTxt = findViewById(R.id.viewHistory);
        cancelBookingTxt = findViewById(R.id.cancelBookings);

        registerStyleTxt.setOnClickListener(this);


//        registerStyleTxt
    }

    @Override
    public void onClick(View view){
       switch (view.getId()){
           case R.id.registerStyle:
               startActivity(new Intent(Stylist.this, StyleDetails.class));
               break;
           case R.id.editStyle:
               break;
           case R.id.viewBookings:
               break;
           case R.id.viewHistory:
               break;
           case R.id.cancelBookings:
               break;
           default:
               break;

        }
    }
}
