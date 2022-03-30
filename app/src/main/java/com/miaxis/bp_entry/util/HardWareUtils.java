package com.miaxis.bp_entry.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * @author Admin
 * @version $
 * @des
 * @updateAuthor $
 * @updateDes
 */
public class HardWareUtils {

    /**
     * 通过WiFiManager获取mac地址
     *
     * @param context
     * @return
     */
    public static String getWifiMac(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wi = wm.getConnectionInfo();
        if (wi == null) {
            return null;
        }
        @SuppressLint("HardwareIds") String macAddress = wi.getMacAddress();
        if (macAddress == null) {
            return null;
        }

        if ("02:00:00:00:00:00".equals(macAddress.trim())) {
            return null;
        } else {
            return macAddress.trim().toUpperCase().replace(":", "");
        }
    }

    public static String getDeviceId(Context context) {
        return Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID).toUpperCase().replace("-", "");
    }

    public static String getEthernetMac() {
        String ethernetMac = null;
        try {
            NetworkInterface NIC = NetworkInterface.getByName("eth0");
            byte[] buf = NIC.getHardwareAddress();
            ethernetMac = byteHexString(buf);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ethernetMac;
    }


    /**
     * @return 获取所有有效的网卡
     */
    public static String[] getAllNetInterface() {
        ArrayList<String> availableInterface = new ArrayList<>();
        String[] interfaces = null;
        try {
            //获取本地设备的所有网络接口
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    // 过滤掉127段的ip地址
                    if (!"127.0.0.1".equals(ip)) {
                        if (ni.getName().substring(0, 3).equals("eth")) {//筛选出以太网
                            availableInterface.add(ni.getName());
                        }
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }
        int size = availableInterface.size();
        if (size > 0) {
            interfaces = new String[size];
            for (int i = 0; i < size; i++) {
                interfaces[i] = availableInterface.get(i);
            }
        }
        return interfaces;
    }

    /**
     * 根据adb shell命令获取
     * DNS地址
     *
     * @return
     * @paramname网卡名称
     */
    public static String getLocalDNS(String name) {
        Process cmdProcess = null;
        BufferedReader reader = null;
        String dnsIP = "";
        try {
            cmdProcess = Runtime.getRuntime().exec("getprop dhcp." + name + ".dns1");
            reader = new BufferedReader(new InputStreamReader(cmdProcess.getInputStream()));
            dnsIP = reader.readLine();
            return dnsIP;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                reader.close();
                cmdProcess.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取掩码
     *
     * @return
     */
    public static String getLocalMask() {
        Process cmdProcess = null;
        BufferedReader reader = null;
        String dnsIP = "";
        try {
            cmdProcess = Runtime.getRuntime().exec("getprop dhcp.eth0.mask");
            reader = new BufferedReader(new InputStreamReader(cmdProcess.getInputStream()));
            dnsIP = reader.readLine();
            return dnsIP;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                cmdProcess.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取网关地址
     *
     * @return
     */
    public static String getLocalGate() {
        Process cmdProcess = null;
        BufferedReader reader = null;
        String dnsIP = "";
        try {
            cmdProcess = Runtime.getRuntime().exec("getprop dhcp.eth0.gateway");
            reader = new BufferedReader(new InputStreamReader(cmdProcess.getInputStream()));
            dnsIP = reader.readLine();
            return dnsIP;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                cmdProcess.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getHostIP() {
        String hostIp = null;
        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hostIp;
    }

    /*
     * 字节数组转16进制字符串
     */
    public static String byteHexString(byte[] array) {
        StringBuilder builder = new StringBuilder();
        for (byte b : array) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            builder.append(hex).append(":");
        }
        if (builder.toString().endsWith(":")) {
            return builder.substring(0, builder.length() - 1).toUpperCase();
        } else {
            return builder.toString().toUpperCase();
        }
    }

}
