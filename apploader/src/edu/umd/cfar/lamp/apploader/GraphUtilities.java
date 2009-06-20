package edu.umd.cfar.lamp.apploader;

import java.util.*;

/**
 * 
 */
public class GraphUtilities {
	public static interface ObjectSorder {
		public Iterator getOutboundLinks(Object node);
	}
	/**
	 * Auxiliary structure for the depth-first search.
	 */
	private static final class DFSColorAndNumber {
		static final int WHITE = 0;
		static final int GRAY = 1;
		static final int BLACK = 2;

		int color = WHITE;
		int d = -1;
		int f = -1;
		Object node = null;
	}
	
	private static void tsortVisit(Object node, DFSColorAndNumber uDesc,
			Map G, List order, int time, ObjectSorder sorder) {
		uDesc.color = DFSColorAndNumber.GRAY;
		uDesc.d = ++time;

		for (Iterator iter = sorder.getOutboundLinks(node); iter.hasNext(); ) {
			Object v = iter.next();
			DFSColorAndNumber vDesc = (DFSColorAndNumber) G.get(v);
			if (vDesc == null) {
				throw new IllegalArgumentException("Cannot have link outside of node set");
			}
			if (vDesc.color == DFSColorAndNumber.WHITE) {
				vDesc.node = node;
				tsortVisit(v, vDesc, G, order, time, sorder);
				time = vDesc.f;
			}
		}

		order.add(0, node); // put u in front of the topological order
		uDesc.color = DFSColorAndNumber.BLACK;
		uDesc.f = ++time;
	}

	public static List topologicalSort(Iterator nodes, ObjectSorder order) {
		List L = new LinkedList();
		Map G = new HashMap();
		int time = 0;

		while (nodes.hasNext()) {
			G.put(nodes.next(), new DFSColorAndNumber());
		}

		for (Iterator iter = G.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object curr = entry.getKey();
			DFSColorAndNumber desc = (DFSColorAndNumber) entry.getValue();
			if (desc.color == DFSColorAndNumber.WHITE) {
				tsortVisit(curr, desc, G, L, time, order);
				time = desc.f;
			}
		}
		return L;
	}
}
