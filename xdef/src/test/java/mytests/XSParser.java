package mytests;

import java.util.ArrayList;
import java.util.List;
import org.xdef.impl.compile.XScriptParser;
import static org.xdef.impl.compile.XScriptParser.CREATE_SYM;
import static org.xdef.impl.compile.XScriptParser.DEFAULT_SYM;
import static org.xdef.impl.compile.XScriptParser.FINALLY_SYM;
import static org.xdef.impl.compile.XScriptParser.FIXED_SYM;
import static org.xdef.impl.compile.XScriptParser.INIT_SYM;
import static org.xdef.impl.compile.XScriptParser.MATCH_SYM;
import static org.xdef.impl.compile.XScriptParser.OCCURS_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_ABSENCE_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_EXCESS_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_FALSE_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_ILLEGAL_ATTR_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_START_ELEMENT_SYM;
import static org.xdef.impl.compile.XScriptParser.ON_TRUE_SYM;
import static org.xdef.impl.compile.XScriptParser.OPTION_SYM;
import static org.xdef.impl.compile.XScriptParser.OPTIONS_SYM;
import static org.xdef.impl.compile.XScriptParser.SEMICOLON_SYM;
import org.xdef.msg.XDEF;
import org.xdef.sys.SBuffer;
import static org.xdef.sys.SParser.NOCHAR;
import org.xdef.sys.SPosition;

/**
 *
 * @author Vaclav Trojan
 */
public class XSParser extends XScriptParser {
	XSParser() {super((byte) 0);}

	/** Check if id of parsed section name is a section name.
	 * @param sym ID of parsed section name.
	 * @return true if it is a section name.
	 */
	private static boolean isSectionCommand(final char sym) {
		return sym==VAR_SYM||sym==FINALLY_SYM||sym==CREATE_SYM
			|| sym==ON_TRUE_SYM||sym==ON_FALSE_SYM||sym==ON_ABSENCE_SYM
			|| sym==ON_ILLEGAL_ATTR_SYM||sym==CREATE_SYM||sym==MATCH_SYM
			|| sym==ON_START_ELEMENT_SYM||sym==FINALLY_SYM||sym==FORGET_SYM
			|| sym==INIT_SYM||sym==DEFAULT_SYM||sym==FIXED_SYM||sym==REF_SYM
			|| sym==ON_EXCESS_SYM||sym==OPTION_SYM||sym==OPTIONS_SYM;
	}

	/** Parse command which follows section.
	 * @return true if section command was parsed.
	 */
	private boolean readSectionCommand() {
		int pos = getIndex();
		if (_sym == BEG_SYM) {
			int n = 1;
			do {
				if (nextSymbol() == END_SYM) {
					if (--n == 0) {
						break;
					}
				} else if (_sym == BEG_SYM) {
					n++;
				}
			} while(!eos());
		} else {
			if (_sym != SEMICOLON_SYM && _sym!= NOCHAR) {
				while(nextSymbol() != SEMICOLON_SYM && _sym!= NOCHAR){}
			}
		}
		return getIndex() > pos;
	}

	/** Check if it is an occurrence specification.
	 * @return SPosition of parsed occurrence specification or null
	 */
	private SPosition isOccurrence() {
		SPosition spos = getLastPosition();
		if ((_sym == OCCURS_SYM)) {
			nextSymbol();
		}
		switch (_sym) {
			case MUL_SYM:
			case PLUS_SYM:
			case REQUIRED_SYM:
			case ASK_SYM:
			case OPTIONAL_SYM:
			case IGNORE_SYM:
			case ILLEGAL_SYM:
				return spos;
			case CONSTANT_SYM:
				int pos = getIndex();
				char sym = _sym;
				if (nextSymbol() != DDOT_SYM) {
					setIndex(pos); // reset position
					_sym = sym;
				} else {
					sym = _sym;
					pos = getIndex();
					if (nextSymbol() != CONSTANT_SYM && _sym != MUL_SYM) {
						setIndex(pos);  // reset position
						_sym = sym;
					}
				}
				return spos;
			default:
				return null;
		}
	}

	/** Add section item to the list.
	 * @param sectionName section name,
	 * @param sectionList where to add.
	 * @param spos SPosition of the section.
	 */
	private void addSection(final String sectionName,
		final List<Object> sectionList,
		final SPosition spos) {
		sectionList.add(sectionName);
		String s = getParsedBufferPartFrom(spos.getIndex()).trim();
		while (s.endsWith(";")) {
			s = s.substring(0, s.length() - 1).trim();
		}
		sectionList.add(new SBuffer(s, spos));
	}

	/** Parse X-script and return the section list.
	 * @param source Source text with X-script.
	 * @return section list. Each section is composed of two items: the first
	 * item is id of section (a character) and the following item is a SBuffer
	 * with the source of the section command.
	 */
	private List<Object> parseXscript(final String source) {
		setSourceBuffer(source);
		return parseXscript();
	}

	/** Parse X-script and return the section list.
	 * @param source Source text with X-script.
	 * @return section list. Each section is composed of two items: the first
	 * item is id of section (a character) and the following item is a SBuffer
	 * with the source of the section command.
	 */
	private List<Object> parseXscript(final SBuffer source) {
		setSourceBuffer(source);
		return parseXscript();
	}

