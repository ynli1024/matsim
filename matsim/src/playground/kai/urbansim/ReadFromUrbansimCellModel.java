/**
 * 
 */
package playground.kai.urbansim;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import org.apache.log4j.Logger;
import org.matsim.basic.v01.IdImpl;
import org.matsim.facilities.Facilities;
import org.matsim.interfaces.basic.v01.Coord;
import org.matsim.interfaces.basic.v01.Id;
import org.matsim.interfaces.core.v01.Facility;
import org.matsim.interfaces.core.v01.Person;
import org.matsim.interfaces.core.v01.Plan;
import org.matsim.interfaces.core.v01.Population;
import org.matsim.population.PersonImpl;
import org.matsim.utils.geometry.CoordImpl;
import org.matsim.utils.io.IOUtils;
import org.matsim.world.Layer;
import org.matsim.world.Location;

import playground.kai.urbansim.ids.HHId;
import playground.kai.urbansim.ids.JobIdFactory;
import playground.kai.urbansim.ids.LocationId;
import playground.kai.urbansim.ids.LocationIdFactory;

/**
 * This is meant to read urbansim input from the cell model.  Since in those models the persons don't know where they work,
 * this needs to be generated by matsim (destination choice).  For this, PseudoGravityModel was written, which deliberately does
 * NOT use any infrastructure that may already be in place (such as zones) but builds its own very simple basic structure.
 * 
 * Unfortunately, it does not work with the eugene example.  This may, however, be a problem with the difference in coordinate
 * systems between the (visum->emme derived) network file and the urbansim files rather than with the approach or the code.
 * 
 * It is left here because it _should_ work. :--)
 * 
 * @date dec 2008
 * 
 * @author nagel
 *
 */
@Deprecated
public class ReadFromUrbansimCellModel implements ReadFromUrbansim {
	private static final Logger log = Logger.getLogger(ReadFromUrbansimCellModel.class);

	public void readFacilities(Facilities facilities) {
		log.fatal("does not work; see javadoc of class.  Aborting ..." + this) ;
		System.exit(-1) ;
		
		// (these are simply defined as those entities that have x/y coordinates in urbansim)
		try {
			BufferedReader reader = IOUtils.getBufferedReader(Matsim4Urbansim.PATH_TO_OPUS_MATSIM+"tmp/gridcells.tab" ) ;

			String header = reader.readLine() ;
			Map<String,Integer> idxFromKey = Utils.createIdxFromKey( header ) ;

			String line = reader.readLine() ;
			while ( line != null ) {

				String[] parts = line.split("[\t]+");

				int idx_id = idxFromKey.get("grid_id:i4") ;
				Id id = new IdImpl( parts[idx_id] ) ;

				int idx_x = idxFromKey.get("relative_x:i4") ;
				int idx_y = idxFromKey.get("relative_y:i4") ;
				Coord coord = new CoordImpl( parts[idx_x], parts[idx_y] ) ;

				Facility facility = facilities.createFacility(id,coord) ;
				facility.setDesc("urbansim location") ;
				
				line = reader.readLine() ;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readPersons( Population population, Facilities facilities, double fraction) {
		log.fatal("does not work; see javadoc of class.  Aborting ..." + this) ;
		System.exit(-1) ;
		Map<Id,Id> gridcellFromJob = new HashMap<Id,Id>() ;
		Utils.readKV( gridcellFromJob, "job_id:i4", new JobIdFactory(), "grid_id:i4", new LocationIdFactory(), 
				"../opus/opus_matsim/tmp/jobs.tab" ) ;
		readPersonsFromHouseholds( population, facilities, fraction ) ;
		PseudoGravityModel gravMod = new PseudoGravityModel( population, facilities, gridcellFromJob ) ;
		gravMod.run();
	}

	public long personCnt = 0 ;
	void readPersonsFromHouseholds ( Population population, Facilities facilities, double fraction ) {
		log.fatal("does not work; see javadoc of class.  Aborting ..." + this) ;
		System.exit(-1) ;
		try {
			BufferedReader reader = IOUtils.getBufferedReader(Matsim4Urbansim.PATH_TO_OPUS_MATSIM+"tmp/households.tab");

			String header = reader.readLine();
			Map<String,Integer> idxFromKey = Utils.createIdxFromKey( header ) ;

			String line = reader.readLine();
			while (line != null) {
				String[] parts = line.split("[\t\n]+");

				if ( Math.random() > fraction ) {
					line = reader.readLine(); // next line
					continue ;
				}
					
				int idx = idxFromKey.get("persons:i4") ;
				int nPersons = Integer.parseInt( parts[idx] ) ;

				idx = idxFromKey.get("workers:i4") ;
				int nWorkers = Integer.parseInt( parts[idx] ) ;

				idx = idxFromKey.get("household_id:i4") ;
				HHId hhId = new HHId( parts[idx] ) ;

				idx = idxFromKey.get("grid_id:i4") ;
				LocationId homeGridId = new LocationId( parts[idx] ) ;
				Location homeLocation = facilities.getLocation( homeGridId ) ;
				if ( homeLocation==null ) {
					log.warn("no home location; hhId: " + hhId.toString() ) ;
					line = reader.readLine(); // next line
					continue ;
				}
				Coord homeCoord = homeLocation.getCenter() ;

				// generate persons only after it's clear that they have a home location:
				for ( int ii=0 ; ii<nPersons ; ii++ ) {
					Id personId = new IdImpl( personCnt ) ;
					Person person = new PersonImpl( personId ) ;
					personCnt++ ;
					if ( personCnt > 10 ) {
						log.error( "hack" ) ;
						return ;
					}

					population.addPerson(person) ;

					Plan plan = person.createPlan(true);
					plan.setSelected(true) ;
					Utils.makeHomePlan(plan, homeCoord) ;

					if ( ii<nWorkers ) {
						person.setEmployed("yes") ;
					} else {
						person.setEmployed("no") ;
					}
				}
				line = reader.readLine(); // next line
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1) ;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readZones(Facilities zones, Layer parcels) {
		log.fatal("not implemented; aborting ...") ;
		System.exit(-1);
	}


}
