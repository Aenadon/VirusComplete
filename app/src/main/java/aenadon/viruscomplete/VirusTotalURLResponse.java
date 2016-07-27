package aenadon.viruscomplete;

import com.google.gson.JsonElement;

import java.io.Serializable;

public class VirusTotalURLResponse implements Serializable {

    private int response_code, total, positives;
    private String scan_date, verbose_msg;
    private JsonElement scans;


    public int getResponse_code() {
        return response_code;
    }

    public void setResponse_code(int response_code) {
        this.response_code = response_code;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPositives() {
        return positives;
    }

    public void setPositives(int positives) {
        this.positives = positives;
    }

    public String getScan_date() {
        return scan_date;
    }

    public void setScan_date(String scan_date) {
        this.scan_date = scan_date;
    }

    public String getVerbose_msg() {
        return verbose_msg;
    }

    public void setVerbose_msg(String verbose_msg) {
        this.verbose_msg = verbose_msg;
    }

    public JsonElement getScans() {
        return scans;
    }

    public void setScans(JsonElement scans) {
        this.scans = scans;
    }

    @Override
    public String toString() {
        return "VirusTotalURLResponse{" +
                "response_code=" + response_code +
                ", total=" + total +
                ", positives=" + positives +
                ", scan_date='" + scan_date + '\'' +
                ", verbose_msg='" + verbose_msg + '\'' +
                '}';
    }
}
