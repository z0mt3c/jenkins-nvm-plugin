package org.jenkinsci.plugins.nvm;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;


  public class NvmWrapper extends BuildWrapper {

  private final static Logger LOGGER = Logger.getLogger(NvmWrapper.class.getName());

  private String version;
  private String nvmInstallURL = "https://raw.githubusercontent.com/creationix/nvm/v0.33.2/install.sh";
  private String nvmNodeJsOrgMirror = "https://nodejs.org/dist";
  private String nvmIoJsOrgMirror = "https://iojs.org/dist";
  private transient NvmWrapperUtil wrapperUtil;

  @DataBoundConstructor
  public NvmWrapper(String version, String nvmInstallURL, String nvmNodeJsOrgMirror, String nvmIoJsOrgMirror) {
    this.version = version;
    this.nvmInstallURL = StringUtils.isNotBlank(nvmInstallURL) ? nvmInstallURL : this.nvmInstallURL;
    this.nvmNodeJsOrgMirror = StringUtils.isNotBlank(nvmNodeJsOrgMirror) ? nvmNodeJsOrgMirror : this.nvmNodeJsOrgMirror;
    this.nvmIoJsOrgMirror = StringUtils.isNotBlank(nvmIoJsOrgMirror) ? nvmIoJsOrgMirror : this.nvmIoJsOrgMirror;
  }

  public String getVersion() {
    return version;
  }

  public String getNvmInstallURL() {
    return nvmInstallURL;
  }

  public String getNvmNodeJsOrgMirror() {
    return nvmNodeJsOrgMirror;
  }

  public String getNvmIoJsOrgMirror() {
    return nvmIoJsOrgMirror;
  }



  @Override
  public BuildWrapper.Environment setUp(AbstractBuild build, Launcher launcher,final BuildListener listener)
    throws IOException, InterruptedException {
    this.wrapperUtil = new NvmWrapperUtil(build, launcher, listener);
    final Map<String, String> npmEnvVars = this.wrapperUtil
      .getNpmEnvVars(this.version, this.nvmInstallURL, this.nvmNodeJsOrgMirror, this.nvmIoJsOrgMirror);

    return new BuildWrapper.Environment() {
      @Override
      public void buildEnvVars(Map<String, String> env) {

        EnvVars envVars = new EnvVars(env);
        envVars.putAll(npmEnvVars);
        env.putAll(envVars);
      }
    };
  }

  @Extension
  public final static class DescriptorImpl extends BuildWrapperDescriptor {
    @Override
    public String getDisplayName() {
      return "Run the build in an NVM managed environment";
    }

    @Override
    public boolean isApplicable(AbstractProject<?, ?> item) {
      return true;
    }

  }

}
