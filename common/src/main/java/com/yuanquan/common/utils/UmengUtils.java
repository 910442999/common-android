package com.yuanquan.common.utils;

public class UmengUtils {

    /**
     * Sign=MD5(${http_method}${url}${post-body}${app_master_secret})
     *
     * @return
     */
    public static String sign(String httpMethod, String url, String postBody, String appMasterSecret) {
        String md5 = MD5.md5(String.format("%s%s%s%s", httpMethod, url, postBody, appMasterSecret));
        return md5;
    }
}
