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


package edu.umd.cfar.lamp.viper.examples.persontracking;

import java.awt.geom.*;
import java.lang.ref.*;

import javax.swing.*;

import org.apache.commons.collections.*;

import edu.oswego.cs.dl.util.concurrent.*;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.util.*;

/**
 * A PNode whose value is retrieved from a background process
 * and may change over time. This also allows for 'just-in-time'
 * node decoding.
 * @author davidm
 */
public class PWorkerNode extends PNode {
	private SoftReference lastNodeRef = null;
	
	/**
	 * Modified factory method that has a 'percent complete'
	 * field. This field is atomic, so any thread may access it.
	 * In order to be cancellable, the create method should 
	 * take care to yield occasionally to listen for interruptions.
	 * The worker node will keep calling the create method until
	 * the factory either returns 100 (complete) or -1 (cancelled),
	 * or it is no longer required (e.g. the node has no parent).
	 * <p />
	 * A version should also respect what other people do to the 
	 * percent complete - e.g. if percent complete is 100, you should
	 * only return the complete node. If percent complete is decremented,
	 * you should return a correspondingly simpler shape - e.g. a simple rectangle
	 * or something for 0%. Negative values are 'cancel', and you construct
	 * should return <code>null</code>.
	 */
	public static abstract class ProgressiveFactory implements Factory {
	    /** 
	     * Class to maintain atomic reference to an int.
	     */
	    private static class IntVar {
	        private int value;
	        IntVar(int i) { value = i; }
	        synchronized int get() { return value; }
	        synchronized void set(int i) { value = i; }
	    }
	    
	    private IntVar percentComplete = new IntVar(0);
	    
	    /**
	     * Gets how close the node is to completion. A negative value indicates
	     * the node will never be completed, and you should stop trying. A 
	     * positive value less than 100 indicates that the node is not yet 
	     * complete, and create is either in progress or should be invoked again.
	     * @return the percent the node is complete
	     */
	    public int getPercentComplete() {
	    	return percentComplete.get();
	    }
	    
	    /**
	     * Sets how complete the node is.
	     * @param percent how complete the node is now
	     */
	    protected void setPercentComplete(int percent) {
	    	percentComplete.set(percent);
	    }
	}
	
	private ProgressiveFactory nodeFactory;
	
	protected NodeWorker worker; 
	
	protected class NodeWorker extends SwingWorker {
		public boolean completelyConstructed = false;
		public Object construct() {
			return nodeFactory.create();
		}
		public void finished() {
			int pc = nodeFactory.getPercentComplete();
			if (pc < 0 || 100 <= pc) {
				completelyConstructed = true;
			}
			removeAllChildren();
			
			PNode newChild = (PNode) get();
			lastNodeRef = new SoftReference(newChild);
			PWorkerNode.this.invalidatePaint();
			if (!completelyConstructed) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						hireWorker();
					}
				});
			}
		}
	}
	
	/**
	 * 
	 */
	public PWorkerNode() {
		super();
	}
	
	/**
	 * 
	 */
	public PWorkerNode(ProgressiveFactory factory) {
		super();
		setNodeFactory(factory);
	}


	public void setNodeFactory(ProgressiveFactory factory) {
		if (this.nodeFactory == factory) {
			return;
		}
		killWorker();
		this.nodeFactory = factory;
	}

	
	private class HireWorker implements Runnable {
		private PWorkerNode outer() {
			return PWorkerNode.this;
		}
		
		public void run() {
			PWorkerNode.this.worker = new NodeWorker();
			PWorkerNode.this.worker.start();
		}

		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj instanceof HireWorker) {
				return ((HireWorker) obj).outer().equals(this.outer());
			}
			return false;
		}

		public int hashCode() {
			return outer().hashCode();
		}
		
	}
	
	public static final Executor defaultExecutor = new PooledExecutor(2);
	protected Executor executor = defaultExecutor;
	
	/**
	 * 
	 */
	private void hireWorker() {
		Runnable hire = new HireWorker();
		if (executor == null) {
			hire.run();
		} else {
			try {
				executor.execute(hire);
			} catch (InterruptedException e) {
				killWorker();
			}
		}
	}

	protected void finalize() throws Throwable {
		super.finalize();
		killWorker();
	}

	/**
	 * 
	 */
	private void killWorker() {
		if (this.worker != null && !this.worker.completelyConstructed) {
			this.worker.interrupt();
			this.nodeFactory.setPercentComplete(-1);
		}
	}
	
	protected PNode getLastNode() {
		if (lastNodeRef == null) {
			return null;
		}
		return (PNode) lastNodeRef.get();
	}

	public boolean intersects(Rectangle2D b) {
		PNode lastNode = getLastNode();
		boolean intersectsR = false;
		if (lastNode != null) {
			intersectsR = lastNode.intersects(b);
		} else {
			intersectsR = super.intersects(b);
		}
		return intersectsR;
	}

	protected void paint(PPaintContext paintContext) {
		PNode lastNode = getLastNode();
		if (lastNode != null) {
			lastNode.fullPaint(paintContext);
		} else {
			super.paint(paintContext);
			rehireWorkerIfNecessary();
		}
	}

	/**
	 * 
	 */
	protected void rehireWorkerIfNecessary() {
		if (0 <= this.nodeFactory.getPercentComplete()) {
			this.nodeFactory.setPercentComplete(0);
			hireWorker();
		}
	}
}
