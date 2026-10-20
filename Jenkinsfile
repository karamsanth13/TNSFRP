pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
        timeout(time: 40, unit: 'MINUTES')
    }

    parameters {
        booleanParam(name: 'DEPLOY_TO_HOST', defaultValue: false, description: 'Deploy main to the configured Ansible target')
        string(name: 'DEPLOY_HOST', defaultValue: '', description: 'Linux deployment host (required when deployment is enabled)')
        string(name: 'DEPLOY_USER', defaultValue: 'ubuntu', description: 'SSH user for the Linux deployment host')
        string(name: 'PUBLIC_HOST', defaultValue: '', description: 'Browser-facing hostname/IP (defaults to DEPLOY_HOST)')
    }

    stages {
        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Backend tests') {
            steps {
                dir('backend') { sh 'mvn -B test' }
            }
        }

        stage('Frontend tests and build') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                    sh 'npm audit --audit-level=moderate'
                    sh 'npm test'
                    sh 'npm run build'
                }
            }
        }

        stage('Container build') {
            steps {
                sh 'docker compose --env-file .env.example build'
            }
        }

        stage('Validate deployment target') {
            when {
                allOf {
                    branch 'main'
                    expression { params.DEPLOY_TO_HOST }
                }
            }
            steps {
                script {
                    if (!(params.DEPLOY_HOST ==~ /^[A-Za-z0-9.-]+$/)) {
                        error('Set DEPLOY_HOST to a hostname or IPv4 address before enabling deployment.')
                    }
                    if (!(params.DEPLOY_USER ==~ /^[A-Za-z_][A-Za-z0-9_.-]*$/)) {
                        error('DEPLOY_USER contains unsupported characters.')
                    }
                }
            }
        }

        stage('Ansible deploy') {
            when {
                allOf {
                    branch 'main'
                    expression { params.DEPLOY_TO_HOST }
                }
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'tnsfrp-admin',
                    usernameVariable: 'TNSFRP_ADMIN_EMAIL',
                    passwordVariable: 'TNSFRP_ADMIN_PASSWORD'
                )]) {
                    withEnv(["TNSFRP_PUBLIC_HOST=${params.PUBLIC_HOST?.trim() ?: params.DEPLOY_HOST}"]) {
                        sshagent(credentials: ['tnsfrp-deploy-ssh']) {
                            sh '''
                                set -eu
                                export ANSIBLE_HOST_KEY_CHECKING=True
                                ansible-playbook -i "${DEPLOY_HOST}," -u "${DEPLOY_USER}" deploy/ansible/site.yml \\
                                  --extra-vars "deploy_ref=${GIT_COMMIT}"
                            '''
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            junit testResults: 'backend/target/surefire-reports/*.xml', allowEmptyResults: true
        }
    }
}
