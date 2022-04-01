package com.miaxis.common.utils;

import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

/**
 * @author Tank
 * @date 2020/10/19 15:20
 * @des
 * @updateAuthor
 * @updateDes
 */
public class MyX509TrustManager  implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }

}
