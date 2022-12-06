package chessboard;

import piece.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import javax.swing.JOptionPane;

import chess_ai.ai_board.AIBoard;
import chess_ai.ai_board.MoveGenerator;
import chess_ai.ai_board.Move.Flag;
import chessboard.FenParser.PieceType;

/**
 * Created by wangyiyi on 2/12/15.
 */

/**
 * This is the chessboard class(Model)
 */
public class ChessBoard {
    public static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	protected int width;           // the width of chess board
    protected int height;          // the height of chess board
    private Piece tiles[][];    // this 2d array is used to save pieces
    protected Piece king1;         // king for player1 White
    protected Piece king2;         // king for player2 Black
    private ArrayList<Piece> white_pieces; // array list used to save all white pieces
    private ArrayList<Piece> black_pieces; // array list used to save all black pieces
    protected int turns;  // game turn
    
    //added by Graham Young: 11/11/22
    private Coordinate enPassantSquare;
	HashMap<PieceType, String> pieceMap = new HashMap<>();
	private Pawn enPassantPawn;
	private int halfMoveClock = 0;
	private final int FEN_POSITIONS = 0;
	private final int FEN_PLAYER_TURN = 1;
	private final int FEN_CASTLING = 2;
	private final int FEN_EN_PASSANT = 3;
	private final int FEN_HALF_MOVE_CLOCK = 4;
	private final int FEN_TURN_COUNTER = 5;
	Stack<String> chessboard_history_log; // used to save move history
	private boolean whiteToMove;
	

	
    /**
     *
     * Construct chess board given width and height
     *
     *      Coordinate system
     *
     *      +
     *      |
     *      |
     *      |
     *      |
     *      (0, 0) -------->  +
     *
     *
     * @param width:  set the width of chessboard
     * @param height: set the height of chessboard
     */
    public ChessBoard(int width, int height){
        initializeVariables(width, height);
        this.chessboard_history_log = new Stack<>(); // no piece move history yet
        

    }
    /*
     * written by Graham Young
     */
    private void initializeVariables(int width, int height) {
    	// set width and height of chessboard
        this.width = width;
        this.height = height;

        // initialize the 2d array to store pieces
        this.tiles = new Piece[height][];
        for(int i = 0; i < height; i++) {
            this.tiles[i] = new Piece[width];
        }

        // initialize white_pieces and black_pieces
        this.white_pieces = new ArrayList<Piece>();
        this.black_pieces = new ArrayList<Piece>();

        // initialize other variables
        this.king1 = null;        // no king is set yet
        this.king2 = null;
        this.turns = 0; // if it is even number, then it's White turn, otherwise Black turn
        this.enPassantSquare = new Coordinate(-1,-1);
        this.enPassantPawn = null;
    }
    
    /*
     * Graham Young:
     * added constructor using a fen string 11/11/22
     * for information about FEN strings:
     * https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation
     * 
     */
    
    public ChessBoard(String fen) {
    	this(8,8);
    	chessboard_history_log.push(fen);
    	setupBoardFromFen(fen);
    }
    
    /**
     * setupBoardFromFen takes a fen string and sets all the important information in the board
     * based on the fen string
     * @param fen the string to be parsed into pertinent information
     * 
     * written by Graham Young
     */
    
