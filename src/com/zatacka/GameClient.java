package com.zatacka;

import java.net.InetAddress;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class GameClient {

	Handler handler;
	Peer peer;
	boolean running = true;
	Client client;
	InetAddress server;
	int port = 1235;
	zatacka z;
	ZatackaClientView zView;
	public GameClient(Handler handler, InetAddress server, int port,zatacka z) {
		this.handler = handler;
		this.server = server;
		this.port = port;
		this.z=z;
		//rtThread = new Receiver();
		client=new Client();
		peer = new Peer(false, 1235);
		peer.start();
		client.start();
		//rtThread.start();
	}

	class Client extends Thread{
		boolean exit=false,connected=false;
		@Override
		public void run() {
			PacketData pd = PacketData.checkOut();
			pd.address = server;
			pd.port = port;
			while (peer.mNetworkHandler == null) {
				try {
					sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			while(!exit){
				Message msgsend = Message.obtain();
				msgsend.obj = pd;
				peer.mNetworkHandler.sendMessage(msgsend);
				
				
				while(!peer.ppQueue.isEmpty()){
					PacketData pp = peer.ppQueue.poll();
				if (pp.running == 1){
					startGame(pp);
					peer.clearQueue();
					exit=true;
				}
				
				}
				if(!connected){
					
					handler.post(new Runnable() {
						@Override
						public void run() {
							 z.clientStatus.setText("Connected:");
							 connected=true;
						}
					});
				}
				
				try {
					sleep(100);
				} catch (InterruptedException e) {}
			}
		}
	}
	void startGame(final PacketData pd) {
		final GameClient gct=this;
		if(pd.players >0 && pd.index>0 && pd.index < pd.players ){
		handler.post(new Runnable() {
			@Override
			public void run() {
				z.setContentView(R.layout.gameclient);
				zView=(ZatackaClientView)z.findViewById(R.id.zview);
				z.text=(TextView) z.findViewById(R.id.text);
				z.score=(TextView) z.findViewById(R.id.score);
				z.left = (ImageView) z.findViewById(R.id.left);
				z.right = (ImageView) z.findViewById(R.id.right);
				z.latency=(TextView) z.findViewById(R.id.latency);
				Log.v("Client started game"," Count: "+pd.players +", Index :" +pd.index);
				zView.Init(gct,pd.players,pd.index);
				zView.setViews(z.text,z.left,z.right,z.score,z.latency);
				
			}
		});
		} else {
			Log.v("client","error in connection"+pd.toString());
			handler.post(new Runnable() {
				@Override
				public void run() {
					 z.clientStatus.setText("Error in connection:"+ pd.toString());
				}
			});
			
		}
		
	}

	}
