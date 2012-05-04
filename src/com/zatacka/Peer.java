package com.zatacka;

import java.io.IOException;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public  class Peer  extends Thread{
	public  int PORT;
	public   String SERVERIP;
	public boolean ishost=true;
	Hashtable<Integer, Long> sentPackets;
	Queue<Integer> receivePackets;
	long RTT=50;
	//Network Constants

	Handler mHandler,mNetworkHandler;
	//ReceiveThread rThread;
	PriorityBlockingQueue<PacketData> ppQueue,ppSend;

	//State
	public static final int NOT_STARTED=-1; //NOT HOSTED
	public static final int NOT_CONNECTED=0; //HOSTED BUT NOT CONNECTED
	public static final int CONNECTED=1;  
	public static final int RETRY=2;
	public static final int TIMEOUT=3;
	public int state=NOT_STARTED;

	public int retry=0;
	//time 
	//public float time=0,ping=0;

	//Reliablity
	public int sequence=0;
	public int remotesequence=0;
	long expirationTime=10000; // 10 second
	//Network

	static byte[] buf=new byte[120];
	static byte[] buf_rec=new byte[23];
	static byte[] buf_send=new byte[23];
	static DatagramPacket request = new DatagramPacket(buf_rec, buf_rec.length);
	static DatagramPacket reply =new DatagramPacket(buf_send, buf_send.length);

	DatagramSocket socket;

	//Server
	InetAddress servAddr;
	int servPort;

	//Client
	InetAddress clientAddr;
	int clientPort;

	boolean running=true;
	public Peer(boolean ishost,int PORT) 
	{
		super("thread-"+ishost);
		sender = new Sender();
		ppQueue = new PriorityBlockingQueue<PacketData>(50);
		ppSend= new PriorityBlockingQueue<PacketData>(50);
		sentPackets=new Hashtable<Integer, Long>(100);
		receivePackets=new LinkedList<Integer>();
		servPort=PORT;
		try {
			socket = new DatagramSocket(PORT);
		} catch (SocketException e) {
			Log.e("socket","creation");
		}
		state=NOT_STARTED;
		this.ishost=ishost;
		if(ishost)SERVERIP=getLocalIpAddress();

	}

	public void SendData(PacketData data_s)
	{
		reply.setAddress(data_s.address);
		reply.setPort(data_s.port);
		data_s.writePacketData(buf_send);
		try {
			socket.send(reply);
		} catch (IOException e) {
			Log.e("Client Send","Client"+e);
		}
	}
	public PacketData ReceiveData() throws SocketTimeoutException
	{
		PacketData data_r=PacketData.checkOut();
		try { 

			socket.setSoTimeout(100);
			request.setLength(buf_rec.length);
			socket.receive(request);			

			data_r.readPacketData(buf_rec );
			data_r.address=request.getAddress();
			data_r.port=request.getPort();
			return data_r;
		} 
		catch(SocketTimeoutException e)
		{
			PacketData.checkIn(data_r);
			throw e;
		}

		catch (IOException e) {
			Log.e(ishost+":receiving -IO","IO"+e.toString());
			PacketData.checkIn(data_r);
			return null;
		}
		catch (Exception e) {
			Log.e(ishost+":receiving- Error","Error"+e);
			PacketData.checkIn(data_r);
			return null;
		}
	}

	private String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) { return inetAddress.getHostAddress().toString(); }
				}
			}
		} catch (SocketException ex) {
			Log.e("Cant find local Ip Address", ex.toString());
		}
		return null;
	}







	@Override
	public void run() {
		sender.start();
		int counter = 0;
		while (running) {
			try {
				PacketData data_r= ReceiveData();


				if(data_r!=null){
					registerReceivePacket(data_r);	
					ppQueue.offer(data_r);
				}
			} catch (SocketTimeoutException e) {
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("Receieve", e.toString());
			}



		}
	}



	Sender sender;
	class Sender extends Thread {
		public Sender() {
			super("sender-"+(ishost?"server":"client"));
		}

		@Override
		public void run() {
			while (running) {
				if(!ppSend.isEmpty()){
					PacketData data_s=ppSend.poll();
					registerSendPacket(data_s);
					try{
						SendData(data_s);
					} catch(Exception e){
						Log.v("Exception in "+sender.getName(),e.toString());
					} finally{
						PacketData.checkIn(data_s);
					}
				}
			}

		}
	}

	void clearQueue(){

		while(!ppQueue.isEmpty()){
			PacketData pd=ppQueue.poll();
			PacketData.checkIn(pd);
		}
	}

	synchronized void  registerSendPacket(PacketData pd){
		long time=System.currentTimeMillis();
		pd.packetid=(short) ++sequence;

		Enumeration<Integer> e =sentPackets.keys();
		while(e.hasMoreElements()){
			Integer i=(Integer) e.nextElement();
			if(sentPackets.get(i)-time > expirationTime){
				sentPackets.remove(i);
			}
		}
		sentPackets.put(sequence,time);
		int ack=-1;
		if(!receivePackets.isEmpty())
			ack=receivePackets.poll();
		pd.ack=(short) ack;
	}

	synchronized void registerReceivePacket(PacketData data_r) {
		long time=System.currentTimeMillis();
		if(remotesequence<data_r.packetid)
			remotesequence=data_r.packetid;
		receivePackets.offer((int) data_r.packetid);
	}
}
