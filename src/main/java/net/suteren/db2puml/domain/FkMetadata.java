package net.suteren.db2puml.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true, exclude = "parent") @Data @ToString(exclude = "parent")
public class FkMetadata extends AbstractDbObjectMetadata {

	private String table;
	private String column;
	private int keySeq;
	private short updateRule;
	private short deleteRule;
	private short deferrability;
	private ReferenceInfo reference = new ReferenceInfo();
	@JsonIgnore private TableMetadata parent;

	@EqualsAndHashCode(callSuper = true) @Data
	public static class ReferenceInfo extends AbstractDbObjectInfo {
		private String table;
		private String column;
	}
}
