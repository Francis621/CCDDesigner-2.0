package com.ccdd.model.diagnostic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * SysML v2 语法与兼容性准入诊断报告 (纯原生 Java 实现)
 */
public class DiagnosticReport implements Serializable {

    private static final long serialVersionUID = 1L;

    private String snapshotToken;
    private boolean diagnosticPassed;
    private DiagnosticSummary summary;
    private List<DiagnosticItem> diagnostics = new ArrayList<>();

    public DiagnosticReport() {
    }

    public DiagnosticReport(String snapshotToken, boolean diagnosticPassed, DiagnosticSummary summary, List<DiagnosticItem> diagnostics) {
        this.snapshotToken = snapshotToken;
        this.diagnosticPassed = diagnosticPassed;
        this.summary = summary;
        this.diagnostics = diagnostics != null ? diagnostics : new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String snapshotToken;
        private boolean diagnosticPassed;
        private DiagnosticSummary summary;
        private List<DiagnosticItem> diagnostics = new ArrayList<>();

        public Builder snapshotToken(String snapshotToken) { this.snapshotToken = snapshotToken; return this; }
        public Builder diagnosticPassed(boolean diagnosticPassed) { this.diagnosticPassed = diagnosticPassed; return this; }
        public Builder summary(DiagnosticSummary summary) { this.summary = summary; return this; }
        public Builder diagnostics(List<DiagnosticItem> diagnostics) { this.diagnostics = diagnostics; return this; }

        public DiagnosticReport build() {
            return new DiagnosticReport(snapshotToken, diagnosticPassed, summary, diagnostics);
        }
    }

    public static class DiagnosticSummary implements Serializable {
        private static final long serialVersionUID = 1L;
        private int totalErrors;
        private int totalWarnings;
        private int scannedElements;

        public DiagnosticSummary() {}
        public DiagnosticSummary(int totalErrors, int totalWarnings, int scannedElements) {
            this.totalErrors = totalErrors;
            this.totalWarnings = totalWarnings;
            this.scannedElements = scannedElements;
        }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private int totalErrors;
            private int totalWarnings;
            private int scannedElements;
            public Builder totalErrors(int totalErrors) { this.totalErrors = totalErrors; return this; }
            public Builder totalWarnings(int totalWarnings) { this.totalWarnings = totalWarnings; return this; }
            public Builder scannedElements(int scannedElements) { this.scannedElements = scannedElements; return this; }
            public DiagnosticSummary build() { return new DiagnosticSummary(totalErrors, totalWarnings, scannedElements); }
        }

        public int getTotalErrors() { return totalErrors; }
        public void setTotalErrors(int totalErrors) { this.totalErrors = totalErrors; }
        public int getTotalWarnings() { return totalWarnings; }
        public void setTotalWarnings(int totalWarnings) { this.totalWarnings = totalWarnings; }
        public int getScannedElements() { return scannedElements; }
        public void setScannedElements(int scannedElements) { this.scannedElements = scannedElements; }
    }

    public static class DiagnosticItem implements Serializable {
        private static final long serialVersionUID = 1L;
        private String severity;
        private String code;
        private String message;
        private SourceLocation location;
        private String elementName;
        private String remediation;

        public DiagnosticItem() {}
        public DiagnosticItem(String severity, String code, String message, SourceLocation location, String elementName, String remediation) {
            this.severity = severity;
            this.code = code;
            this.message = message;
            this.location = location;
            this.elementName = elementName;
            this.remediation = remediation;
        }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private String severity;
            private String code;
            private String message;
            private SourceLocation location;
            private String elementName;
            private String remediation;
            public Builder severity(String severity) { this.severity = severity; return this; }
            public Builder code(String code) { this.code = code; return this; }
            public Builder message(String message) { this.message = message; return this; }
            public Builder location(SourceLocation location) { this.location = location; return this; }
            public Builder elementName(String elementName) { this.elementName = elementName; return this; }
            public Builder remediation(String remediation) { this.remediation = remediation; return this; }
            public DiagnosticItem build() { return new DiagnosticItem(severity, code, message, location, elementName, remediation); }
        }

        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public SourceLocation getLocation() { return location; }
        public void setLocation(SourceLocation location) { this.location = location; }
        public String getElementName() { return elementName; }
        public void setElementName(String elementName) { this.elementName = elementName; }
        public String getRemediation() { return remediation; }
        public void setRemediation(String remediation) { this.remediation = remediation; }
    }

    public static class SourceLocation implements Serializable {
        private static final long serialVersionUID = 1L;
        private String filePath;
        private int line;
        private int column;

        public SourceLocation() {}
        public SourceLocation(String filePath, int line, int column) {
            this.filePath = filePath;
            this.line = line;
            this.column = column;
        }

        public static Builder builder() { return new Builder(); }
        public static class Builder {
            private String filePath;
            private int line;
            private int column;
            public Builder filePath(String filePath) { this.filePath = filePath; return this; }
            public Builder line(int line) { this.line = line; return this; }
            public Builder column(int column) { this.column = column; return this; }
            public SourceLocation build() { return new SourceLocation(filePath, line, column); }
        }

        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public int getLine() { return line; }
        public void setLine(int line) { this.line = line; }
        public int getColumn() { return column; }
        public void setColumn(int column) { this.column = column; }
    }

    public String getSnapshotToken() { return snapshotToken; }
    public void setSnapshotToken(String snapshotToken) { this.snapshotToken = snapshotToken; }
    public boolean isDiagnosticPassed() { return diagnosticPassed; }
    public void setDiagnosticPassed(boolean diagnosticPassed) { this.diagnosticPassed = diagnosticPassed; }
    public DiagnosticSummary getSummary() { return summary; }
    public void setSummary(DiagnosticSummary summary) { this.summary = summary; }
    public List<DiagnosticItem> getDiagnostics() { return diagnostics; }
    public void setDiagnostics(List<DiagnosticItem> diagnostics) { this.diagnostics = diagnostics; }
}
