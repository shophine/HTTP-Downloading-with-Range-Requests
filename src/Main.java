import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class Main {
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException {
		if (args.length != 2) {
			System.out.println("Syntax Error!!\nSyntax : <url> <filepath> <No of TCP Connections>\n");
			return;
		}

		// getting inputs from the commandline
		String url = args[0];
		int noOfTcpConnections = Integer.parseInt(args[1]);

		/*
		 * handling two types of url 1. url - path parameter 2. url - query parameter
		 */
		URL urlTest = new URL(url);
		String urlPath = urlTest.getPath();
		String fileName = urlPath.substring(urlPath.lastIndexOf('/') + 1);
		System.out.println("Test : Filename : " + fileName);

		// String fileName = url.substring(url.lastIndexOf('/') + 1);

		String extension = getExtension(fileName);

		FileDetails fileDetails = new FileDetails();
		fileDetails.setFileName(fileName);
		fileDetails.setExtension(extension);

		String originalFile = "originalFile" + "." + extension;

		int fileSize = 0;

		System.out.println("\n\nUrl : " + url + "\nFile Name : " + fileName + "\nNo.of.TCP connections : "
				+ noOfTcpConnections + "\nExtension : " + extension + "\n\n\n");

		// get the file size

		try {
			URL urlObj = new URL(url);

			HttpURLConnection httpCon = (HttpURLConnection) urlObj.openConnection();
			// getting the content length of the original file using HEAD request
			httpCon.setRequestMethod("HEAD");

			fileSize = httpCon.getContentLength();

			httpCon.disconnect();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// splitting size of each file - calculation
		int eachFileDownloadableSize = fileSize / noOfTcpConnections;
		// padding bits - calculation
		int remainingBits = fileSize % noOfTcpConnections;
		System.out.println("\n\nFile Size : " + fileSize + "\n\nEach File Downloadable Size : "
				+ eachFileDownloadableSize + "\n\nRemaining Bits : " + remainingBits + "\n");

		int arr[] = new int[noOfTcpConnections];
		for (int i = 0; i < noOfTcpConnections; i++) {
			if (i == 0) {
				arr[i] = eachFileDownloadableSize;
			} else {
				arr[i] = arr[i - 1] + eachFileDownloadableSize;
			}
			// System.out.println("\t\tRANGE LIMIT" + (i+1)+" : a[" + i + "] = " + arr[i] +
			// "\n");

		}

		// threads for TCP connections
		MyThread myThread[] = new MyThread[noOfTcpConnections];

		/*
		 * handling two cases 1. Padding not required 2. Padding required
		 */
		if (fileSize % noOfTcpConnections == 0) {
			// No padding of bits required
			for (int i = 0; i < noOfTcpConnections; i++) {
				if (i == 0) {
					myThread[i] = new MyThread(url, "0-" + (arr[i] - 1), "part_" + (i + 1));
					myThread[i].start();
					System.out.println("\t\tThread : " + (i + 1) + " Started\n");
				} else {
					myThread[i] = new MyThread(url, arr[i - 1] + "-" + (arr[i] - 1), "part_" + (i + 1));
					myThread[i].start();
					System.out.println("\t\tThread : " + (i + 1) + " Started\n");
				}
			}
		} else {
			// Padding of bits required
			for (int i = 0; i < noOfTcpConnections; i++) {
				if (i == 0) {
					myThread[i] = new MyThread(url, "0-" + (arr[i] - 1), "part_" + (i + 1));
					myThread[i].start();
					System.out.println("\t\tThread : " + (i + 1) + " Started\n");
				} else if (i == noOfTcpConnections - 1) {
					// padding done to the last part of the file
					myThread[i] = new MyThread(url, arr[i - 1] + "-" + ((arr[i] - 1) + remainingBits),
							"part_" + (i + 1));
					myThread[i].start();
					System.out.println("\t\tThread : " + (i + 1) + " Started\n");
				} else {
					myThread[i] = new MyThread(url, arr[i - 1] + "-" + (arr[i] - 1), "part_" + (i + 1));
					myThread[i].start();
					System.out.println("\t\tThread : " + (i + 1) + " Started\n");
				}
			}

		}

		// ensuring all threads download it's part before merging
		for (int i = 0; i < noOfTcpConnections; i++) {
			myThread[i].join();
		}

		// downloading the original file for checksum
		try {
			System.out.println("\n\nStarting to download the ORIGINAL FILE...");
			URL urlObj = new URL(url);
			HttpURLConnection httpCon = (HttpURLConnection) urlObj.openConnection();

			int responseCode = httpCon.getResponseCode();
			String responseMessage = httpCon.getResponseMessage();
			String contentType = httpCon.getContentType();
			int contentLength = httpCon.getContentLength();
			long date = httpCon.getDate();
			long lastModified = httpCon.getLastModified();

			System.out.println("ORIGINAL FILE : Response Code: " + responseCode);
			System.out.println("ORIGINAL FILE Response Message: " + responseMessage);
			System.out.println("ORIGINAL FILE Content Type: " + contentType);
			System.out.println("ORIGINAL FILE Content Length: " + contentLength);
			System.out.println("ORIGINAL FILE Date: " + new Date(date));
			System.out.println("ORIGINAL FILE Last Modified: " + new Date(lastModified) + "\n\n");

			InputStream inputStream = httpCon.getInputStream();
			BufferedInputStream reader = new BufferedInputStream(inputStream);
			BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(originalFile));

			byte[] buffer = new byte[4096];
			int bytesRead = -1;
			while ((bytesRead = reader.read(buffer)) != -1) {
				// writing into the buffer
				writer.write(buffer, 0, bytesRead);
			}

			writer.close();
			reader.close();

			System.out.println("\nORIGINAL FILE Downloaded!!!\n");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("\nStarting to merge the file...");

		// calling the merge function
		Merge.main(args);
		System.out.println(("\nMerge completed!!!\n\nPerforming SHA-256...\n"));

		// hash function to get the checksum
		sha256("mergedFile", originalFile);

	}

	private static String getExtension(String fileName) {
		if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0) {
			String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
			return extension;
		} else
			return " ";
	}

	private static void sha256(String combinedFilePath, String originalFilePath)
			throws IOException, NoSuchAlgorithmException {
		// checksum for mergedFile
		File file = new File(combinedFilePath);
		MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
		String checksumMerged = getFileChecksum(messageDigest, file);
		System.out.println("\nChecksum for mergedFile : " + checksumMerged);

		// checksum for originalFile
		File fileOriginal = new File(originalFilePath);
		MessageDigest messageDigestOriginal = MessageDigest.getInstance("SHA-256");
		String checksumOriginal = getFileChecksum(messageDigestOriginal, fileOriginal);
		System.out.println("\nChecksum for originalFile : " + checksumOriginal);

		// comparing the checksum of mergedFile and originalFile
		boolean checksumCheck = checksumMerged.equals(checksumOriginal);
		if (checksumCheck) {
			System.out.println("\nChecksums are equal\n\n");
		} else {
			System.out.println("\nChecksums are not equal\n\n");
		}
	}

	private static String getFileChecksum(MessageDigest digest, File file) throws IOException {

		FileInputStream fis = new FileInputStream(file);
		byte[] byteArray = new byte[1024];
		int bytesCount = 0;
		while ((bytesCount = fis.read(byteArray)) != -1) {
			digest.update(byteArray, 0, bytesCount);
		}
		fis.close();
		byte[] bytes = digest.digest();

		// Convert it to hexadecimal format
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		return sb.toString();
	}

}
