package com.youbroughther.stompbot;

import android.widget.SeekBar;

public class EffectIOKnob implements EffectIO {
	private int _maxValue = 1024;
	private int _minValue = 0;
	private int _rate = 16;
	private int _curValue = 512;
	private String _name;
	private String _pdcmd;
	private SeekBar _sb;
	private QuickActionWindow qaw;

	public EffectIOKnob(String name) {
		_name = name;
	}

	public EffectIOKnob(String name, int minValue, int maxValue) {
		_name = name;
		_minValue = minValue;
		_maxValue = maxValue;
		_curValue = (maxValue + minValue) / 2;
	}

	public EffectIOKnob(String name, int minValue, int maxValue, int rate) {
		_name = name;
		_minValue = minValue;
		_maxValue = maxValue;
		_curValue = (maxValue + minValue) / 2;
		_rate = rate;
	}

	public int getMinValue() {
		return _minValue;
	}

	public void setMinValue(int minValue) {
		_minValue = minValue;
	}

	public int getMaxValue() {
		return _maxValue;
	}

	public void setMaxValue(int maxValue) {
		this._maxValue = maxValue;
	}

	public int getRate() {
		return _rate;
	}

	public void setRate(int rate) {
		this._rate = rate;
	}

	public int getValue() {
		return _curValue;
	}

	public void setValue(int v) {
		this._curValue = v;
	}

	public void incrementValue() {
		int tmp = _curValue + _rate;
		if (tmp > _maxValue)
			tmp = _maxValue;
		_curValue = tmp;
	}

	public void decrementValue() {
		int tmp = _curValue - _rate;
		if (tmp < _minValue)
			tmp = _minValue;
		_curValue = tmp;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		this._name = name;
	}

	public String getHashName() {
		return _name.replace(" ", "-").toLowerCase() + "-knob";
	}

	public void setSeekBar(SeekBar sb) {
		_sb = sb;
	}

	public SeekBar getSeekBar() {
		return _sb;
	}

	public void setQAW(QuickActionWindow qaw) {
		this.qaw = qaw;
	}

	public QuickActionWindow getQAW() {
		return qaw;
	}

}
