package net.suteren.db2puml

import groovy.util.logging.Slf4j
import net.suteren.db2puml.domain.ColumnMetadata
import net.suteren.db2puml.domain.DbMetadata
import net.suteren.db2puml.domain.FkMetadata
import net.suteren.db2puml.domain.IndexMetadata
import net.suteren.db2puml.domain.PkMetadata
import net.suteren.db2puml.domain.TableMetadata

import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.DriverManager
import java.sql.ResultSet

@Slf4j
class DbMetadataParser {

	static DbMetadata parse(url, user, password, catalog, schema, tableTypes) {

		Properties properties = new Properties();
		properties.setProperty("user", user);
		properties.setProperty("password", password);
		Connection connection = DriverManager.getConnection(url, properties)
		DatabaseMetaData databaseMetaData = connection.getMetaData()
		DbMetadata dbMetadata = new DbMetadata()

		updateTableTypes(dbMetadata, databaseMetaData)
		log.info("Supported table types: ${dbMetadata.tableTypes.join(', ')}")
		updateCatalogs(dbMetadata, databaseMetaData)
		log.info("Catalogs: ${dbMetadata.catalogs}")
		updateSchemas(dbMetadata, databaseMetaData)
		log.info("Schemas: ${dbMetadata.schemas}")
		updateTablesMetadata(dbMetadata, databaseMetaData, catalog, schema, tableTypes as String[])

		log.debug(dbMetadata.toString())

		return dbMetadata
	}

	private static void updateCatalogs(DbMetadata dbMetadata, DatabaseMetaData databaseMetaData) {
		try (ResultSet resultSet = databaseMetaData.getCatalogs()) {
			while (resultSet.next()) {
				String catalog = resultSet.getString('TABLE_CAT')
				dbMetadata.catalogs.put(catalog, getSchemas(dbMetadata, databaseMetaData, catalog))
			}
		}
	}

	private static void updateSchemas(DbMetadata dbMetadata, DatabaseMetaData databaseMetaData) {
		try (ResultSet resultSet = databaseMetaData.getSchemas()) {
			while (resultSet.next()) {
				dbMetadata.schemas.add(resultSet.getString('TABLE_SCHEM'))
			}
		}
	}

	private static void getSchemas(DbMetadata dbMetadata, DatabaseMetaData databaseMetaData, String catalog) {
		Collection<String> schemas = []
		try (ResultSet resultSet = databaseMetaData.getSchemas(catalog, null)) {
			while (resultSet.next()) {
				schemas.add(resultSet.getString('TABLE_SCHEM'))
			}
		}
	}

	private static void updateTablesMetadata(DbMetadata dbMetadata, DatabaseMetaData databaseMetaData, String catalog, String schema, String[] tableTypes) {
		if (!tableTypes) {
			tableTypes = dbMetadata.tableTypes as String[]
		}
		try (ResultSet resultSet = databaseMetaData.getTables(catalog, schema, null, tableTypes)) {
			while (resultSet.next()) {
				TableMetadata table = new TableMetadata()
				table.name = resultSet.getString('TABLE_NAME')
				table.remarks = resultSet.getString('REMARKS')
				table.catalog = resultSet.getString('TABLE_CAT')
				table.schema = resultSet.getString('TABLE_SCHEM')
				table.type = resultSet.getString('TABLE_TYPE')
				if (resultSet.getMetaData().getColumnCount() > 5) {
					table.typeInfo.schema = resultSet.getString('TYPE_SCHEM')
					table.typeInfo.catalog = resultSet.getString('TYPE_CAT')
					table.typeInfo.name = resultSet.getString('TYPE_NAME')
					table.selfReferencingColumn = resultSet.getString('SELF_REFERENCING_COL_NAME')
					table.generator = resultSet.getString('REF_GENERATION')
				}
				if (!(table.type in ['SYNONYM', 'SEQUENCE'])) {
					updateColumnMetadata(table, databaseMetaData)
					updatePkMetadata(table, databaseMetaData)
					updateImportedKeysMetadata(table, databaseMetaData)
					updateExportedKeysMetadata(table, databaseMetaData)
					updateIndexMetadata(table, databaseMetaData)
				}
				dbMetadata.tables.add(table)
			}
		}
	}

