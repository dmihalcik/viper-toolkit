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

package edu.umd.cfar.lamp.viper.gui.players;

import java.awt.*;
import java.awt.image.*;

import viper.api.*;
import viper.api.time.*;
import viper.api.time.Frame;
import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

/**
 * This player is the one that is launched when all the others fail,
 * or, alternatively, when the media file cannot be located.
 * @author davidm
 */
public class NotFoundPlayer extends DataPlayer {
	private Image nullImage = null;
	private Span nullSpan = new Span(new Frame(1), new Frame(2));
	private FrameRate nullRate = new RationalFrameRate(30, 1);
	private Instant now = null;
	protected PrefsManager prefs;
	/**
	 * @return Returns the prefs.
	 */
	public PrefsManager getPrefs() {
		return prefs;
	}
	/**
	 * @param prefs The prefs to set.
	 */
	public void setPrefs(PrefsManager prefs) {
		this.prefs = prefs;
	}

	public Image getImage() {
		return nullImage;
	}
	public Image getImage(Instant i) {
		return nullImage;
	}
	public Span getSpan() {
		return nullSpan;
	}

	public Instant getNow() {
		return now;
	}
	public void setNow(Instant i) {
		// XXX-davidm: should this expand 'nullSpan' to include i?
		this.now = i;
	}

	public FrameRate getRate() {
		return nullRate;
	}

	public void destroy() {
	}
	public int nextIndex() {
		return 0;
	}

	public int previousIndex() {
		return now == null ? 0 : now.intValue() - 1;
	}
	public void remove() {
		throw new UnsupportedOperationException();
	}
	public boolean hasNext() {
		return now != null && nullSpan.contains(now.next());
	}
	public boolean hasPrevious() {
		return now != null && nullSpan.contains(now.previous());
	}

	public Object next() {
		return nullImage;
	}
	public Object previous() {
		return nullImage;
	}
	public void add(Object o) {
		throw new UnsupportedOperationException();
	}
	public void set(Object o) {
		throw new UnsupportedOperationException();
	}
	public Rational getPixelAspectRatio() {
		return new Rational(1);
	}
	/**
	 * Always returns 'empty'.
	 * @param i instant to check. Any instant works.
	 * @return {@link edu.umd.cfar.lamp.viper.gui.players.DataPlayer#EMPTY_FRAME empty-frame}
	 * @see edu.umd.cfar.lamp.viper.gui.players.DataPlayer#getImageType(viper.api.time.Instant)
	 */
	public String getImageType(Instant i) {
		return EMPTY_FRAME;
	}
	/**
	 * Modifies the current span and rate as indicated in the element
	 * @param element {@inheritDoc} 
	 */
	public void setElement(MediaElement element) {
		nullRate = element.getFrameRate();
		InstantInterval ii = element.getSpan();
		Frame s = nullRate.asFrame(ii.getStartInstant());
		Frame e = nullRate.asFrame(ii.getEndInstant());
		ViperData v = getMediator().getViperData();
		Sourcefile sf = v.getSourcefile(element.getSourcefileName());
		if (sf != null) {
			ii = nullRate.asFrame(viper.api.impl.Util.guessMediaInterval(sf));
			s = (Frame) (s.compareTo(ii.getStart()) < 0 ? s : ii.getStart());
			e = (Frame) (e.compareTo(ii.getEnd()) > 0 ? e : ii.getEnd());
		}
		nullSpan = new Span(s,e);
	}
	
	/** {@inheritDoc} */
	public void setMediator(ViperViewMediator mediator) {
		this.mediator = mediator;
	}
	public ViperViewMediator getMediator() {
		return this.mediator ;
	}
	private ViperViewMediator mediator;

	public ImageProducer getImageProducer() {
		return null;
	}
}
