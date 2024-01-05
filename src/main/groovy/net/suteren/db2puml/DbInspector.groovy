package net.suteren.db2puml

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import groovy.util.logging.Slf4j
import domain.DbMetadata
import org.apache.commons.io.FilenameUtils
import picocli.CommandLine

@Slf4j
@CommandLine.Command(name = "db_inspector.groovy", description = "Render DB structure as plantuml", mixinStandardHelpOptions = true, version = '1.0.0', versionProvider = DbInspector.VersionProvider)
class DbInspector implements Runnable {

	@CommandLine.Option(names = ['-c', '--stdout'], description = 'Output to STDOUT.')
	boolean stdout = false

	@CommandLine.Option(names = ['-j', '--jdbc-url'], arity = '1', required = true, description = 'JDBC connection string.')
	String jdbcUrl

	@CommandLine.Option(names = ['-u', '--user'], arity = '1', description = 'Database user name.')
	String userName

	@CommandLine.Option(names = ['-p', '--password'], arity = '1', description = 'Database user password.')
	String password

	@CommandLine.Option(names = ['-s', '--schema'], arity = '1', description = 'Restrict to schema.')
	String schema

	@CommandLine.Option(names = ['-d', '--catalog'], arity = '1', description = 'Restrict to catalog.')
	String catalog

	@CommandLine.Option(names = ['-i', '--includes'], arity = '1..*', description = 'Pattern for tables to be included.')
	Collection<String> includes

	@CommandLine.Option(names = ['-e', '--excludes'], arity = '1..*', description = 'Pattern for tables to be excluded.')
	Collection<String> excludes

	@CommandLine.Option(names = ['-t', '--table-types'], arity = '1..*', description = 'Table types to be displayed.')
	Collection<String> tableTypes

	public static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build()
	public static final ObjectMapper YAML_MAPPER = YAMLMapper.builder().build()


	void run() {

		DbMetadata dbMetadata = DbMetadataParser.parse(jdbcUrl, userName, password, catalog, schema, tableTypes)

		if (catalog) {
			dbMetadata.tables.removeIf { it.catalog != catalog }
			dbMetadata.catalogs.remove(catalog)
		}
		if (schema) {
			dbMetadata.tables.removeIf { it.schema != schema }
			dbMetadata.schemas.removeIf { it != schema }
		}
		if (includes) {
			dbMetadata.tables.removeIf { table -> !includes.any { table.name =~ it } }
		}
		if (excludes) {
			dbMetadata.tables.removeIf { table -> excludes.any { table.name =~ it } }
		}

		//YAML_MAPPER.writeValue(System.out, dbMetadata)

		DbMetadataRenderer.render(System.out, dbMetadata, tableTypes)
	}

	static class VersionProvider implements CommandLine.IVersionProvider {
		@Override
		String[] getVersion() throws Exception {
			return new String[]{
					"       Java version: ${System.getProperty('java.version')}",
					"     Groovy Version: ${GroovySystem.version}",
					"Application version: ${Class.forName(FilenameUtils.getBaseName(this.class.protectionDomain.codeSource.location.getFile())).getAnnotation(CommandLine.Command).version()[0]}"
			}
		}
	}
}