package com.daman.solarcalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.location.Geocoder
import android.location.Location
import android.util.Log
import com.daman.solarcalculator.calculator.SunCalc
import com.daman.solarcalculator.calculator.models.SunPhase
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.IOException
import java.util.*
import java.text.SimpleDateFormat
import android.content.Intent
import com.google.android.gms.location.places.ui.PlaceAutocomplete


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var onCameraIdleListener: GoogleMap.OnCameraIdleListener
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var calendar: Calendar = Calendar.getInstance()
    private var latLng: LatLng = LatLng(-34.0, 151.0)

    private val AUTOCOMPLETE_REQ_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        configureCameraIdle()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

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
                    val locality = addressList[0].getAddressLine(0)
                    val country = addressList[0].countryName
//                    if (!locality.isEmpty() && !country.isEmpty())
//                        Toast.makeText(this, "$locality  $country", Toast.LENGTH_LONG).show()
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnCameraIdleListener(onCameraIdleListener)
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15F))

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                latLng = LatLng(location?.latitude ?: -34.0, location?.longitude ?: 151.0)
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap.moveCamera(CameraUpdateFactory.zoomTo(15F))
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
                    sunRiseStartTextView.text = sunriseStart
                    sunRiseEndTextView.text = sunriseEnd
                }
                SunPhase.Name.SUNSET -> {
                    val startDate = phase.startDate
                    val endDate = phase.endDate
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    val sunsetStart = sdf.format(startDate.time)
                    val sunsetEnd = sdf.format(endDate.time)
                    sunSetStartTextView.text = sunsetStart
                    sunSetEndTextView.text = sunsetEnd
                }
            }
        }
    }

    private fun init() {
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

        try {
            val intent = PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                .build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQ_CODE)
        } catch (e: Exception) {
            Log.e("Tag", e.stackTrace.toString())
        }

    }

    private fun setDate(calendar: Calendar){
        val sdf = SimpleDateFormat("EEEE, MMM dd, YYY", Locale.getDefault())
        val date = sdf.format(calendar.time)
        selectedDateTextView.text = date
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
