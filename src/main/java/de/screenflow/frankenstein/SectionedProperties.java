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
package de.screenflow.frankenstein;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Container for property files containing sections. A section header is a line,
 * with the section name in brackets, e.g. [section1]. The container allows to
 * access main (top level) and section properties individually.
 *
 * @author Oliver Rode (https://github.com/olir/)
 */
public class SectionedProperties {
	/**
	 * mapping section name to sections's properties.
	 */
	private Map<String, Properties> sectionMap;

	/**
	 * top level properties
	 */
	private Properties main;

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
	 */
	void clear() {
		main.clear();
		sectionMap.clear();
	}

	/**
	 * Loads sectioned properties from the given file.
	 *
	 * @param f
	 *            file to read from.
	 */
	void load(File f) throws IOException {
		InputStream is = new FileInputStream(f);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		String section = null;
		StringBuffer comment = null;
		StringBuffer linebuffer = new StringBuffer();
		try {
			while ((line = br.readLine()) != null) {
				if (line.startsWith("#") || line.startsWith("!")) {
					if (section == null) {
						if (comment == null)
							comment = new StringBuffer();
						comment.append(line);
						comment.append('\n');
					}
				} else if (line.startsWith("[")) {
					String name = line.trim();
					if (name.endsWith("]")) {
						section = name.substring(1, name.length() - 1);
						continue;
					}
				} else {
					if (line.endsWith("\\")) {
						linebuffer.append(line.substring(0, line.length()-1));
					}
					else {
						linebuffer.append(line.trim());
						String entryLine = linebuffer.toString();
						linebuffer.setLength(0);
						int n = entryLine.indexOf('=');
						String key, value;
						if (n>=0) {
							key = entryLine.substring(0,n).trim();
							value = entryLine.substring(n+1).trim();
						}
						else {
							key = entryLine.trim();
							value = "";
						}
						if (section==null) {
							main.put(key, value);
						}
						else {
							Properties sprops = sectionMap.get(section);
							if (sprops==null) {
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

	}

	/**
	 * Saves sectioned properties to the given file.
	 *
	 * @param f
	 *            file to write to.
	 */
	void save(File f) {
	}

}