    private void setupBoardFromFen(String fen) {
		String[] fenSplit = fen.split(" ");
		//piece positions
		PieceType[][] boardMatrix = FenParser.parseFEN(fen);
		setupBoardFromMatrix(boardMatrix);
    	//halfMoveClock (50 move rule)
    	halfMoveClock = Integer.parseInt(fenSplit[FEN_HALF_MOVE_CLOCK]);
    	
    	//turn counter
    	if(fenSplit[FEN_PLAYER_TURN].equals("b")) { //if black to move we add one to make it odd
    		turns = ((Integer.parseInt(fenSplit[FEN_TURN_COUNTER])-1) * 2) + 1;
    		whiteToMove = false;
    	}
    	else { //if white to move
    		turns = (Integer.parseInt(fenSplit[FEN_TURN_COUNTER])-1) * 2;
    		whiteToMove = true;
    	}
    	//castling
    	
    	if(fenSplit[FEN_CASTLING].equals("-")) {
    		king1.moved();
    		king2.moved();
    		for(Piece[] l : tiles) {
    			for(Piece p : l) {
    				if(p instanceof Rook) {
    					p.moved();
    				}
    			}
    		}
    	}
    	else if (!fenSplit[FEN_CASTLING].contains("K")) {
    		for(Piece[] l : tiles) {
    			for(Piece p : l) {
    				if(p instanceof Rook 
    						&& getPieceAtCoordinate(0,0) != p
    						&& getPieceAtCoordinate(0,7) != p 
    						&& getPieceAtCoordinate(7,7) != p) {
    					p.moved();
    				}
    			}
    		}
    	}
    	else if (!fenSplit[FEN_CASTLING].contains("Q")) {
    		for(Piece[] l : tiles) {
    			for(Piece p : l) {
    				if(p instanceof Rook 
    						&& getPieceAtCoordinate(7,0) != p
    						&& getPieceAtCoordinate(0,7) != p 
    						&& getPieceAtCoordinate(7,7) != p) {
    					p.moved();
    				}
    			}
    		}
    	}
    	else if (!fenSplit[FEN_CASTLING].contains("k")) {
    		for(Piece[] l : tiles) {
    			for(Piece p : l) {
    				if(p instanceof Rook 
    						&& getPieceAtCoordinate(0,0) != p
    						&& getPieceAtCoordinate(0,7) != p 
    						&& getPieceAtCoordinate(7,0) != p) {
    					p.moved();
    				}
    			}
    		}
    	}
    	else if (!fenSplit[FEN_CASTLING].contains("q")) {
    		for(Piece[] l : tiles) {
    			for(Piece p : l) {
    				if(p instanceof Rook 
    						&& getPieceAtCoordinate(0,0) != p
    						&& getPieceAtCoordinate(7,0) != p 
    						&& getPieceAtCoordinate(7,7) != p) {
    					p.moved();
    				}
    			}
    		}
    	}
    	
    	//en passant
    	if(!fenSplit[FEN_EN_PASSANT].equals("-")) {
    		enPassantSquare = Coordinate.convertChessNotationToCoordinate(fenSplit[FEN_EN_PASSANT]);
    	}
    	
    }
    
    
    /*
     * created by Graham Young 11/11/22
     * Helper function for fen string constructor
     * puts pieces into the tiles 
     * 
     */
    private void setupBoardFromMatrix(PieceType[][] boardMatrix) {
    	assert(tiles.length == boardMatrix.length && tiles[0].length == boardMatrix[0].length);
    	for(int x = 0; x < boardMatrix.length; x++) {
    		for(int y = boardMatrix.length-1; y >= 0 ; y--) {
    			if(boardMatrix[x][y] != PieceType.EMPTY) {
    				Piece p = getPiecefromPieceType(boardMatrix[x][y]);
    				p.setCoordinate(y, x);
    				//this.tiles[y][x] = p;
    			}
    		}
    		
    	}
    }
    /*
     * created by Graham Young 11/11/22
     * Helper function for setupBoardFromMatrix
     * makes a map of all the pieceTypes and returns a new piece based on the input pieceType
     * 
     */
    
