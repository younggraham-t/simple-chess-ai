package chess_ai.ai_board;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import org.apache.commons.io.IOUtils;
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
public class Zobrist {
	final static int seed = 2361912;
	final static String randomNumbersFilePath = "/RandomNumbers.txt";

	/// piece type, colour, square index
	public static final long[][][] piecesArray = new long[8][2][64];
	public static final long[] castlingRights = new long[16];
	/// ep file (0 = no ep).
	public static long[] enPassantFile = new long[9]; // no need for rank info as side to move is included in key
	public static long sideToMove;

	static Random prng = new Random(seed);


	static void WriteRandomNumbers () throws Exception {
		prng = new Random(seed);
		String randomNumberString = "";
		int numRandomNumbers = 64 * 8 * 2 + castlingRights.length + 9 + 1;

		for (int i = 0; i < numRandomNumbers; i++) {
			randomNumberString += RandomUnsigned64BitNumber ();
			if (i != numRandomNumbers - 1) {
				randomNumberString += ',';
			}
		}
		var writer = new FileWriter(getRandomNumbersPath());
		writer.write(randomNumberString);
		writer.close();
	}

	static Queue<Long> ReadRandomNumbers () throws Exception {
		File f = new File(getRandomNumbersPath());
		if (!f.exists()) {
			//Debug.Log ("Create");
			WriteRandomNumbers ();
		}
		Queue<Long> randomNumbers = new LinkedList<> ();

		var reader = new FileReader(getRandomNumbersPath());
		String numbersString = IOUtils.toString(reader);
		reader.close ();

		String[] numberStrings = numbersString.split(",");
		for (int i = 0; i < numberStrings.length; i++) {
			long number = Long.parseUnsignedLong(numberStrings[i]);
			((LinkedList<Long>) randomNumbers).add(number);
		}
		return randomNumbers;
	}

	public Zobrist() throws Exception {

		var randomNumbers = ReadRandomNumbers ();

		for (int squareIndex = 0; squareIndex < 64; squareIndex++) {
			for (int pieceIndex = 0; pieceIndex < 8; pieceIndex++) {
				piecesArray[pieceIndex][AIBoard.WhiteIndex][squareIndex] = randomNumbers.poll();
				piecesArray[pieceIndex][AIBoard.BlackIndex][squareIndex] = randomNumbers.poll();
			}
		}

		for (int i = 0; i < 16; i++) {
			castlingRights[i] = randomNumbers.poll();
		}

		for (int i = 0; i < enPassantFile.length; i++) {
			enPassantFile[i] = randomNumbers.poll ();
		}

		sideToMove = randomNumbers.poll ();
	}

	/// Calculate zobrist key from current board position. This should only be used after setting board from fen; during search the key should be updated incrementally.
	public static long CalculateZobristKey (AIBoard board) {
		long zobristKey = 0;

		for (int squareIndex = 0; squareIndex < 64; squareIndex++) {
			if (board.Square[squareIndex] != 0) {
				int pieceType = Piece.PieceType (board.Square[squareIndex]);
				int pieceColour = Piece.Colour (board.Square[squareIndex]);

				zobristKey ^= piecesArray[pieceType][(pieceColour == Piece.White) ? AIBoard.WhiteIndex : AIBoard.BlackIndex][squareIndex];
			}
		}

		int epIndex = (int) (board.currentGameState >> 4) & 15;
		if (epIndex != -1) {
			zobristKey ^= enPassantFile[epIndex];
		}

		if (board.ColourToMove == Piece.Black) {
			zobristKey ^= sideToMove;
		}

		zobristKey ^= castlingRights[board.currentGameState & 0b1111];

		return zobristKey;
	}

	static String getRandomNumbersPath() {
		return randomNumbersFilePath;
	}


	static long RandomUnsigned64BitNumber () throws Exception {
		byte[] buffer = new byte[8];
		prng.nextBytes(buffer);
	
		return Long.parseUnsignedLong(BitConverter.toUInt64(buffer, 0));

	}
}