	/** Parse X-script and return the section list.
	 * @return section list. Each section is composed of two items: the first
	 * item is id of section (character) and the following item is a SBuffer
	 * with the source of the section command.
	 */
	private List<Object> parseXscript() {
		List<Object> sectionList = new ArrayList<>();
		SPosition spos = getPosition();
		nextSymbol();
		char sym;
		for (;;) {
			while (_sym == SEMICOLON_SYM || _sym == END_SYM) {
				nextSymbol();
				spos = getPosition();
			}
			if (_sym == NOCHAR) {
				break;
			}
			if ((spos = isOccurrence()) != null) {
				addSection("occurs", sectionList, spos);
				if (_sym == SEMICOLON_SYM) {
					continue;
				}
				if (!isSectionCommand(sym = _sym)) {
					spos = getPosition();
					if (readSectionCommand()) {
						String s = getParsedBufferPartFrom(spos.getIndex());
						if (!(s = s.trim()).equals(";")) { //it is not only ";"!
							addSection("", sectionList, spos);
						}
					}
				}
			} else if (!isSectionCommand(sym = _sym)) {
				spos = getLastPosition();
				if (!readSectionCommand()) {// this never should not happeh
					error(XDEF.XDEF425); //Script error&{#SYS000}
				}
				addSection("", sectionList, spos);
			} else {
				spos = getPosition();
				String sectionName = getParsedString();
				nextSymbol();
				if (sym != FORGET_SYM && readSectionCommand()) {
					addSection(sectionName, sectionList, spos);
				} else {  // here should be only "forget"
					addSection(sectionName, sectionList, getPosition());
				}
			}
			spos = getPosition();
		}
		return sectionList;
	}

	/** Create X-script string from the list of sections.
	 * @param sectionList list of sections.
	 * @return string with X-script source.
	 */
	private static String xsToString(final List<Object> sectionList) {
		String result = "";
		boolean wasOccurs = false;
		for (int i = 0; i < sectionList.size(); i++) {
			Object o = sectionList.get(i);
			if (o instanceof String) {
				String sectionName = (String) o;
				if (++i >= sectionList.size()) {
					result += sectionName;
					break;
				}
				o = sectionList.get(i);
				if ("occurs".equals(sectionName)) {
					if (!result.isEmpty() && !result.endsWith(";")
						&& !result.endsWith("}")) {
						result += ';';
					}
					result += ((SBuffer) o).getString();
					wasOccurs = true;
				} else if (sectionName.isEmpty()) { // type validation
					if (wasOccurs) {
						if (!result.isEmpty()) {
							result += ' ';
						}
						result += ((SBuffer) o).getString();
					} else {
						if (!result.isEmpty() && !result.endsWith(";")
							&& !result.endsWith("}")) {
							result += ';';
						}
						result += ((SBuffer) o).getString();
					}
					wasOccurs = false;
				} else {
					if (!result.isEmpty() && !result.endsWith(";")
						&& !result.endsWith("}")) {
						result += ';';
					}
					String s = ((SBuffer) o).getString();
					if (s.isEmpty()) {
						result += sectionName;
					} else {
						result += sectionName + ' ' + s;
					}
					wasOccurs = false;
				}
			}
		}
		if (!result.isEmpty() && !result.endsWith(";")) {
			result += ';';
		}
		return result;
	}

	/** Find given section in section list.
	 * @param name name of section or emptyString if it is velidation method.
	 * @param list list of section.
	 * @return index of found section in list or -1.
	 */
	private static int findSection(final String name, final List<Object> list) {
		for (int i = 0; i < list.size(); i+=2) {
			if (name.equals(list.get(i))) {
				return i;
			}
		}
		return -1;
	}

	/** Remove given section from section list.
	 * @param name name of section or emptyString if it is velidation method.
	 * @param list list of section.
	 * @return true if section was removed.
	 */
	private static boolean removeSection(final String name,
		final List<Object> list) {
		return removeSection(findSection(name, list), list);
	}

	/** Remove given section from section list.
	 * @param index index of section in the section list.
	 * @param list list of section.
	 * @return true if section was removed.
	 */
	private static boolean removeSection(final int index,
		final List<Object> list) {
		if (index >= 0 && index < list.size() + 1) {
				list.remove(index);
				list.remove(index);
				return true;
		}
		return false;
	}

	private static void test(String source) {
		List<Object> result = new XSParser().parseXscript(source);
		System.out.println("SCR:\n" + source);
		System.out.println(xsToString(result));
//		for (Object o: result) {
//			if (o instanceof SBuffer) {
//				System.out.println("SBUF: '" + ((SBuffer) o).getString() + "'");
//			} else {
//				System.out.println("NAME: '" + o + "'");
//			}
//		}
//		System.out.println(removeSection("", result) + ", "
//			+ removeSection("occurs", result));
//		System.out.println(xsToString(result));
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
//		System.out.println(Long.MAX_VALUE + ", " + ((Long.MAX_VALUE) ^ 0xFFFFFFFFFFFFFFFFL-1L));
//		for (int i = -16; i <= 16; i++) {
//			System.out.println((i - 1) ^ 0xFFFFFFFF);
//		}
//		if (true) return;

		test("occurs 3 int()");
		test("required");
		test("1");
		test("1..");
		test("*");
		test("1..3");
		test("occurs *");
		test("forget");
		test("string();");
		test("? string();");
		test("occurs ? string();");
		test("1.. string();"); //string ()
		test("1..3 string()"); //string ()
		test("finally outln();"); //finally  outln()
		test("string(); finally outln();");
		test("init {out('a');out('b');}");
		test("init {out('a');out('b');} forget; onAbsence outln();");
		test("init out('a'); string(); forget");
		test("init {out('a');out('b');} string();");
		test("var int x=0;;; init {out('a');out('b');} string(); finally outln(); create true;");
		test("var int x=0; init {out('a');out('b');} 1..3 string(); finally outln();");
		test("var {int x=0; String y;}1..*; match false; finally outln();");
		test("var {int x=0; String y;} ?; match {return false;}; finally {outln();}");
		test("forget; var {int x=0; String y;}?; ref A:B; finally {outln();}");
	}
}