package playground.meisterk.org.matsim.run.ptRouting;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.matsim.core.network.NetworkLayer;
import org.matsim.matrices.Matrices;
import org.matsim.matrices.Matrix;
import org.matsim.visum.VisumMatrixReader;
import org.matsim.world.MatsimWorldReader;
import org.matsim.world.World;
import org.xml.sax.SAXException;

import playground.marcel.kti.router.SwissHaltestellen;
import playground.meisterk.org.matsim.config.groups.KtiConfigGroup;


public class PTRoutingInfo {
	private Matrix ptTravelTimes = null;
	private SwissHaltestellen haltestellen = null;
	private World localWorld=null;

	private static final Logger log = Logger.getLogger(PTRoutingInfo.class);
	
	public void prepareKTIRouter(final KtiConfigGroup ktiConfigGroup, final NetworkLayer network) {
		
		if (!ktiConfigGroup.isUsePlansCalcRouteKti()) {
			log.error("The kti module is missing.");
		}

		// municipality layer from world file
		this.localWorld = new World();
		try {
			new MatsimWorldReader(localWorld).parse(ktiConfigGroup.getWorldInputFilename());
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		log.info("Reading traveltime matrix...");
		Matrices matrices = new Matrices();
		this.ptTravelTimes = matrices.createMatrix("pt_traveltime", localWorld.getLayer("municipality"), null);
		VisumMatrixReader reader = new VisumMatrixReader(this.ptTravelTimes);
		reader.readFile(ktiConfigGroup.getPtTraveltimeMatrixFilename());
		log.info("Reading traveltime matrix...done.");

		log.info("Reading haltestellen...");
		this.haltestellen = new SwissHaltestellen(network);
		try {
			haltestellen.readFile(ktiConfigGroup.getPtHaltestellenFilename());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		log.info("Reading haltestellen...done.");

	}

	public Matrix getPtTravelTimes() {
		return ptTravelTimes;
	}

	public SwissHaltestellen getHaltestellen() {
		return haltestellen;
	}

	public World getLocalWorld() {
		return localWorld;
	}
}
