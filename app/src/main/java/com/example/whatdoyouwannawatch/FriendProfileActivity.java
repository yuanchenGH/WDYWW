package com.example.whatdoyouwannawatch;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class FriendProfileActivity extends AppCompatActivity {
    String uName;
    ImageView profImg;
    private static final int RESULT_LOAD_IMAGE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);
        profImg = findViewById(R.id.profileImage);

        Intent intent = getIntent();
        final String user = intent.getStringExtra("username");
        //final String uid = user.getUid();
        if (user != null) {


            uName = user;
            // profImg = findViewById(R.id.profileImage);
            TextView name = findViewById(R.id.textView_Name);
            name.setText(uName);
            MainActivity.checkProfileImg(new CheckCallBack() {
                @Override
                public void onCallback(Boolean fileFound) {
                    if (fileFound) {
                        MainActivity.downloadProfileImg(new ImageCallBack() {
                            @Override
                            public void onCallback(byte[] bytes) {
                                Log.d("file", "Downloading profile pictuere");
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                profImg.setImageBitmap(bitmap);
                            }
                        }, uName);
                    } else {
                        Log.d("file", "File not found, no upload");
                    }
                }
            }, uName);

            profImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);

                }
            });
            MainActivity.pullData('u', user, new DataCallback() {
                @Override
                public void onCallback(Object obj) {
                    if (obj != null) {
                        User u = (User) obj;
                        String genres = u.getPreferences().toString();
                        Log.i("Genre", u.toString());
                        TextView displayGenres = findViewById(R.id.textView);
                        displayGenres.setText(genres);
                    }
                }
            });

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            profImg.setImageURI(selectedImage);

            // Get the data from an ImageView as bytes
            profImg.setDrawingCacheEnabled(true);
            profImg.buildDrawingCache();
            Bitmap bitmap = ((BitmapDrawable) profImg.getDrawable()).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();

            MainActivity.setProfileImg(uName, bytes);
        }
    }

    public void onClickUnfriend(View v) {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        MainActivity.pullData('u', fbUser.getDisplayName(), new DataCallback() {
            @Override
            public void onCallback(Object obj) {
                if (obj != null) {
                    final User u = (User) obj;
                    MainActivity.pullData('u', uName, new DataCallback() {
                        @Override
                        public void onCallback(Object obj) {
                            if (obj != null) {
                                User friend = (User) obj;
                                if(u.getFriends()==null){
                                    Toast.makeText(getApplicationContext(), "You and this user aren't currently friends", Toast.LENGTH_SHORT).show();
                                }else {
                                    ArrayList<User> friends = (ArrayList<User>) u.getFriends();
                                    boolean found = false;
                                    for (int i = 0; i < friends.size(); i++) {
                                        if (friends.get(i).getUsername().equals(friend.getUsername())) {
                                            found = true;
                                        }
                                    }
                                    if (found) {
                                        u.removeFriend(friend);
                                        MainActivity.pushData(u);
                                        Toast.makeText(getApplicationContext(), "Friend successfully removed", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "You and this user aren't currently friends", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    public void onClickFriend(View v) {
        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        MainActivity.pullData('u', fbUser.getDisplayName(), new DataCallback() {
            @Override
            public void onCallback(Object obj) {
                if (obj != null) {
                    final User u = (User) obj;
                    MainActivity.pullData('u', uName, new DataCallback() {
                        @Override
                        public void onCallback(Object obj) {
                            if (obj != null) {
                                if (u.getFriends() == null) {
                                    User friend = (User) obj;
                                    u.addFriend(friend);
                                    MainActivity.pushData(u);
                                    Toast.makeText(getApplicationContext(), "Friend successfully added", Toast.LENGTH_SHORT).show();
                                } else {
                                    User friend = (User) obj;
                                    ArrayList<User> friends = (ArrayList<User>) u.getFriends();
                                    boolean found =false;
                                    for(int i = 0; i< friends.size(); i++){
                                        if(friends.get(i).getUsername().equals(friend.getUsername())){
                                            found =true;
                                        }
                                    }
                                    if(found) {
                                        Toast.makeText(getApplicationContext(), "You are already friends with this user", Toast.LENGTH_SHORT).show();
                                    }else {
                                        u.addFriend(friend);
                                        MainActivity.pushData(u);
                                        Toast.makeText(getApplicationContext(), "Friend successfully added", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            } else {
                                Toast.makeText(getApplicationContext(), "User profile not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}