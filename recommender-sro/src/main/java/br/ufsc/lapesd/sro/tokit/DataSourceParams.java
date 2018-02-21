package br.ufsc.lapesd.sro.tokit;

public class DataSourceParams {
	private String appName;
    private String sourceLocation;
    private String enclosure;
	private String delimiter;
	private boolean hasHeaderLine;

	public String getAppName() {
		return appName;
	}
	
	public String getSourceLocation() {
        return sourceLocation;
    }

	public String getEnclosure() {
		return enclosure;
	}

	public String getDelimiter() {
		return delimiter;
	}

	public boolean hasHeaderLine() {
		return hasHeaderLine;
	}
	
	public DataSourceParams(String appName, String sourceLocation, String enclosure, String delimiter, boolean hasHeaderLine) {
		this.appName = appName;
		this.sourceLocation = sourceLocation;
		this.delimiter = delimiter;
		this.enclosure = enclosure;
		this.hasHeaderLine = hasHeaderLine;
	}
}
