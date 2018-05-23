package com.example.naveenkumar_v.map_u

import com.google.android.gms.maps.model.LatLng

/**
 * Created by naveenkumar_v on 22-05-2018.
 */
class Plaz {
    var title: String? = null
    lateinit var latlng: LatLng

    /**
     * Runtime Constructor
     */
    constructor( title: String,latilong:LatLng) {
        this.title = title
        this.latlng=latilong
    }
}