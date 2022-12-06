package chess_ai.ai_board;
/**
 * this class was originally written by SebLague in C# and converted to Java by
 * Graham Young
 * 
 * original file: https://github.com/SebLague/Chess-AI/blob/main/Assets/Scripts/Core/Board.cs
 * 
 * 
 * @author SebLague, Graham Young
 *
 */
public class Coord implements Comparable<Coord> {
	public final int fileIndex;
	public final int rankIndex;

	public Coord (int fileIndex, int rankIndex) {
		this.fileIndex = fileIndex;
		this.rankIndex = rankIndex;
	}

	public boolean IsLightSquare () {
		return (fileIndex + rankIndex) % 2 != 0;
	}

	public int compareTo (Coord other) {
		return (fileIndex == other.fileIndex && rankIndex == other.rankIndex) ? 0 : 1;
	}
}
