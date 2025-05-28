package com.example.adnapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.adnapp.SharedViewModel
import com.example.adnapp.databinding.FragmentUserInfoBinding
import com.example.adnapp.UserInfoData
import com.example.adnapp.interfaces.OnUserInfoCompleteListener

class UserInfoFragment : Fragment() {

    private var _binding: FragmentUserInfoBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserInfoBinding.inflate(inflater, container, false)

        binding.btnNextUserInfo.setOnClickListener {
            val name = binding.etName.text.toString()
            val age = binding.etAge.text.toString().toIntOrNull()
            val weight = binding.etWeight.text.toString().toIntOrNull()
            val height = binding.etHeight.text.toString().toIntOrNull()
            val selectedGenderId = binding.rgGender.checkedRadioButtonId

            if (age != null && weight != null && height != null && selectedGenderId != -1) {
                val genero = view?.findViewById<RadioButton>(selectedGenderId)?.text.toString()

                // Guardar datos en SharedViewModel
                sharedViewModel.userInfoData = UserInfoData(name, age, weight, height, genero)

                (activity as? OnUserInfoCompleteListener)?.onUserInfoComplete()

            } else {
                Toast.makeText(requireContext(), "Rellena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
