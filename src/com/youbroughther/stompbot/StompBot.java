package com.youbroughther.stompbot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.puredata.android.service.PdPreferences;
import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;
import org.puredata.core.PdReceiver;
import org.puredata.core.utils.IoUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class StompBot extends Activity implements
		SharedPreferences.OnSharedPreferenceChangeListener {

	private class EffectIOSetting {
		int _effect;
		int _io;

		public EffectIOSetting(int effect, int io) {
			_effect = effect;
			_io = io;
		}
	}

	private ArrayList<Effect> effectList;
	private FileInputStream fis = null;
	private Thread serialThread = null;
	static private int tmpEffect;
	private EffectIOSetting[] knobSettings = new EffectIOSetting[3];
	private EffectIOSetting[] switchSettings = new EffectIOSetting[3];
	private EffectIOSetting[] ledSettings = new EffectIOSetting[3];
	private Button[] ledButtons = new Button[3];
	private Button[] knobButtons = new Button[3];
	private Button[] switchButtons = new Button[3];
	private boolean[] switchStates = new boolean[] { false, false, false };
	private boolean[] ledStates = new boolean[] { false, false, false };
	private TextView logs;
	private PdService pdService = null;

	private Handler switchHandler = new Handler() {
		public void handleMessage(Message msg) {
			int knob = msg.getData().getInt("knob");
			onSwitchClick(knob);
		}
	};

	private final ServiceConnection pdConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			pdService = ((PdService.PdBinder) service).getService();
			initPd();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// this method will never be called
		}
	};
	protected boolean alive = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		effectList = new ArrayList<Effect>();
		effectList.add(new EffectInput());
		effectList.add(new EffectLowPass());
		effectList.add(new EffectHighPass());
		// effectList.add(new EffectChorus());
		// effectList.add(new EffectDistortion());
		setContentView(R.layout.dashboard);
		// TextView sevenSeg = (TextView) findViewById(R.id.pure_data_text);
		// sevenSeg.setTypeface(Typeface.createFromAsset(getApplicationContext()
		// .getAssets(), "Clockopia.ttf"), 0);
		ledButtons[0] = (Button) findViewById(R.id.led_one_button);
		ledButtons[1] = (Button) findViewById(R.id.led_two_button);
		ledButtons[2] = (Button) findViewById(R.id.led_three_button);
		switchButtons[0] = (Button) findViewById(R.id.switch_one_button);
		switchButtons[1] = (Button) findViewById(R.id.switch_two_button);
		switchButtons[2] = (Button) findViewById(R.id.switch_three_button);
		knobButtons[0] = (Button) findViewById(R.id.knob_one_button);
		knobButtons[1] = (Button) findViewById(R.id.knob_two_button);
		knobButtons[2] = (Button) findViewById(R.id.knob_three_button);
		refreshButtons();
		ledButtons[0].setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				onLEDLongClick(0, arg0);
				return false;
			}
		});
		ledButtons[1].setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				onLEDLongClick(1, arg0);
				return false;
			}
		});
		ledButtons[2].setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				onLEDLongClick(2, arg0);
				return false;
			}
		});
		knobButtons[0].setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				onKnobLongClick(0, arg0);
				return false;
			}
		});
		knobButtons[1].setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				onKnobLongClick(1, arg0);
				return false;
			}
		});
		knobButtons[2].setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				onKnobLongClick(2, arg0);
				return false;
			}
		});
		switchButtons[0].setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				onSwitchLongClick(0, arg0);
				return false;
			}
		});
		switchButtons[1].setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				onSwitchLongClick(1, arg0);
				return false;
			}
		});
		switchButtons[2].setOnLongClickListener(new View.OnLongClickListener() {
			public boolean onLongClick(View arg0) {
				onSwitchLongClick(2, arg0);
				return false;
			}
		});

		SharedPreferences prefs = getPreferences(MODE_WORLD_READABLE);
		for (int i = 0; i < 3; i++) {
			switchSettings[i] = new EffectIOSetting(prefs.getInt("s" + i + "e",
					-1), prefs.getInt("s" + i + "i", -1));
			if (prefs.getInt("s" + i + "e", -1) != -1) {
				setButtonName(switchButtons[i], effectList.get(
						switchSettings[i]._effect).getName(), effectList.get(
						switchSettings[i]._effect).getSwitches().get(
						switchSettings[i]._io).getName());
				switchButtons[i].setCompoundDrawablesWithIntrinsicBounds(null,
						getResources().getDrawable(R.drawable.spstoff), null,
						null);
			}
			ledSettings[i] = new EffectIOSetting(prefs
					.getInt("l" + i + "e", -1), prefs.getInt("l" + i + "i", -1));
			if (prefs.getInt("l" + i + "e", -1) != -1) {
				setButtonName(ledButtons[i], effectList.get(
						ledSettings[i]._effect).getName(), effectList.get(
						ledSettings[i]._effect).getLEDValues().get(
						ledSettings[i]._io).getName());
				ledButtons[i].setCompoundDrawablesWithIntrinsicBounds(null,
						getResources().getDrawable(R.drawable.ledoff), null,
						null);
			}
			knobSettings[i] = new EffectIOSetting(prefs.getInt("k" + i + "e",
					-1), prefs.getInt("k" + i + "i", -1));
			if (prefs.getInt("k" + i + "e", -1) != -1) {
				setButtonName(knobButtons[i], effectList.get(
						knobSettings[i]._effect).getName(), effectList.get(
						knobSettings[i]._effect).getKnobs().get(
						knobSettings[i]._io).getName());
				knobButtons[i].setCompoundDrawablesWithIntrinsicBounds(null,
						getResources().getDrawable(R.drawable.knobon), null,
						null);
			}
		}
		for (int i = 1; i < effectList.size(); i++) {
			int epos = prefs.getInt(effectList.get(i).getHashName(), i);
			if (epos != i) {
				Effect e = effectList.get(i);
				Effect f = effectList.get(epos);
				effectList.set(i, f);
				effectList.set(epos, e);
				ArrayList<EffectIOKnob> eios = effectList.get(i).getKnobs();
				for (int j = 0; j < eios.size(); j++) {
					eios.get(j).setValue(
							prefs.getInt(effectList.get(i).getHashName() + ":"
									+ j, eios.get(j).getValue()));
				}
			}
		}

		ListView pedals = (ListView) findViewById(R.id.drag_drop_list);
		pedals.setOnCreateContextMenuListener(this);
		((DragDropListView) pedals).setDropListener(mDropListener);

		pedals.setAdapter(new EffectArrayAdapter(getApplicationContext(),
				R.layout.pedalrow, getEffectNamesForList()));
		if (pedals.getChildCount() > 1) {
			pedals.getChildAt(pedals.getChildCount() - 1).findViewById(
					R.id.arrow_down).setVisibility(View.GONE);
		}

		PdPreferences.initPreferences(getApplicationContext());
		logs = (TextView) findViewById(R.id.pure_data_text);
		logs.setMovementMethod(new ScrollingMovementMethod());
		bindService(new Intent(this, PdService.class), pdConnection,
				BIND_AUTO_CREATE);
		serialThread = getSerialThread();
		serialThread.start();
		alive = true;

		pedals.requestLayout();

	}

	public void onPause() {
		super.onPause();
		SharedPreferences prefs = getPreferences(MODE_WORLD_READABLE);
		SharedPreferences.Editor editor = prefs.edit();

		for (int i = 0; i < 3; i++) {
			if (switchSettings[i] != null) {
				editor.putInt("s" + i + "e", switchSettings[i]._effect);
				editor.putInt("s" + i + "i", switchSettings[i]._io);
			}
			if (ledSettings[i] != null) {
				editor.putInt("l" + i + "e", ledSettings[i]._effect);
				editor.putInt("l" + i + "i", ledSettings[i]._io);
			}
			if (knobSettings[i] != null) {
				editor.putInt("k" + i + "e", knobSettings[i]._effect);
				editor.putInt("k" + i + "i", knobSettings[i]._io);
			}
		}
		for (int i = 1; i < effectList.size(); i++) {
			Effect e = effectList.get(i);
			editor.putInt(e.getHashName(), i);
		}
		editor.commit();
	}

	private void cleanup() {

		if (fis != null) {
			alive = false;
			fis = null;
		}
		PdBase.release();
		try {
			unbindService(pdConnection);
		} catch (IllegalArgumentException e) {
			// already unbound
			pdService = null;
		}
	}

	protected void onDestroy() {
		super.onDestroy();
		cleanup();
	}

	public void finish() {
		cleanup();
		super.finish();
	}

	public String[] getEffectNames() {
		String[] names = new String[effectList.size() + 1];
		int i = 0;
		Iterator<Effect> efit = effectList.iterator();
		while (efit.hasNext()) {
			names[i] = efit.next().getName();
			i++;
		}
		names[effectList.size()] = "None";
		return names;
	}

	public String[] getEffectNamesForList() {
		String[] names = new String[effectList.size() - 1];
		int i = 0;
		Iterator<Effect> efit = effectList.iterator();
		efit.next();
		while (efit.hasNext()) {
			names[i] = efit.next().getName();
			i++;
		}
		return names;
	}

	private void setButtonName(Button b, String effect, String setting) {
		b.setText(Html.fromHtml("<b>" + setting
				+ "</b><br/><small><font color='#818181'>" + effect
				+ "</font></small>"));
		b.setTypeface(b.getTypeface(), 0);
	}

	private void refreshButtons() {
		for (int i = 0; i < 3; i++) {
			Button l = ledButtons[i];
			EffectIOSetting ls = ledSettings[i];
			if (ls != null) {
				Effect lef = effectList.get(ls._effect);
				String ln = lef.getName();
				String le = lef.getLEDValues().get(ls._io).getName();
				setButtonName(l, le, ln);
			} else {
				setButtonName(l, "&mdash;", "LED");
			}

			Button k = knobButtons[i];
			EffectIOSetting ks = knobSettings[i];
			if (ks != null) {
				Effect kef = effectList.get(ks._effect);
				String kn = kef.getName();
				String ke = kef.getKnobs().get(ks._io).getName();
				setButtonName(k, ke, kn);
			} else {
				setButtonName(k, "&mdash;", "Knob");
			}

			Button s = switchButtons[i];
			EffectIOSetting ss = switchSettings[i];
			if (ss != null) {
				Effect sef = effectList.get(ss._effect);
				String sn = sef.getName();
				String se = sef.getSwitches().get(ss._io).getName();
				setButtonName(s, se, sn);
			} else {
				setButtonName(s, "&mdash;", "Switch");
			}
		}
	}

	private class KnobOnClickListener implements
			DialogInterface.OnClickListener {
		private int _io;

		public KnobOnClickListener(int io) {
			super();
			_io = io;
		}

		public void onClick(DialogInterface dialog, int item) {
			if (item == effectList.size()) {
				knobSettings[_io] = null;
				setButtonName(knobButtons[_io], "&mdash;", "Knob");
				knobButtons[_io].setCompoundDrawablesWithIntrinsicBounds(null,
						getResources().getDrawable(R.drawable.knoboff), null,
						null);
				serialSend("x" + (_io + 1) + "0");
			} else {
				tmpEffect = item;
				dialog.cancel();
				int selectedKnob = -1;
				if (knobSettings[_io] != null
						&& knobSettings[_io]._effect == item)
					selectedKnob = knobSettings[_io]._io;
				new AlertDialog.Builder(StompBot.this).setTitle("Knob")
						.setSingleChoiceItems(
								effectList.get(item).getKnobNames(),
								selectedKnob,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int item) {
										knobSettings[_io] = new EffectIOSetting(
												tmpEffect, item);
										dialog.dismiss();
										setButtonName(knobButtons[_io],
												effectList.get(tmpEffect)
														.getName(), effectList
														.get(tmpEffect)
														.getKnobs().get(item)
														.getName());
										knobButtons[_io]
												.setCompoundDrawablesWithIntrinsicBounds(
														null,
														getResources()
																.getDrawable(
																		R.drawable.knobon),
														null, null);

										onKnobClick(_io);

									}
								}).show();
			}
		}
	}

	private class SwitchOnClickListener implements
			DialogInterface.OnClickListener {
		private int _io;

		public SwitchOnClickListener(int io) {
			super();
			_io = io;
		}

		public void onClick(DialogInterface dialog, int item) {
			if (item == effectList.size()) {
				switchSettings[_io] = null;
				setButtonName(switchButtons[_io], "&mdash;", "Switch");
				switchStates[_io] = false;
				switchButtons[_io].setCompoundDrawablesWithIntrinsicBounds(
						null, getResources().getDrawable(R.drawable.spstoff),
						null, null);
			} else {
				tmpEffect = item;
				dialog.cancel();
				int selectedSwitch = -1;
				if (switchSettings[_io] != null
						&& switchSettings[_io]._effect == item)
					selectedSwitch = switchSettings[_io]._io;
				new AlertDialog.Builder(StompBot.this).setTitle("Switch")
						.setSingleChoiceItems(
								effectList.get(item).getSwitchNames(),
								selectedSwitch,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int item) {
										switchSettings[_io] = new EffectIOSetting(
												tmpEffect, item);
										dialog.dismiss();
										setButtonName(switchButtons[_io],
												effectList.get(tmpEffect)
														.getName(), effectList
														.get(tmpEffect)
														.getSwitches()
														.get(item).getName());
										if (effectList.get(tmpEffect)
												.getSwitches().get(item)
												.getEnabled()) {
											switchStates[_io] = true;
											switchButtons[_io]
													.setCompoundDrawablesWithIntrinsicBounds(
															null,
															getResources()
																	.getDrawable(
																			R.drawable.spston),
															null, null);
										}
									}
								}).show();
			}
		}
	}

	private class LEDOnClickListener implements DialogInterface.OnClickListener {
		private int _io;

		public LEDOnClickListener(int io) {
			super();
			_io = io;
		}

		public void onClick(DialogInterface dialog, int item) {
			if (item == effectList.size()) {
				ledSettings[_io] = null;
				setButtonName(ledButtons[_io], "&mdash;", "LED");
				ledStates[_io] = false;
				ledButtons[_io].setCompoundDrawablesWithIntrinsicBounds(null,
						getResources().getDrawable(R.drawable.ledoff), null,
						null);
			} else {
				tmpEffect = item;
				dialog.cancel();
				int selectedLED = -1;
				if (ledSettings[_io] != null
						&& ledSettings[_io]._effect == item)
					selectedLED = ledSettings[_io]._io;
				new AlertDialog.Builder(StompBot.this).setTitle("LED Values")
						.setSingleChoiceItems(
								effectList.get(item).getLEDNames(),
								selectedLED,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int item) {
										ledSettings[_io] = new EffectIOSetting(
												tmpEffect, item);
										dialog.dismiss();
										setButtonName(ledButtons[_io],
												effectList.get(tmpEffect)
														.getName(), effectList
														.get(tmpEffect)
														.getLEDValues().get(
																item).getName());
										if (effectList.get(tmpEffect)
												.getLEDValues().get(item)
												.getEnabled()) {
											ledStates[_io] = true;
											ledButtons[_io]
													.setCompoundDrawablesWithIntrinsicBounds(
															null,
															getResources()
																	.getDrawable(
																			R.drawable.ledon5),
															null, null);
										}
									}
								}).show();
			}
		}
	}

	public void onLEDClick(int knob) {
		if (ledSettings[knob] != null) {
			if (ledStates[knob]) {
				ledStates[knob] = false;
				effectList.get(ledSettings[knob]._effect).getLEDValues().get(
						ledSettings[knob]._io).setEnabled(false);
				ledButtons[knob].setCompoundDrawablesWithIntrinsicBounds(null,
						getResources().getDrawable(R.drawable.ledoff), null,
						null);
			} else {
				ledStates[knob] = true;
				effectList.get(ledSettings[knob]._effect).getLEDValues().get(
						ledSettings[knob]._io).setEnabled(true);
				ledButtons[knob].setCompoundDrawablesWithIntrinsicBounds(null,
						getResources().getDrawable(R.drawable.ledon5), null,
						null);
			}
			String floatstr = "1002."
					+ effectList.get(ledSettings[knob]._effect).getHashName()
					+ ".1"
					+ " "
					+ effectList.get(ledSettings[knob]._effect).getLEDValues()
							.get(ledSettings[knob]._io).getHashName();
			int floatval = ledStates[knob] ? 1 : 0;
			post(floatstr + ": " + floatval);
			PdBase.sendFloat(floatstr, floatval);
		}
	}

	public void onLEDLongClick(int knob, View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Effect");
		builder.setItems(getEffectNames(), new LEDOnClickListener(knob));
		builder.create();
		builder.show();
	}

	public void onKnobClick(int knob) {
		View v = knobButtons[knob];
		if (knobSettings[knob] != null) {
			int[] xy = new int[2];
			v.getLocationInWindow(xy);
			Rect rect = new Rect(xy[0], xy[1], xy[0] + v.getWidth(), xy[1]
					+ v.getHeight());

			EffectIOKnob kn = effectList.get(knobSettings[knob]._effect)
					.getKnobs().get(knobSettings[knob]._io);
			if (kn.getQAW() == null) {
				kn.setQAW(new QuickActionWindow(this, v, rect));
			}
			float ledfloat = 10 * kn.getValue()
					/ (kn.getMaxValue() - kn.getMinValue());
			String poststr = ((int) ledfloat) + "";
			if (poststr.equals("10"))
				poststr = ":";
			poststr = "x" + (knob + 1) + poststr;
			serialSend(poststr);
			kn.getQAW().setKnobValues(logs,
					effectList.get(knobSettings[knob]._effect), kn, knob);
			kn.getQAW().show();
		}
	}

	public void onKnobLongClick(int knob, View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Effect");
		builder.setItems(getEffectNames(), new KnobOnClickListener(knob));
		builder.create();
		builder.show();
	}

	public void onSwitchLongClick(int knob, View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Effect");
		builder.setItems(getEffectNames(), new SwitchOnClickListener(knob));
		builder.create();
		builder.show();
	}

	public void onSwitchClick(int knob) {
		if (switchSettings[knob] != null) {
			if (switchStates[knob]) {
				switchStates[knob] = false;
				effectList.get(switchSettings[knob]._effect).getSwitches().get(
						switchSettings[knob]._io).setEnabled(false);
				switchButtons[knob].setCompoundDrawablesWithIntrinsicBounds(
						null, getResources().getDrawable(R.drawable.spstoff),
						null, null);
			} else {
				switchStates[knob] = true;
				effectList.get(switchSettings[knob]._effect).getSwitches().get(
						switchSettings[knob]._io).setEnabled(true);
				switchButtons[knob].setCompoundDrawablesWithIntrinsicBounds(
						null, getResources().getDrawable(R.drawable.spston),
						null, null);
			}
			// serialSend("S:"+knob+":"+(switchStates[knob] ? "1" : "0"));
			if (knob == 0) {
				PdBase.sendFloat("left", switchStates[knob] ? 1 : 0);

			} else if (knob == 1) {
				PdBase.sendFloat("right", switchStates[knob] ? 1 : 0);

			} else {
				PdBase.sendFloat("mic", switchStates[knob] ? 1 : 0);
			}

			String floatstr = "1002."
					+ effectList.get(switchSettings[knob]._effect)
							.getHashName()
					+ ".1"
					+ " "
					+ effectList.get(switchSettings[knob]._effect)
							.getSwitches().get(switchSettings[knob]._io)
							.getHashName();
			int floatval = switchStates[knob] ? 1 : 0;
			post(floatstr + ": " + floatval);
			PdBase.sendFloat(floatstr, floatval);

		}
	}

	public void onLEDOneClick(View v) {
		onLEDClick(0);
	}

	public void onLEDTwoClick(View v) {
		onLEDClick(1);
	}

	public void onLEDThreeClick(View v) {
		onLEDClick(2);
	}

	public void onKnobOneClick(View v) {
		onKnobClick(0);
	}

	public void onKnobTwoClick(View v) {
		onKnobClick(1);
	}

	public void onKnobThreeClick(View v) {
		onKnobClick(2);
	}

	public void onSwitchOneClick(View v) {
		onSwitchClick(0);
	}

	public void onSwitchTwoClick(View v) {
		onSwitchClick(1);
	}

	public void onSwitchThreeClick(View v) {
		onSwitchClick(2);
	}

	public void onRefreshButtonClick(View v) {
		View vv1 = findViewById(R.id.pure_data_loading);
		View vv2 = findViewById(R.id.pure_data_text);
		vv2.setVisibility(View.GONE);
		vv1.setVisibility(View.VISIBLE);
		cleanup();
		serialThread = getSerialThread();
		serialThread.start();
		alive = true;
		PdPreferences.initPreferences(getApplicationContext());
		logs.setText("");
		bindService(new Intent(this, PdService.class), pdConnection,
				BIND_AUTO_CREATE);
		vv1.setVisibility(View.GONE);
		vv2.setVisibility(View.VISIBLE);

	}

	public void onHomeButtonClick(View v) {
		View vv1 = findViewById(R.id.order_view);
		View vv2 = findViewById(R.id.knob_view);
		ImageButton homeicon = (ImageButton) findViewById(R.id.title_home_icon);
		ImageButton knobicon = (ImageButton) findViewById(R.id.title_knob_icon);
		homeicon.setBackgroundColor(Color.WHITE);
		homeicon.setImageResource(R.drawable.ic_title_home_alt);
		knobicon.setBackgroundColor(Color.BLACK);
		knobicon.setImageResource(R.drawable.ic_menu_settings);

		vv2.setVisibility(View.GONE);
		vv1.setVisibility(View.VISIBLE);

	}

	public void onKnobButtonClick(View v) {
		View vv1 = findViewById(R.id.order_view);
		View vv2 = findViewById(R.id.knob_view);
		ImageButton homeicon = (ImageButton) findViewById(R.id.title_home_icon);
		ImageButton knobicon = (ImageButton) findViewById(R.id.title_knob_icon);
		homeicon.setBackgroundColor(Color.BLACK);
		homeicon.setImageResource(R.drawable.ic_title_home);
		knobicon.setBackgroundColor(Color.WHITE);
		knobicon.setImageResource(R.drawable.ic_menu_settings_alt);

		vv1.setVisibility(View.GONE);
		vv2.setVisibility(View.VISIBLE);
	}

	public void onSettingsButtonClick(View v) {
		startActivity(new Intent(this, PdPreferences.class));
	}

	public void serialSend(String str) {
		try {
			FileOutputStream fos = new FileOutputStream("/dev/ttyMSM0");
			PrintStream p = new PrintStream(fos);
			p.println(str);
			fos.close();
		} catch (Exception e) {
			post(e.toString());
			return;
		}
	}

	public Thread getSerialThread() {
		return new Thread(new Runnable() {
			public void run() {
				char[] oldv = new char[3];
				while (alive) {
					try {
						char c;
						fis = new FileInputStream("/dev/ttyMSM0");
						BufferedReader isr = new BufferedReader(
								new InputStreamReader(fis));
						while (alive) {
							String line = isr.readLine();
							if (line.length() > 0) {
								char[] linec = line.toCharArray();
								if (linec[0] == 'x' && linec[1] == 'k') {
									int knob = Character
											.getNumericValue(linec[2]) - 1;
									if (knobSettings[knob] != null) {
										EffectIOKnob kn = effectList.get(
												knobSettings[knob]._effect)
												.getKnobs().get(
														knobSettings[knob]._io);
										char v = linec[3];
										if (v == '-') {
											kn.decrementValue();
										} else if (v == '1') {
											kn.incrementValue();
										}
										if (kn.getSeekBar() != null) {
											kn.getSeekBar().setProgress(
													kn.getValue()
															+ kn.getMinValue());
											float ledfloat = 10
													* kn.getValue()
													/ (kn.getMaxValue() - kn
															.getMinValue());
											String poststr = ((int) ledfloat)
													+ "";
											if (poststr.equals("10"))
												poststr = ":";
											poststr = "x" + (knob + 1)
													+ poststr;
											serialSend(poststr);
										}
									}
								} else if (linec[0] == 'x' && linec[1] == 's') {
									int knob = Character
											.getNumericValue(linec[2]) - 1;
									if (linec[3] != oldv[knob]) {
										oldv[knob] = linec[3];
										if (switchSettings[knob] != null) {
											Message msg = new Message();
											Bundle b = new Bundle();
											b.putInt("knob", knob);
											msg.setData(b);
											switchHandler
													.sendMessageAtFrontOfQueue(msg);
										}
									}
								}
							}
						}
					} catch (Exception e) {
						// post(e.toString() + "\n");
					}
				}
			}
		});
	}

	private DragDropListView.DropListener mDropListener = new DragDropListView.DropListener() {
		public void drop(int from, int to) {
			if (to < effectList.size() - 1) {
				Effect ef = effectList.get(from + 1);
				effectList.remove(from + 1);
				effectList.add(to + 1, ef);
				for (int i = 1; i < effectList.size(); i++) {
					Effect eff = effectList.get(i);
					String floatstr = eff.getName().toLowerCase() + "-receive-from";
					
					String chfromstr;
					if(i == 1) {
						chfromstr = "clean-input";
					} else {
						eff = effectList.get(i-1);
						chfromstr = eff.getName().toLowerCase() + "-output";
					}
					
					//int floatval = i - 1;
					//post(floatstr + ": " + floatval);
					//PdBase.sendFloat(floatstr, floatval);

					post(floatstr + ": " + chfromstr);
					PdBase.sendSymbol(floatstr, chfromstr);
				}
			}
			ListView pedals = (ListView) findViewById(R.id.drag_drop_list);
			pedals.setAdapter(new EffectArrayAdapter(getApplicationContext(),
					R.layout.pedalrow, getEffectNamesForList()));
			pedals.requestLayout();
		}
	};

	private class EffectArrayAdapter extends ArrayAdapter<String> {
		private Context mContext;
		private int mLayoutId;
		private String[] mListContent;

		public EffectArrayAdapter(Context context, int textViewResourceId,
				String[] objects) {
			super(context, textViewResourceId, objects);
			mContext = context;
			mLayoutId = textViewResourceId;
			mListContent = objects;
		}

		public View getView(int position, View rowView, ViewGroup parent) {
			if (rowView != null) {
				return rowView;
			}
			LayoutInflater inflater = LayoutInflater.from(mContext);
			View v = inflater.inflate(mLayoutId, null);

			TextView rowTitle = (TextView) v.findViewById(R.id.text1);
			rowTitle.setOnClickListener(new OnClickListener(){

				public void onClick(View arg0) {
					
				}});
			rowTitle.setText(mListContent[position]);
			if (position == effectList.size() - 2) {
				v.findViewById(R.id.arrow_down).setVisibility(View.INVISIBLE);
			}
			return v;
		}

	}

	private PdReceiver receiver = new PdReceiver() {

		private void pdPost(String msg) {
			toast("Pure Data says, \"" + msg + "\"");
		}

		@Override
		public void print(String s) {
			post(s);
		}

		@Override
		public void receiveBang(String source) {
			pdPost("bang");
		}

		@Override
		public void receiveFloat(String source, float x) {
			pdPost("float: " + x);
		}

		@Override
		public void receiveList(String source, Object... args) {
			pdPost("list: " + Arrays.toString(args));
		}

		@Override
		public void receiveMessage(String source, String symbol, Object... args) {
			pdPost("message: " + Arrays.toString(args));
		}

		@Override
		public void receiveSymbol(String source, String symbol) {
			pdPost("symbol: " + symbol);
		}
	};

	private void initPd() {
		Resources res = getResources();
		File patchFile = null;
		try {
			PdBase.setReceiver(receiver);
			PdBase.subscribe("android");
			InputStream in = res.openRawResource(R.raw.test);
			patchFile = IoUtils.extractResource(in, "stompbot.pd", getCacheDir());
			PdBase.openPatch(patchFile);
			startAudio();
			Log.d(getResources().getString(R.string.app_name), "loaded"+patchFile.getName());
		} catch (IOException e) {
			Log.e(getResources().getString(R.string.app_name), e.toString());
			finish();
		} finally {
			if (patchFile != null)
				patchFile.delete();
		}
	}

	private void startAudio() {
		String name = getResources().getString(R.string.app_name);
		try {

			pdService.initAudio(-1, -1, -1, -1); // negative values will be
			// replaced with
			// defaults/preferences
			pdService.startAudio(new Intent(this, StompBot.class),
					R.drawable.icon, name, "Return to " + name + ".");
			PdBase.sendFloat("left", 0);
			PdBase.sendFloat("right", 0);
			PdBase.sendFloat("mic", 0);

		} catch (IOException e) {
			toast(e.toString());
		}
	}

	private Toast toast = null;

	private void toast(final String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	public void post(final String s) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				logs.append("\n" + s.trim());
			}
		});
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		startAudio();

	}

}