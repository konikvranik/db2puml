package net.suteren.db2puml.domain;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true, exclude = "parent") @Data @ToString(exclude = "parent")
public class ColumnMetadata extends AbstractDbObjectMetadata {
	public boolean isFk() {
		return parent.getForeignKeys().stream()
			.anyMatch(fk -> Objects.equals(fk.getName(), getName()));
	}

	public boolean isPk() {
		return parent.getPrimaryKeys().stream()
			.anyMatch(pk -> Objects.equals(pk.getName(), getName()));
	}

	private String table;
	private int dataType;
	private int size;
	private Integer decimalDigits;
	private int radix;
	private int nullable;
	private String columnDefinition;
	private Integer charOctetLength;
	private int ordinalPosition;
	private Boolean isNullable;
	private ScopeInfo scopeTable = new ScopeInfo();
	private short sourceDataType;
	private Boolean isAutoincrement;
	private Boolean isGeneratedColumn;
	@JsonIgnore private TableMetadata parent;

	public static class ScopeInfo extends AbstractDbObjectInfo {
	}
}
