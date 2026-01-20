package cm.iusjc.reporting.entity;

public enum ReportFormat {
    PDF("application/pdf", ".pdf"),
    EXCEL("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", ".xlsx"),
    CSV("text/csv", ".csv"),
    JSON("application/json", ".json");
    
    private final String mimeType;
    private final String extension;
    
    ReportFormat(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public String getExtension() {
        return extension;
    }
}