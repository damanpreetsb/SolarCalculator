package com.daman.solarcalculator

import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.daman.solarcalculator.calculator.SunCalc
import com.daman.solarcalculator.calculator.models.SunPhase
import com.daman.solarcalculator.data.AppDatabase
import com.daman.solarcalculator.data.DbWorkerThread
import com.daman.solarcalculator.data.entities.UserLocations
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var onCameraIdleListener: GoogleMap.OnCameraIdleListener
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val mDb: AppDatabase by lazy { AppDatabase.getInstance(this) }
    private val mDbWorkerThread: DbWorkerThread by lazy { DbWorkerThread("dbWorkerThread") }
    private val mUiHandler = Handler()

    private var calendar: Calendar = Calendar.getInstance()
    private var latLng: LatLng = LatLng(-34.0, 151.0)
    private var locality: String = ""

    private val AUTOCOMPLETE_REQ_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        configureCameraIdle()
        init()
    }

    private fun configureCameraIdle() {
        onCameraIdleListener = GoogleMap.OnCameraIdleListener {
            latLng = mMap.cameraPosition.target

            sunriseSunsetTime(calendar, latLng)
            val geocoder = Geocoder(this@MapsActivity)

            try {
                val addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                if (addressList != null && addressList.size > 0) {
                    locality = addressList[0].getAddressLine(0)
                    if (!locality.isEmpty()) {
                        fetchFromDB(locality)
                        locationNameTextView.text = locality
                    }
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnCameraIdleListener(onCameraIdleListener)
    }

    private fun init() {
        mDbWorkerThread.start()

        getMyLocation()

        currentDayIcon.setOnClickListener {
            calendar = Calendar.getInstance()
            sunriseSunsetTime(calendar, latLng)
            setDate(calendar)
        }
        previousDayIcon.setOnClickListener {
            val cal = calendar
            cal.add(Calendar.DAY_OF_MONTH, -1)
            calendar = cal
            sunriseSunsetTime(calendar, latLng)
            setDate(calendar)
        }
        nextDayIcon.setOnClickListener {
            val cal = calendar
            cal.add(Calendar.DAY_OF_MONTH, 1)
            calendar = cal
            sunriseSunsetTime(calendar, latLng)
            setDate(calendar)
        }

        setDate(calendar)

        searchCard.setOnClickListener {
            try {
                val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(this)
                startActivityForResult(intent, AUTOCOMPLETE_REQ_CODE)
            } catch (e: Exception) {
                Log.e("Tag", e.stackTrace.toString())
            }
        }

        targetFAB.setOnClickListener {
            getMyLocation()
        }

        favoriteLocationImageView.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                val userLocations = UserLocations(latLng.latitude, latLng.longitude, locality)
                insertDataInDb(userLocations)
            } else {
                deleteDataInDb(locality)
            }
        }

        favoriteListImageview.setOnClickListener {
            FavoritesActivity.start(this)
        }
    }

    private fun getMyLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                latLng = LatLng(location?.latitude ?: -34.0, location?.longitude ?: 151.0)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap.moveCamera(CameraUpdateFactory.zoomTo(15F))
                mMap.isMyLocationEnabled = true
            }
    }

    private fun sunriseSunsetTime(calendar: Calendar, location: LatLng) {
        val list = SunCalc.getPhases(calendar, location.latitude, location.longitude)
        for (phase in list) {
            when(phase.name) {
                SunPhase.Name.SUNRISE -> {
                    val startDate = phase.startDate
                    val endDate = phase.endDate
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val sunriseStart = sdf.format(startDate.time)
                    val sunriseEnd = sdf.format(endDate.time)
                    sunRiseStartTextView.text = "Start Time: $sunriseStart"
                    sunRiseEndTextView.text = "End Time: $sunriseEnd"
                }
                SunPhase.Name.SUNSET -> {
                    val startDate = phase.startDate
                    val endDate = phase.endDate
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val sunsetStart = sdf.format(startDate.time)
                    val sunsetEnd = sdf.format(endDate.time)
                    sunSetStartTextView.text = "Start Time: $sunsetStart"
                    sunSetEndTextView.text = "End Time: $sunsetEnd"
                }
            }
        }
    }

    private fun setDate(calendar: Calendar){
        val sdf = SimpleDateFormat("EEEE, MMM dd, YYY", Locale.getDefault())
        val date = sdf.format(calendar.time)
        selectedDateTextView.text = date
    }

    private fun deleteDataInDb(id: String) {
        val task = Runnable { mDb.userLocationsDao().delete(id) }
        mDbWorkerThread.postTask(task)
    }

    private fun insertDataInDb(userLocations: UserLocations) {
        val task = Runnable { mDb.userLocationsDao().insert(userLocations) }
        mDbWorkerThread.postTask(task)
    }

    private fun fetchFromDB(id: String) {
        val task = Runnable {
            val userLocations: UserLocations? =
                mDb.userLocationsDao().get(id)
            mUiHandler.post {
                favoriteLocationImageView.isChecked = userLocations != null
            }
        }
        mDbWorkerThread.postTask(task)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == AUTOCOMPLETE_REQ_CODE) {
            if (resultCode == RESULT_OK) {
                val place = PlaceAutocomplete.getPlace(this, data)
                latLng = place.latLng
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap.moveCamera(CameraUpdateFactory.zoomTo(15F))
                sunriseSunsetTime(calendar, latLng)
            }
        }
    }
}
