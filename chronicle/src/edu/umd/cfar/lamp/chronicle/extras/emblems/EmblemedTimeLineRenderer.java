package edu.umd.cfar.lamp.chronicle.extras.emblems;

import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cs.piccolo.*;

/**
 * Wraps an existing timeline renderer to attach a set of emblems,
 * given by the emblem model, to the end of the label the wrapped
 * renderer returns.
 */
public class EmblemedTimeLineRenderer implements TimeLineRenderer {
	private int inset = 2;
	private int emblemSize = 12;
	private EmblemModel emblemModel = new EmptyEmblemModel();
	private TimeLineRenderer wrappedRenderer = new BasicTimeLineRenderer();

	public PNode generateLabel(ChronicleViewer v, TimeLine tqe,
			boolean isSelected, boolean hasFocus, double infoLength,
			int orientation) {
		EmblemedTimeLineLabel l = new EmblemedTimeLineLabel(wrappedRenderer, tqe, emblemModel, v, isSelected, hasFocus, infoLength, orientation);
		l.setInset(getInset());
		l.setEmblemSize(getEmblemSize());
		return l;
	}

	public EmblemModel getEmblemModel() {
		return emblemModel;
	}
	public void setEmblemModel(EmblemModel emblemModel) {
		this.emblemModel = emblemModel;
	}
	public int getEmblemSize() {
		return emblemSize;
	}
	public void setEmblemSize(int emblemSize) {
		this.emblemSize = emblemSize;
	}
	public int getInset() {
		return inset;
	}
	public void setInset(int inset) {
		this.inset = inset;
	}
	public TimeLineRenderer getWrappedRenderer() {
		return wrappedRenderer;
	}
	public void setWrappedRenderer(TimeLineRenderer wrappedRenderer) {
		this.wrappedRenderer = wrappedRenderer;
	}
	
	public double getPreferedTimeLineInfoLength(ChronicleViewer chronicle,
			TimeLine t, boolean isSelected, boolean hasFocus, double timeLength,
			int orientation) {
		return wrappedRenderer.getPreferedTimeLineInfoLength(chronicle, t,
				isSelected, hasFocus, timeLength, orientation);
	}
	public PNode getTimeLineRendererNode(ChronicleViewer chronicle, TimeLine t,
			boolean isSelected, boolean hasFocus, double timeLength,
			double infoLength, int orientation) {
		return wrappedRenderer.getTimeLineRendererNode(chronicle, t,
				isSelected, hasFocus, timeLength, infoLength, orientation);
	}
}
