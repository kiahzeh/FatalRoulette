import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AssetLoader {
	public static String loadTextAsset(String assetsDir, String name) {
		Path path = Paths.get(assetsDir, name);
		if (!Files.exists(path)) return null;
		try {
			return Files.readString(path, StandardCharsets.UTF_8);
		} catch (IOException e) {
			return null;
		}
	}

	public static List<String> loadFrames(String assetsDir, String baseNamePrefix, int maxFramesToTry) {
		List<String> frames = new ArrayList<>();
		for (int i = 0; i < maxFramesToTry; i++) {
			String file = baseNamePrefix + i + ".txt";
			String content = loadTextAsset(assetsDir, file);
			if (content != null) frames.add(content);
		}
		return frames;
	}
}


