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

import java.awt.*;
import java.awt.Component;
import java.awt.dnd.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.logging.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import viper.api.time.*;
import viper.api.time.Frame;
import edu.umd.cfar.lamp.chronicle.*;
import edu.umd.cfar.lamp.chronicle.markers.*;
import edu.umd.cfar.lamp.viper.examples.persontracking.images.*;
import edu.umd.cfar.lamp.viper.geometry.*;
import edu.umd.cfar.lamp.viper.gui.canvas.*;
import edu.umd.cfar.lamp.viper.gui.core.*;
import edu.umd.cfar.lamp.viper.util.*;
import edu.umd.cs.piccolo.*;
import edu.umd.cs.piccolo.event.*;
import edu.umd.cs.piccolo.nodes.*;
import edu.umd.cs.piccolo.util.*;
import edu.umd.cs.piccolox.swing.*;


/**
 * Displays people marked up in the video.
 * 
 * @author davidm
 */
public class PersonGallery extends JPanel {

//	private static final String TRACKER_OUTPUT_ROOT = "e:\\Nagia\\Gallery_Table\\";
//	private static final String TRACKER_BGS_IMAGES_ROOT = "E:\\Nagia\\sequences\\2-cameras\\camera1\\";
//	private static final String TRACKER_OUTPUT_ROOT = "C:/Documents and Settings/davidm/My Documents/My Videos/";
//	private static final String TRACKER_BGS_IMAGES_ROOT = "C:/Documents and Settings/davidm/My Documents/My Videos/camera1/";
	private static final String TRACKER_OUTPUT_ROOT = "/Users/davidm/Movies/";
	private static final String TRACKER_BGS_IMAGES_ROOT = "/Users/davidm/Movies/camera1/";
	private static final Logger log = Logger
			.getLogger("edu.umd.cfar.lamp.viper.examples.persontracking");
	private static final int MAX_TILE_SIZE = 128;

	private static final int MIN_TILE_SIZE = 32;

	private PersonSelector personSelector;

	private JPopupMenu personMenu;
	
	private JCheckBox groupByPersonCheckBox;

	private PVideoAnnotationItem underClick;
	
	private PersonGalleryModel model;
	
	private Tracker MyTracker;

	private Action selectItem = new AbstractAction("Select") {
		public void actionPerformed(ActionEvent e) {
			if (underClick == null) {
				return;
			}
			GalleryEvidence ev = (GalleryEvidence) underClick.getAttribute("evidence");
			model.selectPersonAtFrame(ev.getEntity(), ev.getFrame());
		}
	};

	private Action sortByItem = new AbstractAction("Sort By") {
		public void actionPerformed(ActionEvent e) {
			if (underClick == null) {
				return;
			}
			GalleryEvidence evidence = (GalleryEvidence) underClick.getAttribute("evidence");
			model.setEvidenceForSimilarity(evidence);
		}
	};

	private Action addToSortListItem = new AbstractAction("Add to Sort List") {
		public void actionPerformed(ActionEvent e) {
			if (underClick == null) {
				return;
			}
			GalleryEvidence evidence = (GalleryEvidence) underClick.getAttribute("evidence");
			model.addEvidenceForSimilarity(evidence);
		}
	};

	private Action deleteItem = new AbstractAction("Delete") {
		public void actionPerformed(ActionEvent e) {
			if (underClick == null) {
				return;
			}
			GalleryEvidence evidence = (GalleryEvidence) underClick.getAttribute("evidence");
			model.deletePersonEvidence(evidence);
		}
	};

	public boolean maybeShowPopup(PInputEvent event, PVideoAnnotationItem item) {
		if (!event.isPopupTrigger()) {
			return false;
		}
		if (null == personMenu) {
			personMenu = new JPopupMenu("Edit Person Annotation");
			personMenu.add(new JMenuItem(selectItem));
			personMenu.add(new JSeparator());
			personMenu.add(new JMenuItem(sortByItem));
			personMenu.add(new JMenuItem(addToSortListItem));
			personMenu.add(new JSeparator());
			personMenu.add(new JMenuItem(deleteItem));
		}
		underClick = item;
		Component whereComp = (Component) event.getComponent();
		int whereX = (int) event.getCanvasPosition().getX();
		int whereY = (int) event.getCanvasPosition().getY();
		personMenu.show(whereComp, whereX, whereY);
		return true;
	}


