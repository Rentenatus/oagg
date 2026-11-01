# Review-Plan: AGG XML Serialization Extraction (Branch review/014-XML-ex)

**Erstellt:** 2026-09-13
**Reviewer:** Mistral Vibe
**Branch:** review/014-XML-ex
**Vergleichsbasis:** main
**Umfang:** 108 Dateien, ~39.910 Zeilen eingefuegt, ~185 Zeilen entfernt

---

## 1. Zusammenfassung der Analyse

Der Branch implementiert eine XML-Serialisierungs-Extraktion fuer das AGG-Projekt nach dem Adapter-Pattern. Die Arbeit umfasst 9 Phasen (laut Plan), 16 Core-Dateien, 22 Adapter-Dateien, 6 Legacy/Migration/Validation-Dateien, 14 Test-Dateien mit 127 @Test-Methoden, 3 POM-Dateien und zahlreiche Dokumentationsdateien.

**Gesamteinschaetzung:** Die Infrastruktur (Interfaces, DOM-Kontexte, Fabriken) ist strukturell vorhanden, aber es gibt **kritische Architekturluecken**, **funktionsunfaehige Codepfade**, **Sicherheitsrisiken** und **systematische Testqualitaetsprobleme**. Die Adapter sind alle duenne Wrapper ohne echte Serialisierungslogik. Die "neue XML-Pfad" Feature-Flag ist nicht in die Anwendung integriert und delegiert intern an Legacy-Code.

---

## 2. Kritische Befunde (Critical)

### C1. Keine Integration in GraGra.save/load

**Befund:** `src/agg/xt_basis/GraGra.java` (die zentrale Domain-Klasse) wurde **nicht modifiziert**. Die Methoden `save()` (Zeile 3957) und `load()` (Zeile 3997) verwenden weiterhin direkt `new XMLHelper()`. Es existiert **keine einzige Referenz** auf `agg.xml.*` im gesamten `src/agg/`-Verzeichnis.

**Auswirkung:** Die Feature-Flag-Mechanismus (`GraGraMigration.setUseNewXml(true)`) ist aus der Anwendung heraus **nicht erreichbar**. Die normale `GraGra.save()`/`GraGra.load()` API verwendet immer den Legacy-Pfad. Das Feature-Flag ist nur aufrufbar, indem man direkt `GraGraMigration.save()`/`GraGraMigration.load()` statisch aufruft.

**Paket:** AP-1

### C2. "Neuer XML-Pfad" ist ein Pass-Through zu Legacy

**Befund:** Beide Pfade in `GraGraMigration` delegieren letztlich an `XMLHelper`:
- `saveUsingNewXml` -> `XMLSaveLoad.saveObject(graGra, file)` -> Prueft `instanceof XMLObject` -> `LegacyCompatibility.serializeToFile()` -> `XMLHelper.save_to_xml()`
- `loadUsingNewXml` -> `LegacyCompatibility.deserializeFromFile(f, graGra)` -> `XMLHelper.read_from_xml()` + `helper.getTopObject(graGra)`

Da alle AGG-Domain-Klassen `XMLObject` implementieren, wird der `DOMXMLSerializer`/`DOMXMLDeserializer` in `XMLSaveLoad.saveObject` (Zeile 71-76) **niemals** erreicht -- er ist nur fuer Nicht-`XMLObject`-Objekte vorgesehen, die in AGG nicht existieren.

**Auswirkung:** Die gesamte neue DOM-Serialisierungsinfrastruktur wird in keinem echten Save/Load-Vorgang verwendet. Sie existiert nur als parallele, unverbundene Bibliothek.

**Paket:** AP-1

### C3. Alle Adapter sind duenne Wrapper ohne Serialisierungslogik

**Befund:** Alle 18+ Domain-Adapter (GraphAdapter, NodeAdapter, RuleAdapter, etc.) erweitern `DomainObjectAdapter<T>` -> `XMLObjectAdapter` und enthalten **keine** eigene `serialize()`/`deserialize()`-Implementierung. Die einzige Implementierung in `XMLObjectAdapter` (Zeilen 72-97) delegiert an `legacyObject.XwriteObject(xmlHelper)` bzw. `XreadObject(xmlHelper)`.

**Auswirkung:** Das Adapter-Pattern wurde strukturell aufgebaut, aber die Adapter sind reine Typ-Wrapper ohne Mehrwert. Die eigentliche Serialisierungslogik bleibt in den Domain-Klassen (ueber `XwriteObject`/`XreadObject`). Das Refactoring-Ziel "Separation of Concerns" wurde nicht erreicht.

**Paket:** AP-2

### C4. XMLAdapterFactory instanceof-Reihenfolge-Bug

**Befund:** In `XMLAdapterFactory.createAdapter()` (Zeilen 63-122) werden Supertypen vor Subtypen geprueft:
- `instanceof Graph` (Zeile 63) feuert vor `instanceof TypeGraph` (Zeile 73) -- TypeGraphAdapter ist tot
- `instanceof Rule` (Zeile 69) feuert vor `instanceof RuleScheme` (Zeile 83) und `instanceof MultiRule` (Zeile 85) -- beide Adapter tot
- `instanceof ExcludePairContainer` (Zeile 109) feuert vor allen 5 Subtypen (Zeilen 111-122) -- alle 5 Adapter tot

**Auswirkung:** 8 von 28 Adaptern sind **unreachable dead code** und werden nie instanziiert. Die entsprechenden Domain-Objekte erhalten den falschen (generischen) Adapter.

**Paket:** AP-3

### C5. Kein XXE-Schutz bei allen XML-Parsern

**Befund:** Keine der `DocumentBuilderFactory`-Instanzen (in `DOMXMLDeserializerContext` Zeilen 81/99, `DOMXMLSerializerContext` Zeile 63, `XMLUtils` Zeile 40) und keine `SchemaFactory` (`XMLValidator` Zeile 87) aktiviert `FEATURE_SECURE_PROCESSING` oder verbietet externe Entities.

**Auswirkung:** Beim Laden von nicht-vertrauenswuerdigen .ggx-Dateien koennen XXE-Angriffe (External Entity Injection, DoS) durchgefuehrt werden.

**Paket:** AP-4

---

## 3. Hochgradige Befunde (High)

### H1. XMLValidator: FATAL-Schweregrad wird nicht als Fehler behandelt

**Befund:** `XMLValidator.validate()` (Zeile 90) prueft nur `Severity.ERROR`, nicht `Severity.FATAL`. Der `CollectingErrorHandler.fatalError()` (Zeile 223-226) fuegt `Severity.FATAL` hinzu, aber `validate()` ignoriert FATAL-Issues.

**Auswirkung:** Ein Dokument mit fatalen Parser-Fehlern (z.B. nicht geschlossene Tags) wird als gueltig gemeldet.

**Paket:** AP-5

