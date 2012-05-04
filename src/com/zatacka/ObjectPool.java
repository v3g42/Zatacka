package com.zatacka;


import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.ArrayBlockingQueue;

public abstract class ObjectPool {
	  private static long expirationTime,count=0;
	  private static int size=10000;
	  private static Hashtable<PacketData,Long> locked, unlocked;
	  static {
	    expirationTime = 30000; // 30 seconds
	    locked = new Hashtable<PacketData,Long>(size);
	    unlocked = new Hashtable<PacketData,Long>(size);
	  }

	  public static String getCounts(){
		  return locked.size() + ","+unlocked.size();
		  
	  }
		protected static PacketData create() {
			return new PacketData();
		}

		
		public static boolean validate(PacketData o) {
			return o.expired;
		}

		
		public static void expire(PacketData o) {
			o.expired=true;
			
		}

	  public static synchronized  PacketData checkOut() {
		  long time=System.currentTimeMillis();
	    PacketData t;
	    if (unlocked.size() > 0) {
	    Enumeration<PacketData> e=unlocked.keys();
	    t=e.nextElement();
	    unlocked.remove(t);
	    locked.put(t, time);
	     return t;
	      }
	    t = create();
	    locked.put(t, time);
	    return (t);
	  }

	  public static synchronized void checkIn(PacketData t) {
		if(t!=null){
		  long time=System.currentTimeMillis();
	    locked.remove(t);
	    t.setData((short)0,(short) 0,(short) 0,(short)-1,(short)0,(short)0,(short)0,(byte) 0,(byte)0,(byte)0,(byte)0);
	    unlocked.put(t,time);
		}
	  }
	
	}

