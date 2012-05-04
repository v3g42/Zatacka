package com.zatacka;


import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class GameRoom {

	Handler handler;
	Peer peer;
	int bots;
	boolean running;
	 List<InetAddress> players;
	 List<Integer> ports;
	 Context context;
	 Activity activity;
	 zatacka z;
	 Server server;
	 ZatackaServerView zView;
	public GameRoom(Handler handler, List<InetAddress> players,List<Integer> ports,Context context,zatacka z){
		this.handler=handler;
		this.z=z;
		//rtThread=new Receiver();
		 server=new Server();
		peer=new Peer(true, 1235);
		peer.start();
			this.players=players;
			this.ports=ports;
			this.context=context;
			server.start();
			//rtThread.start();
	}
	
	class Server extends Thread{
		boolean exit=false;
		@Override
		public void run() {
			handler.post(new Runnable(){
				@Override
				public void run() {
					z.serverStatus.setText("Listening on"+peer.SERVERIP);		
				}
			});
			
			
		while(!exit){
			while(!peer.ppQueue.isEmpty()){
				PacketData pp = peer.ppQueue.poll();
				if(pp.address!=null && !players.contains(pp.address)){
					players.add(pp.address);
					ports.add(pp.port);
					AddRow("Player"+pp.address.toString());
					Log.v("Player connected",pp.address.toString());
				}
				PacketData.checkIn(pp);
			}
				for(int i=0;i<players.size();i++){
					PacketData pd=PacketData.checkOut();
					pd.running=(byte) (running?1:0);
					pd.players=(short)(bots+1+players.size());
				
					pd.address=players.get(i);
					pd.port=ports.get(i);
					pd.index=(short) (i+bots+1);
					Message msgsend =Message.obtain();
					msgsend.obj = pd;
					peer.mNetworkHandler.sendMessage(msgsend);
					//Log.v("Player",players.get(i).toString());
				}
				if(running)exit=true;
				else{
				try {
					sleep(100);
				} catch (InterruptedException e) {}
				}
			
		}
		//Game Started--exit flag
		startGame();
		
		
		
		}
		
	}
	void startClicked(int bots){
		
		this.bots=bots;
		this.running=true;
		//Set the running flag and send the packet to all clients
	
	}
	void startGame(){
		//clear all previous packets
		peer.clearQueue();
		
		final GameRoom gr=this;
		final int botcount=this.bots;
	handler.post(new Runnable() {
		
		@Override
		public void run() {
			
	//		 rtThread.mZatackaHandler.getLooper().quit();
				z.setContentView(R.layout.gameserver);
				zView=(ZatackaServerView)z.findViewById(R.id.zview);
				z.text=(TextView) z.findViewById(R.id.text);
				z.score=(TextView) z.findViewById(R.id.score);
				z.left = (ImageView) z.findViewById(R.id.left);
				z.right = (ImageView) z.findViewById(R.id.right);
				zView.Init(gr,botcount);
				zView.setViews(z.text,z.left,z.right,z.score);
		}
	});
	
	
	}
	public void AddRow(final String txt)
	{
		handler.post(new Runnable(){

			@Override
			public void run() {
				TableRow row = new TableRow(context);
				TextView t = new TextView(context);
		        t.setText(txt);	
		        row.addView(t);
		        z.table.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		    
			}});
		
	}
}
