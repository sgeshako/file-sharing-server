import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class FileIO {
	
	private File sourceFile;

	public FileIO(File sourceFile) {
		this.sourceFile = sourceFile;
	}
	
	public synchronized char[] readChars() throws IOException {
		FileReader fileReader = new FileReader(sourceFile);
		char[] fileChars = new char[(int) sourceFile.length()];
		fileReader.read(fileChars);
		fileReader.close();
		return fileChars;
	}
	
	
	public synchronized void writeChars(char[] fileChars) throws IOException {
		FileWriter fileWriter = new FileWriter(sourceFile);
		fileWriter.write(fileChars);
		fileWriter.close();
	}

}
