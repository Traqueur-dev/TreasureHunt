package fr.traqueur.treasurehunt.api.utils.jsons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class DiscUtil {
	// -------------------------------------------- //
	// CONSTANTS
	// -------------------------------------------- //

	private final static String UTF8 = "UTF-8";

	// -------------------------------------------- //
	// BYTE
	// -------------------------------------------- //

	public static byte[] readBytes(File file) throws IOException {
		int length = (int) file.length();
		byte[] output = new byte[length];
		InputStream in = new FileInputStream(file);
		int offset = 0;
		// normally it should be able to read the entire file with just a single
		// iteration below, but it depends on the whims of the FileInputStream
		while (offset < length) {
			offset += in.read(output, offset, (length - offset));
		}
		in.close();
		return output;
	}

	public static void writeBytes(File file, byte[] bytes) throws IOException {
		File parent = file.getParentFile();
		if (parent != null && !parent.exists())
			parent.mkdirs();
		FileOutputStream out = new FileOutputStream(file);
		out.write(bytes);
		out.close();
	}

	// -------------------------------------------- //
	// STRING
	// -------------------------------------------- //

	public static void write(File file, String content) throws IOException {
		writeBytes(file, utf8(content));
	}

	public static String read(File file) throws IOException {
		return utf8(readBytes(file));
	}

	// -------------------------------------------- //
	// CATCH
	// -------------------------------------------- //

	public static boolean writeCatch(File file, String content) {
		try {
			write(file, content);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static String readCatch(File file) {
		try {
			return read(file);
		} catch (IOException e) {
			return null;
		}
	}

	// -------------------------------------------- //
	// FILE DELETION
	// -------------------------------------------- //

	public static boolean deleteRecursive(File path) throws FileNotFoundException {
		if (!path.exists())
			throw new FileNotFoundException(path.getAbsolutePath());
		boolean ret = true;
		if (path.isDirectory()) {
			for (File f : path.listFiles()) {
				ret = ret && deleteRecursive(f);
			}
		}
		return ret && path.delete();
	}

	// -------------------------------------------- //
	// UTF8 ENCODE AND DECODE
	// -------------------------------------------- //

	public static byte[] utf8(String string) {
		try {
			return string.getBytes(UTF8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String utf8(byte[] bytes) {
		try {
			return new String(bytes, UTF8);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
}