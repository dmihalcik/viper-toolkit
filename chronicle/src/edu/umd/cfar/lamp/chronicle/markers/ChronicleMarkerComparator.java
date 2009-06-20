package edu.umd.cfar.lamp.chronicle.markers;

import java.util.Comparator;


public class ChronicleMarkerComparator implements Comparator {
	private boolean useLabel;

	public ChronicleMarkerComparator(boolean useLabel){
		this.useLabel = useLabel;
	}
	
	public int compare(Object l, Object r){
		ChronicleMarker left, right;
		if (!(l instanceof ChronicleMarker)
				|| !(r instanceof ChronicleMarker))
			throw new ClassCastException();
		left = (ChronicleMarker) l;
		right = (ChronicleMarker) r;

		if(useLabel)
			return left.getLabel().compareTo(right.getLabel());
		else
			return left.getWhen().compareTo(right.getWhen());
	}
}