	private static void updateColumnMetadata(TableMetadata tableMetadata, DatabaseMetaData databaseMetaData) {
		try (ResultSet resultSet = databaseMetaData.getColumns(tableMetadata.catalog, tableMetadata.schema, tableMetadata.name, null)) {
			while (resultSet.next()) {
				ColumnMetadata column = new ColumnMetadata()
				column.parent = tableMetadata
				column.catalog = resultSet.getString('TABLE_CAT')
				column.schema = resultSet.getString('TABLE_SCHEM')
				column.table = resultSet.getString('TABLE_NAME')
				column.name = resultSet.getString('COLUMN_NAME')
				column.dataType = resultSet.getInt('DATA_TYPE')
				column.type = resultSet.getString('TYPE_NAME')
				column.size = resultSet.getInt('COLUMN_SIZE')
				column.decimalDigits = resultSet.getInt('DECIMAL_DIGITS')
				column.radix = resultSet.getInt('NUM_PREC_RADIX')
				column.nullable = resultSet.getInt('NULLABLE')
				column.remarks = resultSet.getString('REMARKS')
				column.columnDefinition = resultSet.getString('COLUMN_DEF')
				column.charOctetLength = resultSet.getInt('CHAR_OCTET_LENGTH')
				column.ordinalPosition = resultSet.getInt('ORDINAL_POSITION')
				column.isNullable = parseBoolean(resultSet.getString('IS_NULLABLE'))
				column.scopeTable.catalog = resultSet.getString('SCOPE_CATALOG')
				column.scopeTable.schema = resultSet.getString('SCOPE_SCHEMA')
				column.scopeTable.name = resultSet.getString('SCOPE_TABLE')
				column.sourceDataType = resultSet.getByte('SOURCE_DATA_TYPE')
				column.isAutoincrement = parseBoolean(resultSet.getString('IS_AUTOINCREMENT'))
				column.isGeneratedColumn = parseBoolean(resultSet.getString('IS_GENERATEDCOLUMN'))

				tableMetadata.columns.add(column)
			}
		}
	}

	private static void updatePkMetadata(TableMetadata tableMetadata, DatabaseMetaData databaseMetaData) {
		try (ResultSet resultSet = databaseMetaData.getPrimaryKeys(tableMetadata.catalog, tableMetadata.schema, tableMetadata.name)) {
			while (resultSet.next()) {
				PkMetadata primaryKey = new PkMetadata()
				primaryKey.parent = tableMetadata
				primaryKey.catalog = resultSet.getString('TABLE_CAT')
				primaryKey.schema = resultSet.getString('TABLE_SCHEM')
				primaryKey.table = resultSet.getString('TABLE_NAME')
				primaryKey.name = resultSet.getString('PK_NAME')
				primaryKey.keySeq = resultSet.getInt('KEY_SEQ')
				primaryKey.column = resultSet.getString('COLUMN_NAME')

				tableMetadata.primaryKeys.add(primaryKey)
			}
		}
	}

	private static void updateImportedKeysMetadata(TableMetadata tableMetadata, DatabaseMetaData databaseMetaData) {
		try (ResultSet resultSet = databaseMetaData.getImportedKeys(tableMetadata.catalog, tableMetadata.schema, tableMetadata.name)) {
			while (resultSet.next()) {
				FkMetadata foreignKey = new FkMetadata()
				foreignKey.parent = tableMetadata
				foreignKey.catalog = resultSet.getString('FKTABLE_CAT')
				foreignKey.schema = resultSet.getString('FKTABLE_SCHEM')
				foreignKey.table = resultSet.getString('FKTABLE_NAME')
				foreignKey.column = resultSet.getString('FKCOLUMN_NAME')
				foreignKey.name = resultSet.getString('FK_NAME')
				foreignKey.keySeq = resultSet.getInt('KEY_SEQ')
				foreignKey.updateRule = resultSet.getByte('UPDATE_RULE')
				foreignKey.deleteRule = resultSet.getByte('DELETE_RULE')
				foreignKey.deferrability = resultSet.getByte('DEFERRABILITY')
				foreignKey.reference.catalog = resultSet.getInt('PKTABLE_CAT')
				foreignKey.reference.schema = resultSet.getString('PKTABLE_SCHEM')
				foreignKey.reference.table = resultSet.getString('PKTABLE_NAME')
				foreignKey.reference.column = resultSet.getString('PKCOLUMN_NAME')
				foreignKey.reference.name = resultSet.getString('PK_NAME')

				tableMetadata.foreignKeys.add(foreignKey)
			}
		}
	}