### H2. XMLHelperWrapper.createAdapter defekt

**Befund:** `XMLHelperWrapper.createAdapter()` (Zeile 134-142) erzeugt einen `XMLObjectAdapter`, setzt aber nie dessen `XMLHelper`. Die Methode `XMLObjectAdapter.serialize()` prueft `xmlHelper == null` (Zeile 73-74) und wirft eine Exception.

**Auswirkung:** Ein Aufruf von `serialize()`/`deserialize()` auf dem erzeugten Adapter schlaegt fehl. Die gesamte Adapter-Integration ueber `XMLHelperWrapper` ist unbrauchbar.

**Paket:** AP-6

### H3. XMLHelperDeserializerContext: Stub-Methoden

**Befund:**
- `moveToParent()` (Zeile 196-201) gibt immer `false` zurueck -- Navigation abwaerts ist moeglich, aufwaerts nicht
- `deserializeObject()` (Zeile 204-208) wirft immer `XMLSerializationException` -- Deserialisierungs-Dispatch unbrauchbar

**Auswirkung:** `XMLHelperDeserializerContext` kann nicht fuer vollstaendige DOM-Baum-Navigation verwendet werden.

**Paket:** AP-6

### H4. XMLHelperSerializerContext.toXMLString() gibt "" zurueck

**Befund:** `XMLHelperSerializerContext.toXMLString()` (Zeile 160-164) gibt einen leeren String zurueck. Kommentar: "For now, we return an empty string".

**Auswirkung:** Serialisierung ueber XMLHelperSerializerContext zu einem String schlaegt stillschweigend fehl.

**Paket:** AP-6

### H5. DOMXMLDeserializer.deserializeFromElement ignoriert Element-Parameter

**Befund:** `deserializeFromElement(Element element, ...)` (Zeile 127-131) erzeugt einen `DOMXMLDeserializerContext(Document)` dessen Konstruktor `document.getDocumentElement()` auf den Stack pusht. Das uebergebene `element` wird ignoriert.

**Auswirkung:** Deserialisierung beginnt immer am Dokument-Root, nicht am angegebenen Element.

**Paket:** AP-5

### H6. Test-Silent-Skipping: Tests bestehen vakuum

**Befund:** Alle 14 Test-Dateien verwenden das Muster `if (!file.exists()) return;` (z.B. RoundtripTest Zeile 58-60, CompatibilityTest Zeile 43, StressTest Zeile 31, etc.). Die Pfadvariable `BASELINE_DIR = "../test_xml/baseline/samples/"` ist relativ und funktioniert nur, wenn Tests aus dem `src_xml/`-Verzeichnis gestartet werden.

**Auswirkung:** Wenn der Pfad falsch ist (z.B. wenn Maven aus dem Root-Verzeichnis laeuft oder in CI), **bestehen alle Tests ohne etwas zu testen**. Es gibt keine Fehlermeldung oder Warnung. Die 127 "passenden" Tests koennten ein falsches Sicherheitsgefuehl vermitteln.

**Paket:** AP-7

### H7. Seichte Roundtrip-Verifikation

**Befund:** Die Roundtrip-Tests pruefen nur:
- Graph-Anzahl (getGraphsVec().size())
- Regel-Anzahl (getRulesVec().size())
- Name (getName())

Es werden **nicht** geprueft: Knotenanzahl, Kantenanzahl, Attributwerte, Typinformationen, Regelstrukturen, NACs/PACs, Constraints.

Der DOM-Roundtrip (`testDomRoundtrip`) prueft nur, dass der Root-Element-Name "Document" ist -- keine inhaltliche Verifikation.

**Auswirkung:** Datenverlust bei der Serialisierung (fehlende Knoten, veraenderte Attribute) wuerde nicht entdeckt.

**Paket:** AP-7

---

## 4. Mittlere Befunde (Medium)

### M1. Stack-Desynchronisation in XMLHelper-Kontexten

**Befund:** `XMLHelperSerializerContext.pushElement()` (Zeile 134-137) ruft `xmlHelper.push()` auf, aber `popElement()` (Zeile 140-148) kann `xmlHelper.pop()` nicht aufrufen (private). Deserialisier-Kontext aehnlich: `moveToFirstChild/moveToNextSibling` pushen, `moveToParent` popt nicht.

**Auswirkung:** Bei grossen Dokumenten wachsen interne Stacks unkontrolliert und `top()` liefert falsche Elemente.

**Paket:** AP-6

### M2. ReferenzResolver ungenutzt in Produktionscode

**Befund:** `ReferenceResolver` wird nur in `ReferenceResolverTest` referenziert. Kein Serializer/Deserializer verwendet ihn.

**Paket:** AP-8

### M3. XMLUtils ungenutzt in Produktionscode

**Befund:** `XMLUtils` wird nur in `XMLUtilsTest` referenziert. Keine Adapter- oder Kontext-Klasse nutzt ihn. Die DOM-Hilfslogik ist dreifach dupliziert (AbstractXMLSerializer, XMLUtils, DOMXMLSerializerContext).

**Paket:** AP-8

### M4. Layering-Verletzung: core importiert adapter

**Befund:** `core/DOMXMLSerializer.java` (Zeile 9) importiert `agg.xml.adapter.XMLAdapterFactory` und referenziert `agg.util.XMLObject` (Zeile 62). Die Core-Schicht darf nicht von der Adapter-Schicht abhaengen.

**Paket:** AP-5

### M5. Leaky Abstraction: Kontext-Interfaces exponieren DOM

**Befund:** `XMLSerializerContext` und `XMLDeserializerContext` verwenden `org.w3c.dom.Element` in fast allen Methoden. Eine SAX/StAX/JAXB-Implementierung ist unmoeglich.

**Paket:** AP-9

### M6. TypeRegistry: Stille ClassNotFoundException

**Befund:** `TypeRegistry.preRegisterAggTypes()` (Zeile 221-223) faengt `ClassNotFoundException` und setzt ohne Logging fort. Fehlende Domain-Klassen werden unbemerkt uebersprungen.

**Paket:** AP-5

### M7. Thread-Safety: TypeRegistry und ReferenceResolver

**Befund:** Beide verwenden unsynchronisierte HashMap/IdentityHashMap mit oeffentlichen Mutationsmethoden.

**Paket:** AP-5

### M8. Resource Leaks

**Befund:** InputStreams werden nicht geschlossen in: `DOMXMLDeserializer.deserializeFromStream` (Zeile 99), `DOMXMLDeserializerContext.parseDocument` (Zeile 97), `XMLValidator.loadDefaultSchema` (Zeile 170-176) und `loadSchemaFromStream` (Zeile 190-197).

**Paket:** AP-5

### M9. XSD-Schema ist ein No-Op

