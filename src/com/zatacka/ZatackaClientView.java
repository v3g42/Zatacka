package com.zatacka;

import java.util.concurrent.PriorityBlockingQueue;



import android.content.Context;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.ImageView;
import android.widget.TextView;



public class ZatackaClientView  extends ZatackaView {
	GameClient gameclient;
	PriorityBlockingQueue<PacketData> ppQueue;

	boolean set,first;

	public ZatackaClientView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}

	public void Init(GameClient gameclient, int players,int index) {
		
		this.gameclient = gameclient;
		ppQueue=gameclient.peer.ppQueue;
		InitPaints();
		// total players
		this.players = players;
		//My Index
		this.index=index;

		holes = 1;
		turnSpeed = 1;
		// reset the scores
		scores = new int[players];
		
		// waitCounter --> wait this many game loops to start moving
		waitCounter = 15;
		sleepTime=50;
		// reset the winner
		winner = 0;
		// set flags
		theEnd = false;
		maxScore = 15;
	//	gameclient.peer.rThread=new Receiver();
	//	gameclient.peer.rThread.start();
		// start a new round
		nextRound();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		synchronized (holder) {
			if (zthread.getState() == Thread.State.TERMINATED) {
				zthread = new ZatackaThread(this, getHolder(), mContext,
						new Handler());
				//	zthread.InitGraphics();
				zthread.running = true;
				zthread.start();
			} else {
				//zthread.InitGraphics();
				zthread.running = true;
				// zthread.start();
			}

		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		synchronized (holder) {
			// Log.i("GameView", "surfaceDestroyed");
			zthread.onPause();
			zthread.destroy();
			// gameThread.mMode=GameThread.STATE_START;
		}

	}

	@Override
	void handleAllEvents(){
		readClientPackets();
		updateserver();
	} 
	@Override
	public void update(int i) {

		//	super.update(i);

	}

	@Override
	public void nextRound() {
		
		Log.v("Client next round",round+"");
		// declare new snakes
		snakes = new Snake[players];
		// reset key events
		left = false;
		right = false;
		// initialize the snakes

		
		
		// Players
		for (int i = 0; i < players; i++){
			snakes[i] =new Snake( i, colors[i], 4,  turnSpeed,  screenW, screenH,holes,i!=index);
		}
		
		set =false;
		first=false;
		
		// set redraw flag
		redraw = true;
		//start new thread
		zthread=new ZatackaThread(this,getHolder() , mContext, mHandler);
		zthread.running=true;
		zthread.start();

		// waitCounter --> wait this many game loops to start moving
		waitCounter = 25;
		// reset alive
		alive = players;
		//Clear screen constant
		reset=4;
		
	}

	void readClientPackets() {
		while (!ppQueue.isEmpty()) {

			PacketData pp = ppQueue.poll();
			try{

			int pindex = pp.index;
			int running=pp.running;
			int sround=pp.round;
			int sscore=pp.score;
			
			//DEBUG -- print packet values when clicked
			if(debug){
				
				Log.v("Client",pp.toString());
				debug=false;
				Log.v("Client","packets locked,unlocked"+PacketData.getCounts());
			}
			//set round info  the first time in a round
			if(!first){
				first=true;
				round=sround;
				scores[pindex]=pp.score;
				setScoreBoard();
			}
			//set the coordinates of the local snake only the first time in a round
				if(pindex!=this.index)
			snakes[pindex].getPacket(pp,false);
			else {
				if(!set){
				snakes[pindex].getPacket(pp,false);	
				set = true;
				Log.v("Client","round start pos : "+pp.toString());
			} else
				snakes[pindex].dead=(pp.dead==1);
			}
			
				//update round from server
				if(round<sround){
					zthread.running = false;
					// remove all previous round messages
					gameclient.peer.clearQueue();
					if (!theEnd) {
						nextRound();
						round=sround;
					}
				}
			
			
			if(scores[pindex]!=sscore)
				setScoreBoard();
			theEnd=(pp.theEnd==1);
			}
			catch(Exception e){
				Log.v("client",e.toString());
			}
			finally{PacketData.checkIn(pp);}
		}
	}

	void updateserver() {
		
		PacketData pd =  PacketData.checkOut();
		pd.address = gameclient.server;
		pd.port = gameclient.port;
		Message msgsend = Message.obtain();
		
		snakes[index].setPacket(pd);
		pd.players=(short) players;
		pd.score=(short) scores[index];
		pd.round=(short) round;
		msgsend.obj = pd;
		gameclient.peer.mNetworkHandler.sendMessage(msgsend);

	}

}
