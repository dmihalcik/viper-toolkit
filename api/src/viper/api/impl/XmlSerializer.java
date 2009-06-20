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

package viper.api.impl;

import java.io.*;
import java.util.*;

import org.apache.xerces.dom.*;
import org.apache.xml.serialize.*;
import org.w3c.dom.*;

import viper.api.*;
import viper.api.extensions.*;
import viper.api.time.*;
import edu.umd.cfar.lamp.viper.util.*;

/**
 * This class contains static methods to print out a 
 * ViperData object to a given document. It returns a 
 * 
 */
public class XmlSerializer {
	/**
	 * Gets a valid viper xml file that contains only the specified 
	 * sourcefile, but all the config.
	 * @param v the viperdata node (contains the config)
	 * @param sf the sourcefile node to include
	 * @param domI the xml DOM implementation factory
	 * @return the viper xml root node
	 */
	public static Element toXmlSingleSourcefile(ViperData v, Sourcefile sf, DOMImplementation domI) {
		Element el = toXmlConfigOnly(v, domI);
		Document root = el.getOwnerDocument();
		Element dataEl = root.createElementNS(ViperData.ViPER_SCHEMA_URI, ViperData.ViPER_SCHEMA_QUALIFIER+"data");
		dataEl.appendChild(helpWithSourcefile(sf, root));
		el.appendChild(dataEl);
		return el;
	}

	/**
	 * Exports the config element.
	 * @param v the data to extract the config from
	 * @param domI the xml implementation
	 * @return the config element
	 */
	public static Element toXmlConfigOnly(ViperData v, DOMImplementation domI) {
		Document root = createDoc(domI);
		Element el = initRoot(root);
		Element cfgEl = root.createElementNS(ViperData.ViPER_SCHEMA_URI, ViperData.ViPER_SCHEMA_QUALIFIER+"config");
		for (Iterator cfgIter = v.getAllConfigs().iterator();
		cfgIter.hasNext();
		) {
			cfgEl.appendChild(helpWithConfig((Config) cfgIter.next(), root));
		}
		el.appendChild(cfgEl);
		return el;
	}
	
	private static Document createDoc(DOMImplementation domI){
		DocumentType dtd =
			domI.createDocumentType(
					"viper",
					"viper",
					ViperData.ViPER_SCHEMA_URI);
		return domI.createDocument(ViperData.ViPER_SCHEMA_URI, ViperData.ViPER_SCHEMA_QUALIFIER+"viper", dtd);
	}
	private static Element initRoot(Document root){
		Element el = root.getDocumentElement();
		assert el.getNodeName().equals(ViperData.ViPER_SCHEMA_QUALIFIER+"viper");
		String schemaXmlns = qualifier2xmlnsAttr(ViperData.ViPER_SCHEMA_QUALIFIER);
		String dataXmlns = qualifier2xmlnsAttr(ViperData.ViPER_DATA_QUALIFIER);
		el.setAttribute(schemaXmlns, ViperData.ViPER_SCHEMA_URI);
		el.setAttribute(dataXmlns, ViperData.ViPER_DATA_URI);
		return el;
	}
	private static String qualifier2xmlnsAttr(String qual) {
		if ("".equals(qual)) {
			return "xmlns";
		} else {
			return "xmlns:" + qual.substring(0, qual.length()-1);
		}
	}
	
	/**
	 * Serializes the viper data as xml.
	 * @param v the data 
	 * @param domI the xml implementation
	 * @return the saved xml
	 */
	public static Element toXml(ViperData v, DOMImplementation domI) {
		Element el = toXmlConfigOnly(v, domI);
		Document root = el.getOwnerDocument();

		Element dataEl = root.createElementNS(ViperData.ViPER_SCHEMA_URI, ViperData.ViPER_SCHEMA_QUALIFIER+"data");
		for (Iterator foIter = v.getAllSourcefiles().iterator();
			foIter.hasNext();
			) {
			dataEl.appendChild(
				helpWithSourcefile((Sourcefile) foIter.next(), root));
		}
		el.appendChild(dataEl);
		return el;
	}
	/**
	 * Writes the data as xml.
	 * @param v the data
	 * @param pw the target writer
	 * @throws IOException if there is an error while writing
	 */
	public static void toWriter(ViperData v, PrintWriter pw)
		throws IOException {
		DOMImplementation domI = DOMImplementationImpl.getDOMImplementation();
		Element root = toXml(v, domI);
		OutputFormat format = new OutputFormat();
		format.setIndenting(true);
		format.setOmitXMLDeclaration(false);
		new XMLSerializer(pw, format).serialize(root);
	}
	private static Element helpWithConfig(Config cfg, Document root) {
		Element el = root.createElementNS(ViperData.ViPER_SCHEMA_URI, ViperData.ViPER_SCHEMA_QUALIFIER+"descriptor");
		el.setAttribute("name", cfg.getDescName());
		el.setAttribute("type", Util.getDescType(cfg.getDescType()));
		for (Iterator i = cfg.getAttributeConfigs(); i.hasNext();) {
			el.appendChild(helpWithAttrConfig((AttrConfig) i.next(), root));
		}
		return el;
	}
	private static Element helpWithSourcefile(Sourcefile sf, Document root) {
		Element el = root.createElementNS(ViperData.ViPER_SCHEMA_URI, ViperData.ViPER_SCHEMA_QUALIFIER+"sourcefile");
		MediaElement rm = sf.getReferenceMedia();
		if (rm.getSourcefileName() != null
			&& rm.getSourcefileName().length() > 0) {
			el.setAttribute("filename", rm.getSourcefileName());
		}
		for (Iterator i = sf.getDescriptors(); i.hasNext();) {
			el.appendChild(helpWithDescriptor((Descriptor) i.next(), root));
		}
		return el;
	}
	private static Element helpWithDescriptor(Descriptor d, Document root) {
		Element el =
			root.createElementNS(ViperData.ViPER_SCHEMA_URI, ViperData.ViPER_SCHEMA_QUALIFIER+Util.getDescType(d.getDescType()).toLowerCase());
		el.setAttribute("name", d.getDescName());
		el.setAttribute("id", String.valueOf(d.getDescId()));
		InstantRange validity = d.getValidRange();
		if (validity != null) {
			if (validity.isFrameBased()) {
				el.setAttribute("framespan", rangeToString(validity));
			} else if (validity.isTimeBased()) {
				el.setAttribute("timespan", rangeToString(validity));
			}
		}
		for (Iterator i = d.getAttributes(); i.hasNext();) {
			Attribute a = (Attribute) i.next();
			el.appendChild(helpWithAttribute(a, root));
		}
		return el;
	}
	private static String rangeToString(InstantRange range) {
		StringBuffer buf = new StringBuffer();
		Iterator iter = range.iterator();
		while (iter.hasNext()) {
			Interval curr = (Interval) iter.next();
			Instant start = (Instant) curr.getStart();
			Instant end = (Instant) curr.getEnd();
			buf.append (start).append(':').append(end.previous());
			if (iter.hasNext()) {
				buf.append(' ');
			}
		}
		return buf.toString();
	}
	
