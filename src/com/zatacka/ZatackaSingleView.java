package com.zatacka;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

public class ZatackaSingleView extends ZatackaView {

	public ZatackaSingleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}

	
//Initialize this mode
	
	public void Init(int bots) {
		InitPaints();
		// bots count
		this.bots = bots;
		// total players
		players = bots + 1;
		holes = 1;
		turnSpeed = 1;
		// reset the scores
		scores = new int[players];
		// reset the winner
		winner = 0;
		// set flags
		theEnd = false;
		maxScore=15;
		sleepTime=50;
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
	public void nextRound() {
		//reset the playfield 
		playField=new boolean[screenH][screenW];
		//declare new snakes
		snakes = new Snake[players];
		//reset key events
		left = false;
		right = false;
		//initialize the snakes
		for(int i=0;i<players;i++)
		{
			snakes[i]=new Snake(i, colors[i],4,turnSpeed,holes, (i!=0), screenW, screenH);
		}
		
		//set redraw flag
		redraw=true;
		//start new thread
		zthread=new ZatackaThread(this,getHolder() , mContext, mHandler);
		zthread.running=true;
		zthread.start();
		//waitCounter --> wait this many game loops to start moving
		waitCounter=15;
		
		//reset alive
		alive=players;
		
	}
}
