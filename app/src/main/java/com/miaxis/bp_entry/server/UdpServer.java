package com.miaxis.bp_entry.server;

import android.util.Log;

import com.miaxis.bp_entry.util.StringUtil;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @author ZJL
 * @date 2022/3/30 16:55
 * @des
 * @updateAuthor
 * @updateDes
 */
public class UdpServer implements Runnable {

    UdpListener mUdpListener;

    public UdpServer(UdpListener udpListener) {
        mUdpListener = udpListener;
    }

    @Override
    public void run() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(8888);
            Log.e("runUDPServer:","start");

            while (true) {
                byte data[] = new byte[1024];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                socket.receive(packet);
                byte[] bytes=new byte[packet.getLength()];
                System.arraycopy(packet.getData(),packet.getOffset(),bytes,0,packet.getLength());
                Log.e("bytes:", StringUtil.byte2Str(bytes));
                mUdpListener.recevice(bytes);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface UdpListener{
        void recevice(byte[] bytes);
    }
}
