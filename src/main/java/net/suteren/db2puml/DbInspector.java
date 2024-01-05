package net.suteren.db2puml;

import java.sql.SQLException;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import groovy.util.logging.Slf4j;
import net.suteren.db2puml.domain.DbMetadata;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(name = "db_inspector.groovy", description = "Render DB structure as plantuml", mixinStandardHelpOptions = true, version = "1.0.0", versionProvider = DbInspectorVersionProvider.class)
public class DbInspector implements Callable<Integer> {

	@CommandLine.Option(names = { "-c", "--stdout" }, description = "Output to STDOUT.") private boolean stdout = false;
	@CommandLine.Option(names = { "-j", "--jdbc-url" }, arity = "1", required = true, description = "JDBC connection string.") private String jdbcUrl;
	@CommandLine.Option(names = { "-u", "--user" }, arity = "1", description = "Database user name.") private String userName;
	@CommandLine.Option(names = { "-p", "--password" }, arity = "1", description = "Database user password.") private String password;
	@CommandLine.Option(names = { "-s", "--schema" }, arity = "1", description = "Restrict to schema.") private String schema;
	@CommandLine.Option(names = { "-d", "--catalog" }, arity = "1", description = "Restrict to catalog.") private String catalog;
	@CommandLine.Option(names = { "-i", "--includes" }, arity = "1..*", description = "Pattern for tables to be included.") private Collection<String> includes;
	@CommandLine.Option(names = { "-e", "--excludes" }, arity = "1..*", description = "Pattern for tables to be excluded.") private Collection<String> excludes;
	@CommandLine.Option(names = { "-t", "--table-types" }, arity = "1..*", description = "Table types to be displayed.") private Collection<String> tableTypes;
	public static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();
	public static final ObjectMapper YAML_MAPPER = YAMLMapper.builder().build();

	public Integer call() throws SQLException {

		DbMetadata dbMetadata = DbMetadataParser.parse(jdbcUrl, userName, password, catalog, schema, tableTypes);

		if (StringGroovyMethods.asBoolean(catalog)) {
			dbMetadata.getTables().removeIf(it -> !it.getCatalog().equals(catalog));
			dbMetadata.getCatalogs().remove(catalog);
		}

		if (StringGroovyMethods.asBoolean(schema)) {
			dbMetadata.getTables().removeIf(it -> !it.getSchema().equals(schema));
			dbMetadata.getSchemas().removeIf(it -> !it.equals(schema));
		}

		if (DefaultGroovyMethods.asBoolean(includes)) {
			dbMetadata.getTables().removeIf(table -> includes.stream()
				.noneMatch(include -> Pattern.matches(include, table.getName())));
		}

		if (DefaultGroovyMethods.asBoolean(excludes)) {
			dbMetadata.getTables().removeIf(table -> excludes.stream()
				.anyMatch(exclude -> Pattern.matches(exclude, table.getName())));
		}

		//YAML_MAPPER.writeValue(System.out, dbMetadata)

		DbMetadataRenderer.render(System.out, dbMetadata, tableTypes);

		return 0;
	}

	public static void main(String[] args) {
		System.exit(new CommandLine(new DbInspector()).execute(args));
	}
}
