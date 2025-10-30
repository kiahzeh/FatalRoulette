import java.util.List;
import java.util.Scanner;

public class Menu {
	public static int choose(String title, List<String> options, Scanner scanner) {
		if (options == null || options.isEmpty()) return -1;
		int selected = 0;
		while (true) {
			Main.clearScreen();
			System.out.println(title);
			System.out.println();
			for (int i = 0; i < options.size(); i++) {
				String prefix = i == selected ? "> " : "  ";
				System.out.println(prefix + options.get(i));
			}
			System.out.println();
			System.out.print("Use W/S to move, Enter to select (or 1-9): ");
			String input = scanner.nextLine();
			if (input == null) input = "";
			input = input.trim();
			if (input.isEmpty()) {
				return selected;
			}
			char c = Character.toLowerCase(input.charAt(0));
			if (c == 'w') {
				selected = (selected - 1 + options.size()) % options.size();
				continue;
			}
			if (c == 's') {
				selected = (selected + 1) % options.size();
				continue;
			}
			if (Character.isDigit(c)) {
				int idx = (c - '0') - 1;
				if (idx >= 0 && idx < options.size()) return idx;
			}
		}
	}

	public static int chooseFromRange(String title, int min, int max, Scanner scanner) {
		int range = max - min + 1;
		int selected = 0;
		while (true) {
			Main.clearScreen();
			System.out.println(title);
			System.out.println();
			for (int i = 0; i < range; i++) {
				int val = min + i;
				String prefix = i == selected ? "> " : "  ";
				System.out.println(prefix + val);
			}
			System.out.println();
			System.out.print("Use W/S to move, Enter to select (or number): ");
			String input = scanner.nextLine();
			if (input == null) input = "";
			input = input.trim();
			if (input.isEmpty()) {
				return min + selected;
			}
			char c = input.isEmpty() ? '\0' : Character.toLowerCase(input.charAt(0));
			if (c == 'w') { selected = (selected - 1 + range) % range; continue; }
			if (c == 's') { selected = (selected + 1) % range; continue; }
			try {
				int val = Integer.parseInt(input);
				if (val >= min && val <= max) return val;
			} catch (NumberFormatException ignored) {}
		}
	}
}


