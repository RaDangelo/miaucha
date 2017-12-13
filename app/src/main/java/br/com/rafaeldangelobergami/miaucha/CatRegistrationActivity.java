package br.com.rafaeldangelobergami.miaucha;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CatRegistrationActivity extends AppCompatActivity {

    private static final int REQUEST_GPS = 1;
    private LocationManager locationManager;
    private Cat cat;
    private Location currentLocation;
    private EditText catNameText, descriptionText, phoneText;
    private TextView locationText;
    private ImageView catPhoto1, catPhoto2, catPhoto3, catPhoto4;
    private FirebaseDatabase db;
    DatabaseReference reference;
    StorageReference storageRootReference;
    private static final int TAKE_PICTURE_REQUEST = 547;
    private String currentPictureField = null;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull
            String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_GPS:
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                            PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                                0, locationListener);

                        if (currentLocation == null) {
                            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                        cat.setLocation(new CatLocation(currentLocation.getLatitude(), currentLocation.getLongitude()));
                        Address a = getAddress(currentLocation);
                        cat.setAddress(a.getThoroughfare() + ", " + a.getSubThoroughfare() +
                                " - " + a.getSubLocality() + ", " + a.getLocality());
                        locationText.setText(cat.getAddress());

                    }
                } else {
                    Toast.makeText(CatRegistrationActivity.this, getString(R.string.gps_not_allowed), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private Address getAddress(Location loc) {
        Address address = null;
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(loc.getLatitude(),
                    loc.getLongitude(), 1);
            if (addresses.size() > 0) {
                address = addresses.get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return address;
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            currentLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle
                extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cat_registration);
        cat = new Cat();
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        phoneText = (EditText) findViewById(R.id.phoneText);
        phoneText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        catNameText = (EditText) findViewById(R.id.catNameText);
        descriptionText = (EditText) findViewById(R.id.descriptionText);
        locationText = (TextView) findViewById(R.id.locationText);
        catPhoto1 = (ImageView) findViewById(R.id.catPhoto1);
        catPhoto2 = (ImageView) findViewById(R.id.catPhoto2);
        catPhoto3 = (ImageView) findViewById(R.id.catPhoto3);
        catPhoto4 = (ImageView) findViewById(R.id.catPhoto4);
        db = FirebaseDatabase.getInstance();
        reference = db.getReference();
        storageRootReference = FirebaseStorage.getInstance().getReference();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabCatRegistration);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String key = reference.child("cats").push().getKey();

                if (cat.getPictures() != null && cat.getPictures().size() > 0) {
                    ByteArrayOutputStream baos;
                    for (Bitmap pic : cat.getPictures()) {
                        StorageReference picReference = storageRootReference.child("img/" + key + "/" +
                                key + cat.getPictures().indexOf(pic) + ".png");
                        baos = new ByteArrayOutputStream();
                        pic.compress(Bitmap.CompressFormat.PNG, 0, baos);
                        byte[] compressedPicture = baos.toByteArray();
                        picReference.putBytes(compressedPicture).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @SuppressWarnings("VisibleForTests")
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            }
                        });
                    }
                }

                getScreenData();

                Map<String, Object> newCat = cat.toMap();
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put("/cats/" + key, newCat);
                reference.updateChildren(childUpdates);
                Toast.makeText(CatRegistrationActivity.this, getString(R.string.cat_registration_success), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(CatRegistrationActivity.this, MainScreenActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void getScreenData() {
        String catName = catNameText.getEditableText().toString();
        String phone = phoneText.getEditableText().toString();
        String description = descriptionText.getEditableText().toString();
        cat.setName(catName);
        cat.setDescription(description);
        cat.setPhone(phone);
        cat.setUser(User.getInstance());
    }

    @Override
    protected void onStart() {
        super.onStart();
        //verifica se a permissão ainda não foi concedida pelo usuário
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //verifica se deve-se exibir uma explicação sobre a necessidade da permissão
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "We need GPS to get your current location!", Toast.LENGTH_SHORT).show();
            }
            //pede permissão
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_GPS);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                    0, locationListener);

            if (currentLocation == null) {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            cat.setLocation(new CatLocation(currentLocation.getLatitude(), currentLocation.getLongitude()));
            Address a = getAddress(currentLocation);
            cat.setAddress(a.getThoroughfare() + ", " + a.getSubThoroughfare() +
                    " - " + a.getSubLocality() + ", " + a.getLocality());
            locationText.setText(cat.getAddress());
        }
    }

    public void takePicture(View view) {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        currentPictureField = view.getResources().getResourceEntryName(view.getId());
        startActivityForResult(i, TAKE_PICTURE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Bitmap picture = (Bitmap) data.getExtras().get("data");
                cat.getPictures().add(picture);
                if ("catPhoto1".equals(currentPictureField)) {
                    catPhoto1.setImageBitmap(picture);
                } else if ("catPhoto2".equals(currentPictureField)) {
                    catPhoto2.setImageBitmap(picture);
                } else if ("catPhoto3".equals(currentPictureField)) {
                    catPhoto3.setImageBitmap(picture);
                } else if ("catPhoto4".equals(currentPictureField)) {
                    catPhoto4.setImageBitmap(picture);
                }
            }
        }
    }
}
