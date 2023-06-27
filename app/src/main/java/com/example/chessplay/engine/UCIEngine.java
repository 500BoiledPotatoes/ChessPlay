

package com.example.chessplay.engine;

import com.example.chessplay.EngineOptions;

import java.util.Map;

public interface UCIEngine {

    /** For reporting engine error messages. */
    public interface Report {
        /** Report error message to GUI. */
        void reportError(String errMsg);
    }

    /** Start engine. */
    void initialize();

    /** Initialize default options. */
    void initOptions(EngineOptions engineOptions);

    /** Read UCI options from .ini file and send them to the engine. */
    void applyIniFile();

    /** Set engine UCI options. */
    boolean setUCIOptions(Map<String,String> uciOptions);

    /** Save non-default UCI option values to file. */
    void saveIniFile(UCIOptions options);

    /** Get engine UCI options. */
    UCIOptions getUCIOptions();

    /** Return true if engine options have correct values.
     * If false is returned, engine will be restarted. */
    boolean optionsOk(EngineOptions engineOptions);

    /** Shut down engine. */
    void shutDown();

    /**
     * Read a line from the engine.
     * @param timeoutMillis Maximum time to wait for data.
     * @return The line, without terminating newline characters,
     *         or empty string if no data available,
     *         or null if I/O error.
     */
    String readLineFromEngine(int timeoutMillis);

    /** Write a line to the engine. \n will be added automatically. */
    void writeLineToEngine(String data);

    /** Temporarily set the engine Elo strength to use for the next search.
     *  Integer.MAX_VALUE means full strength. */
    void setEloStrength(int elo);

    /** Set an engine integer option. */
    void setOption(String name, int value);

    /** Set an engine boolean option. */
    void setOption(String name, boolean value);

    /** Set an engine option. If the option is not a string option,
     * value is converted to the correct type.
     * @return True if the option was changed. */
    boolean setOption(String name, String value);

    /** Clear list of supported options. */
    void clearOptions();

    /** Register an option as supported by the engine.
     * @param tokens  The UCI option line sent by the engine, split in words. */
    UCIOptions.OptionBase registerOption(String[] tokens);
}
