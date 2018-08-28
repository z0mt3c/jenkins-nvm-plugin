package org.jenkinsci.plugins.nvm.NvmStep

import lib.LayoutTagLib


l = namespace(LayoutTagLib)
t = namespace('/lib/hudson')
st = namespace('jelly:stapler')
f = namespace('/lib/form')

f.entry(title: 'Node Version', field: 'nodeVersion') {
  f.textbox()
}

f.entry(title:"NVM Install URL", field:"nvmInstallURL") {
  f.textbox()
}

f.entry(title:"NVM_NODEJS_ORG_MIRROR", field:"nvmNodeJsOrgMirror") {
  f.textbox()
}

f.entry(title:"NVM_IOJS_ORG_MIRROR", field:"nvmIoJsOrgMirror") {
  f.textbox()
}

f.entry(title:"NVM_DIR", field:"nvmInstallDir") {
  f.textbox()
}

f.entry(title:"NODE_VERSION", field:"nvmInstallNodeVersion") {
  f.textbox()
}

