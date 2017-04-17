import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class FileServer {
	private int port;
	private FileIO fileIO;
	private MessagesQueue messages = new MessagesQueue();
	private Map<Integer, FileTransmissionService> clients = Collections.synchronizedMap(new HashMap<>());
	private Map<Integer, FileServerThread> threads = new HashMap<>();
	private final static FileLock fileLock = new FileLock();
	private static int id = 1;
	
	public FileServer(int port, File sourceFile) {
		this.port = port;
		this.fileIO = new FileIO(sourceFile);
		new ClientConnectionService().start();
		new ClientRequestService().start();
		System.out.println("Server started");
	}
	
	class ClientConnectionService extends Thread {
		public void run() {
			try (ServerSocket serverSocket = new ServerSocket(port)) {
				while (true) {
					Socket socket = serverSocket.accept();
					FileTransmissionService fts = new FileTransmissionService(id, socket, messages);
					fts.setDaemon(true);
					fts.start();
					System.out.printf("Client %d connected\n", id);
					clients.put(id++, fts);
				}
				
			} catch (IOException e) {
				// throw it outside
				System.err.println(e.getMessage());
	            System.exit(-1);
			}
		}
	}
	
	class ClientRequestService extends Thread {
		public void run() {
			while (true) {
				FileDataMessage message = messages.takeOrBlock();
				
				FileServerThread thread;
				if (threads.containsKey(message.getClientId())) {
					thread = new FileServerThread(fileIO, message, clients, fileLock, threads.get(message.getClientId()));
				} else {
					thread = new FileServerThread(fileIO, message, clients, fileLock, null);
				}
				threads.put(message.getClientId(), thread);
				thread.setDaemon(true);
				thread.start();
			}
		}
	}
	
	
	public static void main(String[] args) {
		int portNumber = 4444;
		if (args.length != 0) {
			portNumber = Integer.valueOf(args[0]);
		}
		
		File sourceFile = new File("hris.txt");
		FileServer server = new FileServer(portNumber, sourceFile);
	}

}
