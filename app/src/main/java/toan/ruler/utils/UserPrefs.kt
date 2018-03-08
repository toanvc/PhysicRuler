package toan.ruler.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by Toan Vu on 3/8/18.
 */
object UserPrefs {
    private val SHARED_PREF_NAME = "PhysicRuler"
    private val RULER_TYPE = "ruler_type"

    fun saveRulerType(context: Context, isInchType: Boolean) {
        val pref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        pref.edit().putBoolean(RULER_TYPE, isInchType).apply()
    }

    fun getRulerType(context: Context): Boolean {
        val pref = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
        return pref.getBoolean(RULER_TYPE, false)
    }
}



