package br.ufsc.lapesd.sro.tokit;

public class DataSourceParams {
    private String sourceLocation;
    private String enclosure;
	private String delimiter;
	private boolean hasHeaderLine;
	private boolean hasEventTimestamp;

	public DataSourceParams(String sourceLocation, String enclosure, String delimiter, boolean hasHeaderLine, boolean hasEventTimestamp) {
		this.sourceLocation = sourceLocation;
		this.delimiter = delimiter;
		this.enclosure = enclosure;
		this.hasHeaderLine = hasHeaderLine;
		this.hasEventTimestamp = hasEventTimestamp;
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

	public boolean hasEventTimestamp() {
		return hasEventTimestamp;
	}
	
}
