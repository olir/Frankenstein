/*
 * This file is licensed under CC0 1.0 Universell (the "License");
 *
 * Public Domain Dedication
 *
 *     https://creativecommons.org/publicdomain/zero/1.0/deed.de
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.serviceflow.frankenstein;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Container for property files containing sections. A section header is a line,
 * with the section name in brackets, e.g. [section1]. The container allows to
 * access main (top level) and section properties individually.
 * 
 * Supports FFmpeg meta data files; comments (lines starting with ';') are
 * stripped off. Supports Java Property files; comments (lines starting with '#'
 * or '!') in sections are stripped off, at top level they collected.
 * 
 * Usage:
 * <pre>
 *	   new SectionedProperties().load(metadataFile).toString();
 * </pre>
 *
 * @author Oliver Rode (https://github.com/olir/)
 */
public class SectionedProperties {
	/**
	 * mapping section name to sections's properties.
	 */
	private final Map<String, Properties> sectionMap;

	/**
	 * top level properties
	 */
	private final Properties main = new Properties();

	/**
	 * top level comments
	 */
	private StringBuffer comment = null;

	/**
	 * ffmpeg meta data header
	 */
	private String ffmetatadata = null;

	/**
	 * Creates an empty container.
	 */
	public SectionedProperties() {
		sectionMap = new HashMap<String, Properties>();
	}

	/**
	 * Returns main (top level) properties that appear before any section.
	 *
	 * @return the Properties object for the non-sectioned properties.
	 */
	Properties main() {
		return main;
	}

	/**
	 * Returns properties of the section.
	 *
	 * @param section
	 *            name of section.
	 * @return the Properties object for the section.
	 */
	Properties section(String section) {
		return sectionMap.get(section);
	}

	/**
	 * Returns sections of the properties.
	 *
	 * @return Set of section names.
	 */
	Set<String> sections() {
		return sectionMap.keySet();
	}

	/**
	 * Clears the sectioned properties from the container so it gets empty.
	 * 
	 * @return this SectionedProperties instance (for chaining calls).
	 */
	SectionedProperties clear() {
		ffmetatadata = null;
		main.clear();
		sectionMap.clear();
		return this;
	}

	/**
	 * Loads sectioned properties from the given file.
	 *
	 * @param f
	 *            file to read from.
	 * @return this SectionedProperties instance (for chaining calls).
	 */
	SectionedProperties load(File f) {
		try {
			InputStream is = new FileInputStream(f);
			InputStreamReader isr = new InputStreamReader(is, "iso-8859-1");
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			String section = null;

			StringBuffer linebuffer = new StringBuffer();
			try {
				while ((line = br.readLine()) != null) {
					if (line.startsWith(";FFMETADATA")) {
						ffmetatadata = line;
					} else if (ffmetatadata == null && (line.startsWith("#") || line.startsWith("!"))) {
						// comment line
						if (section == null) {
							if (comment == null)
								comment = new StringBuffer();
							comment.append(line.substring(1));
							comment.append(System.getProperty("line.separator"));
						}
					} else if (ffmetatadata != null && line.startsWith(";")) {
						// strip of any ffmpeg comments
					} else if (line.startsWith("[")) {
						// section header
						String name = line.trim();
						if (name.endsWith("]")) {
							section = name.substring(1, name.length() - 1);
							continue;
						}
					} else {
						// property line
						if (line.endsWith("\\")) {
							linebuffer.append(line.substring(0, line.length() - 1));
						} else {
							linebuffer.append(line.trim());
							String entryLine = linebuffer.toString();
							linebuffer.setLength(0);
							int n = entryLine.indexOf('=');
							String key, value;
							if (n >= 0) {
								key = entryLine.substring(0, n);
								value = entryLine.substring(n + 1);
							} else {
								key = entryLine;
								value = "";
							}
							if (ffmetatadata == null) {
								key = key.trim();
								value = value.trim();
							}
							if (section == null) {
								main.put(key, value);
							} else {
								Properties sprops = sectionMap.get(section);
								if (sprops == null) {
									sprops = new Properties();
									sectionMap.put(section, sprops);
								}
								sprops.put(key, value);
							}
						}
					}
				}
			} finally {
				br.close();
				isr.close();
				is.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	/**
	 * Saves sectioned properties to the given file.
	 *
	 * @param f
	 *            file to write to.
	 * @return this SectionedProperties instance (for chaining calls).
	 */
	SectionedProperties save(File f) {
		try {
			OutputStream os = new FileOutputStream(f);
			OutputStreamWriter osw = new OutputStreamWriter(os, "iso-8859-1");
			BufferedWriter bw = new BufferedWriter(osw);
			try {
				dump(bw);
			} finally {
				bw.close();
				osw.close();
				os.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

	private void dump(BufferedWriter bw) throws IOException {
		if (ffmetatadata != null) {
			bw.write(ffmetatadata);
			bw.newLine();
		}
		main.store(bw, getComments());
		for (String section : sections()) {
			bw.newLine();
			bw.write("[");
			bw.write(section);
			bw.write("]");
			bw.newLine();
			section(section).store(bw, null);
		}
	}

	/**
	 * Getter for the description of the property list.
	 * 
	 * @return a description of the property list, or null if no comment is
	 *         available
	 */
	public String getComments() {
		if (comment == null)
			return null;
		else {
			return comment.toString();
		}
	}

	/**
	 * Setter for the description of the property list.
	 * 
	 * @param comment
	 *            a description of the property list, or null if no comment is
	 *            desired
	 */
	public void setComments(String comment) {
		if (comment == null)
			this.comment = null;
		else {
			if (this.comment == null)
				this.comment = new StringBuffer();
			this.comment.setLength(0);
			this.comment.append(comment);
		}
	}

	public String toString() {
		try {
			StringWriter sw = new StringWriter();
			BufferedWriter bw = new BufferedWriter(sw);
			dump(bw);
			bw.close();
			return sw.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
