import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("==== FATAL ROULETTE ====");
		System.out.println("Survive by choosing: spin (s), advance (a), or fire (f). Type 'q' to quit.\n");

		String assetsDir = "assets";
		String gunAscii = AssetLoader.loadTextAsset(assetsDir, "gun.txt");
		String personAscii = AssetLoader.loadTextAsset(assetsDir, "person.txt");
		// Load side-view frames
		List<String> gunFrames = AssetLoader.loadFrames(assetsDir, "gun_", 5);
		List<String> personIdle = AssetLoader.loadFrames(assetsDir, "person_idle_", 5);
		List<String> personFlinch = AssetLoader.loadFrames(assetsDir, "person_flinch_", 5);
		Animator animator = new Animator(80, 90);

		boolean playAgain = true;
		while (playAgain) {
			int mode = showMainMenu(scanner);
			if (mode == 1) {
				int roundScore = playSoloRound(scanner, gunAscii, personAscii, animator, gunFrames, personIdle, personFlinch);
				System.out.println("Round over. Score: " + roundScore);
			} else if (mode == 2) {
				playMultiplayerRound(scanner, gunAscii, personAscii, animator, gunFrames, personIdle, personFlinch);
			} else {
				break;
			}
			playAgain = askYesNo(scanner, "Play again? (y/n): ");
			System.out.println();
		}

		System.out.println("Thanks for playing! Goodbye.");
		scanner.close();
	}

	private static int playSoloRound(Scanner scanner, String gunAscii, String personAscii, Animator animator, List<String> gunFrames, List<String> personIdle, List<String> personFlinch) {
		final int chambers = 6;
		int currentChamber = randomInt(0, chambers - 1);
		int bulletChamber = randomInt(0, chambers - 1);
		int score = 0;

		refreshScreen(personAscii, null, false);
		printStatus(chambers, currentChamber, score);
		while (true) {
			System.out.print("Action [s=spin, a=advance, f=fire, q=quit]: ");
			String cmd = scanner.nextLine().trim().toLowerCase();
			if (cmd.equals("q")) {
				System.out.println("You walked away.");
				return score;
			}

			if (cmd.equals("s") || cmd.equals("spin")) {
				currentChamber = randomInt(0, chambers - 1);
				refreshScreen(personAscii, null, false);
				System.out.println("You spun the cylinder.");
			} else if (cmd.equals("a") || cmd.equals("advance")) {
				currentChamber = (currentChamber + 1) % chambers;
				refreshScreen(personAscii, null, false);
				System.out.println("You advanced to the next chamber.");
			} else if (cmd.equals("f") || cmd.equals("fire")) {
				boolean dead = currentChamber == bulletChamber;
				// Side-view animation: gun (left) vs person (right)
				if (!gunFrames.isEmpty() && !personIdle.isEmpty()) {
					List<String> rightFrames = dead && !personFlinch.isEmpty() ? personFlinch : personIdle;
					animator.playFrames(gunFrames, rightFrames, 1);
				} else {
					refreshScreen(personAscii, gunAscii, true);
				}
				if (dead) {
					System.out.println("BANG! You are dead.");
					return score;
				} else {
					System.out.println("click... Safe.");
					score++;
					currentChamber = (currentChamber + 1) % chambers;
				}
			} else {
				System.out.println("Unknown command. Use s, a, f, or q.");
			}

			printStatus(chambers, currentChamber, score);
		}
	}

	private static void playMultiplayerRound(Scanner scanner, String gunAscii, String personAscii, Animator animator, List<String> gunFrames, List<String> personIdle, List<String> personFlinch) {
		final int chambers = 6;
		int currentChamber = randomInt(0, chambers - 1);
		int bulletChamber = randomInt(0, chambers - 1);

		System.out.println();
		System.out.println("=== Multiplayer Setup ===");
		int totalPlayers = Menu.chooseFromRange("Select total number of players (2-5):", 2, 5, scanner);
		int humanCount = Menu.chooseFromRange("Human players (1-" + totalPlayers + "):", 1, totalPlayers, scanner);
		int maxBots = 5 - humanCount;
		int botCount = Math.min(totalPlayers - humanCount, maxBots);
		if (totalPlayers - humanCount > maxBots) {
			botCount = maxBots;
		}
		if (humanCount + botCount < totalPlayers) {
			botCount = totalPlayers - humanCount;
		}

		List<Player> players = new ArrayList<>();
		for (int i = 1; i <= humanCount; i++) {
			System.out.print("Enter name for Player " + i + ": ");
			String name = readNonEmpty(scanner, "Player " + i);
			players.add(new Player(name, false));
		}
		for (int i = 1; i <= botCount; i++) {
			players.add(new Player("Bot-" + i, true));
		}

		System.out.println();
		System.out.println("Players:");
		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			System.out.println((i + 1) + ". " + p.name + (p.isBot ? " [BOT]" : ""));
		}

		int turn = 0;
		refreshScreen(personAscii, null, false);
		printStatus(chambers, currentChamber, 0);
		while (aliveCount(players) > 1) {
			Player current = nextAlive(players, turn);
			turn = current.index;

			System.out.println();
			System.out.println("-- " + current.name + "'s turn --");
			String action;
			if (current.isBot) {
				action = botChooseAction(currentChamber, bulletChamber, chambers);
				System.out.println(current.name + " chooses: " + action);
			} else {
				action = readAction(scanner);
				if (action.equals("q")) {
					System.out.println(current.name + " walked away and forfeits.");
					current.alive = false;
					turn = (turn + 1) % players.size();
					continue;
				}
			}

			if (action.equals("s")) {
				currentChamber = randomInt(0, chambers - 1);
			} else if (action.equals("a")) {
				currentChamber = (currentChamber + 1) % chambers;
			}

			// Target selection: self or enemy
			Player target;
			if (current.isBot) {
				target = botChooseTarget(players, current, currentChamber, bulletChamber, chambers);
				System.out.println(current.name + " aims at: " + target.name + (target.isBot ? " [BOT]" : ""));
			} else {
				target = chooseTarget(scanner, players, current);
			}

			boolean dead = currentChamber == bulletChamber;
			// Side-view: show shooter gun vs target person
			if (!gunFrames.isEmpty() && !personIdle.isEmpty()) {
				List<String> rightFrames = dead && !personFlinch.isEmpty() ? personFlinch : personIdle;
				animator.playFrames(gunFrames, rightFrames, 1);
			} else {
				refreshScreen(personAscii, gunAscii, true);
			}
			if (dead) {
				System.out.println("BANG! " + target.name + " is dead.");
				target.alive = false;
				// Reset for next survivor cycle
				currentChamber = randomInt(0, chambers - 1);
				bulletChamber = randomInt(0, chambers - 1);
			} else {
				System.out.println("click... " + target.name + " survives.");
				currentChamber = (currentChamber + 1) % chambers;
			}
			printStatus(chambers, currentChamber, 0);
			turn = (turn + 1) % players.size();
		}

		Player winner = lastAlive(players);
		System.out.println();
		if (winner != null) {
			System.out.println("Winner: " + winner.name + (winner.isBot ? " [BOT]" : ""));
		} else {
			System.out.println("No winner.");
		}
	}

	private static void printStatus(int chambers, int currentChamber, int score) {
		System.out.println("- Chamber: " + (currentChamber + 1) + "/" + chambers + " | Score: " + score);
	}

	private static boolean askYesNo(Scanner scanner, String prompt) {
		while (true) {
			System.out.print(prompt);
			String line = scanner.nextLine().trim().toLowerCase();
			if (line.equals("y") || line.equals("yes")) return true;
			if (line.equals("n") || line.equals("no")) return false;
			System.out.println("Please type 'y' or 'n'.");
		}
	}

	private static int randomInt(int minInclusive, int maxInclusive) {
		return new Random().nextInt(maxInclusive - minInclusive + 1) + minInclusive;
	}

	private static int showMainMenu(Scanner scanner) {
		java.util.List<String> options = new java.util.ArrayList<>();
		options.add("Solo");
		options.add("Multiplayer (2-5 players)");
		options.add("Quit");
		int idx = Menu.choose("==============================\n        FATAL ROULETTE\n==============================\n", options, scanner);
		return idx + 1; // 1..3
	}

	private static void refreshScreen(String personAscii, String gunAscii, boolean showGun) {
		clearScreen();
		if (personAscii != null) {
			printCenteredBlock(personAscii);
		}
		if (showGun && gunAscii != null) {
			System.out.println();
			printCenteredBlock(gunAscii);
		}
		System.out.println();
	}

	public static void clearScreen() {
		// Attempt ANSI clear; if not supported, print newlines
		try {
			System.out.print("\033[H\033[2J");
			System.out.flush();
		} catch (Exception ignored) {
			// Fallback
			for (int i = 0; i < 30; i++) System.out.println();
		}
	}

	private static void printCenteredBlock(String block) {
		String[] lines = block.replace("\r\n", "\n").replace('\r', '\n').split("\n");
		int width = 80; // simple console width hint
		for (String line : lines) {
			String trimmed = line;
			int pad = Math.max(0, (width - visibleLength(trimmed)) / 2);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < pad; i++) sb.append(' ');
			sb.append(trimmed);
			System.out.println(sb);
		}
	}

	private static int visibleLength(String s) {
		return s == null ? 0 : s.length();
	}

	private static Player chooseTarget(Scanner scanner, List<Player> players, Player current) {
		List<Integer> choices = new ArrayList<>();
		System.out.println("Choose target:");
		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			if (!p.alive) continue;
			choices.add(i);
			System.out.println("  " + i + ": " + p.name + (p == current ? " (you)" : "") + (p.isBot ? " [BOT]" : ""));
		}
		while (true) {
			System.out.print("Target index: ");
			String t = scanner.nextLine().trim();
			try {
				int idx = Integer.parseInt(t);
				if (choices.contains(idx)) return players.get(idx);
				System.out.println("Pick a valid living player index.");
			} catch (NumberFormatException e) {
				System.out.println("Enter a valid number.");
			}
		}
	}

	private static Player botChooseTarget(List<Player> players, Player current, int currentChamber, int bulletChamber, int chambers) {
		// Simple logic: if near certain death, aim at self to avoid target blame; else aim at a random opponent
		List<Player> opponents = new ArrayList<>();
		for (Player p : players) if (p.alive && p != current) opponents.add(p);
		if (opponents.isEmpty()) return current;
		int nextChamber = (currentChamber + 1) % chambers;
		if (nextChamber == bulletChamber && randomInt(1, 100) <= 40) {
			return current; // sometimes bots take the risk themselves
		}
		return opponents.get(randomInt(0, opponents.size() - 1));
	}

	private static String readAction(Scanner scanner) {
		while (true) {
			System.out.print("Choose action [s=spin+fire, a=advance+fire, f=fire, q=quit]: ");
			String a = scanner.nextLine().trim().toLowerCase();
			if (a.equals("s") || a.equals("a") || a.equals("f") || a.equals("q")) return a.substring(0, 1);
			System.out.println("Invalid. Use s, a, f, or q.");
		}
	}

	private static int readIntInRange(Scanner scanner, String prompt, int min, int max) {
		while (true) {
			System.out.print(prompt);
			String t = scanner.nextLine().trim();
			try {
				int v = Integer.parseInt(t);
				if (v < min || v > max) {
					System.out.println("Enter a number between " + min + " and " + max + ".");
					continue;
				}
				return v;
			} catch (NumberFormatException e) {
				System.out.println("Invalid number. Try again.");
			}
		}
	}

	private static String readNonEmpty(Scanner scanner, String label) {
		while (true) {
			String s = scanner.nextLine().trim();
			if (!s.isEmpty()) return s;
			System.out.print(label + " name cannot be empty. Enter again: ");
		}
	}

	private static String botChooseAction(int currentChamber, int bulletChamber, int chambers) {
		// Simple heuristic: if next chamber is risky, spin; otherwise 50/50 advance or fire
		int nextChamber = (currentChamber + 1) % chambers;
		if (nextChamber == bulletChamber) {
			return "s"; // spin to randomize away from certain death
		}
		// Otherwise mix of actions
		int r = randomInt(1, 100);
		if (r <= 45) return "a";     // advance then fire
		if (r <= 75) return "f";     // fire now
		return "s";                  // spin sometimes
	}

	private static int aliveCount(List<Player> players) {
		int c = 0;
		for (Player p : players) if (p.alive) c++;
		return c;
	}

	private static Player lastAlive(List<Player> players) {
		Player w = null;
		for (Player p : players) if (p.alive) w = p;
		return w;
	}

	private static Player nextAlive(List<Player> players, int startIndex) {
		int i = startIndex;
		for (int step = 0; step < players.size(); step++) {
			Player p = players.get(i);
			p.index = i;
			if (p.alive) return p;
			i = (i + 1) % players.size();
		}
		return players.get(startIndex);
	}

	private static class Player {
		final String name;
		final boolean isBot;
		boolean alive = true;
		int index = 0;
		Player(String name, boolean isBot) {
			this.name = name;
			this.isBot = isBot;
		}
	}
}

