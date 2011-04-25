package com.youbroughther.stompbot;

public class EffectIOLED implements EffectIO {
	private String _name;
	private boolean enabled = false;

	public EffectIOLED(String name) {
		_name = name;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		this._name = name;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean b) {
		enabled = b;
	}

	public String getHashName() {
		return _name.replace(" ", "-").toLowerCase() + "-led";
	}

	public int getValue() {
		return enabled ? 1 : 0;
	}

	public void setValue(int v) {
		enabled = (v == 1);
	}

}
