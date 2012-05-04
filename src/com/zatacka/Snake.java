package com.zatacka;
import java.net.InetAddress;
import java.util.*;

import android.graphics.Paint;
import android.graphics.Point;

public class Snake
{
	//private attributes
	
	//Player Index
	private int index;
	//current x-position
	private double x;
	//current y-position
	private double y;
	//current direction (in degrees)
	private double direction;
	//color of the snake
	private Paint color;
	//counter for next allowed key event
	private int counter;
	//counter for snake holes
	private int dontDrawCounter;
	//the width of the snake
	private int width;
	//list with the latest occupied positions of the snake
	private LinkedList latest;
	//determines whether the snake is dead or not
	boolean dead;
	//maximum listsize of the 'latest' list
	private int listSize;
	//turnspeed (0=low,1=normal,2=high)
	private int turnSpeed;
	//amount of holes (0=low,1=normal,2=high)
	private int holes;
	//timestampt of the last snakehole
	private long lastHole;
	//AI contants
	public boolean isbot,isremote,ishole;
	private InetAddress ip;
	private int port;
	boolean aiAttack;
	
	static int COWARDNESS_FRONT =85;
	static int  COWARDNESS_SIDES= 40;
	static int  COWARDNESS_DIAGS=60;
	
	
	//Server -local snakes for player and bots
	public Snake(int index,Paint color, int width, int turnSpeed, int holes,boolean isbot,int swidth,int sheight)
	{
		latest=new LinkedList();
		//generate random x and y position
	
	//	y=swidth/2+ (Math.random()-1/2) * swidth/2;
	//	x=sheight/2+ (Math.random()-1/2) * sheight/2;
		y=20+ Math.random() * (swidth-20);
		x=20+ Math.random() * (sheight-20);
		//generate a random direction (in steps of 10 degrees)
		direction=((int) (Math.random()*36))*10;
		//set other attributes
		this.index=index;
		this.color=color;
		this.width=width;
		listSize=width*width*width;
		this.turnSpeed= turnSpeed;
		this.holes=holes;
		this.isbot=isbot;
		
	}
	//Server - client snakes with ip and port
	public Snake(int index,Paint color, int width, int turnSpeed, int holes,int swidth,int sheight,InetAddress ip,int port)
	{
		this( index, color,  width,  turnSpeed,  holes, false, swidth, sheight);
		this.ip=ip;
		this.port=port;
		this.isremote=true;
		
	}
	
	//Client - 
	public Snake(int index,Paint color, int width, int turnSpeed, int holes,int swidth,int sheight,boolean isremote)
	{
		this.isremote=isremote;
		latest=new LinkedList();
		//generate a random direction (in steps of 10 degrees)
		direction=((int) (Math.random()*36))*10;
		//generate a random direction (in steps of 10 degrees)
		direction=((int) (Math.random()*36))*10;
		//set other attributes
		this.index=index;
		this.color=color;
		this.width=width;
		listSize=width*width*width;
		this.turnSpeed= turnSpeed;
		this.holes=holes;
	}
	//get index
	public int getIndex(){
		return index;
	}
	//move to the left
	public void moveLeft()
	{
		//the snake can only move when the counter is 0 or less
		if(counter<=0)
		{
			//increment the direction with 10 degrees
			direction=(direction+10)%360;
			//wait for # loops for next movement
			counter=3-turnSpeed;
		}
	}

	//move to the right
	public void moveRight()
	{
		//the snake can only move when the counter is 0 or less
		if(counter<=0)
		{
			//decrement the direction with 10 degrees
			direction=(direction-10+360)%360;
			//wait for # loops for next movement
			counter=3-turnSpeed;
		}
	}

	//update x- and y- position
	public void move()
	{
		//vertical offset = sinus
		y-=Math.sin(Math.toRadians(direction))*1.5;
		//horizontal offset = cosinus
		x+=Math.cos(Math.toRadians(direction))*1.5;

		//decrease counter
		counter--;
	}

	//getter for the x-position (rounded integer)
	public int getX()
	{
		return (int) Math.round(x);
	}

