package org.xdef.sys;

import org.xdef.msg.SYS;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/** Collection of utilities to handle with files.
 * @author Vaclav Trojan
 */
public class FUtils {
	/** Temporary file items extension (used in secureCopy). */
	public static final String TEMPORARY_FILE_EXTENSION = ".tmp";
	/** Backup file items extension (used in secureCopy). */
	public static final String BACKUP_FILE_EXTENSION = ".bak";

	/** Don't allow user to instantiate this class. */
	FUtils() {}

	/** Get usable space of file system of the file. Note this method returns
	 * the reasonable value starting from java version 1.6. Otherwise it
	 * returns Long.MAX_VALUE.
	 * @param file the file to be checked.
	 * @return number of bytes available in the filesystem of the file or return
	 * Long.MAX_VALUE if filesystem not exists or if it is not allowed to write
	 * to the file.
	 */
	public static final long getUsableSpace(final File file) {
		File f = file;
		for (;;) {
			if (f.isDirectory()) {
				 return f.exists() ? f.getUsableSpace() : Long.MAX_VALUE;
			}
			String s = file.getAbsolutePath().replace('\\', '/');
			int ndx = s.lastIndexOf('/');
			if (ndx <= 0) {
				return Long.MAX_VALUE; // we do not know, so we return max. size
			}
			f = new File(s.substring(0, ndx));
		}
	}

	/** Check directory and create it if it doesn't exist.
	 * @param path full path to the directory.
	 * @param create If true the directory is created if it not exists yet.
	 * @return java.io.File object with directory.
	 * @throws SException
	 * <ul>
	 * <li>SYS020 Can't create directory: {dir}</li>
	 * <li>SYS025 Directory doesn't exist or isn't accessible: {dir}</li>
	 * <li>SYS032 File is not directory: {dir}</li>
	 * </ul>
	 */
	public static final File checkDir(final String path, final boolean create)
	throws SException {
		return checkDir(new File(path), create);
	}

	/** Check directory and create it if it doesn't exist.
	 * @param dir File object representing the directory.
	 * @param create If true the directory is created if it not exists yet.
	 * @return java.io.File object with directory.
	 * @throws SException
	 * <ul>
	 * <li>SYS020 Can't create directory: {dir}</li>
	 * <li>SYS025 Directory doesn't exist or isn't accessible: {dir}</li>
	 * <li>SYS032 File is not directory: {dir}</li>
	 * </ul>
	 */
	public static final File checkDir(final File dir, final boolean create)
	throws SException {
		if (!dir.exists()) {
			if (!create) {
				//Directory doesn't exist or isn't accessible: &{0}
				throw new SException(SYS.SYS025, dir);
			}
			if (!dir.mkdirs()) {
				//Can't create directory: &{0}
				throw new SException(SYS.SYS020, dir);
			}
		}
		if (dir.isDirectory()) {
			return dir;
		}
		throw new SException(SYS.SYS032, dir); //File is not directory: &{0}
	}

	/** Check if path points to a file and create it if it doesn't exist (if the
	 * argument create is true).
	 * @param fname full pathname to the file.
	 * @param create If true the directory is created if it not exists yet.
	 * @return java.io.File object created according to arguments.
	 * @throws SException
	 * <ul>
	 * <li>SYS020 Can't create directory: {dir}</li>
	 * <li>SYS025 Directory doesn't exist or isn't accessible: {dir}</li>
	 * <li>SYS032 File is not directory: {dir}</li>
	 * </ul>
	 */
	public static final File checkFile(final String fname, final boolean create)
	throws SException {
		return checkFile(new File(fname), create);
	}

	/** Check if path points to a file and create it if it doesn't exist (if the
	 * argument create is true).
	 * @param file file to be checked.
	 * @param create If true the directory is created if it not exists yet.
	 * @return java.io.File object created according to arguments.
	 * @throws SException
	 * <ul>
	 * <li>SYS022 The file is directory: {file}</li>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS026 FCan't create file: {file}</li>
	 * </ul>
	 */
	public static final File checkFile(final File file, final boolean create)
	throws SException {
		if (!file.exists()) {
			if (create) {
				try {
					if (!file.createNewFile()) {
						//Can't create file: &{0}
						throw new SException(SYS.SYS026, file);
					}
				} catch (IOException ex) {
					//Can't create file: &{0}
					throw new SException(SYS.SYS026, file);
				}
			} else {
				//File doesn't exist: &{0}
				throw new SException(SYS.SYS024, file);
			}
		}
		if (!file.isFile()) {
			throw new SException(SYS.SYS022, file);//The file is directory: &{0}
		}
		return file;
	}

	/** Rename file.
	 * @param f1 file to be renamed.
	 * @param f2 new file.
	 * @throws SException SYS031 Can't rename file {f1} to {f2}
	 */
	public static final void renameFile(final File f1, final File f2)
	throws SException {
		if (!f1.renameTo(f2)) {
			/*Can't rename file &{0} to &{1}*/
			throw new SException(SYS.SYS031, f1, f2);
		}
	}

	/** delete file.
	 * @param f file to be deleted.
	 * @throws SException SYS021 Can't delete file: {file}
	 */
	public static final void deleteFile(final File f) throws SException {
		if (!f.exists()) {
			return;
		}
		if (!f.delete()) {
			throw new SException(SYS.SYS021, f); //Can't delete file: &amp;{0}
		}
	}

	/** Get directory path terminated with file separator.
	 * @param dir file with a directory.
	 * @return string with the path terminated with file separator.
	 * @throws SException
	 * <ul>
	 * <li>SYS032 File is not directory.</li>
	 * <li>SYS034 IO error detected on file.</li>
	 * </ul>
	 */
	public static final String getDirPath(final File dir) throws SException {
		try {
			if (dir.isDirectory()) {
				return dir.getCanonicalPath() + File.separatorChar;
			}
			throw new SException(SYS.SYS032, dir); //File is not directory: &{0}
		} catch(IOException ex) {
			//IO error detected on &{0}&{1}{, reason: }
			throw new SException(SYS.SYS034, dir, ex);
		}
	}

	/** Compare files. Returns -1 if the file contents are equal,
	 * otherwise returns the offset of the first difference.
	 * @param fn1 name of the first file.
	 * @param fn2 name of the  second file.
	 * @return -1 if both files are equal, otherwise return the offset of the
	 * first difference. If a file doesn't exist or if it is not readable
	 * return 0;
	 */
	public static final long compareFile(final String fn1, final String fn2) {
		return compareFile(new File(fn1), new File(fn2));
	}

	/** Compare files. Returns -1 if the file contents are equal,
	 * otherwise returns the offset of the first difference.
	 * @param f1 first file.
	 * @param f2 second file.
	 * @return -1 if the files are equal, otherwise return the offset of the
	 * first difference. If a file doesn't exist or if it is not readable
	 * return 0;
	 */
	public static final long compareFile(final File f1, final File f2) {
		long result = 0;
		InputStream fs1 = null;
		InputStream fs2 = null;
		if (f1.exists() && f1.canRead() && f2.exists() && f2.canRead()) {
			try {
				fs1 = new FileInputStream(f1);
				fs2 = new FileInputStream(f2);
				result = compareFile(fs1, fs2);
			} catch (Exception ex) {}
		}
		try {fs1.close();} catch (Exception ex) {}
		try {fs2.close();} catch (Exception ex) {}
		return result;
	}

	/** Check if contents of files are equal. Returns true if files are equal,
	 * otherwise return false.
	 * @param f1 first file.
	 * @param f2 second file.
	 * @return true if contents of files are equal, false otherwise.
	 * If a file doesn't exist or if it is not readable return also false.
	 */
	public static final boolean filesEqual(final File f1, final File f2) {
		if (f1.exists() && f2.exists() && f1.length() == f2.length()) {
			if (f1.equals(f2)) {
				return true;
			}
			FileInputStream f1is = null;
			FileInputStream f2is = null;
			try {
				f1is = new FileInputStream(f1);
				f2is = new FileInputStream(f2);
				boolean result = compareFile(f1is, f2is) == -1L;
				return result;
			} catch (Exception ex) {
			} finally {
				try {
					f1is.close();
				} catch (Exception exx) {}
				try {
					f2is.close();
				} catch (Exception exx) {}
			}
		}
		return false;
	}

