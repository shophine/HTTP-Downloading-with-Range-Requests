import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;

public class MyThread extends Thread {

	private volatile boolean flag = true;
	String url, filePath, byteRange;

	public MyThread() {
	}

	public MyThread(String url, String byteRange, String filepath) {
		this.url = url;
		this.byteRange = byteRange;
		this.filePath = filepath;
	}

	public void run() {

		String tempByteRange = byteRange;
		System.out.println("\nRunning : " + getName() + " | Thread ID : " + getId() + "\n");

		try {

			System.out.println("Starting a new connection for : " + getName() + "\n");
			System.out.println("\n\t\tFor " + getName() + "\n\t\tURL : " + url + "\n\t\tByteRange : " + byteRange
					+ "\n\t\tFilePath :" + filePath + "\n\n");
			URL urlObj = new URL(url);
			HttpURLConnection httpCon = (HttpURLConnection) urlObj.openConnection();

			// requesting the downloadable file in specific range - RANGE REQUEST
			httpCon.setRequestProperty("Range", "bytes=" + tempByteRange);

			// getting the file details
			int responseCode = httpCon.getResponseCode();
			String responseMessage = httpCon.getResponseMessage();
			String contentType = httpCon.getContentType();
			int contentLength = httpCon.getContentLength();
			long date = httpCon.getDate();
			long lastModified = httpCon.getLastModified();

			// printing the file details

			System.out.println(getName() + " : Response Code: " + responseCode + "\n" + getName()
					+ " : Response Message: " + responseMessage + "\n" + getName() + " : Content Type: " + contentType
					+ "\n" + getName() + " : Content Length: " + contentLength + "\n" + getName() + " : Date: "
					+ new Date(date) + "\n" + getName() + " : Last Modified: " + new Date(lastModified));
			/*
			 * System.out.println(getName() +" : Response Message: " + responseMessage);
			 * System.out.println(getName() +" : Content Type: " + contentType);
			 * System.out.println(getName() +" : Content Length: " + contentLength);
			 * System.out.println(getName() +" : Date: " + new Date(date));
			 * System.out.println(getName() +" : Last Modified: " + new Date(lastModified));
			 */

			InputStream inputStream = httpCon.getInputStream();
			BufferedInputStream reader = new BufferedInputStream(inputStream);

			BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(filePath));

			byte[] buffer = new byte[4096];
			int bytesRead = -1;

			while ((bytesRead = reader.read(buffer)) != -1) {
				// writing it to the buffer
				writer.write(buffer, 0, bytesRead);
			}

			writer.close();
			reader.close();

			System.out.println("\n" + getName() + " : File Downloaded\n\n\n");

			// closing the connection
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
	}

}
