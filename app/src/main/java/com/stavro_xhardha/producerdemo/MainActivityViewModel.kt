package com.stavro_xhardha.producerdemo

import androidx.lifecycle.ViewModel
import com.stavro_xhardha.producer.Producer

@Producer
class MainActivityViewModel(
    fakeRetrofit: FakeRetrofit,
    fakeDependency: FakeDependency,
    fakeRepository: FakeRepository,
    fakeFake: FakeFake
) : ViewModel() {
}