import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.SequenceInputStream;
import java.util.Enumeration;
import java.util.Vector;

public class Merge {

	public static void main(String[] args) throws IOException {

		FileDetails fileDetails = new FileDetails();
		String extension = fileDetails.getExtension();

		int noOfTCPConnections = Integer.parseInt(args[1]);

		FileInputStream fileInputStreams[] = new FileInputStream[noOfTCPConnections];
		Vector vector = new Vector();

		for (int i = 0; i < noOfTCPConnections; i++) {
			fileInputStreams[i] = new FileInputStream("part_" + (i + 1));
			vector.addElement(fileInputStreams[i]);
		}
		Enumeration enumeration = vector.elements();
		SequenceInputStream sequenceInputStream = new SequenceInputStream(enumeration);
		FileOutputStream fileOutputStream = new FileOutputStream("mergedFile");

		int charReader;
		while ((charReader = sequenceInputStream.read()) != -1) {
			// System.out.println((char) charReader);
			// writing it to the file
			fileOutputStream.write(charReader);
		}

		fileOutputStream.close();
		sequenceInputStream.close();
		for (int i = 0; i < noOfTCPConnections; i++) {
			// closing all the input streams
			fileInputStreams[i].close();
		}
	}
}
