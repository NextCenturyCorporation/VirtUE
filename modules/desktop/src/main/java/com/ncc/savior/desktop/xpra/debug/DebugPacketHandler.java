/*
 * Copyright (C) 2019 Next Century Corporation
 * 
 * This file may be redistributed and/or modified under either the GPL
 * 2.0 or 3-Clause BSD license. In addition, the U.S. Government is
 * granted government purpose rights. For details, see the COPYRIGHT.TXT
 * file at the root of this project.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 * 
 * SPDX-License-Identifier: (GPL-2.0-only OR BSD-3-Clause)
 */
package com.ncc.savior.desktop.xpra.debug;

import java.awt.image.RenderedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ncc.savior.desktop.xpra.XpraClient;
import com.ncc.savior.desktop.xpra.application.swing.SwingImageEncoder;
import com.ncc.savior.desktop.xpra.protocol.IPacketHandler;
import com.ncc.savior.desktop.xpra.protocol.ImageEncoding;
import com.ncc.savior.desktop.xpra.protocol.packet.PacketType;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.DrawPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.IImagePacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.Packet;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.UnknownPacket;
import com.ncc.savior.desktop.xpra.protocol.packet.dto.WindowPacket;

/**
 * Attached to the {@link XpraClient} for debugging. Prints all packets to the
 * file system including images of the {@link DrawPacket}s.
 *
 */
public class DebugPacketHandler implements IPacketHandler {
	private static final Logger logger = LoggerFactory.getLogger(DebugPacketHandler.class);

	private final HashSet<PacketType> types;
	private File debugDirectory;

	private boolean writePingPackets = false;
	private boolean logPingPackets = false;

	private FileWriter pointerLog;
	private FileWriter pingLog;

	private LinkedBlockingQueue<DebugPackage> queue;

	private Thread thread;

	public DebugPacketHandler(File directory) {
		this.queue = new LinkedBlockingQueue<DebugPackage>();
		if (directory == null) {
			directory = getDefaultTimeBasedDirectory();
		}
		debugDirectory = directory;
		types = new HashSet<PacketType>();
		try {
			pingLog = new FileWriter(new File(debugDirectory, "PingLog.txt"));
		} catch (IOException e) {
			logger.warn("Unable to create Ping Packet log FileWriter", e);
		}
		try {
			pointerLog = new FileWriter(new File(debugDirectory, "PointerLog.txt"));
		} catch (IOException e) {
			logger.warn("Unable to create Pointer Packet log FileWriter", e);
		}
		startWritingThread();
	}

