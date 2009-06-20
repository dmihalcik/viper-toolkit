import edu.umd.cfar.lamp.apploader.AppLoaderScript
import edu.umd.cfar.lamp.apploader.AppLoader

import edu.umd.cfar.lamp.viper.gui.players.DataPlayer

import java.util.Iterator

import viper.api.Config
import viper.api.Descriptor

import viper.api.time.Span

class ScrubInvalidAttributes implements AppLoaderScript {
	myIdentifier = "http://viper-toolkit.sf.net/samples#scrubInvalidAttributes"
	void run(AppLoader application) {
		// For each descriptor, clean each dynamic attribute
		mediator = application.getBean("#mediator")
		sf = mediator->currFile
		if (sf != null) {
			success = false;
			trans = sf.begin(myIdentifier);
			try {
				for( d in sf.children ) {
					assert d instanceof viper.api.Descriptor
					for (a in d.children) {
						if (a.attrConfig.dynamic && a.range != null && !a.range.empty) {
							cleanAttribute(a, d.validRange)
						}
					}
				}
				success = true;
			} finally {
				if (trans != null) {
					if (success) {
						trans.commit();
					} else {
						trans.rollback();
					}
				}
			}
		}
	}

	void cleanAttribute(viper.api.Attribute a, viper.api.time.InstantRange r) {
		ex = a.range.extrema
		i = ex.start
		for (span in r.iterator(ex)) {
			if (i < span.start) {
				a.setAttrValueAtSpan(null, new Span(i, span.start))
			}
			i = span.end
		}
		if (i < ex.end) {
			a.setAttrValueAtSpan(null, new Span(i, ex.end))
		}
	}

	String getScriptName() {
		"Scrub Invalid Attribute Data"
	}
}