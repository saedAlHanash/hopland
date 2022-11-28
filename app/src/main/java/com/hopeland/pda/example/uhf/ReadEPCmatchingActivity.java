package com.hopeland.pda.example.uhf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.hopeland.pda.example.PublicData;
import com.hopeland.pda.example.R;
import com.pda.rfid.EPCModel;
import com.pda.rfid.IAsynchronousMessage;
import com.pda.rfid.uhf.UHFReader;
import com.port.Adapt;
import com.util.Helper.*;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.media.*;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.widget.*;

/**
 * @author RFID_C 匹配读标签窗体
 */
public class ReadEPCmatchingActivity extends UHFBaseActivity implements
		IAsynchronousMessage {

	private ListView listView = null; // 数据列表对象
	private SimpleAdapter sa = null;
	private Button btn_Read = null;
	private TextView tv_TitleTagID = null;
	private TextView lb_ReadTime = null;
	private TextView lb_ReadSpeed = null;
	private TextView lb_TagCount = null;
	private Spinner sp_ReadType = null;

	private static boolean isStartPingPong = false; //
	private boolean isKeyDown = false; // 板机是否按下
	private boolean isLongKeyDown = false; // 板机是否是长按状态
	private int keyDownCount = 0; // 板机按下次数

	private int readTime = 0;
	private int lastReadCount = 0;
	private int totalReadCount = 0; // 总读取次数
	private int speed = 0; // 读取速度
	private static int _ReadType = 0; // 0 为读EPC，1 为读TID
	private HashMap<String, EPCModel> hmList = new HashMap<String, EPCModel>();
	private Object hmList_Lock = new Object();
	private boolean flag = true; //
	private Boolean IsFlushList = true; // 是否刷列表
	private Object beep_Lock = new Object();
	ToneGenerator toneGenerator = new ToneGenerator(AudioManager.STREAM_SYSTEM,
			ToneGenerator.MAX_VOLUME);

	private static boolean isPowerLowShow = false;

	private Button btnRead = null;

	// 匹配的标签数据的起始值
	private String matchingData = "";

	private final int MSG_RESULT_READ = MSG_USER_BEG + 1; // 常量读
	private final int MSG_FLUSH_READTIME = MSG_USER_BEG + 2;
	private final int MSG_UHF_POWERLOW = MSG_USER_BEG + 3;
	private final int MSG_MATCHING_DATA = MSG_USER_BEG + 4;

	// Handler mHandler = new Handler() {
	@Override
	protected void msgProcess(Message msg) {
		switch (msg.what) {
		case MSG_RESULT_READ:
			ShowList(); // 刷新列表
			break;
		case MSG_FLUSH_READTIME:
			if (lb_ReadTime != null) { // 刷新读取时间
				readTime++;
				lb_ReadTime.setText("Time:" + readTime + "S");
			}
			break;
		case MSG_UHF_POWERLOW:
			// TODO lowpower
			break;
		case MSG_MATCHING_DATA:

			break;
		default:
			super.msgProcess(msg);
			break;
		}
	}

	// 读功能
	public void Read(View v) {
		matchingData = "";
		btnRead = (Button) v;
		String controlText = btnRead.getText().toString();
		if (controlText.equals(getString(R.string.btn_read))) {
			if (PublicData._IsCommand6Cor6B.equals("6C")) {// 读6C标签
				LayoutInflater inflater = getLayoutInflater();
				final View viewDialog = inflater.inflate(R.layout.uhf_dialog,
						(ViewGroup) findViewById(R.id.dialog));
				Builder builder = new Builder(this)
						.setTitle(getString(R.string.uhf_please_input_match_value))
						.setIcon(android.R.drawable.ic_dialog_info)
						.setView(viewDialog)
						.setPositiveButton(getString(R.string.str_ok),
								new DialogInterface.OnClickListener() {// 添加确定按钮
									@Override
									public void onClick(DialogInterface dialog,
											int which) {// 确定按钮的响应事件
										EditText ad_delete_edit = (EditText) viewDialog
												.findViewById(R.id.etname);
										matchingData = ad_delete_edit.getText()
												.toString();
										if (matchingData.length() % 2 == 0
												&& checkHexInput(matchingData)) {
											PingPong_Read();
											btnRead.setText(getString(R.string.btn_read_stop));
											sp_ReadType.setEnabled(false);
										} else {
											showTip(getString(R.string.uhf_data_error));
											btnRead.setText(getString(R.string.btn_read));
											sp_ReadType.setEnabled(true);
										}
									}
								})
						.setNegativeButton(R.string.str_cancel,
								new DialogInterface.OnClickListener() {// 添加确定按钮
									@Override
									public void onClick(DialogInterface dialog,
											int which) {// 取消按钮的响应事件
										PingPong_Read();
										btnRead.setText(getString(R.string.btn_read_stop));
										sp_ReadType.setEnabled(false);
									}
								});
				builder.show();
			} else {// 6B
				showTip(getString(R.string.uhf_6b_not_support_match_mode));
			}
		} else {
			Pingpong_Stop();
			btnRead.setText(getString(R.string.btn_read));
			sp_ReadType.setEnabled(true);
		}

	}

	// 间歇性读
	public void PingPong_Read() {
		if (isStartPingPong)
			return;
		isStartPingPong = true;
		Clear(null);
		Helper_ThreadPool.ThreadPool_StartSingle(new Runnable() {
			@Override
			public void run() {
				while (isStartPingPong) {
					try {
						if (!isPowerLowShow) {
							int rt = 0;
							if (tv_TitleTagID.getText().toString()
									.equals("EPC")) {// EPC
								rt = UHFReader._Tag6C.GetEPC_MatchEPC(_NowAntennaNo,
										1, matchingData);
							} else if (tv_TitleTagID.getText().toString()
									.equals("TID")) {
								rt = UHFReader._Tag6C.GetEPC_TID_MatchTID(
										_NowAntennaNo, 1, matchingData);
							}

							if (!UHF_CheckReadResult(rt)) {
								Pingpong_Stop();
								break;
							}

							synchronized (this) {
								try {
									this.wait(PublicData._PingPong_ReadTime);
								} catch (Exception ex) {
								}
							}

							if (PublicData._PingPong_StopTime > 0) {
								CLReader.Stop();
								synchronized (this) {
									try {
										this.wait(PublicData._PingPong_StopTime);
									} catch (Exception ex) {
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

	// 停止间歇性读
	public void Pingpong_Stop() {
		isStartPingPong = false;
		synchronized (this) {
			this.notifyAll();
		}
		CLReader.Stop();
	}

	public void Clear(View v) {
		totalReadCount = 0;
		readTime = 0;
		hmList.clear();
		ShowList();
	}

	// 返回主页
	public void Back(View v) {
		if (btn_Read.getText().toString()
				.equals(getString(R.string.btn_read_stop))) {
			showMsg(getString(R.string.uhf_please_stop), null);
			return;
		}

		ReadEPCmatchingActivity.this.finish();
	}

	protected void Init() {
		if (!UHF_Init(this)) { // 打开模块电源失败
			showMsg(getString(R.string.uhf_low_power_info),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							ReadEPCmatchingActivity.this.finish();
						}
					});
		} else {
			try {
				super.UHF_GetReaderProperty(); // 获得读写器的能力
				Thread.sleep(20);
				CLReader.Stop(); // 停止指令
				Thread.sleep(20);
				super.UHF_SetTagUpdateParam(); // 设置标签重复上传时间为20ms
			} catch (Exception ee) {
			}
			IsFlushList = true;
			// 刷新线程
			Refush();

			listView = (ListView) this.findViewById(R.id.lv_Main_matching);
			tv_TitleTagID = (TextView) findViewById(R.id.tv_TitleTagID_matching);
			lb_ReadTime = (TextView) findViewById(R.id.lb_ReadTime_matching);
			lb_ReadSpeed = (TextView) findViewById(R.id.lb_ReadSpeed_matching);
			lb_TagCount = (TextView) findViewById(R.id.lb_TagCount_matching);
			btn_Read = (Button) findViewById(R.id.btn_Read_matching);
			btn_Read.setText(getString(R.string.btn_read));
			sp_ReadType = (Spinner) findViewById(R.id.sp_ReadType_matching);
			//

			sp_ReadType
					.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							if (isStartPingPong)
								return;
							int selectItem = sp_ReadType
									.getSelectedItemPosition();
							if (PublicData._IsCommand6Cor6B.equals("6C")) {// 读6C标签
								if (selectItem == 0) {
									_ReadType = 0;
									tv_TitleTagID.setText("EPC");
								} else if (selectItem == 1) {
									_ReadType = 1;
									tv_TitleTagID.setText("TID");
								}
							} else {
								if (selectItem == 0) {
									_ReadType = 0;
									tv_TitleTagID.setText("EPC");
								} else if (selectItem == 1) {
									_ReadType = 1;
									tv_TitleTagID.setText("TID");
								}
							}

						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {

						}

					});

		}
		return;
	}

	protected void Refush()
	{
		IsFlushList = true;
		// 刷新线程
		Helper_ThreadPool.ThreadPool_StartSingle(new Runnable() {
			@Override
			public void run() {
				while (IsFlushList) {
					try {
						sendMessage(MSG_RESULT_READ, null);
						Thread.sleep(1000); // 一秒钟刷新一次
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		Helper_ThreadPool.ThreadPool_StartSingle(new Runnable() { // 蜂鸣器发声
					@Override
					public void run() {
						while (IsFlushList) {
							synchronized (beep_Lock) {
								try {
									beep_Lock.wait();
								} catch (InterruptedException e) {
								}
							}
							if (IsFlushList) {
								toneGenerator
										.startTone(ToneGenerator.TONE_PROP_BEEP);
							}

						}
					}
				});
	}
	
	// 释放资源
	protected void Dispose() {
		isStartPingPong = false;
		IsFlushList = false;
		synchronized (beep_Lock) {
			beep_Lock.notifyAll();
		}
		UHF_Dispose();
	}

	protected void ShowList() {
		if (!isStartPingPong)
			return;
		sa = new SimpleAdapter(this, GetData(), R.layout.epclist_item,
				new String[] { "EPC", "ReadCount" }, new int[] {
						R.id.EPCList_TagID, R.id.EPCList_ReadCount });
		listView.setAdapter(sa);
		listView.invalidate();
		if (lb_ReadTime != null) { // 刷新读取时间
			readTime++;
			lb_ReadTime.setText("Time:" + readTime / 1 + "S");
		}
		if (lb_ReadSpeed != null) { // 刷新读取速度
			if (flag) {
				speed = totalReadCount - lastReadCount;
				if (speed < 0)
					speed = 0;
				lastReadCount = totalReadCount;
				if (lb_ReadSpeed != null) {
					lb_ReadSpeed.setText("SP:" + speed + "T/S");
				}
			}
			// flag = !flag;
		}
		if (lb_TagCount != null) { // 刷新标签总数
			lb_TagCount.setText("Total:" + hmList.size());
		}
	}

	// 获得更新数据源
	@SuppressWarnings({ "rawtypes", "unused" })
	protected List<Map<String, Object>> GetData() {
		List<Map<String, Object>> rt = new ArrayList<Map<String, Object>>();
		synchronized (hmList_Lock) {
			// if(hmList.size() > 0){ //
			Iterator iter = hmList.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = (String) entry.getKey();
				EPCModel val = (EPCModel) entry.getValue();
				Map<String, Object> map = new HashMap<String, Object>();
				if (_ReadType == 0) {
					map.put("EPC", val._EPC);
				} else if (_ReadType == 1) {
					map.put("EPC", val._TID);
				} else {
					map.put("EPC", val._UserData);
				}
				map.put("ReadCount", val._TotalCount);
				rt.add(map);
			}
			// }
		}
		return rt;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 创建
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.setContentView(R.layout.uhf_read_matching);

		showCustomBar(getString(R.string.tv_Read_Title_matching),
				getString(R.string.str_back), null,
				R.drawable.left, 0,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Back(v);
					}
				},
				null
		);
	}


	@Override
	protected void onDestroy() {
		// 释放
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// 待机锁屏
		super.onPause();
		Dispose();
	}

	@Override
	protected void onResume() {
		// 待机恢复
		super.onResume();
		Init();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		//log.d("PDADemo", "onKeyDown keyCode = " + keyCode);
		if ((Adapt.DEVICE_TYPE_HY820 == Adapt.getDeviceType() && (keyCode == KeyEvent.KEYCODE_F9 /* RFID扳机*/ || keyCode == 285  /* 左快捷*/ || keyCode == 286  /* 右快捷*/))
				|| ((Adapt.getSN().startsWith("K3")) && (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F5))
				|| ((Adapt.getSN().startsWith("K6")) && (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F5))) { // 按下扳机

			btn_Read.setText(getString(R.string.btn_read_stop));
			sp_ReadType.setEnabled(false);
			btn_Read.setClickable(false);
			if (!isKeyDown) {
				isKeyDown = true; //
				if (!isStartPingPong) {
					Clear(null);
					Pingpong_Stop(); // 停止间歇性读
					isStartPingPong = true;
					String rt;
					if (PublicData._IsCommand6Cor6B.equals("6C")) {// 读6C标签
						rt = "0";
						if (tv_TitleTagID.getText().toString()
								.equals("EPC")) {// EPC
							rt = ""+ UHFReader._Tag6C.GetEPC_MatchEPC(_NowAntennaNo,
									1, matchingData);
						} else if (tv_TitleTagID.getText().toString()
								.equals("TID")) {
							rt = ""+UHFReader._Tag6C.GetEPC_TID_MatchTID(
									_NowAntennaNo, 1, matchingData);
						}
					} else {// 读6B标签
						rt = CLReader.Get6B(_NowAntennaNo + "|1" + "|1" + "|"
								+ "1,000F");
					}

					int retval = CLReader.GetReturnData(rt);
					if (!UHF_CheckReadResult(retval)) {
						CLReader.Stop();
					}
				}
			} else {
				if (keyDownCount < 10000)
					keyDownCount++;
			}
			if (keyDownCount > 100) {
				isLongKeyDown = true;
			}
			if (isLongKeyDown) { // 长按事件

			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		//log.d("PDADemo", "onKeyUp keyCode = " + keyCode);
		if ((Adapt.DEVICE_TYPE_HY820 == Adapt.getDeviceType() && (keyCode == KeyEvent.KEYCODE_F9 /* RFID扳机*/ || keyCode == 285  /* 左快捷*/ || keyCode == 286  /* 右快捷*/))
				|| ((Adapt.getSN().startsWith("K3")) && (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F5))
				|| ((Adapt.getSN().startsWith("K6")) && (keyCode == KeyEvent.KEYCODE_F1 || keyCode == KeyEvent.KEYCODE_F5))) { // 放开扳机

			CLReader.Stop();
			isStartPingPong = false;
			keyDownCount = 0;
			isKeyDown = false;
			isLongKeyDown = false;
			btn_Read.setText(getString(R.string.btn_read));
			sp_ReadType.setEnabled(true);
			btn_Read.setClickable(true);
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void OutPutEPC(EPCModel model) {
		if (!isStartPingPong)
			return;
		try {
			synchronized (hmList_Lock) {
				if (hmList.containsKey(model._EPC + model._TID)) {
					EPCModel tModel = hmList.get(model._EPC + model._TID);
					tModel._TotalCount++;
				} else {
					hmList.put(model._EPC + model._TID, model);
				}
			}
			synchronized (beep_Lock) {
				beep_Lock.notify();
			}
			totalReadCount++;
		} catch (Exception ex) {
			//log.d("Debug", "Tags output exceptions:" + ex.getMessage());
		}

	}
}
