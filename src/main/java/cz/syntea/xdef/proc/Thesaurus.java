/*
 * Copyright 2017 Syntea software group a.s. All rights reserved.
 *
 * File: Thesaurus.java, created 2017-04-11.
 * Package: cz.syntea.xd
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENCE.TXT.
 *
 */
package cz.syntea.xdef.proc;

/** Provides methods for thesaurus
 * @author Vaclav Trojan
 */
public interface Thesaurus {

	/** Get ID of language.
	 * @param language language name
	 * @return ID of language.
	 * @throws RuntimeException if language is not declared in thesaurus.
	 */
	public int getLanguageID(final String language);
//
//	/** Check if the language is default.
//	 * @param language language name
//	 * @return true if the language is default.
//	 */
//	public boolean isDefault(final String language);

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

	/** Get array of all keys from thesaurus.
	 * @return array of all keys from thesaurus.
	 */
	public String[] getKeys();

}