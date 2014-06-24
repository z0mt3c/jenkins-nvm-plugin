# Jenkins nvm Plugin (NvmWrapper)

A java/groovy version of [Jenkins nvm plugin](https://github.com/codevise/jenkins-nvm-plugin), it
doesn't require `ruby-runtime` to be installed.

## Usage
- Configure node version to use
- Avoid using `npm -g` to install global packages instead just use `npm install'
    * Ej: To Run Run Gulp do in a shell command:
     
    
     ```bash
      cd src/gulp;
      npm install;
      node node_modules/gulp/bin/gulp.js
      ```
                                                                           
## Build

- 'gradle jpi' - Build the Jenkins plugin file, which can then be
  found in the build directory. The file will currently end in ".hpi".
- 'gradle install' - Build the Jenkins plugin and install it into your
  local Maven repository.
- 'gradle uploadArchives' (or 'gradle deploy') - Deploy your plugin to
  the Jenkins Maven repository to be included in the Update Center.
- 'gradle server' - Run a local jenkins to test

## Features

- Installs `nvm.sh`
- Installs node version configured for job.
- Amends build environment to use configured node version.

## Acknowledgements

Based on :

[Jenkins rvm plugin](https://github.com/jenkinsci/rvm-plugin) and
[Jenkins nvm plugin](https://github.com/codevise/jenkins-nvm-plugin).

## License

Please fork and improve.
