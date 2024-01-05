package domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

@Data
public class DbMetadata {
	private final Collection<String> tableTypes = new ArrayList<String>();
	private final Map<String, Collection<String>> catalogs = new LinkedHashMap<String, Collection<String>>();
	private final Collection<String> schemas = new ArrayList<String>();
	private final Collection<TableMetadata> tables = new ArrayList<TableMetadata>();
}
