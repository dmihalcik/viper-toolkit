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

package edu.umd.cfar.lamp.viper.util;

import java.io.*;
import java.net.*;
import java.util.*;

import org.w3c.dom.*;

/**
 * @author davidm@cfar.umd.edu
 * @since Jun 20, 2003
 */
public class XmlHelper {
	/**
	 * Given a uri that represents a qualified name, tries to 
	 * determine the index of the split point between the namespace
	 * and the local name. Returns the last index of #, /, or :,
	 * checking for each. If the string contains no :, it doesn't count 
	 * as a uri and nothing is returned.
	 * @param uri the uri
	 * @return the split point, or -1 if not found
	 */
	public static int getQNameSplitPoint(String uri) {
		int col = uri.indexOf(":");
		if (col < 0) {
			return -1;
		} else {
			int hash = uri.indexOf("#");
			if (hash < 0) {
				int slash = Math.max(uri.lastIndexOf("/"), uri.lastIndexOf("\\"));
				if (slash > 0) {
					return slash;
				} else {
					return col;
				}
			} else {
				return hash;
			}
		}
	}
	/**
	 * Gets the local part of a uri.
	 * @param uri the uri to split
	 * @return the local part, if found
	 */
	public static String localName(String uri) {
		int i = getQNameSplitPoint(uri) + 1;
		if (i < uri.length()) {
			return uri.substring(i);
		} else {
			return "";
		}
	}
	/**
	 * gets the namespace part of the uri
	 * @param uri the uri to split
	 * @return the namespace part
	 */
	public static String namespacePart(String uri) {
		int splitChar = getQNameSplitPoint(uri);
		if (splitChar > 0) {
			return uri.substring(0, splitChar);
		} else {
			return "";
		}
	}
	/**
	 * Splits the rdf-style uri into namespace
	 * uri and local name 
	 * @param uri the uri to split
	 * @return the split uri
	 */
	public static String[] splitIntoQName(String uri) {
		String[] S = new String[] {"", ""};
		int i = getQNameSplitPoint(uri);
		if (i > 0) {
			S[0] = uri.substring(0, i);
		}
		i++;
		if (i < uri.length()) {
			S[1] = uri.substring(i);
		}
		return S;
	}
	
	private static final String charset = "UTF-8";
	
	/**
	 * Except-iterator that operates on iterators of DOM nodes,
	 * returning only elements in the list.
	 */
	public static final ExceptIterator.ExceptFunctor ELEMENTS_ONLY = new ExceptIterator.ExceptFunctor() {
		/**
		 * Checks to see that the node is an element.
		 */
		public boolean check(Object o) {
			return ((Node) o).getNodeType() == Node.ELEMENT_NODE;
		}
	};
	
	/**
	 * Combined the namespace and the local name back together.
	 * @param ns the namespace uri
	 * @param lname the local name
	 * @return the joined uri
	 */
	public static String unsplit(String ns, String lname) {
		return unsplit(ns, lname, "#");
	}

	/**
	 * Combined the namespace and the local name back together.
	 * @param ns the namespace uri
	 * @param lname the local name
	 * @param joiner the join string, e.g. <code>#</code>
	 * @return the joined uri
	 */
	public static String unsplit(String ns, String lname, String joiner) {
		if (!ns.endsWith(joiner)) {
			ns += joiner;
		}
		try {
			return ns + URLEncoder.encode(lname, charset);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("You don't support " + charset + "?");
		}
	}
	
	/**
	 * Converts a w3c DOM nodelist into an iterator.
	 * @param nl the node list
	 * @return the iterator wrapping the list of DOM nodes
	 */
	public static Iterator nodeList2Iterator(final NodeList nl) {
		final int[] i = new int[]{0};
		return new Iterator() {
			public boolean hasNext() {
				return i[0] < nl.getLength();
			}

			public Object next() {
				return nl.item(i[0]++);
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