    @SuppressWarnings("unchecked")
	private Piece getPiecefromPieceType(PieceType pieceType) {
    	PieceFactory pieceFactory = new PieceFactory();
    	//both kings are already in the map to prevent making more than one king
    	ArrayList<PieceType> blackPieceTypes = new ArrayList(Arrays.asList(PieceType.BB, PieceType.BR, PieceType.BN, 
    									PieceType.BK, PieceType.BQ, PieceType.BP));
    	pieceMap.put(PieceType.BR, "Rook");
    	pieceMap.put(PieceType.BN, "Knight");
    	pieceMap.put(PieceType.BB, "Bishop");
    	pieceMap.put(PieceType.BQ, "Queen");
    	pieceMap.put(PieceType.BP, "Pawn");
    	pieceMap.put(PieceType.BK, "King");
    	
    	ArrayList<PieceType> whitePieceTypes = new ArrayList(Arrays.asList(PieceType.WB, PieceType.WR, PieceType.WN, 
				PieceType.WK, PieceType.WQ, PieceType.WP));
    	pieceMap.put(PieceType.WR, "Rook");
    	pieceMap.put(PieceType.WN, "Knight");
    	pieceMap.put(PieceType.WB, "Bishop");
    	pieceMap.put(PieceType.WQ, "Queen");
    	pieceMap.put(PieceType.WP, "Pawn");
    	pieceMap.put(PieceType.WK, "King");
    	
    	Piece piece = null;
    	if(blackPieceTypes.contains(pieceType)) {
    		piece = pieceFactory.makePiece(pieceMap.get(pieceType), this, Player.BLACK);
    	}
    	else {
    		piece = pieceFactory.makePiece(pieceMap.get(pieceType), this, Player.WHITE);
    	}
    	if(piece instanceof Pawn) {
    		if(piece.getPlayer() == Player.WHITE && piece.getY_coordinate() != 1) {
    			piece.moved();
    		}
    		if(piece.getPlayer() == Player.BLACK && piece.getY_coordinate() != 6) {
    			piece.moved();
    		}
    		
    	}
    	
    	return piece;

	}

    

	/**
     * add piece to white_pieces or black_pieces array list
     * @param p: The piece we want to save.
     */
    public void addPieceToList(Piece p) {
        if(p.getPlayer() == Player.WHITE){
            this.white_pieces.add(p);
        }
        else{
            this.black_pieces.add(p);
        }
    }
    /*
     * written by Graham Young
     */
    public ArrayList<Piece> getPiecesForPlayer(Player playerForThisTurn) {
		if (playerForThisTurn == Player.WHITE) {
			return this.white_pieces;
		}
		else {
			return this.black_pieces;
		}
		
	}

    /**
     * Getter: return this.white_pieces
     * @return white_piece array list that contains all white pieces
     */
    public ArrayList<Piece> getWhite_pieces(){
        return this.white_pieces;
    }

    /**
     * Getter: return this.black_pieces
     * @return black_piece array list that contains all black pieces
     */
    public ArrayList<Piece> getBlack_pieces(){
        return this.black_pieces;
    }


    /**
     * Getter: get king1 from WHITE player
     * @return king piece from WHITE player
     */
    public Piece getKing1(){
        return this.king1;
    }

    /**
     * Setter: set king1
     * @param p
     */
    public void setKing1(Piece p){
        this.king1 = p;
    }

    /**
     * Getter: get king2 from BLACK player
     * @return king piece from BLACK player
     */
    public Piece getKing2(){
        return this.king2;
    }

    /**
     * Setter: set king2
     * @param p
     */
    public void setKing2(Piece p){
        this.king2 = p;
    }
    
    /*
     * Graham Young setter enPassantSquare
     * 11/11/22
     * 
     */
    public void setEnPassantSquare(Coordinate c) {
    	enPassantSquare = c;
    }
    
    /*
     * Graham Young setter enPassantSquare
     * 11/11/22
     * 
     */
    public Coordinate getEnPassantSquare() {
    	return enPassantSquare;
    }
    /*
     * written by Graham Young
     */
	public Piece getEnPassantPawn() {
		return enPassantPawn;
	}
    /*
     * written by Graham Young
     */
	public void setEnPassantPawn(Pawn p) {
		this.enPassantPawn = p;
	}
    /*
     * written by Graham Young
     */
    public int getHalfMoveClock() {
		return halfMoveClock;
	}
    /*
     * written by Graham Young
     */
    public void incrementHalfMoveClock() {
    	halfMoveClock++;
    }
    /*
     * written by Graham Young
     */
	public void resetHalfMoveClock() {
		this.halfMoveClock = 0;
	}

