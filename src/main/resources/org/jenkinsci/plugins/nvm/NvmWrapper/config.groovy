import lib.LayoutTagLib

l=namespace(LayoutTagLib)
t=namespace("/lib/hudson")
st=namespace("jelly:stapler")
f=namespace("lib/form")

f.entry(title:"Node version", field:"version") {
  f.textbox()
}

f.optionalBlock(title:"NVM Advanced Settings", inline:true){


  f.entry(title:"NVM_NODEJS_ORG_MIRROR", field:"nvmNodeJsOrgMirror") {
    f.textbox()
  }
  f.entry(title:"NVM_IOJS_ORG_MIRROR", field:"nvmIoJsOrgMirror") {
    f.textbox()
  }

}

f.optionalBlock(title:"Customize NVM Installation Settings", inline:true){

  f.entry(title:"NVM Install URL", field:"nvmInstallURL") {
    f.textbox()
  }


  f.entry(title:"NVM_DIR installation dir", field:"nvmInstallDir") {
    f.textbox()
  }

}





