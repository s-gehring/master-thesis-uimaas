package gehring.uima;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.resource.ResourceInitializationException;

import gehring.uima.distributed.benchmark.BenchmarkMetadata;
import gehring.uima.examples.factories.SamplePipelineFactory;
import gov.va.vinci.leo.Service;
import gov.va.vinci.leo.descriptors.LeoAEDescriptor;

public class LeoServiceDeployment {

	private static final Logger LOGGER = Logger.getLogger(LeoServiceDeployment.class);

	public LeoServiceDeployment() throws LeoException {
		if (System.getenv("UIMA_HOME") == null) {
			System.err.println("System Environment Variable UIMA_HOME is missing. Dumping entire environment...");
			List<Entry<String, String>> entries = new ArrayList<>(System.getenv().entrySet());
			Collections.sort(entries, new Comparator<Entry<String, String>>() {

				@Override
				public int compare(final Entry<String, String> o1, final Entry<String, String> o2) {
					return o1.getKey().compareTo(o2.getKey());
				}

			});

			for (Entry<String, String> entry : entries) {
				System.err.println("\t" + entry.getKey() + " = " + entry.getValue());
			}
			throw new LeoException("System environment variable UIMA_HOME is missing.");
		}
	}

	public boolean deploy(final AnalysisEngineDescription pipeline) {
		Service service;
		try {
			service = new Service();
		} catch (Exception e) {
			throw new RuntimeException("Failed to allocate a new service.",
					new LeoException("Properties file cannot be read or data is invalid.", e));
		}

		LeoAEDescriptor annotatorA = new LeoAEDescriptor(pipeline);
		annotatorA.setNumberOfInstances(1);
		// tcp://broker:61616
		// http://broker:8080
		service.setBrokerURL("tcp://broker:61616");

		try {
			service.deploy(annotatorA);
		} catch (Exception e) {
			throw new LeoException("Any error occured during deployment.", e);
		}
		System.out.println("Hallo :)");
		return true;
	}

	private static boolean clientMode(final String[] args) {
		for (String arg : args) {
			if (arg.equals("--client")) {
				return true;
			}
		}
		return false;
	}

	private static String getClientName(final String[] args) {
		for (int i = 0; i < args.length - 1; ++i) {
			if (args[i].equals("--name")) {
				return args[i + 1];
			}
		}
		return "<no name>";
	}

	private static int getPipelineId(final String[] args) {
		for (int i = 0; i < args.length - 1; ++i) {
			if (args[i].equals("--pipelineId")) {
				return Integer.parseInt(args[i + 1]);
			}
		}
		return 0;
	}

	private static long getCliLong(final String command, final String[] args, final long defaultVal) {
		for (int i = 0; i < args.length - 1; ++i) {
			if (args[i].equals(command)) {
				return Long.parseLong(args[i + 1]);
			}
		}
		return defaultVal;
	}
	private static float getCliFloat(final String command, final String[] args, final float defaultVal) {
		for (int i = 0; i < args.length - 1; ++i) {
			if (args[i].equals(command)) {
				return Float.parseFloat(args[i + 1]);
			}
		}
		return defaultVal;
	}

	@SuppressWarnings("unused")
	public static void main(final String[] args) throws ResourceInitializationException {
		System.out.println("Arguments");
		for (String arg : args) {
			System.out.println("\t" + arg);
		}
		long min = getCliLong("--minSize", args, 0);
		long max = getCliLong("--maxSize", args, -1);
		float ratio = getCliFloat("-d", args, 1F);

		if (clientMode(args)) {
			LOGGER.info("Starting client...");
			BenchmarkMetadata meta = new BenchmarkMetadata(0, 0, 0, getClientName(args));
			new LeoClient(meta, min, max, ratio);
			System.exit(0);
		}

		LOGGER.info("Starting server with minSize " + min + ", maxSize " + max + ", ratio " + ratio);
		LeoServiceDeployment leo = new LeoServiceDeployment();

		AnalysisEngineDescription ae = SamplePipelineFactory.getPipelineDescriptionById(getPipelineId(args));
		// ae.setAnnotatorImplementationName("AnnotatorImplementationName");
		// ae.setImplementationName("ImplementationName");
		// ae.getAnalysisEngineMetaData().setName("MetaDataName");

		leo.deploy(ae);
		/*
		 * if (MINS.length != MAXS.length) { throw new
		 * RuntimeException("Mins length " + MINS.length +
		 * " is not equal to maxs length " + MAXS.length); } for (int i = 0; i <
		 * MINS.length; ++i) { compute(SUFF[i], MINS[i], MAXS[i]); }
		 */
	}

}
