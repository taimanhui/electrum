package org.haobtc.onekey.business.wallet

import androidx.annotation.WorkerThread
import com.alibaba.fastjson.JSON
import com.google.common.base.Strings
import org.haobtc.onekey.activities.base.MyApplication
import org.haobtc.onekey.bean.TokenList.ERCToken
import org.haobtc.onekey.manager.PyEnv
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

/** @Description: java类作用描述 @Author: peter Qin
 */
class TokenManager {


  /**
   * 如果没网，就用本地文件存储的 TokenList 展示 如果能从服务器拿到数据，判断是否需要更新本地文件
   *
   * @param json
   */
  @WorkerThread
  fun uploadLocalTokenList(json: String?): Boolean {
    var writer: FileWriter? = null
    var isSuccess: Boolean
    try {
      val file = File(FILE_PATH)
      if (file.exists()) {
        file.delete()
      }
      file.createNewFile()
      writer = FileWriter(file)
      if (!Strings.isNullOrEmpty(json)) {
        writer.write(json)
      }
      writer.flush()
      isSuccess = true
    } catch (e: Exception) {
      isSuccess = false
    } finally {
      if (null != writer) {
        try {
          writer.close()
        } catch (e: IOException) {
          e.printStackTrace()
        }
      }
    }
    return isSuccess
  }

  @WorkerThread
  fun initFile() {
    mReadWriteLock.write {
      val response = PyEnv.getAllTokenInfo()
      uploadLocalTokenList(response.result)
    }
  }

  @get:WorkerThread
  private val localTokenList: String?
    private get() {
      try {
        val bfr = BufferedReader(FileReader(FILE_PATH))
        var line = bfr.readLine()
        val builder = StringBuilder()
        while (line != null) {
          builder.append(line)
          builder.append("\n")
          line = bfr.readLine()
        }
        bfr.close()
        return builder.toString()
      } catch (e: IOException) {
        e.printStackTrace()
      }
      return null
    }
  val tokenList: List<ERCToken>
    get() = mReadWriteLock.read {
      JSON.parseArray(localTokenList, ERCToken::class.java)
    }// 拼接字符串

  /**
   * 通过 address 获取到 ERCToken 实体类
   *
   * @param address token 地址
   * @return
   */
  fun getTokenByAddress(address: String?): ERCToken? = mReadWriteLock.read {
    val tokenList = JSON.parseArray(localTokenList, ERCToken::class.java)
    for (token in tokenList) {
      if (!Strings.isNullOrEmpty(token.address) && !Strings.isNullOrEmpty(address)) {
        if (token.address.equals(address, ignoreCase = true)) {
          return token
        }
      }
    }
    return null
  }

  companion object {
    //    private static final String FILE_PATH = "eth_token_list.json";
    private val FILE_PATH = (MyApplication.getInstance().filesDir.absolutePath
        + "/"
        + "eth_token_list.json")

    // 读写锁
    private val mReadWriteLock = ReentrantReadWriteLock()
  }
}
