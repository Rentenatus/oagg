# AGG Code Optimierung - TODO Liste

## Legende
- [.] = Offene Aufgabe (pending)
- [d] = Erledigte Aufgabe (done)

## Priorität 1 - Kritische Korrekturen (ALLE ERLEDIGT)

[d] 1. AbstractGraphOrientation zum Arc ins selbe package verschoben
    - Erledigt in Commit fbdaa9c
    - Agenten-Anweisung: Keine Aktion nötig

[d] 2. AbstractGraphOrientation.java Zeile 38-45: Parameter src/tar → source/target umbenannt
    - Konsistenz mit Commit cc95476 (Improve parameter names)
    - Geänderte Dateien: AbstractGraphOrientation.java (createArc Methode)
    - Agenten-Anweisung: ERLEDIGT

[d] 3. Arc.java: Alle Konstruktoren - Parameter src/tar → source/target umbenannt
    - Betroffene Konstruktoren: Zeile 42-47, 63-87, 93-97
    - Agenten-Anweisung: ERLEDIGT

[d] 4. BaseFactory.java Zeile 569-591: s_L/t_L/s_B/t_B → source_L/target_L/source_B/target_B
    - Status: Bereit erledigt - keine Vorkommen mehr gefunden
    - Agenten-Anweisung: ERLEDIGT

[d] 5. Arc.java: a_minmax → otherMinmax in compareMultiplicityTo
    - a_minmax war alter Variablenname (Commit 60c6d9f)
    - Status: Erledigt - alle Vorkommen zu otherMinmax geändert
    - Agenten-Anweisung: ERLEDIGT

## Priorität 2 - Code Qualitätsverbesserungen (OFFEN)

[.] 6. Arc.java: String-Concatenation standardisieren (.concat() → + Operator)
    - Betroffene Zeilen: 60-62, 90-91, 204-205, 225-226, 249-250, 261-262
    - Agenten-Anweisung: STARTE HIER - alle .concat() Aufrufe in String-Concatenation durch + ersetzen

[.] 7. Graph.java: Iterator-Variablen typisieren
    - Betroffene Zeilen: 340, 366, 411, 452, 531, und viele mehr
    - Beispiel: Iterator<?> → Iterator<Node> oder Iterator<Arc>
    - Agenten-Anweisung: Durchsuchen und alle Iterator<?>-Deklarationen mit konkreten Typen ersetzen

[.] 8. Alle Dateien: JavaDoc vervollständigen
    - Arc.java: addToSrcTar, dispose, compareSrcTarTo, compareMultiplicityTo
    - Graph.java: viele copy-Methoden, destroy-Methoden
    - Node.java: verschiedene Methoden
    - AbstractGraphOrientation.java: Konstruktor, abstrakte Methoden
    - Agenten-Anweisung: JavaDoc für alle öffentlichen und geschützten Methoden ergänzen

[.] 9. Graph.java: Raw Types in Methodensignaturen korrigieren
    - Beispiel: setObservers(List<?> observers) → setObservers(List<Observer> observers)
    - Agenten-Anweisung: Alle Methodensignaturen mit raw types oder wildcards auf konkrete Generics prüfen

[.] 10. Arc.java: keyStr Caching optimieren
    - Problem: keyStr wird in convertToKey(), resetTypeKey(), setSource(), setTarget() immer neu berechnet
    - Vorschlag: Lazy computation oder nur bei Änderungen berechnen
    - Agenten-Anweisung: Analyse durchführen und optimierte Implementierung vorschlagen

[.] 11. Node.java: Duplizierten Code in Konstruktoren bereinigen
    - Problem: addXYPosAttrs() wird in zwei Konstruktoren aufgerufen
    - Vorschlag: Code in einen gemeinsamen privaten Initialisierungsmethoden auslagern
    - Agenten-Anweisung: Konstruktoren refaktorieren

## Priorität 3 - Dokumentation

[.] 12. OPTIMIZATIONS_xt_basis.md pflegen
    - Alle Optimierungsvorschläge dokumentieren
    - Status jeder Optimierung tracken
    - Agenten-Anweisung: Datei bereits erstellt, weiter pflegen

---

## Zusammenfassung

### Erledigt: 5 Aufgaben (Priorität 1)
- Package-Verschiebung
- Parameter-Namens-Konsistenz (src/tar → source/target)
- Variablen-Konsistenz (s_L/t_L → source_L/target_L)
- Variablen-Konsistenz (a_minmax → otherMinmax)

### Offen: 7 Aufgaben (Priorität 2-3)
- String-Concatenation
- Iterator-Typisierung
- JavaDoc
- Raw Types
- Caching
- Code-Duplizierung
- Dokumentation

## Nächste Schritte

1. **Nächste Aufgabe**: Aufgabe 6 (Arc.java String-Concatenation)
2. **Agenten-Anweisung**: STARTE MIT AUFGABE 6 - Ersetze alle .concat() Aufrufe in Arc.java durch + Operator
3. Nach jeder Aufgabe: Kompilieren testen, dann nächste Aufgabe
4. Erledigte Aufgaben mit [d] markieren

---
*Erstellt von: Mistral Vibe Agent*
*Letzte Aktualisierung: 2026-06-03*
*Status: Priorität 1 abgeschlossen, Priorität 2 in Bearbeitung*
