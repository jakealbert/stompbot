package com.youbroughther.stompbot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Effect {
	private String _name;
	private ArrayList<EffectIO> _controls = null;
	protected ArrayList<EffectIOLED> _leds = new ArrayList<EffectIOLED>();
	protected ArrayList<EffectIOKnob> _knobs = new ArrayList<EffectIOKnob>();
	protected ArrayList<EffectIOSwitch> _switches = new ArrayList<EffectIOSwitch>();

	public Effect(String name) {
		_name = name;
	}

	public ArrayList<EffectIOKnob> getKnobs() {
		return _knobs;
	}

	public ArrayList<EffectIOSwitch> getSwitches() {
		return _switches;
	}

	public ArrayList<EffectIOLED> getLEDValues() {
		return _leds;
	}

	public ArrayList<EffectIO> getControls() {
		if (_controls == null) {
			_controls = new ArrayList<EffectIO>();
			_controls.addAll(_leds);
			_controls.addAll(_knobs);
			_controls.addAll(_switches);
		}
		return _controls;
	}

	public String[] getLEDNames() {
		String[] names = new String[_leds.size()];
		Iterator<EffectIOLED> eioit = _leds.iterator();
		int i = 0;
		while (eioit.hasNext()) {
			names[i] = eioit.next().getName();
			i++;
		}
		return names;
	}

	public String[] getKnobNames() {
		String[] names = new String[_knobs.size()];
		Iterator<EffectIOKnob> eioit = _knobs.iterator();
		int i = 0;
		while (eioit.hasNext()) {
			names[i] = eioit.next().getName();
			i++;
		}
		return names;
	}

	public String[] getSwitchNames() {
		String[] names = new String[_switches.size()];
		Iterator<EffectIOSwitch> eioit = _switches.iterator();
		int i = 0;
		while (eioit.hasNext()) {
			names[i] = eioit.next().getName();
			i++;
		}
		return names;
	}

	public String getName() {
		return _name;
	}

	public String getHashName() {
		return _name.replace(" ", "-").toLowerCase();
	}

}
