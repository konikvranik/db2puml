package net.suteren.db2puml.domain

import groovy.transform.ToString

@ToString
class TableMetadata extends AbstractDbObjectMetadata {
	Collection<ColumnMetadata> columns = []
	Collection<PkMetadata> primaryKeys = []
	Collection<FkMetadata> foreignKeys = []
	Collection<FkMetadata> exportedKeys = []
	Collection<IndexMetadata> indexes = []
	TableMetadata.TypeInfo typeInfo = new TableMetadata.TypeInfo()
	String selfReferencingColumn
	String generator

	@ToString
	class TypeInfo extends AbstractDbObjectInfo {}
}