	private JToolBar toolbar;

	private JSlider tileSizeSlider;

	private JTextField entityForSort;

	private int[] entitiesForComparison;

	private int tileSize = 64;

	private void applySamplerPanel() {
		StringTokenizer st = new StringTokenizer(entityForSort.getText());
		StringBuffer valid = new StringBuffer();
		List tokens = new ArrayList();
		while (st.hasMoreTokens()) {
			String curr = st.nextToken();
			try {
				int colon = curr.indexOf(':');
				int entity = -1;
				int frame = -1;
				boolean hasColon = colon > 0;
				if (hasColon) {
					entity = Integer.parseInt(curr.substring(0, colon));
					if (colon + 1 < curr.length()) {
						frame = Integer.parseInt(curr.substring(colon + 1));
					}
				} else {
					entity = Integer.parseInt(curr);
				}
				GalleryEvidence evidence = model.getAnEvidenceForAnEntity(model.getEntity(entity), frame);
				if (null == evidence) {
					tokens.add(evidence);
					valid.append(' ').append(curr);
				} else if (!st.hasMoreTokens()) {
					valid.append(' ').append(curr);
				}
			} catch (NumberFormatException nfx) {
				// bad number
			}
		}
		entityForSort.setText(valid.toString());
		model.setEvidenceForSimilarity(tokens);
	}

	static final double QLAYOUT_MARGIN = 2;

	static final double QLAYOUT_BORDER = 4;

	static final int QLAYOUT_LABEL_HEIGHT = 16;

	private static PPath makePresenceSparkline(int frame, int frameCount, InstantRange validRange, double w, double h, Paint color) {
		PPath r = new PPath();
		r.setPaint(color);
		
		double y = h/2;
		Iterator ints = validRange.iterator();
		while (ints.hasNext()) {
			InstantInterval curr = (InstantInterval) ints.next();
			double x0 = (curr.getStartInstant().doubleValue() - 1) * w / frameCount;
			double x1 = (curr.getEndInstant().doubleValue() - 1) * w / frameCount;
			r.getPathReference().append(new Line2D.Double(x0, y, x1, y).getPathIterator(null), false);
		}
		double xF = (frame - .5) * w / frameCount;
		r.getPathReference().append(new Line2D.Double(xF, 0, xF, h).getPathIterator(null), false);
		return r;
	}
	
