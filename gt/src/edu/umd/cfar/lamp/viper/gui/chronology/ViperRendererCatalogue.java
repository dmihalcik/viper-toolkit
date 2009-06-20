package edu.umd.cfar.lamp.viper.gui.chronology;

import edu.umd.cfar.lamp.apploader.prefs.*;
import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.chronicle.extras.emblems.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

public class ViperRendererCatalogue implements RendererCatalogue {
	private TimeLineRenderer defaultTimeLineRenderer;
	private PrefsManager prefs;
	private ViperViewMediator mediator;
	private EmblemModel emblemModel = new EmptyEmblemModel();
	
	public ViperRendererCatalogue() {
		defaultTimeLineRenderer = wrapRenderer(new BasicTimeLineRenderer());
	}
	
	private TimeLineRenderer wrapRenderer(TimeLineRenderer r) {
		ViperEnhancedTimeLineRenderer etlr = new ViperEnhancedTimeLineRenderer();
		etlr.setEmblemModel(emblemModel);
		etlr.setWrappedRenderer(r);
		return etlr;
	}
	
	public TimeLineRenderer getTimeLineRenderer(TimeLine tqe) {
		if (tqe instanceof VConfigTimeLine) {
			return wrapRenderer(new VConfigTimeLineRenderer());
		}
		if (tqe instanceof VDescriptorTimeLine) {
			return wrapRenderer(new VDescriptorTimeLineRenderer());
		}
		return this.defaultTimeLineRenderer;
	}

	public void setDefaultTimeLineRenderer(TimeLineRenderer renderer) {
		((EmblemedTimeLineRenderer) defaultTimeLineRenderer).setWrappedRenderer(renderer);
	}

	/**
	 * @return Returns the mediator.
	 */
	public ViperViewMediator getMediator() {
		return mediator;
	}
	/**
	 * @param mediator The mediator to set.
	 */
	public void setMediator(ViperViewMediator mediator) {
		this.mediator = mediator;
	}
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
	/**
	 * @return Returns the defaultTimeLineRenderer.
	 */
	public TimeLineRenderer getDefaultTimeLineRenderer() {
		return defaultTimeLineRenderer;
	}

	/**
	 * @param model
	 */
	public void setEmblemModel(EmblemModel model) {
		EmblemedTimeLineRenderer etlr = (EmblemedTimeLineRenderer) defaultTimeLineRenderer;
		etlr.setEmblemModel(model);
		this.emblemModel = model;
	}

	/**
	 * @return
	 */
	public EmblemModel getEmblemModel() {
		return this.emblemModel;
	}
}
