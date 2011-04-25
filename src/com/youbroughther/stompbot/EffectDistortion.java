package com.youbroughther.stompbot;

import java.util.ArrayList;

public class EffectDistortion extends Effect {
	public EffectDistortion() {
		super("Distortion");
		ArrayList<EffectIOKnob> knobs = new ArrayList<EffectIOKnob>();
		knobs.add(new EffectIOKnob("Tone"));
		knobs.add(new EffectIOKnob("Distortion", -1024, 1024));
		knobs.add(new EffectIOKnob("Level"));
		this._knobs = knobs;
		ArrayList<EffectIOSwitch> switches = new ArrayList<EffectIOSwitch>();
		switches.add(new EffectIOSwitch("Enable"));
		this._switches = switches;
		ArrayList<EffectIOLED> leds = new ArrayList<EffectIOLED>();
		leds.add(new EffectIOLED("Tone"));
		leds.add(new EffectIOLED("Distortion"));
		leds.add(new EffectIOLED("Level"));
		this._leds = leds;
	}
}
