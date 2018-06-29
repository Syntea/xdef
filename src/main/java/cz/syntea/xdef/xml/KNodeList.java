/*
 * Copyright 2009 Syntea software group a.s. All rights reserved.
 *
 * File: KNodeList.java
 *
 * This file may be used, copied, modified and distributed only in accordance
 * with the terms of the limited license contained in the accompanying
 * file LICENSE.TXT.
 *
 * Tento soubor muze byt pouzit, kopirovan, modifikovan a siren pouze v souladu
 * s licencnimi podminkami uvedenymi v prilozenem souboru LICENSE.TXT.
 *
 */
package cz.syntea.xdef.xml;

import java.util.ArrayList;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** Simple implementation of org.w3c.dom.NodeList interface. It also allows
 * to use methods addItem, clearItems, addAllItems and containsItem.
 * @author Vaclav Trojan
 */
public class KNodeList extends ArrayList<Node> implements NodeList {

	/** Create empty list. */
	KNodeList() {super();}

	/** Create list with one node from the argument.
	 * @param x node which will be in the created list.
	 */
	KNodeList(final Node x) {
		super();
		if (x != null){
			add(x);
		}
	}

	/** Construct list with items from the list from argument.
	 * @param x the list with items which will be in the created list.
	 */
	KNodeList(final NodeList x) {
		this();
		if (x != null) {
			for (int i = 0; i < x.getLength(); i++) {
				add(x.item(i));
			}
		}
	}

	@Override
	/** Get number of items in this list. */
	public final int getLength() {return size();}

	@Override
	/** Get node from the index in this list.
	 * @return node from the index in this list.
	 */
	public final Node item(final int index) {return get(index);}

	/** Add an item to the position given by argument index.
	 * @param index
	 * @param item
	 */
	final void addItem(final int index, final Node item) {add(index, item);}

	/** Add an item to the end of list..
	 * @param index index where to add.
	 * @param item the item to be added.
	 */
	final void addItem(final Node item) {add(item);}

	/** Delete all items in this list. */
	final void clearItems() {clear();}

	/** Add items from NodeList to this list.
	 * @param nl list with items to be added.
	 */
	final void addAllItems(final NodeList nl) {
		for (int i = 0; i < nl.getLength(); i++) {
			add(nl.item(i));
		}
	}

	/** Check if an item is in thi list.
	 * @param node node to be checked.
	 * @return true if the node exists in the list.
	 */
	final boolean containsItem(final Node node) {
		for (int i = 0; i < getLength(); i++) {
			if (node == item(i)) {
				return true;
			}
		}
		return false;
	}
}