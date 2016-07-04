/*
 * Copyright (c) 2016 Data Harmonisation Panel
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
 *     Data Harmonisation Panel <http://www.dhpanel.eu>
 */

package eu.esdihumboldt.hale.io.html.svg.json

import java.nio.file.Files
import java.nio.file.Path

import eu.esdihumboldt.cst.test.TransformationExample
import eu.esdihumboldt.cst.test.TransformationExamples
import eu.esdihumboldt.hale.common.core.io.report.IOReport
import eu.esdihumboldt.hale.common.core.io.supplier.FileIOSupplier
import eu.esdihumboldt.hale.common.schema.model.impl.DefaultSchemaSpace
import eu.esdihumboldt.hale.io.html.svg.mapping.json.JsonMappingExporter
import groovy.json.JsonSlurper


/**
 * Tests for {@link JsonMappingExporter}.
 * 
 * @author Simon Templer
 */
class JsonMappingExporterTest extends GroovyTestCase {

	/**
	 * Test creating a mapping documentation file based on
	 * {@link TransformationExamples#PROPERTY_JOIN}.
	 */
	void testExportPropertyJoin() {
		// load example project
		TransformationExample ex = TransformationExamples.getExample(TransformationExamples.PROPERTY_JOIN)

		Path file = Files.createTempFile('mapping', '.json')
		try {
			JsonMappingExporter exporter = new JsonMappingExporter()
			exporter.alignment = ex.alignment
			exporter.sourceSchema = new DefaultSchemaSpace().addSchema(ex.sourceSchema)
			exporter.targetSchema = new DefaultSchemaSpace().addSchema(ex.targetSchema)
			exporter.target = new FileIOSupplier(file.toFile())
			//TODO also retrieve project information?

			IOReport rep = exporter.execute(null)
			assertTrue 'Export failed', rep.isSuccess()
			assertEquals 0, rep.errors.size()

			def c = new JsonSlurper().parse(file.toFile())

			def cells = c.cells as List
			assertEquals 8, cells.size()
		} finally {
			Files.delete(file)
		}
	}
}
