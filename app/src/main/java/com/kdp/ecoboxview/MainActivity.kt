package com.kdp.ecoboxview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val data = arrayListOf<Part>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        data.add(Part("A",0.5F))
        data.add(Part("B",0.3F))
        data.add(Part("C",0.95F))

        ecoBoxView.setData(data)
    }
}
