package domain;

import java.util.Objects;

import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import com.fasterxml.jackson.annotation.JsonIgnore;

import groovy.lang.Closure;
import lombok.Data;

@Data
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
