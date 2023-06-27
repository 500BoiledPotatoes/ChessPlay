package com.example.chessplay;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.example.chessplay.pieces.Piece;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Chessboard {
    private int startX;
    private int startY;
    private Player player;
    private ChessboardSquare[][] squaresArray;
    private Pixmap overallPixmap;
    private SpriteBatch batch;
    private Texture overallTexture;

    private ChessboardSquare currentSquare;
    private ChessboardSquare lastSquare;
    private ChessboardSquare[] currentAllowedSquares;


    private Map<Pieces, Pixmap> whitePiecesPM;
    private Map<Pieces, Pixmap> blackPiecesPM;

    public Chessboard(Player player, SpriteBatch batch, int startX, int startY) {
        this.batch = batch;
        this.player = player;
        this.startX = startX;
        this.startY = startY;
        whitePiecesPM = new HashMap<>();
        blackPiecesPM = new HashMap<>();

        initPiecesPixmaps();

        squaresArray = convertCellsToSquares();

        overallPixmap = makeStartPixmap();
    }


    public void draw() {
        if (overallTexture != null) overallTexture.dispose();
        overallTexture = new Texture(overallPixmap);
        batch.draw(overallTexture, startX, startY);
    }

    public void redrawSquare(ChessboardSquare square) {
        int x;
        int y;
        x = square.getCell().getX();
        y = square.getCell().getY();


        Piece piece = square.getCell().getPiece();
        if (piece != null) {
            Pixmap piecePM = getPiecePixmap(piece.getName(), piece.isWhite());
            square.setPiece(piece, piecePM);
        } else {
            square.setPiece(piece, null);
        }
        overallPixmap.drawPixmap(square.getPixmap(), x * ChessboardSquare.sideLength, (7 - y) * ChessboardSquare.sideLength);
    }

    private void selectPieceAndMoves() {
        currentAllowedSquares = getAllowedSquares(currentSquare);
        currentSquare.select();
        player.setCurrentCell(currentSquare.getCell());
        redrawSquare(currentSquare);
        selectAllowedSquares(currentAllowedSquares);
        lastSquare = currentSquare;
    }


    public ChessboardSquare setCurrentSquare(int x, int y) {
        for (ChessboardSquare[] line : squaresArray) {
            for (ChessboardSquare square : line) {
                if (square.contains(x, y)) {
                    currentSquare = square;
                    return square;
                }
            }
        }
        currentSquare = null;
        return null;
    }

    public void makeMove(boolean isMoveTapped) {
        Set<Base> changedBases = player.putPiece(currentSquare.getCell());
        for (Base base :
                changedBases) {
            ChessboardSquare changedSquare = getSquare(base.getX(), base.getY());
            redrawSquare(changedSquare);
        }
        redrawSquare(lastSquare);
        redrawSquare(currentSquare);
        player.changeTurn();
        player.setColor(!player.isWhite());
    }



    public void tap() {
        if (!player.isThisPlayersTurn()) return;
        boolean isSelected = false;
        if (currentSquare != null) isSelected = currentSquare.isSelected();
        unselectAll();
        if (currentSquare != null) {
            if (currentSquare.getCell().getPiece() != null && isItPLayersPiece()) {
                if (!isSelected) {
                    selectPieceAndMoves();
                } else {
                    if (!currentSquare.equals(lastSquare)) {
                        makeMove(true);
                    }
                }
            } else {
                if (isSelected) {
                    makeMove(true);
                }
            }
        }

    }

    enum Pieces {
        BISHOP,
        KING,
        KNIGHT,
        PAWN,
        QUEEN,
        ROOK
    }

    private boolean isItPLayersPiece() {
        return currentSquare.getCell().getPiece().isWhite() == player.isWhite();
    }

    private void unselectAll() {
        for (ChessboardSquare[] line : squaresArray) {
            for (ChessboardSquare square : line) {
                if (square.isSelected()) {
                    square.unselect();
                    redrawSquare(square);
                }
            }
        }
    }

    private ChessboardSquare[] getAllowedSquares(ChessboardSquare square) {
        if (square == null) return new ChessboardSquare[0];
        Set<Base> bases = player.capturePiece(square.getCell());
        ChessboardSquare[] result = new ChessboardSquare[bases.size()];
        int k = 0;
        for (Base base : bases) {
            result[k] = squaresArray[base.getY()][base.getX()];
            k++;
        }
        return result;
    }

    private void selectAllowedSquares(ChessboardSquare[] allowedSquares) {
        for (ChessboardSquare square : allowedSquares) {
            square.select();
            redrawSquare(square);
        }
    }

    private ChessboardSquare[][] convertCellsToSquares() {
        int nextX;
        int nextY = startY - ChessboardSquare.sideLength;
        ChessboardSquare[][] result = new ChessboardSquare[8][8];
        Base addedBase;
        for (int i = 0; i < 8; i++) {
            nextX = startX;
            nextY += ChessboardSquare.sideLength;
            for (int j = 0; j < 8; j++) {
                addedBase = player.getBoard().getData()[i][j];
                if (addedBase.getPiece() != null)
                    result[i][j] = new ChessboardSquare(addedBase, nextX, nextY, getPiecePixmap(addedBase.getPiece().getName(), addedBase.getPiece().isWhite()));
                else result[i][j] = new ChessboardSquare(addedBase, nextX, nextY);
                nextX += ChessboardSquare.sideLength;
            }
        }
        return result;
    }

    private Pixmap makeStartPixmap() {
        Pixmap pixmap = new Pixmap(ChessboardSquare.sideLength * 8, ChessboardSquare.sideLength * 8, Pixmap.Format.RGBA8888);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                pixmap.drawPixmap(squaresArray[i][j].getPixmap(), j * ChessboardSquare.sideLength, (7 - i) * ChessboardSquare.sideLength);
            }
        }
        return pixmap;
    }

    private Pixmap getPiecePixmap(String name, boolean color) {
        Map<Pieces, Pixmap> pixmaps;
        if (color) pixmaps = whitePiecesPM;
        else pixmaps = blackPiecesPM;
        switch (name) {
            case "King":
                return pixmaps.get(Pieces.KING);
            case "Knight":
                return pixmaps.get(Pieces.KNIGHT);
            case "Rook":
                return pixmaps.get(Pieces.ROOK);
            case "Queen":
                return pixmaps.get(Pieces.QUEEN);
            case "Pawn":
                return pixmaps.get(Pieces.PAWN);
            case "Bishop":
                return pixmaps.get(Pieces.BISHOP);
        }
        return null;
    }

    private void initPiecesPixmaps() {
        whitePiecesPM.put(Pieces.BISHOP, getPixmapByImageName("white_bishop.png"));
        whitePiecesPM.put(Pieces.KING, getPixmapByImageName("white_king.png"));
        whitePiecesPM.put(Pieces.KNIGHT, getPixmapByImageName("white_knight.png"));
        whitePiecesPM.put(Pieces.ROOK, getPixmapByImageName("white_rook.png"));
        whitePiecesPM.put(Pieces.QUEEN, getPixmapByImageName("white_queen.png"));
        whitePiecesPM.put(Pieces.PAWN, getPixmapByImageName("white_pawn.png"));

        blackPiecesPM.put(Pieces.BISHOP, getPixmapByImageName("black_bishop.png"));
        blackPiecesPM.put(Pieces.KING, getPixmapByImageName("black_king.png"));
        blackPiecesPM.put(Pieces.KNIGHT, getPixmapByImageName("black_knight.png"));
        blackPiecesPM.put(Pieces.ROOK, getPixmapByImageName("black_rook.png"));
        blackPiecesPM.put(Pieces.QUEEN, getPixmapByImageName("black_queen.png"));
        blackPiecesPM.put(Pieces.PAWN, getPixmapByImageName("black_pawn.png"));
    }

    private Pixmap getPixmapByImageName(String name) {
        return new Pixmap(Gdx.files.internal(name));
    }

    public ChessboardSquare getCurrentSquare() {
        return currentSquare;
    }


    public ChessboardSquare getSquare(int x, int y) {
        return squaresArray[y][x];
    }

    public void dispose() {
        overallPixmap.dispose();
        overallTexture.dispose();
        for (Pieces piece :
                whitePiecesPM.keySet()) {
            whitePiecesPM.get(piece).dispose();
            blackPiecesPM.get(piece).dispose();
        }
    }
}
