import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultCaret;


public class FileClientGUI {
	
	private FileClient fileClient;
	private static BlockingQueue<String> blockingQ = new ArrayBlockingQueue<String>(Short.MAX_VALUE);

	private JFrame mainFrame;
	private JButton connectionButton;
	private JButton receiveFileButton;
	private JButton sendFileButton;
	private JFileChooser fileChooser;
	private JTextArea console;
	private JScrollPane scrollConsole;
	
	public FileClientGUI(FileClient fileClient) {
		init();
		this.fileClient = fileClient;
		DisplayConsole displayConsole = new DisplayConsole();
		displayConsole.setDaemon(true);
		displayConsole.start();
	}
	
	private void init() {
		mainFrame = new JFrame("FileSharing client");
		mainFrame.setSize(800, 500);
		GroupLayout layout = new GroupLayout(mainFrame.getContentPane());
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		mainFrame.getContentPane().setLayout(layout);
		mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		fileChooser = new JFileChooser();
		
		connectionButton = new JButton("Connect");
		receiveFileButton = new JButton("Receive file");
		receiveFileButton.setEnabled(false);
		sendFileButton = new JButton("Send file");
		sendFileButton.setEnabled(false);
		
		console = new JTextArea();
		console.setEditable(false);
		((DefaultCaret) console.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		scrollConsole = new JScrollPane(console);
		
		connectionButton.addActionListener(new ConnectionListener());
		receiveFileButton.addActionListener(new ReceiveFileListener());
		sendFileButton.addActionListener(new SendFileListener());
		
		
		layout.setHorizontalGroup(
				layout.createParallelGroup(GroupLayout.Alignment.CENTER)
					.addGroup(layout.createSequentialGroup()
							.addComponent(receiveFileButton)
							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 100, 100)
							.addComponent(sendFileButton))
					.addGroup(layout.createSequentialGroup()
							.addComponent(connectionButton, 30, 170, 170)
							.addComponent(scrollConsole, 100, 580, 580))
		);
		
		layout.setVerticalGroup(
				layout.createSequentialGroup()
					.addGap(0, 50, 50)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(receiveFileButton)
							.addComponent(sendFileButton))
					.addGap(0, 50, 50)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
							.addComponent(connectionButton, Alignment.CENTER, 25, 35, 35)
							.addComponent(scrollConsole, 25, 280, 280))
		);

		mainFrame.setVisible(true);
	}
	
	private class ConnectionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (!fileClient.isConnected()) {
				try {
					fileClient.connect();
					connectionButton.setText("Disconnect");
					receiveFileButton.setEnabled(true);
					sendFileButton.setEnabled(true);
				} catch (IOException e1) {
					// print in console "Error connecting to server"
				}
			} else {
				try {
					fileClient.requestDisconnect();
					connectionButton.setText("Connect");
					receiveFileButton.setEnabled(false);
					sendFileButton.setEnabled(false);
				} catch (IOException e1) {
					// print in console "Failed to disconnect"
				}
			}
		}
		
	}
	
	private class ReceiveFileListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			int returnVal = fileChooser.showSaveDialog(mainFrame);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				fileClient.requestFile(file);
			}
		}
		
	}
	
	private class SendFileListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			fileClient.returnFile();
		}
		
	}
	
	private class DisplayConsole extends Thread {
		public void run() {
			while (true) {
				try {
					console.append(blockingQ.take() + "\n");
				} catch (InterruptedException e) {}
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			String host = null;
			int port = 4444;
			if (args.length != 0) {
				host = args[0];
				port = Integer.valueOf(args[1]);
			}
			FileClient fileClient = new FileClient(host, port, blockingQ);
			new FileClientGUI(fileClient);
		} catch (IOException e) {
			System.err.println(e.getMessage());
            System.exit(1);
		}
		
	}
	
	
}