	private static void updateExportedKeysMetadata(TableMetadata tableMetadata, DatabaseMetaData databaseMetaData) {
		try (ResultSet resultSet = databaseMetaData.getExportedKeys(tableMetadata.catalog, tableMetadata.schema, tableMetadata.name)) {
			while (resultSet.next()) {
				FkMetadata foreignKey = new FkMetadata()
				foreignKey.parent = tableMetadata
				foreignKey.catalog = resultSet.getString('FKTABLE_CAT')
				foreignKey.schema = resultSet.getString('FKTABLE_SCHEM')
				foreignKey.table = resultSet.getString('FKTABLE_NAME')
				foreignKey.column = resultSet.getString('FKCOLUMN_NAME')
				foreignKey.name = resultSet.getString('FK_NAME')
				foreignKey.keySeq = resultSet.getInt('KEY_SEQ')
				foreignKey.updateRule = resultSet.getByte('UPDATE_RULE')
				foreignKey.deleteRule = resultSet.getByte('DELETE_RULE')
				foreignKey.deferrability = resultSet.getByte('DEFERRABILITY')
				foreignKey.reference.catalog = resultSet.getInt('PKTABLE_CAT')
				foreignKey.reference.schema = resultSet.getString('PKTABLE_SCHEM')
				foreignKey.reference.table = resultSet.getString('PKTABLE_NAME')
				foreignKey.reference.column = resultSet.getString('PKCOLUMN_NAME')
				foreignKey.reference.name = resultSet.getString('PK_NAME')

				tableMetadata.exportedKeys.add(foreignKey)
			}
		}
	}

	private static void updateIndexMetadata(TableMetadata tableMetadata, DatabaseMetaData databaseMetaData) {
		try (ResultSet resultSet = databaseMetaData.getIndexInfo(tableMetadata.catalog, tableMetadata.schema, tableMetadata.name, false, true)) {
			while (resultSet.next()) {
				IndexMetadata index = new IndexMetadata()
				tableMetadata.indexes.add(index)
				index.parent = tableMetadata
				index.catalog = resultSet.getString('TABLE_CAT')
				index.schema = resultSet.getString('TABLE_SCHEM')
				index.table = resultSet.getString('TABLE_NAME')
				index.name = resultSet.getString('INDEX_NAME')
				index.column = resultSet.getString('COLUMN_NAME')
				index.nonUnique = resultSet.getBoolean('NON_UNIQUE')
				index.indexQualifier = resultSet.getString('INDEX_QUALIFIER')
				index.type = resultSet.getByte('TYPE')
				index.ordinalPosition = resultSet.getByte('ORDINAL_POSITION')
				index.ascOrDesc = resultSet.getString('ASC_OR_DESC')
				index.cardinality = resultSet.getLong('CARDINALITY')
				index.pages = resultSet.getLong('PAGES')
				index.filterCondition = resultSet.getString('FILTER_CONDITION')
			}
		}
	}


	private static boolean parseBoolean(String string) {
		Boolean.parseBoolean(string) || "yes".equalsIgnoreCase(string);
	}

	private static void updateTableTypes(DbMetadata dbMetadata, DatabaseMetaData databaseMetaData) {
		try (ResultSet resultSet = databaseMetaData.getTableTypes()) {
			while (resultSet.next()) {
				String type = resultSet.getString(1)
				dbMetadata.tableTypes.add(type)
			}
		}
	}

}