package edu.umd.cfar.lamp.chronicle.extras.emblems;

import java.awt.*;

import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.chronicle.extras.*;

/**
 * Draws +/- emblems next to labels that can be expanded.
 */
public class TreeEmblemModel extends EmptyEmblemModel {
	private Image expandIcon;
	private Image contractIcon;
	private TreeChronicleViewModel tcm;
	
	/**
	 * Creates a new emblem model for the given data set.
	 * @param tcm the tree data set
	 */
	public TreeEmblemModel(TreeChronicleViewModel tcm) {
		this.tcm = tcm;
	}
	
	/**
	 * Expands/collapses the given node, if possible.
	 * @param tqe the timeline of whom to display/hide the children
	 * @param i zero
	 */
	public void click(TimeLine tqe, int i) {
		if (i == 0) {
			if (tcm.isExpanded(tqe)) {
				tcm.collapseLine(tqe);
			} else {
				tcm.expandLine(tqe);
			}
		}
	}
	
	/**
	 * @inheritDoc
	 */
	public Image getEmblemFor(TimeLine tqe, int i) {
		if (i == 0 && tcm.canExpand(tqe)) {
			return tcm.isExpanded(tqe) ? contractIcon : expandIcon;
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 * @return one
	 */
	public int getMaxEmblemCount() {
		return 1;
	}

	/**
	 * {@inheritDoc}
	 * @return Collapse or Expand
	 */
	public String getTextEmblemFor(TimeLine tqe, int i) {
		if (i == 0 && tcm.canExpand(tqe)) {
			return tcm.isExpanded(tqe) ? "Collapse" : "Expand";
		} else {
			return "";
		}
	}

	/**
	 * Gets the emblem that is displayed on an
	 * expanded node.
	 * @return the contract emblem
	 */
	public Image getContractIcon() {
		return contractIcon;
	}

	/**
	 * Gets the emblem that is displayed on an
	 * contracted node.
	 * @return the expand emblem
	 */
	public Image getExpandIcon() {
		return expandIcon;
	}
	
	/**
	 * Gets the tree model associated with this emblem model.
	 * @return the tree model
	 */
	public TreeChronicleViewModel getTreeModel() {
		return tcm;
	}

	/**
	 * Sets the emblem that is displayed on an
	 * expanded node.
	 * @param image the contract emblem
	 */
	public void setContractIcon(Image image) {
		contractIcon = image;
		fireChangeEvent();
	}

	/**
	 * Sets the emblem that is displayed on an
	 * contracted node.
	 * @param image the expand emblem
	 */
	public void setExpandIcon(Image image) {
		expandIcon = image;
		fireChangeEvent();
	}
	
	/**
	 * Sets the tree model associated with this emblem model.
	 * @param tcm the tree model to view
	 */
	public void setTreeModel(TreeChronicleViewModel tcm) {
		if (this.tcm != tcm) {
			this.tcm = tcm;
			fireChangeEvent();
		}
	}
}