	private final class PersonDetailsSelectionChangeListener implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			resetDetailsView();
		}
	}

	private final class AllPeopleTimeline implements TimeLine {
		public String getPluralName() {
			return "People";
		}

		public String getSingularName() {
			return "Person";
		}

		public int getNumberOfChildren() {
			return 0;
		}

		public Iterator getChildren() {
			return Collections.EMPTY_SET.iterator();
		}

		public String getTitle() {
			return "Personages Found";
		}

		public TemporalRange getMyRange() {
			return Intervals.singletonRange(model.getIntervalForVideo());
		}
	}
	
	final class SinglePersonTimeLine implements TimeLine {
		public final GalleryEntity entity;

		private SinglePersonTimeLine(GalleryEntity entity) {
			super();
			this.entity = entity;
		}

		public String getPluralName() {
			return getSingularName() + "s";
		}

		public String getSingularName() {
			return entity.getName();
		}

		public int getNumberOfChildren() {
			return 0;
		}

		public Iterator getChildren() {
			return Collections.EMPTY_SET.iterator();
		}

		public String getTitle() {
			String name = entity.getName();
			if (name != null) {
				return name;
			}
			return "Person " + entity.getId();
		}

		public TemporalRange getMyRange() {
			return entity.getRange();
		}
	}

	private final class PeopleByPersonTimeline implements TimeLine {
		private List children;
		public String getPluralName() {
			return "People";
		}

		public String getSingularName() {
			return "Person";
		}

		public int getNumberOfChildren() {
			return model.getEntityCount();
		}

		public Iterator getChildren() {
			if (null == children) {
				children = new ArrayList();
				int stop = getNumberOfChildren();
				for (int i = 0; i < stop; i++) {
					children.add(new SinglePersonTimeLine(model.getEntity(i)));
				}
			}
			return children.iterator();
		}

		public String getTitle() {
			return "Personages Found";
		}

		public TemporalRange getMyRange() {
			return Intervals.singletonRange(model.getIntervalForVideo());
		}
	}

	private class TimeCorrelationLayoutNode extends PNode {
		private long numFrames;
		
		
		/**
		 * @param frames
		 */
		public TimeCorrelationLayoutNode(long frames) {
			super();
			numFrames = frames;
		}

		protected void layoutChildren() {
			ArrayList added = new ArrayList(getChildrenCount());
			Iterator i = getChildrenIterator();
			while (i.hasNext()) {
				PNode each = (PNode) i.next();
				
				GalleryEvidence evidence = (GalleryEvidence) each.getClientProperty("evidence");
				int frame = evidence.getFrame();
				double x = (double) frame * (getWidth() / numFrames);
				double x0 = x - each.getBoundsReference().getWidth() / 2;
				double y0 = spread * tileSize * (100 - evidence.getSimilarity()) / 100;
				each.setOffset(x0, y0);
				
				boolean addIt = true;
				PBounds eachBounds = each.getGlobalBounds();
				Iterator existing = added.iterator();
				while (existing.hasNext()) {
					PNode nextNode = (PNode) existing.next();
					Rectangle2D nextBounds = nextNode.getGlobalBounds();
					if (nextNode.getVisible() && nextBounds.intersects(eachBounds)) {
						addIt = false;
						break;
					}
				}
				if (addIt) {
					added.add(each);
				}
				each.setVisible(addIt);
			}
		}

		protected void paint(PPaintContext paintContext) {
			super.paint(paintContext);
		}
	}

	PNode splayout(double width, long numFrames, PCamera refCamera, GalleryEntity onlyEntity) {
		if (!model.isLoaded()) {
			return new PNode();
		}
		if (width <= 0) {
			width = numFrames;
		}
		TimeCorrelationLayoutNode n = new TimeCorrelationLayoutNode(numFrames);
		int lastSimilarity = 100;
		double lastY = 0;
		Iterator evRowIterator = model.getEvidenceInOrder(EvidenceAndEntityComparisons.EVIDENCE_BY_SIMILARITY);
		while (evRowIterator.hasNext()) {
			GalleryEvidence row = (GalleryEvidence) evRowIterator.next();
			GalleryEntity entity = row.getEntity();
			if (null != onlyEntity && !onlyEntity.equals(entity)) {
				continue;
			}
			int frame = row.getFrame();
			int similarity = row.getSimilarity();
			BoundingBox bbox = row.getBox();

			// calculate the size to make the photo tile for the image
			int smartSize = tileSize;//tileSize*(100 + similarity) / 200;
			Dimension prefSize = SmartImageUtilities.smartResize(bbox
					.getWidth(), bbox.getHeight(), smartSize);

			n.addChild(model.makePhotoTile(row, refCamera, prefSize, 1, smartSize, smartSize));
			lastSimilarity = similarity;
		}
		n.setBounds(0, 0, width, tileSize + tileSize * spread);
		n.addInputEventListener(this.listenForClicks);
		return n;
	}
	
	private int spread = 0;
	
	private PersonDetailsView selectedPersonDetailsView;

	private void resetImageTable() {
		if (null == imageTableSP) {
			return;
		}
		imageTableSP.clearTable();
		imageTableSP.setTileSize(getTileSize());
		if (model.isLoaded()) {
			model.addEvidenceToImageTable(imageTableSP, groupingByPerson, null, EvidenceAndEntityComparisons.EVIDENCE_BY_SIMILARITY);
		}
		// parent.setBounds(0, 0, 128, parent.getFullBoundsReference().height);
		imageTableSP.getLayer().removeInputEventListener(this.listenForClicks);
		imageTableSP.getLayer().addInputEventListener(this.listenForClicks);
	}

	private ScrollableImageTable imageTableSP = null;

	public PScrollPane getImageTable() {
		if (imageTableSP == null) {
			imageTableSP = new ScrollableImageTable();
			resetImageTable();
		}
		return imageTableSP;
	}

	public PersonGallery() {
		super(new BorderLayout());
		model = new PersonGalleryModel();
		model.addChangeListener(modelChangeListener);

		entityForSort = new JTextField();
		entityForSort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applySamplerPanel();
			}
		});

		tileSizeSlider = new JSlider(MIN_TILE_SIZE, MAX_TILE_SIZE, tileSize);
		tileSizeSlider.getModel().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setTileSize(tileSizeSlider.getValue());
			}
		});

		createButton = new JButton("New Person");
		createButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				model.setSelectedEntity(model.addEntity(null, null));
			}
		});
		
		toolbar = new JToolBar("Person Tracking Toolbar");
		toolbar.add(tileSizeSlider);
		toolbar.add(entityForSort);
		toolbar.add(createButton);

		super.add(toolbar, BorderLayout.PAGE_START);
		super.add(getImageTable(), BorderLayout.CENTER);
		
		final String imageRootPath = TRACKER_BGS_IMAGES_ROOT;
		final String trackTablePath = TRACKER_OUTPUT_ROOT;
		MyTracker=new Tracker(imageRootPath + "JPG\\camera1-",
				imageRootPath + "BGS_JPG\\binary.camera1-",
				imageRootPath + "blobs\\blobs.camera1-", trackTablePath + "track_");

	}

	private ChangeListener modelChangeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			resetChronicleModel();
			resetImageTable();
			resetDetailsView();
		}
	};
	

	public TimeLineRenderer getRenderer() {
		return new StoryTimeLineRenderer(this);
	}

	private PScrollPane chronicleScrollPane;

	ChronicleViewer chronicle;

	private DefaultChronicleViewModel chronicleViewModel;

	private DefaultChronicleDataModel chronicleData;

	private static final int WAIT_FOR_RESORT = 500;

	long applyEvidenceSortCommands = 0;

	ActionListener applyEvidence = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if (applyEvidenceSortCommands < System.currentTimeMillis()) {
				model.helpApplyEvidenceSortCommands();
				entityForSort.setText(model.getSortedEntityString());
			}
		}
	};

	private PInputEventListener listenForClicks = new VideoAnnotationEventListener(
			this, WAIT_FOR_RESORT);

	private static final Color bgColor = Color.black;
	
	public ChronicleViewer getChronicle() {
		if (chronicle == null) {
			chronicle = new ChronicleViewer();
			chronicle.getRuler().setBackgroundColor(bgColor);
			chronicle.getRuler().setLineColorTable(new Color[] {Color.gray});
			((DefaultRendererCatalogue) chronicle.getRendererCatalogue())
					.setDefaultTimeLineRenderer(getRenderer());
			resetChronicleModel();
		}
		return chronicle;
	}
	private ActionListener fitChronicleAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			getChronicle().fitInWindow();
		}
	};
	private ActionListener zoomInChronicleAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			double zoomAmount = getChronicle().getZoomAmount();
			Instant where = getChronicle().getModel().getMajorMoment();
			getChronicle().setZoom(zoomAmount * 2, where);
		}
	};
	private ActionListener zoomOutChronicleAction = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			double zoomAmount = getChronicle().getZoomAmount();
			Instant where = getChronicle().getModel().getMajorMoment();
			getChronicle().setZoom(zoomAmount / 2, where);
		}
	};

	public PScrollPane getChronicleScrollPane() {
		if (chronicleScrollPane == null) {
			ChronicleViewer.ScrollViews sv = getChronicle().getScrollViews();
			sv.content.setBackground(bgColor);
			sv.rowHeader.setBackground(bgColor);
			sv.cornerHeader.setBackground(bgColor);
			sv.columnHeader.setBackground(bgColor);
			chronicleScrollPane = new PScrollPane();
			chronicleScrollPane.setViewportView(sv.content);
			chronicleScrollPane.setWheelScrollingEnabled(false);
			chronicleScrollPane.setRowHeaderView(sv.rowHeader);
			chronicleScrollPane.setColumnHeaderView(sv.columnHeader);
			chronicleScrollPane.setCorner(
					ScrollPaneConstants.UPPER_LEFT_CORNER, sv.cornerHeader);
			chronicleScrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		}
		return chronicleScrollPane;
	}

	/**
	 * 
	 */
	private void resetChronicleModel() {
		if (chronicle == null) {
			return;
		}
		if (!model.upToDate()) {
			model.extractPeople();
		}
		chronicleData = new DefaultChronicleDataModel();
		if (groupingByPerson) {
			for (int i = 0; i < model.getEntityCount(); i++) {
				chronicleData.add(new SinglePersonTimeLine(model.getEntity(i)));
			}
		} else {
			chronicleData.add(new AllPeopleTimeline());
		}

		if (model.isLoaded()) {
			chronicleViewModel = new DefaultChronicleViewModel(chronicleData,
					new Frame(model.getSelectedFrame()), model.getIntervalForVideo(),
					model.getFrameRate());
		} else {
			chronicleViewModel = new DefaultChronicleViewModel();
		}
		chronicle.getMarkerModel().addChronicleMarkerListener(
				new ChronicleMarkerListener() {
					public void markersChanged(ChronicleMarkerEvent e) {
						if (model.isLoaded()) {
							model.setSelectedFrame(chronicleViewModel
									.getMajorMoment().intValue());
						}
					}
				});
		chronicle.setModel(chronicleViewModel);
	}

	public int getTileSize() {
		return tileSize;
	}

	public void setTileSize(int tileSize) {
		if (this.tileSize != tileSize) {
			this.tileSize = tileSize;
			resetChronicleModel();
			resetImageTable();
		}
	}


	public PersonSelector getPersonSelector() {
		return personSelector;
	}

	public void setPersonSelector(PersonSelector personSelector) {
		if (this.personSelector == personSelector) {
			return;
		}
		this.personSelector = personSelector;
	}

	private JButton createButton;

	private DragSource dragSource;

	private DragGestureListener dgListener;

	private DragSourceListener dsListener;
	
	public JPanel getPersonDetailsView() {
		if (null == selectedPersonDetailsView) {
			selectedPersonDetailsView = new PersonDetailsView(this);
			resetDetailsView();
		}
		return selectedPersonDetailsView;
	}
	
