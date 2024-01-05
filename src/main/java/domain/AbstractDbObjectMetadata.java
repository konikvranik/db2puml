package domain;

import lombok.Data;

@Data
public abstract class AbstractDbObjectMetadata extends AbstractDbObjectInfo {
	private String type;
	private String remarks;
}
