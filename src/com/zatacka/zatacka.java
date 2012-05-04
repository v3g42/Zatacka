package com.zatacka;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class zatacka extends Activity {
	ZatackaSingleView zsView;
	
	ImageView left,right;
	TextView text,score,latency;
	TableLayout table;
	public static int screenwidth;
	public static int screenheight;
	Context context;
	Activity activity;
	zatacka ztck;
	private Button sPlayer,host,join,connectPhones,startGame,addBot;
	TextView serverStatus;

	TextView clientStatus;
	private EditText serverIp;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        screenwidth = getWindowManager().getDefaultDisplay().getWidth();
		screenheight = getWindowManager().getDefaultDisplay().getHeight();
		context=this;
		activity=this;
		ztck=this;
		setContentView(R.layout.menu);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		sPlayer= (Button) findViewById(R.id.sButton);
		host= (Button) findViewById(R.id.hostButton);
		join= (Button) findViewById(R.id.clientButton);
		sPlayer.setOnClickListener(listener);
		host.setOnClickListener(listener);

		join.setOnClickListener(listener);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
    }
    

OnClickListener listener = new OnClickListener()
{

	int bots=0;
	public void AddRow(final String txt)
	{
		Handler handler=new Handler();
		handler.post(new Runnable(){

			@Override
			public void run() {
				TableRow row = new TableRow(context);
				TextView t = new TextView(context);
		        t.setText(txt);	
		        row.addView(t);
		        table.addView(row,new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		    
			}});
		
	}
	OnClickListener addbotlistener=new OnClickListener() {
		 
		@Override
		public void onClick(View v) {

		bots++;
		AddRow(" Bot - "+bots);
		}
	};
	@Override
	public void onClick(View v) {

		switch(v.getId()){
		case R.id.sButton:
			bots=0;
			setContentView(R.layout.single);
			table = (TableLayout) findViewById(R.id.players);
			startGame=(Button)findViewById(R.id.startGame);
			addBot=(Button)findViewById(R.id.addBot);
			addBot.setOnClickListener(addbotlistener);
				startGame.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						setContentView(R.layout.gamesingle);
						zsView=(ZatackaSingleView)findViewById(R.id.zview);
						text=(TextView) findViewById(R.id.text);
						score=(TextView) findViewById(R.id.score);
						left = (ImageView) findViewById(R.id.left);
						right = (ImageView) findViewById(R.id.right);
						zsView.Init(bots);
						zsView.setViews(text,left,right,score);
						//setContentView(zView);
						
					}
				});
			
			break;
		case R.id.hostButton:
			bots=0;
			setContentView(R.layout.host);
			table = (TableLayout) findViewById(R.id.players);
			startGame=(Button)findViewById(R.id.startGame);
			serverStatus=(TextView)findViewById(R.id.server_status);
			addBot=(Button)findViewById(R.id.addBot);
			addBot.setOnClickListener(addbotlistener);
			 List<InetAddress> players=Collections.synchronizedList(new ArrayList<InetAddress>());
			 List<Integer> ports=new ArrayList<Integer>();
			final GameRoom groom=new GameRoom(new Handler(), players,ports,context,ztck);
			startGame.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					groom.startClicked(bots);
				}
				});
			break;
		case R.id.clientButton:
			bots=0;
			setContentView(R.layout.client);
			serverIp = (EditText) findViewById(R.id.server_ip);
			clientStatus = (TextView) findViewById(R.id.clientStatus);
			table = (TableLayout) findViewById(R.id.players);
			connectPhones = (Button) findViewById(R.id.connect_phones);
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(serverIp.getWindowToken(), 0);
			connectPhones.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						GameClient gclient=new GameClient(new Handler(), InetAddress.getByName(serverIp.getText().toString()), 1235,ztck);
						
					} catch (UnknownHostException e) {
						clientStatus.setText(e.toString());
					}
					
					

				}			
			});
			break;
		}
	}
};
}