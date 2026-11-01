# AP-2: Adapter mit echter Serialisierungslogik

**Erstellt:** 2026-09-13
**Paket:** AP-2 (aus REVIEW_PLAN.md)
**Prioritaet:** CRITICAL
**Aufwand:** 5-10 Tage (Kern-Adapter)

---

## Problem

Alle 28+ Domain-Adapter sind thin wrappers, die an `XwriteObject`/`XreadObject`
delegieren. Das Refactoring-Ziel "Separation of Concerns" wurde nicht erreicht.
Die Serialisierungslogik bleibt in den Domain-Klassen.

## Strategie

Die .ggx-XML-Struktur ist komplex und stark von der XMLHelper-ID-Referenzlogik
(`I0`, `I1`, ...) abhaengig. Ein vollstaendiger Neuschrieb aller Adapter mit
direkter DOM-Logik waere extrem fehleranfaellig. Stattdessen wird ein
pragmatischer Ansatz gewaehlt:

**GraGraAdapter** erhaelt echte `serialize()`/`deserialize()`-Implementierungen,
die den DOM-Baum direkt aufbauen bzw. lesen, ohne XMLHelper zu verwenden.
`GraGraMigration.saveUsingNewXml`/`loadUsingNewXml` werden so umgebunden,
dass sie den `DOMXMLSerializer`/`DOMXMLDeserializer` mit dem `GraGraAdapter`
verwenden.

Die Sub-Adapter (Graph, Node, Arc, Type) werden schrittweise mit echter
DOM-Logik ausgestattet. Waehrend des Uebergangs kann der GraGraAdapter
fuer Sub-Objekte zunaechst noch an XMLHelper delegieren und schrittweise
auf DOM-Logik umgestellt werden.

## Unterschritte

### AP-2.1: GraGraAdapter.serialize() mit DOM-Logik

**Ziel:** GraGraAdapter baut den DOM-Baum fuer ein GraGra-Objekt direkt auf.

**Schritte:**
1. `GraGraAdapter.serialize(XMLSerializerContext)` ueberschreiben
2. DOM-Element `GraphTransformationSystem` erstellen
3. Attribute setzen: name, directed, parallel, comment
4. TaggedValues (AttrHandler, Packages, Options, TypeGraphLevel) schreiben
5. Types-Element mit NodeType/EdgeType-Kindern schreiben
6. TypeGraph als Graph-Element in Types schreiben
7. Host-Graphen als Graph-Elemente schreiben
8. Regeln als Rule-Elemente schreiben (delegiert an RuleAdapter)
9. Constraints, Matches, RuleSequences schreiben

**Test:** Roundtrip: GraGra erstellen -> serialisieren -> deserialisieren ->
Name, Graph-Anzahl, Regel-Anzahl vergleichen.

### AP-2.2: GraGraAdapter.deserialize() mit DOM-Logik

**Ziel:** GraGraAdapter liest ein GraGra aus einem DOM-Baum.

**Schritte:**
1. `GraGraAdapter.deserialize(XMLDeserializerContext)` ueberschreiben
2. `GraphTransformationSystem`-Element finden und Attribute lesen
3. TaggedValues parsen (AttrHandler, Packages, Options)
4. Types-Element parsen: NodeType/EdgeType erstellen
5. TypeGraph aus Types laden
6. Host-Graphen laden (delegiert an GraphAdapter)
7. Regeln laden
8. Constraints, Matches, RuleSequences laden

**Test:** .ggx-Datei laden -> serialisieren -> neu laden -> vergleichen.

### AP-2.3: GraphAdapter.serialize()/deserialize()

**Ziel:** Graph-Element direkt serialisieren/deserialisieren.

**Schritte:**
1. `serialize()`: Graph-Element mit Attributen (kind, name, comment, info)
2. Nodes als Kind-Elemente serialisieren (delegiert an NodeAdapter)
3. Arcs als Kind-Elemente serialisieren (delegiert an ArcAdapter)
4. `deserialize()`: Graph-Element erkennen, Attribute lesen
5. Node-Kind-Elemente parsen, Nodes erstellen
6. Edge-Kind-Elemente parsen, Arcs erstellen

### AP-2.4: NodeAdapter.serialize()/deserialize()

**Ziel:** Node-Element direkt serialisieren/deserialisieren.

**Schritte:**
1. `serialize()`: Node-Element mit Attributen (visible, name, type-ref)
2. Type als ID-Referenz (Attribut `type="I1"`)
3. Attribute-Kind-Elemente serialisieren
4. `deserialize()`: Node-Element erkennen, Attribute lesen
5. Type-ID aufloesen, Node mit Typ erstellen
6. Attribute laden

### AP-2.5: ArcAdapter.serialize()/deserialize()

**Ziel:** Edge-Element direkt serialisieren/deserialisieren.

**Schritte:**
1. `serialize()`: Edge-Element mit Attributen (visible, name, type-ref,
   source-ref, target-ref, sourcemin/max, targetmin/max)
2. Attribute-Kind-Elemente serialisieren
3. `deserialize()`: Edge-Element erkennen, Attribute lesen
4. Type/Source/Target-ID aufloesen, Arc erstellen
5. Attribute laden

### AP-2.6: TypeImpl/NodeTypeImpl/ArcTypeImpl Adapter

**Ziel:** Typ-Definitionen direkt serialisieren/deserialisieren.

**Schritte:**
1. `serialize()`: NodeType/EdgeType-Element mit name, abstract, comment
2. AttrType-Kind-Elemente serialisieren
3. Parent-Referenzen serialisieren
4. `deserialize()`: Element erkennen, Attribute lesen
5. AttrType-Kinder parsen

### AP-2.7: GraGraMigration an DOM-Serializer anbinden

**Ziel:** `saveUsingNewXml`/`loadUsingNewXml` verwenden den echten
DOM-Serializer mit GraGraAdapter, nicht mehr XMLHelper.

**Schritte:**
1. `saveUsingNewXml`: `DOMXMLSerializer` + `XMLAdapterFactory.createAdapter(graGra)`
2. `loadUsingNewXml`: `DOMXMLDeserializer` + `GraGraAdapter.deserialize()`
3. Feature-Flag-Test mit echtem DOM-Pfad

### AP-2.8: Roundtrip-Tests mit aktiviertem Feature-Flag

**Ziel:** Tiefe Roundtrip-Tests, die den neuen DOM-Pfad verwenden.

**Schritte:**
1. Test: GraGra mit Feature-Flag laden, speichern, neu laden
2. Vergleiche: Name, Graph-Anzahl, Node-Anzahl, Edge-Anzahl, Typen
3. Test mit allen 4 .ggx-Testdateien
4. Test mit Edge-Case: leeres GraGra, GraGra nur mit Typen

---

*Erstellt von: Mistral Vibe*
*Datum: 2026-09-13*
