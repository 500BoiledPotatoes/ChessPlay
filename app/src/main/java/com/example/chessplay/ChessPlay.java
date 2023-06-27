package com.example.chessplay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.example.chessplay.engine.ChessEngine;
import com.example.chessplay.engine.ChessEngineResolver;
import com.example.chessplay.engine.DroidComputerPlayer.EloData;
import com.example.chessplay.engine.EngineUtil;
import com.example.chessplay.gamelogic.ChessController;
import com.example.chessplay.gamelogic.GameTree.Node;
import com.example.chessplay.gamelogic.Move;
import com.example.chessplay.gamelogic.Position;
import com.example.chessplay.gamelogic.TextIO;
import com.example.chessplay.gamelogic.TimeControlData;
import com.example.chessplay.view.MoveListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UpdateListener;

@SuppressLint("ClickableViewAccessibility")
public class ChessPlay extends Activity
                       implements GUIInterface,
                                  ActivityCompat.OnRequestPermissionsResultCallback {
    private ChessBoardPlay cb;
    ChessController ctrl = null;
    private boolean mWhiteBasedScores;
    GameMode gameMode;
    private boolean mPonderMode;
    private int timeControl;
    private int movesPerSession;
    private int timeIncrement;
    private String playerName;
    private boolean boardFlipped;
    private boolean autoSwapSides;
    private boolean playerNameFlip;
    private boolean discardVariations;
    private String steps;

    private TextView status;
    private ScrollView moveListScroll;
    private MoveListView moveList;
    private TextView thinking;
    private ImageButton engineButton, flipButton, modeButton, undoButton, redoButton;
    private TextView whiteTitleText, blackTitleText, engineTitleText;
    private View secondTitleLine;
    private TextView whiteFigText, blackFigText, summaryTitleText;
    private Dialog moveListMenuDlg;

    private DrawerLayout drawerLayout;
    private ListView leftDrawer;
    private ListView rightDrawer;

    private SharedPreferences settings;
    private ObjectCache cache;

    boolean dragMoveEnabled;
    float scrollSensitivity;
    boolean invertScrollDirection;
    boolean scrollGames;
    private boolean autoScrollMoveList;

    private boolean leftHanded;
    private boolean vibrateEnabled;
    private boolean animateMoves;
    private boolean autoScrollTitle;
    private boolean showVariationLine;


    private enum PermissionState {
        UNKNOWN,
        REQUESTED,
        GRANTED,
        DENIED
    }
    private PermissionState storagePermission = PermissionState.UNKNOWN;
    private PGNOptions pgnOptions = new PGNOptions();

    private long lastVisibleMillis;
    private long lastComputationMillis;

    private PgnScreenText gameTextListener;

    private Typeface figNotation;
    private Typeface defaultThinkingListTypeFace;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        cache = new ObjectCache();

        setWakeLock(false);

        figNotation = Typeface.createFromAsset(getAssets(), "fonts/ChessNotation.otf");
        setPieceNames(PGNOptions.PT_LOCAL);
        initUI();

        gameTextListener = new PgnScreenText(this, pgnOptions);
        moveList.setOnLinkClickListener(gameTextListener);
        if (ctrl != null)
            ctrl.shutdownEngine();
        ctrl = new ChessController(this, gameTextListener, pgnOptions);
        readPrefs(false);
        TimeControlData tcData = new TimeControlData();
        tcData.setTimeControl(timeControl, movesPerSession, timeIncrement);
        ctrl.newGame(gameMode, tcData);
        {
            byte[] data = null;
            int version = 1;
            if (savedInstanceState != null) {
                byte[] token = savedInstanceState.getByteArray("gameStateT");
                if (token != null)
                    data = cache.retrieveBytes(token);
                version = savedInstanceState.getInt("gameStateVersion", version);
            } else {
                String dataStr = settings.getString("gameState", null);
                version = settings.getInt("gameStateVersion", version);
                if (dataStr != null)
                    data = strToByteArr(dataStr);
            }
            if (data != null)
                ctrl.fromByteArray(data, version);
        }
        ctrl.setGuiPaused(true);
        ctrl.setGuiPaused(false);
        ctrl.startGame();
    }

    private static final String figurinePieceNames = PieceFontInfo.NOTATION_PAWN   + " " +
                                                     PieceFontInfo.NOTATION_KNIGHT + " " +
                                                     PieceFontInfo.NOTATION_BISHOP + " " +
                                                     PieceFontInfo.NOTATION_ROOK   + " " +
                                                     PieceFontInfo.NOTATION_QUEEN  + " " +
                                                     PieceFontInfo.NOTATION_KING;

    private void setPieceNames(int pieceType) {
        if (pieceType == PGNOptions.PT_FIGURINE) {
            TextIO.setPieceNames(figurinePieceNames);
        } else {
            TextIO.setPieceNames(getString(R.string.piece_names));
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] permissions, int[] results) {
        if (storagePermission == PermissionState.REQUESTED) {
            if ((results.length > 0) && (results[0] == PackageManager.PERMISSION_GRANTED))
                storagePermission = PermissionState.GRANTED;
            else
                storagePermission = PermissionState.DENIED;
        }
    }

    /** Return true if the WRITE_EXTERNAL_STORAGE permission has been granted. */
    private boolean storageAvailable() {
        return storagePermission == PermissionState.GRANTED;
    }

    private byte[] strToByteArr(String str) {
        if (str == null)
            return null;
        int nBytes = str.length() / 2;
        byte[] ret = new byte[nBytes];
        for (int i = 0; i < nBytes; i++) {
            int c1 = str.charAt(i * 2) - 'A';
            int c2 = str.charAt(i * 2 + 1) - 'A';
            ret[i] = (byte)(c1 * 16 + c2);
        }
        return ret;
    }

    private String byteArrToString(byte[] data) {
        if (data == null)
            return null;
        StringBuilder ret = new StringBuilder(32768);
        for (int b : data) {
            if (b < 0) b += 256;
            char c1 = (char)('A' + (b / 16));
            char c2 = (char)('A' + (b & 15));
            ret.append(c1);
            ret.append(c2);
        }
        return ret.toString();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        reInitUI();
    }

    /** Re-initialize UI when layout should change because of rotation or handedness change. */
    private void reInitUI() {
        ChessBoardPlay oldCB = cb;
        String statusStr = status.getText().toString();
        initUI();
        readPrefs(true);
        cb.setPosition(oldCB.pos);
        cb.setFlipped(oldCB.flipped);
        cb.setDrawSquareLabels(oldCB.drawSquareLabels);
        cb.oneTouchMoves = oldCB.oneTouchMoves;
        cb.toggleSelection = oldCB.toggleSelection;
        cb.highlightLastMove = oldCB.highlightLastMove;
        setSelection(oldCB.selectedSquare);
        cb.userSelectedSquare = oldCB.userSelectedSquare;
        setStatusString(statusStr);
        moveList.setOnLinkClickListener(gameTextListener);
        moveListUpdated();
        ctrl.updateRemainingTime();
        ctrl.updateMaterialDiffList();
    }

    /** Return true if the current orientation is landscape. */
    private boolean landScapeView() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
    
    /** Return true if left-handed layout should be used. */
    private boolean leftHandedView() {
        return settings.getBoolean("leftHanded", false) && landScapeView();
    }

    private void initUI() {
        leftHanded = leftHandedView();
        setContentView(leftHanded ? R.layout.main_left_handed : R.layout.chess_play);
        overrideViewAttribs();

        // title lines need to be regenerated every time due to layout changes (rotations)
        View firstTitleLine = findViewById(R.id.first_title_line);
        secondTitleLine = findViewById(R.id.second_title_line);
        whiteTitleText = findViewById(R.id.white_clock);
        whiteTitleText.setSelected(true);
        blackTitleText = findViewById(R.id.black_clock);
        blackTitleText.setSelected(true);
        engineTitleText = findViewById(R.id.title_text);
        whiteFigText = findViewById(R.id.white_pieces);
        whiteFigText.setTypeface(figNotation);
        whiteFigText.setSelected(true);
        whiteFigText.setTextColor(whiteTitleText.getTextColors());
        blackFigText = findViewById(R.id.black_pieces);
        blackFigText.setTypeface(figNotation);
        blackFigText.setSelected(true);
        blackFigText.setTextColor(blackTitleText.getTextColors());
        summaryTitleText = findViewById(R.id.title_text_summary);

        status = findViewById(R.id.status);
        moveListScroll = findViewById(R.id.scrollView);
        moveList = findViewById(R.id.moveList);
        thinking = findViewById(R.id.thinking);
        defaultThinkingListTypeFace = thinking.getTypeface();
        status.setFocusable(false);
        moveListScroll.setFocusable(false);
        moveList.setFocusable(false);
        thinking.setFocusable(false);

        initDrawers();

        class ClickListener implements OnClickListener, OnTouchListener {
            private float touchX = -1;
            @Override
            public void onClick(View v) {
                boolean left = touchX <= v.getWidth() / 2.0;
                drawerLayout.openDrawer(left ? Gravity.LEFT : Gravity.RIGHT);
                touchX = -1;
            }

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                touchX = event.getX();
                return false;
            }
        }
        ClickListener listener = new ClickListener();
        firstTitleLine.setOnClickListener(listener);
        firstTitleLine.setOnTouchListener(listener);
        secondTitleLine.setOnClickListener(listener);
        secondTitleLine.setOnTouchListener(listener);

        cb = findViewById(R.id.chessboard);
        cb.setFocusable(true);
        cb.requestFocus();
        cb.setClickable(true);

        ChessBoardPlayListener cbpListener = new ChessBoardPlayListener(this, cb);
        cb.setOnTouchListener(cbpListener);

        moveList.setOnLongClickListener(v -> {
            reShowDialog(MOVELIST_MENU_DIALOG);
            return true;
        });


        engineButton = findViewById(R.id.engineButton);
        engineButton.setOnClickListener(v -> showDialog(SELECT_ENGINE_BUTTON));
        engineButton.setOnLongClickListener(v -> {
            drawerLayout.openDrawer(Gravity.LEFT);
            return true;
        });

        flipButton = findViewById(R.id.flipButton);
        flipButton.setOnClickListener(v -> {
            boardFlipped = !cb.flipped;
            setBooleanPref("boardFlipped", boardFlipped);
            cb.setFlipped(boardFlipped);
        });


        modeButton = findViewById(R.id.modeButton);
        modeButton.setOnClickListener(v -> showDialog(GAME_MODE_DIALOG));
        modeButton.setOnLongClickListener(v -> {
            drawerLayout.openDrawer(Gravity.LEFT);
            return true;
        });
        undoButton = findViewById(R.id.undoButton);
        undoButton.setOnClickListener(v -> {
            ctrl.undoMove();
        });
        redoButton = findViewById(R.id.redoButton);
        redoButton.setOnClickListener(v -> {
            ctrl.redoMove();
        });
    }

    private static final int serializeVersion = 4;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (ctrl != null) {
            byte[] data = ctrl.toByteArray();
            byte[] token = data == null ? null : cache.storeBytes(data);
            outState.putByteArray("gameStateT", token);
            outState.putInt("gameStateVersion", serializeVersion);
        }
    }

    @Override
    protected void onResume() {
        lastVisibleMillis = 0;
        if (ctrl != null)
            ctrl.setGuiPaused(false);
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (ctrl != null) {
            ctrl.setGuiPaused(true);
            byte[] data = ctrl.toByteArray();
            Editor editor = settings.edit();
            String dataStr = byteArrToString(data);
            editor.putString("gameState", dataStr);
            editor.putInt("gameStateVersion", serializeVersion);
            editor.apply();
        }
        lastVisibleMillis = System.currentTimeMillis();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (ctrl != null)
            ctrl.shutdownEngine();
        super.onDestroy();
    }

    private int getIntSetting(String settingName, int defaultValue) {
        String tmp = settings.getString(settingName, String.format(Locale.US, "%d", defaultValue));
        return Integer.parseInt(tmp);
    }

    private void readPrefs(boolean restartIfLangChange) {
        int modeNr = getIntSetting("gameMode", 1);
        gameMode = new GameMode(modeNr);
        String oldPlayerName = playerName;
        playerName = settings.getString("playerName", "Player");
        boardFlipped = settings.getBoolean("boardFlipped", false);
        autoSwapSides = settings.getBoolean("autoSwapSides", false);
        playerNameFlip = settings.getBoolean("playerNameFlip", true);
        setBoardFlip(!playerName.equals(oldPlayerName));
        boolean drawSquareLabels = settings.getBoolean("drawSquareLabels", false);
        cb.setDrawSquareLabels(drawSquareLabels);
        cb.oneTouchMoves = settings.getBoolean("oneTouchMoves", false);
        cb.toggleSelection = getIntSetting("squareSelectType", 0) == 1;
        cb.highlightLastMove = settings.getBoolean("highlightLastMove", true);
        mWhiteBasedScores = settings.getBoolean("whiteBasedScores", false);

        String engine = settings.getString("engine", "stockfish");
        setEngine(engine);


        timeControl = getIntSetting("timeControl", 120000);
        movesPerSession = getIntSetting("movesPerSession", 60);
        timeIncrement = getIntSetting("timeIncrement", 0);

        dragMoveEnabled = settings.getBoolean("dragMoveEnabled", true);
        scrollSensitivity = Float.parseFloat(settings.getString("scrollSensitivity", "2"));
        invertScrollDirection = settings.getBoolean("invertScrollDirection", false);
        scrollGames = settings.getBoolean("scrollGames", false);
        autoScrollMoveList = settings.getBoolean("autoScrollMoveList", true);
        discardVariations = settings.getBoolean("discardVariations", false);
        Util.setFullScreenMode(this, settings);
        boolean useWakeLock = settings.getBoolean("wakeLock", false);
        setWakeLock(useWakeLock);

        int fontSize = getIntSetting("fontSize", 12);
        int statusFontSize = fontSize;
        Configuration config = getResources().getConfiguration();
        if (config.orientation == Configuration.ORIENTATION_PORTRAIT)
            statusFontSize = Math.min(statusFontSize, 16);
        status.setTextSize(statusFontSize);

        vibrateEnabled = settings.getBoolean("vibrateEnabled", false);
        animateMoves = settings.getBoolean("animateMoves", true);
        autoScrollTitle = settings.getBoolean("autoScrollTitle", true);
        updateButtons();

        ColorTheme.instance().readColors(settings);
        PieceSet.instance().readPrefs(settings);
        cb.setColors();
        overrideViewAttribs();

        gameTextListener.clear();
        setPieceNames(pgnOptions.view.pieceType);
        // update the typeset in case of a change anyway, cause it could occur
        // as well in rotation
        setFigurineNotation(pgnOptions.view.pieceType == PGNOptions.PT_FIGURINE, fontSize);

        boolean showMaterialDiff = settings.getBoolean("materialDiff", false);
        secondTitleLine.setVisibility(showMaterialDiff ? View.VISIBLE : View.GONE);
    }

    private void overrideViewAttribs() {
        Util.overrideViewAttribs(findViewById(R.id.main));
    }


    private void setFigurineNotation(boolean displayAsFigures, int fontSize) {
        if (displayAsFigures) {
            // increase the font cause it has different kerning and looks small
            float increaseFontSize = fontSize * 1.1f;
            moveList.setTypeface(figNotation, increaseFontSize);
            thinking.setTypeface(figNotation);
            thinking.setTextSize(increaseFontSize);
        } else {
            moveList.setTypeface(null, fontSize);
            thinking.setTypeface(defaultThinkingListTypeFace);
            thinking.setTextSize(fontSize);
        }
    }

    private void updateButtons() {
        Resources r = getResources();
        int bWidth  = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 36, r.getDisplayMetrics()));
        int bHeight = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, r.getDisplayMetrics()));
        SVG svg = null;
        try {
            svg = SVG.getFromResource(getResources(), R.raw.touch);
        } catch (SVGParseException ignore) {
        }
        setButtonData(flipButton, bWidth, bHeight, R.raw.flip, svg);
        setButtonData(engineButton, bWidth, bHeight, R.raw.engine, svg);
        setButtonData(modeButton, bWidth, bHeight, R.raw.mode, svg);
        setButtonData(undoButton, bWidth, bHeight, R.raw.left, svg);
        setButtonData(redoButton, bWidth, bHeight, R.raw.right, svg);
    }

    @SuppressWarnings("deprecation")
    private void setButtonData(ImageButton button, int bWidth, int bHeight,
                                     int svgResId, SVG touched) {
        SVG svg = null;
        try {
            svg = SVG.getFromResource(getResources(), svgResId);
        } catch (SVGParseException ignore) {
        }
        button.setBackgroundDrawable(new SVGPictureDrawable(svg));

        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[]{android.R.attr.state_pressed}, new SVGPictureDrawable(touched));
        button.setImageDrawable(sld);

        LayoutParams lp = button.getLayoutParams();
        lp.height = bHeight;
        lp.width = bWidth;
        button.setLayoutParams(lp);
        button.setPadding(0,0,0,0);
        button.setScaleType(ScaleType.FIT_XY);
    }

    @SuppressLint("Wakelock")
    private synchronized void setWakeLock(boolean enableLock) {
        if (enableLock)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setEngine(String engine) {
        if (!storageAvailable()) {
            if (!"stockfish".equals(engine) && !"cuckoochess".equals(engine))
                engine = "stockfish";
        }
        ctrl.setEngine(engine);
        setEngineTitle(engine, ctrl.eloData().getEloToUse());
    }

    private void setEngineTitle(String engine, int elo) {
        String eName = "";
        if (EngineUtil.isOpenExchangeEngine(engine)) {
            String engineFileName = new File(engine).getName();
            ChessEngineResolver resolver = new ChessEngineResolver(this);
            List<ChessEngine> engines = resolver.resolveEngines();
            for (ChessEngine ce : engines) {
                if (EngineUtil.openExchangeFileName(ce).equals(engineFileName)) {
                    eName = ce.getName();
                    break;
                }
            }
        } else if (engine.contains("/")) {
            int idx = engine.lastIndexOf('/');
            eName = engine.substring(idx + 1);
        } else {
            eName = getString("cuckoochess".equals(engine) ?
                              R.string.cuckoochess_engine :
                              R.string.stockfish_engine);
        }
        if (ctrl != null)
            if (elo != Integer.MAX_VALUE)
                eName = String.format(Locale.US, "%s: %d", eName, elo);
        engineTitleText.setText(eName);
    }

    /** Update center field in second header line. */
    public final void updateTimeControlTitle() {
        int[] tmpInfo = ctrl.getTimeLimit();
        StringBuilder sb = new StringBuilder();
        int tc = tmpInfo[0];
        int mps = tmpInfo[1];
        int inc = tmpInfo[2];
        if (mps > 0) {
            sb.append(mps);
            sb.append("/");
        }
        sb.append(timeToString(tc));
        if ((inc > 0) || (mps <= 0)) {
            sb.append("+");
            sb.append(tmpInfo[2] / 1000);
        }
        summaryTitleText.setText(sb.toString());
    }

    @Override
    public void updateEngineTitle(int elo) {
        String engine = settings.getString("engine", "stockfish");
        setEngineTitle(engine, elo);
    }

    @Override
    public void updateMaterialDifferenceTitle(Util.MaterialDiff diff) {
        whiteFigText.setText(diff.white);
        blackFigText.setText(diff.black);
    }

    private class DrawerItem {
        DrawerItemId id;
        private int resId; // Item string resource id

        DrawerItem(DrawerItemId id, int resId) {
            this.id = id;
            this.resId = resId;
        }

        @Override
        public String toString() {
            return getString(resId);
        }
    }

    private enum DrawerItemId {
        NEW_GAME,
        SET_STRENGTH,
        RESIGN,
        FORCE_MOVE
    }

    /** Initialize the drawer part of the user interface. */
    private void initDrawers() {
        drawerLayout = findViewById(R.id.drawer_layout);
        leftDrawer = findViewById(R.id.left_drawer);
        rightDrawer = findViewById(R.id.right_drawer);

        final DrawerItem[] leftItems = new DrawerItem[] {
            new DrawerItem(DrawerItemId.NEW_GAME, R.string.option_new_game),
            new DrawerItem(DrawerItemId.SET_STRENGTH, R.string.set_engine_strength),
        };
        leftDrawer.setAdapter(new ArrayAdapter<>(this,
                                                 R.layout.drawer_list_item,
                                                 leftItems));
        leftDrawer.setOnItemClickListener((parent, view, position, id) -> {
            DrawerItem di = leftItems[position];
            handleDrawerSelection(di.id);
        });

        final DrawerItem[] rightItems = new DrawerItem[] {
            new DrawerItem(DrawerItemId.RESIGN, R.string.option_resign_game),
            new DrawerItem(DrawerItemId.FORCE_MOVE, R.string.option_force_computer_move),
        };
        rightDrawer.setAdapter(new ArrayAdapter<>(this,
                                                  R.layout.drawer_list_item,
                                                  rightItems));
        rightDrawer.setOnItemClickListener((parent, view, position, id) -> {
            DrawerItem di = rightItems[position];
            handleDrawerSelection(di.id);
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        drawerLayout.openDrawer(Gravity.LEFT);
        return false;
    }

    /** React to a selection in the left/right drawers. */
    private void handleDrawerSelection(DrawerItemId id) {
        drawerLayout.closeDrawer(Gravity.LEFT);
        drawerLayout.closeDrawer(Gravity.RIGHT);
        leftDrawer.clearChoices();
        rightDrawer.clearChoices();

        switch (id) {
        case NEW_GAME:
            showDialog(NEW_GAME_DIALOG);
            break;
        case SET_STRENGTH:
            reShowDialog(SET_STRENGTH_DIALOG);
            break;
        case RESIGN:
            if (ctrl.humansTurn())
                ctrl.resignGame();
            break;
        case FORCE_MOVE:
            ctrl.stopSearch();
            break;
        }
    }


    private void newGameMode(int gameModeType) {
        Editor editor = settings.edit();
        String gameModeStr = String.format(Locale.US, "%d", gameModeType);
        editor.putString("gameMode", gameModeStr);
        editor.apply();
        gameMode = new GameMode(gameModeType);
        ctrl.setGameMode(gameMode);
    }

    private int nameMatchScore(String name, String match) {
        if (name == null)
            return 0;
        String lName = name.toLowerCase(Locale.US);
        String lMatch = match.toLowerCase(Locale.US);
        if (name.equals(match))
            return 6;
        if (lName.equals(lMatch))
            return 5;
        if (name.startsWith(match))
            return 4;
        if (lName.startsWith(lMatch))
            return 3;
        if (name.contains(match))
            return 2;
        if (lName.contains(lMatch))
            return 1;
        return 0;
    }

    private void setBoardFlip() {
        setBoardFlip(false);
    }


    private void setBooleanPref(String name, boolean value) {
        Editor editor = settings.edit();
        editor.putBoolean(name, value);
        editor.apply();
    }

    private void setBoardFlip(boolean matchPlayerNames) {
        boolean flipped = boardFlipped;
        if (playerNameFlip && matchPlayerNames && (ctrl != null)) {
            final TreeMap<String,String> headers = new TreeMap<>();
            ctrl.getHeaders(headers);
            int whiteMatch = nameMatchScore(headers.get("White"), playerName);
            int blackMatch = nameMatchScore(headers.get("Black"), playerName);
            if (( flipped && (whiteMatch > blackMatch)) ||
                (!flipped && (whiteMatch < blackMatch))) {
                flipped = !flipped;
                boardFlipped = flipped;
                setBooleanPref("boardFlipped", flipped);
            }
        }
        if (autoSwapSides) {
            if (gameMode.playerWhite() && gameMode.playerBlack()) {
                flipped = !cb.pos.whiteMove;
            } else if (gameMode.playerWhite()) {
                flipped = false;
            } else if (gameMode.playerBlack()) {
                flipped = true;
            }
        }
        cb.setFlipped(flipped);
    }

    @Override
    public void setSelection(int sq) {
        cb.setSelection(cb.highlightLastMove ? sq : -1);
        cb.userSelectedSquare = false;
    }


    @Override
    public void setStatus(GameStatus s) {
        User user = BmobUser.getCurrentUser(User.class);
        String str;
        switch (s.state) {
            case ALIVE:
                str = Integer.valueOf(s.moveNr).toString();
                if (s.white){
                    str += ". " + getString(R.string.whites_move);
                }
                else
                    str += "... " + getString(R.string.blacks_move);
                if (s.ponder) str += " (" + getString(R.string.ponder) + ")";
                if (s.thinking) str += " (" + getString(R.string.thinking) + ")";
                if (s.analyzing) str += " (" + getString(R.string.analyzing) + ")";
                break;
            case WHITE_MATE:
                str = getString(R.string.white_mate);
                if (ctrl.getGameMode().playerWhite()){
                    if (steps != null){
                        String[] records = steps.split(" ");
                        user.setWwin(user.getWwin()+1);
                        if (records.length/3 <= 10){
                            user.setsWin(user.getsWin()+1);
                        }
                        else if (records.length/3 > 10 && records.length/3 <=30){
                            user.setmWin(user.getmWin()+1);
                        }else{
                            user.seteWin(user.geteWin()+1);
                        }
                        if (records[1].equals("e4")){
                            user.setKingStart(user.getKingStart()+1);
                            if (!records[2].equals("e5")){
                                user.setSemiClosedStart(user.getSemiClosedStart()+1);
                            }
                        }
                        else if (records[1].equals("d4")){
                            user.setQueenStart(user.getQueenStart()+1);
                            if (!records[2].equals("e5")){
                                user.setSemiClosedStart(user.getSemiClosedStart()+1);
                            }
                        }
                        else{
                            user.setClosedStart(user.getClosedStart()+1);
                        }
                    }
                }
                else{
                    if (steps != null){
                        String[] records = steps.split(" ");
                        user.setwLose(user.getwLose()+1);
                        if (records.length/3 <= 10){
                            user.setsLose(user.getsLose()+1);
                        }
                        else if (records.length/3 > 10 && records.length/3 <=30){
                            user.setmLose(user.getmLose()+1);
                        }else{
                            user.seteLose(user.geteLose()+1);
                        }
                        if (records[1].equals("e4")){
                            user.setKingStart(user.getKingStart()+1);
                            if (!records[2].equals("e5")){
                                user.setSemiClosedStart(user.getSemiClosedStart()+1);
                            }
                        }
                        else if (records[1].equals("d4")){
                            user.setQueenStart(user.getQueenStart()+1);
                            if (!records[2].equals("e5")){
                                user.setSemiClosedStart(user.getSemiClosedStart()+1);
                            }
                        }
                        else{
                            user.setClosedStart(user.getClosedStart()+1);
                        }
                        user.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                } else {
                                    Log.e("error", e.getMessage());
                                }
                            }
                        });
                    }
                }

                break;
            case BLACK_MATE:
                str = getString(R.string.black_mate);
                if (ctrl.getGameMode().playerBlack()){
                    if (steps != null){
                        String[] records = steps.split(" ");
                        user.setBwin(user.getBwin()+1);
                        if (records.length/3 <= 10){
                            user.setsWin(user.getsWin()+1);
                        }
                        else if (records.length/3 > 10 && records.length/3 <=30){
                            user.setmWin(user.getmWin()+1);
                        }else{
                            user.seteWin(user.geteWin()+1);
                        }
                        if (records[1].equals("e4")){
                            user.setKingStart(user.getKingStart()+1);
                            if (!records[2].equals("e5")){
                                user.setSemiClosedStart(user.getSemiClosedStart()+1);
                            }
                        }
                        else if (records[1].equals("d4")){
                            user.setQueenStart(user.getQueenStart()+1);
                            if (!records[2].equals("e5")){
                                user.setSemiClosedStart(user.getSemiClosedStart()+1);
                            }
                        }
                        else{
                            user.setClosedStart(user.getClosedStart()+1);
                        }
                    }
                }
                else{
                    if (steps != null){
                        String[] records = steps.split(" ");
                        user.setbLose(user.getbLose()+1);
                        if (records.length/3 <= 10){
                            user.setsLose(user.getsLose()+1);
                        }
                        else if (records.length/3 > 10 && records.length/3 <=30){
                            user.setmLose(user.getmLose()+1);
                        }else{
                            user.seteLose(user.geteLose()+1);
                        }
                        if (records[1].equals("e4")){
                            user.setKingStart(user.getKingStart()+1);
                            if (!records[2].equals("e5")){
                                user.setSemiClosedStart(user.getSemiClosedStart()+1);
                            }
                        }
                        else if (records[1].equals("d4")){
                            user.setQueenStart(user.getQueenStart()+1);
                            if (!records[2].equals("e5")){
                                user.setSemiClosedStart(user.getSemiClosedStart()+1);
                            }
                        }
                        else{
                            user.setClosedStart(user.getClosedStart()+1);
                        }
                        user.update(new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if (e == null) {
                                } else {
                                    Log.e("error", e.getMessage());
                                }
                            }
                        });
                    }
                }

                break;
            case WHITE_STALEMATE:
            case BLACK_STALEMATE:
                str = getString(R.string.stalemate);
                break;
            case DRAW_REP: {
                str = getString(R.string.draw_rep);
                if (s.drawInfo.length() > 0)
                    str = str + " [" + s.drawInfo + "]";
                break;
            }
            case DRAW_50: {
                str = getString(R.string.draw_50);
                if (s.drawInfo.length() > 0)
                    str = str + " [" + s.drawInfo + "]";
                break;
            }
            case DRAW_NO_MATE:
                str = getString(R.string.draw_no_mate);
                break;
            case DRAW_AGREE:
                str = getString(R.string.draw_agree);
                break;
            case RESIGN_WHITE:
                str = getString(R.string.resign_white);
                if (steps != null){
                    String[] records = steps.split(" ");
                    user.setwLose(user.getwLose()+1);
                    if (records.length/3 <= 10){
                        user.setsLose(user.getsLose()+1);
                    }
                    else if (records.length/3 > 10 && records.length/3 <=30){
                        user.setmLose(user.getmLose()+1);
                    }else{
                        user.seteLose(user.geteLose()+1);
                    }
                    if (records[1].equals("e4")){
                        user.setKingStart(user.getKingStart()+1);
                        if (!records[2].equals("e5")){
                            user.setSemiClosedStart(user.getSemiClosedStart()+1);
                        }
                    }
                    else if (records[1].equals("d4")){
                        user.setQueenStart(user.getQueenStart()+1);
                        if (!records[2].equals("e5")){
                            user.setSemiClosedStart(user.getSemiClosedStart()+1);
                        }
                    }
                    else{
                        user.setClosedStart(user.getClosedStart()+1);
                    }
                    user.update(new UpdateListener() {
                        @Override
                        public void done(BmobException e) {
                            if (e == null) {
                            } else {
                                Log.e("error", e.getMessage());
                            }
                        }
                    });
                }
                break;
            case RESIGN_BLACK:
                str = getString(R.string.resign_black);
                if (steps != null){
                    String[] records = steps.split(" ");
                    user.setbLose(user.getbLose()+1);
                    if (records.length/3 <= 10){
                        user.setsLose(user.getsLose()+1);
                    }
                    else if (records.length/3 > 10 && records.length/3 <=30){
                        user.setmLose(user.getmLose()+1);
                    }
                    else{
                        user.seteLose(user.geteLose()+1);
                    }
                    if (records[1].equals("e4")){
                        user.setKingStart(user.getKingStart()+1);
                        if (!records[2].equals("e5")){
                            user.setSemiClosedStart(user.getSemiClosedStart()+1);
                        }
                    }
                    else if (records[1].equals("d4")){
                        user.setQueenStart(user.getQueenStart()+1);
                        if (!records[2].equals("e5") || !records[2].equals("d5")){
                            user.setSemiClosedStart(user.getSemiClosedStart()+1);
                        }
                    }
                    else{
                        user.setClosedStart(user.getClosedStart()+1);
                    }
                }
                user.update(new UpdateListener() {
                    @Override
                    public void done(BmobException e) {
                        if (e == null) {
                        } else {
                            Log.e("error", e.getMessage());
                        }
                    }
                });
                break;
            default:
                throw new RuntimeException();
        }
        setStatusString(str);
    }


    private void setStatusString(String str) {
        status.setText(str);
    }

    @Override
    public void moveListUpdated() {
        moveList.setText(gameTextListener.getText());
        steps = gameTextListener.getText().toString();
        int currPos = gameTextListener.getCurrPos();
        int line = moveList.getLineForOffset(currPos);
        if (line >= 0 && autoScrollMoveList) {
            int y = moveList.getLineStartY(line - 1);
            moveListScroll.scrollTo(0, y);
        }
    }

    @Override
    public boolean whiteBasedScores() {
        return mWhiteBasedScores;
    }

    @Override
    public boolean ponderMode() {
        return mPonderMode;
    }

    @Override
    public String playerName() {
        return playerName;
    }

    @Override
    public boolean discardVariations() {
        return discardVariations;
    }


    public void setAnimMove(Position sourcePos, Move move, boolean forward) {
        if (animateMoves && (move != null))
            cb.setAnimMove(sourcePos, move, forward);
    }

    private String variantStr = "";
    private ArrayList<Move> variantMoves = null;
    @Override
    public void setPosition(Position pos, String variantInfo, ArrayList<Move> variantMoves) {
        variantStr = variantInfo;
        this.variantMoves = variantMoves;
        cb.setPosition(pos);
        setBoardFlip();

    }

    static private final int PROMOTE_DIALOG = 0;
    static         final int BOARD_MENU_DIALOG = 1;
    static private final int GAME_MODE_DIALOG = 2;
    static private final int MOVELIST_MENU_DIALOG = 3;
    static private final int NEW_GAME_DIALOG = 4;
    static private final int SELECT_ENGINE_BUTTON = 5;
    static private final int SET_STRENGTH_DIALOG = 6;

    /** Remove and show a dialog. */
    void reShowDialog(int id) {
        removeDialog(id);
        showDialog(id);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case NEW_GAME_DIALOG:                return newGameDialog();
        case SET_STRENGTH_DIALOG:            return setStrengthDialog();
        case PROMOTE_DIALOG:                 return promoteDialog();
        case GAME_MODE_DIALOG:               return gameModeDialog();
        case SELECT_ENGINE_BUTTON:          return selectEngineDialog();
        }
        return null;
    }

    private Dialog newGameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.option_new_game);
        builder.setMessage(R.string.start_new_game);
        builder.setNeutralButton(R.string.yes, (dialog, which) -> startNewGame(2));
        builder.setNegativeButton(R.string.white, (dialog, which) -> startNewGame(0));
        builder.setPositiveButton(R.string.black, (dialog, which) -> startNewGame(1));
        return builder.create();
    }

    private Dialog setStrengthDialog() {
        EloStrengthSetter m = new EloStrengthSetter();
        return m.getDialog();
    }

    /** Handle user interface to set engine strength. */
    private class EloStrengthSetter {
        private final EloData eloData = ctrl.eloData();

        private CheckBox checkBox;
        private TextView eloLabel;
        private EditText editTxt;
        private SeekBar seekBar;

        private int progressToElo(int p) {
            return eloData.minElo + p;
        }

        private int eloToProgress(int elo) {
            return elo - eloData.minElo;
        }

        private void updateText(int elo) {
            String txt = Integer.valueOf(elo).toString();
            if (!txt.equals(editTxt.getText().toString())) {
                editTxt.setText(txt);
                editTxt.setSelection(txt.length());
            }
        }

        private void updateEnabledState(boolean enabled) {
            eloLabel.setEnabled(enabled);
            editTxt.setEnabled(enabled);
            seekBar.setEnabled(enabled);
        }

        public Dialog getDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(ChessPlay.this);
            builder.setTitle(R.string.set_engine_strength);
            View content = View.inflate(ChessPlay.this, R.layout.set_strength, null);
            builder.setView(content);

            checkBox = content.findViewById(R.id.strength_checkbox);
            eloLabel = content.findViewById(R.id.strength_elolabel);
            editTxt = content.findViewById(R.id.strength_edittext);
            seekBar = content.findViewById(R.id.strength_seekbar);

            checkBox.setChecked(eloData.limitStrength);
            seekBar.setMax(eloToProgress(eloData.maxElo));
            seekBar.setProgress(eloToProgress(eloData.elo));
            updateText(eloData.elo);
            updateEnabledState(eloData.limitStrength);

            checkBox.setOnCheckedChangeListener((button, isChecked) -> {
                updateEnabledState(isChecked);
            });
            seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    updateText(progressToElo(progress));
                }
            });
            editTxt.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String txt = editTxt.getText().toString();
                    try {
                        int elo = Integer.parseInt(txt);
                        int p = eloToProgress(elo);
                        if (p != seekBar.getProgress())
                            seekBar.setProgress(p);
                        updateText(progressToElo(p));
                    } catch (NumberFormatException ignore) {
                    }
                }
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override
                public void afterTextChanged(Editable s) { }
            });

            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                boolean limitStrength = checkBox.isChecked();
                int elo = progressToElo(seekBar.getProgress());
                ctrl.setStrength(limitStrength, elo);
            });

            return builder.create();
        }
    }

    private void startNewGame(int type) {
        if (type != 2) {
            int gameModeType = (type == 0) ? GameMode.PLAYER_WHITE : GameMode.PLAYER_BLACK;
            Editor editor = settings.edit();
            String gameModeStr = String.format(Locale.US, "%d", gameModeType);
            editor.putString("gameMode", gameModeStr);
            editor.apply();
            gameMode = new GameMode(gameModeType);
        }
        TimeControlData tcData = new TimeControlData();
        tcData.setTimeControl(timeControl, movesPerSession, timeIncrement);
        ctrl.newGame(gameMode, tcData);
        ctrl.startGame();
        setBoardFlip(true);
        updateEngineTitle(ctrl.eloData().getEloToUse()); // Game mode affects Elo setting
    }

    private Dialog promoteDialog() {
        final String[] items = {
            getString(R.string.queen), getString(R.string.rook),
            getString(R.string.bishop), getString(R.string.knight)
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.promote_pawn_to);
        builder.setItems(items, (dialog, item) -> ctrl.reportPromotePiece(item));
        return builder.create();
    }

    private Dialog selectEngineDialog() {
        final ArrayList<String> items = new ArrayList<>();
        final ArrayList<String> ids = new ArrayList<>();
        ids.add("stockfish"); items.add(getString(R.string.stockfish_engine));
        ids.add("cuckoochess"); items.add(getString(R.string.cuckoochess_engine));

        String currEngine = ctrl.getEngine();
        int defaultItem = 0;
        final int nEngines = items.size();
        for (int i = 0; i < nEngines; i++) {
            if (ids.get(i).equals(currEngine)) {
                defaultItem = i;
                break;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_chess_engine);
        builder.setSingleChoiceItems(items.toArray(new String[0]), defaultItem,
                (dialog, item) -> {
                    if ((item < 0) || (item >= nEngines))
                        return;
                    Editor editor = settings.edit();
                    String engine = ids.get(item);
                    editor.putString("engine", engine);
                    editor.apply();
                    dialog.dismiss();
                    setEngine(engine);
                });
        return builder.create();
    }

    private Dialog gameModeDialog() {
        final String[] items = {
            getString(R.string.play_white),
            getString(R.string.play_black),
            getString(R.string.two_players),
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.select_game_mode);
        builder.setItems(items, (dialog, item) -> {
            int gameModeType = -1;
            boolean matchPlayerNames = false;
            switch (item) {
            case 0: gameModeType = GameMode.PLAYER_WHITE; matchPlayerNames = true; break;
            case 1: gameModeType = GameMode.PLAYER_BLACK; matchPlayerNames = true; break;
            case 2: gameModeType = GameMode.TWO_PLAYERS;   break;
            default: break;
            }
            dialog.dismiss();
            if (gameModeType >= 0) {
                newGameMode(gameModeType);
                setBoardFlip(matchPlayerNames);
            }
        });
        return builder.create();
    }

    @Override
    public void requestPromotePiece() {
        showDialog(PROMOTE_DIALOG);
    }

    @Override
    public void reportInvalidMove(Move m) {
        String msg = String.format(Locale.US, "%s %s-%s",
                                   getString(R.string.invalid_move),
                                   TextIO.squareToString(m.from), TextIO.squareToString(m.to));
        ChessPlayApp.toast(msg, Toast.LENGTH_SHORT);
    }

    @Override
    public void reportEngineName(String engine) {
        String msg = String.format(Locale.US, "%s: %s",
                                   getString(R.string.engine), engine);
        ChessPlayApp.toast(msg, Toast.LENGTH_SHORT);
    }

    @Override
    public void reportEngineError(String errMsg) {
        String msg = String.format(Locale.US, "%s: %s",
                                   getString(R.string.engine_error), errMsg);
        ChessPlayApp.toast(msg, Toast.LENGTH_LONG);
    }

    @Override
    public void movePlayed(Position pos, Move move, boolean computerMove) {
        if (vibrateEnabled && computerMove) {
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        }
    }

    @Override
    public void runOnUIThread(Runnable runnable) {
        runOnUiThread(runnable);
    }

    private String timeToString(int time) {
        int secs = (int)Math.floor((time + 999) / 1000.0);
        boolean neg = false;
        if (secs < 0) {
            neg = true;
            secs = -secs;
        }
        int mins = secs / 60;
        secs -= mins * 60;
        StringBuilder ret = new StringBuilder();
        if (neg) ret.append('-');
        ret.append(mins);
        ret.append(':');
        if (secs < 10) ret.append('0');
        ret.append(secs);
        return ret.toString();
    }

    private Handler handlerTimer = new Handler();
    private Runnable r = () -> ctrl.updateRemainingTime();

    @Override
    public void setRemainingTime(int wTime, int bTime, int nextUpdate) {
        if (ctrl.getGameMode().clocksActive()) {
            whiteTitleText.setText(getString(R.string.white_square_character) + " " + timeToString(wTime));
            blackTitleText.setText(getString(R.string.black_square_character) + " " + timeToString(bTime));
        } else {
            TreeMap<String,String> headers = new TreeMap<>();
            ctrl.getHeaders(headers);
            whiteTitleText.setText(headers.get("White"));
            blackTitleText.setText(headers.get("Black"));
        }
        handlerTimer.removeCallbacks(r);
        if (nextUpdate > 0)
            handlerTimer.postDelayed(r, nextUpdate);
    }


    /** Go to given node in game tree. */
    public void goNode(Node node) {
        if (ctrl == null)
            return;
        Dialog mlmd = moveListMenuDlg;
        if ((mlmd == null) || !mlmd.isShowing()) {

            ctrl.goNode(node);
        }
    }
}
