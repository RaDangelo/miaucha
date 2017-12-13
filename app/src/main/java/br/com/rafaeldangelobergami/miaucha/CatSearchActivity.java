package br.com.rafaeldangelobergami.miaucha;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CatSearchActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter {

    private GoogleMap mMap;
    private Marker myMarker;
    private HashMap<String, Cat> myCats;
    private HashMap<Marker, String> catMarkers;
    private HashMap<String, List<Bitmap>> catPictures;
    private TextView catName, catDescription, phone, owner;
    private ImageView catImage1, catImage2, catImage3, catImage4;

    private static final int REQUEST_GPS = 1;
    private LocationManager locationManager;
    private FirebaseDatabase db;
    DatabaseReference reference;
    StorageReference storageRootReference;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cat_search);
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        myCats = new HashMap<>();
        catMarkers = new HashMap<>();
        catPictures = new HashMap<>();

        db = FirebaseDatabase.getInstance();
        reference = db.getReference();
        storageRootReference = FirebaseStorage.getInstance().getReference();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        db.getReference("cats").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;
                int icon = R.mipmap.caticon5;
                String snippet = getString(R.string.salute_0);
                for (DataSnapshot d : dataSnapshot.getChildren()) {
                    downloadPictures(d.getKey());
                    if (i == 1) {
                        icon = R.mipmap.caticon1;
                        snippet = getString(R.string.salute_1);
                    } else if (i == 2) {
                        icon = R.mipmap.caticon2;
                        snippet = getString(R.string.salute_2);
                    } else if (i == 3) {
                        icon = R.mipmap.caticon3;
                        snippet = getString(R.string.salute_3);
                    } else if (i == 4) {
                        icon = R.mipmap.caticon4;
                        snippet = getString(R.string.salute_4);
                    }

                    Cat c = d.getValue(Cat.class);
                    myCats.put(d.getKey(), c);
                    LatLng catPosition = new LatLng(c.getLocation().getLatitude(), c.getLocation().getLongitude());
                    Marker m = mMap.addMarker(new MarkerOptions().position(catPosition).title(getString(R.string.cat_found_default)).snippet("\"I'm " + c.getName() + "! " + snippet + "\"")
                            .icon(BitmapDescriptorFactory.fromResource(icon)));

                    catMarkers.put(m, d.getKey());

                    if (i < 4) {
                        i++;
                    } else {
                        i = 0;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
//        return prepareInfoView(marker);
    }

    @Override
    public View getInfoContents(Marker marker) {
        if (!marker.equals(myMarker)) {
            return prepareInfoView(marker);
        } else {
            return null;
        }

    }

    private View prepareInfoView(Marker marker) {
        View v = getLayoutInflater().inflate(R.layout.cat_showcase, null);

        catName = (TextView) v.findViewById(R.id.cat_name_showcase);
        catDescription = (TextView) v.findViewById(R.id.cat_description_showcase);
        phone = (TextView) v.findViewById(R.id.phone_showcase);
        owner = (TextView) v.findViewById(R.id.owner_showcase);
        catImage1 = (ImageView) v.findViewById(R.id.catImage1);
        catImage2 = (ImageView) v.findViewById(R.id.catImage2);
        catImage3 = (ImageView) v.findViewById(R.id.catImage3);
        catImage4 = (ImageView) v.findViewById(R.id.catImage4);

        String key = catMarkers.get(marker);
        Cat cat = myCats.get(key);

        if (catPictures.get(key) != null) {
            for (Bitmap b : catPictures.get(key)) {
                if (catPictures.get(key).indexOf(b) == 0) {
                    catImage1.setImageBitmap(b);
                } else  if (catPictures.get(key).indexOf(b) == 1) {
                    catImage2.setImageBitmap(b);
                } else  if (catPictures.get(key).indexOf(b) == 2) {
                    catImage3.setImageBitmap(b);
                } else {
                    catImage4.setImageBitmap(b);
                }

            }
        }

        catName.setText(cat.getName());
        catDescription.setText(cat.getDescription());
        phone.setText(cat.getPhone());
        owner.setText("Owner: " + cat.getUser().getName());

        return v;
    }

    private void downloadPictures(final String key) {
        if (key != null) {
            for (int i = 0; i < 4
                    ; i++) {
                StorageReference picReference = storageRootReference.child("img/" + key + "/" + key + i + ".png");
                try {
                    File temp = File.createTempFile("img", "png");
                    final String path = temp.getPath();
                    picReference.getFile(temp).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            Bitmap b = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(path), 100, 100, false);
                            if (catPictures.get(key) != null) {
                                catPictures.get(key).add(b);
                            } else {
                                List<Bitmap> list = new ArrayList<Bitmap>();
                                list.add(b);
                                catPictures.put(key, list);
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(MyOnInfoWindowClickListener);
        mMap.setInfoWindowAdapter(this);

        LatLng currentPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        myMarker = mMap.addMarker(new MarkerOptions().zIndex(100).position(currentPosition).title("You're here!").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 14.0f));
    }

    GoogleMap.OnInfoWindowClickListener MyOnInfoWindowClickListener
            = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            if (!marker.equals(myMarker)) {

                String key = catMarkers.get(marker);
                Cat cat = myCats.get(key);

                Toast.makeText(CatSearchActivity.this, "Calling " +
                                cat.getName() + "...",
                        Toast.LENGTH_LONG).show();

                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + phone.getText()));
                startActivity(callIntent);
            } else {
                Toast.makeText(CatSearchActivity.this, "Hi, " +
                                User.getInstance().getName(),
                        Toast.LENGTH_LONG).show();
            }
        }
    };

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
                    {android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_GPS);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                    0, locationListener);

            if (currentLocation == null) {
                currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
        }
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
                    }
                } else {
                    Toast.makeText(CatSearchActivity.this, getString(R.string.gps_not_allowed), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void callNumber() {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + phone.getText()));
        startActivity(callIntent);
    }
}
