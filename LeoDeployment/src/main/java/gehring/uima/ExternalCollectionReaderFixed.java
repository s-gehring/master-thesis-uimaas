package gehring.uima;

import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.resource.ResourceInitializationException;

import gov.va.vinci.leo.cr.ExternalCollectionReader;

public class ExternalCollectionReaderFixed extends ExternalCollectionReader {

	public ExternalCollectionReaderFixed(final CollectionReader reader) {
		super((CollectionReader_ImplBase) null);

		super.reader = reader;
	}

	public ExternalCollectionReaderFixed(final CollectionReaderDescription readerDescription) {
		super((CollectionReader_ImplBase) null);
		CollectionReader reader;
		try {
			reader = CollectionReaderFactory.createReader(readerDescription);
		} catch (ResourceInitializationException e) {
			throw new RuntimeException("Failed to instantiate reader.", e);
		}

		super.reader = reader;
	}

}
