/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.BluetoothChat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static android.widget.Toast.LENGTH_LONG;

/**
 * This is the main Activity that displays the current chat session.
 */
public class BluetoothChat extends Activity {
	// 调试
	private static final String TAG = "BluetoothChat";
	private static boolean D = true;
	private static final String info = "junge";
	// 类型的消息发送从bluetoothchatservice处理程序
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
    public static final String BluetoothData = "fullscreen";
	private String newCode = "";
	private String newCode2 = "";
	private String fmsg = ""; // 保存用数据缓存
	// 键名字从收到的bluetoothchatservice处理程序
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	// 独特的是这个应用程序

	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	// Intent需要 编码
	public static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	// 布局控件
	private TextView mTitle;
	private EditText redInputText;
	private EditText greenInputText;
	private EditText yellowInputText;
	private EditText timeInputText;
	private Button redSendButton;
	private Button greenSendButton;
	private Button yellowSendButton;
	private Button urgencyStartButton;
	private Button urgencyStopButton;
	private Button timeChangeButton;
	private Button breakButton;
	private Button mButton_close;

	
	// 名字的连接装置
	private String mConnectedDeviceName = null;
	// 传出消息的字符串缓冲区
	private StringBuffer mOutStringBuffer;
	// 当地的蓝牙适配器
	private BluetoothAdapter mBluetoothAdapter = null;
	// 成员对象的聊天服务
	private BluetoothChatService mChatService = null;
	// 设置标识符，选择用户接受的数据格式
	private boolean dialogs;

    //第一次输入加入-->变量
	private int sum =1;
	private int UTF =1;

	// 名社民党记录当创建服务器套接字
	String mmsg = "";
	String mmsg2 = "";


	@TargetApi(Build.VERSION_CODES.ECLAIR)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (D)
			Log.e(TAG, "+++ ON CREATE +++");
		Log.i(info, "" + dialogs);
		// 设置窗口布局
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		// 设置文本的标题
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		// 初始化Radiobutton]
		breakButton = (Button) findViewById(R.id.button_break);
		// 得到当地的蓝牙适配器
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		mButton_close = (Button) findViewById(R.id.btclose);



