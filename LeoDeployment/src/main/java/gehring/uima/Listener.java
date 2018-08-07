package gehring.uima;

import org.apache.uima.aae.UimaASApplicationEvent.EventTrigger;
import org.apache.uima.aae.client.UimaASProcessStatus;
import org.apache.uima.aae.client.UimaAsBaseCallbackListener;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.EntityProcessStatus;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import gehring.uima.distributed.benchmark.BenchmarkMetadata;
import gehring.uima.distributed.benchmark.BenchmarkResult;

public class Listener extends UimaAsBaseCallbackListener {

	private static int numberOfDocuments = 0;

	private BenchmarkResult benchmark;

	public Listener(final BenchmarkMetadata meta) {
		this.benchmark = new BenchmarkResult();
		this.benchmark.setMetadata(meta);
	}

	/**
	 * Called by Uima AS client API just before the CAS is send to the service.
	 *
	 * @param status
	 *            - status object containing id of the CAS being send.
	 */
	@Override
	public void onBeforeMessageSend(final UimaASProcessStatus status) {
		Integer no = ++numberOfDocuments;
		long docSize = status.getCAS().getDocumentText().length();

		synchronized (this.benchmark) {
			BenchmarkMetadata meta = this.benchmark.getMetadata();
			if (meta != null) {
				meta.setSumOfAllDocumentSizes(meta.getSumOfAllDocumentSizes() + docSize);
			}
		}
		System.out.println("Starting CAS #" + no + ".");
	}
	/**
	 * Called by Uima AS client API before CAS processing begins at the remote
	 * UIMA AS service
	 *
	 * @param status
	 * @param nodeIP
	 * @param pid
	 */
	@Override
	public void onBeforeProcessCAS(final UimaASProcessStatus status, final String nodeIP, final String pid) {
		this.benchmark.startMeasurement("CAS (" + status.getCAS().getDocumentText().hashCode() + ")");
		System.out.println("Starting to process CAS as node '" + nodeIP + "' with PID '" + pid + "'.");
	}
	/**
	 * Called by Uima AS client API before GetMeta processing begins at the
	 * remote UIMA AS service
	 *
	 * @param nodeIP
	 *            - Node IP where GetMeta request is handled
	 * @param pid
	 *            - Remote Service PID where GetMeta request is handled. The has
	 *            the following syntax: <PID:THREADID>
	 */
	@Override
	public void onBeforeProcessMeta(final String nodeIP, final String pid) {
		System.out.println("Hook: Before GetMeta processing begins.");
	}

	@Override
	public void onUimaAsServiceExit(final EventTrigger cause) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(this.benchmark.toString());
		String prettyJsonString = gson.toJson(je);

		System.out.println("Benchmark results: " + prettyJsonString);

	}

	/**
	 * The callback used to inform the application that the initialization
	 * request has completed. On success aStatus will be null; on failure use
	 * the EntityProcessStatus class to get the details.
	 *
	 * @param aStatus
	 *            the status of the processing. This object contains a record of
	 *            any Exception that occurred, as well as timing information.
	 */
	@Override
	public void initializationComplete(final EntityProcessStatus aStatus) {
		System.out.println("Hook: Initialization complete. Status: " + (aStatus == null ? "SUCCESS" : "FAILURE"));
	}

	/**
	 * Called when the processing of each entity has completed.
	 *
	 * @param aCas
	 *            the CAS containing the processed entity and the analysis
	 *            results
	 * @param aStatus
	 *            the status of the processing. This object contains a record of
	 *            any Exception that occurred, as well as timing information.
	 */
	@Override
	public void entityProcessComplete(final CAS aCas, final EntityProcessStatus aStatus) {
		String id = "CAS (" + aCas.getDocumentText().hashCode() + ")";
		System.out.println("Entity complete --- (" + id + ")");
		this.benchmark.endMeasurement(id);
		BenchmarkMetadata meta = this.benchmark.getMetadata();
		long casSize = aCas.size();
		synchronized (meta) {
			meta.setSumOfAllCasSizes(meta.getSumOfAllCasSizes() + casSize);
		}

	}

	/**
	 * The callback used to inform the application that the CPC request has
	 * completed. On success aStatus will be null; on failure use the
	 * EntityProcessStatus class to get the details.
	 *
	 * @param aStatus
	 *            the status of the processing. This object contains a record of
	 *            any Exception that occurred, as well as timing information.
	 */
	@Override
	public void collectionProcessComplete(final EntityProcessStatus aStatus) {
		System.out.println("Hook: Collection processing completed.");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(this.benchmark.toString());
		String prettyJsonString = gson.toJson(je);
		System.out.println(prettyJsonString);
	}
}
