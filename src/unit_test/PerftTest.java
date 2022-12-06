package unit_test;


import org.junit.Test;

import chessboard.ChessBoard;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import chess_ai.ChessAI;
import chess_ai.ai_board.AIBoard;
import chess_ai.ai_board.Move;
import chess_ai.ai_board.MoveGenerator;
import chessboard.Player;
import piece.Coordinate;


/**
 * this test is used to determine whether the generateMoves function correctly generates legal moves
 * the test numbers are from https://www.chessprogramming.org/Perft_Results
 * 
 * @author Graham Young
 *
 */

public class PerftTest {
	
	public static final String FEN_POS_6 = "r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10";
	public static final String START_POS_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	public static final String FEN_POS_2 = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1"; 
	public static final String FEN_POS_3 = "8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - -";
	public static final String FEN_POS_4 = "r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1";
	public static final String FEN_POS_5 = "rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8";
	
	
	MoveGenerator mg;
	AIBoard board;

	@Test
	public void perftTestStartPos() {
		mg = new MoveGenerator();
		board = new AIBoard();
		board.LoadStartPosition();

		assertEquals(20, search(1, true));
		assertEquals(400, search(2, true));
		assertEquals(8902, search(3, true));
		assertEquals(197281, search(4, true));
		assertEquals(4865609, search(5, true));
		assertEquals(119060324, search(6, true));
		assertEquals(3195901860L, search(7, true));
	}
	
	
	@Test
	public void perftTestPos2() {
		mg = new MoveGenerator();
		board = new AIBoard();
		board.LoadPosition(FEN_POS_2);

		assertEquals(48, search(1, true));
		assertEquals(2039, search(2, true));
		assertEquals(97862, search(3, true));
		assertEquals(4085603, search(4, true));
		assertEquals(193690690, search(5, true));
		assertEquals(8031647685L, search(6,true));
	}
	
	@Test
	public void perftTestPos3() {
		mg = new MoveGenerator();
		board = new AIBoard();
		board.LoadPosition(FEN_POS_3);

		assertEquals(14, search(1, true));
		assertEquals(191, search(2, true));
		assertEquals(2812, search(3, true));
		assertEquals(43238, search(4, true));
		assertEquals(674624, search(5, true));
		assertEquals(11030083, search(6,true));
		assertEquals(178633661, search(7,true));
		assertEquals(3009794393L, search(8,true));
	}
	
	@Test
	public void perftTestPos4() {
		mg = new MoveGenerator();
		board = new AIBoard();
		board.LoadPosition(FEN_POS_4);

		assertEquals(6, search(1, true));
		assertEquals(264, search(2, true));
		assertEquals(9467, search(3, true));
		assertEquals(422333, search(4, true));
		assertEquals(15833292, search(5, true));
		assertEquals(706045033, search(6,true));

	}
	
	@Test
	public void perftTestPos5() {
		mg = new MoveGenerator();
		board = new AIBoard();
		board.LoadPosition(FEN_POS_5);

		assertEquals(44, search(1, true));
		assertEquals(1486, search(2, true));
		assertEquals(62379, search(3, true));
		assertEquals(2103487, search(4, true));
		assertEquals(89941194, search(5, true));

	}
	
	@Test
	public void perftTestPos6() {
		mg = new MoveGenerator();
		board = new AIBoard();
		board.LoadPosition(FEN_POS_6);

		assertEquals(46, search(1, true));
		assertEquals(2079, search(2, true));
		assertEquals(89890, search(3, true));
		assertEquals(3894594, search(4, true));
		assertEquals(164075551, search(5, true));

	}
	
	
	public long search(int depth, boolean root) {
		

		boolean leaf = (depth == 2);
		long numLocalNodes = 0;
		long numSuccessors = 0;
		List<Move> moves = mg.generateMoves(board);
		for(Move m : moves) {
			if (root && depth <= 1) {
	            numSuccessors = 1; 
				numLocalNodes++;
			}
	        else {
				board.MakeMove(m, true);
				
				numSuccessors =  leaf ? mg.generateMoves(board).size() : search(depth-1, false);
				
				numLocalNodes += numSuccessors;
	
				board.UnmakeMove(m, true);
	        }
			if (root) {
				System.out.println(m + ": " + numSuccessors);
			}
			
		}
		if(root) {
			System.out.println(numLocalNodes);
		}
		
		
		
		
		return numLocalNodes;
		
	}

	
}
