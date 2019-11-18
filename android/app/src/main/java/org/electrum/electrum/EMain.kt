package org.electrum.electrum

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import android.os.Bundle

val test1Deamo by lazy { guiMod("daemon") }

class EMActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.out.println("main.kt onCreate in.....=========================================================================")

        test1Deamo.callAttr("test")
    }
}
