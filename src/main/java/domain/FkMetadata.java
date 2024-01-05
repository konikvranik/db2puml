package domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Data
public class FkMetadata extends AbstractDbObjectMetadata {

	private String table;
	private String column;
	private int keySeq;
	private short updateRule;
	private short deleteRule;
	private short deferrability;
	private ReferenceInfo reference = new ReferenceInfo();
	@JsonIgnore private TableMetadata parent;

	@Data
	public static class ReferenceInfo extends AbstractDbObjectInfo {
		private String table;
		private String column;
	}
}
