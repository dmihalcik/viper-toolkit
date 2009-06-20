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

package viper.descriptors.attributes;

import java.util.*;

import viper.comparison.distances.*;
import viper.descriptors.*;
import viper.filters.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * This class represents the numeric span of frames in a video, consisting of
 * the beginning and end frame and a bitmask on all frames in between, as well
 * as several actions upon them, such as intersection, union, and size. Frame
 * indexes cannot fall below zero.
 * 
 * @author davidm
 */
public class FrameSpan implements Cloneable, Filterable, Composable, Measurable {
	static {
		Distances.HelperMeasureDistance d;

		d = new Distances.HelperMeasureDistance(new DiceDistance(), "dice",
				Distance.BALANCED, "Dice coefficient", true);
		Distances.putDistanceFunctorFor(" framespan", d);

		d = new Distances.HelperMeasureDistance(new OverlapDistance(),
				"overlap", Distance.TARG_V_CANDS, "Target overlap", true);
		Distances.putDistanceFunctorFor(" framespan", d);

		d = new Distances.HelperMeasureDistance(new TemporalRecall(), "recall",
				Distance.TARG_V_CANDS, "Temporal Recall", true);
		Distances.putDistanceFunctorFor(" framespan", d);

		d = new Distances.HelperMeasureDistance(new TemporalPrecision(),
				"precision", Distance.CAND_V_TARGS, "Temporal Precision", true);
		Distances.putDistanceFunctorFor(" framespan", d);

		d = new Distances.HelperMeasureDistance(new ExtentDistance(), "extent",
				Distance.BALANCED, "Normalized extent", true);
		Distances.putDistanceFunctorFor(" framespan", d);
		Distances.putDistanceFunctorFor(" framespan", Distances
				.getEqualityDistance());
		try {
			DefaultMeasures.setDefaultMetricFor(" framespan", "e");
		} catch (ImproperMetricException imx) {
			throw new RuntimeException(imx.getMessage());
		}
		DefaultMeasures.setDefaultToleranceFor(" framespan", 0.0);
	}

	private static abstract class HelpFrameSpanDistance implements
			Distances.QuickValueDistance {
		/** @inheritDoc */
		public Number helpGetDistance(Measurable.Difference D) {
			FrameSpanDiff fsd;
			if (D instanceof FrameSpanDiff) {
				fsd = (FrameSpanDiff) D;
			} else {
				try {
					fsd = new FrameSpanDiff((FrameSpan) D.getAlpha(),
							(FrameSpan) D.getBeta(), (FrameSpan) D
									.getBlackout(), (FrameSpan) D.getIgnore(),
							D.getFileInformation());
				} catch (IgnoredValueException ivx) {
					throw new RuntimeException("Unexpected exception: "
							+ ivx.getMessage());
				}
			}
			return helpGetDistance(fsd);
		}

		/**
		 * Helper method for the child methods to implement.
		 * Saves on some casting.
		 * @param fsd the difference of two frame span objects
		 * @return the distance, per the implementation of a distance
		 * metric
		 */
		public abstract Number helpGetDistance(FrameSpanDiff fsd);
	}

	private static class DiceDistance extends HelpFrameSpanDistance {
		/** @inheritDoc */
		public Number helpGetDistance(FrameSpanDiff fsd) {
			return new Double(1.0 - ((2.0 * fsd.getShared()) / (fsd
					.getTargetFrames() + fsd.getCandidateFrames())));
		}

		/** @inheritDoc */
		public boolean equals(Object o) {
			return (this == o) || (o instanceof DiceDistance);
		}

		/** @inheritDoc */
		public int hashCode() {
			return this.getClass().hashCode();
		}
	}

	private static class OverlapDistance extends HelpFrameSpanDistance {
		/** @inheritDoc */
		public Number helpGetDistance(FrameSpanDiff fsd) {
			return new Double(1.0 - ((double) fsd.getShared() / fsd
					.getTargetFrames()));
		}

		/** @inheritDoc */
		public boolean equals(Object o) {
			return (this == o) || (o instanceof OverlapDistance);
		}

		/** @inheritDoc */
		public int hashCode() {
			return this.getClass().hashCode();
		}
	}

	private static class TemporalRecall extends HelpFrameSpanDistance {
		/** @inheritDoc */
		public Number helpGetDistance(FrameSpanDiff fsd) {
			return new Double((double) fsd.getShared() / fsd.getTargetFrames());
		}

		/** @inheritDoc */
		public boolean equals(Object o) {
			return (this == o) || (o instanceof TemporalRecall);
		}

		/** @inheritDoc */
		public int hashCode() {
			return this.getClass().hashCode();
		}
	}

	private static class TemporalPrecision extends HelpFrameSpanDistance {
		/** @inheritDoc */
		public Number helpGetDistance(FrameSpanDiff fsd) {
			return new Double((double) fsd.getShared()
					/ fsd.getCandidateFrames());
		}

