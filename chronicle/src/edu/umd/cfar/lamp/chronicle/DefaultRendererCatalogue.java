package edu.umd.cfar.lamp.chronicle;

import java.io.*;

public class DefaultRendererCatalogue implements RendererCatalogue, Serializable{
	private TimeLineRenderer  defaultRenderer = new BasicTimeLineRenderer();
	
	public TimeLineRenderer getTimeLineRenderer(TimeLine tqe) {
		return defaultRenderer;
	}

	public void setDefaultTimeLineRenderer(TimeLineRenderer renderer) {
		this.defaultRenderer = renderer;
	}
}
