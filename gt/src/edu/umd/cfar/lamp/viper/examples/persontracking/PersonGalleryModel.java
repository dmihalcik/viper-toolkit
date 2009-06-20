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

import infovis.*;
import infovis.column.*;
import infovis.table.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.event.*;

import org.apache.commons.collections.*;
import org.apache.commons.collections.iterators.*;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.lang.*;

import viper.api.*;
import viper.api.datatypes.*;
import viper.api.extensions.*;
import viper.api.extensions.TransactionalNode.*;
import viper.api.time.*;
import viper.api.time.Frame;
import edu.oswego.cs.dl.util.concurrent.*;
import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.viper.examples.persontracking.images.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.gui.players.*;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cs.piccolo.*;

public class PersonGalleryModel {
	private static final Logger log = Logger
			.getLogger("edu.umd.cfar.lamp.viper.examples.persontracking");
	public static final String UNDO_URI_PREFIX = "http://viper-toolkit.sourceforge.net/owl/gt/people#";
	public static final String UNDO_ADD_ENTITY = UNDO_URI_PREFIX + "addEntity";
	
	
	private ImageArchive imageArchive = new ImageArchive();
	private Map correlogramCache = new ReferenceMap(ReferenceMap.WEAK, ReferenceMap.HARD);
	private ViperViewMediator mediator;
	private int[][][][] referenceCgrams;
	private GalleryEvidence[] evidenceForSimilarity = new GalleryEvidence[] {};
	private List evidenceToChangeCommands = new ArrayList();
	private EventListenerList listenerList = new EventListenerList();
	
	public PersonGalleryModel() {
		super();
		initializeInfovisTables();
	}
	
	private long lastRefresh;
	
	public boolean upToDate () {
		if (lastRefresh < getMediator().getViperData().getLastModifiedTime()) {
			return false;
		}
		return true;
	}
	
	private class EvidenceRow implements GalleryEvidence {
		private int descId;
		private ImageSlice id;
		private long creationTime;
		
		private int row;
		
		private EvidenceRow (int row) {
			this.row = row;
			id = ImageSlice.createImageSlice(mediator.getCurrFileName(), frameForEvidence.get(row), (BoundingBox) bboxForEvidence.get(row));
			this.descId = entityForEvidence.get(row);
			this.creationTime = lastRefresh;
		}
		
		public boolean upToDate() {
			long vtime = getMediator().getViperData().getLastModifiedTime();
			if (this.creationTime < vtime) {
				long oldCreationTime = this.creationTime;
				boolean wrong = false;
				wrong = wrong || (this.descId != entityForEvidence.get(row));
				wrong = wrong || (this.id.getFrame() != frameForEvidence.get(row));
				wrong = wrong || (!this.id.getBox().equals(bboxForEvidence.get(row))); 
				// XXX Is this necessary? Should evidence be marked as out of date if the box changes?
				// yes, because otherwise the correlogram will be off.
				if (wrong) {
					log.warning("GalleryEvidence cache expired");
					entities: for (int i = 0; i < getEntityCount(); i++) {
						if (this.descId == idForEntities.get(row)) {
							GalleryEntity entity = PersonGalleryModel.this.getEntity(i);
							evidences: for (int j = 0; j < getEvidenceCount(); j++) {
								if (this.id.getFrame() == frameForEvidence.get(j)) {
									this.row = j;
									if (!this.id.getBox().equals(bboxForEvidence.get(row))) {
										break entities;
									}
									this.creationTime = vtime;
									return true;
								}
							}
						}
					}
					return false;
				} else {
					this.creationTime = vtime;
				}
			} else {
				assert this.id.getFrame() == frameForEvidence.get(row);
				assert this.descId == entityForEvidence.get(row);
				assert this.id.getBox().equals(bboxForEvidence.get(row)); 
			}
			return true;
		}
		