	/** Compare streams. Returns -1 if contents are equal,
	 * otherwise returns the index of the first difference.
	 * @param f1 first file.
	 * @param f2 second file.
	 * @return -1 if the files are equal, otherwise the offset of the
	 * first difference. If a file doesn't exist or if it is not readable
	 * return 0;
	 */
	public static final long compareFile(final InputStream f1,
		final InputStream f2) {
		long diff = 0;
		try {
			byte[] buf1 = new byte[4096], buf2 = new byte[4096];
			while (true) {
				int len1 = f1.read(buf1);
				if (len1 == 0) {// may happen!
					sleep1();
				} else {
					if (len1 < 0) {
						if (f2.read() < 0) {
							diff = -1L;
						}
						break;
					}
					int len2 = f2.read(buf2, 0, len1);
					int len = len2;
					int off = 0;
					while (len >= 0 && len2 < len1) {
						if (len == 0) {
							sleep1();
							len = f2.read(buf2, off, len1 - len2);
						} else {
							off += len;
							len = f2.read(buf2, off, len1 - len2);
							if (len == -1) {
								throw new Exception();
							}
							len2 += len;
						}
					}
					//compare buffers
					for (int i = 0; i < len1; i++) {
						if (buf1[i] != buf2[i]) {
							diff = diff + i;
							throw new Exception();
						}
					}
					diff += len1;
				}
			}
		} catch (Exception ex) {}
		try {f1.close();} catch (Exception ex) {}
		try {f2.close();} catch (Exception ex) {}
		return diff;
	}

	private static void sleep1() {
		try {
			Thread.sleep(1);
		} catch (InterruptedException ex) {}
	}

	/** Read integer from InputStream as two bytes.
	 * @param is input stream.
	 * @return number from input stream (always positive in range 0..65535).
	 * @throws IOException if an error occurs.
	 */
	public static final int readInt2(final InputStream is) throws IOException {
		int i = is.read();
		if (i < 0) {
			throw new SIOException(SYS.SYS019); //Unexpected end of data
		}
		int j = is.read();
		if (j < 0) {
			throw new SIOException(SYS.SYS019); //Unexpected end of data
		}
		return (i << 8) + j;
	}

	/** Read integer from InputStream.
	 * @param is input stream.
	 * @return integer from input stream.
	 * @throws IOException if an error occurs.
	 */
	public static final int readInt4(final InputStream is) throws IOException {
		int i = is.read();
		int j = is.read();
		int k = is.read();
		int l = is.read();
		if (i < 0 || j < 0 || k < 0 || l < 0) {
			throw new SIOException(SYS.SYS019); //Unexpected end of data
		}
		return (i << 24) + (j << 16) + (k << 8) + l;
	}

	/** Write integer as two bytes to the OutputStream.
	 * @param i number to be written as two bytes (must be in limits
	 * -32768 .. 32767).
	 * @param os output stream.
	 * @throws IOException if an error occurs.
	 */
	public static void writeInt2(final int i, final OutputStream os)
		throws IOException {
		if (i < Short.MIN_VALUE || i > Short.MAX_VALUE) {
			throw new SIOException(SYS.SYS016); //Argument out of bounds
		}
		os.write(new byte[] {(byte) (i >> 8), (byte) i});
	}

	/** Write integer as four bytes to the OutputStream.
	 * @param i integer to be written.
	 * @param os output stream.
	 * @throws IOException if an error occurs.
	 */
	public static final void writeInt4(final int i, final OutputStream os)
	throws IOException {
		os.write(new byte[] {
			(byte) (i >> 24), (byte) (i >> 16), (byte) (i >> 8), (byte) i});
	}

	/** Read integer from InputStream.
	 * @param is input stream.
	 * @return integer from input stream.
	 * @throws IOException if an error occurs.
	 */
	public static final long readInt8(final InputStream is) throws IOException {
		long result = 0;
		for (int i = 0; i < 8; i++) {
			int j = is.read();
			if (j < 0) {
				throw new SIOException(SYS.SYS019); //Unexpected end of data
			}
			result = (result<<8) + j;
		}
		return result;
	}

	/** Write long integer to OutputStream.
	 * @param m long to be written.
	 * @param os output stream.
	 * @throws IOException if an error occurs.
	 */
	public static final void writeInt8(final long m, final OutputStream os)
		throws IOException {
		byte[] b = new byte[8];
		long n = m;
		for (int i = 7; i >= 0; i--) {
			b[i] = (byte) n;
			n = n >> 8;
		}
		os.write(b);
	}

	/** Read block of bytes from InputStream in given length.
	 * @param is input stream.
	 * @param length length of data block to be read.
	 * @return required length.
	 * @throws IOException if an error occurs.
	 */
	public static final byte[] readBlock(final InputStream is,
		final int length) throws IOException {
		if (length <= 0) {
			if (length == 0) {
				return new byte[0];
			}
			return null;
		}
		int len;
		byte[] data = new byte[len = length];
		int off = 0;
		int i;
		while ((i = is.read(data, off, len)) > 0) {
			if (i == 0) {
				sleep1();
			} else {
				if ((len -= i) == 0) {
					return data;
				}
				off += i;
			}
		}
		throw new SIOException(SYS.SYS019); //Unexpected end of data
	}

	/** Read first length block from two bytes from the InputStream
	 * and then read the block of bytes in given length.
	 * @param is input stream.
	 * @return byte array with data from input.
	 * @throws IOException if an error occurs.
	 */
	public static final byte[] readShortBlock(final InputStream is)
		throws IOException {
		return readBlock(is, readInt2(is));
	}

	/** Read first length block from four bytes from the InputStream
	 * and then read th block of bytes in given length.
	 * @param is input stream.
	 * @return byte array with data from input.
	 * @throws IOException if an error occurs.
	 */
	public static final byte[] readLongBlock(final InputStream is)
		throws IOException {
		return readBlock(is, readInt2(is));
	}

	/** Write first two bytes of the length of data and then write
	 * the data block to the output stream.
	 * @param b block of bytes.
	 * @param os output stream.
	 * @throws IOException if an error occurs.
	 */
	public static final void writeShortBlock(final byte[] b,
		final OutputStream os) throws IOException {
		if (b == null || b.length == 0) {
			os.write(new byte[] {0, 0});
		} else {
			writeInt2(b.length, os);
			os.write(b);
		}
	}

	/** Write first four bytes of the length of data and then write
	 * the data block to the output stream.
	 * @param b block of bytes.
	 * @param os output stream.
	 * @throws IOException if an error occurs.
	 */
	public static final void writeLongBlock(final byte[] b,
		final OutputStream os) throws IOException {
		if (b == null || b.length == 0) {
			os.write(new byte[] {0, 0, 0, 0});
		} else {
			writeInt4(0, os);
			os.write(b);
		}
	}

	/** Copy InputStream to the file given by the name.
	 * @param inName name of input file.
	 * @param outName name of file to which the copy is made.
	 * @param append if true input is appended to output.
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS026 Can't create file: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * <li>SYS030 File already exists</li>
	 * <li>SYS038 File is too big</li>
	 * </ul>
	 */
	public static final void copyToFile(final String inName,
		final String outName,
		final boolean append) throws SException {
		copyToFile(new File(inName), new File(outName), append);
	}

