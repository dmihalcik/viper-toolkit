import edu.umd.cfar.lamp.apploader.AppLoaderScript
import edu.umd.cfar.lamp.apploader.AppLoader

import edu.umd.cfar.lamp.viper.gui.players.DataPlayer

import java.util.Iterator

import viper.api.Config
import viper.api.Descriptor

class InsertIframesDescriptor implements AppLoaderScript {
	static final String DESCRIPTOR_NAME = "I-Frames";
	void run(AppLoader application) {
		d = insertIFrameDescriptor(application.getBean("#mediator"))
		s = application.getBean("#chronicle")->selectionModel
		if (s != null && d != null) {
			s.setNodeWhoseTimeToSelect(d->config)
		}
	}
	String getScriptName() {
		"Insert I-Frame Descriptor"
	}

	/**
	 * Creates a new type of descriptor, called 'I-Frames',
	 * if it doesn't already exist. Otherwise, locates it.
	 * @return the I-Frame descriptor config information
	 */
	Config insertIFrameDescriptorConfig(mediator) {
		V = mediator.getViperData()
		c = V.getConfig(Config.OBJECT, DESCRIPTOR_NAME)
		if (c == null) {
			c = V.createConfig(Config.OBJECT, DESCRIPTOR_NAME)
		}
		return c
	}
	
	/**
	 * Inserts a new I-Frame descriptor into the currently selected
	 * file, using the mediator's current DataPlayer object to  
	 * find where the iframes are.
	 */
	Descriptor insertIFrameDescriptor(mediator) {
		c = insertIFrameDescriptorConfig(mediator)
		sf = mediator.getCurrFile()
		if (sf != null) {
			allIFrames = sf.getDescriptorsBy(c)
			d = null
			if (allIFrames.hasNext())
				d = allIFrames.next()
			else 
				d = sf.createDescriptor(c)
			iframes = new viper.api.time.InstantRange()
			p = mediator.getDataPlayer()
			frameSpan = p.getRate().asFrame(p.getSpan())
			for ( f in frameSpan ) {
				if (DataPlayer.I_FRAME.equals(p.getImageType(f))) {
					iframes.add(f)
				}
			}
			d.setValidRange(iframes)
			return d
		}
		return null
	}
}