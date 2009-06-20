import edu.umd.cfar.lamp.apploader.AppLoaderScript
import edu.umd.cfar.lamp.apploader.AppLoader

import edu.umd.cfar.lamp.viper.gui.players.DataPlayer

import java.util.Iterator

import viper.api.Config
import viper.api.Descriptor

class InvalidateElsewhen implements AppLoaderScript {
	myIdentifier = "http://viper-toolkit.sf.net/samples#invalidateElsewhen"
	void run(AppLoader application) {
		mediator = application.getBean("#mediator")
		chronicle = application.getBean("#chronicle")->wrappedChronicle
		time = chronicle->selectionModel->selectedTime
		sf = mediator->currFile
		if (sf != null && time != null) {
			success = false;
			trans = sf.begin(myIdentifier);
			try {
				for( d in sf.children ) {
					assert d instanceof viper.api.Descriptor
					r = new viper.api.time.InstantRange()
					r.addAll(d.validRange.intersect(time))
					d.validRange = r
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
	String getScriptName() {
		"Crop Descriptors to Match Current Time Selection"
	}
}