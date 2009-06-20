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
package edu.umd.cfar.lamp.chronicle.extras;

import java.util.*;

import edu.umd.cfar.lamp.chronicle.*;

/**
 * A chronicle view model that allows expanding the 
 * timeline graph as a tree from the timeline root.
 */
public abstract class TreeChronicleViewModel extends AbstractChronicleViewModel {
	/// The currently displayed lines
	/// This may change when a user expands/collapses a node,
	/// or when the model changes 
	private List displayedLines;

	/// The currently expanded nodes
	protected Set expandeds;

	/// The set of currently displayed roots
	protected Collection roots;
	
	/// The graph
	protected ChronicleDataModel graph;
	
	/**
	 * Constructs an empty tree model.
	 */
	public TreeChronicleViewModel() {
		this.displayedLines = new ArrayList();
		this.expandeds = new HashSet();
		this.graph = null;
		this.treeCml = new ChronicleModelListener() {
			public void timeLinesChanged(ChronicleEvent e) {
				fireDataChanged(null);
			}
			public void timeLinesAdded(ChronicleEvent e) {
				resetLines();
				fireDataChanged(null);
			}
			public void timeLinesRemoved(ChronicleEvent e) {
				resetLines();
				fireDataChanged(null);
			}
			public void structureChanged(ChronicleEvent e) {
				resetLines();
				fireDataChanged(null);
			}
		};
	}
	
	
	private void addLines(TimeLine tqe, Set already) {
		if (!already.contains(tqe)) {
			this.displayedLines.add(tqe);
			already.add(tqe);
			if (expandeds.contains(tqe)) {
				for (Iterator iter = tqe.getChildren(); iter.hasNext(); ) {
					addLines((TimeLine) iter.next(), already);
				}
			}
		}
	}
	protected void resetLines() {
		this.displayedLines.clear();
		HashSet already = new HashSet();
		for (Iterator iter = roots.iterator(); iter.hasNext(); ) {
			addLines((TimeLine) iter.next(), already);
		}
	}

	/**
	 * @inheritDoc
	 */
	public int getSize() {
		return displayedLines.size();
	}

	/**
	 * @inheritDoc
	 */
	public TimeLine getElementAt(int i) {
		return (TimeLine) displayedLines.get(i);
	}
	
	/**
	 * Tests to see if the given line is expanded.
	 * @param tqe the line to test
	 * @return if the given line's children are currently displayed
	 */
	public boolean isExpanded(TimeLine tqe) {
		return expandeds.contains(tqe);
	}
	
	public int indexOf(TimeLine tqe) {
		return displayedLines.indexOf(tqe);
	}

	/**
	 * Tests to see if the node has children.
	 * @param tqe if the node has children
	 * @return if it is worth displaying the expand/collapse emblem
	 */
	public boolean canExpand(TimeLine tqe) {
		return tqe.getNumberOfChildren() > 0;
	}
	
	/**
	 * Expands the given line in the view.
	 * @param tqe the line of which to display the children
	 */
	public void expandLine(TimeLine tqe) {
		if (!isExpanded(tqe)) {
			expandeds.add(tqe);
			resetLines();
			fireFocusChange(null);
		}
	}

	/**
	 * Help by expanding this node and all its kids recursively
	 * @param t
	 * @return true if change
	 */
	protected boolean helpExpandAll(TimeLine t) {
		if (!isExpanded(t)) {
			expandeds.add(t);
			for (Iterator iter = t.getChildren(); iter.hasNext(); ) {
				helpExpandAll ((TimeLine) iter.next());
			}
			return true;
		} else {
			return false;
		}
	}
	/**
	 * Expands the all the lines in the chronicle
	 */
	public void expandAll() {
		expandeds.clear();
		boolean changed = false;
		for (Iterator iter = roots.iterator(); iter.hasNext(); ) {
			changed = helpExpandAll((TimeLine) iter.next()) || changed;
		}
		if (changed) {
			resetLines();
			fireFocusChange(null);
		}
	}
	
	/**
	 * Expands all the children down the tree from this node.
	 * @param t the node to expand down from
	 */
	public void expandAllFrom(TimeLine t) {
		Set alreadyExpanded = expandeds;
		expandeds = new HashSet();
		if (helpExpandAll(t)) {
			expandeds.removeAll(alreadyExpanded);
			if (!expandeds.isEmpty()) {
				resetLines();
				fireFocusChange(null);
			}
		}
	}


	/**
	 * Collapse this node completely.
	 * @param tqe the node to collapse in the view
	 */
	public void collapseLine(TimeLine tqe) {
		if (expandeds.contains(tqe)) {
			expandeds.remove(tqe);
			resetLines();
			fireFocusChange(null);
		}
	}
	
	
	private ChronicleModelListener treeCml;
	protected void setGraph(ChronicleDataModel graph) {
		if (this.graph != null) {
			this.graph.removeChronicleModelListener(treeCml);
		}
		this.graph = graph;
		if (this.graph != null) {
			this.graph.addChronicleModelListener(treeCml);
		}
		resetLines();
	}
}