	//getter for the y-position (rounded integer)
	public int getY()
	{
		return (int) Math.round(y);
	}

	//getter for the color
	public Paint getColor()
	{
		return color;
	}

	//isDead()
	public boolean isDead()
	{
		return dead;
	}
	
	//kill the snake
	public void kill()
	{
		dead=true;
	}

	//does the snake need to be drawn or not --> hole or not
	public boolean dontDraw()
	{
		//if a remote snake holes are calculated on the remote peer
		if(isremote)return ishole;
		
		
		//the snake needs to be drawn (no hole)
		if(dontDrawCounter<=0)
		{
			//generate random number
			//based on variabele holes (0=low,1=normal,2=high)
			//-> the higher 'holes' is, the higher the chance is that
			//   the snake will need to have a hole now
			int rnd = (int) (Math.random()*(40+150*(2-holes)));
			//don't draw a hole when the last hole was less than 1 second ago
			if(rnd==37 && lastHole < (System.currentTimeMillis()-1000))
			{
				dontDrawCounter = getWidth();
				//set lastHole timestamp
				lastHole = System.currentTimeMillis();
			}
			return false;
		}
		//the snake does not need to be drawn (hole)
		else
		{
			//decrease the counter
			dontDrawCounter--;
			return true;
		}
	}

	//is Point p one of the latest occupied positions of this snake?
	public boolean contains(Point p)
	{
		if(latest.contains(p))
		{
			//yes it is
			return true;
		}
		//no it isn't
		//add the point to the list
		latest.add(p);
		//trim the list to listSize
		if (latest.size()>listSize)
		{
			latest.subList(0,latest.size()-listSize).clear();
		}
		return false;
	}

	//getter for the width
	public int getWidth()
	{
		return width;
	}
	public void moveBot(int screenW,int screenH,boolean[][] playField){
		
		//int moveC = think(screenW, screenH, playField);
		int moveC = checkStraightPath(screenW, screenH, playField);
		
		switch(moveC){
		case 1:
			moveLeft();
		case -1:
			moveRight();
		default:{
		double r = (int) (Math.random()*20);
			if (r < 6)
			moveLeft();
		else if (r > 5 && r < 12)
			moveRight();
			
		}
		
		}
		
	}
	public int think(int screenW,int screenH,boolean[][] playField) {
		 for (int i = 0; i < COWARDNESS_FRONT; ++i) {
		        int checkx, checky;
		        boolean result;
		        if (i < COWARDNESS_DIAGS) {
		            /* left diagonal */
		            checkx=(int) (x+i* Math.cos(Math.toRadians(direction-45)));
		            checky = (int) (y+i* Math.sin(Math.toRadians(direction-45)));
		            if (checkx < 0 || checky < 0 || checkx >= screenW || checky >= screenH)
		                return 1;
		            result = playField[checkx][ checky];
		            if (result)
		                return 1;
		            /* right diagonal */
		            checkx=(int) (x+i* Math.cos(Math.toRadians(direction+45)));
		            checky = (int) (y+i* Math.sin(Math.toRadians(direction+45)));
		            if (checkx < 0 || checky < 0 || checkx >= screenW || checky >= screenH)
		                return 1;
		            result = playField[checkx][ checky];
		            if (result)
		                return -1;
		        }
		        if (i < COWARDNESS_SIDES) {
		            /* right side */
		            checkx = (int) (x + i *  Math.cos(Math.toRadians(direction+90)));
		            checky =(int) (y + i *  Math.sin(Math.toRadians(direction+90)));
		            if (checkx < 0 || checky < 0 || checkx >= screenW || checky >= screenH)
		                return 1;
		            result = playField[checkx][ checky];
		            if (result )
		                return -1;
		            /* left side */
		            checkx = (int) (x + i *  Math.cos(Math.toRadians(direction-90)));
		            checky =(int) (y + i *  Math.sin(Math.toRadians(direction-90)));
		            if (checkx < 0 || checky < 0 || checkx >= screenW || checky >= screenH)
		                return 1;
		            result = playField[checkx][ checky];
		            if (result)
		                return 1;
		        }
		        /* front */
		        checkx = (int) (x + i *  Math.cos(Math.toRadians(direction)));
	            checky =(int) (y + i *  Math.sin(Math.toRadians(direction)));
	            if (checkx < 0 || checky < 0 || checkx >= screenW || checky >= screenH)
		            return 1;
	            result = playField[checkx][ checky];
		        if (result)
		            if (result)
		                return -1;
		            else
		                return 1;
		        }
		    return 0;
		}
	