**Befund:** `agg-ggx-schema.xsd` verwendet 15x `xsd:any` mit `processContents="lax"` und 15x `anyAttribute` mit `processContents="lax"`. 13 von 15 complexTypes sind orphaned (nie von einem Element referenziert). Das Schema lehnt nur Dokumente ab, die nicht "Document" als Root haben oder die `version`-Attribut fehlt.

**Auswirkung:** Die "Validierung" liefert fuer praktisch jede XML-Datei mit `<Document version="...">` Root true zurueck, unabhaengig vom Inhalt.

**Paket:** AP-10

### M10. GraGraMigration: Kein transaktionales Speichern

**Befund:** `saveUsingNewXml` schreibt direkt in die Zieldatei. Bei Fehlern waehrend des Schreibens bleibt die Datei korrupt. Kein Temp-Datei-dann-Rename-Muster.

**Paket:** AP-5

---

## 5. Niedrige Befunde (Low)

### L1. 24 redundante `implements XMLSerializable` Deklarationen
Alle Domain-Adapter deklarieren `implements XMLSerializable`, obwohl sie es bereits ueber `DomainObjectAdapter` -> `XMLObjectAdapter` erben. **Paket:** AP-8

### L2. 5 Dateien mit unbenutzten Imports
GraphAdapter (4), NodeAdapter (1), ArcAdapter (1), RuleAdapter (1), DomainObjectAdapter (4). **Paket:** AP-8

### L3. Inkonsistente Interface-Referenzierung
Manche Dateien importieren `XMLSerializable`, andere nutzen den voll-qualifizierten Namen `agg.xml.core.XMLSerializable`. **Paket:** AP-8

### L4. Dead Field: DomainObjectAdapter.domainObject
`private final T domainObject` (Zeile 26) wird im Konstruktor geschrieben aber nie gelesen. `getDomainObject()` nutzt `getWrappedObject()`. **Paket:** AP-8

### L5. Dead Field: DOMXMLSerializerContext.xmlOutput
`private final StringBuilder xmlOutput` (Zeile 24) wird initialisiert aber nie gelesen oder geschrieben. **Paket:** AP-8

### L6. Dead Lifecycle Methods
`beginDeserialize`/`endDeserialize`/`beginSerialize`/`endSerialize` haben keine Produktionsaufrufer. **Paket:** AP-8

### L7. 18 identische Adapter-Skelette
Alle 18 trivialen Adapter haben identischen Code. Koennten durch einen generischen `TypedXMLObjectAdapter<T>` ersetzt werden. **Paket:** AP-2

### L8. 18 System.out.println in Tests
Tests sollten Asserts verwenden, nicht Konsolenausgaben. **Paket:** AP-7

### L9. Hardcodierter Apache-Xalan-Property
`DOMXMLSerializer` (Zeile 197) nutzt `{http://xml.apache.org/xslt}indent-amount` -- Xalan-spezifisch, nicht portabel. **Paket:** AP-5

### L10. Case-sensitive Dateiendungspruefung
`GraGraMigration` prueft `.ggx` case-sensitive (Zeile 122, 138). `.GGX`-Dateien wuerden abgelehnt. **Paket:** AP-5

### L11. System-scoped Dependency mit hartcodiertem Pfad
`src_xml/pom.xml` (Zeile 45): `D:/sdk_workspaces/andimcol/dist/andimcol.jar` -- nicht portabel, bricht auf anderen Maschinen/CI. **Paket:** AP-11

### L12. Nicht-Standard sourceDirectory
Beide POMs nutzen `<sourceDirectory>.</sourceDirectory>` weil Java-Quelldateien direkt unter `src/` und `src_xml/` liegen (nicht unter `src/main/java`). **Paket:** AP-11

---

## 6. Dokumentations-Inkonsistenzen

### D1. MIGRATION_TRACKER.md: Alle 96 Klassen zeigen "Not Started / 0%"
Der Header sagt "Phase 3 - Complete" und die Phasen-Tabelle sagt Phasen 3-8 "Complete", aber die detailed Per-Klassen-Tabellen (Zeilen 29-37, 68-83) zeigen fuer alle 96 Klassen "0% / Not Started". Der Tracker wurde nie aktualisiert, um die tatsaechliche Adapter-Erstellung widerzuspiegeln. **Paket:** AP-12

### D2. src_xml/README.md: Test-Anzahl inkonsistent
Phase 7 sagt "131 tests passing" (Zeile 193), der Refactoring-Plan sagt "139 tests" (Zeile 376). Tatsaechliche @Test-Count: 127. **Paket:** AP-12

### D3. src_xml/README.md: Phase 5 fehlt in der Status-Sektion
Die Status-Sektion geht von Phase 4 direkt zu Phase 6 (Zeilen 173-178). Phase 5 (Save/Load Migration) wird uebersprungen. **Paket:** AP-12

### D4. docs/refactoring/README.md: Veraltete Datum-Angabe
Sagt "All documentation is current and complete as of 2026-09-08" (Zeile 246), aber Phasen 3-8 wurden am 2026-09-13 abgeschlossen. **Paket:** AP-12

### D5. Duplizierte Test-Daten
Vier identische .ggx-Dateien existieren sowohl in `test-data/baseline/samples/` als auch in `test_xml/baseline/samples/` (insgesamt 8 Kopien, ~410KB). **Paket:** AP-11

### D6. src_xml/README.md: Architektur-Zusammenfassung unvollstaendig
Die Architektur-Zusammenfassung (Zeilen 199-209) listet "20+ classes" fuer adapter, aber es sind 22 Dateien. Zeigt mapper als "(Planned)" obwohl die Klassen existieren. **Paket:** AP-12

### D7. Test-Framework falsch dokumentiert
src_xml/README.md (Zeile 145) erwaehnt "JUnit 4.13.2", aber alle Tests verwenden TestNG (`org.testng.annotations.Test`). **Paket:** AP-12

---

## 7. Arbeitspakete

Die Arbeitspakete sind nach Prioritaet geordnet. Jedes Paket ist unabhaengig durchfuehrbar, sofern nicht anders angegeben.

---

### AP-1: Architektur-Integration -- GraGra an neues XML-Modul anbinden

**Prioritaet:** CRITICAL
**Aufwand:** Hoch (2-3 Tage)
**Abhaengigkeiten:** Keine (kann parallel zu AP-2 gestartet werden, aber AP-2 ist Voraussetzung fuer echten Mehrwert)

**Problem:** GraGra.save/load verwendet direkt XMLHelper. Das Feature-Flag ist nicht erreichbar. Der "neue XML-Pfad" delegiert an Legacy.

**Schritte:**

1. **GraGra.save() mit Feature-Flag versehen**
   - Datei: `src/agg/xt_basis/GraGra.java`, Methode `save()` ab Zeile 3957
   - Fuege Bedingung ein: `if (GraGraMigration.isUseNewXml()) { GraGraMigration.save(this, filename); return; }`
   - Importiere `agg.xml.migration.GraGraMigration`
   - Achtung: Dies ist die einzige Modifikation an einer Domain-Klasse -- sie ist minimal (3 Zeilen) und dient nur als Dispatch