		// 初始化Socket
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.not_connected, LENGTH_LONG)
					.show();
			finish();
			return;
		}

	}

	@TargetApi(Build.VERSION_CODES.ECLAIR)
	@Override
	public void onStart() {
		super.onStart();
		if (D)
			Log.e(TAG, "++ ON START ++");

		// 如果是没有，要求它启用。
		// setupchat()将被称为在onactivityresult
		if (!mBluetoothAdapter.isEnabled()) {
	//以为这样会无提示，结果无效，fu'c'k		
    //			mBluetoothAdapter.enable();
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// 否则，设置聊天会话
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	//自行添加的功能
	@TargetApi(Build.VERSION_CODES.ECLAIR)
	public void onBlueToothclose(View view){
		//count++;
		{
			if (mBluetoothAdapter.isEnabled()) {

				mBluetoothAdapter.disable();
				Toast.makeText(this, "关闭蓝牙", LENGTH_LONG)
						.show();
				mButton_close.setText("Open Bluetooth");

			} else {
				Toast.makeText(this, "开启蓝牙", LENGTH_LONG)
						.show();
				mBluetoothAdapter.enable();
				mButton_close.setText("Close Bluetooth");
			}
		}

	}

	// 连接按键响应函数(蓝牙连接服务)
	public void onConnectButtonClicked(View v) {

		if (breakButton.getText().equals("Connected")) {
			Intent serverIntent = new Intent(this, DeviceListActivity.class); // 跳转程序设置
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); // 设置返回宏定义
			breakButton.setText(R.string.duankai);

		} else {
			// 关闭连接socket
			try {
				// 关闭蓝牙
				breakButton.setText(R.string.button_break);
				mChatService.stop();

			} catch (Exception e) {
			}
		}
		return;
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// 执行此检查onresume()涵盖的案件中
		// 不可在onstart()，所以我们停下来让它…
		// onresume()将被调用时，action_request_enable活动返回。
		if (mChatService != null) {
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// 启动蓝牙聊天服务
				mChatService.start();
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.CUPCAKE)
	private void setupChat() {
		Log.d(TAG, "setupChat()");
		// 初始化撰写与听众的返回键
		redInputText = (EditText) findViewById(R.id.red);
		greenInputText = (EditText) findViewById(R.id.green);
		yellowInputText = (EditText) findViewById(R.id.yellow);
		timeInputText = (EditText) findViewById(R.id.timeChange);


		// 初始化发送按钮，单击事件侦听器
		redSendButton = (Button) findViewById(R.id.redSend);
		greenSendButton = (Button) findViewById(R.id.greenSend);
		yellowSendButton = (Button) findViewById(R.id.yellowSend);
		urgencyStartButton = (Button) findViewById(R.id.urgencystart);
		urgencyStopButton = (Button) findViewById(R.id.urgencystop);
		timeChangeButton = (Button) findViewById(R.id.timeChangeSend);
		//公用同一监听器
		redSendButton.setOnClickListener(new ButtonListener());
		greenSendButton.setOnClickListener(new ButtonListener());
		yellowSendButton.setOnClickListener(new ButtonListener());
		urgencyStartButton.setOnClickListener(new ButtonListener());
		urgencyStopButton.setOnClickListener(new ButtonListener());
		timeChangeButton.setOnClickListener(new ButtonListener());

//		redSendButton.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				// 发送消息使用内容的文本编辑控件
//				TextView view = (TextView) findViewById(R.id.red);
//				String message = view.getText().toString();
//
//				try {
//					message.getBytes("ISO_8859_1");
//					if (message.length() > 0) {
//						// 得到消息字节和告诉bluetoothchatservice写
//						byte[] send = message.getBytes();
//						mChatService.write(send);
//
//					}
//				} catch (UnsupportedEncodingException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//			}
//		});

		// 初始化bluetoothchatservice执行蓝牙连接
		mChatService = new BluetoothChatService(this, mHandler);

		// 缓冲区初始化传出消息
		mOutStringBuffer = new StringBuffer("");
	}

	private class ButtonListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// 发送消息使用内容的文本编辑控件
			String message = null;
			switch (v.getId()) {

				case R.id.redSend:
					message = "red"+redInputText.getText().toString();
					break;
				case R.id.greenSend:
					message = "green"+greenInputText.getText().toString();
					break;
				case R.id.yellowSend:
					message = "yellow"+yellowInputText.getText().toString();
					break;
				case R.id.urgencystart:
					message = "urgencystart";
					break;
				case R.id.urgencystop:
					message = "urgencystop";
					break;
				case R.id.timeChangeSend:
					message = "timechange"+timeInputText.getText().toString();
				default:
					break;
			}

			try {
					message.getBytes("ISO_8859_1");    //转换编码，类似ascii编码
					if (message.length() > 0) {
						// 得到消息字节和告诉bluetoothchatservice写
						byte[] send = message.getBytes();
						mChatService.write(send);
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	public void onMyButtonClick(View view) {

		if (view.getId() == R.id.button_break) {

			onConnectButtonClicked(breakButton);
		}

	}
	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// 蓝牙聊天服务站
		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	@TargetApi(Build.VERSION_CODES.ECLAIR)
	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * 发送一个消息
	 * 
	 * @param message
	 * 一个文本字符串发送.
	 */
	private void sendMessage(String message) {
		// 检查我们实际上在任何连接
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// 检查实际上有东西寄到
		if (message.length() > 0) {
			// 得到消息字节和告诉bluetoothchatservice写
			byte[] send = message.getBytes();
			mChatService.write(send);
		}
	}

	// 处理程序，获取信息的bluetoothchatservice回来
	private final Handler mHandler = new Handler() {
		

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothChatService.STATE_CONNECTED:
					mTitle.setText(R.string.title_connected_to);
					mTitle.append(mConnectedDeviceName);
					break;
				case BluetoothChatService.STATE_CONNECTING:
					mTitle.setText(R.string.title_connecting);
					break;
				case BluetoothChatService.STATE_LISTEN:
				case BluetoothChatService.STATE_NONE:
					mTitle.setText(R.string.title_not_connected);
					break;
				}
				break;
//			case MESSAGE_WRITE:
//				byte[] writeBuf = (byte[]) msg.obj;
//				// 构建一个字符串缓冲区
//				String writeMessage = new String(writeBuf);
//				sum=1;
//				UTF=1;
//				mmsg += writeMessage;
//
//				break;
//			case MESSAGE_READ:
//				byte[] readBuf = (byte[]) msg.obj;
//				// 构建一个字符串从有效字节的缓冲区
//				if (sum==1) {
//					fmsg+="\n-->\n";
//					sum++;
//				}else {
//					sum++;
//				}
//				String readMessage = new String(readBuf, 0, msg.arg1);
//
//				break;
			case MESSAGE_DEVICE_NAME:
				// 保存该连接装置的名字
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"已连接 " + mConnectedDeviceName, Toast.LENGTH_SHORT)
						.show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	public String changeCharset(String str, String newCharset)
			throws UnsupportedEncodingException {
		if (str != null) {
			// 用默认字符编码解码字符串。
			byte[] bs = str.getBytes();
			// 用新的字符编码生成字符串
			return new String(bs, newCharset);
		}
		return null;
	}

	/**
	 * 将字符编码转换成UTF-8码
	 */
	public String toUTF_8(String str) throws UnsupportedEncodingException {
		return this.changeCharset(str, "UTF_8");
	}

	@TargetApi(Build.VERSION_CODES.ECLAIR)
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			// 当devicelistactivity返回连接装置
			if (resultCode == Activity.RESULT_OK) {
				// 获得设备地址
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// 把蓝牙设备对象
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// 试图连接到装置
				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// 当请求启用蓝牙返回
			if (resultCode == Activity.RESULT_OK) {
				// 蓝牙已启用，所以建立一个聊天会话
				setupChat();
			} else {
				// 用户未启用蓝牙或发生错误
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		case R.id.scan:
//			// 用户未启用蓝牙或发生错误
//			Intent serverIntent = new Intent(this, DeviceListActivity.class);
//			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
//			return true;
//		case R.id.discoverable:
//			// 确保此设备是发现别人
//			ensureDiscoverable();
//			return true;
//
//		case R.id.setup:
//			new AlertDialog.Builder(this)
//					.setTitle("设置可选参数")
//					.setIcon(android.R.drawable.ic_dialog_info)
//					.setSingleChoiceItems(new String[] { "十六进制", "字符串" }, 0,
//							new DialogInterface.OnClickListener() {
//								public void onClick(DialogInterface dialog,
//										int which) {
//
//									if (dialog.equals("十六进制")) {
//										Log.d(TAG, "十六进制");
//										dialogs = true;
//									} else {
//										dialogs = false;
//										Log.d(TAG, "字符串");
//									}
//									dialog.dismiss();
//								}
//							}).setNegativeButton("取消", null).show();
//			     return true;
//
//		case R.id.clenr:
//			finish();
//			return true;
//		}
//		return false;
//	}

}