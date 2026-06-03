# Jenkins — conteneur `mystartup-jenkins`

## Accès (dashboard web)

| Élément | Valeur |
|---------|--------|
| **URL** | **http://localhost:8085** |
| **Port hôte → conteneur** | `8085:8080` (Jenkins écoute sur **8080** dans le conteneur) |
| **Nom du conteneur** | `mystartup-jenkins` |
| **Fichier** | `docker-compose.yml` (service `jenkins`) |

> **Ne pas ouvrir** http://localhost:50000/ pour l’interface : le port **50000** sert aux **agents Jenkins** (protocole JNLP), pas au navigateur.

## Démarrage

```powershell
cd C:\Users\eswatini\Documents\MyStartUp_PFA
docker compose up -d jenkins
```

Vérifier :

```powershell
docker ps --filter name=mystartup-jenkins
docker logs mystartup-jenkins --tail 30
```

## Premier accès

1. Ouvrir **http://localhost:8085**
2. Mot de passe initial :

```powershell
docker exec mystartup-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

3. Assistant d’installation → plugins → utilisateur admin.

## Pipeline

- Script : `Jenkinsfile` à la racine du dépôt.
- Credential SonarCloud : ID `SonarCloud` (comme dans le Jenkinsfile).

## Arrêt

```powershell
docker compose stop jenkins
```
