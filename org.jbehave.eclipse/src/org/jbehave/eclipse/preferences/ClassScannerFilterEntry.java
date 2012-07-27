package org.jbehave.eclipse.preferences;

import org.apache.commons.lang.StringUtils;

import fj.Equal;
import fj.F;

public class ClassScannerFilterEntry {

    public enum ApplyOn {
        PackageRoot, Package, Class
    }

    public static F<ClassScannerFilterEntry, Boolean> filter(final ApplyOn applyOn, final boolean exclude) {
        return new F<ClassScannerFilterEntry, Boolean>() {
            @Override
            public Boolean f(ClassScannerFilterEntry entry) {
                return entry.applyOn == applyOn && entry.exclude == exclude;
            }
        };
    }

    public static F<ClassScannerFilterEntry, String> toPatterns() {
        return new F<ClassScannerFilterEntry, String>() {
            @Override
            public String f(ClassScannerFilterEntry entry) {
                return entry.patterns;
            }
        };
    }

    public static F<ClassScannerFilterEntry, String[]> toSplittedPatterns() {
        return new F<ClassScannerFilterEntry, String[]>() {
            @Override
            public String[] f(ClassScannerFilterEntry entry) {
                return entry.splitPatterns();
            }
        };
    }

    private boolean exclude;
    private String patterns;
    private ApplyOn applyOn;

    public ClassScannerFilterEntry(String pattern, ApplyOn applyOn, boolean exclude) {
        super();
        this.patterns = pattern;
        this.applyOn = applyOn;
        this.exclude = exclude;
    }

    public ApplyOn getApplyOn() {
        return applyOn;
    }

    public String getPatterns() {
        return patterns;
    }

    public String[] splitPatterns() {
        return patterns.split("[,; ]");
    }

    public boolean isExclude() {
        return exclude;
    }

    public boolean isInclude() {
        return !isExclude();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ClassScannerFilterEntry))
            return false;
        ClassScannerFilterEntry other = (ClassScannerFilterEntry) obj;
        return other.applyOn == applyOn//
                && other.exclude == exclude//
                && StringUtils.equals(patterns, other.patterns);
    }

    public F<ClassScannerFilterEntry, Boolean> equalF() {
        Equal<ClassScannerFilterEntry> any = Equal.anyEqual();
        F<ClassScannerFilterEntry, Boolean> filter = any.eq(this);
        return filter;
    }
}
