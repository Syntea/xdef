package org.xdef.proc;

/** Provides methods for XDLexicon.
 * @author Vaclav Trojan
 */
public interface XDLexicon {

	/** Get ID of language.
	 * @param language language name
	 * @return ID of language.
	 * @throws RuntimeException if language is not declared in lexicon.
	 */
	public int getLanguageID(final String language);

	/** Find text assigned to a key for given language.
	 * @param key reference key.
	 * @param languageID language ID.
	 * @return word assigned for given language to the reference alias or return
	 * null if such alias doesn't exist.
	 */
	public String findText(final String key, final int languageID);

	/** Get array of language names.
	 * @return array of language names.
	 */
	public String[] getLanguages();

	/** Get array of language texts corresponding to given reference key.
	 * @param key reference key.
	 * @return array of language words.
	 */
	public String[] findTexts(final String key);

	/** Get array of all keys from lexicon.
	 * @return array of all keys from lexicon.
	 */
	public String[] getKeys();

}