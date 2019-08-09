package game;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class ResourceLoader implements ImageObserver {


	private Map<String, BufferedImage> images = new HashMap<String, BufferedImage>();
	private Map<String, AudioClip> sounds = new HashMap<String, AudioClip>();

	private static ResourceLoader instance = new ResourceLoader();


	private ResourceLoader() {
	}

	public static ResourceLoader getInstance() {
		return instance;
	}

	public void cleanup() {
		for (AudioClip sound : sounds.values()) {
			sound.stop();
		}

	}

	public AudioClip getSound(String name) {
		AudioClip sound = sounds.get(name);
		if (null != sound)
			return sound;

		URL url = null;
		try {
			url = getClass().getClassLoader().getResource("res/" + name);
			sound = Applet.newAudioClip(url);
			sounds.put(name, sound);
		} catch (Exception e) {
			System.err.println("Cound not locate sound " + name + ": " + e.getMessage());
		}

		return sound;
	}

	/**
	 * creates a compatible image in memory, faster than using the original image format
	 *
	 * @param width        image width
	 * @param height       image height
	 * @param transparency type of transparency
	 * @return a compatible BufferedImage
	 */
	public static BufferedImage createCompatible(int width, int height,
												 int transparency) {
		GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		BufferedImage compatible = gc.createCompatibleImage(width, height,
				transparency);
		return compatible;
	}

	/**
	 * check if image is cached, if not, load it
	 *
	 * @param name
	 * @return
	 */
	public BufferedImage getSprite(String name) {
		BufferedImage image = images.get(name);
		if (null != image)
			return image;

		URL url = null;
		try {
			url = getClass().getClassLoader().getResource("res/" + name);
			image = ImageIO.read(url);
			//store a compatible image instead of the original format
			BufferedImage compatible = createCompatible(image.getWidth(), image.getHeight(), Transparency.BITMASK);
			compatible.getGraphics().drawImage(image, 0, 0, this);

			images.put(name, compatible);
		} catch (Exception e) {
			System.err.println("Cound not locate image " + name + ": " + e.getMessage());
		}

		return image;
	}

	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		return (infoflags & (ALLBITS | ABORT)) == 0;
	}

	/*public static void createFont() {
		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			InputStream inStream = MooseTheGame.class.getResourceAsStream("res/BitPotionExt.ttf");
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, inStream));
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
	}*/

	public static void createFont() {

		// TODO: How to load this from relative path??
		try {
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			BufferedInputStream myStream = new BufferedInputStream(
					new FileInputStream(("Space-Invaders\\src\\main\\resources\\res\\BitPotionExt.ttf")));
			Font bitPotion = Font.createFont(Font.TRUETYPE_FONT, myStream);
			//Font bitPotion = ttfBase.deriveFont(Font.PLAIN, 32);
			ge.registerFont(bitPotion);
		} catch (Exception ex) {
			ex.printStackTrace();
			System.err.println("Font not loaded.");
		}
	}
}
