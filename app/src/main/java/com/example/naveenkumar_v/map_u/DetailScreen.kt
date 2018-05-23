package com.example.naveenkumar_v.map_u

import android.content.Context
import android.os.Bundle
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.naveenkumar_v.map_u.R
import kotlinx.android.synthetic.main.actionbar_titletext_layout.*

/**
 * Created by naveenkumar_v on 22-05-2018.
 */


class DetailScreen:AppCompatActivity(){
    lateinit var edit_notes:EditText
    lateinit var txt_location:TextView
    lateinit var edit_label:EditText
    lateinit var bt_click:Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_screen)
        /**
         * Custom action bar
         */
        getSupportActionBar()!!.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        val viewActionBar = layoutInflater.inflate(R.layout.actionbar_titletext_layout, null)
        var params= ActionBar.LayoutParams(//Center the textview in the ActionBar !
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER)
        getSupportActionBar()!!.setCustomView(viewActionBar,params);
        actionbar_textview.setText(R.string.detail)

        txt_location=findViewById(R.id.location)
        edit_notes=findViewById(R.id.notes)
        edit_label=findViewById(R.id.label)
        bt_click=findViewById(R.id.save)

        var strUser: String = intent.getStringExtra("ADDRESS")
        if(strUser!=null)
        {
            txt_location.text=strUser
        }
        val shared = applicationContext.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        val count = shared.getInt("locationcount",0)
        var def_loc= shared.getInt("default_count",0);


        if((count!=0)||(def_loc!=0)) {
            var addresslocation =""
            /***
             *Custom_Location from user
             */
            if(count!=0) {
                for (i in 0 until count) {
                    addresslocation = shared!!.getString("address" + i, "");
                    if (strUser.equals(addresslocation)) {
                        edit_notes.setText(shared!!.getString(strUser, "").toString())
                        edit_label.setText(shared.getString(strUser+"1","").toString())
                    }
                }
            }
            /***
             *location from server
             */
            if(def_loc!=0){
            for(i in 0 until def_loc){
                addresslocation = shared!!.getString("default_name"+i,"")
                if(strUser.equals(addresslocation)){
                    edit_notes.setText(shared!!.getString(strUser,"").toString())
                    edit_label.setText(shared.getString(strUser+"1","").toString())

                }
            }
            }
        }

        /**
         * saving the label and notes
         */
        bt_click.setOnClickListener(){

            val editor = shared!!.edit();
            editor.putString(strUser, edit_notes.getText().toString())
            editor.putString(strUser+"1", edit_label.getText().toString())
            editor.apply();
        }

    }
}