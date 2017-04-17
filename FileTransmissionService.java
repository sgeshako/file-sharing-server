import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class FileTransmissionService extends Thread {
	
	private int clientId;
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	private MessagesQueue messages;
	private boolean isAlive;
	private boolean allowOutput;
	
	public FileTransmissionService(int clientId, Socket socket, MessagesQueue messages) throws IOException {
		this.clientId = clientId;
		this.socket = socket;
		out = new ObjectOutputStream(socket.getOutputStream());
		in = new ObjectInputStream(socket.getInputStream());
		this.messages = messages;
		isAlive = true;
		allowOutput = true;
	}
	
	public void run() {
		while (true) {
			try {
				if (!isConnectionAlive()) {
					break;
				}
				FileDataMessage msg = (FileDataMessage) in.readObject();
				msg.setClientId(clientId);
				messages.put(msg);
			} catch (EOFException ex) {
				break;
			} catch (ClassNotFoundException | IOException e) {
				System.err.println(e);
				e.printStackTrace();
			}
			
		}
	}
	
	public FileDataMessage receive() throws IOException, ClassNotFoundException {
		return (FileDataMessage) in.readObject();
	}
	
	public synchronized void send(FileDataMessage message) throws IOException {
		if (!socket.isClosed() && allowOutput) {
			out.writeObject(message);
			out.flush();
		}
	}

	public int getClientId() {
		return clientId;
	}
	
	public synchronized void stopReceiving() {
		isAlive = false;
	}
	
	public synchronized void closeOutput() throws IOException {
		allowOutput = false;
		socket.shutdownOutput();
	}
	
	public synchronized void closeConnection() throws IOException {
		try {
			sleep(500);
		} catch (InterruptedException e) {}
		socket.close();
	}
	
	public synchronized boolean isConnectionAlive() {
		return !socket.isClosed() && isAlive;
	}

}
