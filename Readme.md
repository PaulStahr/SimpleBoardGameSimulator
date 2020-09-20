[![Build Status](https://api.travis-ci.com/PaulStahr/SimpleBoardGameSimulator.svg?branch=master)](https://travis-ci.com/github/PaulStahr/SimpleBoardGameSimulator)

Zum einfachen Ausführen des Spieles bitte die Datei https://github.com/PaulStahr/SimpleBoardGameSimulator/blob/master/SimpleBoardGameSimulator.jar herunterladen und starten.

Server:
Verwaltet eine Liste von Spielen und Spielern. Bietet auch die Möglichkeit eine Spielsession zu verwalten, was auch der Standardweg ein Spiel miteinander zu spielen sein wird.

Game:
Beinhaltet alle für das Spiel relevanten Daten

GameInstance:
Beinhaltet die Art des Spiels (Game) und die aktuelle Konfiguration

GameObject
Beinhaltet die Informationen für einen Spielobjekttyp (Beispielsweise Herz 10)

GameObjectInstance
Beinhaltet ein Objekt das sich im Spielfeld befindet

Object State
Beinhaltet die aktuelle Konfiguration eines Objektes welches sich im Spielfeld befindet. Kann für einzelne Objekte weiter spezifiziert werden (Beispielsweise kann eine Karte auf verschiedenen Seiten liegen)

AsynchronousGameConnection
Stellt die Verbindung zwischen zwei Spielinstanzen her, womit sie idealerweise von außen wie ein und dieselbe Instanz behandelt werden können. Beide Seiten werden dabei symmetrisch behandelt.

Manuelles starten eines Servers und verbinden mit beliebig vielen clienten Clienten:
Server starten: java -jar SimpleBoardGameSimulator.jar --server 1234
Client Sitzung erstellen und verbinden: java -jar SimpleBoardGameSimulator.jar --create 127.0.0.1 1234 Spieler1 0
Weitere clienten verbinden: java -jar SimpleBoardGameSimulator.jar --join 127.0.0.1 1234 Spieler2 0

Dateiformat:
Grundsätzlich wird als Austauschformat für die Daten Xml verwendet. Um mehrere daten gemeinsam zu versenden oder zu speichern werden Zip-Container verwendet. Die Klasse, welche für das codieren zuständig ist ist die GameIO.
Ein Spielsnapshot wird als Zip-Datei gespeichert. Die wichtigsten Dateien sind die game.xml, welche die allgemeine Definition des Spieles enthält und einer game_instance.xml welche den Status also die veränderlichen Daten des Spieles enthält. Desweiteren können Bilder in einem von java unterstützten Format abgespeichert werden.

Allgemeines Modell:
Spieler verbinden sich zu einem Server, welcher alle Spielsessions verwaltet. Daten an und vom Server werden über direkte Commandos oder Zip-Archive versendet. Spiele besitzen für den gesicherten Zugriff ein Passwort. Der eigene Spielernamen ist Teil der Anfrage.

Verbindungen außerhalb eines Spiels sind fürs erste synchron geplant (Spiele auflisten, ...)

Die Verbindung bei einem laufenden Spiel ist dagegen asynchron, jede Verbindong besitzt einen Thread zum Schreiben in den Stream und einem zum Lesen von diesem. Der Client kann somit Anfragen stellen oder auch Zustandsänderungen bewirken. Am Beginn der Nachricht steht immer der Spieler, der Befehl eventuell gefolgt von einem Zip-Archiv für größere Datenblöcke. Die Antworten kommen idealerweise aber nicht zwingend in der selben Reihenfolge, mit anderen Events, die dazwischen aufgetreten sind zurück. Server und Client verhalten sich im wesentlichen symmetrisch, bis auf den Unterschied, dass der Server keinen Spieler zugeordnet hat und somit nicht in der Lage ist selbst Events zu erzeugen. Die Netwerkverbindung buffert selbstständing Updates die im Spiel auftraten, bis der Sende-Thread diese abschicken kann.

Auf Programmierebene stellt die AsynchronousGameConnection somit sicher, dass sich zwei GameInstances immer in demselben Zustand befinden. Wird ein Object geändert wird durch das aufrufen der Update-Funktion auch ein Update in der verbundenen Instanz ausgelößt.
