package cz.droidboy.wibeacon.range;

import android.net.wifi.ScanResult;
import android.support.annotation.Nullable;

import java.util.Set;

import cz.droidboy.wibeacon.util.ProximityUtils;
import cz.droidboy.wibeacon.util.WifiUtils;

/**
 * @author Jonas Sevcik
 */
public class ScanFilter {

    //private static final Pattern BSSID_PATTERN = Pattern.compile("([\\p{XDigit}]{2}:){5}[\\p{XDigit}]{2}");

    @Nullable
    private String mac;
    @Nullable
    private String ssid;
    @Nullable
    private Set<Integer> channels;
    @Nullable
    private Proximity proximity;

    /**
     * Filter parameters. Enter null for no filtering.
     *
     * @param mac      Ethernet MAC address, e.g., XX:XX:XX:XX:XX:XX where each X is a hex digit
     * @param ssid     service set identifier of the 802.11 network
     * @param channels Set of channel numbers
     * @throws IllegalArgumentException if {@code mac}'s length > 17 chars or {@code ssid}'s length > 32 chars
     */
    public ScanFilter(@Nullable String mac, @Nullable String ssid, @Nullable Set<Integer> channels, @Nullable Proximity proximity) {
        if (mac != null && mac.length() > 17) {
            throw new IllegalArgumentException("Mac longer than 17 chars");
        }

        if (ssid != null && ssid.length() > 32) {
            throw new IllegalArgumentException("SSID longer than 32 chars");
        }

        if (mac != null) {
            this.mac = mac.toLowerCase();
        }

        if (ssid != null) {
            this.ssid = ssid.toLowerCase();
        }

        this.channels = channels;
        this.proximity = proximity;
    }

    public boolean matches(ScanResult scanResult) {
        if (scanResult == null) {
            return false;
        }

        if (mac != null && !mac.equalsIgnoreCase(scanResult.BSSID)) {
            return false;
        }

        if (ssid != null && !ssid.equalsIgnoreCase(scanResult.SSID)) {
            return false;
        }

        if (channels != null && !channels.contains(WifiUtils.toChannel(scanResult.frequency))) {
            return false;
        }

        return !(proximity != null && proximity != ProximityUtils.getProximity(scanResult.level, scanResult.frequency));
    }

    public boolean matchesStart(ScanResult scanResult) {
        if (scanResult == null) {
            return false;
        }

        if (mac != null && !scanResult.BSSID.toLowerCase().startsWith(mac)) {
            return false;
        }

        if (ssid != null && !scanResult.SSID.toLowerCase().startsWith(ssid)) {
            return false;
        }

        if (channels != null && !channels.contains(WifiUtils.toChannel(scanResult.frequency))) {
            return false;
        }

        return !(proximity != null && proximity != ProximityUtils.getProximity(scanResult.level, scanResult.frequency));
    }

    public static class Builder {

        private String mac;
        private String ssid;
        private Set<Integer> channels;
        private Proximity proximity;

        public Builder setMac(String mac) {
            this.mac = mac;
            return this;
        }

        public Builder setSsid(String ssid) {
            this.ssid = ssid;
            return this;
        }

        public Builder setChannels(Set<Integer> channels) {
            this.channels = channels;
            return this;
        }

        public Builder setProximity(Proximity proximity) {
            this.proximity = proximity;
            return this;
        }

        public ScanFilter build() {
            return new ScanFilter(mac, ssid, channels, proximity);
        }
    }
}
