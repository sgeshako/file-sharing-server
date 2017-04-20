import java.io.Serializable;

public class FileDataMessage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3534162898535714127L;

	private int clientId;
	private MessageStatus status;
	private char[] txtChars;

	public FileDataMessage(char[] content, MessageStatus status) {
		this.status = status;
		this.txtChars = content;
	}
	
	public MessageStatus getStatus() {
		return status;
	}

	public char[] getTxtChars() {
		return txtChars;
	}
	
	public int getClientId() {
		return clientId;
	}
	
	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

}
