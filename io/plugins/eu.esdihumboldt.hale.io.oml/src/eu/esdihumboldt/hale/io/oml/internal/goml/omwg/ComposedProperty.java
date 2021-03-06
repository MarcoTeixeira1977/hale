/*
 * Copyright (c) 2012 Data Harmonisation Panel
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     HUMBOLDT EU Integrated Project #030962
 *     Data Harmonisation Panel <http://www.dhpanel.eu>
 */
package eu.esdihumboldt.hale.io.oml.internal.goml.omwg;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import eu.esdihumboldt.hale.io.oml.internal.goml.rdf.About;
import eu.esdihumboldt.hale.io.oml.internal.model.rdf.IAbout;

/**
 * This class represents omwg:propertyConst. Used for building an expression
 * that constructs a Property based on other Properties.
 * 
 * @author Thorsten Reitz, Marian de Vries
 * @partner 01 / Fraunhofer Institute for Computer Graphics Research
 * @partner 08 / Delft University of Technology
 */
@SuppressWarnings("javadoc")
public class ComposedProperty extends Property {

	/**
	 * Indicates the (first) operator/predicate for this
	 * {@link ComposedProperty}.
	 */
	private final PropertyOperatorType operator;

	/**
	 * In case of AND and OR the operand is a collection of two or more
	 * (Composed)PropertyClasses. In case of a unary operator the operand is
	 * either: - a collection consisting of one Property (or ComposedProperty);
	 * - a Relation (or ComposedRelation). Only one of the two is allowed - at
	 * the moment not forced, must be dealt with by application code.
	 * 
	 */
	private List<Property> collection;
	private Relation relation;

	// constructors ............................................................

	/**
	 * @return the relation
	 */
	public Relation getRelation() {
		return relation;
	}

	/**
	 * @param relation the relation to set
	 */
	public void setRelation(Relation relation) {
		this.relation = relation;
	}

	/**
	 * Constructor
	 * 
	 * @param operator
	 * @param about
	 */
	public ComposedProperty(PropertyOperatorType operator, IAbout about) {
		super(about);
		this.operator = operator;
		this.collection = new ArrayList<Property>();
	}

	/**
	 * Constructor
	 * 
	 * sets a default operator value as {@link PropertyOperatorType.OR}
	 * 
	 * @param about IAbout
	 */
	public ComposedProperty(IAbout about) {
		super(about);
		// sets union as default operator
		this.operator = PropertyOperatorType.OR;
		this.collection = new ArrayList<Property>();
	}

	// FIXME
	public ComposedProperty(PropertyOperatorType operator, String namespace) {
		this(operator, new About(namespace, UUID.randomUUID().toString()));
	}

	// FIXME
	public ComposedProperty(String namespace) {
		this(new About(namespace, UUID.randomUUID().toString()));
	}

	// getters / setters .......................................................

	/**
	 * @see eu.esdihumboldt.hale.io.oml.internal.goml.omwg.Property#getFeatureClassName()
	 */
	@Override
	public String getFeatureClassName() {
		// TODO what should the composed property return here?
		return getLocalname();
	}

	/**
	 * @see eu.esdihumboldt.hale.io.oml.internal.goml.omwg.Property#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return this.getAbout().getAbout()
				.substring(0, (this.getAbout().getAbout().lastIndexOf("/")));
	}

	/**
	 * @return the collection
	 */
	public List<Property> getCollection() {
		return collection;
	}

	/**
	 * @param collection the collection to set
	 */
	public void setCollection(List<Property> collection) {
		this.collection = collection;
	}

	/**
	 * @return the operator
	 */
	public PropertyOperatorType getPropertyOperatorType() {
		return operator;
	}

	public enum PropertyOperatorType {
		AND, // intersection
		OR, // union
		NOT, FIRST, NEXT
	}

}
