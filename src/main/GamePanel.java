package main;

import piece.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements Runnable{

    public static final int WIDTH = 825;
    public static final int HEIGHT = 600;
    final int FPS = 60;
    Thread gameThread;
    Board board = new Board();
    Mouse mouse = new Mouse();

    //Pieces
    public static ArrayList<Piece> pieces = new ArrayList<>();
    public static ArrayList<Piece> simPieces = new ArrayList<>();
    ArrayList<Piece> promoPieces = new ArrayList<>();
    Piece activeP, checkingP;
    public static Piece castlingP;


    //Piece Color
    public static final int WHITE = 0;
    public static final int BLACK = 1;
    int currentColor = WHITE;

    // BOOLEANS
    boolean canMove;
    boolean validSquare;
    boolean promotion;
    boolean gameOver;
    boolean stalemate;


    public GamePanel(){
        setPreferredSize(new Dimension (WIDTH,HEIGHT));
        setBackground(Color.black);
        addMouseMotionListener(mouse);
        addMouseListener(mouse);

        setPieces();
        copyPieces(pieces, simPieces);
    }

    public void launchGame(){
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void setPieces(){

        //WHITE TEAM
        pieces.add(new Pawn(WHITE, 0, 6));
        pieces.add(new Pawn(WHITE, 1, 6));
        pieces.add(new Pawn(WHITE, 2, 6));
        pieces.add(new Pawn(WHITE, 3, 6));
        pieces.add(new Pawn(WHITE, 4, 6));
        pieces.add(new Pawn(WHITE, 5, 6));
        pieces.add(new Pawn(WHITE, 6, 6));
        pieces.add(new Pawn(WHITE, 7, 6));
        pieces.add(new Rook(WHITE, 0, 7));
        pieces.add(new Rook(WHITE, 7, 7));
        pieces.add(new Knight(WHITE, 1, 7));
        pieces.add(new Knight(WHITE, 6, 7));
        pieces.add(new Bishop(WHITE, 2, 7));
        pieces.add(new Bishop(WHITE, 5, 7));
        pieces.add(new Queen(WHITE, 3, 7));
        pieces.add(new King(WHITE, 4, 7));

        //BLACK TEAM
        pieces.add(new Pawn(BLACK, 0,  1));
        pieces.add(new Pawn(BLACK, 1, 1));
        pieces.add(new Pawn(BLACK, 2, 1));
        pieces.add(new Pawn(BLACK, 3, 1));
        pieces.add(new Pawn(BLACK, 4, 1));
        pieces.add(new Pawn(BLACK, 5, 1));
        pieces.add(new Pawn(BLACK, 6, 1));
        pieces.add(new Pawn(BLACK, 7, 1));
        pieces.add(new Rook(BLACK, 0, 0));
        pieces.add(new Rook(BLACK, 7, 0));
        pieces.add(new Knight(BLACK, 1, 0));
        pieces.add(new Knight(BLACK, 6, 0));
        pieces.add(new Bishop(BLACK, 2, 0));
        pieces.add(new Bishop(BLACK, 5, 0));
        pieces.add(new Queen(BLACK, 3, 0));
        pieces.add(new King(BLACK, 4, 0));
    }


    private void copyPieces(ArrayList<Piece> source, ArrayList<Piece> target){
        target.clear();
        for (int i = 0; i<source.size(); i++){
            target.add(source.get(i));
        }
    }

    @Override
    public void run() {//Create a game loop
        double drawInterval = 1000000000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null){
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime)/drawInterval;
            lastTime = currentTime;

            if (delta >= 1){
                update();
                repaint(); //Calling paintComponent method.
                delta--;
            }
        }
    }

    private void update(){

        if (promotion){
            promoting();
        }
        else if(!gameOver && !stalemate){
            // MOUSE BUTTON PRESSED ////
            if (mouse.pressed) {

                if (activeP == null){
                    // if the activeP is null, check if you can pick up a piece //
                    for (Piece piece : simPieces){
                        // If the mouse is on an allay piece, pick it up as activeP
                        if (piece.color == currentColor && piece.col == mouse.x/Board.SQUARE_SIZE &&
                                piece.row == mouse.y/Board.SQUARE_SIZE){

                            activeP = piece;
                        }
                    }
                }
                else{
                    // If the player is holding a piece, simulate the piece//
                    simulate();
                }
            }

            /// Mouse button released //

            if (!mouse.pressed){

                if (activeP != null){

                    if(validSquare){

                        //Move confirmed

                        //Update the piece list in case a piece has been captured and removed during simulation.
                        copyPieces(simPieces, pieces);
                        activeP.updatePosition();
                        if (castlingP != null){
                            castlingP.updatePosition();
                        }

                        if(isKingInCheck() && isCheckMate()){
                            gameOver = true;
                        }
                        else if(isStalemate() && !isKingInCheck()){
                            stalemate = true;
                        }
                        else{ //The game is still going
                            if (canPromote()){
                                promotion = true;
                            }
                            else{
                                changePlayer();
                            }
                        }
                    }
                    else{

                        //The move is not valid
                        copyPieces(pieces, simPieces);
                        activeP.resetPosition();
                        activeP = null;
                    }
                }
            }
        }
    }

    private void simulate(){

        canMove = false;
        validSquare = false;

        /* Reset the piece list in every loop
            this is for restoring the removed pieces during simulation
         */

        copyPieces(pieces,simPieces);

        //Reset the castling piece's position.
        if (castlingP != null){
            castlingP.col = castlingP.preCol;
            castlingP.x = castlingP.getX(castlingP.col);
            castlingP = null;
        }

        // If a piece is being held, update its position
        activeP.x = mouse.x - Board.HALF_SQAURE_SIZE;
        activeP.y = mouse.y - Board.HALF_SQAURE_SIZE;
        activeP.col = activeP.getCol(activeP.x);
        activeP.row = activeP.getRow(activeP.y);

        //check if piece is hovering over a valid square
        if(activeP.canMove(activeP.col , activeP.row)){
            canMove = true;

            //If you captured a piece, remove it from the list
            if (activeP.hittingP != null){
                simPieces.remove(activeP.hittingP.getIndex());
            }

            checkCastling();

            if(!isIllegal(activeP) && !opponentCanCaptureKing()){
                validSquare = true;
            }
        }
    }

    private boolean isIllegal(Piece king){
        if (king.type == Type.KING){
            for(Piece piece: simPieces){
                if (piece != king && piece.color != king.color && piece.canMove(king.col,king.row)){
                    return true;
                }
            }
        }
        return false;
    }

    private boolean opponentCanCaptureKing(){

        Piece king = getKing(false);
        for(Piece piece: simPieces){
            if (piece.color != king.color && piece.canMove(king.col, king.row)){
                return true;
            }
        }
        return false;
    }

    private boolean isKingInCheck(){

        Piece king = getKing(true);

        if(activeP.canMove(king.col, king.row)){
            checkingP = activeP;
            return true;
        }
        else{
            checkingP = null;
        }

        return false;
    }

    private Piece getKing(boolean opponent){

        Piece king = null;

        for(Piece piece: simPieces){
            if(opponent){
                if(piece.type == Type.KING && piece.color != currentColor){
                    king = piece;
                }
            }
            else{
                if(piece.type == Type.KING && piece.color == currentColor){
                    king = piece;
                }
            }
        }
        return king;
    }

    private boolean isCheckMate(){

        Piece king = getKing(true);

        if(kingCanMove(king)){
            return false;
        }
        else{
            //Check if the player can block the attack

            //Check the position of the checking piece and the king in check
            int colDiff = Math.abs(checkingP.col - king.col);
            int rowDiff = Math.abs(checkingP.row - king.row);

            if (colDiff == 0){//Attacking vertically

                if(checkingP.row < king.row){//Attacking from above
                    for (int row = checkingP.row; row< king.row; row++){
                        for(Piece piece: simPieces){
                            if(piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)){
                                return false;
                            }
                        }
                    }
                }
                if(checkingP.row > king.row){//Attacking from below
                    for (int row = checkingP.row; row > king.row; row--){
                        for(Piece piece: simPieces){
                            if(piece != king && piece.color != currentColor && piece.canMove(checkingP.col, row)){
                                return false;
                            }
                        }
                    }
                }
            }
            else if(rowDiff == 0){//Attacking Horizontally
                if(checkingP.col < king.col){//Attacking from left
                    for (int col = checkingP.col; col< king.col; col++){
                        for(Piece piece: simPieces){
                            if(piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)){
                                return false;
                            }
                        }
                    }
                }
                if(checkingP.col > king.col){//Attacking from right
                    for (int col = checkingP.col; col > king.col; col--){
                        for(Piece piece: simPieces){
                            if(piece != king && piece.color != currentColor && piece.canMove(col, checkingP.row)){
                                return false;
                            }
                        }
                    }
                }
            }
            else if (colDiff == rowDiff){//Attacking diagonally
                if (checkingP.row < king.row){//Attacking from above
                    if(checkingP.col < king.col){//Attacking from upper left
                        for(int col = checkingP.col, row = checkingP.row; col< king.col; col++, row++){
                            for(Piece piece: simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col,row)){
                                    return false;
                                }
                            }
                        }
                    }
                    if(checkingP.col > king.col){//Attacking from upper right
                        for(int col = checkingP.col, row = checkingP.row; col > king.col; col--, row++){
                            for(Piece piece: simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col,row)){
                                    return false;
                                }
                            }
                        }
                    }
                }
                if(checkingP.row > king.row){//Attacking from bellow
                    if(checkingP.col < king.col){//Attacking from lower left
                        for(int col = checkingP.col, row = checkingP.row; col< king.col; col++, row--){
                            for(Piece piece: simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col,row)){
                                    return false;
                                }
                            }
                        }
                    }
                    if(checkingP.col > king.col){//Attacking from lower right
                        for(int col = checkingP.col, row = checkingP.row; col > king.col; col--, row--){
                            for(Piece piece: simPieces){
                                if(piece != king && piece.color != currentColor && piece.canMove(col,row)){
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean kingCanMove(Piece king){
        //Sim if there is any square where the king can move to in order to avoid the opponent
        if(isValidMove(king,-1,-1)) {return true;}
        if(isValidMove(king,0,-1)) {return true;}
        if(isValidMove(king,1,-1)) {return true;}
        if(isValidMove(king,-1,0)) {return true;}
        if(isValidMove(king,1,0)) {return true;}
        if(isValidMove(king,-1,1)) {return true;}
        if(isValidMove(king,0,1)) {return true;}
        if(isValidMove(king,1,1)) {return true;}

        return false;
    }

    private boolean isValidMove(Piece king, int colPlus, int rowPlus){

        boolean isValidMove = false;

        //Temporarily update kings position
        king.col += colPlus;
        king.row += rowPlus;

        if(king.canMove(king.col, king.row)){

            if(king.hittingP != null){
                simPieces.remove(king.hittingP.getIndex());
            }
            if(isIllegal(king) == false){
                isValidMove = true;
            }
        }

        //Reset king's position and restore the removes piece.
        king.resetPosition();
        copyPieces(pieces, simPieces);

        return isValidMove;
    }

    private boolean isStalemate(){
        int count = 0;

        for (Piece piece: simPieces){
            if(piece.color != currentColor){
                count++;
            }
        }

        if(count == 10){//Only king left
            if(!kingCanMove(getKing(true))){
                return true;
            }
        }
        return false;
    }
    private void checkCastling(){

        if(castlingP != null){
            if(castlingP.col == 0){
                castlingP.col += 3;
            }else if(castlingP.col == 7){
                castlingP.col -=2;
            }
            castlingP.x = castlingP.getX(castlingP.col);
        }

    }

    private void changePlayer(){

        if (currentColor == WHITE) {
            currentColor = BLACK;
            //Reset Black's twoStepped status
            for(Piece piece: pieces){
                if(piece.color == BLACK){
                    piece.twoStepped = false;
                }
            }
        }else{
            currentColor = WHITE;
            //Reset white's twoStepped status
            for(Piece piece: pieces){
                if(piece.color == WHITE){
                    piece.twoStepped = false;
                }
            }
        }
        activeP = null;
    }

    private boolean canPromote(){

        if(activeP.type == Type.PAWN){
            if(currentColor == WHITE && activeP.row == 0 || currentColor == BLACK && activeP.row == 7){
                promoPieces.clear();
                promoPieces.add(new Rook(currentColor,9,2));
                promoPieces.add(new Knight(currentColor,9,3));
                promoPieces.add(new Bishop(currentColor,9,4));
                promoPieces.add(new Queen(currentColor,9,5));
                return true;
            }
        }
        return false;
    }

    private void promoting(){

        if(mouse.pressed){
            for(Piece piece : promoPieces){
                if(piece.col == mouse.x/Board.SQUARE_SIZE && piece.row == mouse.y/Board.SQUARE_SIZE){
                    switch(piece.type){
                        case ROOK: simPieces.add(new Rook(currentColor, activeP.col, activeP.row)); break;
                        case KNIGHT: simPieces.add(new Knight(currentColor, activeP.col, activeP.row)); break;
                        case BISHOP: simPieces.add(new Bishop(currentColor, activeP.col, activeP.row)); break;
                        case QUEEN: simPieces.add(new Queen(currentColor, activeP.col, activeP.row)); break;
                        default: break;
                    }
                    simPieces.remove(activeP.getIndex());
                    copyPieces(simPieces, pieces);
                    activeP = null;
                    promotion = false;
                    changePlayer();
                }
            }
        }
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;//Casting in order to call draw method.

        //BOARD
        board.draw(g2);

        //PIECES
        for (Piece p: simPieces){
            p.draw(g2);
        }

        if (activeP != null){

            if (canMove){
                if(isIllegal(activeP) || opponentCanCaptureKing()){
                    g2.setColor(Color.red);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(activeP.col*Board.SQUARE_SIZE, activeP.row*Board.SQUARE_SIZE,
                            Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }
                else{
                    g2.setColor(Color.white);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
                    g2.fillRect(activeP.col*Board.SQUARE_SIZE, activeP.row*Board.SQUARE_SIZE,
                            Board.SQUARE_SIZE, Board.SQUARE_SIZE);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                }

            }
            // Draw the active piece in the end, so it's not hidden by the board or colored square
            activeP.draw(g2);
        }

        // STATUS MESSAGES

        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(new Font("Book Antiqua", Font.PLAIN,25));
        g2.setColor(Color.white);

        if(promotion){
            g2.drawString("Promote to:",637,100);
            for(Piece piece: promoPieces){
                g2.drawImage(piece.image, piece.getX(piece.col),piece.getY(piece.row), Board.SQUARE_SIZE,Board.SQUARE_SIZE,null);
            }
        }
        else{
            if (currentColor == WHITE){
                g2.drawString("White's Turn",637,442);
                if(checkingP != null && checkingP.color == BLACK){
                    g2.setColor(Color.red);
                    g2.drawString("The King", 637,500);
                    g2.drawString("is in check!",637,525);
                }
            }
            else{
                g2.drawString("Black's Turn",637,178);
                if(checkingP != null && checkingP.color == WHITE){
                    g2.setColor(Color.red);
                    g2.drawString("The King", 637,225);
                    g2.drawString("is in check!",637,250);
                }
            }
        }
        if(gameOver){
            String s = "";
            if(currentColor == WHITE){
                s = "White Wins";
            }
            else{
                s = "Black Wins";
            }
            g2.setFont(new Font("Arial", Font.PLAIN,90));
            g2.setColor(Color.GREEN);
            g2.drawString(s,200,300);
        }
        if(stalemate){
            g2.setFont(new Font("Arial", Font.PLAIN,90));
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawString("Stalemate",200,300);
        }
    }
}
