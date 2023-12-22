package Utils;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;

/**
 * Класс пишет ГИФ изображения по входным данным
 * 
 * @author Kerravitarr
 *
 */
public class GifSequenceWriter {
	/**Интерфейс для рисования кадров*/
	public static interface FrameFun{
		/**Нужно нарисовать картинку на переданном объекте*/
		public void paint(Graphics2D g);
	}
	
	/**Сам писака, поток, куда мы пишем*/
	protected ImageWriter writer;
	/**Параметры записи - зацикленость, задержка и т.д.*/
	protected ImageWriteParam params;
	/**Метаданные файла*/
	protected IIOMetadata metadata;
	/**Размер гифки*/
	protected Dimension size;
	/**Тип гифки*/
	protected int typeImg;

	/**
	 * @param out - поток, файловый, куда будет писаться гифка
	 * @param imageType - стандартный код из набора BufferedImage, писывающий тип гифки
	 * @param delay - частота смена кадров, мс
	 * @param loop - зацикливать или нет гифку
	 * @throws IOException
	 * 
	 * 
     * @see java.awt.image.BufferedImage
     * @see java.awt.image.BufferedImage#TYPE_INT_RGB
     * @see java.awt.image.BufferedImage#TYPE_INT_ARGB
     * @see java.awt.image.BufferedImage#TYPE_INT_ARGB_PRE
     * @see java.awt.image.BufferedImage#TYPE_INT_BGR
     * @see java.awt.image.BufferedImage#TYPE_3BYTE_BGR
     * @see java.awt.image.BufferedImage#TYPE_4BYTE_ABGR
     * @see java.awt.image.BufferedImage#TYPE_4BYTE_ABGR_PRE
     * @see java.awt.image.BufferedImage#TYPE_USHORT_565_RGB
     * @see java.awt.image.BufferedImage#TYPE_USHORT_555_RGB
     * @see java.awt.image.BufferedImage#TYPE_BYTE_GRAY
     * @see java.awt.image.BufferedImage#TYPE_USHORT_GRAY
     * @see java.awt.image.BufferedImage#TYPE_BYTE_BINARY
     * @see java.awt.image.BufferedImage#TYPE_BYTE_INDEXED
	 */
	public GifSequenceWriter(ImageOutputStream out, int imageType, int delay, boolean loop, Dimension maxSize) throws IOException {
		writer = javax.imageio.ImageIO.getImageWritersBySuffix("gif").next();
		params = writer.getDefaultWriteParam();

		typeImg = imageType;
		ImageTypeSpecifier imageTypeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(typeImg);
		metadata = writer.getDefaultImageMetadata(imageTypeSpecifier, params);

		configureRootMetadata(delay, loop);

		writer.setOutput(out);
		writer.prepareWriteSequence(null);
		size = maxSize;
	}
	/**
	 * Создаёт гифку с частотой смены кадров по 40 мс (25 кадров в сек)
	 * @param fileName - Имя файла для записи
	 * @param loop - зацикливать или нет гифку
	 * @param maxSize - размер гифки
	 * @throws IOException
	 */
	public GifSequenceWriter(String fileName, boolean loop, Dimension maxSize) throws IOException {
		this(new FileImageOutputStream(new File(fileName)),java.awt.image.BufferedImage.TYPE_INT_ARGB,40,loop,maxSize);
	}

	/**
	 * Записывает метаданные в файл
	 * @param delay - задержка воспроизведенеия
	 * @param loop - зацикленность
	 * @throws IIOInvalidTreeException - возникает при ошибке чтения файла метаданных
	 */
	private void configureRootMetadata(int delay, boolean loop) throws IIOInvalidTreeException {
		String metaFormatName = metadata.getNativeMetadataFormatName();
		IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree(metaFormatName);

		IIOMetadataNode graphicsControlExtensionNode = getNode(root, "GraphicControlExtension");
		graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
		graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
		graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
		graphicsControlExtensionNode.setAttribute("delayTime", Integer.toString(delay / 10));
		graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

		IIOMetadataNode commentsNode = getNode(root, "CommentExtensions");
		commentsNode.setAttribute("CommentExtension", "Created by: Kerravitarr");

		IIOMetadataNode appExtensionsNode = getNode(root, "ApplicationExtensions");
		IIOMetadataNode child = new IIOMetadataNode("ApplicationExtension");
		child.setAttribute("applicationID", "NETSCAPE");
		child.setAttribute("authenticationCode", "2.0");

		int loopContinuously = loop ? 0 : 1;
		child.setUserObject(
				new byte[] { 0x1, (byte) (loopContinuously & 0xFF), (byte) ((loopContinuously >> 8) & 0xFF) });
		appExtensionsNode.appendChild(child);
		metadata.setFromTree(metaFormatName, root);
	}

	/**
	 * Вспомогательная функция, возвращает узел метаданных по назвнаию
	 * @param rootNode - старший узел
	 * @param nodeName - имя узла, который надо получить
	 * @return
	 */
	private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
		int nNodes = rootNode.getLength();
		for (int i = 0; i < nNodes; i++) {
			if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
				return (IIOMetadataNode) rootNode.item(i);
			}
		}
		IIOMetadataNode node = new IIOMetadataNode(nodeName);
		rootNode.appendChild(node);
		return (node);
	}
	
	/**Дорисовывает новый кадр*/
	public synchronized void nextFrame(FrameFun f) throws IOException {
		BufferedImage image = new BufferedImage(size.width, size.height,typeImg);
		f.paint((Graphics2D)image.getGraphics());
		writer.writeToSequence(new IIOImage(image, null, metadata), params);
	}

	/**
	 * Обязательное закрытие потоков
	 * @throws IOException
	 */
	public synchronized void close() throws IOException {
		writer.endWriteSequence();
	}
}
