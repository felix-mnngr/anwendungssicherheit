# anwendungssicherheit

## Systemanforderungen
1. Docker
2. Quarkus CLI
3. Node.js (inkl. npm)

## Build 
Alle Befehle beziehen sich auf das Projekt-Root-Verzeichnis
1. Build Webapp Frontend: `npm run build --prefix frontend`
2. Build Webapp Backend: `cd backend && quarkus build --no-tests && cd ..`
3. Start Docker Compose: `docker-compose up -d --build`

## Konfiguration
Nach dem initialen Start muss in der Keycloak [Admin-Console](http://localhost:8180/admin/master/console/#/anwendungssicherheit/users) ein User für das Realm _anwendungssicherheit_ angelegt werden. Anschließend kann dieser Benutzer zur Authentifizierung in der [Webapp](http://localhost:8080/) genutzt werden. 

## Troubleshouting
HTTP Status 401: 
- Seite neuladen
- Seite mit http://localhost:8080/ statt http://localhost:8080 aufrufen
