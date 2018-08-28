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


public class NvmWrapper extends BuildWrapper {


  private String version;
  private String nvmInstallURL;
  private String nvmNodeJsOrgMirror;
  private String nvmIoJsOrgMirror;
  private String nvmInstallDir;

  private transient NvmWrapperUtil wrapperUtil;

  @DataBoundConstructor
  public NvmWrapper(String version, String nvmInstallURL, String nvmNodeJsOrgMirror, String nvmIoJsOrgMirror,
                    String nvmInstallDir) {
    this.version = version;
    this.nvmInstallURL = StringUtils.isNotBlank(nvmInstallURL) ? nvmInstallURL : NvmDefaults.nvmInstallURL;
    this.nvmNodeJsOrgMirror = StringUtils.isNotBlank(nvmNodeJsOrgMirror) ? nvmNodeJsOrgMirror : NvmDefaults.nvmNodeJsOrgMirror;
    this.nvmIoJsOrgMirror = StringUtils.isNotBlank(nvmIoJsOrgMirror) ? nvmIoJsOrgMirror : NvmDefaults.nvmIoJsOrgMirror;
    this.nvmInstallDir = nvmInstallDir;
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

  public String getNvmInstallDir() {
    return nvmInstallDir;
  }

  @Override
  public BuildWrapper.Environment setUp(AbstractBuild build, Launcher launcher, final BuildListener listener)
    throws IOException, InterruptedException {
    this.wrapperUtil = new NvmWrapperUtil(build.getWorkspace(), launcher, listener);
    final Map<String, String> npmEnvVars = this.wrapperUtil
      .getNpmEnvVars(this.version, this.nvmInstallURL, this.nvmNodeJsOrgMirror, this.nvmIoJsOrgMirror,
                     this.nvmInstallDir);

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
