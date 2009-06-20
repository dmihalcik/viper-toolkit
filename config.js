WScript.Echo ("Creating viper-gt.bat & viper-pe.bat");

function getJarsIn(folder) {
   var files = new Enumerator(folder.files);
   var jars = new Array();
   for (; !files.atEnd(); files.moveNext()) {
   	  var currFile = files.item();
   	  if (4 < currFile.name.length) {
   	  	ext = currFile.name.toLowerCase();
   	  	ext = ext.substr(ext.length-3);
   	  	if ("jar" == ext || "zip" == ext) {
	   	  jars.push(currFile);
   	  	}
   	  }
   }
   return(jars);
}

var WshShell = WScript.CreateObject ("WScript.Shell");
var fso = new ActiveXObject( "Scripting.FileSystemObject" ); 

var WshSysEnv = WshShell.Environment("SYSTEM");
var classPath = WshSysEnv("CLASSPATH")
if (classPath != undefined && classPath != "") {
   classPath = classPath + ";";
}
var libFolder = fso.getFolder(WshShell.CurrentDirectory + "\\common\\lib");

var apiJarFile = WshShell.CurrentDirectory + "\\api\\bin\\viper-api.jar";
var apploaderJarFile = WshShell.CurrentDirectory + "\\apploader\\bin\\viper-apploader.jar";
var chronicleJarFile = WshShell.CurrentDirectory + "\\chronicle\\bin\\viper-chronicle.jar";
var jmpegJarFile = WshShell.CurrentDirectory + "\\jmpeg\\bin\\viper-jmpeg.jar";
var peJarFile = WshShell.CurrentDirectory + "\\pe\\bin\\viper-pe.jar";
var gtJarFile = WshShell.CurrentDirectory + "\\gt\\bin\\viper-gt.jar";

var jars = getJarsIn(libFolder);
jars.push(fso.GetFile(apiJarFile));
jars.push(fso.GetFile(apploaderJarFile));
jars.push(fso.GetFile(chronicleJarFile));
jars.push(fso.GetFile(jmpegJarFile));
jars.push(fso.GetFile(peJarFile));
jars.push(fso.GetFile(gtJarFile));

for (var i in jars) {
    classPath += "\"" + jars[i].Path + "\";";
}
classPath += ".";

var propertiesFile = WshShell.CurrentDirectory + "\\gt\\CONFIG\\gt-config.n3";
var batchFile = fso.OpenTextFile( "gt/bin/viper-gt.bat", 2, true );

batchFile.Write ("rem  Usage: viper-gt.bat <config.gtf> <series>\n");
batchFile.Write ("java -cp " + classPath + " \"-Dlal.prefs=" + propertiesFile + "\" edu.umd.cfar.lamp.viper.gui.core.RunGT %*\n");

batchFile.Close();


var batchFile = fso.OpenTextFile( "pe/bin/viper-pe.bat", 2, true );

batchFile.Write ("rem  Usage: viper-pe.bat [options]\n");
batchFile.Write ("java -cp " + classPath + " viper.comparison.ViperPE %* \n");

batchFile.Close();