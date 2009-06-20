/***************************************
 *            ViPER                    *
 *  The Video Processing               *
 *         Evaluation Resource         *
 *                                     *
 *  Distributed under the GPL license  *
 *        Terms available at gnu.org.  *
 *                                     *
 *  Copyright University of Maryland,  *
 *                      College Park.  *
 ***************************************/

package edu.umd.cfar.lamp.viper.gui.remote;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import viper.api.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * A class for controlling video playback, a feature not provided by the 
 * mediator or the DataPlayer.
 */
public class ViperControls {
	private boolean paused = false;
	private boolean looping = false;
	private static Rational MULTIPLIER_THRESHOLD = new Rational(1, 16);
	public static Object[] MULTIPLIERS = new Rational[] {
		new Rational(-256),
		new Rational(-128),
		new Rational(-64),
		new Rational(-32),
		new Rational(-16),
		new Rational(-8),
		new Rational(-4),
		new Rational(-2),
		new Rational(-1),
		new Rational(-1,2),
		new Rational(-1,4),
		new Rational(-1,8),
		new Rational(-1,16),
		new Rational(1,16),
		new Rational(1,8),
		new Rational(1,4),
		new Rational(1,2),
		new Rational(1),
		new Rational(2),
		new Rational(4),
		new Rational(8),
		new Rational(16),
		new Rational(32),
		new Rational(64),
		new Rational(128),
		new Rational(256),
	};
	
