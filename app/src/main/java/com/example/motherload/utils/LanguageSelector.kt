package com.example.motherload.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.FragmentActivity
import com.example.motherland.MotherLoad

class LanguageSelector(private val activity: FragmentActivity) : Activity(), AdapterView.OnItemSelectedListener{
    override fun onItemSelected(
        parent: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long
    ) {
        var lang = "fr-FR"
        var pos = 0
        when(position){
            1 -> {lang = "en-US"
                pos = 1}
            2 -> {lang = "ko-KR"
                pos = 2}
            3 -> {lang = "ja-JP"
                pos = 3}
            else -> {}
        }
        val appLocales : LocaleListCompat = LocaleListCompat.forLanguageTags(lang)
        val sharedPref = MotherLoad.instance.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        if (sharedPref.getInt("posLangue",0) != pos){
            val editor = sharedPref.edit()
            editor.putString("langue",lang)
            editor.putInt("posLangue",pos)
            editor.apply()
            AppCompatDelegate.setApplicationLocales(appLocales)
            if (activity != null){
                activity?.finish()
                activity?.startActivity(activity?.intent)
            }

        }
    }
    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}
