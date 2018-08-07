package gehring.uima;

import org.apache.log4j.Logger;
import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.collection.CollectionReaderDescription;

import gehring.uima.distributed.benchmark.BenchmarkMetadata;
import gehring.uima.examples.factories.SampleCollectionReaderFactory;
import gov.va.vinci.leo.Client;
import gov.va.vinci.leo.cr.ExternalCollectionReader;

public class LeoClient {

	private static final Logger LOGGER = Logger.getLogger(LeoClient.class);

	private static ExternalCollectionReader getLeoReader(final long min, final long max, final float ratio) {
		CollectionReaderDescription reader = SampleCollectionReaderFactory
				.getGutenbergPartialReaderSizedDescription(ratio, min, max);
		return new ExternalCollectionReaderFixed(reader);
	}

	public LeoClient(final BenchmarkMetadata meta, final long min, final long max, final float ratio) {
		Client client;
		try {
			client = new Client();
		} catch (Exception e) {
			throw new LeoException("An error occurred while loading the property files for the client.", e);
		}

		client.setBrokerURL("tcp://broker:61616");
		client.addUABListener(new Listener(meta));
		LOGGER.info("Running request to '" + client.getBrokerURL() + "' (" + client.getEndpoint() + ")");
		try {
			client.run(getLeoReader(min, max, ratio), (UimaAsBaseCallbackListener[]) null);
		} catch (Exception e) {
			throw new LeoException("An error occurred while running the client.", e);
		}
	}

}
