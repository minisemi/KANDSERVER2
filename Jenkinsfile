script /usr/bin/make
node{
stage('Build') { // <2>
        echo 'Hello'
        sh 'make' // <3>
        archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
    }

}
