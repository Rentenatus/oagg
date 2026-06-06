# Optimierungsvorschläge für agg.xt_basis

## Erledigte Korrekturen (Consistency Fixes)

### 1. Parameter Namens-Konsistenz (src/tar → source/target)
- **Dateien**: AbstractGraphOrientation.java, Arc.java
- **Zeilen**: 
  - AbstractGraphOrientation.java: 38-45 (createArc Methode)
  - Arc.java: 42-47 (protected Konstruktor), 63-87 (public Konstruktor), 93-97 (copy Konstruktor)
- **Änderung**: Alle Parameter `src`, `tar` → `source`, `target`
- **Begründung**: Konsistenz mit Commit cc95476 (Improve parameter names)

### 2. Variablen Konsistenz (s_L, t_L, s_B, t_B → source_L, target_L, source_B, target_B)
- **Datei**: BaseFactory.java
- **Zeilen**: 569-591 (makeIPO Methode)
- **Status**: Bereits korrigiert - keine Vorkommen mehr gefunden

### 3. Variablen Konsistenz (a_minmax → otherMinmax)
- **Datei**: Arc.java
- **Methode**: compareMultiplicityTo (Zeile 360-390)
- **Status**: Bereits korrigiert - alle Vorkommen zu otherMinmax geändert

---

## Vorschläge für weitere Optimierungen

### A. Arc.java Optimierungen

#### A1. String-Concatenation standardisieren
**Aktuell**: Verwendung von `.concat()` an vielen Stellen
**Vorschlag**: Durch `+` Operator ersetzen für bessere Lesbarkeit

```java
// Aktuell (Zeile 60-62):
this.keyStr = this.itsSource.getType().convertToKey()
        .concat(this.itsType.convertToKey())
        .concat(this.itsTarget.getType().convertToKey());

// Vorschlag:
this.keyStr = this.itsSource.getType().convertToKey()
        + this.itsType.convertToKey()
        + this.itsTarget.getType().convertToKey();
```

**Betroffene Zeilen**: 60-62, 90-91, 204-205, 225-226, 249-250, 261-262

#### A2. keyStr Caching optimieren
**Problem**: `keyStr` wird in `convertToKey()`, `resetTypeKey()`, `setSource()`, `setTarget()` immer neu berechnet
**Vorschlag**: 
- `keyStr` nur berechnen, wenn sich Quelle oder Ziel ändert
- Oder: `keyStr` als transient markieren und lazy berechnen

**Betroffene Methoden**: convertToKey(), resetTypeKey(), setSource(), setTarget()

#### A3. JavaDoc vervollständigen
**Fehlende Dokumentation**:
- Methode `addToSrcTar()` (Zeile 100-107) - keine JavaDoc
- Methode `dispose()` (Zeile 110-122) - unvollständige JavaDoc
- Methode `compareSrcTarTo()` (Zeile 340-342) - keine JavaDoc
- Methode `compareMultiplicityTo()` (Zeile 356-390) - keine JavaDoc

---

### B. Graph.java Optimierungen

#### B1. Raw Types in Methodensignaturen
**Aktuell**: 
```java
public void setObservers(List<?> observers)  // Zeile 255
```
**Vorschlag**:
```java
public void setObservers(List<Observer> observers)
```

#### B2. @SuppressWarnings("rawtypes") reduzieren
**Aktuell**: Viele Klassen haben @SuppressWarnings("rawtypes")
**Vorschlag**: Generics explizit angeben, wo möglich

**Betroffene Dateien**: 
- Graph.java (kein @SuppressWarnings gefunden, aber raw types in internen Variablen)
- Andere Dateien in xt_basis haben viele @SuppressWarnings("rawtypes")

#### B3. Iterator-Variablen typisieren
**Aktuell**: 
```java
Iterator<?> iter = this.itsNodes.iterator();  // Zeile 531
```
**Vorschlag**:
```java
Iterator<Node> iter = this.itsNodes.iterator();
```

**Betroffene Zeilen**: 340, 366, 411, 452, 531, und viele mehr

#### B4. Unnötige Synchronization prüfen
**Aktuell**: Viele Methoden haben `synchronized` Blöcke
**Vorschlag**: Prüfen, ob Synchronization wirklich nötig ist, oder ob Collections.synchronized* verwendet werden sollte

**Beispiele**:
- Node.java: addOut(), addIn(), removeOut(), removeIn() (Zeilen 115-135)

#### B5. JavaDoc vervollständigen
**Fehlende Dokumentation**:
- Viele Methoden haben keine oder unvollständige JavaDoc
- Besonders betroffen: copy-Methoden, destroy-Methoden

---

### C. Node.java Optimierungen

#### C1. LinkedHashSet Initialisierung
**Aktuell**: 
```java
final protected LinkedHashSet<Arc> itsOutgoingArcs = new LinkedHashSet<>();
final protected LinkedHashSet<Arc> itsIncomingArcs = new LinkedHashSet<>();
```
**Vorschlag**: Direkt initialisieren (bereits so, kein Problem)

#### C2. addXYPosAttrs Methode
**Problem**: Duplizierter Code in zwei Konstruktoren (Zeile 45-46 und 53-54)
**Vorschlag**: Immer über den privaten Konstruktor aufrufen

**Aktuell**:
```java
public Node(Type type, Graph context) {
    // ...
    addXYPosAttrs(this.itsContext != null && this.itsContext.xyAttr);
    // ...
}

public Node(AttrInstance attr, Type type, Graph context) {
    // ...
    addXYPosAttrs(this.itsContext != null && this.itsContext.xyAttr);
    // ...
}
```

#### C3. String-Concatenation in toString()
**Aktuell**: 
```java
return " (" + "[" + hashCode() + "] " + "Node: " + typeStr + ") ";
```
**Vorschlag**: Konsistente Formatierung (bereits gut, kein Problem)

---

### D. AbstractGraphOrientation.java Optimierungen

#### D1. JavaDoc vervollständigen
**Fehlende Dokumentation**:
-Konstruktor (Zeile 27-29) - nur Kommentar, keine JavaDoc
- Viele Methoden haben unvollständige JavaDoc

#### D2. Implementierung prüfen
**Problem**: AbstractGraphOrientation hat viele abstrakte Methoden
**Vorschlag**: Prüfen, ob alle implementierenden Klassen (GraphOrientationDirected, GraphOrientationUndirected) alle Methoden korrekt implementieren

---

## Priorisierte Aufgabenliste

1. **Hoch**: Arc.java - String-Concatenation standardisieren (.concat() → +)
2. **Hoch**: Graph.java - Iterator-Variablen typisieren
3. **Mittel**: Alle Dateien - JavaDoc vervollständigen
4. **Mittel**: Graph.java - Raw Types in Methodensignaturen korrigieren
5. **Niedrig**: Arc.java - keyStr Caching optimieren
6. **Niedrig**: Node.java - Duplizierten Code in Konstruktoren bereinigen

---

## Agenten-Anweisungen

1. **Nächste Aufgabe**: Starte mit Arc.java String-Concatenation (Priorität 1)
2. **Erstelle Batch-Skripte** für jede Optimierung
3. **Teste nach jeder Änderung** ob der Code noch kompiliert
4. **Dokumentiere alle Änderungen** in dieser Datei

*Erstellt: 2026-06-03*
*Status: Bereit für Umsetzung*