	/**
     *
     * Return the piece at given coordinate
     * if there is no piece at that coordinate, or that coordinate is invalid, return null
     * @param x  the x coordinate
     * @param y  the y coordinate
     * @return   the piece at that (x, y) coordinate. If coordinate not valid, return null.
     */
    public Piece getPieceAtCoordinate(int x, int y){
        if(x >= this.width || x < 0 || y >= this.height || y < 0) // outside the boundary
            return null;
        return this.tiles[y][x];
    }
    /*
     * written by Graham Young
     */
    public Piece getPieceAtCoordinate(Coordinate c) {
    	return getPieceAtCoordinate(c.getX(), c.getY());
    }
    /*
     * written by Graham Young
     */
    public boolean isPieceAtCoordinate(int x, int y) {
    	return this.getPieceAtCoordinate(x, y) != null;
    }

    /**
     * Store the piece at (x, y) coordinate
     * @param p  the piece we want to set
     * @param x  the x coordinate
     * @param y  the y coordinate
     */
    public void setPieceAtCoordinate(Piece p, int x, int y){
        this.tiles[y][x] = p;
    }

    /**
     * Getter: get the width of chessboard
     * @return the width of chessboard
     */
    public int getWidth(){
        return this.width;
    }

    /**
     * Getter: get the height of chessboard
     * @return the height of chessboard
     */
    public int getHeight(){
        return this.height;
    }


    /**
     * Remove a piece from the chessboard
     * @param p the piece we want to remove from chessboard
     */
    public void removePiece(Piece p){
    	if(p == null) return;
        int x = p.getX_coordinate();
        int y = p.getY_coordinate();

        p = null;
        
        this.tiles[y][x] = null;
        
        
    }

    /**
     * Setter: set turns
     * @param turns
     */
    public void setTurns(int turns){
        this.turns = turns;
    }

    /**
     * Increment turns by 1
     */
    public void incrementTurns(){
        this.turns++;
        whiteToMove = whiteToMove ? false : true;
    }

    /**
     * Getter: get turns
     * @return turns of the game
     */
    public int getTurns(){
        return this.turns;
    }

    /**
     * Get the player for this turn
     * @return the player for this turn
     */
    public Player getPlayerForThisTurn(){
        return whiteToMove ? Player.WHITE : Player.BLACK;
    }
//    /*
//     * written by Graham Young
//     */
//    public List<Piece> getMovablePiecesForPlayer(Player player) {
//    	List<Piece> piecesForPlayer = getPiecesForPlayer(player);
//    	List<Piece> outputList = new ArrayList<>();
//    	for(Piece p : piecesForPlayer) {
//    		if(p.getLegalMoves().size() >= 1 && p.getX_coordinate() >= 0 && p.getY_coordinate() >= 0) {
//    			outputList.add(p);
//    		}
//    	}
//    	return outputList;
//    }
//    /*
//     * written by Graham Young
//     */
//    public List<Piece> getMovablePiecesForPlayer() { 
//    	return getMovablePiecesForPlayer(getPlayerForThisTurn());
//    }
    /*
     * written by Graham Young
     */
	public boolean makeAMove(Piece pieceToMove, int x, int y) {
//		Piece p = this.board.getPieceAtCoordinate(x, y);
		Move move = new Move(pieceToMove.getLocation(), new Coordinate(x,y));
		return makeAMove(move);
	}
    
