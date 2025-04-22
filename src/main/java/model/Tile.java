package model;

import client.ChessBoardClient;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

    public class Tile extends Rectangle {
        private Piece piece;

        public boolean hasPiece() {
            return piece != null;
        }

        public Piece getPiece() {
            return piece;
        }

        public void setPiece(Piece piece) {
            this.piece = piece;
        }

        public Tile(boolean light, int x, int y) {
            setWidth(ChessBoardClient.TILE_SIZE);
            setHeight(ChessBoardClient.TILE_SIZE);

            relocate(x * ChessBoardClient.TILE_SIZE, y * ChessBoardClient.TILE_SIZE);

            setFill(light ? Color.valueOf("#C1A89F") : Color.valueOf("#5D5364"));
        }
    }

