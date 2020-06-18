public class FileDetails {
	String fileName, extension;

	public FileDetails(String fileName, String extension) {
		this.fileName = fileName;
		this.extension = extension;
	}

	public FileDetails() {
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}
}