2. **GraGra.load() mit Feature-Flag versehen**
   - Datei: `src/agg/xt_basis/GraGra.java`, Methode `load()` ab Zeile 3997
   - Fuege Bedingung ein: `if (GraGraMigration.isUseNewXml()) { GraGraMigration.load(this, filename); return; }`

3. **GraGraMigration.saveUsingNewXml an echten DOM-Serializer anbinden**
   - Datei: `src_xml/agg/xml/migration/GraGraMigration.java`, Methode `saveUsingNewXml` ab Zeile 107
   - Statt `XMLSaveLoad.saveObject(graGra, outputFile)` (das an XMLHelper delegiert):
     - Erzeuge `DOMXMLSerializer` via `XMLSerializerFactory.createSerializer()`
     - Erzeuge Adapter via `XMLAdapterFactory.createAdapter(graGra)`
     - Rufe `serializer.serializeToFile(adapter, file)` auf
   - Voraussetzung: AP-2 muss abgeschlossen sein, damit der Adapter echte Serialisierung durchfuehrt

4. **GraGraMigration.loadUsingNewXml an echten DOM-Deserializer anbinden**
   - Datei: `src_xml/agg/xml/migration/GraGraMigration.java`, Methode `loadUsingNewXml` ab Zeile 133
   - Statt `LegacyCompatibility.deserializeFromFile(f, graGra)` (das an XMLHelper delegiert):
     - Erzeuge `DOMXMLDeserializer` via `XMLDeserializerFactory.createDeserializerWithAggTypes()`
     - Rufe `deserializer.deserializeFromFile(file, GraGra.class)` auf
   - Voraussetzung: AP-2 muss abgeschlossen sein, damit die Deserialisierung ohne XMLHelper funktioniert

5. **Roundtrip-Test mit aktiviertem Feature-Flag hinzufuegen**
   - Neue Test-Methode in `RoundtripTest.java`: `testNewXmlRoundtripWithFeatureFlag`
   - Setze `GraGraMigration.setUseNewXml(true)`
   - Lade .ggx-Datei, speichere, lade neu, vergleiche (mit tiefen Vergleichen, siehe AP-7)
   - Reset: `GraGraMigration.setUseNewXml(false)` im `@AfterTest`

**Akzeptanzkriterien:**
- `GraGra.save()`/`load()` verwendet bei `setUseNewXml(true)` den neuen DOM-Pfad
- Der neue Pfad verwendet `DOMXMLSerializer`/`DOMXMLDeserializer`, nicht `XMLHelper`
- Roundtrip-Test mit Feature-Flag bestanden
- Bestehende Tests bei `setUseNewXml(false)` unverändert bestanden

---

### AP-2: Adapter mit echter Serialisierungslogik ausstatten

**Prioritaet:** CRITICAL
**Aufwand:** Sehr hoch (5-10 Tage fuer Kern-Adapter)
**Abhaengigkeiten:** AP-4 (XXE-Schutz muss vorher fuer Sicherheit vorhanden sein)

**Problem:** Alle Adapter sind duenne Wrapper, die an `XwriteObject`/`XreadObject` delegieren. Das Refactoring-Ziel (Separation of Concerns) wurde nicht erreicht.

**Schritte:**

1. **Priorisierung der Adapter festlegen**
   - Reihenfolge: GraGraAdapter -> GraphAdapter -> NodeAdapter -> ArcAdapter -> RuleAdapter -> TypeGraphAdapter -> TypeImplAdapter -> ValueTupleAdapter -> ...
   - Beginne mit GraGraAdapter, da er der Einstiegspunkt fuer Save/Load ist

2. **GraGraAdapter.serialize() mit DOM-Logik implementieren**
   - Datei: `src_xml/agg/xml/adapter/GraGraAdapter.java`
   - Lies `GraGra.saveXML(XMLHelper h)` (Zeile 4073 in GraGra.java) als Referenz
   - Implementiere `serialize(XMLSerializerContext context)` mit direkten DOM-Operationen:
     - `context.createAndAppendElement("GraGra")`
     - Schreibe Name, Version, etc. als Attribute
     - Iteriere ueber Graphen, Regeln, Typen, Constraints
     - Fuer jede Sub-Komponente: delegiere an deren Adapter (rekursiv)
   - Kein Aufruf von `XwriteObject`

3. **GraGraAdapter.deserialize() mit DOM-Logik implementieren**
   - Lies `GraGra.XreadObject(XMLHelper h)` als Referenz
   - Implementiere `deserialize(XMLDeserializerContext context)`:
     - Lese Attribute vom aktuellen Element
     - Navigiere zu Kind-Elementen
     - Erzeuge Graphen, Regeln, etc. ueber deren Adapter
   - Kein Aufruf von `XreadObject`

4. **Pro Adapter: Unit-Test mit Roundtrip-Verifikation**
   - Fuer jeden implementierten Adapter:
     - Test: Objekt erzeugen -> serialisieren -> deserialisieren -> tiefes Vergleichen
     - Test: .ggx-Datei laden -> serialisieren -> neu laden -> tiefes Vergleichen

5. **Fallback-Strategie definieren**
   - Falls ein Adapter nicht vollstaendig implementiert werden kann: Feature-Flag pro Typ ermoeglichen
   - `GraGraMigration.setUseNewXmlForType(Class<?> type, boolean useNew)`

6. **Generischen TypedXMLObjectAdapter fuer triviale Typen erstellen**
   - Ersetze die 18 identischen Adapter-Skelette durch einen generischen Adapter
   - Behalte nur Adapter mit echter Logik als separate Klassen

**Akzeptanzkriterien:**
- Mindestens GraGraAdapter, GraphAdapter, NodeAdapter, ArcAdapter haben eigene serialize/deserialize
- Diese verwenden `XMLSerializerContext`/`XMLDeserializerContext` direkt, nicht XMLHelper
- Roundtrip-Tests fuer diese Adapter bestanden
- Die anderen Adapter koennen vorerst delegieren, sind aber als `TypedXMLObjectAdapter` markiert

---

### AP-3: XMLAdapterFactory instanceof-Reihenfolge korrigieren

**Prioritaet:** CRITICAL
**Aufwand:** Niedrig (1-2 Stunden)
**Abhaengigkeiten:** Keine

**Problem:** Supertypen werden vor Subtypen geprueft, wodurch 8 Adapter unreachable sind.

**Schritte:**

1. **Vererbungshierarchie pruefen**
   - Datei: `src_xml/agg/xml/adapter/XMLAdapterFactory.java`
   - Bestaetige Vererbung: TypeGraph extends Graph, RuleScheme extends Rule, MultiRule extends Rule, DependencyPairContainer extends ExcludePairContainer, LayeredExcludePairContainer extends ExcludePairContainer, etc.