    /*
     * written by Graham Young
     */
    public boolean makeAMove(Move move) {
    	Piece pieceToMove = move.getPiece(this);
    	Coordinate c = move.getMoveTo();
    	int x = c.getX();
    	int y = c.getY();
    	if(pieceToMove == null) {
			return false;
		}
		
		boolean moveHappened = false;
		
		
		AIBoard aiBoard = new AIBoard();
		aiBoard.LoadPosition(this.toString());
		List<chess_ai.ai_board.Move> legalMovesFromGenerator = new MoveGenerator().generateMoves(aiBoard);
		List<Move> legalMoves = new ArrayList<>();
		for(chess_ai.ai_board.Move m : legalMovesFromGenerator) {
			legalMoves.add(m.getChessBoardMove());
		}
		
		if (!legalMoves.contains(move)) {
			return false;
		}

		if (pieceToMove != null) { // that means p == null, and player clicked a tile that is not occupied
			moveHappened = this.movePlayerPieceToLocationIfValid(move);
		}

		
		
		return moveHappened;
    
        
    }
    /*
     * written by Graham Young
     */
    public boolean unMakeMove() {
    	assert(chessboard_history_log.size() > 0);
    
    	String log = chessboard_history_log.pop(); // get most recent log
    	
    	if(log != null) {
        	initializeVariables(width, height);
            setupBoardFromFen(log);
    	}
    	return true;
        
        
    }
    /**
     * check whether is checkmate or stalemate
     * @return null if neither checkmate nor stalemate; return "checkmate" if checkmate; return "stalemate" if stalemate
     */
    public String isCheckmateOrStalemate(){
        /*
         * Check checkmate and stalemate
         */
    	AIBoard aiBoard = new AIBoard();
    	aiBoard.LoadPosition(this.toString());
        if (this.playerCannotMove(this.getTurns() % 2 == 0 ? Player.WHITE : Player.BLACK)){ // so right now that player cannot move any chess
            King king = (this.getTurns() % 2 == 0) ? (King)this.getKing1() : (King)this.getKing2();  // get current player's king
            if(king == null) // chessboard not initialized yet.
                return null;
            if(new MoveGenerator().isInCheck(aiBoard)){ // checkmate
                return "checkmate";
            }
            else{ // stalemate
                return "stalemate";
            }
        }
        //50 move rule
        if(this.getHalfMoveClock() == 50) {
        	return "stalemate";
        }
        return null;
    }
    /**
     * Move player's piece to unoccupied tile if valid, which means the move is not a suicide move
     * @param panel
     * @param x     the x coord to move to
     * @param y     the y coord to move to
     */
    