	/** Copy input file to the output file given by the name.
	 * If argument append is true and if output file already exists then the
	 * input data are appended to the output data, otherwise the output file
	 * is written over by the input data.
	 * @param inFile input file.
	 * @param outFile file to which the copy is made.
	 * @param append if true the input is appended to the output file.
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS026 Can't create file: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * <li>SYS030 File already exists</li>
	 * <li>SYS036 Program exception</li>
	 * <li>SYS038 File is too big</li>
	 * </ul>
	 */
	public static final void copyToFile(final File inFile,
		final File outFile,
		final boolean append) throws SException {
		InputStream in = null;
		if (!inFile.exists()) {
			throw new SException(SYS.SYS024, inFile); //File doesn't exist: &{0}
		}
		try {
			in = new FileInputStream(inFile);
		} catch (Exception ex) {
			throw new SException(SYS.SYS024, inFile); //File doesn't exist: &{0}
		}
		if (inFile.length() + 10 > getUsableSpace(outFile)) {
			if (outFile.getParentFile() != null &&
				!outFile.getParentFile().exists()) {
				//File doesn't exist: &{0}
				throw new SException(SYS.SYS024, outFile);
			}
			throw new SException(SYS.SYS038, inFile); //File is too big: &{0}
		}
		OutputStream out = null;
		try {
			out = new FileOutputStream(outFile, append);
		} catch (Exception ex) {
			//Can't write to file: &{0}
			throw new SException(SYS.SYS023, outFile);
		}
		try {
			copyToFile(in,
				inFile.getCanonicalPath(), out, outFile.getCanonicalPath());
		} catch (Exception ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
		try {in.close();} catch (Exception ex) {}
		try {out.close();} catch (Exception ex) {}
	}

	/** Copy input file to the output file given by the name.
	 * @param inFile input file.
	 * @param outFile file to which the copy is made. If the file already
	 * exists it is written over by the input data.
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS026 Can't create file: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * <li>SYS030 File already exists</li>
	 * <li>SYS036 Program exception</li>
	 * <li>SYS038 File is too big</li>
	 * </ul>
	 */
	public static final void copyToFile(final File inFile,
		final File outFile) throws SException {
		copyToFile(inFile, outFile, false);
	}

	/** Copy InputStream to the file given by the name.
	 * @param is InputStream.
	 * @param fname name of file to which the copy is made.
	 * @param append if true the input is appended to output.
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS026 Can't create file: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * <li>SYS030 File already exists</li>
	 * </ul>
	 */
	public static final void copyToFile(final InputStream is,
		final String fname,
		final boolean append) throws SException {
		copyToFile(is, new File(fname), append);
	}

	/** Copy InputStream to the file.
	 * @param is InputStream.
	 * @param file file to which the copy is made.
	 * @param append if true the input is appended to output.
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS026 Can't create file: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * <li>SYS030 File already exists</li>
	 * </ul>
	 */
	public static final void copyToFile(final InputStream is,
		final File file,
		final boolean append) throws SException {
		if (!append && file.exists()) {
			throw new SException(SYS.SYS030, file); //File already exists: &{0}
		}
		OutputStream fos = null;
		try {
			fos = new FileOutputStream(file, append);
		} catch (IOException ex) {
			throw new SException(SYS.SYS026, file); //Can't create file: &{0}
		}
		try {
			copyToFile(is,is.getClass().getName(),fos,file.getCanonicalPath());
		} catch(IOException ex) {
			// Can't write to file: &{0}
			throw new SException(SYS.SYS023, "java.io.OutputStream");
		}
		try {fos.close();} catch (Exception ex) {}
	}

	/** Copy InputStream to the file.
	 * @param inFile input file.
	 * @param os OutputStream.
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * <li>SYS036 Program exception</li>
	 * </ul>
	 */
	public static final void copyToFile(final File inFile,
		final OutputStream os) throws SException {
		FileInputStream is = null;
		try {
			is = new FileInputStream(inFile);
		} catch (Exception ex) {
			throw new SException(SYS.SYS024, inFile); //File doesn't exist: &{0}
		}
		copyToFile(is, is.getClass().getName(), os, os.getClass().getName());
		try {is.close();} catch (Exception ex) {}
	}

	/** Copy InputStream to the file.
	 * @param is InputStream.
	 * @param os OutputStream.
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * </ul>
	 */
	public static final void copyToFile(final InputStream is,
		final OutputStream os) throws SException {
		copyToFile(is, is.getClass().getName(), os, os.getClass().getName());
	}

	/** Copy InputStream to the file.
	 * @param is InputStream.
	 * @param inFile name of input file.
	 * @param os OutputStream.
	 * @param outFile name of output file (we need it to report error message).
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * </ul>
	 */
	private static void copyToFile(final InputStream is,
		final String inFile,
		final OutputStream os,
		final String outFile) throws SException {
		int len;
		byte[] buf = new byte[4096];
		try {
			len = is.read(buf);
		} catch (IOException ex) {
			throw new SException(SYS.SYS028, inFile); //Can't read file: &{0}
		}
		while (len >= 0) {
			if (len == 0) {
				sleep1();
			} else {
				try {
					os.write(buf,0, len);
				} catch(IOException ex) {
					//Can't write to file: &{0}
					throw new SException(SYS.SYS023, outFile);
				}
			}
			try {
				len = is.read(buf);
			} catch (IOException ex) {
				throw new SException(SYS.SYS028, inFile);//Can't read file: &{0}
			}
		}
	}

	/** Check if file should be excluded
	 * @param f file to be checked.
	 * @param exc exclude list
	 * @return true if and only if the file should be skipped.
	 */
	private static boolean chkExclude(final File f, final String... exc) {
		String s = f.getAbsolutePath().replace('\\', '/');
		if (f.isDirectory() && !s.endsWith("/")) {
			s += "/";
		}
		for (String x: exc) {
			if (s.endsWith(x.replace('\\', '/'))) {
				return true;
			}
		}
		return false;
	}

	/** Copy files (and directories) from given list to the specified directory.
	 * @param from list of input files
	 * @param to directory where files to be copied.
	 * @param exclude array with exclude list. If path ends with an string from
	 * exclude list the input item is skipped. Name of an directory must end
	 * with separator character "/". If this argument is <tt>null</tt> or the
	 * empty array then no files are excluded.
	 * @param deep if false the directories are skipped.
	 * @throws SException if an error occurs.
	 */
	public static final void xcopy(final File[] from,
		final File to,
		final boolean deep,
		final String... exclude) throws SException {
		for (File x: from) {
			if (!chkExclude(x, exclude)) {
				if (x.isDirectory() && deep) {
					File newDir = new File(to, x.getName());
					newDir.mkdirs();
					xcopy(x.listFiles(), newDir, true, exclude);
				} else { //file
					File newFile = new File(to, x.getName());
					copyToFile(x, newFile);
				}
			}
		}
	}

	/** Copy files from source directory to the target directory.
	 * @param fromDir list of input files
	 * @param toDir directory where files to be copied.
	 * @param excludes array with exclude list. If path ends with an string from
	 * exclude list the input item is skipped. Name of an directory must end
	 * with separator character "/".
	 * @param deep if false the directories are skipped otherwise copy also
	 * directories.
	 * @throws SException if an error occurs.
	 */
	public static final void xcopy(final String fromDir,
		final String toDir,
		final boolean deep,
		final String... excludes) throws SException {
		File from = new File(fromDir);
		if (!from.exists() || !from.isDirectory()) {
			// Directory doesn't exist or isn't accessible: &{0}
			throw new SException(SYS.SYS025, from);
		}
		File to = new File(toDir);
		to.mkdirs(); //create target directory if it not exists
		xcopy(from.listFiles(), to, deep, excludes);
	}

	/** Read input stream to StringBuffer (decoded from given character
	 * set table).
	 * @param in input stream.
	 * @param sb StringBuffer where to write. If this argument
	 * is <tt>null</tt> then the new StringBuffer is created.
	 * @param encoding The name of encoding table. If this argument is
	 * <tt>null</tt> the default encoding is applied.
	 * @return StringBuffer with result.
	 * @throws SException
	 * <ul>
	 * <li>SYS029 Can't read input stream</li>
	 * <li>SYS035 Unsupported character set name</li>
	 * <li>SYS036 Program exception</li>
	 * </ul>
	 */
	public static final StringBuffer readToStringBuffer(final InputStream in,
		final StringBuffer sb,
		final String encoding) throws SException {
		InputStreamReader is;
		try {
			is = new InputStreamReader(in, encoding);
		} catch (UnsupportedEncodingException ex) {
			//Unsupported character set name: &{0}
			throw new SException(SYS.SYS035, encoding);
		}
		StringBuffer mysb;
		if (sb == null) {
			mysb = new StringBuffer();
		} else {
			mysb = sb;
		}
		try {
			int len;
			char[] buf = new char[4096];
			while ((len = is.read(buf)) >= 0) {
				if (len == 0) {
					sleep1();
				} else {
					mysb.append(buf, 0 , len);
				}
			}
			try {is.close();} catch (Exception ex) {}
		} catch(IOException ex) {
			throw new SException(SYS.SYS029,ex);//Can't read input stream&{0}{;}
		}
		return mysb;
	}


	/** Read input stream to StringBuffer.
	 * @param is input reader.
	 * @param sb StringBuffer where to write. If this argument
	 * is <tt>null</tt> the new StringBuffer is created.
	 * @return StringBuffer with result.
	 * @throws SException SYS029 Can't read input stream
	 */
	public static final StringBuffer readToStringBuffer(final Reader is,
		final StringBuffer sb) throws SException {
		StringBuffer mysb;
		if (sb == null) {
			mysb = new StringBuffer();
		} else {
			mysb = sb;
		}
		try {
			int len;
			char[] buf = new char[4096];
			while ((len = is.read(buf)) >= 0) {
				if (len == 0) {
					sleep1();
				} else {
					mysb.append(buf, 0 , len);
				}
			}
			try {is.close();} catch (Exception ex) {}
		} catch(IOException ex) {
			throw new SException(SYS.SYS029,ex);//Can't read input stream&{0}{;}
		}
		return mysb;
	}

	/** Read file to StringBuffer (decoded from given character set table).
	 * @param file input file.
	 * @param sb StringBuffer where to write. If this argument
	 * is <tt>null</tt> the new StringBuffer is created.
	 * @param encoding name of encoding table. If this argument is
	 * <tt>null</tt> the decoding from default system character set table
	 * is used.
	 * @return StringBuffer with result.
	 * @throws SException
	 * <ul>
	 * <li>SYS024 File doesn't exist</li>
	 * <li>SYS028 Can't read file</li>
	 * <li>SYS035 Unsupported character set name</li>
	 * <li>SYS036 Program exception</li>
	 * </ul>
	 */
	public static final StringBuffer readToStringBuffer(final File file,
		final StringBuffer sb,
		final String encoding) throws SException {
		InputStream is;
		try {
			is = new FileInputStream(file);
		} catch (FileNotFoundException ex) {
			throw new SException(SYS.SYS024, file);// File doesn't exist: &{0}
		}
		try {
			StringBuffer mysb = readToStringBuffer(is, sb, encoding);
			try {
				is.close();
			} catch (Exception ex) {
				throw new SException(SYS.SYS036, ex); //Program exception &{0}
			}
			try {is.close();} catch (Exception ex) {}
			return mysb;
		} catch (SException ex) {
			try {is.close();} catch(Exception exx) {}
			throw ex;
		}
	}

	/** Read file to StringBuffer (decoded from default character set table).
	 * @param file The file.
	 * @param buf StringBuffer where to write. If this argument
	 * is <tt>null</tt> the new StringBuffer is created.
	 * @return StringBuffer with result.
	 * @throws SException
	 * <ul>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * <li>SYS036 Program exception: {msg}</li>
	 * </ul>
	 */
	public static final StringBuffer readToStringBuffer(final File file,
		final StringBuffer buf) throws SException {
		return readToStringBuffer(file, buf, "UTF-8");
	}

	/** Read string from input stream (decoded from given code table).
	 * @param in input stream.
	 * @param encoding name of encoding table. If this argument is
	 * <tt>null</tt> the default encoding is applied.
	 * @return The string with result.
	 * @throws SException
	 * <ul>
	 * <li>SYS029 Can't read from input stream</li>
	 * <li>SYS035 Unsupported character set name</li>
	 * <li>SYS036 Program exception: {msg}</li>
	 * </ul>
	 */
	public static final String readString(final InputStream in,
		final String encoding) throws SException {
		return readToStringBuffer(in, null, encoding).toString();
	}

	/** Read string from file (decoded from given code table).
	 * @param file The file.
	 * @param encoding The name of encoding table. If this argument is
	 * <tt>null</tt> the decoding from default system character set table
	 * is used.
	 * @return string with result.
	 * @throws SException
	 * <ul>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * <li>SYS035 Unsupported character set name</li>
	 * <li>SYS036 Program exception: {msg}</li>
	 * </ul>
	 */
	public static String readString(final File file, final String encoding)
	throws SException {
		return readToStringBuffer(file, null, encoding).toString();
	}

	/** Read string from input stream (encoded from "UTF-8" character set).
	 * @param in input stream.
	 * @return String with result.
	 * @throws SException
	 * <ul>
	 * <li>SYS029 Can't read from input stream</li>
	 * <li>SYS035 Unsupported character set name</li>
	 * <li>SYS036 Program exception: {msg}</li>
	 * </ul>
	 */
	public static final String readString(final InputStream in)
		throws SException {
		return readToStringBuffer(in, null, "UTF-8").toString();
	}

	/** Read string from file (encoded from "UTF-8" character set).
	 * @param file input file.
	 * @return String with result.
	 * @throws SException
	 * <ul>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * <li>SYS036 Program exception: {msg}</li>
	 * </ul>
	 */
	public static final String readString(final File file) throws SException {
		return readString(file, "UTF-8");
	}

	/** Read input stream to String.
	 * @param is input stream.
	 * @return String with result.
	 * @throws SException SYS029 Can't read input stream.
	 */
	public static final String readString(final Reader is) throws SException {
		StringBuilder mysb = new StringBuilder();
		try {
			int len;
			char[] buf = new char[4096];
			while ((len = is.read(buf)) >= 0) {
				if (len == 0) {
					sleep1();
				} else {
					mysb.append(buf, 0 , len);
				}
			}
			try {is.close();} catch (Exception ex) {}
		} catch(IOException ex) {
			throw new SException(SYS.SYS029,ex);//Can't read input stream&{0}{;}
		}
		return mysb.toString();
	}

	/** Read byte array from input stream.
	 * @param in input stream.
	 * @return byte array with result.
	 * @throws SException SYS029 Can't read from input stream
	 */
	public static final byte[] readBytes(final InputStream in)
		throws SException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buf = new byte[4096];
		try {
			int len;
			while ((len = in.read(buf)) >= 0) {
				if (len == 0) {
					sleep1();
				} else {
					bos.write(buf, 0, len);
				}
			}
			try {in.close();} catch (Exception ex) {}
		} catch(IOException ex) {
			throw new SException(SYS.SYS029,ex);//Can't read input stream&{0}{;}
		}
		return bos.toByteArray();
	}