	public int checkStraightPath(int screenW,int screenH,boolean[][] playField) {
		int d=15;
		int angle = 0;
		double tAngle=direction;
		if (tAngle < 0)
			tAngle = tAngle + 360;

		for (int i = 2; i < d + 5; i++) {
			
			//Offsets
			double dy =Math.sin(Math.toRadians(direction))*1.5;
			double dx =Math.cos(Math.toRadians(direction))*1.5;
			
			dx = (int) (dx + x) / 2;
			dy = (int) (dy + y) / 2;
			if (dx < 0)
				dx = 0;
			if (dx > screenW / 2)
				dx = screenW / 2;
			if (dy < 0)
				dy = 0;
			if (dy > screenH / 2)
				dy = screenH / 2;

			int pos = (int) (dx + dy * screenW / 2);
			if (pos == screenH / 2 * screenW / 2)
				pos = screenH / 2 * screenW / 2 - 1;
			if (!playField[(int) dx][(int) dy]) {
				for (int j = 2; j < i; j += 2) {
					 dy =Math.sin(Math.toRadians(direction))*1.5;
					dx =Math.cos(Math.toRadians(direction))*1.5;
					dx = (int) (dx + x) / 2;
					dy = (int) (dy + y) / 2;
					if (dx < 0)
						dx = 0;
					if (dx > screenW / 2)
						dx = screenW / 2;
					if (dy < 0)
						dy = 0;
					if (dy > screenH / 2)
						dy = screenH / 2;

					pos = (int) (dx + dy * screenW / 2);
					if (pos == screenH / 2 * screenW / 2)
						pos = screenH / 2 * screenW / 2 - 1;
					if (!playField[(int) dx][(int) dy]){
						aiAttack = false;
						// fx=dx*2;
						// fy=dy*2;
						// Log.i("Sub Angle", "=" + angle);
						return -1;
					} else {
						 dy =Math.sin(Math.toRadians(direction))*1.5;
							dx =Math.cos(Math.toRadians(direction))*1.5;
						dx = (int) (dx + x) / 2;
						dy = (int) (dy + y) / 2;
						if (dx < 0)
							dx = 0;
						if (dx > screenW / 2)
							dx = screenW / 2;
						if (dy < 0)
							dy = 0;
						if (dy > screenH / 2)
							dy = screenH / 2;
						pos = (int) (dx + dy * screenW / 2);
						if (pos == screenH / 2 * screenW / 2)
							pos = screenH / 2 * screenW / 2 - 1;
						if (!playField[(int) dx][(int) dy]) {
							aiAttack = false;
							// fx2=dx*2;
							// fy2=dy*2;
							// Log.i("And Angle", "=" + angle);
							return 1;
						}
					}
				}
				aiAttack = false;
				return angle;
			}

		}
		angle = 0;
		return angle;
	}
	
	public boolean isBot(){
		
		return isbot;
	}
	
	public void setPacket(PacketData pd){
	pd.x=(short) x;
	pd.y=(short) y;
	pd.direction=(short) direction;
	pd.dead=dead? (byte) 1 : (byte) 0;
	pd.hollow=dontDraw()  ? (byte) 1 : (byte) 0;
	pd.index=(short) index;
		
	}
	public void getPacket(PacketData pd,boolean isserver) {
	if(!isserver)
	dead=(pd.dead==1);
	
	x=pd.x;
	y=pd.y;
	direction=pd.direction;
	ishole=(pd.hollow ==(byte) 1);
	}
}