    /*
     * Graham Young: updated to return a boolean based on whether the move happened or not 
     *  on 11/11/22
     *  
     *  added en passant logic 11/11/22
     *  added castling 11/11/22
     *  refactored to include all moves not just moves to empty tiles 11/13/22
     *  
     */
    public boolean movePlayerPieceToLocationIfValid(Move move){
    	if(move == null) {
    		return false;
    	}
    	Piece pieceToMove = move.getPiece(this);
    	int x = move.getMoveTo().getX();
    	int y = move.getMoveTo().getY();
    	
    	AIBoard aiBoard = new AIBoard();
		aiBoard.LoadPosition(this.toString());
		List<chess_ai.ai_board.Move> legalMovesFromGenerator = new MoveGenerator().generateMoves(aiBoard);
		List<Move> legalMoves = new ArrayList<>();
		for(chess_ai.ai_board.Move m : legalMovesFromGenerator) {
			legalMoves.add(m.getChessBoardMove());
		}

        if(legalMoves.contains(move)) {
            //if (coord.getX() == x && coord.getY() == y){ // player can move the piece there
                // System.out.println("You moved a piece");

                // save current history log
                //Chessboard_Log log = new Chessboard_Log(this.board);
//        		String log = board.toString();
//                chessboard_history_log.push(log);
        		String log = makeFEN();
        		//System.out.println(log);
        		chessboard_history_log.push(log);
                //check if the move is a castle and move the rook accordingly
                castle(pieceToMove, x,y);
                
                Piece opponent_piece = this.getPieceAtCoordinate(x, y);
                if(opponent_piece != null && opponent_piece.getPlayer() != pieceToMove.getPlayer()) {
                	opponent_piece.removeSelf(); // remove opponent's piece
                	this.resetHalfMoveClock();
                }
                
                //if a pawn moves to the enpassant square (which it can only do if it en passants)
                //then remove the en passant pawn
                int pawnCoord = preMoveEnPassantCheck(pieceToMove, x, y);
                
                // move player's piece to that coordinate
                pieceToMove.setCoordinate(x, y);
                pieceToMove.moved();
                
                //if a pawn moves 2 squares make it the en passant pawn and make the square
                //it crossed the en passant square
                postMoveEnPassantCheck(pieceToMove, x, pawnCoord);
                
                //if a pawn should promote do it
                promotePawn(pieceToMove, x, y, move.getPromotionPieceType());
                
                // update turns and redraw the canvas
               	
                this.incrementTurns();
                
                return true;
            }
        
        return false;
    }
    /*
     * written by Graham Young
     */
    private int preMoveEnPassantCheck(Piece pieceToMove, int x, int y) {
    	int pawnCoord = -1;
    	if(pieceToMove instanceof Pawn) {
    		this.resetHalfMoveClock();
        	pawnCoord = pieceToMove.getY_coordinate();
        	if(this.getEnPassantSquare().equals(new Coordinate(x,y))) {
        		this.removePiece(this.getEnPassantPawn());
        		this.incrementHalfMoveClock();
        	}
        }
        else {
        	this.incrementHalfMoveClock();
        }
    	return pawnCoord;
    }
    /*
     * written by Graham Young
     */
    private void postMoveEnPassantCheck(Piece pieceToMove, int x, int pawnCoord) {
    	if(pawnCoord >= 0) {
        	
        	if(pieceToMove.getY_coordinate() == pawnCoord + 2) {
        		this.setEnPassantSquare(new Coordinate(x, pawnCoord + 1));
        		this.setEnPassantPawn((Pawn)pieceToMove);
        	}
        	else if (pieceToMove.getY_coordinate() == pawnCoord - 2) {
        		this.setEnPassantSquare(new Coordinate(x, pawnCoord - 1));
        		this.setEnPassantPawn((Pawn)pieceToMove);
        	}
        	//if the pawn only moved 1 sqaure
        	else {
        		this.setEnPassantSquare(new Coordinate(-1,-1));
            	this.setEnPassantPawn(null);
            }
        }
        //if anything other than a pawn moves
        else {
        	this.setEnPassantSquare(new Coordinate(-1,-1));
        	this.setEnPassantPawn(null);
        }
    }
    

    /*
     * written by Graham Young
     */
    private void castle(Piece pieceToMove, int x, int y) {
    	//castling
        if(pieceToMove instanceof King && !pieceToMove.hasMoved()) {
        	
        	if(x-pieceToMove.getX_coordinate() == 2) { //king side castle
        		castleRookIfValid(pieceToMove, x, y, Player.WHITE, new Coordinate(7,0), -1);
        		castleRookIfValid(pieceToMove, x, y, Player.BLACK, new Coordinate(7,7), -1);
        	}
        	else if (x-pieceToMove.getX_coordinate() == -2) { //queen side castle
        		castleRookIfValid(pieceToMove, x, y, Player.WHITE, new Coordinate(0,0), 1);
        		castleRookIfValid(pieceToMove, x, y, Player.BLACK, new Coordinate(0,7), 1);
        	}
        }
    }
    /*
     * written by Graham Young
     */
    private void castleRookIfValid(Piece pieceToMove, int x, int y, Player player, Coordinate rookLocation, int rookOffset) {
    	if(pieceToMove.getPlayer() == player) {
			Piece possibleRook = this.getPieceAtCoordinate(rookLocation.getX(), rookLocation.getY());
			if(possibleRook instanceof Rook &&
					!possibleRook.hasMoved() && 
					possibleRook.getPlayer() == player) {
				possibleRook.setCoordinate(x+rookOffset, y);
			}
		}
    }

