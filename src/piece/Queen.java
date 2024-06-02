package piece;

import main.GamePanel;

public class Queen extends Piece{

    public Queen(int color, int col, int row){
        super(color, col, row);

        if(color == GamePanel.WHITE){
            image = getImage("/piece/w-queen");
        }
        else{
            image = getImage("/piece/b-queen");
        }
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {

        if (isWithinBoard(targetCol,targetRow) && !isSameSquare(targetCol,targetRow)){
            //Vertical and Horizontal
            if(targetCol == preCol || targetRow == preRow){
                if(isValidSquare(targetCol,targetRow) && !pieceIsOnStraightLine(targetCol,targetRow)){
                    return true;
                }
            }

            //Diagonal
            if (Math.abs(targetCol - preCol) == Math.abs(targetRow - preRow)){
                if (isValidSquare(targetCol,targetRow) && !pieceIsOnDiagonalLine(targetCol,targetRow)){
                    return true;
                }
            }
        }
        return false;
    }
}