	/// set this to the region you wish to play back.
	private ChronicleSelectionModel playbackSelected = null;
	private ChangeListener playbackSelectedListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			ViperControls.this.fireChangeEvent();
		}
	};
	private long framesPerTick = 1;
	private int resolutionOfTicks = 125;
	private Rational lastFractionalFrame = new Rational(1);
	private Rational framesPerSecondOfVideo = new Rational(30, 1);
	private Rational frameRateMultiplier = new Rational(1);
	private Rational realFrameRate = new Rational(framesPerSecondOfVideo);
	private boolean realtimePlayback = true;
	private long lastTickTime = 0;
	private ViperViewMediator mediator;
	private EventListenerList changeListeners = new EventListenerList();
	
	public void addChangeListener(ChangeListener cl) {
		changeListeners.add(ChangeListener.class, cl);
	}
	public void removeChangeListener(ChangeListener cl) {
		changeListeners.remove(ChangeListener.class, cl);
	}
	protected void fireChangeEvent() {
		// TODO: coagulate events to the same listener
		Object[] L = changeListeners.getListenerList();
		ChangeEvent e = null;
		for (int i = L.length - 2; i >= 0; i -= 2) {
			if (L[i] == ChangeListener.class) {
				if (e == null) {
					e = new ChangeEvent(this);
				}
				((ChangeListener) L[i+1]).stateChanged(e);
			}
		}
	}

	private Timer ticker;

	/**
	 * Goes to instant i, if possible. If i is out
	 * of range, goes to end of range closest to i.
	 * Doesn't fire stateChanged event.
	 * @param i the instant to go to
	 * @return if the exact instant was used. 
	 */
	boolean smartGo(Instant i) {
		if (mediator == null || i == null) {
			return false;
		}
		boolean good = false;
		Instant now = mediator.getMajorMoment();
		FrameRate fr = mediator.getDataPlayer().getRate();
		boolean alreadyThere = i.equals(now);
		TemporalRange tr = getPlaybackRange();
		if (tr == null) {
			return false;
		} else {
			good = tr.contains(i);
		}
		if (!good) {
			Instant real = null;
			if (fr.compare(i, now) > 0) {
				// i greater than current moment
				Comparable c = tr.firstBefore(i);
				Incrementable before_end = null;
				if (c != null) {
					before_end = (Incrementable) tr.endOf(c);
				}
				if (before_end != null && fr.compare(before_end.previous(), now) > 0) {
					real = (Instant) before_end.previous();
				} else {
					Comparable after_start = tr.firstAfter(i);
					if (after_start != null) {
						real = (Instant) after_start;
					}
				}
			} else {
				// i less than current moment
				Comparable after_start = tr.firstAfter(i);
				if (after_start != null && fr.compare(after_start, now) < 0) {
					real = (Instant) after_start;
				} else {
					Comparable c = tr.firstBefore(i);
					Incrementable before_end = (Incrementable) ((c == null) ? null : tr.endOf(c));
					if (before_end != null) {
						real = (Instant) before_end.previous();
					}
				}
			}
			if (real != null) {
				mediator.setMajorMoment(real);
			}
		}
		if (good && !alreadyThere) {
			mediator.setMajorMoment(i);
		}
		return good;
	}
	public TemporalRange getPlaybackRange() {
		if (playbackSelected != null && playbackSelected.getSelectedTime() != null) {
			return playbackSelected.getSelectedTime();
		} else {
			Sourcefile sf = mediator.getCurrFile();
			if (sf != null) {
				return sf.getRange();
			}
		}
		return null;
	}

	private Clicker task = new Clicker();
	private class Clicker implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (!paused && null != mediator) {
				Instant now = mediator.getMajorMoment();
				if (now.longValue() != lastFractionalFrame.longValue()) {
					lastFractionalFrame = new Rational(now.longValue());
				}
				if (isRealtimePlayback()) {
					long sysTime = System.currentTimeMillis();
					boolean futured = (sysTime < lastTickTime);
					if (lastTickTime > 0 && !futured) {
						long diff = sysTime - lastTickTime;
						Rational temp = new Rational(0);
						Rational delta = framesInSpecifiedMilliseconds(diff);
						Rational.plus(lastFractionalFrame, delta, temp);
						TemporalRange tr = getPlaybackRange();
						Instant startInstant = (Instant) tr.getExtrema().getStart();
						Rational start = new Rational (startInstant.longValue());
						Instant endInstant = (Instant) tr.getExtrema().getEnd();
						Rational end = new Rational (endInstant.longValue()-1);
						if (temp.lessThan(start)) {
							temp = start;
						} else if (temp.greaterThan(end)) {
							temp = end;
						}
						long oldTick = now.longValue();
						long newTick = temp.longValue();
						if (oldTick != newTick) {
							smartGo(now.go(newTick - oldTick));
						} else if (temp.equals(lastFractionalFrame)) {
							// at end of the line
							if (isLooping()) {
								smartGo(startInstant);
								temp = start;
							} else {
								setPaused(true);
							}
						}
						lastFractionalFrame = temp;
					}
					if (!futured) {
						lastTickTime = sysTime;
					}
				} else {
					if (null != now) {
						fireChangeEvent();
						smartGo(now.go(framesPerTick));
					}
				}
			}
		}
	}
	

	public void pause() {
		if (!paused) {
			paused = true;
			lastTickTime = 0;
			ticker.stop();
			fireChangeEvent();
		}
	}
	public void play() {
		if (paused && null != mediator) {
			paused = false;
			if (mediator.getMajorMoment().next().equals(getPlaybackRange().getExtrema().getEnd())) {
				smartGo((Instant) getPlaybackRange().getExtrema().getStart());
			}
			ticker.start();
			fireChangeEvent();
		}
	}
	
	private Rational changeMultiplier(boolean forward) {
		Rational multiplier = getFrameRateMultiplier();
		if (!forward) {
			multiplier.negate();
		}
		if (multiplier.lessThan(MULTIPLIER_THRESHOLD.negate()) && MULTIPLIER_THRESHOLD.negate() != null) {
			MULTIPLIER_THRESHOLD.negate();
			Rational.divide(multiplier, new Rational(2), multiplier);
		} else if (multiplier.isNegative()) {
			MULTIPLIER_THRESHOLD.negate();
			multiplier.setTo(MULTIPLIER_THRESHOLD);
		} else {
			Rational.multiply(multiplier, new Rational(2), multiplier);
		}
		if (!forward) {
			multiplier.negate();
		}
		return multiplier;
	}

	
	public void accelerate() {
		if (isRealtimePlayback()) {
			setFrameRateMultiplier(changeMultiplier(true));
		} else {
			setRate(getRate() + 1);
		}
	}
	
	public boolean isForward() {
		if (isRealtimePlayback()) {
			return getFrameRateMultiplier().isPositive();
		} else {
			return 0 < getRate();
		}
	}
	
	public void setMultiplier(Rational m) {
		if (isRealtimePlayback()) {
			setFrameRateMultiplier(m);
		} else {
			setRate(m.longValue());
		}
	}
	public Rational getMultiplier() {
		if (isRealtimePlayback()) {
			return getFrameRateMultiplier();
		} else {
			return new Rational(getRate());
		}
	}

	public void decelerate() {
		if (isRealtimePlayback()) {
			setFrameRateMultiplier(changeMultiplier(false));
		} else {
			setRate(getRate() - 1);
		}
	}

	/**
	 * @return
	 */
	public ViperViewMediator getMediator() {
		return mediator;
	}

	/**
	 * @return
	 */
	public boolean isPaused() {
		return paused;
	}

	/**
	 * @return
	 */
	public long getRate() {
		return framesPerTick;
	}

	/**
	 * @return
	 */
	public long getResolution() {
		return resolutionOfTicks;
	}

	/**
	 * @param mediator
	 */
	public void setMediator(ViperViewMediator mediator) {
		this.mediator = mediator;
	}

	/**
	 * @param b
	 */
	public void setPaused(boolean b) {
		if (b) {
			pause();
		} else {
			play();
		}
	}

	/**
	 * @param l
	 */
	public void setRate(long l) {
		lastDelayedAction = System.currentTimeMillis();
		framesPerTick = l;
		fireChangeEvent();
	}

	/**
	 * @param l
	 */
	public void setResolution(int l) {
		lastDelayedAction = System.currentTimeMillis();
		resolutionOfTicks = l;
		ticker.setDelay(resolutionOfTicks);
		fireChangeEvent();
	}

	public void nextFrame() {
		if (paused && null != mediator) {
			Instant now = mediator.getMajorMoment();
			if (null != now) {
				smartGo(now.go(1));
				fireChangeEvent();
			}
		}
	}

	public void previousFrame() {
		if (paused && null != mediator) {
			Instant now = mediator.getMajorMoment();
			if (null != now) {
				smartGo(now.go(-1));
				fireChangeEvent();
			}
		}
	}
	Rational framesInSpecifiedMilliseconds(long millis) {
		Rational temp = new Rational(millis, 1000);
		Rational.multiply(realFrameRate, temp, temp);
		return temp;
	}
	long framesPlayedInASecond() {
		if (realtimePlayback) {
			return realFrameRate.longValue();
		}
		return framesPerTick * 1000 / resolutionOfTicks; 
	}
	
	public void previousSection() {
		if (null == mediator) {
			return;
		}
		Instant now = mediator.getMajorMoment();
		Instant newI = null;
		if (playbackSelected != null && playbackSelected.getSelectedTime() != null) {
			TemporalRange tr = playbackSelected.getSelectedTime();
			newI = (Instant) tr.firstBefore(now);
			long threshold = framesPlayedInASecond() / 2;
			while (!paused && newI != null) {
				long diff = now.longValue() - newI.longValue();
				if (diff > threshold) {
					break;
				}
				newI = (Instant) tr.firstBefore(newI);
			}
		}
		if (newI == null) {
			newI = mediator.getFocusInterval().getStartInstant();
		}
		smartGo(newI);
		fireChangeEvent();
	}

	public void nextSection() {
		if (null == mediator) {
			return;
		}
		Instant now = mediator.getMajorMoment();
		Instant newI = null;
		if (playbackSelected != null && playbackSelected.getSelectedTime() != null) {
			TemporalRange tr = playbackSelected.getSelectedTime();
			newI = (Instant) tr.firstAfter(now);
			if (newI == null) {
				newI = (Instant) tr.endOf(now);
			}
		}
		if (newI == null) {
			newI = (Instant) mediator.getFocusInterval().getEndInstant().previous();
		}
		smartGo(newI);
		fireChangeEvent();
	}

	public ViperControls() {
		ticker = new Timer(resolutionOfTicks, task);
	}
	
	/**
	 * @return Returns the playbackSelected.
	 */
	public ChronicleSelectionModel getPlaybackSelected() {
		return playbackSelected;
	}
	/**
	 * @param playbackSelected The playbackSelected to set.
	 */
	public void setPlaybackSelected(ChronicleSelectionModel playbackSelected) {
		if (this.playbackSelected != null) {
			this.playbackSelected.removeChangeListener(this.playbackSelectedListener);
		}
		this.playbackSelected = playbackSelected;
		if (this.playbackSelected != null) {
			this.playbackSelected.addChangeListener(this.playbackSelectedListener);
		}
	}
	/**
	 * @return Returns the frameRate.
	 */
	public Rational getFrameRate() {
		return framesPerSecondOfVideo;
	}
	/**
	 * @param frameRate The frameRate to set.
	 */
	public void setFrameRate(Rational frameRate) {
		this.framesPerSecondOfVideo = frameRate;
		Rational.multiply(this.framesPerSecondOfVideo, this.frameRateMultiplier, this.realFrameRate);
	}
	/**
	 * @return Returns the frameRateMultiplier.
	 */
	public Rational getFrameRateMultiplier() {
		return frameRateMultiplier;
	}
	/**
	 * @param frameRateMultiplier The frameRateMultiplier to set.
	 */
	public void setFrameRateMultiplier(Rational frameRateMultiplier) {
		lastDelayedAction = System.currentTimeMillis();
		this.frameRateMultiplier = new Rational(frameRateMultiplier);
		Rational.multiply(this.framesPerSecondOfVideo, this.frameRateMultiplier, this.realFrameRate);
		fireChangeEvent();
	}
	/**
	 * @return Returns the realtimePlayback.
	 */
	public boolean isRealtimePlayback() {
		return realtimePlayback;
	}
	/**
	 * @param realtimePlayback The realtimePlayback to set.
	 */
	public void setRealtimePlayback(boolean realtimePlayback) {
		this.realtimePlayback = realtimePlayback;
	}
	
	/**
	 * Number of milliseconds ago to apply pause/play actions.
	 */
	public static final int DELAY_CORRECTION_FACTOR = 400;
	
	private long lastDelayedAction = System.currentTimeMillis();
	
	/**
	 * 
	 */
	public void humanPause() {
		if (!isPaused()) {
			setPaused(true);
			Rational rate = getFrameRate();
			Rational multiplier = getFrameRateMultiplier();
			Rational skipBack = new Rational();
			long errTime = Math.min(DELAY_CORRECTION_FACTOR, System.currentTimeMillis() - lastDelayedAction);
			Rational.multiply(rate, new Rational(DELAY_CORRECTION_FACTOR, 1000), skipBack);
			Rational.multiply(multiplier, skipBack, skipBack);
			smartGo(getMediator().getMajorMoment().go(-skipBack.longValue()));
		}
	}
	
	/**
	 * 
	 */
	public void humanPlay() {
		if (!isPaused() && getFrameRateMultiplier().equals(1)) {
			return;
		}
		humanPause();

		Rational r = getMultiplier();
		if (r.lessThan(0) || r.greaterThan(1)) {
			setMultiplier(new Rational(1));
		}
		//lastTickTime = System.currentTimeMillis() + DELAY_CORRECTION_FACTOR * 2;
		play();
	}

	public void humanAccelerate() {
		if (isPaused() || getFrameRateMultiplier().intValue() < 1 || getFrameRateMultiplier().intValue() >= 16) {
			humanPlay();
			return;
		}
		humanPause();
		setFrameRateMultiplier(new Rational(getFrameRateMultiplier().intValue() * 2));
		play();
	}
	
	public static final int SNAP_BACK_TIME = 8000;
	public void snapBack() {
		// jumps back 8 seconds x frame rate multiplier
		boolean playAfter = !isPaused();
		Rational oldMultiplier = getFrameRateMultiplier();
		Rational jumpFrames = new Rational();
		humanPause();
		Rational.multiply(oldMultiplier, framesInSpecifiedMilliseconds(SNAP_BACK_TIME), jumpFrames);
		smartGo(mediator.getCurrentFrame().go(-jumpFrames.ceiling().intValue()));
		if (playAfter) {
			play();
		}
	}
	public boolean isLooping() {
		return looping;
	}
	public void setLooping(boolean looping) {
		if (this.looping == looping) { // no change
			return;
		}
		this.looping = looping;
		fireChangeEvent();
	}
}
