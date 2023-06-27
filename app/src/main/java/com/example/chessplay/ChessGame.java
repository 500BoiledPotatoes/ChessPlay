package com.example.chessplay;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import com.example.chessplay.pieces.Bishop;
import com.example.chessplay.pieces.King;
import com.example.chessplay.pieces.Knight;
import com.example.chessplay.pieces.Piece;
import com.example.chessplay.pieces.Queen;
import com.example.chessplay.pieces.Rook;

public class ChessGame extends ApplicationAdapter {
    private int startX;
    private int startY;
    private final String RUS_LETTERS = "ABVGDEYOJZIKLMNOPRSTUFCHSH'YIYUYA" + "abvgdeyojziklmnoprstufhtsch'yiyuya";

    private boolean isGameFinished = false;
    private boolean isDialog = false;
    private boolean isOnline = false;
    private boolean isThisPlayerWhite;

    private Player player;
    private Chessboard chessboard;
    private GameActivity gameActivity;
    private PromotingDialog dialog;

    private SpriteBatch batch;
    private BitmapFont check;
    private BitmapFont forBlack;
    private BitmapFont forWhite;
    private BitmapFont infoText;

    @Override
    public void create() {
        Gdx.graphics.setContinuousRendering(false);
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (isDialog) {
                    choosePromotingPiece(screenX, screenY);
                } else {
                    if (!isGameFinished) {
                        chessboard.setCurrentSquare(screenX, Gdx.graphics.getHeight() - screenY);
                        chessboard.tap();
                        isDialog = isPromotingDialogCalled();

                    } else {
                        gameActivity.backToMenu();
                    }
                }
                return true;
            }
        });

        startX = (Gdx.graphics.getWidth() - ChessboardSquare.sideLength * 8) / 2;
        startY = (Gdx.graphics.getHeight() - ChessboardSquare.sideLength * 8) / 2;
        batch = new SpriteBatch();
        player = new Player(isThisPlayerWhite);
        if (!isOnline) chessboard = new Chessboard(player, batch, startX, startY);
        else chessboard = new Chessboard(player, batch, startX, startY);
        check = createTextStyle("font_1.ttf", 200, Color.RED);
        forBlack = createTextStyle("font_1.ttf", 200, Color.BLACK);
        forWhite = createTextStyle("font_1.ttf", 200, Color.WHITE);
        infoText = createTextStyle("font_1.ttf", 100, Color.BLACK);
        dialog = new PromotingDialog(batch, (Gdx.graphics.getWidth() - 128 * 4) / 2, startY + ChessboardSquare.sideLength * 8 + 100, isThisPlayerWhite);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.4f, 0.6f, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        chessboard.draw();
        drawCheckMessage();
        drawEndScreen();
        if (isDialog) {
            dialog.draw();
        }
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        check.dispose();
        infoText.dispose();
        chessboard.dispose();
        dialog.dispose();
    }

    private void drawCheckMessage() {
        boolean isWhite = player.isWhite();
        boolean yourCheck = player.isCheck(isWhite) && !player.isCheckmate(isWhite);
        boolean opponentCheck = player.isCheck(!isWhite) && !player.isCheckmate(!isWhite);
        if (yourCheck || opponentCheck) {
            drawText("Kill", check, HorizontalAlignment.CENTER, VerticalAlignment.TOP, 0, 0);
            if (yourCheck) {
                if (isWhite) {
                    drawText("Check", forWhite, HorizontalAlignment.CENTER, VerticalAlignment.TOP, 0, 150);
                } else {
                    drawText("Check", forBlack, HorizontalAlignment.CENTER, VerticalAlignment.TOP, 0, 150);
                }
            }
            if (opponentCheck) {
                if (isWhite) {
                    drawText("Check", forBlack, HorizontalAlignment.CENTER, VerticalAlignment.TOP, 0, 150);
                } else {
                    drawText("Check", forWhite, HorizontalAlignment.CENTER, VerticalAlignment.TOP, 0, 150);
                }
            }

        }
    }

    private void drawEndScreen() {
        boolean isWhite = player.isWhite();
        boolean yourCheckmate = player.isCheckmate(isWhite);
        boolean opponentCheckmate = player.isCheckmate(!isWhite);
        if (yourCheckmate || opponentCheckmate) {
            isGameFinished = true;
            Pixmap finishScreenPM = new Pixmap(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), Pixmap.Format.RGBA8888);
            finishScreenPM.setColor(1, 0, 0, 0.4f);
            finishScreenPM.fill();
            Texture finishScreen = new Texture(finishScreenPM);
            batch.draw(finishScreen, 0, 0);
            drawText("Victory", check, HorizontalAlignment.CENTER, VerticalAlignment.TOP, 0, 0);
            if (yourCheckmate) {
                if (isWhite) {
                    drawText("CheckMate", forBlack, HorizontalAlignment.CENTER, VerticalAlignment.TOP, 0, 150);
                } else {
                    drawText("CheckMate", forWhite, HorizontalAlignment.CENTER, VerticalAlignment.TOP, 0, 150);
                }
            }
            if (opponentCheckmate) {
                if (isWhite) {
                    drawText("CheckMate", forWhite, HorizontalAlignment.CENTER, VerticalAlignment.TOP, 0, 150);
                } else {
                    drawText("CheckMate", forBlack, HorizontalAlignment.CENTER, VerticalAlignment.TOP, 0, 150);
                }
            }
            drawText("Click on the screen", infoText, HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM, 0, 400);

        }
    }

    private boolean isPromotingDialogCalled() {
        Base promotedBase = player.getBoard().getPromotedCell();
        if (promotedBase != null) {
            if (isOnline) dialog.setWhite(player.isWhite());
            else dialog.setWhite(!player.isWhite());
            return true;
        }
        return false;
    }

    public ChessGame(boolean isWhite, GameActivity gameActivity) {
        this.isThisPlayerWhite = isWhite;
        this.gameActivity = gameActivity;
    }

    private void choosePromotingPiece(int screenX, int screenY) {
        Piece choosedPiece = dialog.getPiece(screenX, Gdx.graphics.getHeight() - screenY);

        if (choosedPiece != null) {
            isDialog = false;
            player.getBoard().makePromotion(choosedPiece);
            chessboard.redrawSquare(chessboard.getCurrentSquare());
        }
    }

    private BitmapFont createTextStyle(String fontFileName, int textSize, Color textColor) {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(fontFileName));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.characters = RUS_LETTERS;

        parameter.size = textSize;
        parameter.color = textColor;
        BitmapFont style = generator.generateFont(parameter);
        generator.dispose();
        return style;
    }

    private void drawText(String text, BitmapFont textStyle, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment, float marginX, float marginY) {
        GlyphLayout glyphLayout = new GlyphLayout(textStyle, text);
        float x = marginX;
        float y = marginY;
        switch (horizontalAlignment) {
            case CENTER:
                x = (Gdx.graphics.getWidth() - glyphLayout.width) / 2;
                break;
            case LEFT:
                x = marginX;
                break;
            case RIGHT:
                x = Gdx.graphics.getWidth() - glyphLayout.width - marginX;
                break;
        }
        switch (verticalAlignment) {
            case CENTER:
                y = (Gdx.graphics.getHeight() - glyphLayout.height) / 2;
                break;
            case TOP:
                y = Gdx.graphics.getHeight() - glyphLayout.height - marginY;
                break;
            case BOTTOM:
                y = marginY;
                break;
        }
        textStyle.draw(batch, glyphLayout, x, y);
    }

    enum HorizontalAlignment {
        CENTER,
        RIGHT,
        LEFT
    }

    enum VerticalAlignment {
        TOP,
        CENTER,
        BOTTOM
    }
}
