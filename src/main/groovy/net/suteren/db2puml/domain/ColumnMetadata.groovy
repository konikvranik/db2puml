package net.suteren.db2puml.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.ToString

@ToString(excludes = 'parent')
class ColumnMetadata extends AbstractDbObjectMetadata {
	String table
	int dataType
	int size
	Integer decimalDigits
	int radix
	int nullable
	String columnDefinition
	Integer charOctetLength
	int ordinalPosition
	Boolean isNullable
	ColumnMetadata.ScopeInfo scopeTable = new ColumnMetadata.ScopeInfo()
	short sourceDataType
	Boolean isAutoincrement
	Boolean isGeneratedColumn
	@JsonIgnore
	TableMetadata parent

	boolean isFk() {
		parent.foreignKeys?.any { it.column == name } ?: false
	}

	boolean isPk() {
		parent.primaryKeys?.any { it.column == name } ?: false
	}

	@ToString
	class ScopeInfo extends AbstractDbObjectInfo {}
}