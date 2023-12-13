package org.xdef.impl.parsers;

import org.xdef.msg.BNF;
import org.xdef.sys.ArrayReporter;
import org.xdef.sys.SException;
import org.xdef.sys.SRuntimeException;
import org.xdef.sys.StringParser;
import org.xdef.XDNamedValue;
import org.xdef.XDParseResult;
import org.xdef.XDParserAbstract;
import org.xdef.XDValue;
import org.xdef.proc.XXNode;
import org.xdef.impl.code.DefBNFGrammar;
import org.xdef.impl.code.DefBNFRule;
import org.xdef.impl.code.DefContainer;
import org.xdef.XDContainer;
import static org.xdef.XDValueID.XD_BNFGRAMMAR;
import static org.xdef.XDValueID.XD_BNFRULE;
import static org.xdef.XDValueID.XD_CONTAINER;
import org.xdef.sys.SBuffer;

/** Parse BNF
 * @author Vaclav Trojan
 */
public class XDParseBNF extends XDParserAbstract {

	private static final String ROOTBASENAME = "BNF";

	private DefBNFRule _rule;

	public XDParseBNF() {super(); _rule = null;} // dummy

	@Override
	public void parseObject(XXNode xnode, XDParseResult p) {
		int pos0 = p.getIndex();
		StringParser parser = new StringParser(new SBuffer(p.getSourceBuffer(),
			xnode == null ? null : xnode.getSPosition()));
		parser.setIndex(pos0);
		XDParseResult r = _rule.perform(parser);
		p.setParsedValue(r.getParsedValue());
		p.addReports(r.getReporter());
		p.setIndex(parser.getIndex());
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
	public void setParseSQParams(final Object... params) {
		ArrayReporter reporter = new ArrayReporter();
		DefBNFGrammar g = new DefBNFGrammar(params[0].toString(), reporter);
		reporter.checkAndThrowErrors();
		String ruleName = params[1].toString();
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
	public String parserName() {return ROOTBASENAME;}
	@Override
	public short parsedType() {return XD_CONTAINER;}
}