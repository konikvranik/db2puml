package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class IndexMetadata extends AbstractDbObjectInfo {
	private String table;
	private String column;
	private boolean nonUnique;
	private String indexQualifier;
	private short type;
	private short ordinalPosition;
	private String ascOrDesc;
	private long cardinality;
	private long pages;
	private String filterCondition;
	@JsonIgnore private TableMetadata parent;
}
