package piece;

import main.GamePanel;

public class Pawn extends Piece{
    
    public Pawn(int color, int col, int row){
        super(color, col, row);

        if(color == GamePanel.WHITE){
            image = getImage("/piece/w-pawn");
        }
        else{
            image = getImage("/piece/b-pawn");
        }
    }

    @Override
    public boolean canMove(int targetCol, int targetRow) {

        int moveValue = 0;
        if (isWithinBoard(targetCol, targetRow) && !isSameSquare(targetCol, targetRow)) {
            //Define move value based on its color
            if (color == GamePanel.WHITE) {
                moveValue = -1;
            } else {
                moveValue = 1;
            }
            //Check the hitting piece
            hittingP = getHittingP(targetCol, targetRow);

            //1 Square Movement
            if (targetCol == preCol && targetRow == preRow + moveValue && hittingP == null) {
                return true;
            }

            //2 Square Movement
            if (targetCol == preCol && targetRow == preRow + 2 * moveValue && hittingP == null && !moved &&
                    !pieceIsOnStraightLine(targetCol, targetRow)) {
                return true;
            }
        }

        //Diagonal Movement and Capture
        if (Math.abs(targetCol - preCol) == 1 && targetRow == preRow + moveValue && hittingP != null &&
                hittingP.color != color) {
            return true;
        }

        return false;
    }
}
