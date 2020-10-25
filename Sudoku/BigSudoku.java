package Sudoku;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class BigSudoku extends JPanel implements MouseListener, KeyListener {

	/**
	 * dimensions of puzzle
	 */
	int n = 4;

	/**
	 * square of n
	 */
	int sq = n * n;

	/**
	 * graphics - width of each square
	 */
	final int squareWidth = 50;

	/**
	 * serial version id
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * sudoku instance
	 */
	static BigSudoku test;

	/**
	 * the current state
	 */
	int[][] puzzle = new int[sq][sq];

	/**
	 * the original puzzle
	 */
	int[][] game = new int[sq][sq];

	/**
	 * the solution
	 */
	int[][] solution = new int[sq][sq];

	/**
	 * JFrame
	 */
	JFrame jf;

	/**
	 * width of screen
	 */
	int screenWidth = getToolkit().getScreenSize().width;

	/**
	 * height of screen
	 */
	int screenHeight = getToolkit().getScreenSize().height;

	/**
	 * the x location that the user has selected
	 */
	int selectx = -1;

	/**
	 * the y location that the user has selected
	 */
	int selecty = -1;

	/**
	 * start time, used for timing
	 */
	long startTime = 0;

	/**
	 * elapsed time, used for timing
	 */
	long time = 0;

	/**
	 * time from saves, if applicable
	 */
	long savedTime = 0;

	/**
	 * difficulty
	 */
	int difficulty = 0;

	/**
	 * is the game over
	 */
	boolean gameOver = false;

	/**
	 * check answers?
	 */
	boolean checkAnswers = false;

	/**
	 * Random instance
	 */
	Random random = new Random();

	/**
	 * file path for saving/loading
	 */
	final String filePath = "saved.txt";

	/**
	 * list of messages to user
	 */
	LinkedList<Message> messages = new LinkedList<>();

	/**
	 * length of each message (in milliseconds)
	 */
	final long messageLength = 10000;

	/**
	 * Sudoku constructor
	 */
	public BigSudoku() {
		solution = generateSolution();
		game = generatePuzzle(solution, difficulty);
		reset();
		addMouseListener(this);
		addKeyListener(this);
		setFocusable(true);
		this.requestFocus();

		jf = new JFrame("big sudoku");
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setSize(getToolkit().getScreenSize());
		jf.setVisible(true);
		jf.add(this);
		startTime = System.currentTimeMillis();
		try {
			while (true) {
				if (!gameOver)
					time = System.currentTimeMillis() - startTime + savedTime;
				jf.repaint();
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			System.out.println("Interrupted Exception");
		}
	}

	/**
	 * Paint function, called each frame
	 */
	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		g.drawOval(screenWidth / 2 - 50, 8, 15, 15);
		g.drawString("C", screenWidth / 2 - 50 + 3, 20);
		g.drawString("Jonathan Guo", screenWidth / 2 - 30, 20);
		Font f = new Font("Helvetica", 20, 25);
		g.setFont(f);
		g.setColor(Color.PINK);
		if (selectx != -1 && selecty != -1) // selected square
			g.fillRect(squareWidth * selectx + 370, squareWidth * selecty + 90, squareWidth, squareWidth);

		g.setColor(Color.BLACK);
		for (int i = 0; i < sq; i++) { // list of numbers
			g.drawRect(1280, squareWidth * i + 90, squareWidth, squareWidth);
			drawNumber(g, "" + (i + 1), 1280 + 17, squareWidth * i + 125);
		}
		// time stuff
		long seconds = (time / 1000) % 60;
		long minutes = (time / 1000) / 60;
		String timeDisplay = Long.toString(seconds);
		if (timeDisplay.length() == 1)
			timeDisplay = "0" + timeDisplay;
		timeDisplay = minutes + ":" + timeDisplay;
		g.drawString(timeDisplay, 1380, 130);

		// save stuff
		g.drawRect(1380, 200, 200, 50);
		g.drawRect(1380, 250, 200, 50);
		g.drawRect(1380, 300, 200, 50);
		g.drawString("Save to file", 1400, 233);
		g.drawString("Load from file", 1400, 283);
		g.drawString("Check answers", 1400, 333);

		// message stuff
		while (!messages.isEmpty() && messages.getFirst().endTime < System.currentTimeMillis()) {
			messages.remove();
		}
		g.setFont(new Font("Helvetica", 15, 15));
		int messageIndex = 0;
		for (Message c : messages) {
			g.drawString(c.message, 1400, 383 + 30 * messageIndex);
			messageIndex++;
		}
		g.setFont(f);

		for (int i = 0; i < sq; i++) {
			for (int j = 0; j < sq; j++) {
				g.setColor(Color.BLACK);
				g.drawRect(squareWidth * i + 370, squareWidth * j + 90, squareWidth, squareWidth);
				if (puzzle[i][j] == 0) // blank square
					continue;
				if (game[i][j] == 0) // user entered number
					g.setColor(Color.BLUE);
				if (checkAnswers) // only if checkAnswers is on
					if (game[i][j] == 0 && solution[i][j] != puzzle[i][j]) // incorrect value entered!
						g.setColor(Color.RED);
				drawNumber(g, "" + puzzle[i][j], squareWidth * i + 387, squareWidth * j + 125);
			}
		}
		g.setColor(Color.BLACK);
		for (int i = 0; i < n + 1; i++) {
			g.fillRect(n * squareWidth * i + 370 - 2, 90, 4, sq * squareWidth);
			g.fillRect(370, n * squareWidth * i + 90 - 2, sq * squareWidth, 4);
		}
	}

	/**
	 * draws a number; special function to account for 1 digit vs 2 digit numbers
	 * 
	 * @param g   graphics instance
	 * @param num number to be drawn
	 * @param x   x coordinate
	 * @param y   y coordinate
	 */
	private void drawNumber(Graphics g, String num, int x, int y) {
		if (num.length() == 1)
			g.drawString(num, x, y);
		else
			g.drawString(num, x - 7, y);
	}

	/**
	 * prints an array using a printwriter
	 * 
	 * @param pw  printwriter
	 * @param arr array to be printed
	 */
	private void printArray(PrintWriter pw, int[][] arr) {
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				pw.print(arr[i][j] + " ");
			}
			pw.println();
		}
	}

	/**
	 * scans an array using a scanner
	 * 
	 * @param sc  Scanner
	 * @param len length of array
	 * @return
	 */
	private int[][] scanArray(Scanner sc, int len) {
		int[][] answer = new int[len][len];
		for (int i = 0; i < len; i++) {
			String[] nums = sc.nextLine().split(" ");
			for (int j = 0; j < len; j++) {
				answer[i][j] = Integer.parseInt(nums[j]);
			}
		}
		return answer;
	}

	/**
	 * Save sudoku to file
	 */
	public void save() {
		System.out.println("saving to file...");
		messages.add(new Message("saving to file..."));
		// TODO add encryption functionality
		// TODO add multiple saved files
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
			out.println(n);
			out.println(time);
			printArray(out, puzzle);
			printArray(out, game);
			printArray(out, solution);
			out.close();
		} catch (IOException e) {
			System.out.println("file not found error");
			return;
			// e.printStackTrace();
		}
		System.out.println("puzzle saved!");
		messages.add(new Message("puzzle saved!"));
	}

	/**
	 * Load sudoku to file
	 */
	public void load() {
		System.out.println("loading from file...");
		messages.add(new Message("loading from file..."));
		try {
			Scanner in = new Scanner(new File(filePath));
			n = Integer.parseInt(in.nextLine());
			sq = n * n;
			savedTime = Integer.parseInt(in.nextLine());
			startTime = System.currentTimeMillis();
			puzzle = scanArray(in, sq);
			game = scanArray(in, sq);
			solution = scanArray(in, sq);
		} catch (FileNotFoundException e) {
			System.out.println("file not found error");
			return;
		}

		System.out.println("puzzle loaded!");
		messages.add(new Message("puzzle loaded!"));
	}

	/**
	 * restart (same puzzle though)
	 */
	public void reset() {
		for (int i = 0; i < sq; i++)
			for (int j = 0; j < sq; j++)
				puzzle[i][j] = game[i][j];
	}

	/**
	 * generate a puzzle
	 * 
	 * @param sol        solution
	 * @param difficulty difficulty
	 * @return a puzzle with solution sol of the specified difficulty
	 */
	public int[][] generatePuzzle(int[][] sol, int difficulty) {
		int[][] answer = new int[sq][sq];
		for (int i = 0; i < sq; i++)
			for (int j = 0; j < sq; j++)
				answer[i][j] = sol[i][j];

		int row = -1;
		int col = -1;
		for (int k = 0; k < 300; k++) {
			while (true) { // make sure it isn't filled in
				row = random.nextInt(sq);
				col = random.nextInt(sq);
				if (answer[row][col] != 0)
					break;
			}
			int previousNumber = answer[row][col];
			answer[row][col] = 0;
			int[][] copy = new int[sq][sq];
			for (int i = 0; i < sq; i++)
				for (int j = 0; j < sq; j++)
					copy[i][j] = answer[i][j];
			int[][] solvedCopy = solve(copy);
			boolean good = true;
			o: for (int i = 0; i < sq; i++) {
				for (int j = 0; j < sq; j++) {
					if (solvedCopy[i][j] == 0) {
						good = false;
						break o;
					}
				}
			}
			if (!good)
				answer[row][col] = previousNumber;
		}
		// matching difficulty
		int tick = 0;
		while (tick < difficulty) {
			row = random.nextInt(sq);
			col = random.nextInt(sq);
			if (answer[row][col] == 0) {
				answer[row][col] = sol[row][col];
				tick++;
			}
			boolean allFilledIn = true;
			for (int i = 0; i < sq; i++)
				for (int j = 0; j < sq; j++)
					if (answer[i][j] == 0)
						allFilledIn = false;
			if (allFilledIn)
				break;
		}
		return answer;
	}

	/**
	 * generate a solution
	 * 
	 * @return a solution
	 */
	public int[][] generateSolution() {
		int[][] puzzle = new int[sq][sq];
		boolean[][][] array = new boolean[sq][sq][sq + 1];
		for (int i = 0; i < sq; i++) {
			for (int j = 0; j < sq; j++) {
				Arrays.fill(array[i][j], true);
				array[i][j][0] = false;
			}
		}
		puzzle = dfs(0, puzzle, array);
		return puzzle;
	}

	/**
	 * depth first search, creates a puzzle
	 * 
	 * @param current current index that is being looked at
	 * @param puzzle  the puzzle
	 * @param arr     which values are possible
	 * @return a randomly generated puzzle
	 */
	public int[][] dfs(int current, int[][] puzzle, boolean[][][] arr) {
		int[][] falsearray = new int[sq][sq]; // no solutions found
		falsearray[0][0] = -1;
		if (current == sq * sq) // dfs done!! made it to the end
			return puzzle;
		int row = current / sq;
		int col = current % sq;
		Permutation permutation = new Permutation();
		boolean[] possible = new boolean[sq + 1];
		for (int i = 0; i < sq + 1; i++) {
			possible[i] = arr[row][col][i];
			if (i >= 1 && possible[i]) {
				permutation.add(i);
			}
		}
		if (permutation.size == 0)
			return falsearray;

		for (int i = 0; i < permutation.size; i++) {
			int j = permutation.remove();
			possible[j] = false;
			int[][] newPuzzle = new int[sq][sq];
			boolean[][][] newArray = new boolean[sq][sq][sq + 1];
			for (int k = 0; k < sq; k++) {
				for (int l = 0; l < sq; l++) {
					newPuzzle[k][l] = puzzle[k][l];
					for (int m = 0; m < sq + 1; m++)
						newArray[k][l][m] = arr[k][l][m];
				}
			}
			newPuzzle[row][col] = j;
			for (int k = 0; k < sq; k++) {
				newArray[row][k][j] = false;
				newArray[k][col][j] = false;
			}
			int squareRow = row / n;
			int squareCol = col / n;
			for (int k = 0; k < n; k++)
				for (int l = 0; l < n; l++)
					newArray[n * squareRow + k][n * squareCol + l][j] = false;
			int[][] result = dfs(current + 1, newPuzzle, newArray);
			if (result[0][0] > 0)
				return result;
		}
		return falsearray;
	}

	/**
	 * solve this puzzle
	 * 
	 * @param puzz puzzle to be solved
	 * @return solved puzzle
	 */
	public int[][] solve(int[][] puzz) {
		boolean[][][] array = new boolean[sq][sq][sq + 1];
		for (int i = 0; i < sq; i++) {
			for (int j = 0; j < sq; j++) {
				Arrays.fill(array[i][j], true);
				array[i][j][0] = false;
			}
		}
		for (int i = 0; i < sq; i++)
			for (int j = 0; j < sq; j++)
				if (puzz[i][j] != 0)
					fillIn(i, j, puzz[i][j], puzz, array);
		for (int i = 0; i < sq * sq; i++)
			if (!solveOne(puzz, array))
				break;
		return puzz;
	}

	/**
	 * is it possible to solve one square?
	 * 
	 * @param puzz  the puzzle
	 * @param array possibilities array
	 * @return whether it is possible to solve one square or not
	 */
	public boolean solveOne(int[][] puzz, boolean[][][] array) {
		for (int i = 0; i < sq; i++) {
			for (int j = 0; j < sq; j++) {
				if (game[i][j] != 0)
					continue;
				int possibilities = 0;
				int num = 0;
				for (int k = 1; k < sq + 1; k++) {
					if (array[i][j][k]) {
						possibilities++;
						num = k;
					}
				}
				if (possibilities != 1) // only one possibility!
					continue;
				fillIn(i, j, num, puzz, array);
				return true;
			}
		}
		return false;
	}

	/**
	 * fills in one square
	 * 
	 * @param r     row
	 * @param c     col
	 * @param value value to be filled in
	 * @param puzz  the puzzle
	 * @param array the possibility array
	 */
	public void fillIn(int r, int c, int value, int[][] puzz, boolean[][][] array) {
		puzz[r][c] = value;
		for (int i = 0; i < sq; i++) {
			array[r][i][value] = false;
			array[i][c][value] = false;
			array[r][c][i + 1] = false;
		}
		int squareRow = r / n;
		int squareCol = c / n;
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				array[n * squareRow + i][n * squareCol + j][value] = false;
	}

	/**
	 * user fill in
	 * 
	 * @param val value (between 0 and n*n)
	 */
	public void userFillIn(int val) {
		if (selectx == -1 || selecty == -1)
			return;
		if (game[selectx][selecty] != 0)
			return;
		puzzle[selectx][selecty] = val;
		boolean allCorrect = true;
		for (int i = 0; i < sq; i++)
			for (int j = 0; j < sq; j++)
				if (solution[i][j] != puzzle[i][j])
					allCorrect = false;
		if (allCorrect) {
			selectx = -1;
			selecty = -1;
			gameOver = true;
			JOptionPane.showMessageDialog(this, "Game Over! You win!");
			System.exit(ABORT);
		}
	}

	/**
	 * main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		test = new BigSudoku();
	}

	/**
	 * Permutation class
	 * 
	 * @author jonguo6
	 *
	 */
	private class Permutation {

		/**
		 * how many nums are in the permutation
		 */
		int size = 0;

		/**
		 * array of nums in the permutation
		 */
		private int[] nums = new int[sq];

		/**
		 * Permutation constructor
		 */
		Permutation() {

		}

		/**
		 * adds an integer to this permutation
		 * 
		 * @param a integer to be added
		 */
		public void add(int a) {
			nums[size] = a;
			size++;
		}

		/**
		 * removes a random integer from this permutation
		 * 
		 * @return the random integer
		 */
		public int remove() {
			int index = random.nextInt(size);
			int answer = nums[index];
			size--;
			nums[index] = nums[size];
			nums[size] = 0; // no loitering!
			return answer;
		}
	}

	/**
	 * Message class
	 * 
	 * @author jonguo6
	 *
	 */
	private class Message {

		/**
		 * message of the message
		 */
		String message;

		/**
		 * when to delete this message
		 */
		long endTime;

		/**
		 * Message constructor
		 * 
		 * @param msg message
		 * @param end end time
		 */
		Message(String msg) {
			message = msg;
			endTime = System.currentTimeMillis() + messageLength;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int mousex = e.getX();
		int mousey = e.getY();
		if (mousey >= 90 && mousey <= 90 + sq * squareWidth) {
			if (mousex >= 370 && mousex <= 370 + sq * squareWidth) {
				selectx = (mousex - 370) / squareWidth;
				selecty = (mousey - 90) / squareWidth;
				if (selectx >= sq || selecty >= sq || game[selectx][selecty] != 0) {
					selectx = -1;
					selecty = -1;
					return;
				}
			} else if (mousex >= 1280 && mousex <= 1340) {
				userFillIn((mousey - 90) / squareWidth + 1);
			} else {
				selectx = -1;
				selecty = -1;
			}
		}
		if (mousex >= 1380 && mousex <= 1580) {
			if (mousey >= 200 && mousey < 250) {
				save();
			} else if (mousey >= 250 && mousey <= 300) {
				load();
			} else if (mousey >= 300 && mousey <= 350) {
				checkAnswers = !checkAnswers;
				messages.add(new Message("check answers: " + checkAnswers));
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		String key = KeyEvent.getKeyText(e.getKeyCode()).toUpperCase();
		char c = key.charAt(0);
		if (selectx == -1 || selecty == -1)
			return;
		if (c <= '9' && c >= '0') {
			int currentValue = puzzle[selectx][selecty];
			// 2 digits!
			if (currentValue == 0)
				userFillIn(c - '0');
			else if (currentValue < 10) {
				userFillIn(currentValue * 10 + (c - '0'));
			} else {
				userFillIn(0);
			}
		} else {
			if (c == 'W')
				selecty--;
			else if (c == 'S')
				selecty++;
			else if (c == 'A')
				selectx--;
			else if (c == 'D')
				selectx++;
			if (selectx == sq || selectx == -1 || selecty == sq || selecty == -1) { // out of bounds
				selectx = -1;
				selecty = -1;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
}