2. **instanceof-Checks umsortieren (most-specific zuerst)**
   - Zeile 63-85: Verschiebe `instanceof TypeGraph` VOR `instanceof Graph`
   - Verschiebe `instanceof RuleScheme` und `instanceof MultiRule` VOR `instanceof Rule`
   - Zeile 109-122: Sortiere alle Parser-Subtypen vor `instanceof ExcludePairContainer`

3. **Test hinzufuegen**
   - Neue Test-Methode in `AdapterFactoryTest.java`:
     - `testTypeGraphGetsTypeGraphAdapter` -- erzeuge TypeGraph, pruefe Adapter-Typ
     - `testRuleSchemeGetsRuleSchemeAdapter` -- erzeuge RuleScheme, pruefe Adapter-Typ
     - `testMultiRuleGetsMultiRuleAdapter` -- erzeuge MultiRule, pruefe Adapter-Typ
     - `testLayeredExcludePairContainerGetsCorrectAdapter`
     - `testDependencyPairContainerGetsCorrectAdapter`

4. **Alle Parser-Subtyp-Adapter testen**
   - Eine Test-Methode pro Subtyp, die verifiziert, dass der korrekte Adapter zurueckgegeben wird

**Akzeptanzkriterien:**
- Alle 8 bisher-unreachable Adapter werden korrekt instanziiert
- Neue Tests bestanden
- Bestehende Tests unbeeinflusst

---

### AP-4: XXE-Schutz fuer alle XML-Parser

**Prioritaet:** CRITICAL
**Aufwand:** Niedrig (2-3 Stunden)
**Abhaengigkeiten:** Keine

**Problem:** Keine DocumentBuilderFactory/SchemaFactory hat XXE-Schutz aktiviert.

**Schritte:**

1. **Zentrale Hilfsmethode erstellen**
   - Datei: `src_xml/agg/xml/util/XMLUtils.java` (existiert bereits)
   - Neue Methode: `public static DocumentBuilderFactory createSecureDocumentBuilderFactory()`
   - Setze: `FEATURE_SECURE_PROCESSING`, `disallow-doctype-decl`, `external-general-entities=false`, `external-parameter-entities=false`, `load-external-dtd=false`

2. **DOMXMLDeserializerContext.patchen**
   - Datei: `src_xml/agg/xml/core/DOMXMLDeserializerContext.java`, Zeilen 81 und 99
   - Ersetze `DocumentBuilderFactory.newInstance()` durch `XMLUtils.createSecureDocumentBuilderFactory()`

3. **DOMXMLSerializerContext.patchen**
   - Datei: `src_xml/agg/xml/core/DOMXMLSerializerContext.java`, Zeile 63
   - Gleiche Ersetzung

4. **XMLValidator.patchen**
   - Datei: `src_xml/agg/xml/validation/XMLValidator.java`, Zeile 87
   - `SchemaFactory.newInstance()` mit secure processing versehen

5. **Test hinzufuegen**
   - `XMLValidatorTest`: Test mit XXE-Payload, der bestatigt, dass externe Entities blockiert werden
   - `DOMXMLContextTest`: Test mit DOCTYPE-Deklaration, der bestaetigt, dass Parsing abgelehnt wird

**Akzeptanzkriterien:**
- Alle DocumentBuilderFactory-Instanzen verwenden secure Factory
- XXE-Payload wird abgelehnt
- Bestehende .ggx-Dateien laden weiterhin korrekt

---

### AP-5: Core-Bugs und Code-Qualitaet beheben

**Prioritaet:** HIGH
**Aufwand:** Mittel (1-2 Tage)
**Abhaengigkeiten:** Keine

**Schritte:**

1. **XMLValidator: FATAL als Fehler behandeln**
   - Datei: `src_xml/agg/xml/validation/XMLValidator.java`, Zeile 90
   - Aendere: `return issues.stream().noneMatch(i -> i.getSeverity() == Severity.ERROR || i.getSeverity() == Severity.FATAL);`
   - Test: `testFatalErrorCausesValidationFailure` mit nicht geschlossener XML-Datei

2. **DOMXMLDeserializer.deserializeFromElement reparieren**
   - Datei: `src_xml/agg/xml/core/DOMXMLDeserializer.java`, Zeilen 127-131
   - Statt `new DOMXMLDeserializerContext(doc)`: Erzeuge Kontext und setze Start-Element auf das uebergebene Element (neuer Konstruktor oder Setter)

3. **Layering-Verletzung beheben**
   - Datei: `src_xml/agg/xml/core/DOMXMLSerializer.java`, Zeile 9 und 62
   - Entferne Import von `XMLAdapterFactory` und Referenz auf `XMLObject`
   - Verschiebe die `instanceof XMLSerializable` / `XMLObject` Dispatch-Logik in eine Factory oder einen Wrapper in der `legacy`- oder `adapter`-Schicht

4. **TypeRegistry: ClassNotFoundException protokollieren**
   - Datei: `src_xml/agg/xml/mapper/TypeRegistry.java`, Zeile 221-223
   - Fuege `System.err.println` oder SLF4J-Logger hinzu: "Warning: AGG type not found on classpath: " + className
   - Alternative: Liste nicht gefundener Typen verfuegbar machen

5. **Thread-Safety dokumentieren oder beheben**
   - TypeRegistry: `Collections.synchronizedMap` oder `ConcurrentHashMap` verwenden
   - ReferenceResolver: Gleiche Behandlung oder `@NotThreadSafe`-JavaDoc

6. **Resource Leaks beheben**
   - DOMXMLDeserializer.deserializeFromStream: try-with-resources fuer InputStream
   - DOMXMLDeserializerContext.parseDocument: try-with-resources
   - XMLValidator.loadDefaultSchema/loadSchemaFromStream: try-with-resources fuer InputStream

7. **Exception Re-Wrapping konsistent machen**
   - DOMXMLDeserializerContext.deserializeObject (Zeile 287): Re-throw XMLSerializationException direkt
   - GraGraMigration.loadUsingNewXml (Zeile 144-146): Werfe XMLSerializationException statt generischer Exception

8. **Dead Field entfernen**
   - DOMXMLSerializerContext.xmlOutput (Zeile 24): Entfernen
   - DomainObjectAdapter.domainObject (Zeile 26): Entfernen oder korrekt verwenden

9. **Dead Lifecycle Methods entfernen oder implementieren**
   - beginDeserialize/endDeserialize/beginSerialize/endSerialize: Wenn ungenutzt, entfernen (YAGNI)

10. **Hardcodierten Xalan-Property entfernen oder konfigurierbar machen**
    - DOMXMLSerializer (Zeile 197): Pruefe, ob Transformer den Property unterstuetzt, bevor er gesetzt wird

