pipeline {
    agent any

    // Tool declarations: Uncomment and adjust names if configured in Jenkins > Manage Jenkins > Tools
    // tools {
    //     maven 'Maven-3.9.x'
    //     jdk 'JDK-21'
    // }

    options {
        // Keep the last 10 builds to conserve disk space
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Abort build if it takes more than 15 minutes
        timeout(time: 15, unit: 'MINUTES')
        // Print timestamps in console log
        timestamps()
    }

    environment {
        APP_NAME = 'inventory-management-system'
        ARTIFACT_PATH = 'target/*.jar'
        TEST_REPORTS_PATH = '**/target/surefire-reports/*.xml'
    }

    stages {
        // =====================================================================
        // STAGE 1: CHECKOUT
        // =====================================================================
        stage('Checkout') {
            steps {
                echo '======================================================'
                echo ' [STAGE 1/7] Checking out source code from Git SCM... '
                echo '======================================================'
                // Clones/pulls the code from the Git repository configured in the Jenkins Job
                checkout scm
            }
        }

        // =====================================================================
        // STAGE 2: ENVIRONMENT VERIFICATION
        // =====================================================================
        stage('Environment Verification') {
            steps {
                echo '======================================================'
                echo ' [STAGE 2/7] Verifying Java, Maven, and Git Tooling...'
                echo '======================================================'
                script {
                    if (isUnix()) {
                        sh 'java -version'
                        sh 'mvn -version'
                        sh 'git --version'
                    } else {
                        bat 'java -version'
                        bat 'mvn -version'
                        bat 'git --version'
                    }
                }
            }
        }

        // =====================================================================
        // STAGE 3: CLEAN
        // =====================================================================
        stage('Clean') {
            steps {
                echo '======================================================'
                echo ' [STAGE 3/7] Cleaning previous build workspace/target '
                echo '======================================================'
                script {
                    if (isUnix()) {
                        sh 'mvn clean'
                    } else {
                        bat 'mvn clean'
                    }
                }
            }
        }

        // =====================================================================
        // STAGE 4: COMPILE
        // =====================================================================
        stage('Compile') {
            steps {
                echo '======================================================'
                echo ' [STAGE 4/7] Compiling Java source code into bytecode '
                echo '======================================================'
                script {
                    if (isUnix()) {
                        sh 'mvn compile'
                    } else {
                        bat 'mvn compile'
                    }
                }
            }
        }

        // =====================================================================
        // STAGE 5: TEST
        // =====================================================================
        stage('Test') {
            steps {
                echo '======================================================'
                echo ' [STAGE 5/7] Executing JUnit 5 automated unit tests...'
                echo '======================================================'
                script {
                    if (isUnix()) {
                        sh 'mvn test'
                    } else {
                        bat 'mvn test'
                    }
                }
            }
            post {
                always {
                    echo 'Publishing JUnit XML test report in Jenkins...'
                    // Parses Surefire XML test reports for test trend graphs and reporting
                    junit testResults: env.TEST_REPORTS_PATH, allowEmptyResults: false
                }
            }
        }

        // =====================================================================
        // STAGE 6: PACKAGE
        // =====================================================================
        stage('Package') {
            steps {
                echo '======================================================'
                echo ' [STAGE 6/7] Packaging application into executable JAR '
                echo '======================================================'
                // Tests have already passed in the Test stage; use -DskipTests to avoid redundant execution
                script {
                    if (isUnix()) {
                        sh 'mvn package -DskipTests'
                    } else {
                        bat 'mvn package -DskipTests'
                    }
                }
            }
        }

        // =====================================================================
        // STAGE 7: ARCHIVE ARTIFACTS
        // =====================================================================
        stage('Archive Artifacts') {
            steps {
                echo '======================================================'
                echo ' [STAGE 7/7] Archiving generated executable JAR file  '
                echo '======================================================'
                // Archive the generated JAR artifact so it can be downloaded directly from Jenkins UI
                archiveArtifacts artifacts: env.ARTIFACT_PATH, fingerprint: true, allowEmptyArchive: false
            }
        }
    }

    // =========================================================================
    // POST-BUILD ACTIONS & REPORTING
    // =========================================================================
    post {
        success {
            echo '=========================================================================='
            echo '   BUILD SUCCESSFUL: All stages passed, tests green, artifact archived!   '
            echo '=========================================================================='
        }
        failure {
            echo '=========================================================================='
            echo '   BUILD FAILED: One or more stages failed. Inspect console output above. '
            echo '=========================================================================='
        }
        unstable {
            echo '=========================================================================='
            echo '   BUILD UNSTABLE: Build completed with test failures. Review JUnit tabs. '
            echo '=========================================================================='
        }
        always {
            echo 'CI pipeline execution finished.'
        }
    }
}
