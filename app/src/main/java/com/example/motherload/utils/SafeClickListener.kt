package com.example.motherload.utils

import android.os.SystemClock
import android.view.View

class SafeClickListener (
    private var defaultInterval: Int = 1000,
    private val onSafeClick: (View) -> Unit
) :View.OnClickListener {
    private var lastTimeClicked: Long = 0

    /**
     * Permet de cliquer 1 seule fois par seconde grâce au time
     *
     * @param v la vue ou se trouve le bouton
     */
    override fun onClick(v: View) {
        if (SystemClock.elapsedRealtime() - lastTimeClicked < defaultInterval){
            return
        }
        lastTimeClicked = SystemClock.elapsedRealtime()
        onSafeClick(v)
    }

}

/**
 * Permet d'attribuer ce listener à un bouton
 *
 * @param onSafeClick la fonction à effectuer en cas de clic
 */
fun View.setSafeOnClickListener(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}