import java.awt.Desktop;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

public class FileClient {
	
	private InetAddress hostAddress;
	private int hostPort;
	private String name;
	private File targetFile;
	private MessagesQueue messages = new MessagesQueue();
	private FileTransmissionService fts;
	private BlockingQueue<String> blockingMessageQ;
	
	public FileClient(String host, int port, BlockingQueue<String> blockingMessageQ) throws IOException {
		this(null, host, port, blockingMessageQ);
	}
	
	public FileClient(String name, String host, int port, BlockingQueue<String> blockingMessageQ) throws IOException {
		this.name = name;
		this.hostAddress = InetAddress.getByName(host);
		this.hostPort = port;
		this.blockingMessageQ = blockingMessageQ;
	}
	
	class MessageHandling extends Thread {
		public void run() {
			try {
				while (true) {
					if (!fts.isConnectionAlive()) {
						break;
					}
					FileDataMessage message = messages.takeOrBlock();
					
					switch (message.getStatus()) {
					case SENDING_FILE:
						saveFromServer(message);
						log("File saved from server");
						if (name == null && Desktop.isDesktopSupported()) {
							Desktop.getDesktop().edit(targetFile);
						}
						break;
					case FILE_IS_LOCKED:
						log("File is locked");
						break;
					case FILE_IS_UNLOCKED:
						log("File is unlocked");
						break;
					case DISCONNECT:
						disconnect();
						log("Server has ended connection");
						break;
					default:
						break;
					}
				}
			} catch (Exception e) {
				log("A fatal error has occured. Please restart application.");
				e.printStackTrace();
			}
		}
		
		private void saveFromServer(FileDataMessage fileData) throws IOException {
			FileWriter fileWriter = new FileWriter(targetFile);
			fileWriter.write(fileData.getTxtChars());
			fileWriter.close();
		}
	}
	
	public void connect() throws IOException {
		log("Connecting to " + hostAddress + ":" + hostPort + " ...");
		Socket socket = new Socket(hostAddress, hostPort);
		fts = new FileTransmissionService(0, socket, messages);
		log("Connected");
		fts.setDaemon(true);
		fts.start();
		new MessageHandling().start();
	}
	
	public void requestFile(File targetFile) {
		this.targetFile = targetFile;
		try {
			log("Requesting file from server...");
			send(new FileDataMessage(null, MessageStatus.REQUESTING_FILE));
		} catch (IOException e) {
			log("Requesting file failed");
			e.printStackTrace();
		}
	}
	
	public void returnFile() {
		try {
			log("Sending file back to server...");
			send(new FileDataMessage(getFileChars(targetFile), MessageStatus.SENDING_FILE));
			log("File sent");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void requestDisconnect() throws IOException {
		log("Disconnecting from server...");
		send(new FileDataMessage(null, MessageStatus.DISCONNECT));
		fts.closeOutput();
	}
	
	public boolean isConnected() {
		if (fts != null && fts.isAlive()) {
			return fts.isConnectionAlive();
		}
		return false;
	}
	
	private void disconnect() throws IOException {
		fts.closeConnection();
	}
	
	// Read data from a file and send it through a socket
	private void send(FileDataMessage fileMsg) throws IOException {
		fts.send(fileMsg);
	}
	
	private char[] getFileChars(File sourceFile) throws IOException {
		FileReader fileReader = new FileReader(sourceFile);
				
		char[] fileChars = new char[(int) sourceFile.length()];
		fileReader.read(fileChars);
		fileReader.close();
		
		//appendChars(fileChars);
		return fileChars;
	}
	
	private void appendChars(char[] array) {
		String change = "\r\n" + name + " changed this";
		char[] modifiedArray = Arrays.copyOf(array, array.length + change.length());
		
		for (int i = modifiedArray.length - change.length(); i < modifiedArray.length; i++) {
			modifiedArray[i] = change.charAt(i - (modifiedArray.length - change.length()));
		}
		array = modifiedArray;
	}
	
	private void log(String text) {
		try {
			if (name == null) {
				blockingMessageQ.put("[" + new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").format(new Date()) + "]" + " " + text);
			} else {
				blockingMessageQ.put(name + ": " + text);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}