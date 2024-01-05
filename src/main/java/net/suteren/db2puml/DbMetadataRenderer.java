package net.suteren.db2puml;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import net.suteren.db2puml.domain.AbstractDbObjectInfo;
import net.suteren.db2puml.domain.AbstractDbObjectMetadata;
import net.suteren.db2puml.domain.ColumnMetadata;
import net.suteren.db2puml.domain.DbMetadata;
import net.suteren.db2puml.domain.FkMetadata;
import net.suteren.db2puml.domain.IndexMetadata;
import net.suteren.db2puml.domain.PkMetadata;
import net.suteren.db2puml.domain.TableMetadata;

class DbMetadataRenderer {

	static void render(PrintStream out, DbMetadata metadata, Collection<String> tableTypes) {
		out.println("@startuml");
		Optional.ofNullable(DbMetadataRenderer.class.getResourceAsStream("/common.puml"))
			.map(s -> {
				try {
					return IOUtils.toString(s, StandardCharsets.UTF_8);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			})
			.ifPresent(out::println);

		metadata.getSchemas()
			.forEach(schema -> {

				out.printf("package %s {%n", schema);

				List<TableMetadata> tables = metadata.getTables()
					.stream().sorted(Comparator.comparing(AbstractDbObjectMetadata::getType))

					.filter(it -> CollectionUtils.isEmpty(tableTypes) || tableTypes.contains(it.getType()))
					.toList();
				tables.forEach(t -> renderTable(out, t, schema));
				tables.forEach(t -> t.getForeignKeys().forEach(f -> renderImportedKey(out, f, schema)));
				out.println("}");
			});

		out.println("@enduml");
	}

	static void renderTable(PrintStream out, TableMetadata table, String schema) {

		if (StringUtils.isNotEmpty(schema) && !Objects.equals(schema, table.getSchema())) {
			return;
		}

		out.print("  ");
		if (table.getType().endsWith("TABLE")) {
			out.print("table");
		} else if (table.getType().endsWith("VIEW")) {
			out.print("view");
		} else if (table.getType().endsWith("INDEX")) {
			out.print("index");
		} else if (table.getType().endsWith("SEQUENCE")) {
			out.print("sequence");
		} else {
			return;
		}
		out.printf("(%s) %s{%n", table.getName(),table.getType().startsWith("SYSTEM") ? "<<system>> #Pink " : "");
		table.getColumns().forEach(it -> renderColumn(out, it));
		table.getPrimaryKeys().stream()
			.collect(Collectors.groupingBy(AbstractDbObjectInfo::getName, Collectors.toList())).values()
			.forEach(pk -> renderPrimaryKey(out, pk));
		table.getForeignKeys().stream()
			.collect(Collectors.groupingBy(AbstractDbObjectInfo::getName, Collectors.toList())).values()
			.forEach(fk -> renderFk(out, fk));
		table.getIndexes().stream()
			.collect(Collectors.groupingBy(AbstractDbObjectInfo::getName, Collectors.toList())).values()
			.forEach(it -> renderIndex(out, it));
		out.println("  }");
	}

	static void renderImportedKey(PrintStream out, FkMetadata fk, String schema) {
		if (StringUtils.isNotEmpty(schema) && !schema.equals(fk.getSchema())) {
			return;
		}
		out.print("  ");
		out.print(fk.getTable());
		out.print("::");
		out.print(fk.getColumn());
		out.print(" \"");
		out.print(fk.getColumn());
		out.print("\" --> \"");
		out.print(fk.getReference().getColumn());
		out.print("\" ");
		out.print(fk.getReference().getTable());
		out.print("::");
		out.print(fk.getReference().getColumn());
		out.print(": ");
		out.println(fk.getName());
	}

	static void renderColumn(PrintStream out, ColumnMetadata columnMetadata) {
		out.print("    ");
		if (columnMetadata.isFk()) {
			if (columnMetadata.isPk()) {
				out.print("combined_key(");
			} else {
				out.print("foreign_key(");
			}
		} else if (columnMetadata.isPk()) {
			out.print("primary_key(");
		} else {
			out.print("column(");
		}
		out.print(columnMetadata.getName());
		out.print(",\"");
		out.print(pumlEscape(columnMetadata.getType()));
		if (!(Set.of("json", "jsonb", "text", "clob", "blob", "lob").contains(columnMetadata.getType())) && columnMetadata.getSize() > 0) {
			out.print("(");
			out.print(columnMetadata.getSize());
			if (columnMetadata.getDecimalDigits() != null && (columnMetadata.getDecimalDigits() != 0)) {
				out.print(",");
				out.print(columnMetadata.getDecimalDigits());
			}
			out.print(")");
		}
		out.print("\"");
		out.println(")");
	}

	static String pumlEscape(String s) {
		return s.replaceAll("\"", "\"\"");
	}

	static void renderFk(PrintStream out, List<FkMetadata> fkMetadataList) {
		out.print("    ");
		String columns = fkMetadataList.stream()
			.sorted(Comparator.comparing(FkMetadata::getKeySeq))
			.map(FkMetadata::getColumn)
			.distinct()
			.collect(Collectors.joining(","));
		String tables = fkMetadataList.stream()
			.sorted(Comparator.comparing(FkMetadata::getKeySeq))
			.map(FkMetadata::getReference)
			.map(FkMetadata.ReferenceInfo::getTable)
			.distinct()
			.collect(Collectors.joining(","));

		String foreignColumns = fkMetadataList.stream()
			.sorted(Comparator.comparing(FkMetadata::getKeySeq))
			.map(FkMetadata::getReference)
			.map(FkMetadata.ReferenceInfo::getColumn)
			.distinct()
			.collect(Collectors.joining(","));
		out.printf("fk_constraint(\"%s\", \"%s\", \"%s\", \"%s\")%n", fkMetadataList.stream().findFirst().map(AbstractDbObjectInfo::getName).orElse(null),
			columns, tables, foreignColumns);
	}

	static void renderIndex(PrintStream out, List<IndexMetadata> indexMetadata) {
		if (indexMetadata.stream()
			.map(IndexMetadata::getParent)
			.map(TableMetadata::getPrimaryKeys)
			.flatMap(Collection::stream)
			.anyMatch(pk -> indexMetadata.stream()
				.map(AbstractDbObjectInfo::getName)
				.anyMatch(i -> Objects.equals(i, pk.getName())))) {
			return;
		}
		out.print("    ");
		String columns = indexMetadata.stream()
			.sorted(Comparator.comparing(IndexMetadata::getOrdinalPosition))
			.map(IndexMetadata::getColumn)
			.distinct()
			.collect(Collectors.joining(","));
		if (indexMetadata.stream().noneMatch(IndexMetadata::isNonUnique)) {
			out.printf("unique(\"%s\", \"%s\")%n", indexMetadata.stream().findFirst().map(AbstractDbObjectInfo::getName).orElse(null),columns);
		} else {
			out.printf("index_column(\"%s\", \"%s\")%n", indexMetadata.stream().findFirst().map(AbstractDbObjectInfo::getName).orElse(null), columns);
		}

	}

	static void renderPrimaryKey(PrintStream out, List<PkMetadata> pkMetadata) {
		out.print("    ");
		String columns = pkMetadata.stream()
			.sorted(Comparator.comparing(PkMetadata::getKeySeq))
			.map(PkMetadata::getColumn)
			.distinct()
			.collect(Collectors.joining(","));
		out.printf("pk_index(\"%s\",\"%s\")%n", pkMetadata.stream().findFirst().map(AbstractDbObjectInfo::getName).orElse(null), columns);
	}
}