	private void startWritingThread() {
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					DebugPackage p;
					try {
						p = queue.take();
						doWritePacket(p);
					} catch (InterruptedException e) {
						logger.warn("");
					}

				}
			}
		}, "DebugWriter");
		thread.setDaemon(true);
		thread.start();
	}

	public static void clearDefaultDebugFolder() {
		File dir = new File("debug");
		deleteContents(dir);
	}

	public static File getDefaultTimeBasedDirectory() {
		long timestamp = System.currentTimeMillis();
		File dir = new File("debug/" + timestamp + "/");
		dir.mkdirs();
		dir.mkdirs();
		return dir;
	}

	public static void deleteContents(File dir) {
		// try {
		if (dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				deleteContents(file);
			}
		}
		dir.delete();
		// } catch (IOException e) {
		// logger.warn("Error attempting to clear debug directory");
		// }
	}

	@Override
	public void handlePacket(Packet packet) {
		DebugPackage p = new DebugPackage(packet, System.currentTimeMillis());
		try {
			queue.put(p);
		} catch (InterruptedException e) {
			logger.warn("Interrupted");
		}
	}

	protected void doWritePacket(DebugPackage debugPackage) {
		Packet packet = debugPackage.getPacket();
		long timestamp = debugPackage.getTimestamp();
		switch (packet.getType()) {
		case WINDOW_ICON:
		case DRAW:
			IImagePacket p = (IImagePacket) packet;
			writeImage(p, timestamp);
			writePacket(packet, timestamp);
			break;
		case PING_ECHO:
		case PING:
			if (writePingPackets) {
				writePacket(packet, timestamp);
			}
			if (logPingPackets) {
				logger.info(packet.toString());
			}
			writePingPackets(packet, timestamp);
			break;
		case POINTER_POSITION:
			writePointerPositionPackets(packet, timestamp);
			break;
		default:
			writePacket(packet, timestamp);
			break;
		}

	}

	private void writePointerPositionPackets(Packet packet, long timestamp) {
		if (pointerLog != null) {
			try {
				pointerLog.write(packet.toString() + " " + timestamp + "\n");
				pointerLog.flush();
			} catch (IOException e) {
				logger.warn("Unable to write packet to pointer log.  Packet=" + packet, e);
			}
		}
	}

	private void writePingPackets(Packet packet, long timestamp) {
		if (pingLog != null) {
			try {
				pingLog.write(packet.toString() + " " + timestamp + "\n");
				pingLog.flush();
			} catch (IOException e) {
				logger.warn("Unable to write packet to ping log.  Packet=" + packet, e);
			}
		}

	}

	private void writePacket(Packet p, long timestamp) {
		BufferedWriter writer = null;

		try {
			String typeString;
			String windowPostfix = "";
			if (p instanceof UnknownPacket) {
				byte[] first = (byte[]) ((UnknownPacket) p).getList().get(0);
				typeString = new String(first);

			} else {
				typeString = p.getType().toString();
				if (p instanceof WindowPacket) {
					windowPostfix = "-" + ((WindowPacket) p).getWindowId();
				}
			}
			File file = new File(debugDirectory, timestamp + "-" + typeString + windowPostfix + ".txt");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(p.toString());

		} catch (IOException e) {
			logger.debug("Unable to write packet for Packet=" + p, e);
		} catch (ClassCastException e) {
			logger.debug("Unable to write packet for Packet=" + p, e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					logger.debug("Error closing writer", e);
				}
			}
		}
	}

	private void writeImage(IImagePacket p, long timestamp) {
		try {
			String extension = getExtensionFromEncoding(p.getEncoding());
			RenderedImage img;
			if (extension == null && p instanceof DrawPacket) {
				DrawPacket dp = (DrawPacket) p;
				img = SwingImageEncoder.decodeImage(ImageEncoding.rgb24, p.getData(), dp.getWidth(), dp.getHeight());
				extension = "png";
			} else {
				img = ImageIO.read(new ByteArrayInputStream(p.getData()));
			}
			if (img != null) {
				File file = new File(debugDirectory, timestamp + "." + extension);
				ImageIO.write(img, extension, file);
			}
		} catch (IOException e) {
			logger.debug("Unable to draw image for DrawPacket=" + p);
		} catch (RuntimeException e) {
			logger.debug("Unknown error", e);
		}
	}

	private String getExtensionFromEncoding(ImageEncoding encoding) {
		switch (encoding) {
		case png:
		case pngL:
		case pngP:
			return "png";
		case jpeg:
			return "jpeg";
		case rgb24:
			return null;
		default:
			break;
		}
		return null;
	}

	@Override
	public Set<PacketType> getValidPacketTypes() {
		return types;
	}

	public static class DebugPackage {
		private Packet packet;
		private long timestamp;
		private boolean fromServer;

		public DebugPackage(Packet packet, long timestamp, boolean fromServer) {
			this.packet = packet;
			this.timestamp = timestamp;
			this.fromServer = fromServer;
		}

		public DebugPackage(Packet packet, long timestamp) {
			this.packet = packet;
			this.timestamp = timestamp;
		}

		public Packet getPacket() {
			return packet;
		}

		public long getTimestamp() {
			return timestamp;
		}

		public boolean isFromServer() {
			return fromServer;
		}
	}
}
