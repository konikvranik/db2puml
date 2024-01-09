# db2puml

This tool is deprecated.
Instead of it,
I recommend to use [SchemaCrawler](https://www.schemacrawler.com/) with [plantuml.groovy] script:

1. [Setup](https://www.schemacrawler.com/downloads.html) SchemaCrawler
1. Download [plantuml.groovy]
2. Call SchemaCrawler with plantuml.groovy script:
```shell
schemacrawler.sh --server *** --host *** --user *** --password *** --database *** --info-level maximum  --command script --script-language groovy --script plantuml.groovy
```

[plantuml.groovy]: https://gist.github.com/konikvranik/15ff54cb2c3b255ce86a689b8e2ba6c6