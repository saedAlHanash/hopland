package com.util;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hopeland.pda.example.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressLint("HandlerLeak")
public class BaseActivity extends Activity {
	protected static final String TAG = "Example";
	private Toast _MyToast = null;

	protected static final int MSG_SHOW_WAIT = -1;
	protected static final int MSG_HIDE_WAIT = -2;
	protected static final int MSG_SHOW_TIP = -3;
	protected static final int MSG_SHOW_MSG = -4;
	protected static final int MSG_SHOW_CONFIRM = -5;
	protected static final int MSG_UPDATE_WAIT = -6;

	protected static final int MSG_SHOW_LONG_TIP = -8;

	public static final int MSG_USER_BEG = 0;

	private ProgressDialog waitDialog = null;
	private final Lock waitDialogLock = new Lock();
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MSG_SHOW_WAIT:
					doShowWaitDialog(null, (String) msg.obj);
					break;
				case MSG_UPDATE_WAIT:
					doUpdateWaitDialog(null, (String) msg.obj);
					break;
				case MSG_HIDE_WAIT:
					doHideWaitDialog();
					break;
				case MSG_SHOW_TIP:
					doShowTip((String) msg.obj, false);
					break;
				case MSG_SHOW_LONG_TIP:
					doShowTip((String) msg.obj, true);
					break;
				case MSG_SHOW_MSG:
					ShowMsgInfo msgInfo = (ShowMsgInfo) msg.obj;
					if (msgInfo != null) {
						doShowMsg(msgInfo.title, msgInfo.msg, msgInfo.listener);
					}
					break;
				case MSG_SHOW_CONFIRM:
					ShowConfimInfo confimInfo = (ShowConfimInfo) msg.obj;
					if (confimInfo != null) {
						doShowConfim(confimInfo.msg,
								confimInfo.okString, confimInfo.cancelString,
								confimInfo.okListener, confimInfo.cancelListener);
					}
					break;
				default:
					msgProcess(msg);
					break;
			}
		}
	};


	/****************************************************************
	 * 消息相关 相关
	 ***************************************************************/
	/**
	 * 消息处理
	 *
	 * @param msg
	 */
	protected void msgProcess(Message msg) {

	}

	/**
	 * 发送消息
	 *
	 * @param what
	 * @param obj
	 */
	protected void sendMessage(int what, Object obj) {
		handler.sendMessage(handler.obtainMessage(what, obj));
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	protected void showTip(String msg) {
		sendMessage(MSG_SHOW_TIP, msg);
	}

	private synchronized void doShowTip(String msg, boolean isLong) {
		if (_MyToast == null) {
			_MyToast = Toast.makeText(BaseActivity.this, msg,
					isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
		} else {
			_MyToast.setText(msg);
		}
		_MyToast.show();
	}

	/**
	 * @param title
	 * @param info
	 */
	private void doShowWaitDialog(String title, String info) {
		synchronized (waitDialogLock) {
			try {
				if (waitDialog != null) {
					waitDialog.dismiss();
					waitDialog = null;
				}
			} catch (Exception e) {
			}
			waitDialog = ProgressDialog.show(this, title, info);
		}
	}

	private void doUpdateWaitDialog(String title, String info) {
		try {
			synchronized (waitDialogLock) {
				if (waitDialog != null) {
					waitDialog.setTitle(title);
					waitDialog.setMessage(info);
				}
			}
		} catch (Exception e) {

		}
	}

	private void doHideWaitDialog() {
		try {
			synchronized (waitDialogLock) {
				if (waitDialog != null) {
					waitDialog.dismiss();
					waitDialog = null;
				}
			}
		} catch (Exception e) {

		}
	}

	/**
	 * 显示等待信息
	 *
	 * @param msg 等待信息
	 */
	protected void showWait(String msg) {
		sendMessage(MSG_SHOW_WAIT, msg);
	}


	/**
	 * 隐藏等待信息
	 */
	protected void hideWait() {
		sendMessage(MSG_HIDE_WAIT, null);
	}

	protected class ShowMsgInfo {
		public String title;
		public String msg;
		public OnClickListener listener;
	}

	protected class ShowConfimInfo {
		public String msg;
		public String okString;
		public String cancelString;
		public OnClickListener okListener;
		public OnClickListener cancelListener;
	}

	public interface InputDialogInputFinishCallBack {
		void onInputDialogFinish(String text);
	}

	protected class ShowInputDialogInfo {
		public String title;
		public String info;
		public String edit;
		public InputDialogInputFinishCallBack callBack;
	}

	/**
	 * 显示提示消息
	 *
	 * @param msg
	 * @param listener
	 */
	private synchronized void doShowMsg(String title, String msg,
										OnClickListener listener) {
		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info).setTitle(title)
				.setMessage(msg)
				.setPositiveButton(getString(R.string.str_ok), listener)
				.setCancelable(false)
				.create().show();
	}

	/**
	 * 显示提示消息
	 *
	 * @param msg
	 * @param listener
	 */
	protected void showMsg(String title, String msg,
						   OnClickListener listener) {
		ShowMsgInfo info = new ShowMsgInfo();

		info.title = title;
		info.msg = msg;
		info.listener = listener;

		sendMessage(MSG_SHOW_MSG, info);
	}

	/**
	 * 显示提示消息
	 *
	 * @param msg
	 * @param listener
	 */
	protected void showMsg(int title, int msg,
						   OnClickListener listener) {
		showMsg(getString(title), getString(msg), listener);
	}

	/**
	 * 显示提示消息
	 *
	 * @param msg      提示消息
	 * @param listener 按键监听事件
	 */
	protected void showMsg(String msg, OnClickListener listener) {
		showMsg(null, msg, listener);
	}


	/**
	 * 显示提示消息
	 *
	 * @param msg 提示消息
	 */
	protected void showMsg(String msg) {
		showMsg(null, msg, null);
	}



	private synchronized void doShowConfim(String msg,
										   String okString, String cancelString,
										   OnClickListener okListener, OnClickListener cancelListener) {

		if (okString != null && !okString.isEmpty()) {
			okString = getString(R.string.str_ok);
		}

		if (cancelString != null && !cancelString.isEmpty()) {
			cancelString = getString(R.string.str_cancel);
		}

		new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info).setTitle(getString(R.string.str_confirm))
				.setMessage(msg)
				.setPositiveButton(okString, okListener)
				.setNegativeButton(cancelString, cancelListener)
				.setCancelable(false)
				.create().show();
	}

	/**
	 * 显示对话框
	 *
	 * @param msg            对话框消息
	 * @param okString       确认按键显示名称
	 * @param cancelString   取消按键显示名称
	 * @param okListener     确认按键监听事件
	 * @param cancelListener 取消按键监听事件
	 */
	protected void showConfim(String msg,
							  String okString, String cancelString,
							  OnClickListener okListener, OnClickListener cancelListener) {
		ShowConfimInfo info = new ShowConfimInfo();

		info.msg = msg;
		info.okString = okString;
		info.cancelString = cancelString;
		info.okListener = okListener;
		info.cancelListener = cancelListener;

		sendMessage(MSG_SHOW_CONFIRM, info);
	}

	/**
	 * 显示对话框
	 *
	 * @param msg            对话框消息
	 * @param okListener     确认按键监听事件
	 * @param cancelListener 取消按键监听事件
	 */
	protected void showConfim(String msg, OnClickListener okListener,
							  OnClickListener cancelListener) {
		showConfim(msg, null, null, okListener, cancelListener);
	}


	/**
	 * 获取版本号
	 *
	 * @return 当前应用的版本号
	 */
	public String getVersion() {
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			String version = info.versionName;
			return version;
		} catch (Exception e) {
			return null;
		}
	}

	private long lastClickTime;

	protected synchronized boolean isFastClick() {
		long time = System.currentTimeMillis();
		if (time - lastClickTime < 600) {
			return true;
		}
		lastClickTime = time;
		return false;
	}

	/****************************************************************
	 * 自定义标题栏 相关
	 ***************************************************************/

	/**
	 * 显示自定义的ActionBar
	 *
	 * @param title       标题
	 * @param subTitle    副标题
	 * @param left        左按钮
	 * @param right       右按钮
	 * @param left_icon   左按钮图标
	 * @param right_icon  有按钮图标
	 * @param leftListen  左按钮事件
	 * @param rightListen 右按钮事件
	 * @return 成功失败
	 */
	@SuppressLint("InflateParams")
	protected boolean showCustomBar(String title, String subTitle, String left,
									String right, int left_icon, int right_icon,
									View.OnClickListener leftListen, View.OnClickListener rightListen) {

		ActionBar actionBar = getActionBar();
		if (null == actionBar) {
			return false;
		}

		actionBar.setTitle("");
		actionBar.setDisplayShowHomeEnabled(false);

		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View custActionBar = inflater.inflate(R.layout.custom_action_bar, null);
		TextView tvTitle = custActionBar
				.findViewById(R.id.actionBarTitle);
		TextView tvSubTitle = custActionBar
				.findViewById(R.id.actionBarSubTitle);
		Button btnLeft = custActionBar
				.findViewById(R.id.actionBarLeft);
		Button btnRight = custActionBar
				.findViewById(R.id.actionBarRight);

		tvTitle.setText(title);
		if (subTitle != null) {
			tvSubTitle.setText(subTitle);
		} else {
			tvSubTitle.setVisibility(View.GONE);
		}

		if (left != null) {
			btnLeft.setText(" " + left);
		} else {
			btnLeft.setText("  ");
		}
		btnLeft.setOnClickListener(leftListen);
		if (left_icon > 0) {
			Drawable drawable = getResources().getDrawable(left_icon);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			btnLeft.setCompoundDrawables(drawable, null, null, null);
		}

		if (right != null) {
			btnRight.setText(right + " ");
		} else {
			btnRight.setText("  ");
		}
		btnRight.setOnClickListener(rightListen);
		if (right_icon > 0) {
			Drawable drawable = getResources().getDrawable(right_icon);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			btnRight.setCompoundDrawables(null, null, drawable, null);
		}

		if (null == left && left_icon <= 0) {
			btnLeft.setVisibility(View.GONE);
		}

		if (null == right && right_icon <= 0) {
			btnRight.setVisibility(View.GONE);
		}

		ActionBar.LayoutParams lp = new ActionBar.LayoutParams(
				ActionBar.LayoutParams.MATCH_PARENT,
				ActionBar.LayoutParams.MATCH_PARENT);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setCustomView(custActionBar, lp);

		return true;
	}

	/**
	 * 显示自定义的ActionBar
	 *
	 * @param title       标题
	 * @param left        左按钮
	 * @param right       右按钮
	 * @param left_icon   左按钮图标
	 * @param right_icon  有按钮图标
	 * @param leftListen  左按钮事件
	 * @param rightListen 右按钮事件
	 * @return 成功失败
	 */
	protected boolean showCustomBar(String title, String left, String right,
									int left_icon, int right_icon, View.OnClickListener leftListen,
									View.OnClickListener rightListen) {
		return showCustomBar(title, null, left, right, left_icon, right_icon,
				leftListen, rightListen);
	}

	/**
	 * 显示自动以的ActionBar
	 *
	 * @param title       标题
	 * @param left        左按钮
	 * @param right       右按钮 * @param leftListen 左按钮事件
	 * @param rightListen 右按钮事件
	 * @return 成功失败
	 */
	protected boolean showCustomBar(String title, String left, String right,
									View.OnClickListener leftListen, View.OnClickListener rightListen) {
		return showCustomBar(title, left, right, 0, 0, leftListen, rightListen);
	}

	/**
	 * 显示自动以的ActionBar
	 *
	 * @param title       标题
	 * @param subTitle    副标题
	 * @param left        左按钮
	 * @param right       右按钮
	 * @param leftListen  左按钮事件
	 * @param rightListen 右按钮事件
	 * @return 成功失败
	 */
	protected boolean showCustomBar(String title, String subTitle, String left,
									String right, View.OnClickListener leftListen,
									View.OnClickListener rightListen) {
		return showCustomBar(title, subTitle, left, right, 0, 0, leftListen,
				rightListen);
	}


	private final Vibrator vibrator = null;//(Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);

	/**
	 * 取消震动
	 */
	protected void cancelVibrator() {
		if (vibrator != null) {
			vibrator.cancel();
		}
	}

