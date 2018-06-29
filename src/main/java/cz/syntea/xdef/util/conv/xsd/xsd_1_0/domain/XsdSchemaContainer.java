/*
 * File: XsdSchemaContainer.java
 *
 * Copyright 2007 Syntea software group a.s.
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 */
package cz.syntea.xdef.util.conv.xsd.xsd_1_0.domain;

/** Represents XML Schema container.
 * @author Ilia Alexandrov
 */
public interface XsdSchemaContainer {

	/**
	 * Schema container type enumeration.
	 */
	public static interface Type {

		/**
		 * Single schema type.
		 */
		public static final int SINGLE_SCHEMA = 1;
		/**
		 * Schema set type.
		 */
		public static final int SCHEMA_SET = SINGLE_SCHEMA + 1;
	}

	/** Returns type constant of current schema container implementation.
	 *
	 * @return type constant of current schema container implementation.
	 */
	public int getType();
}