    public int promptPromotionType() {
    	Object[] possibleValues = {"queen", "bishop", "rook", "knight" };
    	Object selectedValue = JOptionPane.showInputDialog(null,
    	"Choose one", 
    	"Promotion Type",
    	JOptionPane.INFORMATION_MESSAGE, 
    	null,
    	possibleValues, 
    	possibleValues[0]);
    	
    	switch ((String)selectedValue) {
		case "rook":
			return chess_ai.ai_board.Piece.Rook;
		case "bishop":
			return chess_ai.ai_board.Piece.Bishop;
		case "knight":
			return chess_ai.ai_board.Piece.Knight;
		case "queen":
			return chess_ai.ai_board.Piece.Queen;
		default:
			return chess_ai.ai_board.Piece.None;
		}

    }
    /*
     * written by Graham Young
     */
    private void promotePawn(Piece pieceToMove, int x, int y, int promotionType) {

    	
    	if(promotionType != chess_ai.ai_board.Piece.None) {
    		
    		this.removePiece(pieceToMove);
    		pieceToMove = getPromotionPiece(x, y, pieceToMove.getPlayer(), promotionType);
    		pieceToMove.setCoordinate(x, y);
    		System.out.println(pieceToMove);
    		return;
    	}
    	
    	
    	if(pieceToMove instanceof Pawn) {
    		
    		
        	if(pieceToMove.getPlayer() == Player.WHITE && y == 7) {
        		promotionType = promptPromotionType();
        		this.removePiece(pieceToMove);
        		pieceToMove = getPromotionPiece(x, y, Player.WHITE, promotionType);
        		pieceToMove.setCoordinate(x, y);
        		
        	}
        	if(pieceToMove.getPlayer() == Player.BLACK && y == 0) {
        		promotionType = promptPromotionType();
        		this.removePiece(pieceToMove);
        		pieceToMove = getPromotionPiece(x, y, Player.BLACK, promotionType);
        		pieceToMove.setCoordinate(x, y);
        	}
        }
    }
    
    private Piece getPromotionPiece(int x, int y, Player player, int promotionType) {
    	
    	Piece out = null;
    	switch(promotionType) {
    	case(chess_ai.ai_board.Piece.Bishop):
    		out = new Bishop(this, player);
    		return out;
    	case(chess_ai.ai_board.Piece.Rook):
    		out = new Rook(this, player);
			return out;
    	case(chess_ai.ai_board.Piece.Knight):
    		out = new Knight(this, player);
			return out;
    	case(chess_ai.ai_board.Piece.Queen):
    		out = new Queen(this, player);
			return out;
    	default:
    		return null;
    	}
    }
    

    /**
     *
     * Check whether player can move a piece
     *
     * If a player can not move any piece, then return true
     *
     * @param player check whether player can move a piece or not.
     * @return return true if player cannot move any piece; otherwise return false
     */
    public boolean playerCannotMove(Player player){
    	AIBoard aiBoard = new AIBoard();
		aiBoard.LoadPosition(this.toString());
		List<chess_ai.ai_board.Move> legalMovesFromGenerator = new MoveGenerator().generateMoves(aiBoard);
		
        return legalMovesFromGenerator.size() == 0;
    }


    /**
     * Check whether player's king is in stalemate.
     *
     * when not checked, check whether is there any legal move.
     *
     * if there is no legal move, return true; otherwise return false.
     * @param player
     * @return true if there is stalemate; otherwise return false.
     */
    public boolean isStalemate(Player player){
    	AIBoard aiBoard = new AIBoard();
    	aiBoard.LoadPosition(this.toString());
        King king = player == Player.WHITE ? (King)this.king1 : (King)this.king2;
        if(new MoveGenerator().isInCheck(aiBoard) == false){ // king is not in check.
            // check whether is there any legal move
            // if there is no legal move, then return true
            // if there is a legal move, then return false
            if(playerCannotMove(player))
                return true;
            return false;
        }
        else{
            return false;
        }
    }
    
    /**
     * Graham Young:
     * 
     * created toString:
     * creates a fen string based on the current state of the board. 
     * @TODO add castling 
     * 
     */

