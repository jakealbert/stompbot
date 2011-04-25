package com.youbroughther.stompbot;

import java.util.ArrayList;

public class EffectExample extends Effect {
	public EffectExample() {
		super("Example");
		ArrayList<EffectIOKnob> knobs = new ArrayList<EffectIOKnob>();
		knobs.add(new EffectIOKnob("Gain"));
		knobs.add(new EffectIOKnob("Reverb"));
		knobs.add(new EffectIOKnob("Tone"));
		this._knobs = knobs;
		ArrayList<EffectIOSwitch> switches = new ArrayList<EffectIOSwitch>();
		switches.add(new EffectIOSwitch("Enable"));
		switches.add(new EffectIOSwitch("Overdrive"));
		this._switches = switches;
		ArrayList<EffectIOLED> leds = new ArrayList<EffectIOLED>();
		leds.add(new EffectIOLED("Gain"));
		leds.add(new EffectIOLED("Reverb"));
		leds.add(new EffectIOLED("Input"));
		leds.add(new EffectIOLED("Output"));
		this._leds = leds;

	}

}
