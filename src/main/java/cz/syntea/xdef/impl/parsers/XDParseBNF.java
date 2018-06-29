/*
 * Copyright 2015 Syntea software group a.s. All rights reserved.
 *
 * File: XDParseBNF.java, created 2015-03-03.
 * Package: cz.syntea.xd.impl.parsers-
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENCE.TXT.
 *
 */
package cz.syntea.xdef.impl.parsers;

import cz.syntea.xdef.msg.BNF;
import cz.syntea.xdef.sys.ArrayReporter;
import cz.syntea.xdef.sys.SException;
import cz.syntea.xdef.sys.SRuntimeException;
import cz.syntea.xdef.sys.StringParser;
import cz.syntea.xdef.XDNamedValue;
import cz.syntea.xdef.XDParseResult;
import cz.syntea.xdef.XDParserAbstract;
import cz.syntea.xdef.XDValue;
import cz.syntea.xdef.proc.XXNode;
import cz.syntea.xdef.impl.code.DefBNFGrammar;
import cz.syntea.xdef.impl.code.DefBNFRule;
import cz.syntea.xdef.impl.code.DefContainer;
import cz.syntea.xdef.XDContainer;

/**
 *
 * @author Vaclav Trojan
 */
public class XDParseBNF extends XDParserAbstract {

	private static final String ROOTBASENAME = "BNF";

	private DefBNFRule _rule;

	public XDParseBNF() {super(); _rule = null;} // dummy

	@Override
	public void parseObject(XXNode xnode, XDParseResult p) {
		int pos0 = p.getIndex();
		StringParser parser = new StringParser(p.getSourceBuffer(), pos0);
		XDParseResult r = _rule.perform(parser);
		p.setParsedValue(r.getParsedValue());
		p.addReports(p.getReporter());
		p.setBufIndex(parser.getIndex());
		p.isSpaces();
	}

	@Override
	public void setNamedParams(final XXNode xnode, final XDContainer params)
		throws SException {
		int num;
		if (params == null || (num = params.getXDNamedItemsNumber()) == 0) {
			return;
		}
		XDNamedValue[] items = params.getXDNamedItems();
		DefBNFGrammar g = null;
		String ruleName = null;
		for (int i = 0; i < num; i++) {
			String name = items[i].getName();
			XDValue x = items[i].getValue();
			if ("a1".equals(name)) {
				if (x.getItemId() == XD_BNFRULE) {
					_rule = (DefBNFRule) x;
				} else if (x.getItemId() != XD_BNFGRAMMAR) {
					//Incorrect method parameter
					throw new SException(BNF.BNF014);
				} else {
					g = (DefBNFGrammar) x;
				}
			} else if ("a2".equals(name)) {
				ruleName = x.toString();
			}
		}
		if (_rule == null) {
			if (g == null) {
				throw new SException(BNF.BNF001); //BNF grammar not exists
			} else if (ruleName == null) {
				throw new SException(BNF.BNF901);//Rule '&{0}' doesn't exist
			} else {
				_rule = g.getRule(ruleName);
			}
		}
		if (_rule == null) {
			//Rule '&{0}' doesn't exist
			throw new SException(BNF.BNF901, ruleName);
		}
	}
	@Override
	/** Set value of two "sequential" parameters of parser.
	 * @param par1 the first "sequential" parameter.
	 * @param par2 the second "sequential" parameter.
	 */
	public void setParseParams(final Object par1, final Object par2) {
		ArrayReporter reporter = new ArrayReporter();
		DefBNFGrammar g = new DefBNFGrammar(par1.toString(), reporter);
		reporter.checkAndThrowErrors();
		String ruleName = par2.toString();
		_rule = g.getRule(ruleName);
		if (_rule == null) {
			// Rule '&{0}' doesn't exist
			throw new SRuntimeException(BNF.BNF901, ruleName);
		}
	}
	@Override
	public XDContainer getNamedParams() {
		XDContainer map = new DefContainer();
		map.setXDNamedItem("a1", _rule);
		return map;
	}
	@Override
	public String parserName() {
		return ROOTBASENAME;
	}
	@Override
	public short parsedType() {return XD_CONTAINER;}

}