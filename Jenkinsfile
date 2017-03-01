script C:\Program Files (x86)\GnuWin32\bin\
//script /usr/bin/make
import jenkins.model.Jenkins
node{
stage('Build') { // <2>
        echo 'Hello'
        sh 'make.exe' // <3>
        archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
    }

}
