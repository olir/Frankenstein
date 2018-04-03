package de.serviceflow.frankenstein;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.io.Writer;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class TextSet extends AbstractSet<String> {

	private SortedSet<String> s = Collections.synchronizedSortedSet(new TreeSet<String>());

	@Override
	public Iterator<String> iterator() {
		return s.iterator();
	}

	@Override
	public int size() {
		return s.size();
	}

	@Override
	public void clear() {
		s.clear();
	}

	
	@Override
	public boolean add(String e) {
		return s.add(e);
	}
	
	public void store(Writer writer) throws IOException {
		for (Iterator<String> iter = iterator(); iter.hasNext();) {
			String text = iter.next();
			text.replace("\\", "\\\\");
			text.replace("\n", "\\/n");
			writer.write(text);
			writer.write('\n');
		}
	}

	public void load(Reader reader) throws IOException {
		LineNumberReader lnr = new LineNumberReader(reader);
		String text;
		String multiLineBuffer = null;
		while ((text = lnr.readLine()) != null) {
			if (text.endsWith("\\")) {
				text.replace("\n", "\\/n");
				if (multiLineBuffer==null) {
					multiLineBuffer = text;
				}
				else {
					multiLineBuffer = multiLineBuffer + text;
				}
				continue;
			}
			if (multiLineBuffer!=null) {
				text = multiLineBuffer + text;
				multiLineBuffer = null;
			}
			text.replace("\\\\", "\\");
			if (text.length()>0)
				s.add(text);
		}
		if (multiLineBuffer!=null) {
			multiLineBuffer.replace("\\\\", "\\");
			if (multiLineBuffer.length()>0)
				s.add(multiLineBuffer);
		}
	}

}
