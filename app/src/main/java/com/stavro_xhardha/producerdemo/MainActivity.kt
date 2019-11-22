package com.stavro_xhardha.producerdemo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val activityViewModel by viewModels<MainActivityViewModel> {

        //generated
        MainActivityViewModelFactory(
            fakeDependency = FakeDependency(),
            fakeRepository = FakeRepository(),
            fakeRetrofit = FakeRetrofit(),
            fakeFake = FakeFake()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}