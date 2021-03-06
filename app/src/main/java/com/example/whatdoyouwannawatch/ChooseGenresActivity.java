package com.example.whatdoyouwannawatch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class ChooseGenresActivity extends AppCompatActivity {
    private String genres[] = {"Biography", "Thriller", "Horror", "Comedy", "Romance", "Crime", "Action-Adventure", "Mystery-Suspense", "Fantasy",
            "Sports", "Drama", "Historical"};

    private ArrayAdapter<String> arrayAdapter;
    private ListView listView;
    private ArrayList<String> selectedGenres;
    private String theatreID;
    private List<String> existingUserPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_genres);

        //Sorting the genres
        for (int i = 0; i < genres.length; i++) {
            for (int j = i + 1; j < genres.length; j++) {
                if (genres[i].compareTo(genres[j]) > 0) {
                    String temp = genres[i];
                    genres[i] = genres[j];
                    genres[j] = temp;
                }
            }
        }
        Intent intent = getIntent();
        theatreID = intent.getStringExtra("theatreID");

        arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_multiple_choice, genres);
        listView = findViewById(R.id.listView_displayGenres);
        listView.setAdapter(arrayAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        String username = fbUser.getDisplayName();
        MainActivity.pullData('u', username, new DataCallback() {
            @Override
            public void onCallback(Object obj) {
                if (obj != null) {
                    final User user = (User) obj;
                    if (!user.isGuest()) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(ChooseGenresActivity.this);
                        builder1.setMessage("Would you like to use your saved genre preferences?");
                        builder1.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                existingUserPreferences = user.getPreferences();
                                for (int i = 0; i < arrayAdapter.getCount(); i++) {
                                    if (existingUserPreferences.contains(listView.getItemAtPosition(i))) {
                                        listView.setItemChecked(i, true);
                                    }
                                }
                                Button button = findViewById(R.id.button_selectGenres);
                                button.performClick();
                                existingUserPreferences = user.getPreferences();
                                dialog.cancel();
                            }
                        });

                        builder1.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                        AlertDialog alert11 = builder1.create();
                        alert11.show();
                    }
                }
            }
        });
    }

    public void onClickSelectGenres(View v) {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        SparseBooleanArray checked = listView.getCheckedItemPositions();
        selectedGenres = new ArrayList<String>();
        for (int i = 0; i < checked.size(); i++) {
            int position = checked.keyAt(i);
            // add genre if checked
            if (checked.valueAt(i)) {
                selectedGenres.add(arrayAdapter.getItem(position));
            }
        }
        final ArrayList<String> userGenres = selectedGenres;
        MainActivity.pullData('u', fbUser.getDisplayName(), new DataCallback() {
            @Override
            public void onCallback(Object obj) {
                if (obj != null) {
                    User u = (User) obj;
                    u.setPreferences(userGenres);
                    MainActivity.pushData(u);
                }
            }
        });
        String genreList = "";
        for (String genre : selectedGenres) {
            genreList += genre + ", ";
        }
        genreList = genreList.trim();
        if (genreList.length() > 0) {
            if (genreList.substring(genreList.length() - 1).equals(",")) {
                genreList = genreList.substring(0, genreList.length() - 1);
            }
        }
//        Toast.makeText(this, genreList, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, ChooseStreamingServicesActivity.class);
        intent.putExtra("genreList", genreList);
        intent.putExtra("theatreID", theatreID);
        startActivity(intent);

    }
}

