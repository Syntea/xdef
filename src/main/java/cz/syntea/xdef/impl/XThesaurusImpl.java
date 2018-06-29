/*
 * Copyright 2017 Syntea software group a.s. All rights reserved.
 *
 * File: XThesaurusImpl.java, created 2017-04-11.
 * Package: cz.syntea.xd.impl
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENCE.TXT.
 *
 */
package cz.syntea.xdef.impl;

import cz.syntea.xdef.msg.XDEF;
import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.proc.Thesaurus;
import java.util.Map;
import java.util.TreeMap;

/** Implementation of thesaurus.
 * @author Vaclav Trojan
 */
public class XThesaurusImpl implements Thesaurus {
		final String[] _languages;
		final Map<String, String[]> _dictionary;

		/** Create new instance of thesaurus.
		 * @param languageNames array of thesaurus names.
		 */
		public XThesaurusImpl(final String[] languageNames) {
			_languages = languageNames;
			_dictionary = new TreeMap<String, String[]>();
		}

		@Override
		/** Get ID of language.
		 * @param language language name
		 * @return ID of language.
		 * @throws SRuntimeException if language is not declared in thesaurus.
		 */
		public final int getLanguageID(final String language) {
			for (int index = 0; index < _languages.length; index++) {
				if (_languages[index].equals(language)) {
					return index;
				}
			}
			throw new SRuntimeException("unknown language: " + language);
		}

		/** Set thesaurus item.
		 * @param key Reference alias name
		 * @param languageID language ID
		 * @param text a word to be set.
		 * @throws SRuntimeException if item can't be set.
		 */
		public final void setItem(final String key,
			final int languageID,
			final String text) {
			if (languageID < 0 || languageID >= _languages.length) {
				//Incorrect thesaurus language ID: &{0}
				throw new SRuntimeException(XDEF.XDEF144, languageID);
			}
			String[] words = _dictionary.get(key);
			if (words == null) {
				// new alias
				words = new String[_languages.length];
				_dictionary.put(key, words);
			}
			if (words[languageID] != null && !words[languageID].equals(text)) {
				//Redefinition of reference alias &{0} and language &{1}: the
				//word &{2} already exists as &{3}.
				throw new SRuntimeException(XDEF.XDEF145,
					key, _languages[languageID], text, words[languageID]);
			}
			words[languageID] = text;
		}

		@Override
		/** Find text assigned to a key for given language.
		 * @param key reference key.
		 * @param languageID language ID.
		 * @return word assigned for given language to the reference alias or return
		 * null if such alias doesn't exist.
		 */
		public String findText(final String key, final int languageID) {
			String[] words = _dictionary.get(key);
			return words != null ? words[languageID] : null;

		}

		@Override
		/** Get array of language names.
		 * @return array of language names.
		 */
		public final String[] getLanguages() {return _languages;}

		@Override
		/** Get array of language texts corresponding to given reference key.
		 * @param key reference key.
		 * @return array of language words.
		 */
		public String[] findTexts(final String key) {
			return _dictionary.get(key);
		}

		@Override
		/** Get array of all keys from thesaurus.
		 * @return array of all keys from thesaurus.
		 */
		public String[] getKeys() {
			String[] result = new String[_dictionary.size()];
			_dictionary.keySet().toArray(result);
			return result;
		}

	}