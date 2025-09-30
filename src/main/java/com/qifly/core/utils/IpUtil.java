package com.qifly.core.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public final class IpUtil {


    /**
     * TODO 可配置
     */
    static String localIp;

    static {
        if (localIp == null || localIp.isEmpty()) {
            loadIps();
        }
    }

    static void loadIps() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress instanceof Inet4Address
                            && inetAddress.isSiteLocalAddress()
                            && !inetAddress.isLoopbackAddress()) {
                        localIp = inetAddress.getHostAddress();
                        return;
                    }
                }
            }
        } catch (SocketException ignored) {
        }
    }

    public static String getLocalIp() {
        return localIp;
    }
}
