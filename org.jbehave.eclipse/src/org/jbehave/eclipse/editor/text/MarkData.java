package org.jbehave.eclipse.editor.text;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.jbehave.eclipse.Activator;
import org.jbehave.eclipse.editor.EditorUtils;
import org.jbehave.eclipse.util.New;

public class MarkData {
	
    private Integer lineNumber, columnNumber;
    private Integer offsetStart, offsetEnd;
    public String message;
    public int markerSeverity;
    private Map<String, Object> attributes = New.hashMap();

    public MarkData message(String message) {
        this.message = message;
        return this;
    }
    
    public MarkData offsetStart(int offsetStart) {
        this.offsetStart = offsetStart;
        return this;
    }
    
    public MarkData offsetEnd(int offsetEnd) {
        this.offsetEnd = offsetEnd;
        return this;
    }

    public MarkData line(int lineNumber) {
        this.lineNumber = lineNumber;
        return this;
    }

    public MarkData column(int columnNumber) {
        this.columnNumber = columnNumber;
        return this;
    }

    public MarkData info() {
        return severity(IMarker.SEVERITY_INFO);
    }

    public MarkData warning() {
        return severity(IMarker.SEVERITY_WARNING);
    }

    public MarkData error() {
        return severity(IMarker.SEVERITY_ERROR);
    }

    public MarkData severity(int markerSeverity) {
        this.markerSeverity = markerSeverity;
        return this;
    }

    public MarkData attribute(String key, String value) {
        attributes.put(key, value);
        return this;
    }
    
    public MarkData attribute(String key, int value) {
        attributes.put(key, value);
        return this;
    }
    
    public void applyTo(IFile file, IDocument document, String markerId) {
        try {
            Map<String, Object> map = createAttributes(file, document);
            MarkerUtilities.createMarker(file, map, markerId);
        } catch (Exception ee) {
            Activator.logError("Unable to apply marks on <" + file + ">", ee);
        }
    }
    
    public Map<String, Object> createAttributes(IFile file, IDocument document) throws BadLocationException {
        Map<String, Object> map = new HashMap<String, Object>(attributes);
        int lineNb = lineNumber(document);
        if(lineNb>=0)
            MarkerUtilities.setLineNumber(map, lineNb);
        MarkerUtilities.setMessage(map, message);
        map.put(IMarker.LOCATION, file.getFullPath().toString());
   
        defineCharStart(document, map);
        defineCharEnd(document, map);
   
        map.put(IMarker.SEVERITY, Integer.valueOf(markerSeverity));
        return map;
    }

    private int lineNumber(IDocument document) throws BadLocationException {
        if(lineNumber!=null)
            return lineNumber;
        else if(offsetStart!=null)
            return document.getLineOfOffset(offsetStart);
        else
            return -1;
    }
    
    private void defineCharStart(IDocument document, Map<String, Object> map) {
        if(offsetStart!=null) {
            map.put(IMarker.CHAR_START, offsetStart);
        }
        else if (lineNumber!=null && columnNumber!=null) {
            Integer charStart = EditorUtils.getCharStart(document, lineNumber, columnNumber);
            if (charStart != null)
                map.put(IMarker.CHAR_START, charStart);
        }
    }
    
    private void defineCharEnd(IDocument document, Map<String, Object> map) {
        if(offsetEnd!=null) {
            map.put(IMarker.CHAR_END, offsetEnd);
        }
        else if (lineNumber!=null && columnNumber!=null) {
            Integer charEnd = EditorUtils.getCharEnd(document, lineNumber, columnNumber);
            if (charEnd != null)
                map.put(IMarker.CHAR_END, charEnd);
        }
    }
    
    @Override
    public String toString() {
        return "MarkData [offsetStart=" + offsetStart + ", offsetEnd=" + offsetEnd + ", message=" + message
                + ", markerSeverity=" + markerSeverity + "]";
    }
}