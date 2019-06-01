package comro.example.nssf.martin;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import comro.example.nssf.martin.dataModels.Style;

public class UtilityFunctions {
    public static void uploadImage(final StorageReference storageRef, Uri uri, final CoordinatorLayout layout, final ProgressBar progressBar, final DatabaseReference databaseRef, final String userId) {

        if (uri != null) {
            progressBar.setVisibility(View.VISIBLE);
            UploadTask uploadTask = storageRef.putFile(uri);
            Snackbar snackbar = Snackbar.make(layout, "Saving image", Snackbar.LENGTH_LONG);
            TextView textView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            snackbar.show();

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful()){
                        Snackbar.make(layout, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                        throw task.getException();
                    }

                    return storageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    progressBar.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                        Snackbar bar =  Snackbar.make(layout, "Image saved successfully", Snackbar.LENGTH_INDEFINITE);
                        TextView tv = bar.getView().findViewById(android.support.design.R.id.snackbar_text);
                        tv.setTextColor(Color.WHITE);
                        bar.show();

                        String uri = task.getResult().toString();
                        databaseRef.child(userId).child("imageUrl").setValue(uri);

                        Log.d("uriToString", uri);
                        Log.d("uripath", task.getResult().getPath());
                    }
                    else{
                        Snackbar snackbar = Snackbar.make(layout, "Unable to save image: "+ task.getException().getMessage(), Snackbar.LENGTH_LONG);
                        TextView textView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(Color.WHITE);
                        snackbar.show();
                    }
                }
            });
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        progressBar.setProgress((int)progress);
                    }
            });
        }
    }

    public static String getEmojiByUnicode(int unicode){
        return new String(Character.toChars(unicode));
    }

    public static void saveArrayList (ArrayList<Style> arrayList, String key, SharedPreferences.Editor editor){
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString(key, json);
        editor.commit();
    }

    public static ArrayList<Style> getArrayList(String key, SharedPreferences sharedPreferences){
        Gson gson = new Gson();
        String list = sharedPreferences.getString(key, null);
        Type type = new TypeToken<ArrayList<Style>>(){}.getType();
        return gson.fromJson(list, type);
    }

}
