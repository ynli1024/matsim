package playground.ciarif.retailers.utils;

import org.matsim.api.basic.v01.Coord;
import org.matsim.core.basic.v01.network.BasicLinkImpl;
import org.matsim.core.facilities.ActivityFacility;
import org.matsim.core.population.PersonImpl;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.world.World;

public abstract class Utils {
	
	
	private final static double EPSILON = 0.0001;
	public static final void moveFacility(ActivityFacility f, BasicLinkImpl link, World world) {
		double [] vector = new double[2];
		vector[0] = link.getToNode().getCoord().getY()-link.getFromNode().getCoord().getY();
		vector[1] = -(link.getToNode().getCoord().getX()-link.getFromNode().getCoord().getX());
//		double length = Math.sqrt(vector[0]*vector[0]+vector[1]*vector[1]);
//		System.out.println("length = " + length);
		Coord coord = new CoordImpl(link.getCoord().getX()+vector[0]*EPSILON,link.getCoord().getY()+vector[1]*EPSILON);
		f.moveTo(coord);

		BasicLinkImpl oldL = (BasicLinkImpl)f.getLink();
		if (oldL != null) {
			world.removeMapping(f, oldL);
		}
		world.addMapping(f, link);

	}
	
	// BAD CODE STYLE but keep that anyway for the moment
	private static QuadTree<PersonImpl> personQuadTree = null;
	private static QuadTree<ActivityFacility> facilityQuadTree = null;
	
	public static final void setPersonQuadTree(QuadTree<PersonImpl> personQuadTree) {
		Utils.personQuadTree = personQuadTree;
	}
	
	public static final QuadTree<PersonImpl> getPersonQuadTree() {
		return Utils.personQuadTree;
	}
	
	public static final void setFacilityQuadTree(QuadTree<ActivityFacility> facilityQuadTree) {
		Utils.facilityQuadTree  = facilityQuadTree;
	}
	
	public static final QuadTree<ActivityFacility> getFacilityQuadTree() {
		return Utils.facilityQuadTree;
	}
}
