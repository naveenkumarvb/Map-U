package com.example.naveenkumar_v.map_u

/**
 * Created by naveenkumar_v on 22-05-2018.
 */
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.actionbar_titletext_layout.*
import kotlinx.android.synthetic.main.list_of_locations.*
import java.util.*
import kotlin.collections.ArrayList


class ListOfLocations : AppCompatActivity() {


    private var listofplaces = ArrayList<Plaz>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list_of_locations)
        /**
         * Custom Action Bar
         */
        getSupportActionBar()!!.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        val viewActionBar = layoutInflater.inflate(R.layout.actionbar_titletext_layout, null)
        var params = ActionBar.LayoutParams(//Center the textview in the ActionBar !
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER)
        getSupportActionBar()!!.setCustomView(viewActionBar, params);
        actionbar_textview.setText(R.string.Favourite)

        //getting list of selected locations
        val shared = applicationContext.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);

        var lat = shared!!.getString("latitude_current", "")
        var lng = shared!!.getString("longitude_current", "")

        if ((lat.equals("")) && (lng.equals(""))) {

            lat = "0.0";
            lng = "0.0";
        }
        val latLng = LatLng(lat!!.toDouble(), lng!!.toDouble())

        var loc = shared.getInt("locationcount", 0);
        var def_loc = shared.getInt("default_count", 0);

        if ((loc != 0) || (def_loc != 0)) {
            var locationlat = "";
            var locationlong = "";
            var location_place = ""
            /***
             *Custom_location from user
             */
            if (loc != 0) {
                for (i in 0 until loc) {
                    locationlat = shared!!.getString("lat" + i, "");
                    locationlong = shared!!.getString("lng" + i, "");
                    location_place = shared!!.getString("address" + i, "");
                    if ((locationlat.equals("")) && (location_place.equals(""))) {
                    } else {
                        listofplaces.add(Plaz(location_place, LatLng(locationlat.toDouble(), locationlong.toDouble())))
                    }
                }
            }
            /***
             *Default_location from server
             */
            if (def_loc != 0) {
                for (i in 0 until def_loc) {
                    locationlat = shared!!.getString("default_lat" + i, "")
                    locationlong = shared!!.getString("default_lng" + i, "")
                    location_place = shared!!.getString("default_name" + i, "")
                    listofplaces.add(Plaz(location_place, LatLng(locationlat.toDouble(), locationlong.toDouble())))
                }
            }
            Collections.sort(listofplaces, ListPlaces(latLng))
        }


        var notesAdapter = PlaceAdapter(this, listofplaces)
        if (notesAdapter != null) {
            lvNotes.adapter = notesAdapter
        }
        lvNotes.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, id ->
            val intent = Intent(this, DetailScreen::class.java);
            intent.putExtra("ADDRESS", listofplaces[position].title)
            startActivity(intent);
        }

    }

    /**
     * Custom ListView Adapter list of Favourite places
     */
    inner class PlaceAdapter : BaseAdapter {

        private var notesList = ArrayList<Plaz>()
        private var context: Context? = null

        constructor(context: Context, notesList: ArrayList<Plaz>) : super() {
            this.notesList = notesList
            this.context = context
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {

            val view: View?
            val vh: ViewHolder

            if (convertView == null) {
                view = layoutInflater.inflate(R.layout.note, parent, false)
                vh = ViewHolder(view)
                view.tag = vh
            } else {
                view = convertView
                vh = view.tag as ViewHolder
            }

            vh.tvTitle.text = notesList[position].title

            return view
        }

        override fun getItem(position: Int): Any {
            return notesList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return notesList.size
        }
    }

    private class ViewHolder(view: View?) {
        val tvTitle: TextView

//        init {
//            this.tvTitle = view?.findViewById(R.id.tvTitle) as TextView
//            this.tvContent = view?.findViewById(R.id.tvContent) as TextView
//        }

        //  if you target API 26, you should change to:
        init {
            this.tvTitle = view?.findViewById<TextView>(R.id.tvTitle) as TextView
        }
    }

}

/**
 * Sorting Places
 */
class ListPlaces(latLng: LatLng) : Comparator<Plaz> {


    lateinit var currentLoc: LatLng

    init {
        this.currentLoc = latLng
    }

    override fun compare(o1: Plaz?, o2: Plaz?): Int {
        var lat1: Double = o1!!.latlng.latitude
        var lon1: Double = o1!!.latlng.longitude
        var lat2: Double = o2!!.latlng.latitude
        var lon2: Double = o2!!.latlng.longitude
        var distance1: Double = diatance(currentLoc!!.latitude, currentLoc!!.longitude, lat1, lon1)
        var distance2: Double = diatance(currentLoc!!.latitude, currentLoc!!.longitude, lat2, lon2)

        return (distance1 - distance2).toInt(); }

    private fun diatance(fromLat: Double, fromLon: Double, toLat: Double, toLon: Double): Double {
        val radius = 6378137.0   // approximate Earth radius, *in meters*
        val deltaLat = toLat - fromLat
        val deltaLon = toLon - fromLon
        val angle = (2 * Math.asin(Math.sqrt(
                Math.pow(Math.sin(deltaLat / 2), 2.0) + Math.cos(fromLat) * Math.cos(toLat) *
                        Math.pow(Math.sin(deltaLon / 2), 2.0))))
        return radius * angle
    }

}




