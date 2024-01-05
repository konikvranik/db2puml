package net.suteren.db2puml.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true, exclude = "parent") @Data @ToString(exclude = "parent")
public class PkMetadata extends AbstractDbObjectInfo {
	private String table;
	private String column;
	private int keySeq;
	@JsonIgnore private TableMetadata parent;
}
