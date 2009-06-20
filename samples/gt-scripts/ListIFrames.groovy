import edu.umd.cfar.lamp.apploader.AppLoaderScript
import edu.umd.cfar.lamp.apploader.AppLoader

import edu.umd.cfar.lamp.viper.gui.players.DataPlayer

import java.lang.StringBuffer
import java.util.Iterator

import viper.api.Config
import viper.api.Descriptor

import javax.swing.JTextArea
import javax.swing.JScrollPane
import javax.swing.JFrame


class ListIFrames implements AppLoaderScript {
	void run(AppLoader application) {
		mediator = application.getBean("#mediator")
		p = mediator.getDataPlayer()
		frameSpan = p.getRate().asFrame(p.getSpan())
		sb = new StringBuffer()
		for ( f in frameSpan ) {
			if (DataPlayer.I_FRAME.equals(p.getImageType(f))) {
				sb.append(f).append("\n")
			}
		}
		textWidget = new JScrollPane(new JTextArea(sb.toString()))
		
		frame = new JFrame("TextDemo");
		frame.defaultCloseOperation = javax.swing.WindowConstants.DISPOSE_ON_CLOSE
		frame.contentPane.add(textWidget)
		frame.pack()
		frame.visible = true
	}
	String getScriptName() {
		"Display a List of I-Frame Numbers"
	}
}