package org.electrum.electrum

import android.app.Activity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import android.os.Bundle
import com.chaquo.python.PyObject
import java.util.ArrayList

val test1Deamo by lazy { guiMod("daemon") }

class EMActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        System.out.println("main.kt onCreate in.....=========================================================================")

        val type:String = "multi"
        if (type == "standard") {
            //create standard wallet
            val name: String? = "hhh"
            daemonModel.commands.callAttr("create", name, "111111")
            daemonModel.commands.callAttr("list_wallets").asList().map { it.toString() }

            val password: String? = "111111"
            daemonModel.commands.callAttr("load_wallet", name, password)
            daemonModel.commands.callAttr("select_wallet", name)
            System.out.println("main.kt onCreate now wallet .....========" + daemonModel.wallet)

            val name1 = "wls"
            daemonModel.commands.callAttr("load_wallet", name1, password)
            daemonModel.commands.callAttr("select_wallet", name1)
            System.out.println("main.kt onCreate now wallet .....========" + daemonModel.wallet)
        }
        else if(type == "multi")
        {
            val name = "hahahahhahh222"
            val password = "111111"
            val m = 2
            val n = 2
            val xpub1 ="Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41vMKQbeHveRvuThAmm"
            val xpub2 ="Vpub5gyCX33B53xAyfEaH1Jfnp5grizbHfxVz6bWLPD92nLcbKMsQzSbM2eyGiK4qiRziuoRhoeVMoPLvEdfbQxGp88PN9cU6zupSSuiPi3RjEg"

            create wallet
            daemonModel.commands.callAttr("delete_wallet", name)
            daemonModel.commands.callAttr("set_multi_wallet_info", name, m, n)
            daemonModel.commands.callAttr("add_xpub", xpub1)
            daemonModel.commands.callAttr("add_xpub", xpub2)
            val info:List<String> = daemonModel.commands.callAttr("get_keystores_info").asList().map { it.toString() }
            System.out.println("main.kt onCreate keystors .....========" + info)
            val num:List<Int> = daemonModel.commands.callAttr("get_cosigner_num").asList().map { it.toInt() }
            System.out.println("main.kt onCreate m+n .....========" + num)
            daemonModel.commands.callAttr("create_multi_wallet", name)
            //daemonModel.commands.callAttr("get_xpub_from_hw")

            //load_wallet
            daemonModel.commands.callAttr("load_wallet", name, password)
            daemonModel.commands.callAttr("select_wallet", name)

            val wallet_str:String = daemonModel.commands.callAttr("get_wallets_list_info").toString()
            System.out.println("main.kt onCreate wallet info is  .....======================" + wallet_str)

        }
    }

}