		private void verify() {
			if (!upToDate()) {
				throw new IllegalStateException("Evidence out of date");
			}
		}
		public BoundingBox getBox() {
			verify();
			return this.id.getBox();
		}
		public int[][][] getCorrelogramForEvidence() {
			verify();
			return (int[][][]) correlogramForEvidence.get(row);
		}
		public int getFrame() {
			verify();
			return this.id.getFrame();
		}
		public GalleryEntity getEntity() {
			verify();
			return PersonGalleryModel.this.getEntity(descriptor2entity(descId));
		}
		public int getSimilarity() {
			verify();
			return similarityToCurrentForEvidence.get(row);
		}
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof EvidenceRow) {
				return ((EvidenceRow) obj).descId == this.descId && ((EvidenceRow) obj).id.equals(this.id);
			}
			return false;
		}
		public int hashCode() {
			return descId ^ id.hashCode();
		}

		public ImageSlice getSlice() {
			return id;
		}

		public int getPriority() {
			verify();
			return priorityForEvidence.get(row);
		}
	}
	public class EntityRow implements GalleryEntity {
		private int descId;
		private int row;
		private long creationTime;
		private EntityRow (int row) {
			this.row = row;
			this.descId = idForEntities.get(row);
			this.creationTime = lastRefresh;
		}
		public boolean upToDate() {
			long vtime = getMediator().getViperData().getLastModifiedTime();
			if (this.creationTime < vtime) {
				log.warning("entity out of date");
				return update(vtime);
			} else {
				assert this.descId == idForEntities.get(row);
			}
			return true;
		}
		/**
		 * @param vtime
		 * @return
		 */
		private boolean update(long vtime) {
			if (this.descId != idForEntities.get(row)) {
				log.warning("GalleryEntity cache expired");
				for (int i = 0; i < getEntityCount(); i++) {
					if (this.descId == idForEntities.get(row)) {
						this.row = i;
						this.creationTime = vtime;
						return true;
					}
				}
				return false;
			} else {
				this.creationTime = vtime;
				return true;
			}
		}
		private void verify() {
			if (!upToDate()) {
				throw new IllegalStateException("Entity out of date");
			}
		}
		public int getId() {
			verify();
			return descId;
		}
		public String getName() {
			verify();
			return nameForEntities.get(row);
		}
		public String getDisplayName() {
			String realName = getName();
			if (realName != null) {
				return realName;
			}
			return "Person " + getId();
		}
		public TemporalRange getRange() {
			verify();
			return mediator.getCurrFile().getDescriptor(getPersonConfig(), getId()).getValidRange();
		}
		public Descriptor getDescriptor() {
			verify();
			return mediator.getCurrFile().getDescriptor(getPersonConfig(), getId());
		}
		public Iterator getEvidence() {
			verify();
			return new FilterIterator(getAllEvidence(), new Predicate() {
				public boolean evaluate(Object o) {
					GalleryEvidence e = (GalleryEvidence) o;
					return EntityRow.this.equals(e.getEntity());
				}
			});
		}
		public boolean equals(Object obj) {
			verify();
			if (this == obj) {
				return true;
			}
			if (obj instanceof EntityRow) {
				return ((EntityRow) obj).descId == this.descId;
			}
			return false;
		}
		public int hashCode() {
			return row;
		}
		public GalleryEvidence getEvidenceAtFrame(int f) {
			verify();
			for (int i = 0; i < evidenceTable.getRowCount(); i++) {
				if (entityForEvidence.get(i) == descId && frameForEvidence.get(i) == f) {
					return PersonGalleryModel.this.getEvidence(i);
				}
			}
			return null;
		}
		public GalleryEntity setName(String newName) {
			getDescriptor().getAttribute("Name").setAttrValue(newName);
			update(getMediator().getViperData().getLastModifiedTime());
			return this;
		}
		public GalleryEntity setValidRange(InstantRange ir) {
			getDescriptor().setValidRange(ir);
			update(getMediator().getViperData().getLastModifiedTime());
			return this;
		}
	}
	
	private int descriptor2entity(Descriptor d) {
		if (d == null) {
			return -1;
		}
		int descId = d.getDescId();
		return descriptor2entity(descId);
	}

	private int descriptor2entity(int descId) {
		for (int i = 0; i < getEntityCount(); i++) {
			if (idForEntities.get(i) == descId) {
				return i;
			}
		}
		return -1;
	}

	public GalleryEvidence getEvidence(int row) {
		if (row < 0 || getEvidenceCount() <= row) {
			throw new IndexOutOfBoundsException();
		}
		return new EvidenceRow(row);
	}
	public Iterator getEvidenceOnFrame(final int frame) {
		return new FilterIterator(getAllEvidence(), new Predicate() {
			public boolean evaluate(Object o) {
				GalleryEvidence e = (GalleryEvidence) o;
				return e.getFrame() == frame;
			}
		});
	}
	public Iterator getAllEvidence() {
		return new TransformIterator(new CountingIterator(0, getEvidenceCount()-1), new Transformer() {
			public Object transform(Object o) {
				return getEvidence(((Integer) o).intValue());
			}
		});
	}
	public GalleryEntity getEntity(int row) {
		if (row < 0 || getEntityCount() <= row) {
			throw new IndexOutOfBoundsException();
		}
		return new EntityRow(row);
	}
	public GalleryEntity getEntity(Descriptor d) {
		return getEntity(descriptor2entity(d));
	}
	public GalleryEntity getEntity(String name) {
		return getEntity(nameForEntities.indexOf(name));
	}
	
	private Descriptor getDescriptorFromEntity(int entityRow) {
		int id = idForEntities.get(entityRow);
		if (id < 0) {
			return null;
		}
		return mediator.getCurrFile().getDescriptor(getPersonConfig(), id);
	}
	
	
	/**
	 * entity table has: string: name, int:id
	 */
	private Table entityTable;

	private IntColumn idForEntities;

	private StringColumn nameForEntities;

	/**
	 * entity table has: int: entity_id, int: frame_no, correlogram, images
	 */
	private Table evidenceTable;

	private IntColumn entityForEvidence;

	private IntColumn frameForEvidence;

	private IntColumn similarityToCurrentForEvidence;

	private IntColumn priorityForEvidence;
	
	private ObjectColumn correlogramForEvidence;

	private ObjectColumn bboxForEvidence;
	
	/**
	 * @return
	 */
	public Config getPersonConfig() {
		return mediator.getViperData().getConfig(Config.OBJECT,
				"Person");
	}

	/**
	 * @param entity
	 * @return
	 */
	public Descriptor getPersonDescripotInstanceForEntity(int entity) {
		return mediator.getCurrFile().getDescriptor(getPersonConfig(),
				entity);
	}

	/**
	 * @param evidence
	 */
	public void deletePersonEvidence(GalleryEvidence evidence) {
		int f = evidence.getFrame();
		EntityRow entity = (EntityRow) evidence.getEntity();
		Descriptor entityD = entity.getDescriptor();
		Attribute evidenceA = entityD.getAttribute("Location");
		evidenceA.setAttrValueAtSpan(null, new Span(new Frame(f),
				new Frame(f + 1)));
		if (!evidenceA.getAttrValuesOverWholeRange().hasNext()) {
			entityD.getParent().removeChild(entityD);
		}
	}
	
	/**
	 * @param entity
	 * @param f
	 */
	public void selectPersonAtFrame(GalleryEntity entity, int f) {
		Config personC = mediator.getViperData().getConfig(Config.OBJECT,
				"Person");
		Descriptor entityD = mediator.getCurrFile().getDescriptor(personC,
				entity.getId());
		//keep only evidences of current object
		//Nagia
		//start
	//	evidenceTable.clear();
	//	addEvidencesFor(entity);
		
		//end
		
		Attribute evidenceA = entityD.getAttribute("Location");
//		if (editRadioButton != null) {
//			editRadioButton.setSelected(true);
//			personSelector.setEditing(true);
//		}
		mediator.setMajorMoment(new Frame(f));
		mediator.getSelection().setTo(evidenceA);
		
	}
	public int getVideoFrameCount() {
		return (int) mediator.getCurrFile().getReferenceMedia().getSpan().width();
	}

	public ImageSlice createImageSlice(GalleryEvidence ev) {
		return ImageSlice.createImageSlice(mediator.getCurrFileName(),
				ev.getFrame(), ev.getBox());
	}
	
	/**
	 * @param dp
	 * @param bbox
	 * @param f
	 * @return
	 */
	public BufferedImage getSubImage(ImageSlice id) {
		// TODO switch to appropriate data player by URI
		DataPlayer dp = mediator.getDataPlayer();
		if (dp == null) {
			log.severe("Tried to read a slice while dataplayer was null: slice " + id);
			return null;
		}
		Object dpuri = dp.getURI();
		Object iduri = id.getSource();
		if (!ObjectUtils.equals(dpuri, iduri)) {
			log.severe("Tried to read a slice from dataplayer with wrong URI: player's URI: " + dp.getURI() + ", slice  "+ id);
			return null;
		}
		Image fromDataPlayer = dp.getImage(new Frame(id.getFrame()));
		if (Thread.interrupted()) {
			log.severe("Interrupted after calling dataplayer.getImage for slice " + id);
			return null;
		}
		if (fromDataPlayer == null) {
			log.severe("Data Player returns null image for slice  "+ id);
			return null;
		}
		BufferedImage wholeImage = DataPlayer.toBufferedImage(fromDataPlayer);
		BoundingBox bbox = BoundingBox.intersection(id.getBox(),
				new BoundingBox(0, 0, wholeImage.getWidth(), wholeImage
						.getHeight()));
		if (Thread.interrupted()) {
			log.severe("Interrupted after rebuffering slice " + id);
			return null;
		}
		BufferedImage subImage = wholeImage.getSubimage(bbox.getX(), bbox
				.getY(), bbox.getWidth(), bbox.getHeight());
		return subImage;
	}
	public void checkCorrelogram(GalleryEvidence ev, ImageSlice id, Image subImage) {
		Object correl = correlogramCache.get(id);
		if (null == correl && null != subImage) {
			correl = SmartImageUtilities.buildCorrelogram((BufferedImage) subImage, null); 
			assert correl != null;
			correlogramCache.put(id, correl);
		}
		if (correl != null) {
			if (ev != null && ev.upToDate()) {
				EvidenceRow evR = (EvidenceRow) ev;
				correlogramForEvidence.set(evR.row, correl);
			}
		} else {
			log.warning("failed to get correlogram for " + id);
		}
	}
	public int[][][] getCorrelogramForSlice (ImageSlice id) {
		return (int[][][]) correlogramCache.get(id);
	}
	public void addToArchive(ImageSlice id, Callable f, Dimension2D prefRect) {
		imageArchive.put(id, f, prefRect, null);
	}
	public int getEvidenceCount() {
		return evidenceTable.getRowCount();
	}

	public int getEntityCount() {
		return entityTable.getRowCount();
	}

	public boolean isLoaded() {
		return mediator != null && mediator.getCurrFile() != null && mediator.getCurrentFrame() != null;
	}
	
	private void computeSimilarityToEvidence(GalleryEvidence[] evidenceIds) {
		resetReferenceCgrams(evidenceIds);
		for (int j = 0; j < getEntityCount(); j++) {
			GalleryEntity e = getEntity(j);
			int similarity = similarityToReferenceCgrams(this.getAnEvidenceForAnEntity(e));
			similarityToCurrentForEvidence.set(j, similarity);
		}
	}

	/**
	 * @param evidenceIds
	 */
	private void resetReferenceCgrams(GalleryEvidence[] evidenceIds) {
		if (referenceCgrams == null) {
			referenceCgrams = new int[evidenceIds.length][][][];
			for (int i = 0; i < evidenceIds.length; i++) {
				referenceCgrams[i] = evidenceIds[i].getCorrelogramForEvidence();
			}
		}
	}

	/**
	 * @param evidenceRow
	 * @return
	 */
	private int similarityToReferenceCgrams(GalleryEvidence evidence) {
		resetReferenceCgrams(this.evidenceForSimilarity);
		if (evidence == null) {
			return 0;
		}
		int[][][] cgram = evidence.getCorrelogramForEvidence();
		int similarity = 0;
		if (cgram != null) {
			for (int i = 0; i < referenceCgrams.length; i++) {
				if (referenceCgrams[i] == null) {
					continue;
				}
				similarity = Math.max(similarity, SmartImageUtilities
						.correlogramSimilarity(referenceCgrams[i], cgram));
			}
		}
		return similarity;
	}

	public GalleryEvidence getAnEvidenceForAnEntity(GalleryEntity entity) {
		EntityRow er = (EntityRow) entity;
		er.verify();
		for (int i = 0; i < getEvidenceCount(); i++) {
			if (er.descId == entityForEvidence.get(i)) {
				return getEvidence(i);
			}
		}
		return null;
	}

	public GalleryEvidence getAnEvidenceForAnEntity(GalleryEntity entity, int frame) {
		if (frame < 0) {
			return getAnEvidenceForAnEntity(entity);
		}
		for (int i = 0; i < getEvidenceCount(); i++) {
			if (frame == frameForEvidence.get(i)) {
				GalleryEvidence ge = getEvidence(i);
				if (ge.getEntity().equals(entity)) {
					return ge;
				}
			}
		}
		return null;
	}

	/**
	 * @param entityId
	 */
	private void computeSimilarity(GalleryEntity entity) {
		GalleryEvidence ev = getAnEvidenceForAnEntity(entity);
		if (null != ev) {
			referenceCgrams = null;
			computeSimilarityToEvidence(new GalleryEvidence[] { ev });
		}
	}

	public void setEvidenceForSimilarity(List tokens) {
		GalleryEvidence[] t = new GalleryEvidence[tokens.size()];
		boolean changed = t.length != evidenceForSimilarity.length;
		for (int i = 0; i < t.length; i++) {
			t[i] = (GalleryEvidence) tokens.get(i);
			if (!changed) {
				changed = t[i] != evidenceForSimilarity[i];
			}
		}
		evidenceForSimilarity = t;
		if (changed) {
			referenceCgrams = null;
			applyEvidenceSimilaritySort();
		}
	}
	
	public Iterator getEvidenceInOrder(Comparator evidenceSorter) {
		final int sz = getEvidenceCount();
		if (evidenceForSimilarity.length == 0) {
			return new Iterator() {
				int i = 0;

				public void remove() {
					throw new UnsupportedOperationException();
				}

				public Object next() {
					if (sz <= i) {
						throw new IndexOutOfBoundsException();
					}
					return getEvidence(i++);
				}

				public boolean hasNext() {
					return i < sz;
				}
			};
		}
		final ArrayList toSort = new ArrayList(sz);
		for (int i = 0; i < sz; i++) {
			toSort.add(getEvidence(i));
		}
		Collections.sort(toSort, evidenceSorter);
		return new Iterator() {
			int i = 0;

			public void remove() {
				throw new UnsupportedOperationException();
			}

			public Object next() {
				if (sz <= i) {
					throw new IndexOutOfBoundsException();
				}
				return toSort.get(i++);
			}

			public boolean hasNext() {
				return i < sz;
			}
		};
	}
	
	/**
	 * 
	 */
	private void initializeInfovisTables() {
		idForEntities = new IntColumn("id");
		nameForEntities = new StringColumn("name");
		entityTable = new DefaultTable();
		entityTable.setName("persons");
		entityTable.addColumn(idForEntities);
		entityTable.addColumn(nameForEntities);

		bboxForEvidence = new ObjectColumn("bbox");
		entityForEvidence = new IntColumn("person");
		frameForEvidence = new IntColumn("frame");
		priorityForEvidence = new IntColumn("priority");
		correlogramForEvidence = new ObjectColumn("correlogram");
		similarityToCurrentForEvidence = new IntColumn("score");
		evidenceTable = new DefaultTable();
		evidenceTable.setName("person blob");
		evidenceTable.addColumn(entityForEvidence);
		evidenceTable.addColumn(frameForEvidence);
		evidenceTable.addColumn(priorityForEvidence);
		evidenceTable.addColumn(correlogramForEvidence);
		evidenceTable.addColumn(similarityToCurrentForEvidence);
		evidenceTable.addColumn(bboxForEvidence);
		
//		LastAddedEvidences=new Vector(5);
	}

	/**
	 * 
	 */
	public void extractPeople() {
		evidenceTable.clear();
		entityTable.clear();
		// Essentially, forces old keys to be kept around until
		// the end of the method
		Set allIds = new HashSet();
		if (mediator == null) {
			log.severe("No mediator set!");
			return;
		}
		ViperData V = mediator.getViperData();
		lastRefresh = V.getLastModifiedTime();
		Config personC = getPersonConfig();
		if (personC == null) {
			log.severe("No 'Person' object descriptor declared");
			return;
		} else if (personC.getAttrConfig("Name") == null) {
			log.severe("'Person' descriptor missing the 'Name' attribute");
			return;
		} else if (personC.getAttrConfig("Location") == null) {
			log.severe("'Person' descriptor missing the 'Location' attribute");
			return;
		} else if (personC.getAttrConfig("Name").isDynamic()) {
			log.severe("'Person : Name' attribute must be static");
			return;
		} else if (!ViperDataFactoryImpl.SVALUE.equals(personC.getAttrConfig(
				"Name").getAttrType())) {
			log.severe("'Person : Name' must be a string");
			return;
		} else if (!personC.getAttrConfig("Location").isDynamic()) {
			log.severe("'Person : Location' attribute must be dynamic");
			return;
		} else if (!ViperDataFactoryImpl.BBOX.equals(personC.getAttrConfig(
				"Location").getAttrType())) {
			log.severe("'Person : Location' attribute must be a bounding box");
			return;
		} else if (mediator.getCurrFile() == null) {
			log.severe("No current file selected");
			return;
		}

		Iterator iter = mediator.getCurrFile().getDescriptorsBy(personC);
		if (!iter.hasNext()) {
			log.warning("No 'Person' object descriptor instances found");
			return;
		}
		while (iter.hasNext()) {
			Descriptor personD = (Descriptor) iter.next();
			int entity = personD.getDescId();
			idForEntities.add(entity);
			nameForEntities.add(personD.getAttribute("Name").getAttrValue());

			DataPlayer dp = mediator.getDataPlayer();
			Iterator evIter = personD.getAttribute("Location")
					.getAttrValuesOverWholeRange();
			while (evIter.hasNext()) {
				DynamicAttributeValue currAV = (DynamicAttributeValue) evIter
						.next();
				BoundingBox bbox = (BoundingBox) currAV.getValue();
				Iterator frameIter = dp.getRate().asFrame(currAV).iterator();
				while (frameIter.hasNext()) {
					Frame f = (Frame) frameIter.next();
					if (!personD.getValidRange().contains(f)) {
						continue;
					}
					Integer priority = null; 
					if (personD.getConfig().hasAttrConfig("Priority")) {
						priority = (Integer) personD.getAttribute("Priority").getAttrValueAtInstant(f);
					}
					
					entityForEvidence.add(entity);
					frameForEvidence.add(f.intValue());
					bboxForEvidence.add(bbox);
					correlogramForEvidence.add(null);
					similarityToCurrentForEvidence.add(0);
					priorityForEvidence.add(priority == null ? Integer.MAX_VALUE : priority.intValue());
					assert entityForEvidence.size() == frameForEvidence.size();
					assert frameForEvidence.size() == bboxForEvidence.size();
					assert bboxForEvidence.size() == correlogramForEvidence.size();
					assert correlogramForEvidence.size() == similarityToCurrentForEvidence.size();
					assert similarityToCurrentForEvidence.size() == getEvidenceCount();
					
					// these will be filled in by the background thread, but check to see if values exist
					EvidenceRow newEvidence = (EvidenceRow) getEvidence(getEvidenceCount() - 1);
					ImageSlice id = ImageSlice.createImageSlice(mediator
							.getCurrFileName(), f.intValue(), bbox);
					newEvidence.verify();
					correlogramForEvidence.set(newEvidence.row, correlogramCache.get(id));
					similarityToCurrentForEvidence.set(newEvidence.row, similarityToReferenceCgrams(newEvidence));

					assert entityForEvidence.size() == frameForEvidence.size();
					assert frameForEvidence.size() == bboxForEvidence.size();
					assert bboxForEvidence.size() == correlogramForEvidence.size();
					assert correlogramForEvidence.size() == similarityToCurrentForEvidence.size();

					GetSliceFunctionObject gp = new GetSliceFunctionObject(
							this, id, null, null);
					allIds.add(id);

					Dimension prefDim = new Dimension(bbox.getWidth(), bbox
							.getHeight());
					imageArchive.put(id, gp, prefDim, null);
				}
			}
		}

		ImageSlice.retain(allIds);
		
		fireChangeEvent();
	}
	
	public GalleryEntity addEntity(String name, InstantRange range) {
		int id = -1;
		TransactionalNode n = (TransactionalNode) mediator.getCurrFile();
		Transaction f = n.begin(UNDO_ADD_ENTITY);
		try {
			Descriptor d = mediator.getCurrFile().createDescriptor(getPersonConfig());
			if (range != null) {
				d.setValidRange(range);
			}
			if (name != null) {
				d.getAttribute("Name").setAttrValue(name);
			}
			f.commit();
			return getEntity(d);
		} catch (RuntimeException rx) {
			log.log(Level.SEVERE, "Error while creating a new entity (" + name + ")", rx);
			if (f.isAlive()) {
				f.rollback();
			}
			return null;
		}
	}
	
	public GalleryEvidence addEvidence(GalleryEntity entity, int frame, BoundingBox box) {
		Descriptor d = ((EntityRow) entity).getDescriptor();
		d.getAttribute("Location").setAttrValueAtSpan(box, new Span(new Frame(frame), new Frame(frame + 1)));
		return entity.getEvidenceAtFrame(frame);
	}
	

	/**
	 * 
	 */
	void helpApplyEvidenceSortCommands() {
		if (evidenceToChangeCommands.isEmpty()) {
			return;
		}
		int start = 0;
		GalleryEvidence curr = (GalleryEvidence) evidenceToChangeCommands.get(0);
		if (null == curr) {
			start = 1;
			evidenceForSimilarity = new GalleryEvidence[evidenceToChangeCommands.size() - 1];
		} else {
			for (int i = 0; i < evidenceForSimilarity.length; i++) {
				evidenceToChangeCommands.add(evidenceForSimilarity[i]);
			}
			evidenceForSimilarity = new GalleryEvidence[evidenceToChangeCommands.size()];
		}
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < evidenceForSimilarity.length; i++) {
			curr = (GalleryEvidence) evidenceToChangeCommands.get(start++);
			evidenceForSimilarity[i] = curr;
			sb.append(curr.getEntity().getId()).append('@').append(curr.getFrame()).append(' ');
		}
		referenceCgrams = null;
		applyEvidenceSimilaritySort();
	}
	
	public String getSortedEntityString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < evidenceForSimilarity.length; i++) {
			GalleryEvidence curr = (GalleryEvidence) evidenceToChangeCommands.get(i);
			evidenceForSimilarity[i] = curr;
			sb.append(curr.getEntity()).append('@').append(curr.getFrame()).append(' ');
		}
		return sb.toString();
	}
	
	 public void addChangeListener(ChangeListener l) {
	     listenerList.add(ChangeListener.class, l);
	 }

	 public void removeChangeListener(ChangeListener l) {
	     listenerList.remove(ChangeListener.class, l);
	 }


	 protected void fireChangeEvent() {
	     // Guaranteed to return a non-null array
	     Object[] listeners = listenerList.getListenerList();
	     // Process the listeners last to first, notifying
	     // those that are interested in this event
	     ChangeEvent changeEvent = null;
	     for (int i = listeners.length-2; i>=0; i-=2) {
	         if (listeners[i]==ChangeListener.class) {
	             // Lazily create the event:
	             if (changeEvent == null)
	                 changeEvent = new ChangeEvent(this);
	             ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
	         }
	     }
	 }
	/**
	 * 
	 */
	private void applyEvidenceSimilaritySort() {
		computeSimilarityToEvidence(evidenceForSimilarity);
		fireChangeEvent();
	}
	
	private Descriptor getSelectedDescriptor() {
		ViperSelectionSetWithPrimarySelection v = (ViperSelectionSetWithPrimarySelection) mediator.getSelection();
		if (v.getPrimary().isFilteredBy(Descriptor.class)) {
			return (Descriptor) v.getPrimary().getDescriptors().next();
		}
		return null;
	}
	public ViperViewMediator getMediator() {
		return mediator;
	}

	private ChangeListener personSelectionListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			fireChangeEvent();
		}
	};


	private ViperMediatorChangeListener vmcl = new ViperMediatorChangeListener() {
		public void schemaChanged(ViperMediatorChangeEvent e) {
			extractPeople();
			fireChangeEvent();
		}

		public void currFileChanged(ViperMediatorChangeEvent e) {
			extractPeople();
			fireChangeEvent();
		}

		public void mediaChanged(ViperMediatorChangeEvent e) {
			extractPeople();
			fireChangeEvent();
		}

		public void frameChanged(ViperMediatorChangeEvent e) {
		}

		public void dataChanged(ViperMediatorChangeEvent e) {
			extractPeople();
			fireChangeEvent();
		}
	};

	public void setMediator(ViperViewMediator mediator) {
		if (this.mediator != null) {
			this.mediator.removeViperMediatorChangeListener(vmcl);
			this.mediator.getSelection().removeChangeListener(personSelectionListener);
		}
		this.mediator = mediator;
		extractPeople();
		if (this.mediator != null) {
			this.mediator.addViperMediatorChangeListener(vmcl);
			this.mediator.getSelection().addChangeListener(personSelectionListener);
		}
	}

	/**
	 * Replaces the current list of blobs for similarity calculation
	 * with the given blob.
	 * @param evidence the blob to use for correlogram
	 * distance calculations
	 */
	public void setEvidenceForSimilarity(GalleryEvidence evidence) {
		evidenceToChangeCommands.clear();
		evidenceToChangeCommands.add(null);
		evidenceToChangeCommands.add(evidence);
		helpApplyEvidenceSortCommands();
	}

	/**
	 * Adds the given piece of evidence to the list of 
	 * elements in the similarity set. Currently, the similarity
	 * measure is the greatest similarity a blob has to any blob
	 * in this 'evidence for similarity' list.
	 * @param evidence the blob to use for correlogram
	 * distance calculations
	 */
	public void addEvidenceForSimilarity(GalleryEvidence evidence) {
		if (evidence == null) {
			evidenceToChangeCommands.clear();
		}
		evidenceToChangeCommands.add(evidence);
		helpApplyEvidenceSortCommands();
	}

	/**
	 * Gets the frame span of the current video as a Span object.
	 * Videos frames are 1-indexed. To get the frame count,
	 * call <code>getIntervalForVideo().width()</code>.
	 * @return the interval of the video in Frames
	 */
	public InstantInterval getIntervalForVideo() {
		return getMediator().getFocusInterval();
	}

	/**
	 * 
	 * @return
	 */
	public int getSelectedFrame() {
		return mediator.getCurrentFrame().intValue();
	}

	public FrameRate getFrameRate() {
		return mediator.getCurrFile().getReferenceMedia().getFrameRate();
	}

	public void setSelectedFrame(int majorMoment) {
		mediator.setMajorMoment(new Frame(majorMoment));
	}
	
	public void setSelectedEntity(GalleryEntity g) {
		mediator.getSelection().setTo(((EntityRow) g).getDescriptor().getAttribute("Location"));
	}

	public void setSelectedEvidence(GalleryEvidence g) {
		setSelectedFrame(g.getFrame());
		mediator.getSelection().setTo(((EntityRow) g.getEntity()).getDescriptor().getAttribute("Location"));
	}
	
	public GalleryEntity getSelectedEntity() {
		Descriptor d = getSelectedDescriptor();
		if (d == null || d.getRoot() == null || !d.getConfig().equals(getPersonConfig())) {
			// selected descriptor is not a person
			return null;
		}
		return getEntity(d);
	}
	
	public GalleryEvidence getSelectedEvidence() {
		int f = getSelectedFrame();
		GalleryEntity e = getSelectedEntity();
		if (e == null) {
			return null;
		}
		return e.getEvidenceAtFrame(f);
	}

	public ImageArchive getImageArchive() {
		return imageArchive;
	}
	
	public void addEvidencesFor(GalleryEntity entity){
		
		if(LastSelectedEntity!=null) {
			EntityRow er = (EntityRow) LastSelectedEntity;
		
			er.verify();
			for (int i = 0; i < getEvidenceCount(); i++) {
				if (er.descId == entityForEvidence.get(i)) {
					deletePersonEvidence(getEvidence(i));
			}
			}

/*			Iterator evidences=getAllEvidences();
			while(evidences.hasNext()){
				GalleryEvidence e=(GalleryEvidence)evidences.next();
				if(e.getEntity()==LastSelectedEntity)
				
			}
*/			}
		
		LastSelectedEntity=entity;
	//	return null;
		
		
		String name=String.valueOf(entity.getId());//.getDescriptor().getDescName();
	
    	
    	String InputFileName=name+"_track.txt";
        BufferedReader infile;
    	try    {
    		 infile = new BufferedReader(new FileReader(InputFileName));
    	
             while(infile.ready()) {
                   String InputString=infile.readLine();
                   StringTokenizer st = new StringTokenizer(InputString); 
                   while(st.hasMoreTokens()){
                        int frame = Integer.parseInt(st.nextToken()); 
                        int xmin= Integer.parseInt(st.nextToken()); 
                        int ymin= Integer.parseInt(st.nextToken()); 
                        int width = Integer.parseInt(st.nextToken()); 
                        int height= Integer.parseInt(st.nextToken());
                        
                        BoundingBox R2=new BoundingBox(xmin,ymin,width, height);
                        
                       addEvidence(entity,frame,R2);
                   }
             }
    	}
    	catch(Exception E){}
	
	}
	
	private GalleryEntity LastSelectedEntity=null;
	
	
	
	/**
	 * Fills up an image table with evidence from this gallery using the given options.
	 * @param imageTable 
	 * @param groupByPerson
	 * @param filter
	 * @param evidenceOrder TODO
	 */
	public void addEvidenceToImageTable(ScrollableImageTable imageTable, boolean groupByPerson, Predicate filter, Comparator evidenceOrder) {
		Set entitiesSeen = new HashSet();
		Iterator evRowIterator = getEvidenceInOrder(EvidenceAndEntityComparisons.EVIDENCE_BY_SIMILARITY);
		if (filter != null) {
			evRowIterator = new FilterIterator(evRowIterator, filter);
		}
		while (evRowIterator.hasNext()) {
			GalleryEvidence ev = (GalleryEvidence)evRowIterator.next();
			
			// Get information about the current evidence
			BoundingBox bbox = ev.getBox();
			GalleryEntity entity = ev.getEntity();
			if (groupByPerson) {
				if (entitiesSeen.contains(entity)) {
					continue;
				} else {
					entitiesSeen.add(entity);
				}
			}
			int frame = ev.getFrame();

			// calculate the size to make the photo tile for the image
			Dimension prefSize = SmartImageUtilities.smartResize(bbox
					.getWidth(), bbox.getHeight(), (int) imageTable.getLayer().getImageWidth(), (int) imageTable.getLayer().getImageHeight());

			// construct the photo tile object and add it
			imageTable.addImage(makePhotoTile(ev, imageTable.getCamera(), prefSize,
					imageTable.getLayer().getBorder(), imageTable.getLayer().getImageWidth(), imageTable.getLayer().getImageHeight()));
		}
	}

	/**
	 * @param refCamera
	 * @param parent
	 * @param bbox
	 * @param entity
	 * @param frame
	 * @param prefRect
	 */
	PVideoAnnotationItem makePhotoTile(GalleryEvidence ev, PCamera refCamera, Dimension2D prefRect, double innerMargin, double tileW, double tileH) {
		GalleryEntity entity = ev.getEntity();
		int frame = ev.getFrame();

		PImageArchiveNode currImage = new PImageArchiveNode(getImageArchive());
		if (ObjectUtils.equals(getSelectedEvidence(), ev)) {
			currImage.setBackgroundColor(Color.red);
		} else {
			currImage.setBackgroundColor(Color.lightGray);
		}
		currImage.setBounds(0, 0, prefRect.getWidth(), prefRect.getHeight());
		GetSliceFunctionObject f = new GetSliceFunctionObject(this, ev.getSlice(), currImage, refCamera);
		currImage.setImageFunction(f);
		currImage.setKey(f.getSliceId());
		addToArchive(ev.getSlice(), f, prefRect);

		PTextLabel nameLabel = new PTextLabel(entity.getId() + " @ " + frame);
		nameLabel.setTransparency(.75f);

		PVideoAnnotationItem currTile = new PVideoAnnotationItem();
		currTile.addClientProperty("entity", entity);
		currTile.addClientProperty("evidence", ev);
		int similarity = ev.getSimilarity();
		similarity = 255 * similarity / 100;
		currTile.setBackgroundPaint(new Color(similarity, similarity,
				similarity));
		if (ObjectUtils.equals(getSelectedEvidence(), ev)) {
			currTile.setBoundaryPaint(Color.red);
			currTile.setBoundaryStroke(new BasicStroke(3));
		}
		currTile.setLabelSize(nameLabel.getHeight() + 1);
		currTile.setImageHeight(tileH);
		currTile.setImageWidth(tileW);
		currTile.setMargin(innerMargin);
		currTile.setImage(currImage);
		currTile.setName(nameLabel);
		//currTile.setSparkline(makePresenceSparkline(id.getFrame(), model.getVideoFrameCount()));

		return currTile;
	}
}
