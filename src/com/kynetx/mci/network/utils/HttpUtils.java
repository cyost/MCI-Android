package com.kynetx.mci.network.utils;


import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.LayeredSocketFactory;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

public class HttpUtils {

	public static HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);

            SSLSocketFactory sf = new MciSSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            
            HttpConnectionParams.setConnectionTimeout(params, 10000);
            HttpConnectionParams.setSoTimeout(params, 15000);

            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));

            ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

            return new DefaultHttpClient(ccm, params);
        } catch (Exception e) {
            e.printStackTrace();
            return new DefaultHttpClient();
        }
    }
    
	public static void workAroundReverseDnsBugInHoneycombAndEarlier(HttpClient client) {
        // Android had a bug where HTTPS made reverse DNS lookups (fixed in Ice Cream Sandwich) 
        // http://code.google.com/p/android/issues/detail?id=13117
        SocketFactory socketFactory = new LayeredSocketFactory() {
            SSLSocketFactory delegate = SSLSocketFactory.getSocketFactory();
            public Socket createSocket() throws IOException {
                return delegate.createSocket();
            }
            public Socket connectSocket(Socket sock, String host, int port,
                    InetAddress localAddress, int localPort, HttpParams params) throws IOException {
                return delegate.connectSocket(sock, host, port, localAddress, localPort, params);
            }
            public boolean isSecure(Socket sock) throws IllegalArgumentException {
                return delegate.isSecure(sock);
            }
            public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
                injectHostname(socket, host);
                return delegate.createSocket(socket, host, port, autoClose);
            }
            private void injectHostname(Socket socket, String host) {
                try {
                    Field field = InetAddress.class.getDeclaredField("hostName");
                    field.setAccessible(true);
                    field.set(socket.getInetAddress(), host);
                } catch (Exception ignored) {
                }
            }
        };
        client.getConnectionManager().getSchemeRegistry()
                .register(new Scheme("https", socketFactory, 443));
    }

	public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
                        if (useIPv4) {
                            if (isIPv4) 
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim<0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }
}
