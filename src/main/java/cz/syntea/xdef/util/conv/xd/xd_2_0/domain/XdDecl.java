/*
 * File: XdDecl.java
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
package cz.syntea.xdef.util.conv.xd.xd_2_0.domain;

/** Represents X-definition <tt>declaration</tt> model.
 * @author Ilia Alexandrov
 */
public final class XdDecl extends XdModel {

	/**
	 * Hashcode.
	 */
	private int _hashCode = 0;

	/** Creates instance of declaration model.
	 *
	 * @param def declaration X-definition.
	 * @param name name of delcaration.
	 * @throws NullPointerException if given model X-definition or declaration
	 * name is <tt>null</tt>.
	 * @throws IllegalArgumentException if given declaration name is empty.
	 */
	public XdDecl(XdDef def, String name) {
		super(def, name);
	}

	@Override
	public int getType() {return Type.DECLARATION;}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XdDecl)) {
			return false;
		}
		XdDecl d = (XdDecl) obj;
		return _name.equals(d._name);
	}
	@Override
	public int hashCode() {
		if (_hashCode == 0) {
			_hashCode = 43 * _name.hashCode();
		}
		return _hashCode;
	}
	@Override
	public String toString() {
		return "XdDecl[def='" + _def + "', " + "name='" + _name + "']";
	}
}