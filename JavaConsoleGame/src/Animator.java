import java.util.ArrayList;
import java.util.List;

public class Animator {
	private final int consoleWidth;
	private final int frameDelayMs;

	public Animator(int consoleWidth, int frameDelayMs) {
		this.consoleWidth = consoleWidth;
		this.frameDelayMs = frameDelayMs;
	}

	public void renderSideBySide(String leftBlock, String rightBlock) {
		String[] left = normalize(leftBlock);
		String[] right = normalize(rightBlock);
		int gap = 6;
		int height = Math.max(left.length, right.length);
		for (int row = 0; row < height; row++) {
			String l = row < left.length ? left[row] : "";
			String r = row < right.length ? right[row] : "";
			int leftPad = Math.max(0, (consoleWidth - (visibleLength(l) + gap + visibleLength(r))) / 2);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < leftPad; i++) sb.append(' ');
			sb.append(l);
			for (int i = 0; i < gap; i++) sb.append(' ');
			sb.append(r);
			System.out.println(sb);
		}
	}

	public void playFrames(List<String> leftFrames, List<String> rightFrames, int loops) {
		if (leftFrames == null) leftFrames = new ArrayList<>();
		if (rightFrames == null) rightFrames = new ArrayList<>();
		int max = Math.max(leftFrames.size(), rightFrames.size());
		if (max == 0) return;
		for (int k = 0; k < loops; k++) {
			for (int i = 0; i < max; i++) {
				String l = i < leftFrames.size() ? leftFrames.get(i) : leftFrames.get(leftFrames.size() - 1);
				String r = i < rightFrames.size() ? rightFrames.get(i) : rightFrames.get(rightFrames.size() - 1);
				Main.clearScreen();
				renderSideBySide(l, r);
				try { Thread.sleep(frameDelayMs); } catch (InterruptedException ignored) {}
			}
		}
	}

	private static String[] normalize(String block) {
		if (block == null) return new String[]{""};
		return block.replace("\r\n", "\n").replace('\r', '\n').split("\n");
	}

	private static int visibleLength(String s) {
		return s == null ? 0 : s.length();
	}
}