	/** Read the file to byte array.
	 * @param file input file.
	 * @return byte array with result.
	 * @throws SException
	 * <ul>
	 * <li>SYS019 Unexpected end of data</li>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * </ul>
	 */
	public static final byte[] readBytes(final File file) throws SException {
		if (!file.exists()) {
			throw new SException(SYS.SYS024, file); //File doesn't exist: &{0}
		}
		long fileLen = file.length();
		if (fileLen >= Integer.MAX_VALUE) {//here should be less!!!
			throw new SException(SYS.SYS038, file); //File is too big: &{0}
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			byte[] buf = readBlock(fis, (int) fileLen);
			fis.close();
			return buf;
		} catch (Exception ex) {
			try { fis.close(); } catch(Exception exx) {}
			if (ex instanceof SException) {
				throw (SException) ex;
			}
			throw new SException(SYS.SYS028, file); //Can't read file: &{0}
		}
	}

	/** Write StringBuffer to file.
	 * @param file file where to write.
	 * @param buf StringBuffer to be written.
	 * @param encoding name of encoding table. If this argument is
	 * <tt>null</tt> then the default encoding is applied.
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS035 Unsupported character set name</li>
	 * <li>SYS036 Program exception: {msg}</li>
	 * <li>SYS038 File is too big: {file}</li>
	 * </ul>
	 */
	public static final void writeStringBuffer(final File file,
		final StringBuffer buf,
		final String encoding) throws SException {
		long estimatedSize = buf.length()*2 + 10;
		if (getUsableSpace(file) < estimatedSize) {
			throw new SException(SYS.SYS038, file); //File is too big: &{0}
		}
		OutputStreamWriter os;
		try {
			os = encoding == null ?
				new OutputStreamWriter(new FileOutputStream(file)) :
				new OutputStreamWriter(new FileOutputStream(file), encoding);
		} catch (UnsupportedEncodingException ex) {
			//Unsupported character set name: &{0}
			throw new SException(SYS.SYS035, encoding);
		} catch (IOException ex) {
			//Can't write to file: &{0}
			throw new SException(SYS.SYS023,
				file.getAbsolutePath() + " ("+ex+")");
		}
		try {
			os.write(buf.toString());
		} catch(IOException ex) {
			//Can't write to file: &{0}
			throw new SException(SYS.SYS023, file);
		}
		try {
			os.close();
		} catch(Exception ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Write StringBuffer to file in UTF-8 encoding.
	 * @param file output file.
	 * @param buf StringBuffer to be written.
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS027 Can't write to output stream: {msg}</li>
	 * <li>SYS036 Program exception: {msg}</li>
	 * <li>SYS038 File is too big: {file}</li>
	 * </ul>
	 */
	public static final void writeStringBuffer(final File file,
		final StringBuffer buf) throws SException {
		writeStringBuffer(file, buf, "UTF-8");
	}

	/** Write string to file in "UTF-8" encoding.
	 * @param file output file.
	 * @param str string to be written.
	 * @throws SException
	 * <ul>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS027 Can't write to output stream: {msg}</li>
	 * <li>SYS036 Program exception: {msg}</li>
	 * <li>SYS038 File is too big: {file}</li>
	 * </ul>
	 */
	public static final void writeString(final File file, final String str)
		throws SException {
		writeString(file, str, "UTF-8");
	}

	/** Write string output stream  in "UTF-8" encoding.
	 * @param out output stream.
	 * @param str string to be written.
	 * @throws SException SYS023 Can't write to file: {file}
	 */
	public static final void writeString(final OutputStream out,
		final String str) throws SException {
		writeString(out, str, "UTF-8");
	}

	/** Write string output stream.
	 * @param out output stream.
	 * @param str string to be written.
	 * @param encoding name of encoding table. If this argument is
	 * <tt>null</tt> then the default encoding is used.
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS027 Can't write to output stream: {msg}</li>
	 * <li>SYS035 Unsupported character set name</li>
	 * </ul>
	 */
	public static final void writeString(final OutputStream out,
		final String str,
		final String encoding) throws SException {
		OutputStreamWriter os;
		try {
			if (encoding == null) {
				os = new OutputStreamWriter(out);
			} else {
				os = new OutputStreamWriter(out, encoding);
			}
		} catch (UnsupportedEncodingException ex) {
			//Unsupported character set name: &{0}
			throw new SException(SYS.SYS035, encoding);
		}
		try {
			os.write(str);
		} catch(IOException ex) {
			//Can't write to output stream&{0}{; }
			throw new SException(SYS.SYS027, ex);
		}
		try {
			os.close();
		} catch(Exception ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Write string to file.
	 * @param file output file.
	 * @param str string to be written.
	 * @param encoding name of encoding table. If this argument is
	 * <tt>null</tt> the default encoding is used.
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS035 Unsupported character set name</li>
	 * <li>SYS038 File is too big: {file}</li>
	 * </ul>
	 */
	public static final void writeString(final File file,
		final String str,
		final String encoding) throws SException {
		long estimatedSize = str.length()*2 + 10;
		if (getUsableSpace(file) < estimatedSize) {
			throw new SException(SYS.SYS038, file); //File is too big: &{0}
		}
		OutputStreamWriter os;
		try {
			if (encoding == null) {
				os = new OutputStreamWriter(new FileOutputStream(file));
			} else {
				os = new OutputStreamWriter(
					new FileOutputStream(file),encoding);
			}
		} catch (UnsupportedEncodingException ex) {
			//Unsupported character set name: &{0}
			throw new SException(SYS.SYS035, encoding);
		} catch (IOException ex) {
			throw new SException(SYS.SYS023,//Can't write to file: &{0}
				file.getAbsolutePath() + " ("+ex+")");
		}
		try {
			os.write(str);
		} catch(IOException ex) {
			throw new SException(SYS.SYS023, file); //Can't write to file: &{0}
		}
		try {
			os.close();
		} catch(Exception ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Write string output writer.
	 * @param out output writer.
	 * @param str string to be written.
	 * <tt>null</tt> the default encoding is used.
	 * @throws SException SYS027 Can't write to output stream: {msg}
	 */
	public static final void writeString(final Writer out, final String str)
		throws SException {
		try {
			out.write(str);
		} catch(IOException ex) {
			//Can't write to output stream&{0}
			throw new SException(SYS.SYS027, ex);
		}
		try {out.close();} catch (Exception ex) {}
	}

	/** Write byte array to file item.
	 * @param file output file.
	 * @param buf byte array to be written.
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS038 File is too big: {file}</li>
	 * <li>SYS038 File is too big: {file}</li>
	 * </ul>
	 */
	public static final void writeBytes(final File file, final byte[] buf)
		throws SException {
		if (getUsableSpace(file) < buf.length + 10) {
			throw new SException(SYS.SYS038, file); //File is too big: &{0}
		}
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
		} catch (IOException ex) {
			throw new SException(SYS.SYS023,//Can't write to file: &{0}
				file.getAbsolutePath() + " ("+ex+")");
		}
		try {
			fos.write(buf);
			fos.flush();
		} catch(IOException ex) {
			throw new SException(SYS.SYS023, file); //Can't write to file: &{0}
		}
		try {
			fos.close();
		} catch (IOException ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Replace all occurrences of given key in the string by given value.
	 * @param file file which is modified.
	 * @param key sequence of characters to be replaced
	 * @param rep sequence of characters which replaces key occurrences
	 * @throws SException
	 * <ul>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * <li>SYS035 Unsupported character set name</li>
	 * <li>SYS036 Program exception: {msg}</li>
	 * <li>SYS038 File is too big: {file}</li>
	 * </ul>
	 */
	public static final void modifyFile(final File file,
		final String key,
		final String rep) throws SException {
		StringBuffer sb = readToStringBuffer(file, null, null);
		SUtils.modifyStringBuffer(sb, key, rep);
		writeStringBuffer(file, sb);
	}

	/** Very dangerous: if the file is directory it deletes all subdirectories!
	 * @param files array of files to be deleted.
	 * @param subdir if <tt>true</tt> then also subdirectories are deleted.
	 */
	public static void deleteAll(final File[] files,
		final boolean subdir) {
		for (File x: files) {
			if (x.isDirectory()) {
				if (subdir) {
					deleteAll(x.listFiles(), true);
				}
			}
			x.delete();
		}
	}

	/** Very dangerous: if the file is directory it deletes all subdirectories!
	 * @param file file(s) to be deleted.
	 * @param subdir if <tt>true</tt> then also subdirectories are deleted.
	 * @throws SException SYS025 Directory doesn't exist or isn't accessible.
	 */
	public static final void deleteAll(final File file, final boolean subdir)
		throws SException {
		if (!file.exists() || !file.isDirectory()) {
			//Directory doesn't exist or isn't accessible: &{0}
			throw new SException(SYS.SYS025, file);
		}
		deleteAll(new File[]{file}, subdir);
	}

	/** Very dangerous: if the file is directory it deletes all subdirectories!
	 * @param fname file(s) to be deleted.
	 * @param subdir if <tt>true</tt> then also subdirectories are deleted.
	 * @throws SException SYS025 Directory doesn't exist or isn't accessible.
	 */
	public static final void deleteAll(final String fname, final boolean subdir)
		throws SException {
		deleteAll(new File(fname), subdir);
	}


	/** Secure copy InputStream to the file. First the input is saved to the
	 * temporary file and after the copy was finished the file is renamed to the
	 * file given by parameter. If the file of same name already exists then
	 * it is first renamed to the file with added extension ".bak".
	 * @param is input stream.
	 * @param pathname name of file to which the copy will be made.
	 * @throws SException
	 * <ul>
	 * <li>SYS021 Can't delete file: {file}</li>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS026 Can't create file: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * <li>SYS031 Can't rename file {f1} to {f2}</li>
	 * <li>SYS036 Program exception: {msg}</li>
	 * </ul>
	 */
	public final static void secureCopy(final InputStream is,
		final String pathname) throws SException {
		secureCopy(is, new File(pathname));
	}

	/** Secure copy InputStream to the file. First the input is saved to the
	 * temporary file and after the copy was finished the file is renamed to the
	 * file given by parameter. If the file of same name already exists then
	 * it is first renamed to the file with added extension ".bak".
	 * @param is input stream.
	 * @param file file to which the copy will be made.
	 * @throws SException
	 * <ul>
	 * <li>SYS021 Can't delete file: {file}</li>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS026 Can't create file: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * <li>SYS031 Can't rename file {f1} to {f2}</li>
	 * <li>SYS036 Program exception: {msg}</li>
	 * </ul>
	 */
	public static final void secureCopy(final InputStream is, final File file)
		throws SException {
		File temp = new File(file.getPath() + TEMPORARY_FILE_EXTENSION);
		if (temp.exists()) {
			if (!temp.delete()) {
				throw new SException(SYS.SYS021, temp);//Can't delete file: &{0}
			}
		}
		copyToFile(is, temp, false);
		if (file.exists()) {
			File bak = new File(file.getPath() + BACKUP_FILE_EXTENSION);
			if (bak.exists()) {
				if (!bak.delete()) {
					//Can't delete file: &{0}
					throw new SException(SYS.SYS021, file);
				}
			}
			if (!file.renameTo(bak)) {
				//Can't rename file &{0} to &{1}
				throw new SException(SYS.SYS031, file, bak);
			}
		}
		if (!temp.renameTo(file)) {
			//Can't rename file &{0} to &{1}
			throw new SException(SYS.SYS031, temp, file);
		}
	}

	/** Copy file to other file. First the input is saved to the temporary
	 * file and after the copy was finished the file is renamed to the file
	 * given by parameter. If the file of same name already exists then
	 * it is first renamed to the file with added extension ".bak".
	 * @param in input file.
	 * @param out output file.
	 * @throws SException
	 * <ul>
	 * <li>SYS021 Can't delete file: {file}</li>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS026 Can't create file: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * <li>SYS031 Can't rename file {f1} to {f2}</li>
	 * <li>SYS036 Program exception: {msg}</li>
	 * <li>SYS038 File is too big: {file}</li>
	 * </ul>
	 */
	public static final void secureCopy(final File in, final File out)
		throws SException {
		if (getUsableSpace(out) < in.length() + 10) {
			throw new SException(SYS.SYS038, out); //File is too big: &{0}
		}
		if (!in.exists()) {
			throw new SException(SYS.SYS024, in); //File doesn't exist: &{0}
		}
		if (!in.canRead()) {
			throw new SException(SYS.SYS028, in); //Can't read file: &{0}
		}
		InputStream is = null;
		try {
			is = new FileInputStream(in);
		} catch (FileNotFoundException ex) {
			throw new SException(SYS.SYS024, in); //File doesn't exist: &{0}
		}
		secureCopy(is,out);
		try {
			is.close();
		} catch (IOException ex) {
			throw new SException(SYS.SYS036, ex); //Program exception &{0}
		}
	}

	/** Get temporary directory.
	 * @return The path to the temporary directory.
	 */
	public static final String getTempDir() {
		String s = System.getProperties().getProperty("java.io.tmpdir");
		if (s != null && !s.endsWith(File.separator)) {
			return s + File.separator;
		}
		return s;
	}

	/** Get class path.
	 * @return The class path.
	 */
	public static final String[] getClassPath() {
		String s = System.getProperties().getProperty("java.class.path");
		if (s == null) {
			return new String[0];
		}
		StringTokenizer st = new StringTokenizer(s, File.pathSeparator);
		int length = st.countTokens();
		String[] result = new String[length];
		for (int i = 0; i < length; i++) {
			result[i] = st.nextToken().trim();
		}
		return result;
	}

	/** Resolve SYSTEM id.
	 * @param sid system id.
	 * @param actPath actual file path.
	 * @return URL created from id.
	 * @throws SException if an error occurs.
	 */
	public static final URL resolveSystemID(final String sid,
		final String actPath) throws SException {
		if (sid.indexOf(":/") > 2 && sid.indexOf(":/") < 12) { //URL
			try {
				return getExtendedURL(sid);
			} catch (Exception ex) {
				//URL &{0} error: &{1}{; }
				throw new SException(SYS.SYS076, sid, ex);
			}
		} else if (actPath != null
			&& actPath.indexOf(":/") > 2 && actPath.indexOf(":/") < 12) {
			 // Actpath is URL and sid is not URL
			try {
				int ndx = actPath.lastIndexOf('/');
				String s = ndx<0 ? actPath + '/' : actPath.substring(0, ndx+1);
				return getExtendedURL(s + sid);
			} catch (Exception ex) {
				//URL &{0} error: &{1}{; }
				throw new SException(SYS.SYS076, sid, ex);
			}
		}
		// sid is file
		File f = sid.indexOf(":/") > 0 || sid.startsWith("/")
			? new java.io.File(sid) : actPath == null
			? new java.io.File(sid) : new java.io.File(actPath, sid);
		if (f.exists() && f.canRead()) {
			try {
				return f.toURI().toURL();
			} catch (Exception ex) {
				//URL &{0} error: &{1}{; }
				throw new SException(SYS.SYS076, f.toURI(), ex);
			}
		} else {
			throw new SException(SYS.SYS028, f); //Can't read file: &{0}
		}
	}

	/** Get array of existing files represented by given argument array. The
	 * argument array is an array of strings where each one can either represent
	 * one concrete file or it can represent a set of files with wildcards
	 * '*' and/or '?'. Comparing is case sensitive.
	 * @param wildNames file names (may be array or list of arguments).
	 * @return array of existing files according to argument.
	 */
	public static final File[] getFileGroup(final String... wildNames) {
		return getFileGroup(wildNames, false);
	}

	/** Get actual path as string.
	 * @return string with actual path.
	 * @throws SRuntimeException SYS051 Actual path isn't accessible.
	 */
	public static final String getActualPath() {
		try {
			File f = new File(".");
			if (f.isDirectory()) {
				String s = f.getCanonicalPath();
				if (!s.endsWith(File.separator)) {
					return s + File.separator;
				} else {
					return s;
				}
			}
		} catch (Exception ex) {}
		throw new SRuntimeException(SYS.SYS051); //Actual path isn't accessable
	}

	/** Get array of existing files represented by given argument. The argument
	 * can either represent one concrete file or it can represent a set of files
	 * with wildcards '*' and/or '?'.
	 * @param wildName file name (wildcards are accepted) .
	 * @param caseInsensitive if true then name comparing is case insensitive.
	 * @return array of existing files according to argument.
	 */
	public static final File[] getFileGroup(final String wildName,
		final boolean caseInsensitive) {
		if (wildName.indexOf('*') < 0 && wildName.indexOf('?') < 0) {
			File f = new File(wildName);
			return f.exists() ? new File[]{f} : new File[0];
		}
		String wn = wildName.replace('\\','/');
		File dir;
		int i;
		if ((i = wn.lastIndexOf('/')) >= 0) {
			dir = new File(wn.substring(0,i));
			wn = wn.substring(i + 1);
		} else {
			dir = new File(getActualPath());
		}
		return dir.listFiles(new NameWildCardFilter(wn, caseInsensitive));
	}

	/** Get array of existing files represented by given argument array. The
	 * argument array is an array of strings where each one can either represent
	 * one concrete file or it can represent a set of files with wildcards
	 * '*' and/or '?'.
	 * @param wildNames array of file names.
	 * @param caseInsensitive if true then name comparing is case insensitive.
	 * @return array of existing files according to argument.
	 */
	public static final File[] getFileGroup(final String[] wildNames,
		final boolean caseInsensitive) {
		ArrayList<File> arr = new ArrayList<File>();
		for (String x: wildNames) {
			File[] files = getFileGroup(x, caseInsensitive);
			for (int j = 0; j < files.length; j++) {
				if (!arr.contains(files[j])) {
					arr.add(files[j]);
				}
			}
		}
		File[] result = new File[arr.size()];
		arr.toArray(result);
		return result;
	}

	/** Get group of URLs according to with wildcard from the directory.
	 * @param urls list where to store created URLs.
	 * @param dir directory where to search files.
	 * @param wc wildcard.
	 * @throws IOException if an error occurs.
	 */
	private static void getSourceFileGroup(final List<String> urls,
		final String dir,
		final String wc) throws Exception {
		File f = new File(dir);
		if (f.isDirectory()) {
			File[] ff = getFileGroup(f.getAbsolutePath() + "/" + wc, true);
			for (File x: ff) {
				if (x.isFile() && x.exists()) {
					urls.add(x.getCanonicalFile().toURI().toURL()
						.toExternalForm());
				}
			}
		} else {
			throw new IOException("URL error");
		}
	}

	/** Get group source URLs from source name which may contain the file name
	 * containing wildcard.
	 * @param src name which may contain the file name containing wildcard.
	 * @return the array of URL names.
	 * @throws Exception if an error occurs.
	 */
	public static final String[] getSourceGroup(final String src)
		throws Exception {
		List<String> urls = new ArrayList<String>();
		if (src.startsWith("classpath://")) {
			String t = src.substring(12);
			int ndx = t.lastIndexOf('.');
			t = t.substring(0,ndx).replace('.', '/') + t.substring(ndx);
			ndx = t.lastIndexOf('/');
			String wc = t.substring(ndx+1); // wildcard of file name
			String dir = t.substring(0,ndx).replace('.', '/');
			if (wc.indexOf('?')>=0 || wc.indexOf('*')>=0) {
				ndx = dir.lastIndexOf('.');
				Enumeration<URL> eu = ClassLoader.getSystemResources(dir);
				while (eu.hasMoreElements()) {
					URL u = eu.nextElement();
					if ("file".equals(u.getProtocol())) {
						getSourceFileGroup(urls, u.getFile(), wc);
					} else if ("jar".equals(u.getProtocol())) {
						String s = u.toExternalForm();
						ndx = s.indexOf('!');
						if (ndx >= 0) {
							URL ux = new URL(s.substring(4, ndx));
							JarFile jf = new JarFile(ux.getFile());
							Enumeration<JarEntry> je = jf.entries();
							while (je.hasMoreElements()) {
								s = je.nextElement().getName();
								if (s.startsWith(dir + "/")
									&& s.length() > dir.length() + 1) {
									s = s.substring(dir.length() + 1);
									if (NameWildCardFilter.chkWildcard(wc, s)) {
										urls.add("classpath://"
											+ dir.replace('/', '.') + "."+s);
									}
								}
							}
						}
					} else {
						throw new RuntimeException(
							"Unknown protocol: " + u.getProtocol());
					}
				}
			} else {
				urls.add(getExtendedURL(
					src).toURI().toURL().toExternalForm());
			}
		} else if (src.startsWith("file:/")) {
			int ndx = src.lastIndexOf('/');
			String wc = src.substring(ndx+1); // wildcard of file name
			if (wc.indexOf('?')>=0 || wc.indexOf('*')>=0) {
				getSourceFileGroup(urls,
					new URL(src.substring(0, ndx)).getFile(), wc);
			} else {
				urls.add(src);
			}
		} else if (src.startsWith("ftp:") || src.startsWith("ftps:")
			|| src.startsWith("http:") || src.startsWith("https:")) {
			urls.add(src);
		} else { // try just pathname
			File[] files = getFileGroup(src);
			for (File x: files) {
				urls.add(x.getCanonicalFile().toURI().toURL().toExternalForm());
			}
		}
		return urls.toArray(new String[urls.size()]);
	}

	/** Get URL from string (accept also protocol "classpath://").
	 * @param source string with URL source (MAY BE ALSO protocol "classpath:").
	 * @return URL created from the source string.
	 * @throws MalformedURLException IF AN ERROR OCCURS.
	 */
	public static final URL getExtendedURL(final String source)
		throws MalformedURLException{
		String s;
		try {
			s = URLDecoder.decode(source,
				System.getProperties().getProperty("file.encoding")).trim();
		} catch (UnsupportedEncodingException ex) {
			s = source.trim();
		}
		if (s.startsWith("classpath://")) {
			try {
				String t = s.substring(12);
				int ndx = t.lastIndexOf('.');
				t = t.substring(0,ndx).replace('.', '/') + t.substring(ndx);
				URL url = ClassLoader.getSystemResource(t);
				if (url != null) {
					return url;
				}
			} catch (Exception ex) {} // try regular URL constructor
		}
		URL u = new URL(s);
		if ("file".equals(u.getProtocol())) {
			try {
				return new File(u.getFile()).getCanonicalFile().toURI().toURL();
			} catch (Exception ex) {}
		}
		return u;
	}

	/** Update directories. If a file from the directory "fromDir" not exists
	 * in the directory "toDir" or if it is different it is replaced by contents
	 * of the file from "fromDir". If the parameter extension is not null or
	 * an empty string only files with given extension are updated.
	 * If "subdirs" argument is true also subdirectories are updated.
	 * If deleteOther argument is true then all files in "toDir" which not
	 * exist in "fromDir" are deleted.
	 * @param fromDir the directory path from which the files are updated
	 * in "toDir".
	 * @param toDir the directory path where files are updated.
	 * @param extension file extension filter or null.
	 * @param subdirs if true also subdirectories are updated.
	 * @param deleteOther if true then all files in "toDir" which not
	 * exist in "fromDir" are deleted.
	 * @return string with reports about changes. If no changes were made
	 * then returns an empty string.
	 * @throws Exception if an error occurs.
	 */
	public static final String updateDirectories(final String fromDir,
		final String toDir,
		final String extension,
		final boolean subdirs,
		final boolean deleteOther) throws Exception {
		return updateDirectories(new File (fromDir),
			new File (toDir), extension, subdirs, deleteOther);
	}

	/** Update directories. If a file from the directory "fromDir" not exists
	 * in the directory "toDir" or if it is different it is replaced by contents
	 * of the file from "fromDir". If the parameter extension is not null or
	 * an empty string only files with given extension are updated.
	 * If "subdirs" argument is true also subdirectories are updated.
	 * If deleteOther argument is true then all files in "toDir" which not
	 * exist in "fromDir" are deleted.
	 * @param fromDir the directory from which the files are updated in "toDir".
	 * @param toDir  the directory where files are updated.
	 * @param extension file extension filter or null.
	 * @param subdirs if true also subdirectories are updated.
	 * @param deleteOther if true then all files in "toDir" which not
	 * exist in "fromDir" are deleted.
	 * @return string with reports about changes. If no changes were made
	 * then returns an empty string.
	 * @throws Exception if an error occurs.
	 */
	public static final String updateDirectories(final File fromDir,
		final File toDir,
		final String extension,
		final boolean subdirs,
		final boolean deleteOther) throws Exception {
		StringBuilder sb = new StringBuilder();
		updateDirectories(fromDir, toDir, extension, subdirs, deleteOther, sb);
		return sb.toString();
	}

	/** Append text of message to the StringBuilder.
	 * @param sb the StringBuilder.
	 * @param msg message to be added.
	 */
	private static void addMessage(final StringBuilder sb, final String msg) {
		if (sb.length() > 0) {
			sb.append('\n');
		}
		sb.append(msg);
	}

	/** Check file name with extension. If the file extension is not correct
	 * return null;
	 * @param f the file to be checked.
	 * @param extension file extension or null.
	 * @return file name or null if file extension not fits to the extension.
	 */
	private static String chkExtension(final File f, final String extension) {
		String name = f.getName();
		if (f.isFile()) {
			if (extension == null || extension.isEmpty()
				|| name.endsWith('.' + extension)) {
				return name;
			}
		}
		return null;
	}

	private static void updateDirectories(final File fromDir,
		final File toDir,
		final String extension,
		final boolean subdirs,
		final boolean deleteOther,
		StringBuilder sb) throws Exception {
		checkDir(fromDir, false);
		if (!toDir.exists()) {
			checkDir(toDir, true);
			addMessage(sb, "Created dir: " + toDir.getCanonicalPath());
		} else {
			checkDir(toDir, true);
		}
		File[] toFiles = toDir.listFiles();
		File[] fromFiles = fromDir.listFiles();
		if (deleteOther) {
			for (File f: toFiles) {
				// delete files in toDir which not exist in fromDir.
				if (f.isFile()) {
					String name = chkExtension(f, extension);
					if (name != null) {
						File g = new File(fromDir, name);
						if (!g.exists()) {
							addMessage(sb, "Deleted: " + f.getCanonicalPath());
							deleteFile(f);
						}
					}
				}
			}
		}
		// replace or add files from fromDir to toDir
		for (File f: fromFiles) {
			String name = chkExtension(f, extension);
			if (name != null) {
				File g = new File(toDir, name);
				if (g.exists()) {
					if (g.isDirectory()) {
						if (deleteOther) {
							deleteAll(g, true);
							addMessage(sb,"Deleted dir: "+g.getCanonicalPath());
							copyToFile(f, g);
							addMessage(sb, "Added: " + g.getCanonicalPath());
						}
					} else {
						if (compareFile(f, g) != -1L) {
							copyToFile(f, g);
							addMessage(sb, "Replaced: " + g.getCanonicalPath());
						}
					}
				} else {
					copyToFile(f, g);
					addMessage(sb, "Added: " + g.getCanonicalPath());
				}
			}
		}
		if (subdirs) {
			toFiles = toDir.listFiles();
			fromFiles = fromDir.listFiles();
			if (deleteOther) {
				// delete directories in toDir
				for (File f: toFiles) {
					if (f.isDirectory()) {
						String name = f.getName();
						File g = new File(fromDir, name);
						if (!g.exists() || !g.isDirectory()) {
							deleteAll(f, true);
							addMessage(sb,
								"Deleted dir: "	+ f.getCanonicalPath());
						}
					}
				}
			}
			for (File f: fromFiles) {
				if (f.isDirectory()) {
					String name = f.getName();
					File g = new File(toDir, name);
					if (g.exists() && !g.isDirectory()) {
						if (deleteOther) {
							deleteFile(f);
						} else {
							//Can't create directory: &{0}
							throw new SException(SYS.SYS020,
								g.getCanonicalPath()
									+ " exists and it is not directory!");
						}
					} else {
						if (!g.exists()) {
							checkDir(g, true);
							addMessage(sb,
								"Created dir: " + g.getCanonicalPath());
						}
						updateDirectories(
							f, g, extension, subdirs, deleteOther, sb);
					}
				}
			}
		}
	}

	/** Store files given by list to zip archive file. Entries of list are
	 * separated by ";". If the entry is directory there are archived all
	 * files from the subtree from this directory. The files with extensions
	 * given by argument <tt>ignore</tt> are ignored. Wild characters in
	 * file names (i.e. "*" or '?') are accepted.
	 * @param fileList list of files separated by ";".
	 * @param ignoreList list of extensions to be ignored(separated by ";").
	 * @param file archive file.
	 * @return sum of length of all archived files.
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * <li>SYS073 Zip file list is empty</li>
	 * </ul>
	 */
	public static final long filesToZip(final String fileList,
		final String ignoreList,
		final File file) throws SException {
		String[] fnames;
		if (fileList.indexOf(";") <= 0) {
			fnames = new String[]{fileList};
		} else {
			StringTokenizer st = new StringTokenizer(fileList,";");
			fnames = new String[st.countTokens()];
			int i=0;
			while (st.hasMoreTokens()) {
				fnames[i++] = st.nextToken();
			}
		}
		ArrayList<String> skipExtensions = new ArrayList<String>();
		if (ignoreList != null) {
			StringTokenizer st = new StringTokenizer(ignoreList,";");
			while (st.hasMoreTokens()) {
				String s = st.nextToken();
				if (s.length() > 0) {
					if (s.charAt(0) != '.') {
						s = '.' + s;
					}
					if (skipExtensions.indexOf(s) < 0) {
						skipExtensions.add(s);
					}
				}
			}
		}
		ArrayList<File> ar = new ArrayList<File>();
		for (String s: fnames) {
			File[] list = getFileGroup(s);
			for (File x: list) {
				if (skipExtensions.size() > 0) {
					String fname = x.getName();
					int k = fname.lastIndexOf('.');
					if (k > 0) {
						if (skipExtensions.indexOf(fname.substring(k)) >= 0) {
							continue;
						}
					}
				}
				if (ar.indexOf(x) < 0) {
					ar.add(x);
				}
			}
		}
		File[] files = new File[ar.size()];
		ar.toArray(files);
		String[] extensions = new String[skipExtensions.size()];
		skipExtensions.toArray(extensions);
		return filesToZip(files, extensions, file);
	}

	/** Store files given by list to zip archive file. Entries of list are
	 * separated by ";". If the entry is directory there are archived all
	 * files from the subtree from this directory.
	 * @param list list of files.
	 * @param skipExtensions array of extensions to be ignored.
	 * @param file archive file.
	 * @return sum of length of all archived files.
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * <li>SYS073 Zip file list is empty</li>
	 * </ul>
	 */
	public static final long filesToZip(final File[] list,
		final String[] skipExtensions,
		final File file) throws SException {
		try {
			String zFileName = file.getCanonicalPath();
			OutputStream os = new FileOutputStream(file);
			long result = filesToZip(list, skipExtensions, os, zFileName);
			try {
				os.close();
			} catch (IOException ex) {
				//Can't write to file: &{0}
				throw new SException(SYS.SYS023, zFileName);
			}
			return result;
		} catch (IOException ex) {
			//Can't write to file: &{0}
			throw new SException(SYS.SYS023, file.getAbsolutePath());
		}
	}

	/** Store files given by list to zip archive file. Entries of list are
	 * separated by ";". If the entry is directory there are archived all
	 * files from the subtree from this directory.
	 * @param list The list of files.
	 * @param skipExtensions array of extensions to be ignored.
	 * @param out archive file stream.
	 * @return sum of length of all archived files.
	 * @param zFileName name of archive (used just for error reports).
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * <li>SYS073 Zip file list is empty</li>
	 * </ul>
	 */
	public static final long filesToZip(final File[] list,
		final String[] skipExtensions,
		final OutputStream out,
		final String zFileName) throws SException {
		if (list.length == 0) {
			throw new SException(SYS.SYS073); //Zip file list is empty
		}
		ZipOutputStream zout = new ZipOutputStream(out);
		long flen = 0;
		for (File f: list) {
			if (f == null) {
				continue;
			}
			if (!f.exists()) {
				throw new SException(SYS.SYS024, f); //File doesn't exist: &{0}
			}
			File[] files;
			if (f.isDirectory()) {
				files = f.listFiles();
			} else {
				files = new File[]{f};
			}
			flen += filesToZip(zout, zFileName, "", files, skipExtensions);
		}
		try {
			zout.finish();
			zout.flush();
			return flen;
		} catch (IOException ex) {
			//Can't write to file: &{0}
			throw new SException(SYS.SYS023, zFileName);
		}

	}

	/** Add files to archive.
	 * @param zout archive output stream.
	 * @param zFileName name of archive (used just for error reports).
	 * @param relPath Relative path for storing of files.
	 * @param files array of files to be archived.
	 * @param skipExtensions array of extensions to be ignored.
	 * @return sum of length of all archived files.
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * </ul>
	 */
	private static long filesToZip(final ZipOutputStream zout,
		final String zFileName,
		final String relPath,
		final File[] files,
		final String[] skipExtensions) throws SException {
		long flen = 0;
		byte[] buf = new byte[4096];
		for (File f: files) {
			if (f.isDirectory()) {
				flen += filesToZip(
					zout,
					zFileName,
					relPath + f.getName() + File.separatorChar,
					f.listFiles(),
					skipExtensions);
			} else {
				String fname = f.getName();
				if (skipExtensions != null) {
					int k = fname.lastIndexOf('.');
					if (k > 0) {
						for (int x = 0; x < skipExtensions.length; x++) {
							if (fname.substring(k).equals(skipExtensions[x])) {
								return 0L;
							}
						}
					}
				}
				ZipEntry z;
				z = new ZipEntry(relPath + fname);
				try {
					zout.putNextEntry(z);
				} catch (IOException ex) {
					//Can't write to file: &{0},
					throw new SException(SYS.SYS023, zFileName);
				}
				FileInputStream src;
				try {
					src = new FileInputStream(f);
				} catch (FileNotFoundException ex) {
					//File doesn't exist: &{0}
					throw new SException(SYS.SYS024, f);
				}
				int len;
				try {
					len = src.read(buf);
				} catch (IOException ex) {
					throw new SException(SYS.SYS028, f);//Can't read file: &{0}
				}
				while (len > 0) {
					try {
						zout.write(buf,0,len);
					} catch (IOException ex) {
						//Can't write to file: &{0}
						throw new SException(SYS.SYS023, zFileName);
					}
					try {
						len = src.read(buf);
					} catch (IOException ex) {
						//Can't read file: &{0}
						throw new SException(SYS.SYS028, f);
					}
				}
				try {
					src.close();
				} catch (Exception ex) {
				}
				flen += f.length();
			}
		}
		return flen;
	}

	/** Extract files from archive and store them to given directory.
	 * @param dir directory for files to be stored.
	 * @param archive archive file.
	 * @param backupExtension If null, files are replaced, otherwise the
	 * original file is renamed to given extension.
	 * @return sum of length of all extracted files.
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS026 Can't create file: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * <li>SYS032 File is not directory: {file}</li>
	 * <li>SYS038 File is too big: {file}</li>
	 * </ul>
	 */
	public static final long filesFromZip(final File dir,
		final File archive,
		final String backupExtension) throws SException {
		try {
			InputStream in = new FileInputStream(archive);
			long result = filesFromZip(dir, in, backupExtension);
			try {
				in.close();
			} catch (IOException ex) {
			}
			return result;
		} catch (FileNotFoundException ex) {
			throw new SException(SYS.SYS024,archive); //File doesn't exist: &{0}
		}
	}

	/** Extract files from archive and store them to given directory.
	 * @param dir directory for files to be stored.
	 * @param archive archive file.
	 * @param backupExtension If null, files are replaced, otherwise the
	 * original file is renamed to given extension.
	 * @return sum of length of all extracted files.
	 * @throws SException
	 * <ul>
	 * <li>SYS023 Can't write to file: {file}</li>
	 * <li>SYS024 File doesn't exist: {file}</li>
	 * <li>SYS026 Can't create file: {file}</li>
	 * <li>SYS028 Can't read file: {file}</li>
	 * <li>SYS032 File is not directory: {file}</li>
	 * <li>SYS038 File is too big: {file}</li>
	 * </ul>
	 */
	public static final long filesFromZip(final File dir,
		final InputStream archive,
		final String backupExtension) throws SException {
		long flen = 0;
		ZipInputStream zin = new ZipInputStream(archive);
		if (!dir.exists() || (!dir.isDirectory())) {
			throw new SException(SYS.SYS032, dir); //File is not directory: &{0}
		}
		String path;
		ZipEntry z;
		try {
			path = dir.getCanonicalPath() + File.separatorChar;
			z = zin.getNextEntry();
		} catch (IOException ex) {
			throw new SException(SYS.SYS044);//Can't read entry from 'zip' file
		}
		byte[] buf = new byte[4096];
		while (z != null) {
			String fname = z.toString();
			int pathIndex = fname.lastIndexOf(File.separatorChar);
			if (pathIndex > 0) {
				new File(path + fname.substring(0,pathIndex)).mkdirs();
			}
			File f = new File(path + fname);
			if (f.exists() &&
				backupExtension != null && backupExtension.length() > 0) {
				File f1 = new File(path + fname + backupExtension);
				if (f1.exists()) {
					f1.delete();
				}
				f.renameTo(f1);
			}
			if (z.getSize() + 10 > getUsableSpace(f)) {
				throw new SException(SYS.SYS038, f); //File is too big: &{0}
			}
			FileOutputStream fos;
			try {
				fos = new FileOutputStream(f);
			} catch (IOException ex) {
				//Can't create file: &{0}
				throw new SException(SYS.SYS026, path + fname);
			}
			int len;
			try {
				len = zin.read(buf);
			} catch (IOException ex) {
				//Can't read file from 'zip' file
				throw new SException(SYS.SYS043);
			}
			while (len > 0) {
				try {
					fos.write(buf,0,len);
				} catch (Exception ex) {
					//Can't write to file: &{0}
					throw new SException(SYS.SYS023, f);
				}
				try {
					len = zin.read(buf);
				} catch (IOException ex) {
					//Can't read file from 'zip' file
					throw new SException(SYS.SYS043);
				}
			}
			try {
				fos.close();
			} catch (Exception ex) {
				throw new SException(SYS.SYS023, f); //Can't write to file: &{0}
			}
			flen += f.length();
			try {
				z = zin.getNextEntry();
			} catch (IOException ex) {
				//Can't read entry from 'zip' file
				throw new SException(SYS.SYS044);
			}
		}
		try {
			zin.close();
		} catch (IOException ex) {}
		return flen;
	}

}