def laszloSwfTarget = args[0]
def lzxDir = args[1]
def openlaszloHome = args[2]
def lzcArgs = "--runtime=${laszloSwfTarget}"

println "args = $args"

def ant = new AntBuilder()

ant.sequential {
  path(id:"classpath") {
    pathelement(location:"${openlaszloHome}/WEB-INF/lps/server/bin")
    pathelement(location:"${openlaszloHome}/WEB-INF/classes")
    fileset(dir:"${openlaszloHome}/WEB-INF/lib",includes:"*.jar")
  }
}

def compile = { file ->
  ant.sequential {
    def swf = "${file}".replaceFirst("\\.lzx",".${laszloSwfTarget}.swf")
    if(!new File("${swf}").exists()) {
      println "Compiling ${file}..."
      println "arg: ${lzcArgs} ${file}"
      java(classname:"org.openlaszlo.compiler.Main",fork:"yes",failonerror:true,classpathref:"classpath") {
        jvmarg(value:"-DLPS_HOME=${openlaszloHome}")
        arg(line:"${lzcArgs} ${file}")
      }
    }
  }
}

dir = new File(lzxDir)
dir.eachFileRecurse {
  def fileName = it.getName().toString()
  if(fileName =~ /\.lzx$/){
    compile(it.getPath())
  }
}