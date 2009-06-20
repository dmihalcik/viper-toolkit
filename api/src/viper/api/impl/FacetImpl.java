package viper.api.impl;

import java.util.*;
import java.util.logging.*;

import org.w3c.dom.*;

import viper.api.*;
import viper.api.Node;
import viper.api.extensions.*;
import viper.api.time.*;

/**
 * 
 */
public class FacetImpl extends EventfulNodeHelper
	implements Facet, XmlVisibleNode, EventfulNode {

	private Logger log = Logger.getLogger("viper.api.impl");
	private FacetConfig config;
	private Descriptor parent;
	
	AttributeImpl getReferenceAttributeImpl() {
		return (AttributeImpl) getReferenceAttribute();
	}

	public Attribute getReferenceAttribute() {
		return parent.getAttribute(config.getReferenceConfig());
	}
	
	protected Logger getLogger() {
		return log;
	}

	protected void helpSetChild(int i, Node n, TransactionalNode.Transaction t, boolean insert) {
		throw new UnsupportedOperationException();
	}

	protected void postHelpSetChild(int i, Node n, TransactionalNode.Transaction t, boolean insert) {
		throw new UnsupportedOperationException();
	}
	
	public void setAttrValue(Object v) throws NotStaticException, BadAttributeDataException {
		AttributeImpl ref = getReferenceAttributeImpl();
		ref.setInternalAttrValue(config.getFacetWrapper().setFacetValue(v, ref.getInternalAttrValue()));
	}

	public void setAttrValueAtSpan(Object v, InstantInterval span) throws UnknownFrameRateException, BadAttributeDataException {
		getReferenceAttribute().setAttrValueAtSpan(config.getParams().setAttributeValue(v, getReferenceAttribute()), span);
	}

	/* (non-Javadoc)
	 * @see viper.api.Attribute#getAttrName()
	 */
	public String getAttrName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see viper.api.Attribute#getAttrValue()
	 */
	public Object getAttrValue() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see viper.api.Attribute#getAttrValueAtInstant(viper.api.time.Instant)
	 */
	public Object getAttrValueAtInstant(Instant i) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see viper.api.Attribute#getAttrValuesOverSpan(viper.api.time.InstantInterval)
	 */
	public Iterator getAttrValuesOverSpan(InstantInterval s) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see viper.api.Attribute#getAttrValuesOverWholeRange()
	 */
	public Iterator getAttrValuesOverWholeRange() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see viper.api.Attribute#iterator()
	 */
	public Iterator iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see viper.api.Attribute#getDescriptor()
	 */
	public Descriptor getDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see viper.api.Attribute#getAttrConfig()
	 */
	public AttrConfig getAttrConfig() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see viper.api.TemporalNode#getRange()
	 */
	public TemporalRange getRange() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see viper.api.TemporalNode#setRange(viper.api.time.TemporalRange)
	 */
	public void setRange(TemporalRange r) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see viper.api.Node#getChildren()
	 */
	public Iterator getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see viper.api.Node#getParent()
	 */
	public Node getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see viper.api.Node#getNumberOfChildren()
	 */
	public int getNumberOfChildren() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see viper.api.Node#indexOf(viper.api.Node)
	 */
	public int indexOf(Node n) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see viper.api.Node#hasChild(viper.api.Node)
	 */
	public boolean hasChild(Node n) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see viper.api.Node#getChild(int)
	 */
	public Node getChild(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see viper.api.extensions.XmlVisibleNode#getXMLFormat(org.w3c.dom.Document)
	 */
	public Element getXMLFormat(Document root) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see viper.api.Attribute#startAggregating()
	 */
	public void startAggregating(){
		throw new UnsupportedOperationException();
	}
	
	/* (non-Javadoc)
	 * @see viper.api.Attribute#aggregateSetAttrValueAtSpan(java.lang.Object, viper.api.time.InstantInterval)
	 */
	public void aggregateSetAttrValueAtSpan(Object v, InstantInterval span) throws UnknownFrameRateException, BadAttributeDataException{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see viper.api.Attribute#finishAggregating(boolean)
	 */
	public void finishAggregating(boolean undoable){
		throw new UnsupportedOperationException();
	}
}
