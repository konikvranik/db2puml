package net.suteren.db2puml;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import net.suteren.db2puml.domain.ColumnMetadata;
import net.suteren.db2puml.domain.DbMetadata;
import net.suteren.db2puml.domain.FkMetadata;
import net.suteren.db2puml.domain.IndexMetadata;
import net.suteren.db2puml.domain.PkMetadata;
import net.suteren.db2puml.domain.TableMetadata;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DbMetadataParser {
	public static DbMetadata parse(String url, String user, String password, String catalog, String schema, Collection<String> tableTypes) throws SQLException {

		Properties properties = new Properties();
		properties.setProperty("user", user);
		properties.setProperty("password", password);
		Connection connection = DriverManager.getConnection(url, properties);
		DatabaseMetaData databaseMetaData = connection.getMetaData();
		final DbMetadata dbMetadata = new DbMetadata();

		updateTableTypes(dbMetadata, databaseMetaData);
		log.info("Supported table types: " + DefaultGroovyMethods.join(dbMetadata.getTableTypes(), ", "));
		updateCatalogs(dbMetadata, databaseMetaData);
		log.info("Catalogs: " + String.valueOf(dbMetadata.getCatalogs()));
		updateSchemas(dbMetadata, databaseMetaData);
		log.info("Schemas: " + String.valueOf(dbMetadata.getSchemas()));
		updateTablesMetadata(dbMetadata, databaseMetaData, catalog, schema, DefaultGroovyMethods.asType(tableTypes, String[].class));

		log.debug(dbMetadata.toString());

		return dbMetadata;
	}

	private static void updateCatalogs(DbMetadata dbMetadata, DatabaseMetaData databaseMetaData) throws SQLException {
		try (ResultSet resultSet = databaseMetaData.getCatalogs()) {
			while (resultSet.next()) {
				String catalog = resultSet.getString("TABLE_CAT");
				dbMetadata.getCatalogs().put(catalog, getSchemas(dbMetadata, databaseMetaData, catalog));
			}

		}

	}

	private static void updateSchemas(DbMetadata dbMetadata, DatabaseMetaData databaseMetaData) throws SQLException {
		try (ResultSet resultSet = databaseMetaData.getSchemas()) {
			while (resultSet.next()) {
				dbMetadata.getSchemas().add(resultSet.getString("TABLE_SCHEM"));
			}
		}
	}

	private static Collection<String> getSchemas(DbMetadata dbMetadata, DatabaseMetaData databaseMetaData, String catalog) throws SQLException {
		Collection<String> schemas = new ArrayList<String>();
		try (ResultSet resultSet = databaseMetaData.getSchemas(catalog, null)) {
			while (resultSet.next()) {
				schemas.add(resultSet.getString("TABLE_SCHEM"));
			}
		}
		return schemas;
	}

	private static void updateTablesMetadata(DbMetadata dbMetadata, DatabaseMetaData databaseMetaData, String catalog, String schema, String[] tableTypes)
		throws SQLException {
		if (!DefaultGroovyMethods.asBoolean(tableTypes)) {
			tableTypes = DefaultGroovyMethods.asType(dbMetadata.getTableTypes(), String[].class);
		}

		try (ResultSet resultSet = databaseMetaData.getTables(catalog, schema, null, tableTypes)) {
			while (resultSet.next()) {
				TableMetadata table = new TableMetadata();
				table.setName(resultSet.getString("TABLE_NAME"));
				table.setRemarks(resultSet.getString("REMARKS"));
				table.setCatalog(resultSet.getString("TABLE_CAT"));
				table.setSchema(resultSet.getString("TABLE_SCHEM"));
				table.setType(resultSet.getString("TABLE_TYPE"));
				if (resultSet.getMetaData().getColumnCount() > 5) {
					table.getTypeInfo().setSchema(resultSet.getString("TYPE_SCHEM"));
					table.getTypeInfo().setCatalog(resultSet.getString("TYPE_CAT"));
					table.getTypeInfo().setName(resultSet.getString("TYPE_NAME"));
					table.setSelfReferencingColumn(resultSet.getString("SELF_REFERENCING_COL_NAME"));
					table.setGenerator(resultSet.getString("REF_GENERATION"));
				}

				if (!(new ArrayList<String>(Arrays.asList("SYNONYM", "SEQUENCE"))).contains(table.getType())) {
					updateColumnMetadata(table, databaseMetaData);
					updatePkMetadata(table, databaseMetaData);
					updateImportedKeysMetadata(table, databaseMetaData);
					updateExportedKeysMetadata(table, databaseMetaData);
					updateIndexMetadata(table, databaseMetaData);
				}

				dbMetadata.getTables().add(table);
			}
		}
	}

	private static void updateColumnMetadata(TableMetadata tableMetadata, DatabaseMetaData databaseMetaData) throws SQLException {
		try (ResultSet resultSet = databaseMetaData.getColumns(tableMetadata.getCatalog(), tableMetadata.getSchema(), tableMetadata.getName(), null)) {
			while (resultSet.next()) {
				ColumnMetadata column = new ColumnMetadata();
				column.setParent(tableMetadata);
				column.setCatalog(resultSet.getString("TABLE_CAT"));
				column.setSchema(resultSet.getString("TABLE_SCHEM"));
				column.setTable(resultSet.getString("TABLE_NAME"));
				column.setName(resultSet.getString("COLUMN_NAME"));
				column.setDataType(resultSet.getInt("DATA_TYPE"));
				column.setType(resultSet.getString("TYPE_NAME"));
				column.setSize(resultSet.getInt("COLUMN_SIZE"));
				column.setDecimalDigits(resultSet.getInt("DECIMAL_DIGITS"));
				column.setRadix(resultSet.getInt("NUM_PREC_RADIX"));
				column.setNullable(resultSet.getInt("NULLABLE"));
				column.setRemarks(resultSet.getString("REMARKS"));
				column.setColumnDefinition(resultSet.getString("COLUMN_DEF"));
				column.setCharOctetLength(resultSet.getInt("CHAR_OCTET_LENGTH"));
				column.setOrdinalPosition(resultSet.getInt("ORDINAL_POSITION"));
				column.setIsNullable(parseBoolean(resultSet.getString("IS_NULLABLE")));
				column.getScopeTable().setCatalog(resultSet.getString("SCOPE_CATALOG"));
				column.getScopeTable().setSchema(resultSet.getString("SCOPE_SCHEMA"));
				column.getScopeTable().setName(resultSet.getString("SCOPE_TABLE"));
				column.setSourceDataType(resultSet.getByte("SOURCE_DATA_TYPE"));
				column.setIsAutoincrement(parseBoolean(resultSet.getString("IS_AUTOINCREMENT")));
				column.setIsGeneratedColumn(parseBoolean(resultSet.getString("IS_GENERATEDCOLUMN")));

				tableMetadata.getColumns().add(column);
			}
		}
	}

	private static void updatePkMetadata(TableMetadata tableMetadata, DatabaseMetaData databaseMetaData) throws SQLException {
		try (ResultSet resultSet = databaseMetaData.getPrimaryKeys(tableMetadata.getCatalog(), tableMetadata.getSchema(), tableMetadata.getName())) {
			while (resultSet.next()) {
				PkMetadata primaryKey = new PkMetadata();
				primaryKey.setParent(tableMetadata);
				primaryKey.setCatalog(resultSet.getString("TABLE_CAT"));
				primaryKey.setSchema(resultSet.getString("TABLE_SCHEM"));
				primaryKey.setTable(resultSet.getString("TABLE_NAME"));
				primaryKey.setName(resultSet.getString("PK_NAME"));
				primaryKey.setKeySeq(resultSet.getInt("KEY_SEQ"));
				primaryKey.setColumn(resultSet.getString("COLUMN_NAME"));

				tableMetadata.getPrimaryKeys().add(primaryKey);
			}
		}
	}

	private static void updateImportedKeysMetadata(TableMetadata tableMetadata, DatabaseMetaData databaseMetaData) throws SQLException {
		try (ResultSet resultSet = databaseMetaData.getImportedKeys(tableMetadata.getCatalog(), tableMetadata.getSchema(), tableMetadata.getName())) {
			while (resultSet.next()) {
				FkMetadata foreignKey = new FkMetadata();
				foreignKey.setParent(tableMetadata);
				foreignKey.setCatalog(resultSet.getString("FKTABLE_CAT"));
				foreignKey.setSchema(resultSet.getString("FKTABLE_SCHEM"));
				foreignKey.setTable(resultSet.getString("FKTABLE_NAME"));
				foreignKey.setColumn(resultSet.getString("FKCOLUMN_NAME"));
				foreignKey.setName(resultSet.getString("FK_NAME"));
				foreignKey.setKeySeq(resultSet.getInt("KEY_SEQ"));
				foreignKey.setUpdateRule(resultSet.getByte("UPDATE_RULE"));
				foreignKey.setDeleteRule(resultSet.getByte("DELETE_RULE"));
				foreignKey.setDeferrability(resultSet.getByte("DEFERRABILITY"));
				foreignKey.getReference().setCatalog(resultSet.getString("PKTABLE_CAT"));
				foreignKey.getReference().setSchema(resultSet.getString("PKTABLE_SCHEM"));
				foreignKey.getReference().setTable(resultSet.getString("PKTABLE_NAME"));
				foreignKey.getReference().setColumn(resultSet.getString("PKCOLUMN_NAME"));
				foreignKey.getReference().setName(resultSet.getString("PK_NAME"));

				tableMetadata.getForeignKeys().add(foreignKey);
			}
		}
	}

	private static void updateExportedKeysMetadata(TableMetadata tableMetadata, DatabaseMetaData databaseMetaData) throws SQLException {
		try (ResultSet resultSet = databaseMetaData.getExportedKeys(tableMetadata.getCatalog(), tableMetadata.getSchema(), tableMetadata.getName())) {
			while (resultSet.next()) {
				FkMetadata foreignKey = new FkMetadata();
				foreignKey.setParent(tableMetadata);
				foreignKey.setCatalog(resultSet.getString("FKTABLE_CAT"));
				foreignKey.setSchema(resultSet.getString("FKTABLE_SCHEM"));
				foreignKey.setTable(resultSet.getString("FKTABLE_NAME"));
				foreignKey.setColumn(resultSet.getString("FKCOLUMN_NAME"));
				foreignKey.setName(resultSet.getString("FK_NAME"));
				foreignKey.setKeySeq(resultSet.getInt("KEY_SEQ"));
				foreignKey.setUpdateRule(resultSet.getByte("UPDATE_RULE"));
				foreignKey.setDeleteRule(resultSet.getByte("DELETE_RULE"));
				foreignKey.setDeferrability(resultSet.getByte("DEFERRABILITY"));
				foreignKey.getReference().setCatalog(resultSet.getString("PKTABLE_CAT"));
				foreignKey.getReference().setSchema(resultSet.getString("PKTABLE_SCHEM"));
				foreignKey.getReference().setTable(resultSet.getString("PKTABLE_NAME"));
				foreignKey.getReference().setColumn(resultSet.getString("PKCOLUMN_NAME"));
				foreignKey.getReference().setName(resultSet.getString("PK_NAME"));

				tableMetadata.getExportedKeys().add(foreignKey);
			}
		}
	}

	private static void updateIndexMetadata(TableMetadata tableMetadata, DatabaseMetaData databaseMetaData) throws SQLException {
		try (ResultSet resultSet = databaseMetaData.getIndexInfo(tableMetadata.getCatalog(), tableMetadata.getSchema(), tableMetadata.getName(), false, true)) {
			while (resultSet.next()) {
				IndexMetadata index = new IndexMetadata();
				tableMetadata.getIndexes().add(index);
				index.setParent(tableMetadata);
				index.setCatalog(resultSet.getString("TABLE_CAT"));
				index.setSchema(resultSet.getString("TABLE_SCHEM"));
				index.setTable(resultSet.getString("TABLE_NAME"));
				index.setName(resultSet.getString("INDEX_NAME"));
				index.setColumn(resultSet.getString("COLUMN_NAME"));
				index.setNonUnique(resultSet.getBoolean("NON_UNIQUE"));
				index.setIndexQualifier(resultSet.getString("INDEX_QUALIFIER"));
				index.setType(resultSet.getByte("TYPE"));
				index.setOrdinalPosition(resultSet.getByte("ORDINAL_POSITION"));
				index.setAscOrDesc(resultSet.getString("ASC_OR_DESC"));
				index.setCardinality(resultSet.getLong("CARDINALITY"));
				index.setPages(resultSet.getLong("PAGES"));
				index.setFilterCondition(resultSet.getString("FILTER_CONDITION"));
			}
		}
	}

	private static boolean parseBoolean(String string) {
		return Boolean.parseBoolean(string) || "yes".equalsIgnoreCase(string);
	}

	private static void updateTableTypes(DbMetadata dbMetadata, DatabaseMetaData databaseMetaData) throws SQLException {
		try (ResultSet resultSet = databaseMetaData.getTableTypes()) {
			while (resultSet.next()) {
				String type = resultSet.getString(1);
				dbMetadata.getTableTypes().add(type);
			}
		}
	}
}
