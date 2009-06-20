package viper.api.impl;

import java.net.*;

import viper.api.*;
import viper.api.time.*;

/**
 * An implementation of the viper api's media element class.
 */
public class MediaElementImpl implements MediaElement {
	private String filename;
	private boolean frameBased = true;
	private FrameRate rate;
	private InstantInterval span = new Span(new Frame(1), new Frame(2));

	/**
	 * Creates a new metadata object for the given clip
	 * with the given rate.
	 * @param fname the name of the clip. This must be a valid URI.
	 * @param rate the frame rate
	 * @throws IllegalArgumentException if fname is not a valid URI or rate
	 * is not a valid rate.
	 */
	public MediaElementImpl(String fname, FrameRate rate) {
		this.rate = rate;
		this.filename = fname;
		URI.create(this.filename); // to throw illegal argument exception
	}

	/**
	 * Since it is common for the filename to be unambiguous,
	 * this just returns the filename's hash code.
	 * @return <code>filename.hashCode()</code>
	 */
	public int hashCode() {
		return filename.hashCode();
	}
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj instanceof MediaElement) {
			MediaElement that = (MediaElement) obj;
			return filename.equals(that.getSourcefileName());
		} else {
			return false;
		}
	}

	/**
	 * Returns the rate of frames per unit of time.
	 * @return FrameRate
	 */
	public FrameRate getFrameRate() {
		return rate;
	}
	/**
	 * @see viper.api.MediaElement#setFrameRate(viper.api.time.FrameRate)
	 */
	public void setFrameRate(FrameRate rate) {
		this.rate = rate;
	}
	/**
	 * @see viper.api.MediaElement#normalize(viper.api.time.Instant)
	 */
	public Instant normalize(Instant i) {
		if (frameBased) {
			return rate.asFrame(i);
		} else {
			return rate.asTime(i);
		}
	}
	/**
	 * @see viper.api.MediaElement#normalize(viper.api.time.InstantInterval)
	 */
	public InstantInterval normalize(InstantInterval s) {
		if (frameBased) {
			return rate.asFrame(s);
		} else {
			return rate.asTime(s);
		}
	}

	/**
	 * @see viper.api.MediaElement#getSourcefileName()
	 */
	public String getSourcefileName() {
		return filename;
	}

	/**
	 * @see viper.api.MediaElement#setSpan(viper.api.time.InstantInterval)
	 */
	public void setSpan(InstantInterval r) {
		this.span = r;
	}

	/**
	 * @see viper.api.MediaElement#getSpan()
	 */
	public InstantInterval getSpan() {
		return this.span;
	}

	public URI getSourcefileIdentifier() {
		try {
			String sfname = getSourcefileName();
			return new URI(sfname);
		} catch (URISyntaxException e1) {
			throw new IllegalStateException();
		}
	}
}
