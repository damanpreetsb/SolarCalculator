package com.daman.solarcalculator.util

import android.content.Context
import android.widget.Toast

/**
 * Created by Daman on 9/10/2018.
 */
fun Any.toast(context: Context): Toast {
    return Toast.makeText(context, this.toString(), Toast.LENGTH_LONG).apply { show() }
}