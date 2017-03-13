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
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
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
	// ����
	private static final String TAG = "BluetoothChat";
	private static boolean D = true;
	private static final String info = "junge";
	// ���͵���Ϣ���ʹ�bluetoothchatservice��������
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
    public static final String BluetoothData = "fullscreen";
	private String newCode = "";
	private String newCode2 = "";
	private String fmsg = ""; // ���������ݻ���
	// �����ִ��յ���bluetoothchatservice��������
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	// ���ص������Ӧ�ó���

	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");
	// Intent��Ҫ ����
	public static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	// ���ֿؼ�
	private TextView mTitle;
	private EditText mOutEditText;
	private Button mSendButton;
	private Button breakButton;
	private Button mButton_close;

	
	// ���ֵ�����װ��
	private String mConnectedDeviceName = null;
	// ������Ϣ���ַ���������
	private StringBuffer mOutStringBuffer;
	// ���ص�����������
	private BluetoothAdapter mBluetoothAdapter = null;
	// ��Ա������������
	private BluetoothChatService mChatService = null;
	// ���ñ�ʶ����ѡ���û����ܵ����ݸ�ʽ
	private boolean dialogs;

    //��һ���������-->����
	private int sum =1;
	private int UTF =1;

	// �����񵳼�¼�������������׽���
	String mmsg = "";
	String mmsg2 = "";


	@TargetApi(Build.VERSION_CODES.ECLAIR)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (D)
			Log.e(TAG, "+++ ON CREATE +++");
		Log.i(info, "" + dialogs);
		// ���ô��ڲ���
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.custom_title);

		// �����ı��ı���
		mTitle = (TextView) findViewById(R.id.title_left_text);
		mTitle.setText(R.string.app_name);
		mTitle = (TextView) findViewById(R.id.title_right_text);
		// ��ʼ��Radiobutton]
		breakButton = (Button) findViewById(R.id.button_break);
		// �õ����ص�����������
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		mButton_close = (Button) findViewById(R.id.btclose);



		// ��ʼ��Socket
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

		// �����û�У�Ҫ�������á�
		// setupchat()������Ϊ��onactivityresult
		if (!mBluetoothAdapter.isEnabled()) {
	//��Ϊ����������ʾ�������Ч��fu'c'k		
    //			mBluetoothAdapter.enable();
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// ������������Ự
		} else {
			if (mChatService == null)
				setupChat();
		}
	}

	//�������ӵĹ���
	@TargetApi(Build.VERSION_CODES.ECLAIR)
	public void onBlueToothclose(View view){
		//count++;
		{
			if (mBluetoothAdapter.isEnabled()) {

				mBluetoothAdapter.disable();
				Toast.makeText(this, "�ر�����", LENGTH_LONG)
						.show();
				mButton_close.setText("��������");

			} else {
				Toast.makeText(this, "��������", LENGTH_LONG)
						.show();
				mBluetoothAdapter.enable();
				mButton_close.setText("�ر�����");
			}
		}

	}

	// ���Ӱ�����Ӧ����
	public void onConnectButtonClicked(View v) {

		if (breakButton.getText().equals("����")||breakButton.getText().equals("connect")) {
			Intent serverIntent = new Intent(this, DeviceListActivity.class); // ��ת��������
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE); // ���÷��غ궨��
			breakButton.setText(R.string.duankai);

		} else {
			// �ر�����socket
			try {
				// �ر�����
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

		// ִ�д˼��onresume()���ǵİ����У�Ӣ������
		// ������onstart()����������ͣ����������
		// onresume()��������ʱ��action_request_enable����ء�
		if (mChatService != null) {
			// ֻ�й�����state_none������֪�������ǻ�û�п�ʼ
			if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
				// ���������������
				mChatService.start();
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.CUPCAKE)
	private void setupChat() {
		Log.d(TAG, "setupChat()");
		// ��ʼ��׫д�����ڵķ��ؼ�
		mOutEditText = (EditText) findViewById(R.id.edit_text_out);
		mOutEditText.setOnEditorActionListener(mWriteListener);

		// ��ʼ�����Ͱ�ť�������¼�������
		mSendButton = (Button) findViewById(R.id.button_send);
		mSendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// ������Ϣʹ�����ݵ��ı��༭�ؼ�
				TextView view = (TextView) findViewById(R.id.edit_text_out);
				String message = view.getText().toString();

				try {
					message.getBytes("ISO_8859_1");
					if (message.length() > 0) {
						// �õ���Ϣ�ֽں͸���bluetoothchatserviceд
						byte[] send = message.getBytes();
						mChatService.write(send);

					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}   

			}
		});

		// ��ʼ��bluetoothchatserviceִ����������
		mChatService = new BluetoothChatService(this, mHandler);

		// ��������ʼ��������Ϣ
		mOutStringBuffer = new StringBuffer("");
	}

	public void onMyButtonClick(View view) {
//		if (view.getId() == R.id.button_clean) {
//			mInputEditText.setText("");
//			fmsg="";
//			sum =0;
//		}
		if (view.getId() == R.id.button_break) {

			onConnectButtonClicked(breakButton);
		}
//			String Data =mInputEditText.getText().toString();
//			if (Data.length()>0) {
//				Intent intent = new Intent();
//			intent.putExtra(BluetoothData,Data);
//			intent.setClass(BluetoothChat.this, FullScreen.class);
//			startActivity(intent);


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
		// �����������վ
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
	 * ����һ����Ϣ
	 * 
	 * @param message
	 *            һ���ı��ַ�������.
	 */
	private void sendMessage(String message) {
		// �������ʵ�������κ�����
		if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// ���ʵ�����ж����ĵ�
		if (message.length() > 0) {
			// �õ���Ϣ�ֽں͸���bluetoothchatserviceд
			byte[] send = message.getBytes();
			mChatService.write(send);
			// �����ַ�������������������ı��༭�ֶ�
			//mOutEditText.setText(mOutStringBuffer);
			//mOutEditText2.setText(mOutStringBuffer);

		}
		// }else if(message.length()<=0){
		// Toast.makeText(BluetoothChat.this, "�����ѶϿ�",
		// Toast.LENGTH_LONG).show();
		// // �û�δ����������������
		// mChatService = new BluetoothChatService(this, mHandler);
		// Intent serverIntent = new Intent(BluetoothChat.this,
		// DeviceListActivity.class);
		// startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
		// }
	}

	// �ж��������ߵı༭��ؼ������س���
	private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
		public boolean onEditorAction(TextView view, int actionId,
				KeyEvent event) {

			// ����ж���һ���ؼ��ж��¼��ķ��ؼ���������Ϣ
			if (actionId == EditorInfo.IME_NULL
					&& event.getAction() == KeyEvent.ACTION_UP) {
				//if (view.getId() == R.id.edit_text_out2) {
					String tmp = view.getText().toString();
					String d;
					for(int i=0;i<tmp.length();i++){
						d=tmp.charAt(i)+"";
						if(i%2!=0){
							d+=" ";
						}

							sendMessage("\n"+ d);

					}




			}
			if (D)
				Log.i(TAG, "END onEditorAction");
			return true;
		}
	};

	// �������򣬻�ȡ��Ϣ��bluetoothchatservice����
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
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// ����һ���ַ���������
				String writeMessage = new String(writeBuf);
				sum=1;
				UTF=1;
				mmsg += writeMessage;

				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// ����һ���ַ�������Ч�ֽڵĻ�����
				if (sum==1) {
					fmsg+="\n-->\n";
					sum++;
				}else {
					sum++;
				}
				String readMessage = new String(readBuf, 0, msg.arg1);

				break;
			case MESSAGE_DEVICE_NAME:
				// ���������װ�õ�����
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"������ " + mConnectedDeviceName, Toast.LENGTH_SHORT)
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
			// ��Ĭ���ַ���������ַ�����
			byte[] bs = str.getBytes();
			// ���µ��ַ����������ַ���
			return new String(bs, newCharset);
		}
		return null;
	}

	/**
	 * ���ַ�����ת����UTF-8��
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
			// ��devicelistactivity��������װ��
			if (resultCode == Activity.RESULT_OK) {
				// ����豸��ַ
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// �������豸����
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				// ��ͼ���ӵ�װ��
				mChatService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			// ������������������
			if (resultCode == Activity.RESULT_OK) {
				// ���������ã����Խ���һ������Ự
				setupChat();
			} else {
				// �û�δ����������������
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.scan:
			// �û�δ����������������
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			return true;
		case R.id.discoverable:
			// ȷ�����豸�Ƿ��ֱ���
			ensureDiscoverable();
			return true;

		case R.id.setup:
			new AlertDialog.Builder(this)
					.setTitle("���ÿ�ѡ����")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setSingleChoiceItems(new String[] { "ʮ������", "�ַ���" }, 0,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {

									if (dialog.equals("ʮ������")) {
										Log.d(TAG, "ʮ������");
										dialogs = true;
									} else {
										dialogs = false;
										Log.d(TAG, "�ַ���");
									}
									dialog.dismiss();
								}
							}).setNegativeButton("ȡ��", null).show();
			     return true;

		case R.id.clenr:
			finish();
			return true;
		}
		return false;
	}
//	public boolean onKeyDown(int keyCode, KeyEvent event)  {
//
//		          if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
//		        	  AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
//		        	   localBuilder.setIcon(R.drawable.logo).setTitle("������ʾ...")
//		        	     .setMessage("��ȷ��Ҫ�˳���");
//		        	   localBuilder.setPositiveButton("ȷ��",
//		        	     new DialogInterface.OnClickListener() {
//		        	      public void onClick(
//		        	        DialogInterface paramDialogInterface,
//		        	        int paramInt) {
//		        	       BluetoothChat.this.finish();
//		        	      }
//		        	     });
//		        	   localBuilder.setNegativeButton("ȡ��",
//		        	     new DialogInterface.OnClickListener() {
//		        	      public void onClick(
//		        	        DialogInterface paramDialogInterface,
//		        	        int paramInt) {
//		        	       paramDialogInterface.cancel();
//		        	      }
//		        	     }).create();
//		        	   localBuilder.show();
//
//		          }else if(keyCode == KeyEvent.KEYCODE_MENU) {
//		             return false;
//		          }
//		          return true;
//		      }
}