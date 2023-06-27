

package com.example.chessplay;

/** Engine options, including endgame tablebase probing options. */
public final class EngineOptions {
    public int hashMB;          // Engine hash table size in MB
    public boolean unSafeHash;  // True if allocating very large hash is allowed
    public boolean hints;       // Hints when playing/analyzing
    public boolean hintsEdit;   // Hints in "edit board" mode
    public boolean rootProbe;   // Only search optimal moves at root
    public boolean engineProbe; // Let engine use EGTB
    String gtbPath;             // GTB directory path
    String gtbPathNet;          // GTB directory path for network engines
    String rtbPath;             // Syzygy directory path
    String rtbPathNet;          // Syzygy directory path for network engines
    public String networkID;    // host+port network settings
    public String workDir;      // Working directory for engine process

    public EngineOptions() {
        hashMB = 16;
        unSafeHash = false;
        hints = false;
        hintsEdit = false;
        rootProbe = false;
        engineProbe = false;
        gtbPath = "";
        gtbPathNet = "";
        rtbPath = "";
        rtbPathNet = "";
        networkID = "";
        workDir = "";
    }

    public EngineOptions(EngineOptions other) {
        hashMB = other.hashMB;
        unSafeHash = other.unSafeHash;
        hints = other.hints;
        hintsEdit = other.hintsEdit;
        rootProbe = other.rootProbe;
        engineProbe = other.engineProbe;
        gtbPath = other.gtbPath;
        gtbPathNet = other.gtbPathNet;
        rtbPath = other.rtbPath;
        rtbPathNet = other.rtbPathNet;
        networkID = other.networkID;
        workDir = other.workDir;
    }

    /** Get the GTB path for an engine. */
    public String getEngineGtbPath(boolean networkEngine) {
        if (!engineProbe)
            return "";
        return networkEngine ? gtbPathNet : gtbPath;
    }

    /** Get the RTB path for an engine. */
    public String getEngineRtbPath(boolean networkEngine) {
        if (!engineProbe)
            return "";
        return networkEngine ? rtbPathNet : rtbPath;
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || (o.getClass() != this.getClass()))
            return false;
        EngineOptions other = (EngineOptions)o;

        return ((hashMB == other.hashMB) &&
                (unSafeHash == other.unSafeHash) &&
                (hints == other.hints) &&
                (hintsEdit == other.hintsEdit) &&
                (rootProbe == other.rootProbe) &&
                (engineProbe == other.engineProbe) &&
                gtbPath.equals(other.gtbPath) &&
                gtbPathNet.equals(other.gtbPathNet) &&
                rtbPath.equals(other.rtbPath) &&
                rtbPathNet.equals(other.rtbPathNet) &&
                networkID.equals(other.networkID) &&
                workDir.equals(other.workDir));
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
