package net.suteren.db2puml.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.ToString

@ToString(excludes = 'parent')
class FkMetadata extends AbstractDbObjectMetadata {
	String table
	String column
	int keySeq
	short updateRule
	short deleteRule
	short deferrability
	FkMetadata.ReferenceInfo reference = new FkMetadata.ReferenceInfo()
	@JsonIgnore
	TableMetadata parent

	@ToString
	class ReferenceInfo extends AbstractDbObjectInfo {
		String table
		String column
	}
}