# anwendungssicherheit
Clone: `git clone https://github.com/felix-mnngr/anwendungssicherheit.git`

## Systemanforderungen
1. [Docker](https://docs.docker.com/get-docker/)
2. [Java (JRE)](https://www.java.com/de/download/manual.jsp)
3. [Quarkus CLI](https://quarkus.io/get-started/)
4. [Node.js (inkl. npm)](https://nodejs.org/de)

## Build 
Alle Befehle beziehen sich auf das Projekt-Root-Verzeichnis und sind für Bash (Linux) oder CMD (Windows)
1. Install npm packages: `npm install --prefix frontend`
2. Build Webapp Frontend: `npm run build --prefix frontend`
3. Build Webapp Backend: `cd backend && quarkus build --no-tests && cd ..`
4. Start Docker Compose: `docker-compose up -d --build`

## Keycloak Admin
- **Benutzername:** admin
- **Passwort:** admin123

Benutzername und Passwort können im docker-compose.yml geändert werden.

## Konfiguration
Nach dem initialen Start muss in der Keycloak [Admin-Console](http://localhost:8180/admin/master/console/#/anwendungssicherheit/users) ein User für das Realm _anwendungssicherheit_ angelegt werden. Anschließend kann dieser Benutzer zur Authentifizierung in der [Webapp](http://localhost:8080/) genutzt werden. 

## Troubleshouting
Build Errors: Nach der Neuinstallation von Docker, Java, Quarkus und Node.js sollte der PC neugestartet werden.

Docker Probleme: 
1. Compose entfernen: `docker-compose down`
2. Volumes löschen: `docker volume prune`
3. Compose starten: `docker-compose up -d`

HTTP Status 401: 
- Seite neuladen
- Seite mit http://localhost:8080/ statt http://localhost:8080 aufrufen
