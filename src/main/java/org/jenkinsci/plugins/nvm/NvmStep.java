package org.jenkinsci.plugins.nvm;

import com.google.common.collect.ImmutableSet;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.TaskListener;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;
import java.util.Set;


public class NvmStep extends Step {

  private final @Nonnull String version;
  private String nvmInstallURL;
  private String nvmNodeJsOrgMirror;
  private String nvmIoJsOrgMirror;
  private String nvmInstallDir;;
  private String nvmInstallNodeVersion;

  @DataBoundConstructor
  public NvmStep(final String version) {
    this.version = version;
    this.nvmInstallURL = NvmDefaults.nvmInstallURL;
    this.nvmNodeJsOrgMirror = NvmDefaults.nvmNodeJsOrgMirror;
    this.nvmIoJsOrgMirror = NvmDefaults.nvmIoJsOrgMirror;
  }

  public String getVersion() {
    return version;
  }

  @DataBoundSetter
  public void setNvmInstallURL(String nvmInstallURL) {
    this.nvmInstallURL = nvmInstallURL;
  }

  public String getNvmInstallURL() {
    return nvmInstallURL;
  }

  @DataBoundSetter
  public void setNvmNodeJsOrgMirror(String nvmNodeJsOrgMirror) {
    this.nvmNodeJsOrgMirror = nvmNodeJsOrgMirror;
  }

  public String getNvmNodeJsOrgMirror() {
    return nvmNodeJsOrgMirror;
  }

  @DataBoundSetter
  public void setNvmIoJsOrgMirror(String nvmIoJsOrgMirror) {
    this.nvmIoJsOrgMirror = nvmIoJsOrgMirror;
  }

  public String getNvmIoJsOrgMirror() {
    return nvmIoJsOrgMirror;
  }

  public String getNvmInstallDir() {
    return nvmInstallDir;
  }

  @DataBoundSetter
  public void setNvmInstallDir(String nvmInstallDir) {
    this.nvmInstallDir = nvmInstallDir;
  }



  public String getNvmInstallNodeVersion() {
    return nvmInstallNodeVersion;
  }

  @DataBoundSetter
  public void setNvmInstallNodeVersion(String nvmInstallNodeVersion) {
    this.nvmInstallNodeVersion = nvmInstallNodeVersion;
  }

  @Override
  public StepExecution start(final StepContext context) throws Exception {
    return new Execution(this.version, this.nvmInstallURL, this.nvmNodeJsOrgMirror, this.nvmIoJsOrgMirror,
                         this.nvmInstallDir, this.nvmInstallNodeVersion,  context);
  }

  @Extension
  public static final class DescriptorImpl extends StepDescriptor {

    @Override
    public String getFunctionName() {
      return "nvm";
    }

    @Override
    public String getDisplayName() {
      return "Setup the environment for an NVM installation.";
    }

    @Override
    public Set<? extends Class<?>> getRequiredContext() {
      return ImmutableSet.of(
        FilePath.class,
        Launcher.class,
        TaskListener.class
      );
    }

    @Override
    public Step newInstance(StaplerRequest req, JSONObject formData) throws FormException {
      final String versionFromFormData = formData.getString("version");
      final String nvmInstallURLFromFormData = formData.getString("nvmInstallURL");
      final String nvmNodeJsOrgMirrorFromFormData = formData.getString("nvmNodeJsOrgMirror");
      final String nvmIoJsOrgMirrorFromFormData = formData.getString("nvmIoJsOrgMirror");
      final String nvmInstallDir = formData.getString("nvmInstallDir");

      final String nvmInstallNodeVersion = formData.getString("nvmInstallNodeVersion");

      NvmStep nvmStep = new NvmStep(versionFromFormData);

      if (StringUtils.isNotBlank(nvmInstallURLFromFormData)) {
        nvmStep.setNvmInstallURL(nvmInstallURLFromFormData);
      }

      if (StringUtils.isNotBlank(nvmNodeJsOrgMirrorFromFormData)) {
        nvmStep.setNvmNodeJsOrgMirror(nvmNodeJsOrgMirrorFromFormData);
      }

      if (StringUtils.isNotBlank(nvmIoJsOrgMirrorFromFormData)) {
        nvmStep.setNvmIoJsOrgMirror(nvmIoJsOrgMirrorFromFormData);
      }


      nvmStep.setNvmInstallDir(nvmInstallDir);
      nvmStep.setNvmInstallNodeVersion(nvmInstallNodeVersion);

      return nvmStep;
    }

    @Override
    public boolean takesImplicitBlockArgument() {
      return true;
    }

  }

  public static class Execution extends AbstractStepExecutionImpl {

    private static final long serialVersionUID = 1;

    private final transient String nodeVersion;
    private final transient String nvmInstallURL;
    private final transient String nvmNodeJsOrgMirror;
    private final transient String nvmIoJsOrgMirror;
    private final transient String nvmInstallDir;
    private final transient String nvmInstallNodeVersion;

    public Execution(final String nodeVersion,final String nvmInstallURL,
                     final String nvmNodeJsOrgMirror,final String nvmIoJsOrgMirror,
                     final String nvmInstallDir,
                     final String nvmInstallNodeVersion,
                     @Nonnull final StepContext context) {
      super(context);
      this.nodeVersion = nodeVersion;
      this.nvmInstallURL = nvmInstallURL;
      this.nvmNodeJsOrgMirror = nvmNodeJsOrgMirror;
      this.nvmIoJsOrgMirror = nvmIoJsOrgMirror;
      this.nvmInstallDir = nvmInstallDir;
      this.nvmInstallNodeVersion = nvmInstallNodeVersion;
    }

    @Override
    public boolean start() throws Exception {
      final FilePath workspace = this.getContext().get(FilePath.class);
      final Launcher launcher = this.getContext().get(Launcher.class);

      workspace.mkdirs();

      final NvmWrapperUtil wrapperUtil = new NvmWrapperUtil(workspace, launcher, launcher.getListener());
      final Map<String, String> npmEnvVars = wrapperUtil.getNpmEnvVars(this.nodeVersion, this.nvmInstallURL,
                                                                       this.nvmNodeJsOrgMirror, this.nvmIoJsOrgMirror,
                                                                       this.nvmInstallDir);

      getContext().newBodyInvoker()
        .withContext(EnvironmentExpander.merge(getContext().get(EnvironmentExpander.class), new ExpanderImpl(npmEnvVars)))
        .withCallback(BodyExecutionCallback.wrap(getContext()))
        .start();

      return false;
    }

    @Override
    public void stop(@Nonnull Throwable cause) throws Exception {
      // No need to do anything heres
    }

  }

  private static class ExpanderImpl extends EnvironmentExpander {

    private final Map<String, String> envOverrides;

    public ExpanderImpl(final Map<String, String> envOverrides) {
      this.envOverrides = envOverrides;
    }

    @Override
    public void expand(@Nonnull final EnvVars env) throws IOException, InterruptedException {
      this.envOverrides.entrySet().forEach((entrySet) -> env.overrideAll(this.envOverrides));
    }

  }

}