	private static Element helpWithAttrConfig(AttrConfig a, Document root) {
		Element el = root.createElementNS(ViperData.ViPER_SCHEMA_URI, ViperData.ViPER_SCHEMA_QUALIFIER+"attribute");
		el.setAttribute("name", a.getAttrName());
		el.setAttribute("dynamic", String.valueOf(a.isDynamic()));
		el.setAttribute("type", a.getAttrType());
		AttrValueWrapper wr = a.getParams();
		if (wr instanceof ExtendedAttrValueParser) {
			Element exEl = ((ExtendedAttrValueParser) wr).getXMLFormatConfig(root, a);
			if (exEl != null) {
				el.appendChild(exEl);
			}
		}
		if ((a.getDefaultVal() != null) && (wr instanceof AttrValueParser)) {
			AttrValueParser pav = (AttrValueParser) wr;
			Element defEl = root.createElementNS(ViperData.ViPER_SCHEMA_URI, ViperData.ViPER_SCHEMA_QUALIFIER+"default");
			defEl.appendChild(pav.getXMLFormat(root, pav.setAttributeValue(a.getDefaultVal(), a), null));
			el.appendChild(defEl);
		}
		return el;
	}
	private static Element nullXML(Document root) {
		return root.createElementNS(ViperData.ViPER_DATA_URI, "data:null");
	}
	private static Element helpWithAttribute(Attribute a, Document root) {
		Element el = root.createElementNS(ViperData.ViPER_SCHEMA_URI, ViperData.ViPER_SCHEMA_QUALIFIER+"attribute");
		Object def = a.getAttrConfig().getDefaultVal();
		el.setAttribute("name", a.getAttrName());
		if (!a.getAttrConfig().isDynamic()) {
			if (a.getAttrValue() == null) {
				if (def != null) {
					el.appendChild(nullXML(root));
				} // else default is null, so don't have to specify
			} else if (def == null || !def.equals(a.getAttrValue())) {
				if (a.getAttrConfig().getParams() instanceof AttrValueParser) {
					AttrValueParser avp =
						(AttrValueParser) a.getAttrConfig().getParams();
					Object decoded = a.getAttrValue();
					Object encoded = avp.setAttributeValue(decoded, a);
					el.appendChild(
						(
							(AttrValueParser) a
								.getAttrConfig()
								.getParams())
								.getXMLFormat(
							root,
							encoded, null));
				} else {
					// XXX attatch comment node saying 'unable to serialize'
				}
			}
		} else if (a.getAttrConfig().getParams() instanceof AttrValueParser) {
			AttrValueParser avp =
				(AttrValueParser) a.getAttrConfig().getParams();
			for (Iterator iter = a.iterator(); iter.hasNext();) {
				DynamicAttributeValue curr =
					(DynamicAttributeValue) iter.next();
				Object decoded = curr.getValue();
				Object encoded = avp.setAttributeValue(decoded, null);
				Element child = avp.getXMLFormat(root, encoded, null);
				if (curr.isFrameBased()) {
					child.setAttribute("framespan", Util.valueOf(curr));
				} else if (curr.isTimeBased()) {
					child.setAttribute("timespan", Util.valueOf(curr));
				} else {
					// assert false, "Neither Frame nor Time Instant"
				}
				el.appendChild(child);
			}
		} else {
			// XXX comment about unable to serialize values
		}
		return el;
	}
}
 	