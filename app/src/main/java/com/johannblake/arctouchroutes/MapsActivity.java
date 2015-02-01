package com.johannblake.arctouchroutes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

/**
 * Used to display a Google Map where the user can select a street by just tapping on the map. Press the left arrow
 * button in the upper left corner of the map to return to the previous activity with the selected street name.
 */
public class MapsActivity extends FragmentActivity
{
  public static final String INTENT_KEY_SELECTED_STREET_NAME = "selectedStreetName";
  private final String LOGTAG = "MapsActivity";

  private Context context;
  private GoogleMap map; // Might be null if Google Play services APK is not available.
  private String selectedStreet;


  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    try
    {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_maps);

      this.context = this;

      setUpMapIfNeeded();
    }
    catch(Exception ex)
    {
      Log.e(LOGTAG, ".onCreate: " + ex.getMessage());
    }
  }

  public void closeActivity(View view)
  {
    try
    {
      // Return the name of the selected street.
      Intent intent = new Intent();
      intent.putExtra(INTENT_KEY_SELECTED_STREET_NAME, this.selectedStreet);
      setResult(Activity.RESULT_OK, intent);

      finish();
    }
    catch(Exception ex)
    {
      Log.e(LOGTAG, ".closeActivity: " + ex.getMessage());
    }
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    setUpMapIfNeeded();
  }

  /**
   * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
   * installed) and the map has not already been instantiated.. This will ensure that we only ever
   * call {@link #setUpMap()} once when {@link #map} is not null.
   * <p/>
   * If it isn't installed {@link SupportMapFragment} (and
   * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
   * install/update the Google Play services APK on their device.
   * <p/>
   * A user can return to this FragmentActivity after following the prompt and correctly
   * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
   * have been completely destroyed during this process (it is likely that it would only be
   * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
   * method in {@link #onResume()} to guarantee that it will be called.
   */
  private void setUpMapIfNeeded()
  {
    // Do a null check to confirm that we have not already instantiated the map.
    if (this.map == null)
    {
      // Try to obtain the map from the SupportMapFragment.
      this.map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
              .getMap();
      // Check if we were successful in obtaining the map.
      if (this.map != null)
      {
        setUpMap();
      }
    }
  }

  /**
   * This is where we can add markers or lines, add listeners or move the camera. In this case, we
   * just add a marker near Africa.
   * <p/>
   * This should only be called once and when we are sure that {@link #map} is not null.
   */
  private void setUpMap()
  {
    try
    {
      //this.map.addMarker(new MarkerOptions().position(new LatLng(-27.577016, -48.535774)).title("Marker"));

      LatLng latLng = new LatLng(-27.577016, -48.535774);
      CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17.0f);
      this.map.moveCamera(cameraUpdate);

      // Handle the user tapping on the map.
      this.map.setOnMapClickListener(new GoogleMap.OnMapClickListener()
      {
        @Override
        public void onMapClick(LatLng latLng)
        {
          try
          {
            // Retrieve the street name.
            Geocoder gc = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addresses.size() > 0)
            {
              // Clear any previous marker that the user created.
              map.clear();

              Address address = addresses.get(0);
              selectedStreet = address.getThoroughfare();
              Marker marker = map.addMarker(new MarkerOptions().position(latLng).title(selectedStreet));
              marker.showInfoWindow();
            }

          }
          catch(Exception ex)
          {
            Log.e(LOGTAG, ".setUpMap.setOnMapClickListener: " + ex.getMessage());
          }
        }
      });

    }
    catch(Exception ex)
    {
      Log.e(LOGTAG, ".setUpMap: " + ex.getMessage());
    }
  }
}