11. **Case-sensitive .ggx-Pruefung**
    - GraGraMigration (Zeilen 122, 138): `filename.toLowerCase().endsWith(".ggx")`

**Akzeptanzkriterien:**
- FATAL-Validierungsfehler fuehren zu `validate() == false`
- deserializeFromElement deserialisiert ab dem angegebenen Element
- core-Paket hat keine Imports von adapter-Paket
- Resource Leaks behoben (statische Analyse)
- Alle Tests bestanden

---

### AP-6: XMLHelper-Kontexte reparieren oder als deprecated markieren

**Prioritaet:** HIGH
**Aufwand:** Mittel (1 Tag)
**Abhaengigkeiten:** Keine

**Problem:** XMLHelperSerializerContext und XMLHelperDeserializerContext haben mehrere Stubs und defekte Methoden.

**Schritte:**

1. **createAdapter reparieren**
   - Datei: `src_xml/agg/xml/legacy/XMLHelperWrapper.java`, Zeile 134-142
   - Nach Erzeugung des XMLObjectAdapter: `((XMLObjectAdapter) adapter).setXMLHelper(this.xmlHelper);`

2. **moveToParent implementieren**
   - Datei: `src_xml/agg/xml/adapter/XMLHelperDeserializerContext.java`, Zeile 196-201
   - Option A: Eigenen Stack verwalten (parallel zu XMLHelper's Stack) und davon poppen
   - Option B: XMLHelper.pop() als package-private oder public freigeben (Aenderung an agg-core)
   - Empfehlung: Option A, da keine Aenderung an agg-core noetig

3. **deserializeObject implementieren**
   - Datei: `src_xml/agg/xml/adapter/XMLHelperDeserializerContext.java`, Zeile 204-208
   - Verwende TypeRegistry oder XMLAdapterFactory, um den Typ zu bestimmen und zu deserialisieren

4. **toXMLString implementieren**
   - Datei: `src_xml/agg/xml/adapter/XMLHelperSerializerContext.java`, Zeile 160-164
   - Verwende `DOMXMLSerializerContext` (wie in `XMLHelperWrapper.toXMLString` bereits gemacht)

5. **Stack-Desynchronisation beheben**
   - Eigenen Stack in beiden Kontexten verwalten, parallel zu XMLHelper's Stack

6. **Alternative: Kontexte als deprecated markieren**
   - Wenn eine Reparatur zu aufwendig ist: `@Deprecated` mit JavaDoc-Erklaerung
   - Verwende stattdessen `DOMXMLSerializerContext`/`DOMXMLDeserializerContext`

**Akzeptanzkriterien:**
- Entweder: Alle Stub-Methoden funktionsfaehig mit Tests
- Oder: Klassen als `@Deprecated` markiert mit Begruendung
- createAdapter setzt XMLHelper korrekt

---

### AP-7: Test-Qualitaet verbessern

**Prioritaet:** HIGH
**Aufwand:** Mittel (1-2 Tage)
**Abhaengigkeiten:** Keine

**Problem:** Tests bestehen vakuum (silent skipping), Roundtrip-Verifikation ist seicht, Pfad-Abhaengigkeit.

**Schritte:**

1. **Silent Skipping durch fail() ersetzen**
   - Alle 14 Test-Dateien: Ersetze `if (!file.exists()) return;` durch:
     ```java
     assumeTrue(file.exists(), "Test data file missing: " + file);
     ```
   - Oder verwende `@BeforeClass` mit `Assume.assumeTrue` fuer alle Dateien
   - Alternative: Verwende Maven-Test-Resource-Path statt relativen Pfad

2. **Pfad-Konfiguration robust machen**
   - Ersetze hardcoded `BASELINE_DIR = "../test_xml/baseline/samples/"` durch:
     - Maven-basierten Test-Resource-Pfad: `src/test/resources/baseline/samples/`
     - Oder `System.getProperty("test.data.dir", "../test_xml/baseline/samples/")`
   - Kopiere Test-Daten in `src_xml/test/resources/` als Maven-Resource

3. **Tiefe Roundtrip-Verifikation implementieren**
   - Neue Hilfsklasse: `GraphComparator` (im test-Verzeichnis)
   - Vergleiche: Knotenanzahl, Kantenanzahl, Typen, Attribute (Name, Wert), Graph-Struktur
   - Verwende in `RoundtripTest.testLegacyRoundtrip` und `testNewXmlRoundtrip`

4. **DOM-Roundtrip mit Inhalt vergleichen**
   - `testDomRoundtrip`: Vergleiche nicht nur Root-Element-Name, sondern:
     - Anzahl Kind-Elemente
     - Attribut-Werte an vergleichen Positionen
     - Text-Content

5. **18 System.out.println entfernen**
   - Ersetze durch Asserts oder entferne (Debug-Output)

6. **Adapter-spezifische Tests hinzufuegen**
   - Fuer jeden Adapter in AP-2 implementiert: Roundtrip-Test
   - Test fuer XMLAdapterFactory-Dispatch (siehe AP-3)

7. **Negativ-Tests hinzufuegen**
   - Test mit fehlerhafter XML-Datei (ungueltige Syntax)
   - Test mit fehlendem Root-Element
   - Test mit leerer Datei
   - Test mit Nicht-.ggx-Datei

8. **Feature-Flag-Isolation sicherstellen**
   - FeatureFlagTest: `@AfterTest` reset ist vorhanden (gut)
   - Aber: Test-Reihenfolge kann andere Tests beeinflussen
   - Verwende `@BeforeClass` in ALLEN Tests, die GraGraMigration verwenden, um Flag explizit zu setzen

**Akzeptanzkriterien:**
- Kein `if (!file.exists()) return;` in Tests
- Roundtrip vergleicht Knoten, Kanten, Typen, Attribute
- 0 System.out.println in Tests
- Negativ-Tests vorhanden
- Tests bestehen unabhaengig vom Arbeitsverzeichnis

---

### AP-8: Dead Code entfernen und DRY-Verletzungen beheben

**Prioritaet:** MEDIUM
**Aufwand:** Niedrig (halber Tag)
**Abhaengigkeiten:** AP-5 (Dead Fields werden dort entfernt)

**Schritte:**

1. **ReferenceResolver: Produktionsverwendung herstellen oder entfernen**
   - Entweder: In Serializer/Deserializer integrieren (fuer Zyklen-/Shared-Reference-Handling)
   - Oder: Als `@Experimental` markieren und JavaDoc aktualisieren
   - Empfehlung: Integrieren, da es fuer korrekte Deserialisierung mit Referenzen noetig ist

2. **XMLUtils: In Produktionscode integrieren oder mit DOMXMLSerializerContext konsolidieren**
   - Entferne Duplikation: DOM-Hilfslogik existiert in XMLUtils, AbstractXMLSerializer, DOMXMLSerializerContext
   - Entscheidung: XMLUtils als einzige Quelle fuer statische DOM-Hilfsmethoden
   - AbstractXMLSerializer und DOMXMLSerializerContext delegieren an XMLUtils

3. **AbstractXMLSerializer: Entfernen oder konsolidieren**
   - Wenn keine Unterklasse die Helper-Methoden nutzt: Entfernen
   - Wenn doch: An XMLUtils delegieren

4. **24 redundante `implements XMLSerializable` entfernen**
   - Alle Domain-Adapter: Entferne `implements XMLSerializable` (bereits ueber Vererbung vorhanden)

5. **Unbenutzte Imports entfernen**
   - GraphAdapter, NodeAdapter, ArcAdapter, RuleAdapter, DomainObjectAdapter

6. **Inkonsistente Interface-Referenzierung vereinheitlichen**
   - Alle Dateien: Verwende importiertes `XMLSerializable` statt FQN

7. **18 identische Adapter-Skelette konsolidieren**
   - Siehe AP-2 Schritt 6: Generischer `TypedXMLObjectAdapter<T>`

**Akzeptanzkriterien:**
- Keine dreifach duplizierte DOM-Helper-Logik
- Keine unbenutzten Imports
- Keine redundanten `implements`-Deklarationen
- Build und Tests bestanden

---

### AP-9: Abstraktion ueberdenken -- Kontext-Interfaces von DOM entkoppeln

**Prioritaet:** MEDIUM
**Aufwand:** Hoch (2-3 Tage)
**Abhaengigkeiten:** AP-2

**Problem:** XMLSerializerContext und XMLDeserializerContext exponieren `org.w3c.dom.Element` in fast allen Methoden. Nicht-DOM-Implementierungen sind unmoeglich.

**Schritte:**

1. **Abstrakte Element-Repraesentation einfuehren**
   - Neues Interface: `XMLElement` mit Methoden wie `getName()`, `getAttribute(String)`, `getTextContent()`, `getChildElements()`
   - DOM-Implementierung: `DOMXMLElement implements XMLElement` (Wrapper um org.w3c.dom.Element)

2. **Kontext-Interfaces auf XMLElement umstellen**
   - `XMLSerializerContext.createElement(String): XMLElement`
   - `XMLDeserializerContext.getCurrentElement(): XMLElement`
   - Alle Methoden, die derzeit Element zurueckgeben/annehmen, auf XMLElement umstellen

3. **DOM-Implementierungen anpassen**
   - DOMXMLSerializerContext, DOMXMLDeserializerContext: Interne Verwendung von DOMXMLElement
   - Alle Aufrufer anpassen

4. **Tests anpassen**
   - Alle Tests, die `org.w3c.dom.Element` direkt verwenden, auf `XMLElement` umstellen

**Hinweis:** Dies ist ein groesserer Eingriff. Alternativ kann die Entscheidung getroffen werden, die DOM-Kopplung zu akzeptieren und das als Architekturentscheidung zu dokumentieren.

**Akzeptanzkriterien:**
- Kontext-Interfaces verwenden kein `org.w3c.dom.*`
- Mindestens eine DOM-Implementierung existiert und funktioniert
- Alle Tests bestanden

---

### AP-10: XSD-Schema sinnvoll gestalten oder entfernen

**Prioritaet:** MEDIUM
**Aufwand:** Mittel (1 Tag)
**Abhaengigkeiten:** Keine

**Problem:** Das Schema validiert praktisch nichts. 13/15 complexTypes sind orphaned. xsd:any mit processContents=lax ueberall.

**Schritte:**

1. **Entscheidung treffen: Schema strikt oder entfernen**
   - Option A: Striktes Schema basierend auf ggx-format-analysis.md (38 Elemente, 46 Attribute)
   - Option B: Schema entfernen und nur wohlgeformt-Check durchfuehren
   - Option C: Hybrides Schema: Nur Root-Struktur strikt, Inhalt lax

2. **Bei Option A: Schema aus Format-Analyse erstellen**
   - Nutze `docs/refactoring/ggx-format-analysis.md` und `ggx-elements.txt`, `ggx-attributes.txt`
   - Definiere echte complexTypes mit required/optional Attributen
   - Entferne xsd:any/anyAttribute Catch-Alls (oder beschraenke auf konkrete Namespaces)

3. **Bei Option C: Orphaned complexTypes referenzieren oder entfernen**
   - Verbinde die 13 orphaned Types mit ihren Elementen
   - Behalte xsd:any nur fuer Erweiterungspunkte

4. **Validierungs-Tests mit echten Fehlern**
   - Test mit Dokument, das fehlende required Attribute hat -> sollte fehlschlagen
   - Test mit falscher Element-Struktur -> sollte fehlschlagen

**Akzeptanzkriterien:**
- Schema lehnt tatsaechlich fehlerhafte Dokumente ab
- Alle 4 .ggx-Testdateien bestehen Validierung
- Negativ-Tests vorhanden

---

### AP-11: Build-Infrastruktur verbessern

**Prioritaet:** MEDIUM
**Aufwand:** Niedrig (halber Tag)
**Abhaengigkeiten:** Keine

**Schritte:**

1. **System-scoped Dependency ersetzen**
   - Datei: `src_xml/pom.xml`, Zeile 41-46
   - Installiere `andimcol.jar` in lokales Maven-Repo oder verwende `mvn install:install-file`
   - Oder: Lade JAR aus einem Repository (falls verfuegbar)
   - Entferne `<scope>system</scope>` und `<systemPath>`

2. **Duplizierte Test-Daten konsolidieren**
   - `test-data/baseline/samples/` und `test_xml/baseline/samples/` sind identisch
   - Behalte nur `test_xml/baseline/samples/` (wird von Tests verwendet)
   - Entferne `test-data/` oder erstelle Symlink

3. **Nicht-Standard sourceDirectory dokumentieren oder beheben**
   - Aktuell: `<sourceDirectory>.</sourceDirectory>` weil Quelldateien direkt unter `src/` liegen
   - Langfristig: Verschiebe nach `src/main/java/` (Standard Maven-Layout)
   - Kurzfristig: Kommentar im POM erklaeren, warum non-Standard

4. **testExcludes no-op entfernen**
   - Datei: `src_xml/pom.xml`, Zeile 79-81
   - `<testExclude>nothing</testExclude>` ist ein no-op -- entfernen

5. **Surefire-Konfiguration: workingDirectory setzen**
   - Setze `<workingDirectory>${project.basedir}</workingDirectory>` im surefire-plugin
   - Damit sind die relativen Pfade `../test_xml/baseline/samples/` immer korrekt
   - Alternative: Verwende `${project.basedir}/../test_xml/baseline/samples/` in Tests

**Akzeptanzkriterien:**
- Keine system-scoped Dependencies mit hartcodierten Pfaden
- Keine duplizierten Test-Daten
- Build funktioniert auf frischer Maschine (nach Maven-Setup)

---

### AP-12: Dokumentation konsistent machen

**Prioritaet:** MEDIUM
**Aufwand:** Niedrig (halber Tag)
**Abhaengigkeiten:** AP-1, AP-2 (fuer korrekte Status-Angaben)

**Schritte:**

1. **MIGRATION_TRACKER.md: Per-Klassen-Status aktualisieren**
   - Aktualisiere alle Tabellen (Zeilen 29-37, 68-83, etc.)
   - Setze Status auf "Adapter Created (thin wrapper)" fuer die 28 erstellten Adapter
   - Setze Status auf "Not Started" fuer die ~68 nicht erstellten Adapter
   - Korrigiere die Statistik-Tabelle (Zeilen 27-37)

2. **src_xml/README.md: Test-Anzahl korrigieren**
   - Zeile 193: Aendere "131 tests" zu tatsaechlicher Anzahl (127)
   - Zeile 145: Aendere "JUnit 4.13.2" zu "TestNG 7.8.0"

3. **src_xml/README.md: Phase 5 hinzufuegen**
   - Fuege Phase 5 Status-Sektion zwischen Zeile 176 und 178 ein

4. **src_xml/README.md: Architektur-Zusammenfassung korrigieren**
   - Zeile 205: "20+ classes" -> "22 files"
   - Zeile 36-37: Entferne "(Planned)" fuer mapper und legacy, da sie existieren

5. **docs/refactoring/README.md: Datum aktualisieren**
   - Zeile 246: Aendere "2026-09-08" zu "2026-09-13"
   - Aktualisiere Status-Tabelle (Zeilen 237-244)

6. **REFACTORING_PLAN_XML_EXTRACTION.md: Ehrliche Status-Angaben**
   - Zeile 403: "Phase 4: 100% Complete (28+ adapters)" -> "(28 thin-wrapper adapters, no DOM logic)"
   - Zeile 405: "Phase 7: 100% Complete (139 tests, 0 failures)" -> "(127 tests, tests skip silently if data missing)"
   - Fuege Hinweis hinzu: "Adapters are thin wrappers. Real DOM serialization not yet implemented."

7. **REFACTORING_PLAN: Version History aktualisieren**
   - Fuege Version 1.5 mit Review-Ergebnissen und ehrlichem Status

**Akzeptanzkriterien:**
- Alle Dokumente haben konsistente Zahlen und Daten
- Keine irrefuehrenden "100% Complete"-Angaben fuer unvollstaendige Phasen
- Status spiegelt tatsaechlichen Implementierungsgrad wider

---

## 8. Prioritaets-Matrix und Empfohlene Reihenfolge

| Reihenfolge | Paket | Prioritaet | Aufwand | Begruendung |
|-------------|-------|-----------|--------|-------------|
| 1 | AP-3 | CRITICAL | 1-2h | Schneller Fix, macht 8 Adapter erreichbar |
| 2 | AP-4 | CRITICAL | 2-3h | Sicherheitskritisch, unabhaengig |
| 3 | AP-5 | HIGH | 1-2 Tage | Behebt Core-Bugs, macht Infrastruktur nutzbar |
| 4 | AP-7 | HIGH | 1-2 Tage | Test-Vertrauen herstellen, bevor weiter gebaut wird |
| 5 | AP-6 | HIGH | 1 Tag | Kontexte funktionsfaehig oder deprecated |
| 6 | AP-1 | CRITICAL | 2-3 Tage | Eigentliche Integration -- erfordert AP-2 fuer Mehrwert |
| 7 | AP-2 | CRITICAL | 5-10 Tage | Kernstueck: echte Serialisierungslogik |
| 8 | AP-8 | MEDIUM | 0.5 Tage | Cleanup nach AP-5 |
| 9 | AP-10 | MEDIUM | 1 Tag | Validation sinnvoll machen |
| 10 | AP-9 | MEDIUM | 2-3 Tage | Architektur-Verbesserung (optional) |
| 11 | AP-11 | MEDIUM | 0.5 Tage | Build-Portabilitaet |
| 12 | AP-12 | MEDIUM | 0.5 Tage | Dokumentation (zuletzt, da Status vom Code abhaengt) |

**Gesamt-Schaetzung:** 15-25 Tage fuer alle Pakete

---

## 9. Was man besser machen koennte (Zusammenfassung)

### Architektur
- **Echte Trennung statt Wrapper:** Die Adapter sollten echte DOM-Serialisierung enthalten, nicht delegieren. Aktuell ist das "Adapter Pattern" ein leeres Pattern -- die Logik bleibt in den Domain-Klassen.
- **Feature-Flag muss in die Domain-Klasse:** Ohne Modifikation an GraGra.save/load ist das Feature-Flag wertlos. Ein 3-Zeilen-Dispatch waere ausreichend gewesen.
- **DOM-Abhaengigkeit kapseln:** Die Kontext-Interfaces duerfen kein `org.w3c.dom.Element` exponieren, sonst ist das "Abstraction"-Pattern nur eine theoretische Trennung.

### Sicherheit
- **XXE-Schutz von Anfang an:** Beim Laden von XML-Dateien (insbesondere aus Benutzereingaben) ist XXE-Schutz Pflicht. Es ist eine 2-Stunden-Aufgabe, die massive Risiken vermeidet.

### Test-Disziplin
- **Kein Silent Skipping:** Tests, die bei fehlenden Dateien stillschweigend passen, sind keine Tests. Sie vermitteln ein falsches Sicherheitsgefuehl.
- **Tiefe Vergleiche:** Roundtrip-Tests, die nur Anzahlen vergleichen, finden keinen Datenverlust. Knoten, Kanten, Attribute muessen verglichen werden.
- **Pfad-Unabhaengigkeit:** Relative Pfade in Tests brechen in CI. Maven-Resources oder System-Properties verwenden.

### Code-Qualitaet
- **Keine dreifache Duplikation:** DOM-Helper-Logik existiert dreimal. Eine Quelle, drei Konsumenten.
- **Kein Dead Code:** Felder, Methoden und Klassen, die niemand verwendet, sollten entfernt werden. Sie verwirren Wartende.
- **Konsistente Exceptions:** `XMLSerializationException` im gesamten Modul, nicht `RuntimeException` an einer Stelle und `Exception` an einer anderen.

### Dokumentation
- **Ehrlichkeit ueber "100% Complete":** Phase 4 mit 28 thin-Wrapper-Adaptern ist nicht "100% Complete" -- es ist 30% (die Struktur steht, die Logik fehlt). Der Migrations-Tracker, der alle 96 Klassen als "Not Started" zeigt, ist naeher an der Wahrheit als die Phasen-Tabelle, die "Complete" sagt.

---

*Erstellt von: Mistral Vibe*
*Datum: 2026-09-13*
*Branch: review/014-XML-ex*
