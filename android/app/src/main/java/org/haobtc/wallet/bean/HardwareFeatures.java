package org.haobtc.wallet.bean;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class HardwareFeatures {
    /**
     * vendor : trezor.io
     * bootloader_mode:
     * major_version : 1
     * minor_version : 8
     * patch_version : 4
     * device_id : 80F417CB404FC5186EBE3899
     * pin_protection : true
     * passphrase_protection : false
     * language : en-US
     * label: liyan
     * initialized : true
     * revision : 901a3ef288fff3be8cabc0af5efd204c8550ceb4
     * bootloader_hash : 58f0235afaee90cc7640cae76ab92f28d2516e82de4e7b5252b5d7e89e619308
     * pin_cached : true
     * passphrase_cached : false
     * needs_backup : true
     * model : 1
     * unfinished_backup : false
     * no_backup : false
     * capabilities : [1,2,5,7,8,10,12,14]
     * 'ble_name': 'BixinKEY1013113806',
     * 'ble_ver': '1.0.1'
     */

    @SerializedName("vendor")
    private String vendor;
    @SerializedName("major_version")
    private int majorVersion;
    @SerializedName("minor_version")
    private int minorVersion;
    @SerializedName("patch_version")
    private int patchVersion;
    @SerializedName("device_id")
    private String deviceId;
    @SerializedName("pin_protection")
    private boolean pinProtection;
    @SerializedName("passphrase_protection")
    private boolean passphraseProtection;
    @SerializedName("language")
    private String language;
    @SerializedName("label")
    private String label;
    @SerializedName("initialized")
    private boolean initialized;
    @SerializedName("revision")
    private String revision;
    @SerializedName("bootloader_hash")
    private String bootloaderHash;
    @SerializedName("pin_cached")
    private boolean pinCached;
    @SerializedName("passphrase_cached")
    private boolean passphraseCached;
    @SerializedName("needs_backup")
    private boolean needsBackup;
    @SerializedName("model")
    private String model;
    @SerializedName("unfinished_backup")
    private boolean unfinishedBackup;
    @SerializedName("no_backup")
    private boolean noBackup;
    @SerializedName("capabilities")
    private List<Integer> capabilities;
    @SerializedName("ble_name")
    private String bleName;
    @SerializedName("ble_ver")
    private String bleVer;
    @SerializedName("bootloader_mode")
    private boolean bootloaderMode;

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public void setMajorVersion(int majorVersion) {
        this.majorVersion = majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    public int getPatchVersion() {
        return patchVersion;
    }

    public void setPatchVersion(int patchVersion) {
        this.patchVersion = patchVersion;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isPinProtection() {
        return pinProtection;
    }

    public void setPinProtection(boolean pinProtection) {
        this.pinProtection = pinProtection;
    }

    public boolean isPassphraseProtection() {
        return passphraseProtection;
    }

    public void setPassphraseProtection(boolean passphraseProtection) {
        this.passphraseProtection = passphraseProtection;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getBootloaderHash() {
        return bootloaderHash;
    }

    public void setBootloaderHash(String bootloaderHash) {
        this.bootloaderHash = bootloaderHash;
    }

    public boolean isPinCached() {
        return pinCached;
    }

    public void setPinCached(boolean pinCached) {
        this.pinCached = pinCached;
    }

    public boolean isPassphraseCached() {
        return passphraseCached;
    }

    public void setPassphraseCached(boolean passphraseCached) {
        this.passphraseCached = passphraseCached;
    }

    public boolean isNeedsBackup() {
        return needsBackup;
    }

    public void setNeedsBackup(boolean needsBackup) {
        this.needsBackup = needsBackup;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public boolean isUnfinishedBackup() {
        return unfinishedBackup;
    }

    public void setUnfinishedBackup(boolean unfinishedBackup) {
        this.unfinishedBackup = unfinishedBackup;
    }

    public boolean isNoBackup() {
        return noBackup;
    }

    public void setNoBackup(boolean noBackup) {
        this.noBackup = noBackup;
    }

    public List<Integer> getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(List<Integer> capabilities) {
        this.capabilities = capabilities;
    }

    public void setBleName(String bleName) {
        this.bleName = bleName;
    }
    public String getBleName() {
        return bleName;
    }
    public String getBleVer() {
        return bleVer;
    }
    public void setBleVer(String bleVer) {
        this.bleVer = bleVer;
    }
    public boolean isBootloaderMode() {
        return bootloaderMode;
    }

    public void setBootloaderMode(boolean bootloaderMode) {
        this.bootloaderMode = bootloaderMode;
    }

    @NonNull
    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}