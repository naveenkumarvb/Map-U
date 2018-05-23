package com.example.naveenkumar_v.map_u

/**
 * Created by naveenkumar_v on 23-05-2018.
 */
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.AsyncTask
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.actionbar_titletext_layout.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HomeActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, GoogleMap.OnMarkerClickListener, OnMapReadyCallback, GoogleMap.OnMapLongClickListener, GoogleMap.OnMapClickListener, GoogleMap.OnInfoWindowClickListener {


    /***
     * Constant Values For Storing Location
     */
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private val TAG = "Activity"
    }

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocation: Location? = null
    private var mLocationManager: LocationManager? = null
    private lateinit var map: GoogleMap
    private var myPreferences = "myPrefs"
    private var mLocationRequest: LocationRequest? = null
    private val listener: com.google.android.gms.location.LocationListener? = null
    private val UPDATE_INTERVAL = (2 * 1000).toLong()  /* 10 secs */
    private val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
    var locationCount = 0;
    private var locationManager: LocationManager? = null
    var mLastLocation: Location? = null
    var mCurrLocationMarker: Marker? = null
    var shredPref: SharedPreferences? = null
    var editor: SharedPreferences.Editor? = null
    private lateinit var button: Button


    private val isLocationEnabled: Boolean
        get() {
            locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            return locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)
        /**
         * custom action bar
         */
        getSupportActionBar()!!.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        val viewActionBar = layoutInflater.inflate(R.layout.actionbar_titletext_layout, null)
        var params = ActionBar.LayoutParams(//Center the textview in the ActionBar !
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER)
        getSupportActionBar()!!.setCustomView(viewActionBar, params);
        actionbar_textview.setText(R.string.app_name)


        shredPref = getSharedPreferences(myPreferences, Context.MODE_PRIVATE)

        /**
         * GoogleAPI Client Call
         */
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
        mLocationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        checkLocation() //check whether location service is enable or not in your  phone
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val url = "http://bit.ly/test-locations  "
        GetLocationAsyncTask().execute(url)

        button = findViewById(R.id.buttonloc)
        button.setOnClickListener {
            val intent = Intent(this, ListOfLocations::class.java)
            startActivity(intent);
        }


    }

    @SuppressLint("MissingPermission")
    override fun onConnected(p0: Bundle?) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        startLocationUpdates()

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)

        if (mLocation == null) {
            startLocationUpdates()
        }
        if (mLocation != null) {

        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show()
        }
    }
    /**
     * Set Google Map Ready
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        map.setOnMapLongClickListener(this);
        map.setOnInfoWindowClickListener(this)
        map.setOnMapClickListener(this)
        setUpMap()
    }

    private fun setUpMap() {
        /**
         * setupmap using current selected location
         */
        locationCount = shredPref!!.getInt("locationcount", 0)
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        map.isMyLocationEnabled = true
        map.mapType = GoogleMap.MAP_TYPE_NORMAL

        if (locationCount != 0) {
            var locationlat = "";
            var locationlong = "";
            for (i in 0 until locationCount) {
                locationlat = shredPref!!.getString("lat" + i, "");
                locationlong = shredPref!!.getString("lng" + i, "");
                if (locationlat.equals("")) {

                } else {
                    drawMarker(LatLng(locationlat.toDouble(), locationlong.toDouble()))

                }
            }
        }
    }

    override fun onConnectionSuspended(i: Int) {
        /**
         * Connection Suspended
         */
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        /**
         * Connection failed. Error
         */
    }

    override fun onStart() {
        super.onStart()
        if (mGoogleApiClient != null) {
            mGoogleApiClient!!.connect()
        }
    }

    override fun onPause() {
        super.onPause()

        //stop location updates when Activity is no longer active

    }

    override fun onResume() {
        super.onResume()
        if (mGoogleApiClient != null &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
        }
    }

    override fun onStop() {
        super.onStop()
        if (mGoogleApiClient!!.isConnected()) {
            mGoogleApiClient!!.disconnect()
        }
    }

    @SuppressLint("MissingPermission")
    protected fun startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this)
        Log.d("reque", "--->>>>")
    }

    override fun onLocationChanged(location: Location) {


        // You can now create a LatLng Object for use with maps

        val latLng = LatLng(location.latitude, location.longitude)
        mLastLocation = location;
        if (mCurrLocationMarker == null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.getLatitude(), location.getLongitude()), 12f))

            if (location != null) {
                val editor = shredPref!!.edit();
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                editor.putString("latitude_current", currentLatLng!!.latitude.toString());
                editor.putString("longitude_current", currentLatLng!!.longitude.toString());
                editor.apply();
            }
            mCurrLocationMarker = map.addMarker(MarkerOptions().position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        } else {
            mCurrLocationMarker!!.setPosition(latLng);
        }

    }

    private fun checkLocation(): Boolean {
        /**
         * checking location is enabled
         */
        if (!isLocationEnabled)
            showAlert()
        return isLocationEnabled
    }

    override fun onMapClick(p0: LatLng?) {

    }

    override fun onMapLongClick(p0: LatLng?) {
        /**
         * On MapLong Click We can add Custom locations and markers
         */
        if (p0 != null) {
            locationCount++;
            val editor = shredPref!!.edit();
            val titleStr = getAddress(p0)  // add these two lines
            Log.e("count", locationCount.toString())

            if (!titleStr.equals("")) {
                editor.putString("lat" + Integer.toString((locationCount - 1)), p0.latitude.toString());
                editor.putString("lng" + Integer.toString((locationCount - 1)), p0.longitude.toString());
                editor.putString("address" + Integer.toString((locationCount - 1)), titleStr.toString());
                editor.putInt("locationcount", locationCount);
                editor.apply();
                drawMarker(p0);
            }
        }
    }

    override fun onMarkerClick(p0: Marker?): Boolean {

        if (p0 != null) {
            if (p0.isInfoWindowShown()) {
                p0.hideInfoWindow()
            } else {
                if (p0 != null) {
                    p0.showInfoWindow();
                }
            }
            if (p0.title.toString() != null) {
                val intent = Intent(this, DetailScreen::class.java);
                intent.putExtra("ADDRESS", p0.title.toString())
                startActivity(intent);
            }
        }
        return true
    }

    override fun onInfoWindowClick(p0: Marker?) {
        val editor = shredPref!!.edit();
        locationCount = shredPref!!.getInt("locationcount", 0)

        if (locationCount != 0) {
            var locationlat = "";
            var locationlong = "";
            for (i in 0 until locationCount) {
                locationlat = shredPref!!.getString("lat" + i, "");
                locationlong = shredPref!!.getString("lng" + i, "");
//                getAddress(LatLng(String.toDouble(locationlat), String.toDouble(locationlong)))
                Log.e("region", p0?.position?.latitude.toString())

                if (locationlat.equals(p0!!.position.latitude.toString())) {
                    editor.remove("lat" + i)
                    editor.remove("lng" + i)
                    editor.remove("address" + i)
                    editor.commit()
                    p0.remove();

                }
            }
        }
    }

    /**
     * Default Marker
     */
    private fun defaultMarker(pointLocation: LatLng?, str_name: String?) {

        if (pointLocation != null) {
            val markerOptions = MarkerOptions().position(pointLocation)
            val titleStr = getAddress(pointLocation)  // add these two lines
            val title_cust_name = str_name;
            val blueicon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)

            markerOptions.title(title_cust_name)
            markerOptions.icon(blueicon)
            if (shredPref!!.getString(title_cust_name + "1", "") != null) {
                val current_custom_title = shredPref!!.getString(title_cust_name + "1", "")
                markerOptions.snippet(current_custom_title);
            }

            map.addMarker(markerOptions).showInfoWindow();
        }
    }

    /**
     * DrawMarker
     */
    private fun drawMarker(pointLocation: LatLng?) {
        if (pointLocation != null) {
            val markerOptions = MarkerOptions().position(pointLocation)
            val titleStr = getAddress(pointLocation)  // add these two lines
            val editor = shredPref!!.edit();
            val greenicon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            markerOptions.title(titleStr)
            markerOptions.icon(greenicon)
            if (shredPref!!.getString(titleStr + "1", "") != null) {
                val current_custom_title = shredPref!!.getString(titleStr + "1", "")
                markerOptions.snippet(current_custom_title);
            }
            map.addMarker(markerOptions).showInfoWindow();
        }
    }

    /**
     *show alert if location is disabled
     */
    private fun showAlert() {
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " + "use this app")
                .setPositiveButton("Location Settings") { paramDialogInterface, paramInt ->
                    val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(myIntent)
                }
                .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> }
        dialog.show()
    }

    /**
     *Get Address for current Location
     */
    private fun getAddress(latLng: LatLng): String {

        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        val Address2: String?
        var addressText = ""

        try {
            // 2
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            // 3
            if (null != addresses && !addresses.isEmpty()) {
                address = addresses[0]
                Address2 = addresses[0].getAddressLine(0)

                addressText += Address2

            }
        } catch (e: IOException) {
        }
        return addressText
    }

    /**
     *Default location from API
     */
    inner class GetLocationAsyncTask : AsyncTask<String, String, String>() {

        override fun onPreExecute() {
            // Before doInBackground
        }

        var redirect = false

        //doInBackground
        override fun doInBackground(vararg urls: String?): String {
            var urlConnection: HttpURLConnection? = null

            try {
                val url = URL(urls[0])
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.setReadTimeout(5000);
                urlConnection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
                urlConnection.addRequestProperty("User-Agent", "Mozilla");
                urlConnection.addRequestProperty("Referer", "google.com");
                val status = urlConnection.getResponseCode()
                if (status != HttpURLConnection.HTTP_OK) {
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP
                            || status == HttpURLConnection.HTTP_MOVED_PERM
                            || status == HttpURLConnection.HTTP_SEE_OTHER)
                        redirect = true
                }
                if (redirect) {

                    // get redirect url from "location" header field
                    val newUrl = urlConnection.getHeaderField("Location")

                    // get the cookie if need, for login
                    val cookies = urlConnection.getHeaderField("Set-Cookie")

                    // open the new connnection again
                    urlConnection = URL(newUrl).openConnection() as HttpURLConnection
                    urlConnection.setRequestProperty("Cookie", cookies)
                    urlConnection.addRequestProperty("Accept-Language", "en-US,en;q=0.8")
                    urlConnection.addRequestProperty("User-Agent", "Mozilla")
                    urlConnection.addRequestProperty("Referer", "google.com")

                }

                var inString = streamToString(urlConnection.inputStream)

                publishProgress(inString)
            } catch (ex: Exception) {

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect()
                }
            }

            return " "
        }

        /**
         *         On Progress update Default Marker Creation
         */
        override fun onProgressUpdate(vararg values: String?) {
            try {
                var json = JSONObject(values[0])
                val query = json.getJSONArray("locations")
                for (i in 0 until query.length()) {
                    val list = query.getJSONObject(i)
                    val editor = shredPref!!.edit();
                    editor.putString("default_name" + i, list.get("name").toString())
                    editor.putString("default_lat" + i, list.get("lat").toString())
                    editor.putString("default_lng" + i, list.get("lng").toString())
                    editor.putInt("default_count", i + 1);
                    editor.apply();
                    defaultMarker(LatLng(shredPref!!.getString("default_lat" + i, "").toDouble(), shredPref!!.getString("default_lng" + i, "").toDouble()), shredPref!!.getString("default_name" + i, ""));
                }
            } catch (ex: Exception) {

            }
        }

        override fun onPostExecute(result: String?) {
            // Done
        }
    }

    fun streamToString(inputStream: InputStream): String {
        //stream to String
        val bufferReader = BufferedReader(InputStreamReader(inputStream))
        var line: String
        var result = ""

        try {
            do {
                line = bufferReader.readLine()
                if (line != null) {
                    result += line
                }
            } while (line != null)
            inputStream.close()
        } catch (ex: Exception) {

        }

        return result
     }
}
