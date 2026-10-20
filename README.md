# TNSFRP — Tamil Nadu Food Rescue & Redistribution Platform

A web app that connects food donors with NGOs. The frontend is React/Vite, the API is Java 17+ / Spring Boot, and the database is MongoDB.

## Run locally with Docker Compose

Requirements: Docker Engine with the Compose plugin.

1. Copy `.env.example` to `.env` and set a unique admin password with at least 16 letters, digits, underscores, or hyphens.
2. Start the app:

   ```sh
   docker compose up --build -d
   ```

3. Open <http://localhost:8082>. The database is persisted in the `mongo-data` volume.
4. Stop it with `docker compose down`; to also erase the local database, use `docker compose down --volumes`.

Do not commit `.env`. The example password is a placeholder and is rejected by the Ansible deployment if used unchanged.

## Run in development mode

Requirements: Java 17+, Maven 3.8+, Node.js 20.19+ (or 22.12+), and MongoDB on `localhost:27017`.

PowerShell, in separate terminals:

```powershell
cd backend
$env:ADMIN_EMAIL = "admin@example.com"
$env:ADMIN_PASSWORD = "Use-A-Unique-Local-Password"
mvn spring-boot:run
```

```powershell
cd frontend
cmd.exe /c "npm.cmd ci"
cmd.exe /c "npm.cmd run dev -- --host 127.0.0.1"
```

Open <http://127.0.0.1:5173>. Vite proxies `/api` to Spring Boot on port 8080.

## Tests

```sh
(cd backend && mvn -B test)
(cd frontend && npm ci && npm test && npm run build)
```

The GitHub Actions workflow runs backend tests and the frontend tests/build for pushes and pull requests. Jenkins runs those checks and builds both container images.

## Jenkins CI and deployment

Create a Jenkins Multibranch Pipeline job for this repository using `Jenkinsfile`. The Jenkins agent needs Java 21+, Maven, Node.js 22.12+, npm, Docker Compose, Ansible, the Pipeline, Credentials Binding, Git, JUnit, and SSH Agent plugins. Jenkins must be able to use Docker to build images.

For a deployment job, configure:

- Jenkins SSH private key credential ID `tnsfrp-deploy-ssh`; trust the deployment host key in the Jenkins agent's `known_hosts`.
- Jenkins Username with password credential ID `tnsfrp-admin`, containing the deployment admin email and a unique password of at least 16 letters, digits, underscores, or hyphens.
- Run the pipeline on the `main` branch and set `DEPLOY_TO_HOST` to true with `DEPLOY_HOST` and `DEPLOY_USER` set. Set `PUBLIC_HOST` if users will reach the app at a different hostname/IP. The remote account needs passwordless sudo for package setup and Docker operations.

The deploy stage is off by default. Every deployment checks out the exact tested commit, writes credentials to a mode-0600 server-side `.env`, starts Docker Compose, and checks the frontend response. Configure a GitHub webhook to Jenkins only when the Jenkins URL is reachable by GitHub; otherwise use polling or manual builds. GitHub Actions provides hosted CI independently.

## Ansible deployment

Ansible runs from Linux, WSL, or macOS. Copy `deploy/ansible/inventory.ini.example` to `deploy/ansible/inventory.ini`, set a reachable Ubuntu/Debian host and SSH user, and ensure its SSH host key is trusted. Then provide the admin values through the environment and deploy:

```sh
export TNSFRP_ADMIN_EMAIL=admin@example.com
export TNSFRP_ADMIN_PASSWORD='a-unique-random-password-of-at-least-16-characters'
ansible-playbook -i deploy/ansible/inventory.ini deploy/ansible/site.yml
```

The playbook installs Docker Compose and Git, checks out `main`, deploys the stack, and waits for `/login` to return HTTP 200. Override the tested revision with `-e deploy_ref=<commit-sha>`, the public port with `-e app_port=8082`, and set `TNSFRP_PUBLIC_HOST` to the browser-facing hostname or IP. Do not place credentials in inventory or command-line arguments.

## GitHub repository setup

The repository is at <https://github.com/karamsanth13/TNSFRP>. Push the `main` branch to trigger hosted CI. Add branch protection requiring the `test` job before merging.

## Project layout

```text
backend/             Spring Boot API and unit tests
frontend/            React app, tests, and Nginx container config
deploy/ansible/       Ansible deployment playbook and inventory example
.github/workflows/    GitHub Actions CI
Jenkinsfile           Jenkins test, container build, and optional deploy pipeline
compose.yaml          MongoDB + API + frontend stack
```
