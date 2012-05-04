package com.zatacka;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;

public class ZatackaServerView extends ZatackaView {
	GameRoom gameroom;
	PriorityBlockingQueue<PacketData> ppQueue;

	public ZatackaServerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}

	public void Init(GameRoom gameroom, int bots) {
		this.gameroom = gameroom;
		ppQueue=gameroom.peer.ppQueue;
		InitPaints();
		// bots count
		this.bots = bots;
		// total players
		players = bots + gameroom.players.size() + 1;
		holes = 1;
		turnSpeed = 1;
		// reset the scores
		scores = new int[players];
		// reset the winner
		winner = 0;
		// set flags
		theEnd = false;
		maxScore = 15;
		sleepTime=100;
		//gameroom.peer.rThread=new Receiver();
		//gameroom.peer.rThread.start();
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
				//zthread.InitGraphics();
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
		for(int i=0;i<players;i++)
			updateclient(i);
		
		
	} 
	@Override
	public void update(int i) {
		//if(!snakes[i].isremote)
		super.update(i);
	}

	@Override
	public void nextRound() {

		//increment round
		round++;
		// reset the playfield
		playField = new boolean[screenH][screenW];
		// declare new snakes
		snakes = new Snake[players];
		// reset key events
		left = false;
		right = false;
		// initialize the snakes
		int i = 0;
		// Host snake
		snakes[i] = new Snake(i, colors[i], 4, turnSpeed, holes, false,
				screenW, screenH);
		// Bots
		for (i = 1; i < bots + 1; i++)
			snakes[i] = new Snake(i, colors[i], 4, turnSpeed, holes, true,
					screenW, screenH);
		// Clients
		for (int j = 0; i < players; i++, j++){
			snakes[i] = new Snake(i, colors[i], 4, turnSpeed, holes,
					screenW, screenH, gameroom.players.get(j),
					gameroom.ports.get(j));
			snakes[i].isremote=true;
		}

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
		;
	}

	void readClientPackets() {

		while (!ppQueue.isEmpty()) {
			PacketData pp = ppQueue.poll();

				int pindex = pp.index;
				int cround=pp.round;
				// remove all previous round messages
				if(round>cround)
					gameroom.peer.clearQueue();
				//can't be server index
				if(pindex>0 && cround==round)
				snakes[pindex].getPacket(pp,true);
				if(debug){
					
					Log.v("Server",pp.toString());
					debug=false;
				}
			PacketData.checkIn(pp);
		}
	}

	void updateclient(int index) {
		

		for (int i = 0; i < gameroom.players.size(); i++) {
			//if(index!=(i+bots+1)){
			PacketData pd=PacketData.checkOut();
			Message msgsend = Message.obtain();
			snakes[index].setPacket(pd);
			pd.score= (short) scores[index];
			pd.running=(byte) ((zthread.running)?1:0);
			pd.round=(short) round;
			pd.players=(short) players;
			pd.theEnd=theEnd ? (byte) 1 : (byte) 0;
			msgsend.obj = pd;
			pd.address = gameroom.players.get(i);
			pd.port = gameroom.ports.get(i);
			gameroom.peer.mNetworkHandler.sendMessage(msgsend);
			//}
		}
		//PacketData.checkIn(pd);
	}

}
