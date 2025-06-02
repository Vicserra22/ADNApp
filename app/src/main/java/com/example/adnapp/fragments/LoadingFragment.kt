package com.example.adnapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.adnapp.R

class LoadingFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    companion object {
        const val TAG = "LoadingFragment"

        fun show(fragmentManager: androidx.fragment.app.FragmentManager) {
            // Evita mostrar múltiples instancias
            if (fragmentManager.findFragmentByTag(TAG) == null) {
                val loadingFragment = LoadingFragment()
                loadingFragment.show(fragmentManager, TAG)
            }
        }

        fun dismiss(fragmentManager: androidx.fragment.app.FragmentManager) {
            val loadingFragment = fragmentManager.findFragmentByTag(TAG) as? LoadingFragment
            loadingFragment?.dismissAllowingStateLoss()
        }
    }
}
