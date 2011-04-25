package com.youbroughther.stompbot;

import java.util.ArrayList;

public class EffectLowPass extends Effect {
	public EffectLowPass() {
		super("Low Pass Filter");
		ArrayList<EffectIOKnob> knobs = new ArrayList<EffectIOKnob>();
		knobs.add(new EffectIOKnob("Cut-Off Frequency", 0, 20000, 200));
		this._knobs = knobs;
		ArrayList<EffectIOSwitch> switches = new ArrayList<EffectIOSwitch>();
		switches.add(new EffectIOSwitch("Enable"));
		this._switches = switches;
		ArrayList<EffectIOLED> leds = new ArrayList<EffectIOLED>();
		leds.add(new EffectIOLED("Frequency"));
		this._leds = leds;
	}
}
