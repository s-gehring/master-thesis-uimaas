package gehring.uima;

public class LeoException extends RuntimeException {

	public LeoException(final String msg) {
		super(msg);
	}
	public LeoException(final String msg, final Throwable t) {
		super(msg, t);
	}
}
