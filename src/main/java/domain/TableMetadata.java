package domain;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Data;

@Data
public class TableMetadata extends AbstractDbObjectMetadata {
	private Collection<ColumnMetadata> columns = new ArrayList<ColumnMetadata>();
	private Collection<PkMetadata> primaryKeys = new ArrayList<PkMetadata>();
	private Collection<FkMetadata> foreignKeys = new ArrayList<FkMetadata>();
	private Collection<FkMetadata> exportedKeys = new ArrayList<FkMetadata>();
	private Collection<IndexMetadata> indexes = new ArrayList<IndexMetadata>();
	private TypeInfo typeInfo = new TypeInfo();
	private String selfReferencingColumn;
	private String generator;

	public static class TypeInfo extends AbstractDbObjectInfo {
	}
}
