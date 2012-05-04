package com.zatacka;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public abstract class ZatackaView extends SurfaceView implements SurfaceHolder.Callback {
	public static int FRAME_DELAY = 50;
	public static int SLOW_FRAME_DELAY = 20;
	//public static final int BASE_X = 320;
	//public static final int BASE_Y = 480;
	int screenW, screenH;
	public static int half_screenW, half_screenH;
	public static float window_aspect;
	long sleepTime = 30;
	public ZatackaThread zthread;
	public Context mContext;
	protected Handler mHandler;
	TextView ready,score,latency;
	ImageView leftArr,rightArr;
	boolean debug=false;
	
	public long gameTime=100;
	
	//Player Index
	int index=0;
	public ZatackaView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		getHolder().addCallback(this);
		mHandler= new Handler();
		zthread = new ZatackaThread(this, getHolder(), mContext,mHandler);
		this.setFocusableInTouchMode(true);
		this.setFocusable(true);
		//Log.v("Focus",this.requestFocus()+"");
		
	}
	 
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	//	Log.v("Touch at","x :"+event.getX() + ", y :"+ event.getY());
		debug=true;
	    return super.onTouchEvent(event);
	}
	
	@Override 
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		switch(keyCode){
		
		case KeyEvent.KEYCODE_BACK:
		
			mHandler.post(
					new Runnable() {
						
						@Override
						public void run() {
							Activity act=	(Activity)mContext;
							act.setContentView(R.layout.menu);
						}
					}		
					);
		return true;
		}
		 return super.onKeyDown(keyCode, event);
	};
	
	
	public void setViews(TextView text, ImageView leftArr, ImageView rightArr,
			TextView score,TextView latency) {
		 setViews( text,  leftArr,  rightArr, score);
		 this.latency=latency;
	}
	public void setViews(TextView text, ImageView leftArr, ImageView rightArr,
			TextView score) {
		
		this.ready=text;
		this.leftArr=leftArr;
		this.rightArr=rightArr;
		this.score=score;
		setScoreBoard();
		leftArr.setOnTouchListener(
		new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				if(action==MotionEvent.ACTION_DOWN)
					left=true;
				else if(action==MotionEvent.ACTION_UP)
					left=false;
				return true;
			}
		}
		);
		rightArr.setOnTouchListener(
				new OnTouchListener() {
					
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						int action = event.getAction();
						if(action==MotionEvent.ACTION_DOWN)
							right=true;
						else if(action==MotionEvent.ACTION_UP)
							right=false;
						return true;
					}
				}
				);
	}
	@Override
	public  abstract void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) ;

	@Override
	public abstract void surfaceCreated(SurfaceHolder holder) ;

	@Override
	public abstract void surfaceDestroyed(SurfaceHolder holder);
	public  void update(int i){
		// detect collision
		if (!addMove(snakes[i])) // this snake dies
		{
			// kill the snake
			snakes[i].kill();
			// decrease # of alive snakes
			alive--;
			// update the scores
			for (int j = 0; j < players; j++) {
				// each living snake gets a point
				if (!snakes[j].isDead())
					scores[j]++;
			}
			// Redraw ScoreBoard
			setScoreBoard();
			
			//Check if game is over
			for (int j=0;j<players;j++)
			{
				if(scores[j]>=maxScore) // j = winner
				{
					theEnd=true;
					 zthread.running=false;
					winner=j;
				}
			}
			// if only 1 snake left
			if (alive <= 1) {
				zthread.running = false;
				if (!theEnd) {
					nextRound();
					reset=4;
				}
			}
		}
		
	};
	
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        screenW = xNew;
        screenH = yNew;
       // Log.v("Width,Height",screenW +","+screenH );
	}
	
	// start new game round
	public  abstract void nextRound();
	 void handleAllEvents() {
			
		}
	
	
	public class ZatackaThread extends Thread {
		public SurfaceHolder holder;
		public ZatackaView zView;
		protected Canvas canvas;
		Handler mHandler;
		boolean running = false;
		public static final int STATE_START = 1;
		public static final int STATE_PAUSE = 2;
		public static final int STATE_READY = 3;
		public static final int STATE_RUNNING = 4;
		public static final int STATE_GAMEOVER = 5;

		public int mMode;

		public ZatackaThread(ZatackaView zView, SurfaceHolder holder,
				Context context, Handler handler) {
			super("ZThread");
			this.zView = zView;
			this.holder = holder;
			this.mHandler = handler;
		}

		protected Paint mBitmapPaint = new Paint();
		protected Paint mTransPaint = new Paint();
		Bitmap black;
		
		
		public Bitmap loadBitmap(Drawable sprite, int width, int height,
				Config bitmapConfig) {

			Bitmap bitmap = Bitmap.createBitmap(width, height, bitmapConfig);
			Canvas canvas = new Canvas(bitmap);
			sprite.setBounds(0, 0, width, height);
			sprite.draw(canvas);
			return bitmap;
		}
		
		@Override
		public void run() {

			long beginTime = 0;
			long diffTime = 0;
			int framesSkipped = 0;

			while (running) {
				// update game
				// draw
				canvas = null;
				try {
					canvas = this.holder.lockCanvas();

					beginTime = System.currentTimeMillis();
					synchronized (holder) {
						if (reset > 0) {
							if (canvas != null)
								synchronized (canvas) {
							canvas.drawColor(Color.BLACK);
								}
							reset--;
						}
							else{
						this.handleUserEvents();
						handleAllEvents();
						if (canvas != null)
							synchronized (canvas) {
								this.Draw(canvas);
							}
						
							}
					}
					long endtime=System.currentTimeMillis();
						try {
							sleep((gameTime-endtime+beginTime)>0?(gameTime-endtime+beginTime):0);
						} catch (InterruptedException e) {
							
						}
					
					
					

				} finally {
					if (canvas != null) {
						this.holder.unlockCanvasAndPost(canvas);
					}
				}

			}
		}

		
		// Update the game
		public void handleUserEvents() {

			//AI BOT movement
			for (int i = 1; i < bots; i++) {
				if(snakes[i].isBot() && playField!=null && playField.length>0){
					snakes[i].moveBot(screenW, screenH, playField);
				}
			}
			
			// handle touch events
			if (left) 
				snakes[index].moveLeft();
			else if (right) 
				snakes[index].moveRight();
			

			// move the snakes only is the waitcounter reached 0
			if (waitCounter <= 0) {
				// update the snake's x and y coördinates
				for (int i = 0; i < players; i++) {
					if(!snakes[i].isremote)
					snakes[i].move();
					//Log.v("Snake-"+i," : x ="+snakes[i].getX()+ " y =  "+snakes[i].getY());
				}
				
			}
		}
		
		public void Draw(Canvas canvas) {
			if(waitCounter>0)
			{
				//Wait for 15 iterations before every round
					for(int i=0; i<snakes.length; i++)
						canvas.drawRect(snakes[i].getY(), snakes[i].getX(), snakes[i].getY()+snakes[i].getWidth(), snakes[i].getX()+snakes[i].getWidth(), colors[i % 6]);
				waitCounter--;
			}
			else
			{	
				for (int i = 0; i < snakes.length; i++) {
					// only draw if necessary (if no snakehole)
					if (!snakes[i].isDead() && !snakes[i].dontDraw()) {
						// draw the Snake
						//canvas.drawCircle(snakes[i].getX(), snakes[i].getY(),
						//		2, colors[i % 6]);
						canvas.drawRect(snakes[i].getY(), snakes[i].getX(), snakes[i].getY()+snakes[i].getWidth(), snakes[i].getX()+snakes[i].getWidth(), colors[i % 6]);
						update(i);
					}
				 
				}
			}
		}

		


		public void onPause() {
			synchronized (holder) {
				if (mMode == STATE_RUNNING)
					// zThread.Save();
					mMode = STATE_PAUSE;
				// mSensor.OnPause();
			}

		}

		@Override
		public void destroy() {
			boolean retry = true;
			this.running = false;
			while (retry) {
				try {
					this.join();
					retry = false;
				} catch (InterruptedException e) {
				}
			}

		}

		public void onResume() {
			synchronized (holder) {
				if (mMode == STATE_PAUSE) {
					// zthread.Load();
					mMode = STATE_RUNNING;
				}
			}
		}

	}

	// start a new game
	public void restart() {
		// reset the scores
		scores = new int[players];
		// reset the winner
		winner = 0;
		// set flags
		theEnd = false;
		// start a new round
		nextRound();
	}
	
	
	// Add snakes position to the play field
	protected boolean addMove(Snake s) {
		Point pnt;
		int x = s.getX() - 2;
		int y = s.getY() - 2;
		try {
			for (int i = x; i <= x + s.getWidth(); i++) {
				for (int j = y; j <= y + s.getWidth(); j++) {
					pnt = new Point(i, j);
					// if the position is occupied and it is not one of the
					// latest moves
					// of this snake --> collision
					if (!s.contains(pnt) && playField[i][j] == true) {
						// --> collision (with a snake)
						Log.v("Dead : Snake-"+Colors[s.getIndex()], "x :"+s.getY() + ", y :"+ s.getX());
						return false;
					}
					// set occupied to true for this position
					playField[i][j] = true;
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			// --> collision (with a wall)
			Log.v("Dead : Snake-"+Colors[s.getIndex()], "x :"+s.getY() + ", y :"+ s.getX());
			return false;
		}
		return true;
	}
	public void InitPaints() {
		colors = new Paint[6];
		Colors=new String[6];
		// mBitmapPaint.setARGB(255, 255, 255, 255);
		// mBitmapPaint.setFilterBitmap(false);
		// mTransPaint.setARGB(100, 30, 200, 220);
		for (int i = 0; i < 6; i++)
			colors[i] = new Paint();
		colors[0].setColor(-16776961);// Blue
		Colors[0]="blue";
		colors[1].setColor(-65536); // Red
		Colors[1]="red";
		colors[2].setColor(-16711936);// Green
		Colors[2]="green";
		colors[3].setColor(-1);// White
		Colors[3]="white";
		colors[4].setColor(-256); // Yellow
		Colors[4]="yellow";
		colors[5].setColor(-7829368); // Grey
		Colors[5]="grey";

	}
	//Update ScoreBaord
	public void setScoreBoard(){
		 StringBuilder sb=new StringBuilder();
		sb.append("Scoreboard : <br />");
		for (int j=0;j<players;j++)
		{
			sb.append("<font color=\""+Colors[j] +"\">"+scores[j]+"</font>&nbsp;&nbsp;");
		}
		final String scre=sb.toString();
		mHandler.post(
				new Runnable() {
					
					@Override
					public void run() {
						//ready.setVisibility(View.VISIBLE);
						//ready.setText("Round "+(++round));
						score.setText(Html.fromHtml(scre));
						//Log.v("Score",scre);
					}
				}		
				);
	}
	
	
	protected int reset=0;
	// 2-dimensional array holding the positions of he playfield which are
	// occupied
	protected boolean playField[][];
	// array with the snakes
	protected Snake[] snakes;
	protected String[] Colors;
	// key events
	protected boolean left;
	protected boolean right;
	// the scores of the players
	protected int scores[];
	//No of rounds
	protected int round=0;
	// the colors of the snakes
	protected Paint[] colors;
	// counter to wait for some loops at the start of each round
	protected int waitCounter;
	// fullscreen or not?
	protected boolean fullscreen;
	// # of players
	protected int players;
	// # of bots
	protected int bots;
	// # of players alive
	protected int alive;
	// score needed to win the game
	protected int maxScore;
	// the winner of the game
	protected int winner;
	// gamespeed (0=low,1=normal,2=high)
	protected int speed;
	// turnspeed (0=low,1=normal,2=high)
	protected int turnSpeed;
	// amount of holes (0=low,1=normal,2=high)
	protected int holes;

	// --game status flags--
	// determines whether the playfield needs to be redrawn or not
	protected boolean redraw;
	// game started flag
	protected boolean started;
	// game over flag
	protected boolean theEnd;

	

}
