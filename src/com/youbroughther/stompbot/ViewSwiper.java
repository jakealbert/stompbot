/* Copyright (c) 2010 -- CommonsWare, LLC

	 Licensed under the Apache License, Version 2.0 (the "License");
	 you may not use this file except in compliance with the License.
	 You may obtain a copy of the License at

		 http://www.apache.org/licenses/LICENSE-2.0

	 Unless required by applicable law or agreed to in writing, software
	 distributed under the License is distributed on an "AS IS" BASIS,
	 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 See the License for the specific language governing permissions and
	 limitations under the License.
 */

package com.youbroughther.stompbot;

import java.util.ArrayList;

import android.content.Context;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

public class ViewSwiper extends GestureOverlayView implements
		GestureOverlayView.OnGesturePerformedListener {
	private ParcelHelper parcel = null;
	private GestureLibrary gestureLibrary = null;
	private ViewFlipper flipper = null;
	private int pageIndex = 0;

	public ViewSwiper(Context ctxt) {
		this(ctxt, null);
	}

	public ViewSwiper(Context ctxt, AttributeSet attrs) {
		this(ctxt, attrs, 0);
	}

	public ViewSwiper(Context ctxt, AttributeSet attrs, int defStyle) {
		super(ctxt, attrs, defStyle);

		parcel = new ParcelHelper("cwac-swiper", getContext());
		flipper = new ViewFlipper(getContext());

		addOnGesturePerformedListener(this);
		addView(flipper);

		gestureLibrary = GestureLibraries.fromRawResource(getContext(), parcel
				.getIdentifier("gestures", "raw"));
		gestureLibrary.load();
	}

	@Override
	public void onFinishInflate() {
		int count = getChildCount();
		ArrayList<View> toMove = new ArrayList<View>();

		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);

			if (child != flipper) {
				toMove.add(child);
			}
		}

		for (View child : toMove) {
			removeView(child);
			flipper.addView(child);
		}
	}

	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> predictions = gestureLibrary.recognize(gesture);

		if (predictions.size() > 0) {
			Prediction prediction = predictions.get(0);

			if (prediction.score > 1.0) {
				if (prediction.name.startsWith("West")) {
					moveToNext();
				} else {
					moveToPrevious();
				}
			}
		}
	}

	public ViewFlipper getFlipper() {
		return (flipper);
	}

	protected void moveToNext() {
		if (pageIndex < getChildCount()) {
			pageIndex++;
			flipper.setInAnimation(AnimationUtils.loadAnimation(getContext(),
					R.anim.slide_in_right));
			flipper.setOutAnimation(AnimationUtils.loadAnimation(getContext(),
					R.anim.slide_out_left));
			flipper.showNext();
		}
	}

	protected void moveToPrevious() {
		if (pageIndex > 0) {
			pageIndex--;
			flipper.setInAnimation(AnimationUtils.loadAnimation(getContext(),
					R.anim.slide_in_left));
			flipper.setOutAnimation(AnimationUtils.loadAnimation(getContext(),
					R.anim.slide_out_right));
			flipper.showPrevious();
		}
	}
}
