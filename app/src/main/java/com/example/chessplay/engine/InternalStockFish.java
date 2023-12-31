

package com.example.chessplay.engine;

import android.os.Environment;

import com.example.chessplay.EngineOptions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/** Stockfish engine running as process, started from assets resource. */
public class InternalStockFish extends ExternalEngine {
    private static final String defaultNet = "nn-6877cd24400e.nnue";
    private static final String netOption = "evalfile";
    private File defaultNetFile; // To get the full path of the copied default network file

    public InternalStockFish(Report report, String workDir) {
        super("", workDir, report);
    }


    @Override
    protected File getOptionsFile() {
        File extDir = Environment.getExternalStorageDirectory();
        return new File(extDir, "/DroidFish/uci/stockfish.ini");
    }

    @Override
    protected boolean editableOption(String name) {
        name = name.toLowerCase(Locale.US);
        if (!super.editableOption(name))
            return false;
        if (name.equals("skill level") || name.equals("write debug log") ||
            name.equals("write search log"))
            return false;
        return true;
    }

    private long readCheckSum(File f) {
        try (InputStream is = new FileInputStream(f);
             DataInputStream dis = new DataInputStream(is)) {
            return dis.readLong();
        } catch (IOException e) {
            return 0;
        }
    }

    private void writeCheckSum(File f, long checkSum) {
        try (OutputStream os = new FileOutputStream(f);
             DataOutputStream dos = new DataOutputStream(os)) {
            dos.writeLong(checkSum);
        } catch (IOException ignore) {
        }
    }

    private long computeAssetsCheckSum(String sfExe) {

        try (InputStream is = context.getAssets().open(sfExe)) {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] buf = new byte[8192];
            while (true) {
                int len = is.read(buf);
                if (len <= 0)
                    break;
                md.update(buf, 0, len);
            }
            byte[] digest = md.digest(new byte[]{0});
            long ret = 0;
            for (int i = 0; i < 8; i++) {
                ret ^= ((long)digest[i]) << (i * 8);
            }
            return ret;
        } catch (IOException e) {
            return -1;
        } catch (NoSuchAlgorithmException e) {
            return -1;
        }
    }

    @Override
    protected String copyFile(File from, File exeDir) throws IOException {
        File to = new File(exeDir, "engine.exe");
        final String sfExe = EngineUtil.internalStockFishName();

        // The checksum test is to avoid writing to /data unless necessary,
        // on the assumption that it will reduce memory wear.
        long oldCSum = readCheckSum(new File(internalSFPath()));
        long newCSum = computeAssetsCheckSum(sfExe);
        if (oldCSum != newCSum) {
            copyAssetFile(sfExe, to);
            writeCheckSum(new File(internalSFPath()), newCSum);
        }
        copyNetFile(exeDir);
        return to.getAbsolutePath();
    }

    /** Copy the Stockfish default network file to "exeDir" if it is not already there. */
    private void copyNetFile(File exeDir) throws IOException {
        defaultNetFile = new File(exeDir, defaultNet);
        if (defaultNetFile.exists())
            return;
        File tmpFile = new File(exeDir, defaultNet + ".tmp");
        copyAssetFile(defaultNet, tmpFile);
        if (!tmpFile.renameTo(defaultNetFile))
            throw new IOException("Rename failed");
    }

    /** Copy a file resource from the AssetManager to the file system,
     *  so it can be used by native code like the Stockfish engine. */
    private void copyAssetFile(String assetName, File targetFile) throws IOException {
        try (InputStream is = context.getAssets().open(assetName);
             OutputStream os = new FileOutputStream(targetFile)) {
            byte[] buf = new byte[8192];
            while (true) {
                int len = is.read(buf);
                if (len <= 0)
                    break;
                os.write(buf, 0, len);
            }
        }
    }

    /** Return true if file "f" should be kept in the exeDir directory.
     *  It would be inefficient to remove the network file every time
     *  an engine different from Stockfish is used, so this is a static
     *  check performed for all engines. */
    public static boolean keepExeDirFile(File f) {
        return defaultNet.equals(f.getName());
    }

    @Override
    public void initOptions(EngineOptions engineOptions) {
        super.initOptions(engineOptions);
        UCIOptions.OptionBase opt = getUCIOptions().getOption(netOption);
        if (opt != null)
            setOption(netOption, opt.getStringValue());
    }

    /** Handles setting the EvalFile UCI option to a full path if needed,
     *  pointing to the network file embedded in DroidFish. */
    @Override
    public boolean setOption(String name, String value) {
        if (name.toLowerCase(Locale.US).equals(netOption) &&
            (defaultNet.equals(value) || value.isEmpty())) {
            getUCIOptions().getOption(name).setFromString(value);
            value = defaultNetFile.getAbsolutePath();
            writeLineToEngine(String.format(Locale.US, "setoption name %s value %s", name, value));
            return true;
        }
        return super.setOption(name, value);
    }
}
