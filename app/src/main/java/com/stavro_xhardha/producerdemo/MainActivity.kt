package com.stavro_xhardha.producerdemo

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import com.squareup.picasso.Picasso
import com.stavro_xhardha.producer.Producer
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

@Producer
class MainActivityViewModel(
    repository: Repository,
    sharedPreferences: SharedPreferences,
    calendar: Calendar,
    picasso: Picasso,
    someDataClass: SomeDataClass
) : ViewModel() {

}

class Repository() {

}

data class SomeDataClass(
    val name: String,
    val id: Int
)
