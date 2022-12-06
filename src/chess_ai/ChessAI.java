package chess_ai;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import chess.GameController;
import chess_ai.ai_board.AIBoard;
import chess_ai.ai_board.BitBoardUtility;
import chess_ai.ai_board.Move;
import chess_ai.ai_board.MoveGenerator;
import chess_ai.ai_board.Piece;
import piece.Coordinate;
import chessboard.*;


/**
 * Written by Graham Young on 11/11/22
 * @author Graham Young
 */

public class ChessAI {
	
	private final int POSITIVE_INFINITY = 999999999;
	private final int NEGATIVE_INFINITY = -POSITIVE_INFINITY;

	private AIBoard board;
	private MoveGenerator moveGenerator = new MoveGenerator();
	

	Move bestMoveThisIteration = Move.getInvalidMove();
	int bestEvalThisIteration = 0;
	
	boolean isWhite;
	
	public ChessAI(boolean isWhite) {
		this.isWhite = isWhite;
	}
	
	
	/**
	 * getNextMove: gets a boardFEN creates a new board from the fen and calls the search algorithm
	 * on that new board (the new board is to avoid messing with the display board)
	 * @param boardFEN 
	 * @return the best move that the search found
	 * 
	 */
	public chessboard.Move getNextMove(String boardFEN, int depth) {
		//make a chess board
		board = new AIBoard();
		board.LoadPosition(boardFEN);
		if(board.WhiteToMove != isWhite) {
			return null;
		}
		
		int eval = search(depth, NEGATIVE_INFINITY, POSITIVE_INFINITY, 0);
		Move bestMove = bestMoveThisIteration;
		
		System.out.println(bestMove);
		chessboard.Move move = bestMove.getChessBoardMove();
		//System.out.println(move);
		return move; 
	}
	
	protected int getRandomInRange (int min, int max) {
		if(max == 0 && min == 0) {
			return 0;
		}
		Random dice = new Random();
        return min + dice.nextInt(max - min) + 1;
    }
	



	/**
	 * alpha beta pruning minimax search.
	 * @param depth the number of times the method recurses
	 * @param alpha 
	 * @param beta
	 * @param int plyFromRoot a number used to tell if it is the root of the tree
	 * @return the int value of the best move
	 */
	
	
	private int search(int depth, int alpha, int beta, int plyFromRoot) {
		
		
		if(depth == 0) {
			return searchOnlyCaptures(alpha, beta);
		}
		List<Move> moves = moveGenerator.generateMoves(board);
		moveOrdering(board, moves);
		//for(Move m : moves) System.out.println(m);
		//System.out.println();
		if(moves.size() == 0) { //means there are no moves so check if stalemate or checkmate
			return moveGenerator.inCheck ? NEGATIVE_INFINITY : 0;
		}
		for(int i = 0; i<moves.size(); i++) {
			if(plyFromRoot == 0) {
				System.out.println(".");
			}
			board.MakeMove(moves.get(i), true);
			int eval = -search(depth - 1, -beta, -alpha, plyFromRoot+1);
			board.UnmakeMove(moves.get(i), true);
			if(eval >= beta) {
				return beta;
			}
			if (eval > alpha) {
				alpha = eval;
				if(plyFromRoot == 0) {
					bestMoveThisIteration = moves.get(i);
				}
			}
		}
		
		return alpha;
		
	}
	
	
	/**
	 * searchOnlyCaptures uses the move generator to only search the capture moves
	 * this method will give a better idea of the evaluation at the end of a regular search
	 * branch to prevent situations where for instance the opposing player captures on the next
	 * move thus making the evaluation wildly different.
	 * @param alpha
	 * @param beta
	 * @return
	 */
	private int searchOnlyCaptures(int alpha, int beta) {
		int eval = board.evaluate();
		//System.out.println("eval" + eval);
		//System.out.println("beta" + beta);
		if (eval >= beta) {
			return beta;
		}
		if (eval > alpha) {
			alpha = eval;
		}
		List<Move> moves = moveGenerator.generateMoves(board, false);
		
		moveOrdering(board, moves);
		for(Move m : moves) {
			board.MakeMove(m, true);
			eval = -searchOnlyCaptures(-beta, -alpha);
			board.UnmakeMove(m, true);
			if(eval >= beta) {
				return beta;
			}
			if (eval > alpha) {
				alpha = eval;
			}
		}
		//System.out.println("alpha" + alpha);
		return alpha;
	}
	
	
	
	/**
	 * moveOrdering adapted from SebLague
	 * 
	 */
	
	final int squareControlledByOpponentPawnPenalty = 350;
	
	private void moveOrdering(AIBoard board, List<Move> moves) {
		Move hashMove = Move.getInvalidMove();
		int[] moveScores = new int[moves.size()];
		for(int i = 0; i < moves.size(); i++) {
			int score = 0;
			int pieceToMove = Piece.PieceType(board.Square[moves.get(i).getStartSquare()]);
			int capturedPiece = Piece.PieceType(board.Square[moves.get(i).getTargetSquare()]);
			int moveFlag = moves.get(i).getMoveFlag();
			
			if(capturedPiece != Piece.None) {
				score = 10 * getPieceValue(capturedPiece) - getPieceValue(pieceToMove);
			}
			
			if (pieceToMove == Piece.Pawn) {

				if (moveFlag == Move.Flag.PromoteToQueen) {
					score += board.queenValue;
				} else if (moveFlag == Move.Flag.PromoteToKnight) {
					score += board.knightValue;
				} else if (moveFlag == Move.Flag.PromoteToRook) {
					score += board.rookValue;
				} else if (moveFlag == Move.Flag.PromoteToBishop) {
					score += board.bishopValue;
				}
			} else {
				// Penalize moving piece to a square attacked by opponent pawn
				if (BitBoardUtility.ContainsSquare (moveGenerator.opponentPawnAttackMap, moves.get(i).getTargetSquare())) {
					score -= squareControlledByOpponentPawnPenalty;
				}
			}
			if (Move.isSameMove(moves.get(i), hashMove)) {
				score += 10000;
			}

			moveScores[i] = score;
		}
		Sort(moves, moveScores);
				
	}
	
	
	/*
	 * getPieceValue written by SebLague
	 */
	static int getPieceValue (int pieceType) {
		switch (pieceType) {
			case Piece.Queen:
				return AIBoard.queenValue;
			case Piece.Rook:
				return AIBoard.rookValue;
			case Piece.Knight:
				return AIBoard.knightValue;
			case Piece.Bishop:
				return AIBoard.bishopValue;
			case Piece.Pawn:
				return AIBoard.pawnValue;
			default:
				return 0;
		}
	}
	/**
	 * Sort written by SebLague and adapted to java by Graham Young
	 * 
	 */
	
	void Sort (List<Move> moves, int[] moveScores) {
		// Sort the moves list based on scores
		for (int i = 0; i < moves.size() - 1; i++) {
			for (int j = i + 1; j > 0; j--) {
				int swapIndex = j - 1;
				if (moveScores[i] < moveScores[j]) {
					Move tempMove = moves.get(j);
					moves.set(j, moves.get(swapIndex));
					moves.set(swapIndex, tempMove);
					int tempScore = moveScores[j];
					moveScores[j] = moveScores[swapIndex];
					moveScores[swapIndex] = tempScore;
					
				}
			}
		}
	}
	
	
	
	
}
