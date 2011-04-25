package com.youbroughther.stompbot;

public class EffectIOSwitch implements EffectIO {
	private boolean enabled = false;
	private String _name;

	public EffectIOSwitch(String name) {
		_name = name;
	}

	public boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(boolean b) {
		this.enabled = b;
	}

	public String getName() {
		return _name;
	}

	public void setName(String name) {
		_name = name;
	}

	void toggle() {
		enabled = !enabled;
	}

	public String getHashName() {
		return _name.replace(" ", "-").toLowerCase() + "-switch";
	}

	public int getValue() {
		return enabled ? 1 : 0;
	}

	public void setValue(int v) {
		enabled = (v == 1);
	}

}
