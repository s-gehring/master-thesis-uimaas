package gehring.uima;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.resource.ResourceInitializationException;

import gehring.uima.examples.factories.SampleCollectionReaderFactory;
import gehring.uima.examples.factories.SamplePipelineFactory;
import gov.va.vinci.leo.Service;
import gov.va.vinci.leo.cr.ExternalCollectionReader;
import gov.va.vinci.leo.descriptors.LeoAEDescriptor;

public class LeoDeployer {

	public LeoDeployer() throws LeoException {
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

	@SuppressWarnings("unused")
	private ExternalCollectionReader getLeoReader() {
		CollectionReaderDescription reader = SampleCollectionReaderFactory.getGutenbergPartialReaderSizedDescription(1F,
				0, -1);
		return new ExternalCollectionReaderFixed(reader);
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

		return true;
	}

	public static void main(final String[] args) throws ResourceInitializationException {
		LeoDeployer leo = new LeoDeployer();

		AnalysisEngineDescription ae = SamplePipelineFactory.getOpenNlpPipelineDescription();
		ae.setAnnotatorImplementationName("AnnotatorImplementationName");
		ae.setImplementationName("ImplementationName");
		ae.getAnalysisEngineMetaData().setName("MetaDataName");

		leo.deploy(ae);
		/*
		 * if (MINS.length != MAXS.length) { throw new
		 * RuntimeException("Mins length " + MINS.length +
		 * " is not equal to maxs length " + MAXS.length); } for (int i = 0; i <
		 * MINS.length; ++i) { compute(SUFF[i], MINS[i], MAXS[i]); }
		 */
	}

}
