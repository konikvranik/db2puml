package net.suteren.db2puml.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.ToString

@ToString(excludes = 'parent')
class PkMetadata extends AbstractDbObjectInfo {
	String table
	String column
	int keySeq
	@JsonIgnore
	TableMetadata parent
}