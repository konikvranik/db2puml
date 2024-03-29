package net.suteren.db2puml.domain;

import lombok.Data;

@Data
public abstract class AbstractDbObjectInfo {

	private String catalog;
	private String schema;
	private String name;
}
