package aenadon.viruscomplete;

import android.support.annotation.NonNull;

class AvCheck implements Comparable<AvCheck> { // Contains the results of the scan for passing them to the adapter
    // First row
    String date;
    int positives, total;
    boolean isFirstRow;

    // URLScan row
    String name;
    int detection;

    // FileScan row
        /* String name; */
    String malwareName;
    boolean isFileScan;

    // URL check
    public AvCheck(String name, int detection) {
        this.name = name;
        this.detection = detection;
        this.isFirstRow = false;
        this.isFileScan = false;
    }

    // First row
    public AvCheck(String date, int positives, int total, boolean firstRow) {
        this.date = date;
        this.positives = positives;
        this.total = total;
        this.isFirstRow = firstRow;
        // isFileScan is never queried if firstRow = true
    }

    // File check
    public AvCheck(String name, String malwareName, boolean isFileScan) {
        this.name = name;
        this.malwareName = malwareName; // if malwareName is null, file is safe
        this.isFileScan = isFileScan;
    }

    @Override
    public int compareTo(@NonNull AvCheck avCheck) {
        return this.name.compareTo(avCheck.name);
    }
}

