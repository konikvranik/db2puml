package net.suteren.db2puml.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.ToString

@ToString(excludes = 'parent')
class IndexMetadata extends AbstractDbObjectInfo {
	String table
	String column
	boolean nonUnique
	String indexQualifier
	short type
	short ordinalPosition
	String ascOrDesc
	long cardinality
	long pages
	String filterCondition
	@JsonIgnore
	TableMetadata parent
}