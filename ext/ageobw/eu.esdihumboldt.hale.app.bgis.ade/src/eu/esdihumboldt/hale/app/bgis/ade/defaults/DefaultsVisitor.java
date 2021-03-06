/*
 * Copyright (c) 2013 Fraunhofer IGD
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
 *     Fraunhofer IGD
 */

package eu.esdihumboldt.hale.app.bgis.ade.defaults;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import eu.esdihumboldt.hale.app.bgis.ade.common.BGISAppConstants;
import eu.esdihumboldt.hale.app.bgis.ade.common.BGISAppUtil;
import eu.esdihumboldt.hale.app.bgis.ade.common.EntityVisitor;
import eu.esdihumboldt.hale.app.bgis.ade.defaults.config.DefaultValues;
import eu.esdihumboldt.hale.common.align.model.Entity;
import eu.esdihumboldt.hale.common.align.model.MutableCell;
import eu.esdihumboldt.hale.common.align.model.ParameterValue;
import eu.esdihumboldt.hale.common.align.model.Priority;
import eu.esdihumboldt.hale.common.align.model.functions.AssignFunction;
import eu.esdihumboldt.hale.common.align.model.functions.GenerateUIDFunction;
import eu.esdihumboldt.hale.common.align.model.impl.DefaultCell;
import eu.esdihumboldt.hale.common.align.model.impl.DefaultProperty;
import eu.esdihumboldt.hale.common.align.model.impl.PropertyEntityDefinition;
import eu.esdihumboldt.hale.common.schema.model.TypeDefinition;
import eu.esdihumboldt.hale.common.schema.model.constraint.property.Cardinality;
import eu.esdihumboldt.hale.common.schema.model.constraint.type.Binding;
import eu.esdihumboldt.hale.common.schema.model.constraint.type.Enumeration;
import eu.esdihumboldt.hale.common.schema.model.constraint.type.HasValueFlag;
import eu.esdihumboldt.hale.functions.bgis.capturespec.DataCaptureSpecFunction;
import eu.esdihumboldt.hale.functions.bgis.sourcedesc.SourceDescriptionFunction;

/**
 * Entity visitor that creates cells assigning default values.
 * 
 * @author Simon Templer
 */
public class DefaultsVisitor extends EntityVisitor implements BGISAppConstants {

	/**
	 * The created cells.
	 */
	private final List<MutableCell> cells = new ArrayList<MutableCell>();

	private final DefaultValues defaultValues;

	/**
	 * Create a default value visitor creating assignment cells.
	 * 
	 * @param defaultValues the custom default value configuration, may be
	 *            <code>null</code>
	 */
	public DefaultsVisitor(DefaultValues defaultValues) {
		this.defaultValues = defaultValues;
	}

	@Override
	protected boolean visit(PropertyEntityDefinition ped) {
		if (ADE_NS.equals(ped.getDefinition().getName().getNamespaceURI())) {
			// property is from ADE

			if (ped.getDefinition().getPropertyType().getConstraint(HasValueFlag.class).isEnabled()) {
				/*
				 * Property is represented by a simple type (only there it makes
				 * sense to assign defaults)
				 */

				// special handling allowing defaults
				if ("dataCaptureSpec".equals(ped.getDefinition().getName().getLocalPart())) {
					addAugmentationCell(ped, DataCaptureSpecFunction.ID, Priority.LOW);
				}

				// special handling allowing no defaults
				if ("sourceDescription".equals(ped.getDefinition().getName().getLocalPart())) {
					addAugmentationCell(ped, SourceDescriptionFunction.ID, Priority.LOWEST);
				}
				// default values
				else {
					String value = null;
					// check config for default value
					value = defaultValues.getDefaultValue(ped);
					/*
					 * Assign custom default value for any property or generic
					 * default value for mandatory property
					 */
					if (value != null
							|| ped.getDefinition().getConstraint(Cardinality.class).getMinOccurs() > 0) {
						addDefaultCell(ped, value);
					}
				}
			}

			return true;
		}
		else {
			// property not from ADE

			// handle mandatory XML IDs in complex properties
			if (ped.getPropertyPath().size() > 1 // ignore feature ID
					&& ped.getDefinition().getConstraint(Cardinality.class).getMinOccurs() > 0
					&& BGISAppUtil.isID(ped.getDefinition().getPropertyType())) {
				// TODO also check the wrapping property actually is mandatory?
				addAugmentationCell(ped, GenerateUIDFunction.ID, Priority.LOWEST);
			}
		}

		return false;
	}

	/**
	 * Add a cell assigning a default value to the given entity.
	 * 
	 * @param ped the property entity definition
	 * @param value the value to assign or <code>null</code> if it should be
	 *            auto-detected
	 */
	private void addDefaultCell(PropertyEntityDefinition ped, String value) {
		String note;

		// determine value to assign
		if (value == null) {
			value = determineDefaultValue(ped.getDefinition().getPropertyType());
			note = "Generated default value based on property type.";
		}
		else {
			note = "Generated cell with specified default value.";
		}
		if (value == null) {
			return;
		}

		// create cell template
		MutableCell cell = new DefaultCell();
		cell.setPriority(Priority.LOWEST);
		ListMultimap<String, Entity> target = ArrayListMultimap.create();
		cell.setTarget(target);
		ListMultimap<String, ParameterValue> parameters = ArrayListMultimap.create();
		cell.setTransformationParameters(parameters);

		// set transformation identifier (Assign)
		cell.setTransformationIdentifier(AssignFunction.ID);
		// set cell target (Property)
		target.put(null, new DefaultProperty(ped));
		// set cell parameters (Value)
		parameters.put(AssignFunction.PARAMETER_VALUE, new ParameterValue(value));

		BGISAppUtil.appendNote(cell, note);

		cells.add(cell);
	}

	/**
	 * Add a simple cell using an augmentation w/o parameters.
	 * 
	 * @param ped the property entity definition
	 * @param functionId the function identifier
	 * @param priority the cell priority
	 */
	private void addAugmentationCell(PropertyEntityDefinition ped, String functionId,
			Priority priority) {
		// create cell template
		MutableCell cell = new DefaultCell();
		cell.setPriority(priority);
		ListMultimap<String, Entity> target = ArrayListMultimap.create();
		cell.setTarget(target);

		// set transformation identifier (Function ID)
		cell.setTransformationIdentifier(functionId);
		// set cell target (Property)
		target.put(null, new DefaultProperty(ped));

		BGISAppUtil.appendNote(cell, "Generated default mapping.");

		cells.add(cell);
	}

	/**
	 * Determine the default value to use for the given property type.
	 * 
	 * @param propertyType the type definition
	 * @return the value or <code>null</code>
	 */
	private String determineDefaultValue(TypeDefinition propertyType) {
		// check for enumeration
		Enumeration<?> vals = propertyType.getConstraint(Enumeration.class);
		if (vals.getValues() != null) {
			if (vals.getValues().contains(ENUMERATION_DEFAULT)) {
				return ENUMERATION_DEFAULT;
			}
		}

		// check for a number binding
		Class<?> binding = propertyType.getConstraint(Binding.class).getBinding();
		if (Number.class.isAssignableFrom(binding)) {
			return NUMBER_DEFAULT;
		}

		return DEFAULT;

		// TODO convert/validate the value to check?
	}

	/**
	 * Get the created cells.
	 * 
	 * @return the cells assigning default values
	 */
	public List<MutableCell> getCells() {
		return cells;
	}

}
