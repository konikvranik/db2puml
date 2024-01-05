package net.suteren.db2puml.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true) @Data
public abstract class AbstractDbObjectMetadata extends AbstractDbObjectInfo {
	private String type;
	private String remarks;
}
