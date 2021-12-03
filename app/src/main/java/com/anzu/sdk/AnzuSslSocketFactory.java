package com.anzu.sdk;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class AnzuSslSocketFactory extends SSLSocketFactory {
    SSLContext context;
    SSLSocketFactory factory;
    String type;

    public AnzuSslSocketFactory() {
        try {
            init();
        }
        catch (KeyManagementException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    Socket configCipherSuites(Socket socket) {
        if (!(socket instanceof SSLSocket)) {
            return socket;
        }

        SSLSocket sSLSocket = (SSLSocket) socket;
        Set<String> set = getCipherSuites().toSet();
        while (set.size() > 0) {
            try {
                sSLSocket.setEnabledCipherSuites(set.toArray(new String[set.size()]));
                break;
            }
            catch (Throwable th) {
                String message = th.getMessage();
                Iterator<String> it = set.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }

                    String next = it.next();
                    if (message != null) {
                        if (message.toLowerCase().contains(next.toLowerCase())) {
                            set.remove(next);
                            PrintStream printStream = System.out;
                            Object[] objArr = new Object[1];
                            objArr[0] = next;
                            printStream.printf("Cipher suite %s has been removed.%n", objArr);
                            break;
                        }
                    }
                }
            }
        }
        return socket;
    }

    @Override
    public Socket createSocket(String str, int i) throws IOException {
        return configCipherSuites(factory.createSocket(str, i));
    }

    @Override
    public Socket createSocket(String str, int i, InetAddress inetAddress, int i2) throws IOException {
        return configCipherSuites(factory.createSocket(str, i, inetAddress, i2));
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
        return configCipherSuites(factory.createSocket(inetAddress, i));
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress2, int i2) throws IOException {
        return configCipherSuites(factory.createSocket(inetAddress, i, inetAddress2, i2));
    }

    @Override
    public Socket createSocket(Socket socket, String str, int i, boolean z) throws IOException {
        return configCipherSuites(factory.createSocket(socket, str, i, z));
    }

    Cube<String> getCipherSuites() {
        String[] strArr = new String[93];
        strArr[0] = "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA";
        strArr[1] = "SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA";
        strArr[2] = "SSL_DHE_DSS_WITH_DES_CBC_SHA";
        strArr[3] = "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA";
        strArr[4] = "SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA";
        strArr[5] = "SSL_DHE_RSA_WITH_DES_CBC_SHA";
        strArr[6] = "SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA";
        strArr[7] = "SSL_DH_anon_EXPORT_WITH_RC4_40_MD5";
        strArr[8] = "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA";
        strArr[9] = "SSL_DH_anon_WITH_DES_CBC_SHA";
        strArr[10] = "SSL_DH_anon_WITH_RC4_128_MD5";
        strArr[11] = "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA";
        strArr[12] = "SSL_RSA_EXPORT_WITH_RC4_40_MD5";
        strArr[13] = "SSL_RSA_WITH_3DES_EDE_CBC_SHA";
        strArr[14] = "SSL_RSA_WITH_DES_CBC_SHA";
        strArr[15] = "SSL_RSA_WITH_NULL_MD5";
        strArr[16] = "SSL_RSA_WITH_NULL_SHA";
        strArr[17] = "SSL_RSA_WITH_RC4_128_MD5";
        strArr[18] = "SSL_RSA_WITH_RC4_128_SHA";
        strArr[19] = "TLS_DHE_DSS_WITH_AES_128_CBC_SHA";
        strArr[20] = "TLS_DHE_DSS_WITH_AES_128_CBC_SHA256";
        strArr[21] = "TLS_DHE_DSS_WITH_AES_128_GCM_SHA256";
        strArr[22] = "TLS_DHE_DSS_WITH_AES_256_CBC_SHA";
        strArr[23] = "TLS_DHE_DSS_WITH_AES_256_CBC_SHA256";
        strArr[24] = "TLS_DHE_DSS_WITH_AES_256_GCM_SHA384";
        strArr[25] = "TLS_DHE_RSA_WITH_AES_128_CBC_SHA";
        strArr[26] = "TLS_DHE_RSA_WITH_AES_128_CBC_SHA256";
        strArr[27] = "TLS_DHE_RSA_WITH_AES_128_GCM_SHA256";
        strArr[28] = "TLS_DHE_RSA_WITH_AES_256_CBC_SHA";
        strArr[29] = "TLS_DHE_RSA_WITH_AES_256_CBC_SHA256";
        strArr[30] = "TLS_DHE_RSA_WITH_AES_256_GCM_SHA384";
        strArr[31] = "TLS_DH_anon_WITH_AES_128_CBC_SHA";
        strArr[32] = "TLS_DH_anon_WITH_AES_128_CBC_SHA256";
        strArr[33] = "TLS_DH_anon_WITH_AES_128_GCM_SHA256";
        strArr[34] = "TLS_DH_anon_WITH_AES_256_CBC_SHA";
        strArr[35] = "TLS_DH_anon_WITH_AES_256_CBC_SHA256";
        strArr[36] = "TLS_DH_anon_WITH_AES_256_GCM_SHA384";
        strArr[37] = "TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA";
        strArr[38] = "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA";
        strArr[39] = "TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256";
        strArr[40] = "TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256";
        strArr[41] = "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA";
        strArr[42] = "TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384";
        strArr[43] = "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384";
        strArr[44] = "TLS_ECDHE_ECDSA_WITH_NULL_SHA";
        strArr[45] = "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA";
        strArr[46] = "TLS_ECDHE_PSK_WITH_AES_128_CBC_SHA";
        strArr[47] = "TLS_ECDHE_PSK_WITH_AES_256_CBC_SHA";
        strArr[48] = "TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA";
        strArr[49] = "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA";
        strArr[50] = "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256";
        strArr[51] = "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256";
        strArr[52] = "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA";
        strArr[53] = "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384";
        strArr[54] = "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384";
        strArr[55] = "TLS_ECDHE_RSA_WITH_NULL_SHA";
        strArr[56] = "TLS_ECDHE_RSA_WITH_RC4_128_SHA";
        strArr[57] = "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA";
        strArr[58] = "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA";
        strArr[59] = "TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256";
        strArr[60] = "TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256";
        strArr[61] = "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA";
        strArr[62] = "TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384";
        strArr[63] = "TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384";
        strArr[64] = "TLS_ECDH_ECDSA_WITH_NULL_SHA";
        strArr[65] = "TLS_ECDH_ECDSA_WITH_RC4_128_SHA";
        strArr[66] = "TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA";
        strArr[67] = "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA";
        strArr[68] = "TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256";
        strArr[69] = "TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256";
        strArr[70] = "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA";
        strArr[71] = "TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384";
        strArr[72] = "TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384";
        strArr[73] = "TLS_ECDH_RSA_WITH_NULL_SHA";
        strArr[74] = "TLS_ECDH_RSA_WITH_RC4_128_SHA";
        strArr[75] = "TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA";
        strArr[76] = "TLS_ECDH_anon_WITH_AES_128_CBC_SHA";
        strArr[77] = "TLS_ECDH_anon_WITH_AES_256_CBC_SHA";
        strArr[78] = "TLS_ECDH_anon_WITH_NULL_SHA";
        strArr[79] = "TLS_ECDH_anon_WITH_RC4_128_SHA";
        strArr[80] = "TLS_EMPTY_RENEGOTIATION_INFO_SCSV";
        strArr[81] = "TLS_FALLBACK_SCSV";
        strArr[82] = "TLS_PSK_WITH_3DES_EDE_CBC_SHA";
        strArr[83] = "TLS_PSK_WITH_AES_128_CBC_SHA";
        strArr[84] = "TLS_PSK_WITH_AES_256_CBC_SHA";
        strArr[85] = "TLS_PSK_WITH_RC4_128_SHA";
        strArr[86] = "TLS_RSA_WITH_AES_128_CBC_SHA";
        strArr[87] = "TLS_RSA_WITH_AES_128_CBC_SHA256";
        strArr[88] = "TLS_RSA_WITH_AES_128_GCM_SHA256";
        strArr[89] = "TLS_RSA_WITH_AES_256_CBC_SHA";
        strArr[90] = "TLS_RSA_WITH_AES_256_CBC_SHA256";
        strArr[91] = "TLS_RSA_WITH_AES_256_GCM_SHA384";
        strArr[92] = "TLS_RSA_WITH_NULL_SHA256";
        return Cube.from(strArr).where(new Cube.Selection<String>() {
            public boolean predicate(String str, int i) {
                return str.startsWith(type);
            }
        });
    }

    public String[] getDefaultCipherSuites() {
        return getCipherSuites().toArray(String.class);
    }

    public String[] getSupportedCipherSuites() {
        return getCipherSuites().toArray(String.class);
    }

    void init() throws KeyManagementException, NoSuchAlgorithmException {
        String[] strArr = new String[2];
        strArr[0] = "SSL";
        strArr[1] = "TLS";
        String str = Cube.from(strArr).random();
        type = str;
        SSLContext instance = SSLContext.getInstance(str);
        context = instance;
        X509TrustManager r4 = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509CertificateArr, String str) {
                PrintStream printStream = System.out;
                Object[] objArr = new Object[2];
                objArr[0] = Arrays.toString(x509CertificateArr);
                objArr[1] = str;
                printStream.printf("[CLIENT] chain = %s, authType = %s%n", objArr);
            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509CertificateArr, String str) {
                PrintStream printStream = System.out;
                Object[] objArr = new Object[2];
                objArr[0] = Arrays.toString(x509CertificateArr);
                objArr[1] = str;
                printStream.printf("[SERVER] chain = %s, authType = %s%n", objArr);
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };

        SecureRandom secureRandom = new SecureRandom();
        TrustManager[] trustManagerArr = new TrustManager[1];
        trustManagerArr[0] = r4;
        instance.init(null, trustManagerArr, secureRandom);
        factory = context.getSocketFactory();
    }
}