    public String toString() {
    	return makeFEN();
    }
    
    private String makeFEN() {
    	//add the positions
    	String str = generateFenPositions();
    	str += " ";
    	//add whose turn it is
    	if(getPlayerForThisTurn() == Player.WHITE) {
    		str += "w";
    	}
    	else {
    		str += "b";
    	}
    	str += " ";
    	//add castling availability
    	String castleString = createCastleString();
    	str += castleString;
    	str += " ";
    	//add en passant target square
    	if(!enPassantSquare.equals(new Coordinate(-1,-1))) {
    		str += Coordinate.convertCoordinateToChessNotation(enPassantSquare);
    	}
    	else {
    		str += "-";
    	}
    	str += " ";
    	//add halfMoveClock
    	str += halfMoveClock;
    	str += " ";
    	//add full move turn
    	str += ((int)turns/2) + 1;
    	
    	
    	//System.out.println(str);
    	return str;
    }
    /*
     * checks if the kings have moved or if the rooks have moved and if not adds the appropriate
     * character to the string
     */
    /*
     * written by Graham Young
     */
    private String createCastleString() {
    	String castleString = "";
    	if(!king1.hasMoved()) {
    		if(isPieceAtCoordinate(7, 0)) {
	    		 if(!getPieceAtCoordinate(7, 0).hasMoved()
	    				&& getPieceAtCoordinate(7, 0) instanceof Rook) {
	    			castleString += "K";
    		}
    	}
    		if(isPieceAtCoordinate(0, 0)) {
	    		if(!getPieceAtCoordinate(0, 0).hasMoved()
	    				&& getPieceAtCoordinate(0, 0) instanceof Rook) {
	    			castleString += "Q";
	    		}
    		}
    	}
    	if(!king2.hasMoved()) {
			if (isPieceAtCoordinate(7, 7)) {
				if (!getPieceAtCoordinate(7, 7).hasMoved() 
						&& getPieceAtCoordinate(7, 7) instanceof Rook) {
					castleString += "k";
				}
			}
			if (isPieceAtCoordinate(0, 7)) {
				if (!getPieceAtCoordinate(0, 7).hasMoved() 
						&& getPieceAtCoordinate(0, 7) instanceof Rook) {
					castleString += "q";
				}
			}
    	}
    	if(castleString.equals("")) {
    		castleString += "-";
    	}
    	return castleString;
    }
    

    /**
     * generatFenPositions:
     * Helper method for toString
     * takes the positions of the pieces on the board and converts it to a string following
     * FEN conventions
     * @return a string of the form rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR
     * where lowercase letters represent black pieces uppercase letters represent white pieces
     * and numbers represent the number of empty spaces in a row. the above example is the
     * FEN string for the standard starting position. for more on FEN notation:
     * https://en.wikipedia.org/wiki/Forsyth%E2%80%93Edwards_Notation
     * 
     * written by Graham Young
     */

    private String generateFenPositions() {

    	String str = "";
    	for(int i = tiles.length-1; i >= 0; i--) {
    		int numNulls = 0;
    		for(Piece p : tiles[i]) {
        		if(p != null) {
        			if(numNulls > 0) {
        				str += numNulls;
        				numNulls = 0;
        			}
        			if(p.getPlayer() == Player.WHITE) {
        				str += Character.toUpperCase(p.getPiece_name());
        			}
        			else {
        				str += p.getPiece_name();
        			}
        			
        		}
        		else {
        			numNulls++;
        		}
    		}
    		if(numNulls > 0) {
				str += numNulls;
			}
    		if(i != 0) str += "/";
    	}
    	return str;
    }
    
    /**
     * Generate standard 8 x 8 chess board
     * refactored to use fenSetupMethod by Graham Young on 11/12/22
     */
    public void generateStandardBoard(){
    	//setupBoardFromFen(STARTING_FEN);
    	setupBoardFromFen("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
    }


	


}
