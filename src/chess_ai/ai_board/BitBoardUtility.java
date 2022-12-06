package chess_ai.ai_board;
/**
 * this class was originally written by SebLague in C# and converted to Java by
 * Graham Young

 * all other methods were written by Graham Young
 * 
 * original file: https://github.com/SebLague/Chess-AI/blob/main/Assets/Scripts/Core/Piece.cs
 * 
 * 
 * @author SebLague, Graham Young
 *
 */
public class BitBoardUtility {
	public static boolean ContainsSquare (long bitboard, int square) {
		return ((bitboard >> square) & 1) != 0;
	}
}
