import edu.umd.cfar.lamp.apploader.AppLoaderScript
import edu.umd.cfar.lamp.apploader.AppLoader

import java.util.Iterator

import viper.api.Config
import viper.api.Descriptor

import viper.api.time.Span

import edu.umd.cfar.lamp.viper.examples.textline.TextlineModel

class CombineLinesAndWords implements AppLoaderScript {
	static final String myIdentifier = "http://viper-toolkit.sf.net/samples#combineWordsIntoLines"

	static final String TEXTLINE_TYPE = "http://lamp.cfar.umd.edu/viperdata#textline"
	
	static final String WORD_DESCRIPTOR_NAME = "Word";
	static final String WORD_LOCATION_ATTRIBUTE_NAME = "location";
	static final String WORD_CONTENT_ATTRIBUTE_NAME = "Content";

	static final String LINE_DESCRIPTOR_NAME = "Line";
	static final String LINE_LOCATION_ATTRIBUTE_NAME = "location";
	static final String LINE_CONTENT_ATTRIBUTE_NAME = "Content";

	static final String COMBINED_DESCRIPTOR_NAME = "Text Line";
	static final String COMBINED_ATTRIBUTE_NAME = "Value";
	
	
	void run(AppLoader application) {
		// For each descriptor, clean each dynamic attribute
		mediator = application.getBean("#mediator")
		V = mediator.viperData
		trans = V.begin(myIdentifier);
		success = false;
		try {
			wordConfig = V.getConfig(Config.OBJECT, WORD_DESCRIPTOR_NAME)
			if (wordConfig == null) {
				throw new RuntimeException("Cannot find descriptor named ${WORD_DESCRIPTOR_NAME}")
			} else if (!wordConfig.hasAttrConfig(WORD_LOCATION_ATTRIBUTE_NAME)) {
				throw new RuntimeException("Cannot find attribute named ${WORD_LOCATION_ATTRIBUTE_NAME}")
			} else if (!wordConfig.hasAttrConfig(WORD_CONTENT_ATTRIBUTE_NAME)) {
				throw new RuntimeException("Cannot find attribute named ${WORD_CONTENT_ATTRIBUTE_NAME}")
			}
			lineConfig = V.getConfig(Config.OBJECT, LINE_DESCRIPTOR_NAME)
			if (lineConfig == null) {
				throw new RuntimeException("Cannot find descriptor named ${LINE_DESCRIPTOR_NAME}")
			} else if (!lineConfig.hasAttrConfig(LINE_LOCATION_ATTRIBUTE_NAME)) {
				throw new RuntimeException("Cannot find attribute named ${LINE_LOCATION_ATTRIBUTE_NAME}")
			} else if (!lineConfig.hasAttrConfig(LINE_CONTENT_ATTRIBUTE_NAME)) {
				throw new RuntimeException("Cannot find attribute named ${LINE_CONTENT_ATTRIBUTE_NAME}")
			}
			combinedConfig = insertCombinedDescriptorConfig(V)
			sf = mediator->currFile
			if (sf != null) {
				oldLines = new java.util.ArrayList()
				for( d in sf.getDescriptorsBy(lineConfig) ) {
					oldLines.add(d)
				}
				for( d in oldLines ) {
					combined = sf.createDescriptor(combinedConfig)
					combined.setValidRange(d.validRange.clone())
					for (a in d.children) {
						if (a.attrName == LINE_LOCATION_ATTRIBUTE_NAME) {
							newAttr = combined.getAttribute(COMBINED_ATTRIBUTE_NAME)
							assert newAttr != null : "Cannot find attribute named ${COMBINED_ATTRIBUTE_NAME}"
							copyInto = {box, line | line.set(box.x, box.y, box.width, box.height, box.rotation)}
							create = {box | return new TextlineModel(box.x, box.y, box.width, box.height, box.rotation)}
							transformInto(newAttr, a, copyInto, create)
						} else if (a.attrName == LINE_CONTENT_ATTRIBUTE_NAME) {
							newAttr = combined.getAttribute(COMBINED_ATTRIBUTE_NAME)
							assert newAttr != null : "Cannot find attribute named ${COMBINED_ATTRIBUTE_NAME}"
							copyInto = {content, line | line.setText(content)}
							create = {content | return new TextlineModel(0,0,0,0,0, content)}
							transformInto(newAttr, a, copyInto, create)
						} else {
							newAttr = combined.getAttribute(a.attrConfig.attrName)
							assert newAttr != null : "Cannot find attribute named ${a.attrConfig.attrName}"
							if (a.attrConfig.dynamic) {
								for(oldVal in a.attrValuesOverWholeRange) {
									assert oldVal != null
									newAttr.setAttrValueAtSpan(oldVal.value, oldVal)
								}
							} else {
								newAttr.attrValue = a.attrValue
							}
						}
					}
					// now we have a text line that is unbroken.
					// we can use the 'word' data to split the 
					// lines into word segments. Yeah, I know - wouldn't
					// it be nice if viper supported relations? And maybe
					// queries? 
				}
				for (w in sf.getDescriptorsBy(wordConfig)) {
					// the basic idea is to put lines at the ends of each word
					// then go through and remove the first and last line,
					// and average the lines inside
					wordAttr = w.getAttribute(WORD_LOCATION_ATTRIBUTE_NAME)
					for (l in sf.getDescriptorsBy(combinedConfig, w.validRange.extrema)) {
						lineAttr = l.getAttribute(COMBINED_ATTRIBUTE_NAME)
						create = {word | return null}
						transformInto(lineAttr, wordAttr, projectBoxIntoAnother, create)
					}
				}
				for (tl in sf.getDescriptorsBy(combinedConfig)) {
					lineAttr = tl.getAttribute(COMBINED_ATTRIBUTE_NAME)
					transformDynamicAttr(lineAttr, cleanLine) 
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
	
	cleanLine = {textline | 
		if (textline.obox.area().doubleValue() <= 0) {
			return textline
		}
		textline = textline.clone()
		oo = textline.wordOffsets
		sz = oo.size()
			oo = oo.clone()
		if (sz > 2) {
			textline.wordOffsets.clear()
			java.util.Collections.sort(oo)
			i = 1
			while( i < sz-1 ) {
				textline.addWordOffset((int) ((oo.get(i) + oo.get(i+1)) / 2))
				i += 2
			}
			//str = "Collapsed offsets ${oo} into ${textline.wordOffsets} for ${textline.text}"
			//java.lang.System.out.println(str)
		} else {
			textline.wordOffsets.clear()
		}
		return textline
	}
	projectBoxIntoAnother = {obox, textline |
		wordArea = obox.area().doubleValue()
		wordAndLineArea = obox.getIntersection(textline.obox).area().doubleValue()
		if (wordAndLineArea < wordArea * .75) {
			return
		}
		s = textline.width * textline.width
		e = 0
		R = java.awt.geom.AffineTransform.getRotateInstance(java.lang.Math.toRadians(textline.rotation))
		P = new java.awt.geom.Point2D.Double(0,1)
		R.transform(P,P)
		sline = new java.awt.geom.Line2D.Double(textline.x, textline.y, textline.x + P.x, textline.y + P.y)
		for (v in obox.verteces) {
			t = sline.ptLineDistSq(v.x.doubleValue(), v.y.doubleValue())
			if (t < s) {
				s = t
			}
			if (t > e) {
				e = t
			}
		}
		if (s < e) {
			s = java.lang.Math.sqrt(s)
			e = java.lang.Math.sqrt(e)
			textline.addWordOffset((int) s)
			textline.addWordOffset((int) e)
			//str = "Projecting ${obox} into ${textline} gave offsets ${s} and ${e}"
			//java.lang.System.out.println(str)
		}
	}

	transformDynamicAttr(attr, Closure trans) {
		if (attr.range == null) {
			return
		}
		copy = attr.range.clone()
		for( val in copy.iterator() ) {
			newV = trans.call(val.value)
			attr.setAttrValueAtSpan(newV, val)
		}
	}


	transformInto(newAttr, oldAttr, Closure copyInto, Closure create) {
		// Utility method that transforms the values of newAttr to reflect
		// information in oldAttr, using the given closures.
		// If a value exists at the newAttr, then copyInto is invoked.
		// When no value exists, create is invoked.
		if (oldAttr.range == null) {
			return
		}
		modify = newAttr.range != null
		copy = modify ? newAttr.range.clone() : null
		for (val in oldAttr.attrValuesOverWholeRange) {
			// To fill in null values, first set the whole range
			partial = create.call(val.value)
			assert val != null
			if (partial != null) {
				newAttr.setAttrValueAtSpan(partial, val)
			}
			if (!modify) {
				continue
			}
			
			// After the creation, then modify to reflect
			// partial changes already there
			already = copy.iterator(val)
			if (already.hasNext()) {
				for (partialSpan in already) {
					partial = partialSpan.value.clone()
					copyInto.call(val.value, partial)
					newAttr.setAttrValueAtSpan(partial, partialSpan)
				}
			}
		}
	}

	/**
	 * Creates a new type of descriptor, called 'TextLines',
	 * if it doesn't already exist. Otherwise, locates it.
	 * @return the text lines descriptor config information
	 */
	Config insertCombinedDescriptorConfig(viperdata) {
		c = viperdata.getConfig(Config.OBJECT, COMBINED_DESCRIPTOR_NAME)
		if (c == null) {
			c = viperdata.createConfig(Config.OBJECT, COMBINED_DESCRIPTOR_NAME)
			c.createAttrConfig(COMBINED_ATTRIBUTE_NAME, TEXTLINE_TYPE, true, null, new edu.umd.cfar.lamp.viper.examples.textline.AttributeWrapperTextline())
			cOld = viperdata.getConfig(Config.OBJECT, LINE_DESCRIPTOR_NAME)
			for (a in cOld.children) {
				if (a.attrName != LINE_LOCATION_ATTRIBUTE_NAME
					&& a.attrName != LINE_CONTENT_ATTRIBUTE_NAME) {
					c.createAttrConfig(a.attrName, a.attrType, a.dynamic, a.defaultVal, a.params)
				}
			}
		}
		return c
	}

	String getScriptName() {
		"Combine 'Word' and 'Line' objects into 'Textlines'"
	}
}