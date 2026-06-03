# Jenkins local setup (Docker)

Jenkins is not bundled in this repository; use Docker for a local CI dashboard.

## URL and port

| Item | Value |
|------|--------|
| Dashboard URL | http://localhost:8080 |
| Port | **8080** (host) |
| Container name | `mystartup-jenkins` (recommended) |

## Start Jenkins with Docker

```powershell
docker volume create jenkins_home
docker run -d --name mystartup-jenkins -p 8080:8080 -p 50000:50000 -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts-jdk17
```

Initial admin password:

```powershell
docker exec mystartup-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

## Pipeline job

1. Install plugins: **Pipeline**, **SonarQube Scanner**, **JUnit**, **Git**.
2. Add SonarCloud credential `SonarCloud` (secret token) for the Jenkinsfile `withSonarQubeEnv('SonarCloud')` block.
3. Create a **Pipeline** job pointing at this repo’s root `Jenkinsfile`.
4. Mount Docker socket if the pipeline must run `docker compose` (Linux example):

   ```bash
   docker run ... -v /var/run/docker.sock:/var/run/docker.sock ...
   ```

## Stop / remove

```powershell
docker stop mystartup-jenkins
docker rm mystartup-jenkins
```
