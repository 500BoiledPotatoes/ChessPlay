

package com.example.chessplay.engine;


import java.io.File;
import java.io.IOException;
import java.util.List;

/** Engine imported from a different android app, resolved using the open exchange format. */
public class OpenExchangeEngine extends ExternalEngine {

    public OpenExchangeEngine(String engine, String workDir, Report report) {
        super(engine, workDir, report);
    }

    @Override
    protected String copyFile(File from, File exeDir) throws IOException {
        new File(internalSFPath()).delete();
        ChessEngineResolver resolver = new ChessEngineResolver(context);
        List<ChessEngine> engines = resolver.resolveEngines();
        for (ChessEngine engine : engines) {
            if (EngineUtil.openExchangeFileName(engine).equals(from.getName())) {
                File engineFile = engine.copyToFiles(context.getContentResolver(), exeDir);
                return engineFile.getAbsolutePath();
            }
        }
        throw new IOException("Engine not found");
    }
}
