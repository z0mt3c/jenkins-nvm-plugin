package org.jenkinsci.plugins.nvm;

import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.util.ArgumentListBuilder;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NvmWrapperUtil {

  private static final Logger LOGGER = Logger.getLogger(NvmWrapperUtil.class.getName());

  private AbstractBuild build;
  private Launcher launcher;
  private BuildListener listener;

  private java.nio.file.FileSystem fs = FileSystems.getDefault();

  private  List<String> nvmPaths = Arrays.asList(
    System.getProperty("user.home") + "/.nvm/nvm.sh",
    "/usr/local/nvm/nvm.sh"
  );

  NvmWrapperUtil(AbstractBuild build, Launcher launcher, BuildListener listener) {
    this.build = build;
    this.listener = listener;
    this.launcher = launcher;
  }

  public Map<String, String> getNpmEnvVars(String version, String nvmInstallURL,
                                     String nvmNodeJsOrgMirror, String nvmIoJsOrgMirror)
                                      throws IOException, InterruptedException {

    if (getNvmPath() == null ) {
      installNvm(nvmInstallURL);
    }

    String mirrorBin = version.contains("iojs") ?
      "NVM_IOJS_ORG_MIRROR=" + nvmIoJsOrgMirror  :
      "NVM_NODEJS_ORG_MIRROR=" + nvmNodeJsOrgMirror ;

    ArgumentListBuilder beforeCmd = new ArgumentListBuilder();
    beforeCmd.add("bash");
    beforeCmd.add("-c");
    beforeCmd.add("export > before.env");

    Map<String, String> beforeEnv = toMap(getExport(beforeCmd, "before.env"));

    ArgumentListBuilder nvmSourceCmd = new ArgumentListBuilder();
    nvmSourceCmd.add("bash");
    nvmSourceCmd.add("-c");
    nvmSourceCmd.add("source " + getNvmPath() +
      " && " + mirrorBin + " nvm install " + version +
      " && nvm use "+ version +
      " && export > nvm.env");

    Map<String, String> afterEnv = toMap(getExport(nvmSourceCmd, "nvm.env"));

    Map<String, String> newEnvVars = new HashMap<>();

    afterEnv.forEach((k,v)-> {
      String beforeValue = beforeEnv.get(k);
      if (!v.equals(beforeValue)) {

        if (k.equals("PATH")){
          String path = Arrays.stream(v.split(File.pathSeparator))
            .filter(it -> it.matches(".*\\.nvm.*"))
            .collect(Collectors.joining(File.pathSeparator));
          newEnvVars.put("PATH", afterEnv.get("PATH"));
          newEnvVars.put("PATH+NVM", path);
        } else {
          newEnvVars.put(k, v);
        }

      }
    });

    return newEnvVars;
  }

  private String getExport(ArgumentListBuilder args, String destFile) throws IOException, InterruptedException {

    Integer statusCode = launcher.launch().pwd(build.getWorkspace()).cmds(args)
      .stdout(listener.getLogger())
      .stderr(listener.getLogger()).join();

    if (statusCode != 0) {
      throw new AbortException("Failed to fork bash ");
    }

    return build.getWorkspace().child(destFile).readToString();

  }

  //**
  private String getNvmPath() {

   return nvmPaths.stream().filter((String str) -> fs.getPath(str).toFile().exists())
     .findFirst().orElse(null);

  }

  private Integer installNvm(String nvmInstallURL) throws IOException, InterruptedException {
    listener.getLogger().println("Installing nvm\n");
    FilePath installer = build.getWorkspace().child("nvm-installer");
    installer.copyFrom(new URL(nvmInstallURL));
    installer.chmod(0755);
    ArgumentListBuilder args = new ArgumentListBuilder();

    args.add(installer.absolutize().getRemote());

    return launcher.launch().cmds(args).pwd(build.getWorkspace())
      .stdout(listener.getLogger())
      .stderr(listener.getLogger()).join();

  }

  private Map<String, String> toMap(String export) {
    Map<String, String> r = new HashMap<>();

    Arrays.asList(export.split("[\n|\r]")).forEach(line -> {
      String[] entry = line.replaceAll("declare -x ", "").split("=");
      if (entry.length == 2) {
        r.put(entry[0], entry[1].replaceAll("\"", ""));
      }

    });
    return r;
  }

}
