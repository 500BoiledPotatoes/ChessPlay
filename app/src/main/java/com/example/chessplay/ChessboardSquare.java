package com.example.chessplay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.math.Rectangle;

import com.example.chessplay.pieces.Piece;

import java.util.Objects;


public class ChessboardSquare extends Rectangle {
    private Base base;
    private Pixmap square;
    private Pixmap piece;
    private boolean selected;


    public static final int sideLength = 128;
    public static final int borderWidth = 10;
    public static final Color borderColor = new Color(0x6df52aff);
    public static final Color blackCellColor = new Color(0.66f, 0.42f, 0.07f, 1);
    public static final Color whiteCellColor = new Color(0.99f, 0.94f, 0.87f, 1);

    public ChessboardSquare(Base base, int x, int y) {
        this.base = base;
        this.square = setSquare(base.getX(), base.getY());
        this.x = x;
        this.y = y;
        this.height = sideLength;
        this.width = sideLength;
    }

    public ChessboardSquare(Base base, int x, int y, Pixmap piece) {
        this.base = base;
        this.square = setSquare(base.getX(), base.getY());
        this.x = x;
        this.y = y;
        this.height = sideLength;
        this.width = sideLength;
        this.piece = piece;
    }
    private Pixmap setSquare(int x, int y) {
        Pixmap square = new Pixmap(sideLength, sideLength, Pixmap.Format.RGBA8888);
        square.setColor((x + y) % 2 == 0 ? blackCellColor : whiteCellColor);
        square.fill();
        return square;
    }

    public void setPiece(Piece pieceObject, Pixmap piecePixmap) {
        this.piece = piecePixmap;
        this.base.setPiece(pieceObject);
    }


    public Base getCell() {
        return base;
    }


    public void select() {
        this.selected = true;
    }

    public void unselect() {
        this.selected = false;
    }

    public boolean isSelected() {
        return selected;
    }

    public Pixmap getBorderPixmap() {
        Pixmap border = new Pixmap(sideLength, sideLength, Pixmap.Format.RGBA8888);
        border.setColor(borderColor);
        border.fillRectangle(0,0, sideLength, borderWidth);
        border.fillRectangle(sideLength - borderWidth, 0, borderWidth, sideLength);
        border.fillRectangle(0, 0, borderWidth, sideLength);
        border.fillRectangle(0, sideLength - borderWidth, sideLength, borderWidth);

        return border;
    }

    public Pixmap getPixmap() {
        Pixmap pixmap = new Pixmap(sideLength, sideLength, Pixmap.Format.RGBA8888);
        pixmap.drawPixmap(square, 0, 0);

        if (piece != null) pixmap.drawPixmap(piece, 0, 0);
        if (selected) pixmap.drawPixmap(getBorderPixmap(), 0, 0);
        return pixmap;
    }

    public Pixmap getPixmap(Pixmap piecePixmap) {
        Pixmap pixmap = new Pixmap(sideLength, sideLength, Pixmap.Format.RGBA8888);
        pixmap.drawPixmap(square, 0, 0);

        if (piecePixmap != null) pixmap.drawPixmap(piecePixmap, 0, 0);
        if (selected) pixmap.drawPixmap(getBorderPixmap(), 0, 0);
        return pixmap;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ChessboardSquare square = (ChessboardSquare) o;
        return base.equals(square.base);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), base);
    }

    @Override
    public String toString() {
        return "ChessboardSquare{" +
                "cell=" + base +
                ", selected=" + selected +
                '}';
    }
}
