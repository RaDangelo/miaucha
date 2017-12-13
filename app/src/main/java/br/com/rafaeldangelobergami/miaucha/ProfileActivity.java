package br.com.rafaeldangelobergami.miaucha;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText nameText, emailText, passwordText;
    private ImageView photoImageView;
    private FirebaseDatabase db;
    DatabaseReference reference;
    StorageReference storageRootReference;
    private static final int TAKE_PICTURE_REQUEST = 547;
    private boolean isEdition = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameText = (EditText) findViewById(R.id.nameText);
        passwordText = (EditText) findViewById(R.id.passwordText);
        emailText = (EditText) findViewById(R.id.emailText);
        photoImageView = (ImageView) findViewById(R.id.photoImageView);

        db = FirebaseDatabase.getInstance();
        reference = db.getReference();
        storageRootReference = FirebaseStorage.getInstance().getReference();


        downloadPicture();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabConfirmation);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (User.getInstance() != null && User.getInstance().getKey() != null) {
                    isEdition = true;
                } else {
                    User.getInstance().setKey(reference.child("users").push().getKey());
                }

                if (User.getInstance().getPicture() != null) {
                    StorageReference picReference = storageRootReference.child("img/" + User.getInstance().getKey() + ".png");
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    User.getInstance().getPicture().compress(Bitmap.CompressFormat.PNG, 0, baos);
                    byte[] compressedPicture = baos.toByteArray();
                    picReference.putBytes(compressedPicture).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @SuppressWarnings("VisibleForTests")
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        }
                    });
                }

                getScreenData();

                Map<String, Object> newUser = User.getInstance().toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/users/" + User.getInstance().getKey(), newUser);
                reference.updateChildren(childUpdates);
//                String key = reference.child("users").push().setValue(User.getInstance()).getKey();
                Toast.makeText(ProfileActivity.this, getString(R.string.registration_success), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ProfileActivity.this, isEdition ? MainScreenActivity.class : LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });


        db.getReference("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (User.getInstance().getKey() != null && dataSnapshot.getKey() == User.getInstance().getKey()) {
                    User u = dataSnapshot.getValue(User.class);
                    User.getInstance().setUser(u.getUser());
                    User.getInstance().setPassword(u.getPassword());
                    User.getInstance().setName(u.getName());
                    User.getInstance().setKey(dataSnapshot.getKey());
                    setFieldsOnScreen();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getScreenData() {
        String name = nameText.getEditableText().toString();
        String password = passwordText.getEditableText().toString();
        String email = emailText.getEditableText().toString();
        User.getInstance().setName(name);
        User.getInstance().setPassword(password);
        User.getInstance().setUser(email);
    }


    private void downloadPicture() {
        if (User.getInstance().getKey() != null) {
            StorageReference picReference = storageRootReference.child("img/" + User.getInstance().getKey() + ".png");
            try {
                File temp = File.createTempFile("img", "png");
                final String path = temp.getPath();
                picReference.getFile(temp).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        User.getInstance().setPicture(BitmapFactory.decodeFile(path));
                        photoImageView.setImageBitmap(User.getInstance().getPicture());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFieldsOnScreen();
    }

    private void setFieldsOnScreen() {
        nameText.setText(User.getInstance().getName());
        emailText.setText(User.getInstance().getUser());
        passwordText.setText(User.getInstance().getPassword());
        if (User.getInstance().getPicture() != null) {
            photoImageView.setImageBitmap(User.getInstance().getPicture());
        }
    }

    public void takePicture(View view) {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, TAKE_PICTURE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Bitmap picture = (Bitmap) data.getExtras().get("data");
                User.getInstance().setPicture(picture);
                photoImageView.setImageBitmap(picture);
            }
        }
    }
}
