package org.xdef.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import org.xdef.msg.XDEF;
import org.xdef.proc.XDLexicon;
import org.xdef.sys.SRuntimeException;

/** Implementation of XDLexicon.
 * @author Vaclav Trojan
 */
public final class XLexicon implements XDLexicon {
	final String[] _languages;
	final Map<String, String[]> _dictionaries;

	/** Create new instance of lexicon.
	 * @param languageNames list of lexicon names.
	 */
	public XLexicon(final String... languageNames) {
		_languages = languageNames;
		_dictionaries = new LinkedHashMap<>();
	}

	/** Get ID of language.
	 * @param language language name
	 * @return ID of language.
	 * @throws SRuntimeException if language is not declared in lexicon.
	 */
	@Override
	public final int getLanguageID(final String language) throws SRuntimeException {
		for (int index = 0; index < _languages.length; index++) {
			if (_languages[index].equals(language)) {
				return index;
			}
		}
		throw new SRuntimeException(XDEF.XDEF144, language); //Incorrect lexicon language ID: &{0}
	}

	/** Set lexicon item.
	 * @param key Reference alias name
	 * @param languageID language ID
	 * @param text a word to be set.
	 * @throws SRuntimeException if item can't be set.
	 */
	public final void setItem(final String key,
		final int languageID,
		final String text) {
		if (languageID < 0 || languageID >= _languages.length) {
			throw new SRuntimeException(XDEF.XDEF144, languageID); //Incorrect lexicon language ID: &{0}
		}
		String[] words = _dictionaries.get(key);
		if (words == null) {
			// new alias
			words = new String[_languages.length];
			_dictionaries.put(key, words);
		}
		if (words[languageID] != null && !words[languageID].equals(text)) {
			//Redefinition of reference alias &{0} and language &{1}: theword &{2} already exists as &{3}.
			throw new SRuntimeException(XDEF.XDEF145, key, _languages[languageID], text, words[languageID]);
		}
		words[languageID] = text;
	}

	/** Find text assigned to a key for given language.
	 * @param key reference key.
	 * @param languageID language ID.
	 * @return word assigned for given language to the reference alias or return
	 * null if such alias doesn't exist.
	 */
	@Override
	public String findText(final String key, final int languageID) {
		String[] words = _dictionaries.get(key);
		return words != null ? words[languageID] : null;

	}

	/** Get array of language names.
	 * @return array of language names.
	 */
	@Override
	public final String[] getLanguages() {return _languages;}

	/** Get array of language texts corresponding to given reference key.
	 * @param key reference key.
	 * @return array of language words.
	 */
	@Override
	public String[] findTexts(final String key) {
		return _dictionaries.get(key);
	}

	/** Get array of all keys from lexicon.
	 * @return array of all keys from lexicon.
	 */
	@Override
	public String[] getKeys() {
		String[] result = new String[_dictionaries.size()];
		_dictionaries.keySet().toArray(result);
		return result;
	}
}