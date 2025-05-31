package com.example.adnapp


import androidx.lifecycle.ViewModel
import com.example.adnapp.models.Diet

data class UserInfoData(
    val name: String,
    val age: Int,
    val weight: Int,
    val height: Int,
    val selectedGenderId: String
)

data class DietSelectionData(
    var selectedDiet: Diet? = null
)



class SharedViewModel : ViewModel() {

    var userInfoData: UserInfoData? = null
    var dietSelectionData: DietSelectionData? = null
}
