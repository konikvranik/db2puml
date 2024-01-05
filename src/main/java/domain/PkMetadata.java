package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class PkMetadata extends AbstractDbObjectInfo {
	private String table;
	private String column;
	private int keySeq;
	@JsonIgnore private TableMetadata parent;
}