		/** @inheritDoc */
		public boolean equals(Object o) {
			return (this == o) || (o instanceof TemporalPrecision);
		}

		/** @inheritDoc */
		public int hashCode() {
			return this.getClass().hashCode();
		}
	}

	private static class ExtentDistance extends HelpFrameSpanDistance {
		/** @inheritDoc */
		public Number helpGetDistance(FrameSpanDiff fsd) {
			return new Double(1.0 - Math.exp(-FrameSpan.alpha
					* fsd.getExtents()));
		}

		/** @inheritDoc */
		public boolean equals(Object o) {
			return (this == o) || (o instanceof ExtentDistance);
		}

		/** @inheritDoc */
		public int hashCode() {
			return this.getClass().hashCode();
		}
	}

	/**
	 * The factor used to apply to the extents distance exponent when converting it
	 * from an unbounded number to the range from zero to one.
	 */
	public static double alpha = 1;

	boolean contiguous = true;

	int beg;

	int end;

	int[] mask = null;

	/**
	 * Default constructor; returns an empty framespan.
	 */
	public FrameSpan() {
		beg = 0;
		end = -1;
	}

	/**
	 * Tests to see if there are any frames in this frame span.
	 * @return if a frame is in this frame span
	 */
	public boolean isEmpty() {
		if (beg <= end && mask != null) {
			for (int i = 0; i < mask.length; i++) {
				if (mask[i] != 0) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Initializes a new FrameSpan with specified bounds. All frames within and
	 * including the bounds are set.
	 * 
	 * @param start
	 *            the index of the first frame
	 * @param finish
	 *            the index of the last frame
	 */
	public FrameSpan(int start, int finish) {
		contiguous = true;
		beg = start;
		end = finish;
		if (start > finish) {
			mask = new int[0];
			return;
		} else {
			mask = FrameSpan.genMask(beg, end);
		}
	}

	private static final int[] genMask(int beg, int end) {
		int[] mask = new int[(end >> 5) - (beg >> 5) + 1];
		int begRem = beg & 0x1F;
		int endRem = (end & 0x1F) + 1;
		for (int i = 0; i < mask.length; i++) {
			mask[i] = 0xFFFFFFFF;
		}

		for (int i = 0; i < begRem; i++) {
			mask[0] <<= 1;
		}

		int temp = 0xFFFFFFFF;
		for (int i = endRem; i < 32; i++) {
			temp >>>= 1;
		}
		mask[mask.length - 1] &= temp;

		return mask;
	}

	/**
	 * Gets the hash code as the xor of the first and last frames, and
	 * the internal bit mask.
	 * @return {@inheritDoc}
	 */
	public int hashCode() {
		return beg ^ end ^ (mask == null ? 0 : mask.hashCode());
	}

	/**
	 * Tests the equality of two FrameSpans. First compares the Beginning and
	 * End frame number, then each individual frame.
	 * 
	 * @param other
	 *            the span to compare with
	 * @return true only if this and the specified span refer to the same set of
	 *         frames.
	 */
	public boolean equals(FrameSpan other) {
		if ((beg != other.beg) || (end != other.end)) {
			return false;
		} else if (this.contiguous && other.contiguous) {
			return true;
		} else {
			try {
				for (int i = 0; i < mask.length; i++) {
					if (mask[i] != other.mask[i]) {
						return false;
					}
				}
			} catch (ArrayIndexOutOfBoundsException aioobx) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Tests to see if <code>this</code> span intersects with the other.
	 * 
	 * @param other
	 *            the span to check
	 * @return <code>true</code> if there is a shared frame
	 */
	public boolean intersects(FrameSpan other) {
		int current = Math.max(other.beg, beg);
		int finish = Math.min(other.end, end);
		if (current > finish) {
			return false;
		} else if (this.contiguous && other.contiguous) {
			return true;
		} else {
			while (current <= finish) {
				if (containsFrame(current) && other.containsFrame(current))
					return true;
				current++;
			}
			return false;
		}
	}

	/**
	 * Returns the span that is shared between this and the other.
	 * 
	 * <pre>
	 * 
	 *           |---------- ---|
	 *                   |---- -----|
	 *  becomes:         |-- - -|
	 *  
	 * </pre>
	 * 
	 * @param other
	 *            The FrameSpan that this is to be intersected with.
	 * @return The span that the two share.
	 */
	public FrameSpan intersect(FrameSpan other) {
		int newBeg = Math.max(other.beg, beg);
		int newEnd = Math.min(other.end, end);
		FrameSpan f;

		if (newBeg > newEnd) {
			int avg = (newBeg + newEnd) / 2;
			f = new FrameSpan(avg, avg - 1);
		} else {
			f = new FrameSpan(newBeg, newEnd);
			if (!this.contiguous || !other.contiguous) {
				int thisOffset = (f.beg >> 5) - (beg >> 5);
				int otherOffset = (f.beg >> 5) - (other.beg >> 5);
				for (int i = 0; i < f.mask.length; i++) {
					int nmask = other.mask[i + otherOffset]
							& mask[i + thisOffset];
					if (f.mask[i] != nmask) {
						f.mask[i] = nmask;
						f.contiguous = false;
					}
				}
				f.helpCrop();
			}
		}
		return f;
	}

	/**
	 * Returns this span withou any frames in the other.
	 * 
	 * <pre>
	 * 
	 *          |---------- ---|
	 *                   |---- -----|
	 *  becomes: |------|     H
	 *  
	 * </pre>
	 * 
	 * @param other
	 *            The FrameSpan to remove from <code>this</code>
	 * @return Equivalent to looping through all of the frames of
	 *         <code>other</code> and calling {@link #clear(int)}for each
	 *         valid frame.
	 */
	public FrameSpan minus(FrameSpan other) {
		if (beginning() > other.ending() || ending() < other.beginning()) {
			return (FrameSpan) this.clone();
		} else if (this.contiguous && other.contiguous) {
			if (beginning() < other.beginning() && ending() > other.ending()) {
				return new FrameSpan(beginning(), other.beginning() - 1)
						.union(new FrameSpan(other.ending() + 1, ending()));
			} else if (beginning() >= other.beginning()) {
				return new FrameSpan(other.ending() + 1, ending());
			} else {
				return new FrameSpan(beginning(), other.beginning() - 1);
			}
		} else {
			FrameSpan f = (FrameSpan) this.clone();
			int last = Math.min(this.end, other.end) / 32;
			int start = Math.max(this.beg, other.beg) / 32;
			int thisOffset = -(f.beg / 32) + start;
			int otherOffset = -(other.beg / 32) + start;

			for (int i = 0; i < last - start; i++) {
				f.mask[i + thisOffset] = ~other.mask[i + otherOffset]
						& mask[i + thisOffset];
			}
			f.helpCrop();

			return f;
		}
	}

	/**
	 * Removes leading and training empty mask ints.
	 */
	private void helpCrop() {
		if (mask != null && mask.length > 0
				&& !(containsFrame(beg) && containsFrame(end))) {
			int firstMask = -1;
			int lastMask = 0;
			int partialBlocks = 0;
			for (int i = 0; i < mask.length; i++) {
				if (mask[i] != 0) {
					if (mask[i] != 0xFFFFFFFF) {
						partialBlocks++;
					}
					lastMask = i;
					firstMask = (firstMask == -1) ? i : firstMask;
				}
			}
			if (firstMask == -1) {
				mask = null;
				beg = 0;
				end = -1;
				contiguous = true;
				return;
			}
			int[] newMask = new int[lastMask - firstMask + 1];
			System.arraycopy(mask, firstMask, newMask, 0, newMask.length);
			int newBeg = Math.max(beg, ((beg >> 5) + firstMask) << 5);
			int newEnd = Math.min(end, ((beg >> 5) + lastMask + 1) << 5);
			boolean foundB = false;
			boolean foundE = false;
			for (int i = 0; (i < 32) && !(foundB && foundE)
					&& (newBeg <= newEnd); i++) {
				if (!foundB) {
					if (!containsFrame(newBeg)) {
						newBeg++;
					} else {
						foundB = true;
					}
				}
				if (!foundE) {
					if (!containsFrame(newEnd)) {
						newEnd--;
					} else {
						foundE = true;
					}
				}
			}
			if (!foundB || !foundE) {
				if (newBeg <= newEnd) {
					FrameSpan f = new FrameSpan();
					beg = f.beg;
					end = f.end;
					mask = f.mask;
					contiguous = f.contiguous;
				} else {
					throw new RuntimeException(
							"Error while reducing framespan: " + this);
				}
			} else {
				beg = newBeg;
				end = newEnd;
				mask = newMask;
				if (mask.length == 1) {
					int q = 0;
					for (int i = beg; i < end; i++) {
						switch (q) {
						case 0:
							q = containsFrame(i) ? 1 : 0;
							break;
						case 1:
							q = containsFrame(i) ? 1 : 2;
							break;
						case 2:
							q = containsFrame(i) ? 3 : 2;
							break;
						default:
							break;
						}
					}
					contiguous = (q != 3);
				} else {
					switch (partialBlocks) {
					case 0:
						contiguous = true;
						break;
					case 1:
					case 2:
						if ((partialBlocks == 1 && (mask[0] != 0xFFFFFFFF || mask[mask.length - 1] != 0xFFFFFFFF))
								|| (partialBlocks == 2 && mask[0] != 0xFFFFFFFF && mask[mask.length - 1] != 0xFFFFFFFF)) {
							int q = 0;
							for (int i = beg; i < ((beg >> 5) + 1) << 5; i++) {
								switch (q) {
								case 0:
									q = containsFrame(i) ? 1 : 0;
									break;
								case 1:
									q = containsFrame(i) ? 1 : 2;
									break;
								default:
									break;
								}
							}
							for (int i = (end >> 5) << 5; i < end; i++) {
								switch (q) {
								case 1:
									q = containsFrame(i) ? 1 : 3;
									break;
								case 3:
									q = containsFrame(i) ? 2 : 3;
									break;
								default:
									break;
								}
							}
							contiguous = (q != 3 && q != 1);
						} else {
							contiguous = false;
						}
					default:
						contiguous = false;
					}
				}
			}
		}
	}

	/**
	 * Removes all frames in this frame span not present
	 * in the other frame span.
	 * @param other the span to intersect with
	 */
	public void intersectWith(FrameSpan other) {
		FrameSpan f;
		if (other == null) {
			f = new FrameSpan();
		} else {
			f = this.intersect(other);
		}
		beg = f.beg;
		end = f.end;
		mask = f.mask;
		contiguous = f.contiguous;
	}

	/**
	 * Returns the span that is shared between the first beginning and the last
	 * end. That is:
	 * 
	 * <pre>
	 * 
	 *           |---- ----- - --|
	 *                   |-- --- ----|
	 *  becomes: |---- ----- --------|
	 *  
	 * </pre>
	 * 
	 * and
	 * 
	 * <pre>
	 * 
	 *           |---------|
	 *                          |----------|
	 *  becomes: |----------    -----------|
	 *  
	 * </pre>
	 * 
	 * @param other -
	 *            the FrameSpan that this is to be unioned with
	 * @return the span covered by either or between both
	 */
	public FrameSpan union(FrameSpan other) {
		if ((other == null) || (other.mask == null) || (other.mask.length == 0))
			return (FrameSpan) this.clone();
		if ((this == null) || (mask == null) || (other.mask.length == 0))
			return (FrameSpan) other.clone();

		FrameSpan f = new FrameSpan(Math.min(other.beg, beg), Math.max(
				other.end, end));
		if (f.beg > f.end) {
			int avg = (f.beg + f.end) / 2;
			return (new FrameSpan(avg, avg - 1));
		} else if (this.intersects(other) && this.contiguous
				&& other.contiguous) {
			return f;
		}

		f.clearAll();
		f.contiguous = false;

		// Do part that is only covered by one
		int thisOffset = (beg / 32) - (f.beg / 32);
		int otherOffset = (other.beg / 32) - (f.beg / 32);
		if (otherOffset == 0) {
			for (int i = 0; i < thisOffset; i++) {
				if (i < other.mask.length)
					f.mask[i] = other.mask[i];
				else
					f.mask[i] = 0;
			}
		} else if (thisOffset == 0) {
			for (int i = 0; i < otherOffset; i++) {
				if (i < mask.length)
					f.mask[i] = mask[i];
				else
					f.mask[i] = 0;
			}
		}

		// Do the shared part
		int maximum = (Math.min(other.end, end) / 32) - (f.beg / 32) + 1;
		for (int i = Math.max(thisOffset, otherOffset); i < maximum; i++) {
			f.mask[i] = other.mask[i - otherOffset] | mask[i - thisOffset];
		}

		// Do the end
		thisOffset = (end / 32) - (f.end / 32);
		otherOffset = (other.end / 32) - (f.end / 32);

		if (otherOffset < 0) {
			if (mask.length + otherOffset < 0)
				otherOffset = -mask.length;
			for (int i = otherOffset; i < 0; i++) {
				f.mask[f.mask.length + i] = mask[mask.length + i];
			}
		} else if (thisOffset < 0) {
			if (other.mask.length + thisOffset < 0)
				thisOffset = -other.mask.length;
			for (int i = thisOffset; i < 0; i++) {
				f.mask[f.mask.length + i] = other.mask[other.mask.length + i];
			}
		}

		f.helpCrop();
		return f;
	}

	/**
	 * Shifts all frames by the specified offset. For example, if this framespan
	 * represents frames 100 through 150 and it is shifted by -75, it will then
	 * represent frames 25 through 75. This is useful for encoding several
	 * different DescVectors into one DescVector.
	 * 
	 * @param offset
	 *            the number of frames to shift the span by
	 * @throws IndexOutOfBoundsException
	 *             if there is a negative frame
	 */
	public void shift(int offset) {
		if (beg + offset < 0) {
			throw new IndexOutOfBoundsException(
					"Cannot have a negative frame value: " + offset);
		}
		int newBeg = beg + offset;
		int newEnd = end + offset;
		if (contiguous) {
			mask = FrameSpan.genMask(newBeg, newEnd);
		} else {
			int[] newMask = new int[(newEnd >> 5) - (newBeg >> 5) + 1];
			int minorShift = (beg & 0x1a) - (newBeg & 0x1a);

			if (minorShift < 0) {
				minorShift = -minorShift;
				int altShift = 32 - minorShift;
				int upperMask = 0xFFFFFFFF >> altShift;
				newMask[0] = (mask[0] << minorShift);
				for (int i = 1; i < newMask.length; i++) {
					if (i < mask.length)
						newMask[i] = mask[i] << minorShift; // newBeg is greater
															// than beg
					newMask[i] = newMask[i]
							| ((mask[i - 1] >> altShift) & upperMask);
				}
				mask = newMask;
			} else if (minorShift > 0) {
				int altShift = 32 - minorShift;
				int upperMask = 0xFFFFFFFF >> minorShift;
				for (int i = 0; i < newMask.length - 1; i++) {
					newMask[i] = mask[i] >> minorShift;
					if (i + 1 < mask.length)
						newMask[i] = newMask[i]
								| ((upperMask & mask[i + 1]) << altShift);
				}
				newMask[newMask.length - 1] = mask[newMask.length - 1] >> minorShift;
				mask = newMask;
			}
		}
		beg = newBeg;
		end = newEnd;
	}

	/**
	 * Returns the total number of frames included in the span. Counts both
	 * active and inactive frames.
	 * 
	 * @return The length of the frame ({@link #ending() ending}-
	 *         {@link #beginning() beginning}+ 1).
	 */
	public int size() {
		return (beg > end) ? 0 : (end - beg + 1);
	}

	/**
	 * Returns a copy of the FrameSpan.
	 * 
	 * @return A new FrameSpan object equal to the original.
	 */
	public Object clone() {
		FrameSpan f = new FrameSpan();
		f.beg = beg;
		f.end = end;
		if (mask != null) {
			f.mask = new int[mask.length];
			f.contiguous = contiguous;
			for (int i = 0; i < mask.length; i++) {
				f.mask[i] = mask[i];
			}
		} else {
			f.mask = null;
		}
		return f;
	}

	/**
	 * Returns a String representing the FrameSpan.
	 * 
	 * @return Either the standard output, or a comma seperated list of
	 *         FrameSpans in the standard output format, like "1:5, 7:10", to
	 *         indicate a span from 1:10 that is missing frame 6.
	 */
	public String toString() {
		if (contiguous && beg <= end) {
			return beg + ":" + end;
		}
		// (state1) --found: print first number--> (state2) --found: print
		// colon--> (state3)
		// (state4) -- see above, but with comma -->
		//      <--not found: print :(current-1)
		//        <----------------------------------------not found: print number-1--
		int state = 1;
		StringBuffer buf = new StringBuffer();
		for (int current = beg; current <= end + 1; current++) {
			if (containsFrame(current)) {
				switch (state) {
				case 1:
					buf.append(current);
					state = 2;
					break;
				case 2:
					buf.append(':');
					state = 3;
					break;
				case 4:
					buf.append(", ").append(current);
					state = 2;
					break;
				}
			} else {
				switch (state) {
				case 2:
					buf.append(':').append(current - 1);
					state = 4;
					break;
				case 3:
					buf.append(current - 1);
					state = 4;
					break;
				}
			}
		}
		if (buf.length() == 0)
			return "NULL";
		return buf.toString();
	}

	/**
	 * Reads a String in and parses it. The format looks like
	 * <code>Start Frame</code>:<code>End Frame</code>, eg "1:1" or
	 * "10:1000". Please note that you might have to write your own parse
	 * function to read the {@link #toString() toString}method's output,
	 * probably by using a StringTokenizer or the
	 * {@link StringHelp#splitBySeparator(String line,char sep) like}.
	 * 
	 * @param S
	 *            the FrameSpan string. Must be in the form ##:##
	 * @throws BadDataException
	 *             if the data is malformed
	 * @return a new FrameSpan object that covers the frames the String
	 *         specified
	 */
	public static FrameSpan parseFrameSpan(String S) throws BadDataException {
		try {
			StringTokenizer st = new StringTokenizer(S, ", ");
			FrameSpan span = null;
			while (st.hasMoreTokens()) {
				StringTokenizer qst = new StringTokenizer(st.nextToken(), ":");
				FrameSpan tempSpan = new FrameSpan(Integer.parseInt(qst
						.nextToken()), Integer.parseInt(qst.nextToken()));
				span = tempSpan.union(span);
			}
			return span;
		} catch (NumberFormatException nfx) {
			throw new BadDataException("Malformed frame span: '" + S + "'");
		} catch (NoSuchElementException nsex) {
			throw new BadDataException("Malformed frame span: \"" + S + "");
		}
	}

	/**
	 * Determines if this span and another's bounds are within a certain
	 * tolerance
	 * 
	 * @param other
	 *            FrameSpan to compare this FrameSpan with.
	 * @param tolerance
	 *            Integer value representing the number of frames each
	 *            terminating frame is allowed from truth.
	 * @return <code>true</code> if they are within the tolerance.
	 */
	public boolean coincide(FrameSpan other, int tolerance) {
		return ((other.beg >= (beg - tolerance))
				&& (other.beg <= (beg + tolerance))
				&& (other.end >= (end - tolerance)) && (other.end <= (end + tolerance)));
	}

	/**
	 * Calculates the overlap of another FrameSpan with this one.
	 * 
	 * @param other
	 *            The FrameSpan to compare this with.
	 * @return (the number of frames in the intersection) / (the number of
	 *         frames in this)
	 */
	public double overlap(FrameSpan other) {
		return (double) intersect(other).numFrames() / (double) numFrames();
	}

	private static int[] quickener = new int[] { 0, 1, 1, 2, 1, 2, 2, 3, 1, 2,
			2, 3, 2, 3, 3, 4 };

	/**
	 * Counts the number of active frames in this FrameSpan.
	 * 
	 * @return the number of frames that are active in the specified span
	 */
	public int numFrames() {
		if (end < beg) {
			return 0;
		} else if (contiguous) {
			return end - beg + 1;
		} else {
			int count = 0;
			for (int i = 0; i < mask.length; i++) {
				if (mask[i] == 0xFFFFFFFF) {
					count += 32;
				} else {
					for (int j = 0; j < 32; j += 4) {
						count += quickener[(mask[i] >>> j) & 15];
					}
				}
			}
			return count;
		}
	}

	/**
	 * Tests to see if a given frame is contained in this FrameSpan.
	 * 
	 * @param frameNumber
	 *            The index to find.
	 * @return <code>true</code> if the frame is set.
	 */
	public boolean containsFrame(int frameNumber) {
		if ((frameNumber < 0) || (end < beg)) {
			return false;
		}
		int major = frameNumber >> 5;
		int minor = frameNumber & 0x1F;
		if ((major >= (beg >> 5)) && (major <= (end >> 5)))
			return (0 != (mask[major - (beg >> 5)] & (1 << minor)));
		return false;
	}

	/**
	 * Returns the sum of the absolute values of the differences between the
	 * beginning and ending points of this FrameSpan object and another.
	 * 
	 * @param other
	 *            The FrameSpan to compare with this.
	 * @return The sum of the absolute values of the extents.
	 */
	public int extents(FrameSpan other) {
		return (((beg < other.beg) ? (other.beg - beg) : (beg - other.beg)) + ((end < other.end) ? (other.end - end)
				: (end - other.end)));
	}

	/**
	 * Returns the index of the end frame of this FrameSpan.
	 * 
	 * @return The index of the last frame.
	 */
	public int ending() {
		return end;
	}

	/**
	 * Returns the index of the first frame of this FrameSpan.
	 * 
	 * @return The index of the first frame.
	 */
	public int beginning() {
		return beg;
	}

	/**
	 * Sets the specified frame to active.
	 * 
	 * @param frameNumber
	 *            the index of the frame to set. If out of this FrameSpan's
	 *            boundary, it will increase in size to accomidate.
	 * @throws IndexOutOfBoundsException
	 *             if the frame is negative
	 */
	public void set(int frameNumber) {
		if (frameNumber < 0)
			throw (new IndexOutOfBoundsException(
					"Trying to access a negative frame " + frameNumber));
		FrameSpan f = new FrameSpan(frameNumber, frameNumber);
		f = f.union(this);
		beg = f.beg;
		end = f.end;
		mask = f.mask;
		contiguous = f.contiguous;
	}

	/**
	 * Determines if the span is contiguous. Empty framespans are defined as
	 * contiguous.
	 * 
	 * @return <code>true</code> if the span contains all frames in its extent
	 */
	public boolean isContiguous() {
		return contiguous;
	}

	/**
	 * Returns a list of non-adjacent, individually contiguous FrameSpan objects
	 * from a single disjoint framespan. They are in order.
	 * 
	 * @return a list of contiguous framespans that, together, construct this
	 *         one
	 */
	public List split() {
		List temp = new LinkedList();
		if (contiguous) {
			temp.add(clone());
		} else {
			boolean outside = true;
			int start = 0;
			for (int i = beg; i < end + 2; i++) {
				if (outside && containsFrame(i)) {
					outside = false;
					start = i;
				} else if (!outside && !containsFrame(i)) {
					temp.add(new FrameSpan(start, i - 1));
					outside = true;
				}
			}
		}
		return temp;
	}

	/**
	 * Removes all frames in the range start to stop, inclusive.
	 * @param start the first frame to remove
	 * @param stop the last frame to remove
	 */
	public void clear(int start, int stop) {
		if (start < 0) {
			throw new IndexOutOfBoundsException(
					"Trying to access a negative frame: " + start);
		} else if (stop < start) {
			throw new IllegalArgumentException(
					"Cannot erase negative frame span: " + start + ":" + stop);
		} else if (start <= ending() && stop <= beginning()) {
			start = Math.max(start, beginning());
			stop = Math.min(stop, ending());

			for (int frameNumber = start; frameNumber <= stop; frameNumber++) {
				mask[(frameNumber >> 5) - (beg >> 5)] &= ~(1 << (frameNumber % 32));
			}
			if (start == beg || stop == end) {
				helpCrop();
			}
		}
	}

	/**
	 * Clears the specified frame.
	 * 
	 * @param frameNumber
	 *            resets the specified frame
	 */
	public void clear(int frameNumber) {
		if (frameNumber < 0) {
			throw new IndexOutOfBoundsException(
					"Trying to access a negative frame: " + frameNumber);
		} else if (containsFrame(frameNumber)) {
			mask[(frameNumber >> 5) - (beg >> 5)] &= ~(1 << (frameNumber % 32));
			if (frameNumber == beg || frameNumber == end) {
				helpCrop();
			}
		}
	}

	/**
	 * Clears all the frames, but leaves the range untouched.
	 */
	private void clearAll() {
		for (int i = 0; i < mask.length; i++) {
			mask[i] = 0;
		}
	}

	/**
	 * Returns true if the checked value contains the rule value.
	 */
	private static class Contains implements Filterable.Rule {
		private FrameSpan v;

		Contains(FrameSpan f) {
			v = f;
		}

		/** @inheritDoc */
		public boolean passes(Measurable o) {
			FrameSpan other = (FrameSpan) o;
			return v.intersect(other).numFrames() == v.numFrames();
		}

		/** @inheritDoc */
		public String toString() {
			return "contains \"" + StringHelp.backslashify(v.toString()) + "\"";
		}

		/** @inheritDoc */
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			} else if (o instanceof Contains) {
				Contains other = (Contains) o;
				return this.v.equals(other.v);
			} else {
				return false;
			}
		}

		/** @inheritDoc */
		public int hashCode() {
			return v.hashCode();
		}

		/**
		 * Tells the filter whether to apply short circuiting.
		 * 
		 * @return <code>true</code>
		 */
		public boolean isShortCircuit() {
			return true;
		}

	}

	/**
	 * Returns true if the checked value contains none of the rule value.
	 */
	private static class Excludes implements Filterable.Rule {
		private FrameSpan v;

		Excludes(FrameSpan f) {
			v = f;
		}

		/** @inheritDoc */
		public boolean passes(Measurable o) {
			FrameSpan other = (FrameSpan) o;
			return !v.intersects(other);
		}

		/** @inheritDoc */
		public String toString() {
			return "excludes \"" + StringHelp.backslashify(v.toString()) + "\"";
		}

		/**
		 * Tells the filter whether to apply short circuiting.
		 * 
		 * @return <code>true</code>
		 */
		public boolean isShortCircuit() {
			return true;
		}

	}

	/**
	 * Returns true if the checked value intersects the rule value.
	 */
	private static class Intersects implements Filterable.Rule {
		private FrameSpan v;

		Intersects(FrameSpan f) {
			v = f;
		}

		/** @inheritDoc */
		public boolean passes(Measurable o) {
			FrameSpan other = (FrameSpan) o;
			return v.intersects(other);
		}

		/** @inheritDoc */
		public String toString() {
			return "intersects \"" + StringHelp.backslashify(v.toString())
					+ "\"";
		}

		/**
		 * Tells the filter whether to apply short circuiting.
		 * 
		 * @return <code>true</code>
		 */
		public boolean isShortCircuit() {
			return true;
		}

	}

	/** @inheritDoc */
	public Filterable.Rule convertRule(String unparsedRule,
			List unparsedValues, ErrorWriter err) throws BadDataException {
		if (unparsedValues.size() != 1) {
			throw new BadDataException(
					"The frame span rules take one frame span argument: "
							+ unparsedRule);
		}
		FrameSpan other = FrameSpan.parseFrameSpan((String) unparsedValues
				.get(0));
		if ("contains".equals(unparsedRule)) {
			return new Contains(other);
		} else if ("excludes".equals(unparsedRule)) {
			return new Excludes(other);
		} else if ("intersects".equals(unparsedRule)) {
			return new Intersects(other);
		} else if ("==".equals(unparsedRule)) {
			return Rules.getEquality(other);
		} else if ("!=".equals(unparsedRule)) {
			return Rules.getInequality(other);
		} else {
			throw new BadDataException("Not a valid rule for frame spans: "
					+ unparsedRule);
		}
	}

	/** @inheritDoc */
	public boolean isValidRule(String ruleName) {
		return "contains".equals(ruleName) || "excludes".equals(ruleName)
				|| "intersects".equals(ruleName) || "==".equals(ruleName)
				|| "!=".equals(ruleName);
	}

	/**
	 * @inheritDoc
	 * @return {@link Composable#UNORDERED}
	 */
	public int getCompositionType() {
		return Composable.UNORDERED;
	}

	/**
	 * @inheritDoc
	 * @return " framespan"
	 */
	public String getType() {
		return " framespan";
	}

	/** @inheritDoc */
	public Measurable.Difference getDifference(Measurable beta,
			Measurable blackout, Measurable ignore, CanonicalFileDescriptor cfd)
			throws IgnoredValueException {
		return getDifference(beta, blackout, ignore, cfd, null);
	}

	/** @inheritDoc */
	public Measurable.Difference getDifference(Measurable beta,
			Measurable blackout, Measurable ignore,
			CanonicalFileDescriptor cfd, Measurable.Difference old)
			throws IgnoredValueException {
		FrameSpanDiff fsd;
		if (old instanceof FrameSpanDiff) {
			fsd = (FrameSpanDiff) old;
			fsd.set(this, (FrameSpan) beta, (FrameSpan) blackout,
					(FrameSpan) ignore, cfd);
		} else {
			fsd = new FrameSpanDiff(this, (FrameSpan) beta,
					(FrameSpan) blackout, (FrameSpan) ignore, cfd);
		}
		return fsd;
	}

	/** @inheritDoc */
	public Composable compose(Composable partner) throws ClassCastException {
		return this.union((FrameSpan) partner);
	}

	/** @inheritDoc */
	public boolean passes(Filterable.Rule rule) {
		return rule.passes(this);
	}

	private static class FrameSpanDiff implements Measurable.Difference {
		private FrameSpan alpha, beta, blackout, ignore;

		private CanonicalFileDescriptor cfd;

		FrameSpanDiff(FrameSpan alpha, FrameSpan beta,
				FrameSpan blackout, FrameSpan ignore,
				CanonicalFileDescriptor cfd) throws IgnoredValueException {
			this.set(alpha, beta, blackout, ignore, cfd);
		}

		/** @inheritDoc */
		public Object getAlpha() {
			return alpha;
		}

		/** @inheritDoc */
		public Object getBeta() {
			return beta;
		}

		/** @inheritDoc */
		public Object getBlackout() {
			return blackout;
		}

		/** @inheritDoc */
		public Object getIgnore() {
			return ignore;
		}

		/** @inheritDoc */
		public CanonicalFileDescriptor getFileInformation() {
			return cfd;
		}

		/**
		 * Replaces the values of this difference object.
		 * @param alpha the target
		 * @param beta the candidate
		 * @param blackout bad frames
		 * @param ignore don't care frames
		 * @param cfd new information about the source media file
		 * @throws IgnoredValueException
		 */
		public void set(FrameSpan alpha, FrameSpan beta, FrameSpan blackout,
				FrameSpan ignore, CanonicalFileDescriptor cfd)
				throws IgnoredValueException {
			this.cfd = cfd;

			this.shared = -1;
			this.union = -1;
			this.extents = -1;
			this.match = null;

			this.alpha = alpha;
			this.beta = beta;
			this.blackout = blackout;
			this.ignore = ignore;
			if (ignore != null && beta != null) {
				if (beta.numFrames() == beta.intersect(ignore).numFrames()) {
					throw new IgnoredValueException(
							"Target and candidate frame ranges subsumed by ignored range");
				}
			}
			if (blackout != null) {
				if (beta != null && alpha != null) {
					this.match = this.beta.intersect(this.alpha);
					this.match = this.match.minus(blackout);
				}

				if (beta == null) {
					this.beta = blackout;
				} else {
					this.beta = this.beta.union(blackout);
				}
				if (this.alpha == null) {
					this.alpha = this.blackout;
				} else {
					this.alpha = this.alpha.union(blackout);
				}
			}

			if (ignore != null) {
				if (this.alpha != null)
					this.alpha = this.alpha.minus(ignore);
				if (this.beta != null)
					this.beta = this.beta.minus(ignore);
			}
		}

		/**
		 * Gets the count of frames in the intersection region (less the blackout
		 * and don't care).
		 * @return the count of shared frames
		 */
		public int getShared() {
			if (shared < 0) {
				shared = (getMatch() != null) ? getMatch().numFrames() : 0;
			}
			return shared;
		}

		/**
		 * Gets the count of shared frames.
		 * @return the number of shared frames
		 */
		public int getUnion() {
			if (union < 0) {
				union = alpha.union(beta).numFrames();
			}
			return union;
		}

		/**
		 * Gets the intersection frames.
		 * @return the intersection area
		 */
		public FrameSpan getMatch() {
			if (alpha == null || beta == null) {
				match = null;
			} else if (match == null) {
				match = alpha.intersect(beta);
			}
			return match;
		}

		/**
		 * Gets the truth frame count.
		 * @return the number of truth frames
		 */
		public int getTargetFrames() {
			return alpha.numFrames();
		}

		/**
		 * Gets the candidate frame count.
		 * @return the number of frames in the result data value
		 */
		public int getCandidateFrames() {
			return beta.numFrames();
		}

		/**
		 * Gets the count of the difference.
		 * @return the frame extents
		 */
		public int getExtents() {
			if (extents < 0) {
				FrameSpan m = getMatch();
				int beg = Math.min(alpha.beginning(), beta.beginning());
				int end = Math.max(alpha.ending(), beta.ending());
				extents = Math.abs(beg - m.beginning())
						+ Math.abs(end - m.ending());
			}
			return extents;
		}

		private FrameSpan match;

		private int shared;

		private int union;

		private int extents;
	}
}