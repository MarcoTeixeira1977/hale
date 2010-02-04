/*
 * HUMBOLDT: A Framework for Data Harmonisation and Service Integration.
 * EU Integrated Project #030962                 01.10.2006 - 30.09.2010
 * 
 * For more information on the project, please refer to the this web site:
 * http://www.esdi-humboldt.eu
 * 
 * LICENSE: For information on the license under which this program is 
 * available, please refer to http:/www.esdi-humboldt.eu/license.html#core
 * (c) the HUMBOLDT Consortium, 2007 to 2010.
 */

package eu.esdihumboldt.hale.rcp.utils.tree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.TreeNode;

/**
 * Tree node that associates children with a key
 * 
 * @param <T> the key type
 * @param <N> the node type
 *
 * @author Simon Templer
 * @partner 01 / Fraunhofer Institute for Computer Graphics Research
 * @version $Id: MultiColumnTreeNode.java 2510 2010-01-21 08:49:00Z stempler $ 
 */
public class MapMultiColumnTreeNode<T, N extends TreeNode> extends AbstractMultiColumnTreeNode {
	
	private final Map<T, N> children = new HashMap<T, N>();

	/**
	 * Create a new node
	 * 
	 * @param values the node values
	 */
	public MapMultiColumnTreeNode(Object... values) {
		super(values);
	}
	
	/**
	 * Add a child to the node
	 * 
	 * @param key the key 
	 * @param child the child node
	 */
	public void addChild(T key, N child) {
		children.put(key, child);
		child.setParent(this);
	}
	
	/**
	 * Get the child with the given key
	 * 
	 * @param key the key
	 * 
	 * @return the child or <code>null</code>
	 */
	public N getChild(T key) {
		return children.get(key);
	}
	
	/**
	 * Remove the child node with the given key
	 * 
	 * @param key the child node
	 */
	public void removeChild(T key) {
		children.remove(key);
	}

	/**
	 * @see TreeNode#setChildren(TreeNode[])
	 */
	@Override
	public void setChildren(TreeNode[] children) {
		throw new UnsupportedOperationException();
	}

	/**
	 * @see AbstractMultiColumnTreeNode#getChildNodes()
	 */
	@Override
	protected Collection<? extends TreeNode> getChildNodes() {
		return children.values();
	}

	/**
	 * Remove a child node
	 * 
	 * @param child the child node
	 */
	public void removeChildNode(N child) {
		Iterator<Entry<T, N>> it = children.entrySet().iterator();
		T key = null;
		while (key == null && it.hasNext()) {
			Entry<T, N> entry = it.next();
			if (entry.getValue().equals(child)) {
				key = entry.getKey();
			}
		}
		
		if (key != null) {
			children.remove(key);
		}
	}

}
