def call(Map configMap){
    pipeline {
        agent {
            node {
                label 'AGENT-1' 
            } 
        }
        options {
            ansiColor('xterm')
            timeout(time: 1, unit: 'HOURS')
            disableConcurrentBuilds()
        }
        environment { 
            packageVersion = ''
            // can maintain in pipelineGlobals.groovy
            // nexusURL = '172.31.88.3:8081'
        }
        parameters {
            booleanParam(name: 'Deploy', defaultValue: false, description: 'Toggle this value')
        }
        // Build
        stages {
            stage('Get the app version') { 
                steps {
                    script {
                        def packageJson = readJSON file: 'package.json'  // variable initialisation using def
                        packageVersion = packageJson.version
                        echo "application_version: $packageVersion"
                    }
                }
            }
            stage('Install dependencies') { 
                steps {
                    sh """
                    npm install
                    """
                }
            }
            stage('unit testing') { 
                steps {
                    sh """
                    echo "unit tests will run here"
                    """
                }
            }
            stage('sonar-scan') { 
                steps {
                    sh """
                    sonar-scanner
                    """
                }
            }
            stage('Build') {
                steps {
                    sh """
                        ls -la
                        zip -q -r ${configMap.component}.zip ./* -x ".git" -x "*.zip"
                        ls -ltr
                    """
                }
            }
            stage('Publish Artifact') {
                steps {
                    nexusArtifactUploader(
                        nexusVersion: 'nexus3',
                        protocol: 'http',
                        nexusUrl: pipelineGlobals.nexusURL(),
                        groupId: 'com.roboshop',
                        version: "${packageVersion}",
                        repository: "${configMap.component}",
                        credentialsId: 'nexus_auth',
                        artifacts: [
                            [artifactId: "${configMap.component}",
                            classifier: '',
                            file: "${configMap.component}.zip",
                            type: 'zip']
                        ]
                    )
                }
            }
            stage ('Invoke_pipeline') {
                when {
                    expression {
                        params.Deploy
                    }
                }
                steps {
                    script{
                        def params = [
                            string(name: 'version', value: "${packageVersion}"),
                            string(name: 'environment', value: "dev")
                            ]
                        build job: 'catalogue-deploy', wait: true, parameters: params
                    }
                }
            }
        }
        // Post Build
        post { 
            always { 
                echo 'I will always say Hello again!'
                deleteDir()
            }
            failure { 
                echo 'I will run when the job has failed!'
            }
            success { 
                echo 'I will run when the job is success!'
            }
            aborted { 
                echo 'I will run when the job is aborted manually!'
            }
        }
    }
}