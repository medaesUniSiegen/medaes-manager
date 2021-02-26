# Medaes-Manager

#### Beschreibung
Indizierungssoftware für Elasticsearch.
Es werden nach Programmstart automatisch, sofern notwendig, die benötigten Indizes erstellt & konfiguriert und anschließend alle Dokumente indiziert, die in einem definierten Verzeichnis vorhanden sind.
Parallel dazu existiert die HTTP API, über die bestimmte Funktionen gestartet werden können, die nicht standardmäßig in Elasticsearch
implementiert sind. Somit ist es möglich die Stoppwortliste per HTTP zu verändern oder abzurufen oder den Index neu zu indizieren
(dies ist notwendig wenn z.B. die Stoppwortliste verändert wurde).

#### Indizes
Standardmäßig werden zwei verschiedene Indizes erstellt:

1. untouched_pdf_index
Dieser Index besitzt kein Mapping und wird von Elasticsearch nicht verändert (Stoppwörter, Analyzer etc.)
Falls z.B. Änderungen an den Stoppwörtern vorgenommen werden, wird der Suchindex (alias pdf_index) ausgehend vom "untouched_pdf_index" neu indiziert.

2. pdf_index_vX (alias pdf_index)
Das **Mapping** des Index:
```json
{"content" : {
    "type" : "string",
    "analyzer" : "custom_medaes_analyzer",
    "term_vector" : "with_positions_offsets_payloads",
    "store" : "true"
  },
  "autocomplete" : {
    "type" : "completion",
    "analyzer" : "simple",
    "search_analyzer" : "simple",
    "payloads" : "true"
  }
}
```

Die definierten **Settings** des Index:
```json
{
  "analysis" : {
    "filter" : {
      "my_stop_german" : {
        "type" : "stop",
        "name" : "german",
        "stopword_path" : "scripts/stopwords.txt"
      },
      "my_stop_en" : {
        "type" : "stop",
        "stopwords" : "_english_"
      }
    },
    "analyzer" : {
      "custom_medaes_analyzer" : {
        "type" : "custom",
        "tokenizer" : "standard",
        "filter" : [
          "lowercase",
          "my_stop_german",
          "my_stop_en"
        ]
      }
    }
  }
}
```

#### Funktionen
- Erstellt automatisch die notwendigen Indizes
- Ein Verzeichnis wird vollständig indiziert und auf Veränderungen überwacht (Dokument hinzugefügt -> wird indiziert, Dokument gelöscht -> aus Index entfernen)
- HTTP API um Stoppwortliste zu verändern & abzurufen und neue Indizierung durchzuführen

#### Bezüglich config
- Die Konfigurationsdatei, auf die der Manager zugreift liegt unter:
    /Volumes/CREATE_Archiv/CREATE_Extensions/8_Textanalyse/elasticsearch/indexer/config.properties
- Bei Servermigration/Änderung der Ordnerstruktur müssen hier die Pfade angepasst werden.
