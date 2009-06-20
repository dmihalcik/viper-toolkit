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

package viper.api.datatypes;

import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;

import viper.api.*;
import viper.api.Node;
import viper.api.extensions.*;
import viper.api.time.*;

/**
 * Attribute data type information for an enumerated type.
 * Unlike most of the other default types, this one varies,
 * and so has a constructor and does not act as a singleton.
 */
public class Lvalue implements ExtendedAttrValueParser {
	private static final String AT_V = "value";

	private String[] possibles;
	private HashMap reverseMap;
	
	/**
	 * Gets a list of the possible values this one can take.
	 * @return all allowed values
	 */
	public String[] getPossibles() {
		return possibles;
	}

	/**
	 * Gets the old skool listing of the type.
	 * @return <code>lvalue [possible*]</code>
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("lvalue [ ");
		for (int i = 0; i < possibles.length; i++) {
			sb.append(possibles[i]).append(" ");
		}
		sb.append("]");
		return sb.toString();
	}
	
	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (o instanceof Lvalue) {
			Lvalue that = (Lvalue) o;
			if (possibles.length == that.possibles.length) {
				for (int i = 0; i < possibles.length; i++) {
					if (!possibles[i].equals(that.possibles[i])) {
						return false;
					}
				}
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return reverseMap.hashCode();
	}
	
	/**
	 * Creates a new lvalue with an empty enumeration list.
	 */
	public Lvalue() {
		possibles = new String[0];
		reverseMap = new HashMap();
	}

	/**
	 * Creates a new enumeration from the given list of acceptable values.
	 * @param possibles the acceptable values
	 */
	public Lvalue(String[] possibles) {
		this.possibles = new String[possibles.length];
		System.arraycopy(possibles, 0, this.possibles, 0, possibles.length);
		setReverseMap();
	}
    private void setReverseMap() {
		reverseMap = new HashMap();
		for (int i = 0; i < this.possibles.length; i++) {
			reverseMap.put(this.possibles[i], new Integer(i));
		}
    }

	/**
	 * Creates a new emueration type from the given collection of 
	 * valid Strings.
	 * @param possibles a collection of Strings to use as
	 * enumeration elements
	 */
	public Lvalue(Collection possibles) {
		this.possibles =
			(String[]) possibles.toArray(new String[possibles.size()]);
		setReverseMap();
	}

	/**
	 * @see viper.api.AttrValueWrapper#getObjectValue(java.lang.Object, Node, Instant)
	 */
	public Object getObjectValue(Object o, Node container, Instant instant) {
		if (o == null) {
			return null;
		} else {
			try {
				return possibles[((Integer) o).intValue()];
			} catch (ClassCastException ccx) {
				throw new BadAttributeDataException("Not an lvalue index: " + o);
			} catch (IndexOutOfBoundsException ioobx) {
				throw new BadAttributeDataException("Not a valid lvalue index: " + o);
			}
		}
	}
	/**
	 * @see viper.api.AttrValueWrapper#setAttributeValue(java.lang.Object, Node)
	 */
	public Object setAttributeValue(Object o, Node container) {
		if (o == null) {
			return null;
		} else if (o instanceof String) {
			if (reverseMap.containsKey(o)) {
				return reverseMap.get(o);
			} else {
				throw new BadAttributeDataException("Not a possible value: " + o + " of " + this);
			}
		} else if (o instanceof Integer) {
			int i = ((Integer) o).intValue();
			if (0 <= i && i < possibles.length) {
				return o;
			} else {
				throw new BadAttributeDataException("lvalue index out of range: " + o);
			}
		} else {
			throw new BadAttributeDataException("Not a possible value: " + o);
		}
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#getXMLFormat(org.w3c.dom.Document, java.lang.Object, Node)
	 */
	public Element getXMLFormat(Document root, Object o, Node container) {
		String qualifier = ViperData.ViPER_DATA_QUALIFIER;
		String uri = ViperData.ViPER_DATA_URI;
		Element el = root.createElementNS(uri, qualifier + "lvalue");
		el.setAttribute(AT_V, (String) getObjectValue(o, null, null));
		return el;
	}

	/**
	 * @see viper.api.extensions.AttrValueParser#setValue(org.w3c.dom.Element, Node)
	 */
	public Object setValue(Element el, Node container) {
		if (el.hasAttribute(AT_V)) {
			String val = el.getAttribute(AT_V);
			return setAttributeValue(val, null);
		} else {
			throw new BadAttributeDataException("Missing an attribute for value of lvalue");
		}
	}

	/**
	 * @see viper.api.extensions.ExtendedAttrValueParser#setConfig(org.w3c.dom.Element, Node)
	 */
	public AttrValueWrapper setConfig(Element el, Node container) {
		String uri = ViperData.ViPER_DATA_URI;
		Logger log = Logger.getLogger("viper.api.datatypes");

		NodeList elements = el.getElementsByTagNameNS(uri, "lvalue-possibles");
		if (elements.getLength() == 0) {
			log.warning ("lvalues should enumerate all possible values in the <lvalue-possibles> tag.");
		} else if (elements.getLength() > 1) {
			log.warning ("lvalues should only have one <lvalue-possibles> tag.");
		}

		List lvalueEnums = new LinkedList();
		Set soFar = new HashSet();
		for (int i = 0; i < elements.getLength(); i++) {
			Element enumsEl = (Element) elements.item(i);
			NodeList lvalueEnumNodes =
				enumsEl.getElementsByTagNameNS(uri, "lvalue-enum");
			if (lvalueEnumNodes.getLength() == 0) {
				log.warning ("lvalues must have at least one <lvalue-enum> tag.");
			} else {
				for (int j = 0; j < lvalueEnumNodes.getLength(); j++) {
					Element currEnum = (Element) lvalueEnumNodes.item(j);
					String s = currEnum.getAttribute("value");
					if (!soFar.contains(s)) {
						soFar.add(s);
						lvalueEnums.add(s);
					}
				}
			}
		}
		String[] np = new String[lvalueEnums.size()];
		lvalueEnums.toArray(np);
		Lvalue nlv = new Lvalue();
		nlv.possibles = np;
		nlv.setReverseMap();
		return nlv;
	}
	
	/**
	 * @see viper.api.extensions.ExtendedAttrValueParser#getXMLFormatConfig(org.w3c.dom.Document, Node)
	 */
	public Element getXMLFormatConfig(Document root, Node container) {
		String qualifier = ViperData.ViPER_DATA_QUALIFIER;
		String uri = ViperData.ViPER_DATA_URI;
		Element poss =
			root.createElementNS(uri, qualifier + "lvalue-possibles");
		for (int i = 0; i < possibles.length; i++) {
			Element childEl =
				root.createElementNS(uri, qualifier + "lvalue-enum");
			childEl.setAttribute("value", possibles[i]);
			poss.appendChild(childEl);
		}
		return poss;
	}
}
