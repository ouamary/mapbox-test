package co.nayt.mapboxtest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;

import static co.nayt.mapboxtest.Utils.DEFAULT_LAT;
import static co.nayt.mapboxtest.Utils.DEFAULT_LNG;
import static co.nayt.mapboxtest.Utils.LOCATION_REQUEST_CODE;
import static co.nayt.mapboxtest.Utils.MAPBOX_DURATION;
import static co.nayt.mapboxtest.Utils.MAPBOX_ZOOM;
import static co.nayt.mapboxtest.Utils.verifyPermissions;

public class MapActivity extends AppCompatActivity implements OnSuccessListener<Location> {
    private SupportMapFragment mMapFragment;
    private Location mLocation;
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Creates a Mapbox SupportMapFragment and retrieves a
     * FusedLocationProviderClient to get a location making
     * sure permissions are granted in the process.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Mapbox.getInstance(this, getString(R.string.mapbox_token));
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (savedInstanceState == null) {
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

            // Setting the location
            LatLng coord = new LatLng(DEFAULT_LAT, DEFAULT_LNG);
            if (mLocation != null) {
                coord = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
            }

            // Creating the map fragment with options
            MapboxMapOptions options = new MapboxMapOptions();
            options.styleUrl(Style.MAPBOX_STREETS);
            options.camera(new CameraPosition.Builder().target(coord).zoom(MAPBOX_ZOOM).build());

            mMapFragment = SupportMapFragment.newInstance(options);

            transaction.add(R.id.container, mMapFragment, getString(R.string.fragment_tag));
            transaction.commit();
        } else {
            // Fragment is already there
            mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.fragment_tag));
        }

        // Runtime permission check for Marshmallow or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Request permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
            } else {
                // Permissions already granted
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, this);
            }
        } else {
            // No need to request permissions
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, this);
        }
    }

    /**
     * Callback that retrieves the permission check result. If
     * all permissions are granted, gets the last known location.
     *
     * Intentionally ignores MissingPermission warning because
     * permissions are granted when a location is requested.
     *
     * See supermethod for params.
     */
    @SuppressWarnings({"MissingPermission"})
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (verifyPermissions(grantResults)) {
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, this);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * The location provider has successfully returned a location
     * and initiates this callback.
     */
    @Override
    public void onSuccess(Location location) {
        // Location might still be null for various reasons
        if (location != null) {
            mLocation = location;
            showLocation(mMapFragment, new LatLng(mLocation.getLatitude(), mLocation.getLongitude()));
        } else {
            showLocation(mMapFragment, new LatLng(DEFAULT_LAT, DEFAULT_LNG));
        }
    }

    /**
     * Moves the map to the specified location and adds a marker
     * on the target position.
     *
     * @param mapFragment The fragment showing the map
     * @param coordinates A set of decimal coordinates
     */
    void showLocation(SupportMapFragment mapFragment, final LatLng coordinates) {
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mapboxMap) {
                    CameraPosition position = new CameraPosition.Builder()
                            .target(coordinates)
                            .zoom(MAPBOX_ZOOM)
                            .build();

                    mapboxMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(position), MAPBOX_DURATION);
                    mapboxMap.addMarker(new MarkerOptions()
                            .position(coordinates));
                }
            });
        }
    }
}
