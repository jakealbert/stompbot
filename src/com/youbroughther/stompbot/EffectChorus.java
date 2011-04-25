package com.youbroughther.stompbot;

import java.util.ArrayList;

public class EffectChorus extends Effect {
	public EffectChorus() {
		super("Chorus");
		ArrayList<EffectIOKnob> knobs = new ArrayList<EffectIOKnob>();
		knobs.add(new EffectIOKnob("Level"));
		knobs.add(new EffectIOKnob("Equalizer"));
		knobs.add(new EffectIOKnob("Rate"));
		knobs.add(new EffectIOKnob("Depth"));
		this._knobs = knobs;
		ArrayList<EffectIOSwitch> switches = new ArrayList<EffectIOSwitch>();
		switches.add(new EffectIOSwitch("Enable"));
		this._switches = switches;
		ArrayList<EffectIOLED> leds = new ArrayList<EffectIOLED>();
		leds.add(new EffectIOLED("Level"));
		leds.add(new EffectIOLED("Equalizer"));
		leds.add(new EffectIOLED("Rate"));
		leds.add(new EffectIOLED("Depth"));
		this._leds = leds;
	}
}
