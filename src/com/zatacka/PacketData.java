package com.zatacka;

import java.io.Serializable;
import java.lang.Character.UnicodeBlock;
import java.net.InetAddress;
import java.util.Hashtable;

public class PacketData  extends ObjectPool implements Comparable<PacketData>{
	public InetAddress address;
	public int port;
	
	public short packetid=0;
	public short ack;
	public short bitack;
public short x;
public short y;
public short direction;
public short  index=-1;
public short round=0;
public short score=0;
public short players=0;
public byte hollow=0;
public byte dead=0;
public byte running=0;
public byte theEnd=0;
public boolean expired=false;

public PacketData(){} 

public void setData(short x,short y,short direction,short index,short round,short score,short players,byte hollow,byte dead,byte running,byte theEnd)
{
this.x=x;
this.y=y;
this.direction=direction;
this.index=index;
this.players=players;
this.round=round;
this.score=score;
this.hollow=hollow;
this.dead=dead;
this.running=running;
this.theEnd=theEnd;
}

  void writePacketData(byte[] b)
{
	int index=0;
	shorttoByte(b,index,packetid);
	index+=2;
	shorttoByte(b,index,ack);
	index+=2;
	shorttoByte(b,index,bitack);
	index+=2;
	shorttoByte(b,index,x);
	index+=2;
	shorttoByte(b,index,y);
	index+=2;
	shorttoByte(b,index,direction);
	index+=2;
	shorttoByte(b,index,this.index);
	index+=2;
	shorttoByte(b,index,round);
	index+=2;
	shorttoByte(b,index,score);
	index+=2;
	shorttoByte(b,index,players);
	index+=1;
	b[index]=hollow;
	index+=1;
	b[index]=dead;
	index+=1;
	b[index]=running;
	index+=1;
	b[index]=theEnd;
}
  void readPacketData(byte[] b)
{
	int index=0;
	packetid=bytesToShort(b,index);
	index+=2;
	ack=bytesToShort(b,index);
	index+=2;
	bitack=bytesToShort(b,index);
	index+=2;
	x=bytesToShort(b,index);
	index+=2;
	y=bytesToShort(b,index);
	index+=2;
	direction=bytesToShort(b,index);
	index+=2;
	this.index=bytesToShort(b,index);
	index+=2;
	round=bytesToShort(b,index);
	index+=2;
	score=bytesToShort(b,index);
	index+=2;
	players=bytesToShort(b,index);
	index+=1;
	hollow=b[index];
	index+=1;
	dead=b[index];
	index+=1;
	running=b[index];
	index+=1;
	theEnd=b[index];
	
}
private static short bytesToShort(byte[] b,int index)
{	
return	(short)( (b[index+1]& 0xff)<<8|(b[index]& 0xff) );

}
private static void shorttoByte(byte[] b,int index,short x)
{
	b[index+1]=(byte)((x>>8)&0xFF);
	b[index]=(byte)(x&0xFF);			
	
}

 @Override
public String toString() {
	return "x:"+x+" y:"+y+" index:"+index+" running:"+running+" dead:"+dead+" round:"+round+" score:"+score+" players:"+players+" hollow:"+hollow;
}

@Override
public int compareTo(PacketData b) {
	if(this.round<b.round)return 1;
	else if(this.round>b.round)return -1;
	else {
		 if(this.running==0 && b.running==1)return 1;
		else if(this.running==0 && b.running==0)return -1;
		return 0;
	}
}

}