//	private static PowerManager.WakeLock wakeLock = null;


	public void keepScreenOn(boolean on) {
		if (on) {
//			if (null == wakeLock) {
//				PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//				wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, this.getClass().getName());
//				wakeLock.acquire();
//			}
//
			try {
				getWindow().setFlags(
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			} catch (Exception ex) {
				//Log.d("Debug", "The initialization of abnormal:" + ex.getMessage());
			}
		} else {
//			if (wakeLock != null) {
//				wakeLock.release();
//				wakeLock = null;
//			}
			try {
				getWindow().setFlags(
						0,
						WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			} catch (Exception ex) {
				//Log.d("Debug", "The initialization of abnormal:" + ex.getMessage());
			}
		}
	}


	@Override
	public void onBackPressed() {
		this.finish();
	}

	protected boolean __Exit = false;
	@Override
	protected void onDestroy() {
		keepScreenOn(false);
		cancelVibrator();
		super.onDestroy();

		__Exit = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		hideWait();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_HOME
				&& event.getRepeatCount() == 0) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000; //需要自己定义标志

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);//关键代码
	}


	/****************************************************************
	 * 通用函数 相关
	 ***************************************************************/
	public boolean checkHexInput(String strInput) {
		boolean rt = false;
		Pattern p = Pattern.compile("^[a-f,A-F,0-9]*$");
		Matcher m = p.matcher(strInput);
		rt = m.matches();
		return rt;
	}
}
