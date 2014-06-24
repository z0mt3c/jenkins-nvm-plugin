package org.jenkinsci.plugins.nvm

import hudson.AbortException
import hudson.Extension
import hudson.FilePath
import hudson.Launcher
import hudson.model.AbstractBuild
import hudson.model.AbstractProject
import hudson.model.BuildListener
import hudson.tasks.BuildWrapper
import hudson.tasks.BuildWrapperDescriptor
import hudson.util.ArgumentListBuilder
import org.kohsuke.stapler.DataBoundConstructor

import java.util.logging.Logger

/**
 * @author Tomas Salazar
 */
public class NvmWrapperUtil {

  private AbstractBuild build;
  private Launcher launcher;
  private BuildListener listener;
  private String nvmPath;

  NvmWrapperUtil(AbstractBuild build, Launcher launcher, BuildListener listener) {
    this.build = build
    this.listener = listener
    this.launcher = launcher
  }

  public Map getVars(String version) throws IOException, InterruptedException {
    if (!isNvmInstalled()) {
      installNvm()
      isNvmInstalled()
    }

    ArgumentListBuilder beforeCmd = new ArgumentListBuilder()
    beforeCmd.add("bash")
    beforeCmd.add("-c")
    beforeCmd.add("export > before.env")

    def beforeEnv = toMap(getExport(beforeCmd, "before.env"))

    //listener.getLogger().println "before env ${beforeEnv}"

    ArgumentListBuilder nvmSourceCmd = new ArgumentListBuilder()
    nvmSourceCmd.add("bash")
    nvmSourceCmd.add("-c")
    nvmSourceCmd.add(" source ${nvmPath} && nvm install ${version} && nvm use ${version} && export > nvm.env")

    def afterEnv = toMap(getExport(nvmSourceCmd, "nvm.env"))

    def newEnvVars = [:]
    afterEnv.each { k, v ->

      def beforeValue = beforeEnv.get(k)
      if (v != beforeValue) {

        if (k == "PATH") {
          def path = v.split(File.pathSeparator).findAll { it =~ /[\\\/]\.nvm[\\\/]/ }.join(File.pathSeparator)
          newEnvVars.put("PATH", afterEnv.get("PATH"))
          newEnvVars.put("PATH+NVM", path)
        } else {
          newEnvVars.put(k, v)
        }

      }
    }

      listener.getLogger().println "env PATH ${newEnvVars.get("PATH")}"
      listener.getLogger().println "env PATH+NVM ${newEnvVars.get("PATH+NVM")}"


    return newEnvVars
  }

  private String getExport(ArgumentListBuilder args, String destFile) {

    def sc = launcher.launch().pwd(build.workspace).cmds(args)
      .stdout(listener.getLogger())
      .stderr(listener.getLogger()).join()

    if (sc != 0) {
      new AbortException("Failed to fork bash [${args.dump()}]")
    }

    build.workspace.child(destFile).readToString()

  }

  private Boolean isNvmInstalled() {

    def installed = false

     this.nvmPath = ["~/.nvm/nvm.sh", "/usr/local/nvm/nvm.sh"].find { path ->
      ArgumentListBuilder args = new ArgumentListBuilder()
      args.add("bash")
      args.add("-c")
      args.add("test -f ${path}")
      launcher.launch().cmds(args)
        .stdout(listener.getLogger())
        .stderr(listener.getLogger()).join() == 0

    }
  }

  private Integer installNvm() {
    listener.println "Installing nvm\n"
    FilePath installer = build.workspace.child("nvm-installer")
    installer.copyFrom("https://raw.github.com/creationix/nvm/master/install.sh".toURL())
    installer.chmod(0755)
    ArgumentListBuilder args = new ArgumentListBuilder()

    args.add(installer.absolutize().getRemote())

    int sc = launcher.launch().cmds(args).pwd(build.workspace)
      .stdout(listener.getLogger())
      .stderr(listener.getLogger()).join()

    return sc
  }

  private Map toMap(String export) {
    def r = [:]
    export.readLines().each { line ->

      def entry = line.replaceAll("declare -x ", "").split("=")
      if (entry.size() == 2) {
        r.put(entry[0], entry[1].replaceAll('"', ''))
      }

    }
    return r
  }


}


