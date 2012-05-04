package com.zatacka;

import android.os.Handler;


public  abstract class ReceiveThread extends Thread {
	Handler mZatackaHandler;
	@Override
	public abstract void run();
	ReceiveThread(){
		super("ReceiveThread");
	}
	ReceiveThread(String name){
		super(name);
	}
}