//	private class PersonAnnotationDragGestureListener implements DragGestureListener {
//		public void dragGestureRecognized(DragGestureEvent dge) {
//			try {
//				
//				VideoRepository r = videoPanel.getVideoRepository();
//				Transferable transferable = new CameraTransferable(r, feed, r
//						.getController().getNow());
//				dge.startDrag(DragSource.DefaultCopyNoDrop, transferable,
//						dsListener);
//			} catch (InvalidDnDOperationException idndox) {
//				idndox.printStackTrace();
//			}
//		}
//	}

	private class PersonAnnotationDragSourceListener extends DragSourceAdapter {
		public void dragEnter(DragSourceDragEvent e) {
			DragSourceContext context = e.getDragSourceContext();
			int myAction = e.getDropAction();
			if ((myAction & DnDConstants.ACTION_COPY) != 0) {
				context.setCursor(DragSource.DefaultCopyDrop);
			} else {
				context.setCursor(DragSource.DefaultCopyNoDrop);
			}
		}

		public void dragDropEnd(DragSourceDropEvent e) {
			if (!e.getDropSuccess()) {
				return; // failure
			}
			int dropAction = e.getDropAction();
			if (dropAction == DnDConstants.ACTION_MOVE) {
				//remove feed 'feed'
			}
		}
	}

	public JCheckBox getGroupByPersonCheckBox() {
		if (null == groupByPersonCheckBox) {
			groupByPersonCheckBox = new JCheckBox("Group by Person", isGroupingByPerson());
			groupByPersonCheckBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setGroupingByPerson(groupByPersonCheckBox.getModel().isSelected());
				}
			});
		}
		return groupByPersonCheckBox;
	}

	
	
	private void resetDetailsView() {
		if (!model.isLoaded() || selectedPersonDetailsView == null) {
			return;
		}
		GalleryEntity entity = model.getSelectedEntity();
		selectedPersonDetailsView.setEntity(entity);
		selectedPersonDetailsView.resetView();
	}

	public PersonGalleryModel getModel() {
		return model;
	}
    
	public ViperViewMediator getMediator() {
		return model.getMediator();
	}

	private boolean trackingEnabled = false;
	private boolean displayThumbsInTimeline = true;
	private boolean similarityEnabled = false;
	private boolean groupingByPerson = true;
	
	
	public void setMediator(ViperViewMediator mediator) {
		model.setMediator(mediator);
	}

	public ActionListener getFitChronicleAction() {
		return fitChronicleAction;
	}

	public ActionListener getZoomInChronicleAction() {
		return zoomInChronicleAction;
	}

	public ActionListener getZoomOutChronicleAction() {
		return zoomOutChronicleAction;
	}
	
	private PersonGalleryPreferences prefPane;
	private PBasicInputEventHandler startTrackingInputEventHandler = new PBasicInputEventHandler() {
		public void mouseClicked(PInputEvent event){
			Point2D clickPoint=event.getPosition();
			int currentFrame = model.getSelectedFrame();
			BoundingBox box=MyTracker.GetBlob(currentFrame,clickPoint.getX(),clickPoint.getY());
			if(box!=null){
				GalleryObject obj=MyTracker.CreateObjectFromBlob(currentFrame,new Rectangle( box.getX(),box.getY(),box.getWidth(),box.getHeight()));
			    MyTracker.SetObjectToTrack(obj);
			//    InstantRange r=new InstantRange();
			    //Frame i=new Frame(currentFrame);
			    //Frame j=new Frame(currentFrame+1);
			    //r.add(i,j);
			    //GalleryEntity person=model.addEntity(null,r);
			    Vector ForwardPath=MyTracker.GetTrack(currentFrame,5,box);
			    Vector BackwardPath=MyTracker.GetTrack(currentFrame,-5,box);
  	            //MyTracker.ShowTrack(null, null, ForwardPath, 0, Color.GREEN);
			   // MyTracker.ShowTrack(null, null, BackwardPath, 0, Color.RED);
				int trackStart=MyTracker.GetFirstFrameNo(BackwardPath);
				int trackEnd=MyTracker.GetLastFrameNo(ForwardPath);
			
				InstantRange r=new InstantRange();
				r.add(new Frame(trackStart),new Frame(trackEnd+1));
				GalleryEntity person=model.getSelectedEntity();
				if(person==null) person=model.addEntity(null,r);
				else{
					InstantRange range = (InstantRange) person.getRange();
					range.addAll((IntervalIndexList) r);
					person.setValidRange(range);
				}
				GalleryEvidence blob=model.addEvidence(person,currentFrame,box);
				model.setSelectedEvidence(blob);
				MyTracker.MergeTracks(BackwardPath,currentFrame,box,ForwardPath,person);
			//	MyTracker.AddEvidences(ForwardPath,model,person);
			//	MyTracker.AddEvidences(BackwardPath,model,person);
				//person.getDescriptor().g
			}
		}
	};
	public PersonGalleryPreferences getPrefPane() {
		if (prefPane == null) {
			prefPane = new PersonGalleryPreferences();
			prefPane.setGallery(this);
		}
		return prefPane;
	}

	public boolean isDisplayThumbsInTimeline() {
		return displayThumbsInTimeline;
	}

	public void setDisplayThumbsInTimeline(boolean displayThumbsInTimeline) {
		if (this.displayThumbsInTimeline != displayThumbsInTimeline) {
			this.displayThumbsInTimeline = displayThumbsInTimeline;
			chronicle.resetLineLayer();
		}
	}

	public boolean isGroupingByPerson() {
		return groupingByPerson;
	}

	public void setGroupingByPerson(boolean groupingByPerson) {
		if (this.groupingByPerson != groupingByPerson) {
			this.groupingByPerson = groupingByPerson;
			if (chronicleViewModel != null) {
				resetChronicleModel();
			}
			if (imageTableSP != null) {
				resetImageTable();
			}
			if (groupByPersonCheckBox!= null) {
				groupByPersonCheckBox.getModel().setSelected(groupingByPerson);
			}
		}
	}

	public boolean isSimilarityEnabled() {
		return similarityEnabled;
	}

	public void setSimilarityEnabled(boolean similarityEnabled) {
		if (this.similarityEnabled != similarityEnabled) {
			this.similarityEnabled = similarityEnabled;
			// TODO disable/enable similarity
		}
	}

	public boolean isTrackingEnabled() {
		return trackingEnabled;
	}

	public void setTrackingEnabled(boolean trackingEnabled) {
		if (this.trackingEnabled != trackingEnabled) {
			this.trackingEnabled = trackingEnabled;
			helpResetTrackingEnabled();
		}
	}

	private void helpResetTrackingEnabled() {
		if (getMediator() == null) {
			return;
		}
		ScrollableViperDataCanvas cScrollPane = (ScrollableViperDataCanvas)model.getMediator().getPrefs().getCore().getBean("#canvas");
		ViperDataCanvas c=cScrollPane.getInterior();
		if (trackingEnabled) {
			c.setDataInputListener(startTrackingInputEventHandler);
		} else {
			c.setDataInputListener(c.getDefaultDataInputListener());
		}
	}
}
