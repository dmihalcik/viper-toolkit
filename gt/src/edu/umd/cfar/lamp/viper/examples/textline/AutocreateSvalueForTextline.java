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


package edu.umd.cfar.lamp.viper.examples.textline;

import java.util.*;

import javax.swing.*;

import viper.api.*;
import viper.api.datatypes.*;
import viper.api.extensions.*;
import edu.umd.cfar.lamp.viper.gui.core.*;

public class AutocreateSvalueForTextline implements ViperMediatorChangeListener {
	private boolean updating = false;
	
	private static final String MyURI = "http://viper-toolkit.sourceforge.net/viper-gt#autocreateSvalue";
	
	
	public void schemaChanged(ViperMediatorChangeEvent e) {
		final ViperViewMediator mediator = (ViperViewMediator) e.getSource();
		final ViperChangeEvent ve = e.getViperEvent();
		if (ve != null && !updating && ve instanceof ViperUndoableEvent) {
			// Okay, this is a bit of a hack
			// since it is difficult to tell what 
			// textlines have been created, we just find all that 
			// have a dangling 
			final ViperData v = mediator.getViperData();
			updating = true;
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					boolean finished = false;
					TransactionalNode.Transaction t = ((TransactionalNode) v).begin(MyURI);
					try {
						Iterator iter = v.getConfigs();
						while (iter.hasNext()) {
							healDescriptor((Config) iter.next());
						}
						finished = true;
					} finally {
						updating = false;
						if (t != null) {
							if (finished) {
								t.commit();
							} else {
								t.rollback();
							}
						}
					}
				}
			});
		}
	}
	
	private void healDescriptor(Config c) {
		Iterator iter = c.getAttributeConfigs();
		Set svalues = new HashSet();
		Set textlines = new LinkedHashSet();
		ArrayList l = new ArrayList();
		while (iter.hasNext()) {
			AttrConfig ac = (AttrConfig) iter.next();
			if ((ViperData.ViPER_DATA_URI + "textline").equals(ac.getAttrType())) {
				textlines.add(ac);
				l.add(ac);
			} else if (ViperDataFactoryImpl.SVALUE.equals(ac.getAttrType()) && !ac.isDynamic()) {
				svalues.add(ac);
			}
		}
		for (int i = 0; i < l.size(); i++) {
			AttrConfig ac = (AttrConfig) l.get(i);
			AttributeWrapperTextline tlw = (AttributeWrapperTextline) ac.getParams();
			if (tlw.getTextLink() != null) {
				if (svalues.remove(tlw.getTextLink())) {
					textlines.remove(ac);
				} else {
					tlw.setTextLinkName(null);
				}
			}
		}
		iter = textlines.iterator();
		Iterator svalueIter = svalues.iterator();
		while (iter.hasNext()) {
			AttrConfig tac = (AttrConfig) iter.next();
			AttrConfig sac = null;
			if (svalueIter.hasNext()) {
				sac = (AttrConfig) svalueIter.next();
			} else {
				sac = ((Config) tac.getParent()).createAttrConfig("Textline Content for " + tac.getAttrName(), ViperDataFactoryImpl.SVALUE, false, null, new Svalue());
			}
			AttributeWrapperTextline tlw = new AttributeWrapperTextline();
			tlw.setTextLink(sac);
			tac.getEditor().setAttrType(tac.getAttrType(), tlw);
		}
	}

	public void currFileChanged(ViperMediatorChangeEvent e) {
		// noop - this only responds to changes in the schema
	}

	public void mediaChanged(ViperMediatorChangeEvent e) {
		// noop - this only responds to changes in the schema
	}

	public void frameChanged(ViperMediatorChangeEvent e) {
		// noop - this only responds to changes in the schema
	}

	public void dataChanged(ViperMediatorChangeEvent e) {
		// noop - this only responds to changes in the schema
	}
}
