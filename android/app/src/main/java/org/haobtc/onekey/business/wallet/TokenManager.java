package org.haobtc.onekey.business.wallet;

import androidx.annotation.WorkerThread;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import org.haobtc.onekey.activities.base.MyApplication;
import org.haobtc.onekey.bean.TokenList;

/** @Description: java类作用描述 @Author: peter Qin */
public class TokenManager {

    //    private static final String FILE_PATH = "eth_token_list.json";

    private static final String FILE_PATH =
            MyApplication.getInstance().getFilesDir().getAbsolutePath()
                    + "/"
                    + "eth_token_list.json";

    /**
     * 如果没网，就用本地文件存储的 TokenList 展示 如果能从服务器拿到数据，判断是否需要更新本地文件
     *
     * @param json
     */
    @WorkerThread
    public boolean uploadLocalTokenList(String json) {
        FileWriter writer = null;
        boolean isSuccess;
        try {
            File file = new File(FILE_PATH);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            writer = new FileWriter(file);
            if (!Strings.isNullOrEmpty(json)) {
                writer.write(json);
            }
            writer.flush();
            isSuccess = true;
        } catch (Exception e) {
            isSuccess = false;
        } finally {
            if (null != writer) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return isSuccess;
    }

    @WorkerThread
    private String getLocalTokenList() {
        try {
            BufferedReader bfr = new BufferedReader(new FileReader(FILE_PATH));
            String line = bfr.readLine();
            StringBuilder builder = new StringBuilder();
            while (line != null) {
                builder.append(line);
                builder.append("\n");
                line = bfr.readLine();
            }
            bfr.close();
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<TokenList.ERCToken> getTokenList() {
        List<TokenList.ERCToken> tokenList =
                JSON.parseArray(getLocalTokenList(), TokenList.ERCToken.class);
        return tokenList;
    }

    @WorkerThread
    public String getLocalTokenListAsset() {
        try {
            // 创建字符流对象
            InputStream open = MyApplication.getInstance().getAssets().open(FILE_PATH);
            Reader reader = new InputStreamReader(open);
            // 创建字符串拼接
            StringBuilder builder = new StringBuilder();
            // 读取一个字符
            int read = reader.read();
            // 能读取到字符
            while (read != -1) {
                // 拼接字符串
                builder.append((char) read);
                // 读取下一个字符
                read = reader.read();
            }
            // 关闭字符流
            reader.close();
            return builder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 通过 address 获取到 ERCToken 实体类
     *
     * @param address token 地址
     * @return
     */
    public TokenList.ERCToken getTokenByAddress(String address) {
        List<TokenList.ERCToken> tokenList =
                JSON.parseArray(getLocalTokenList(), TokenList.ERCToken.class);
        for (TokenList.ERCToken token : tokenList) {
            if (!Strings.isNullOrEmpty(token.address) && !Strings.isNullOrEmpty(address)) {
                if (token.address.equalsIgnoreCase(address)) {
                    return token;
                }
            }
        }
        return null;
    }
}
