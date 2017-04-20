import java.io.IOException;
import java.util.Map;


public class FileServerThread extends Thread {
	private FileIO fileIO;
	private FileDataMessage message;
	private Map<Integer, FileTransmissionService> clients;
	private FileLock fileLock;
	private static int id = 1;
	private Thread previousThread;
	
	public FileServerThread(FileIO fileIO, FileDataMessage message, Map<Integer, FileTransmissionService> clients, FileLock fileLock, Thread previousThread) {
		super("FileMultiServerThread " + id++);
		this.fileIO = fileIO;
		this.message = message;
		this.clients = clients;
		this.fileLock = fileLock;
		this.previousThread = previousThread;
	}
	
	public void run() {
		try {
			if (previousThread != null && previousThread.isAlive()) {
				previousThread.join();
			}
			
			int clientId = message.getClientId();
			
			if (message.getStatus() == MessageStatus.REQUESTING_FILE) {
				if (fileLock.lockIfUnlocked(clientId)) {
					System.out.println("File locked for client " + clientId);
				}
				System.out.printf("Sending file from server to client %d...\n", clientId);
				FileDataMessage fileData = new FileDataMessage(fileIO.readChars(), MessageStatus.SENDING_FILE);
				sendToClient(clientId, fileData);
				System.out.println("File sent");
			} else if (message.getStatus() == MessageStatus.SENDING_FILE) {
				if (fileLock.isLocked(clientId)) {
					System.out.printf("Saving file from client %d...\n", clientId);
					fileIO.writeChars(message.getTxtChars());
					System.out.println("File saved");
					
					System.out.println("Alerting all clients that file is unlocked...");
					sendToAll(new FileDataMessage(null, MessageStatus.FILE_IS_UNLOCKED));
					fileLock.unlock();
				} else {
					System.out.printf("Notifying client %d that file is locked...\n", clientId);
					FileDataMessage fileMsg = new FileDataMessage(null, MessageStatus.FILE_IS_LOCKED);
					sendToClient(clientId, fileMsg);
				}
			} else if (message.getStatus() == MessageStatus.DISCONNECT) {
				if (fileLock.isLocked(clientId)) {
					System.out.println("Alerting all clients that file is unlocked...");
					sendToAll(new FileDataMessage(null, MessageStatus.FILE_IS_UNLOCKED));
					fileLock.unlock();
				}
				System.out.printf("Closing connection with client %d...\n", clientId);
				disconnect(clientId);
				System.out.println("Connection closed.");
			}

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	// Read data from a file and send it through a socket
	private void sendToClient(int clientId, FileDataMessage fileData) throws IOException {
		clients.get(clientId).send(fileData);
	}
	
	private void sendToAll(FileDataMessage fileData) throws IOException {
		for (FileTransmissionService fts : clients.values()) {
			fts.send(fileData);
		}
	}
	
	private void disconnect(int clientId) throws IOException {
		clients.get(clientId).stopReceiving();
		sendToClient(clientId, new FileDataMessage(null, MessageStatus.DISCONNECT));
		clients.remove(clientId).closeOutput();
	}

}
