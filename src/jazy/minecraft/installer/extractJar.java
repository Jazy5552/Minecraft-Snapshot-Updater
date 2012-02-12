package jazy.minecraft.installer;

import java.io.FileOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class extractJar {

	final String jarPath;
	File to;
	static int extracted;
	int totalbytes;
	
	public int extract(String filePath){//if filePath null extract it all (filePath can be directory to) returns amount extracted -1 if error
		extracted=0;
		JarFile jar;
		totalbytes = 0;
		try{
		jar = new JarFile(jarPath);}catch(Exception e){log(e.getMessage(),false);return -1;}
		InputStream in = null;
		Enumeration<JarEntry> entries = jar.entries();
		JarEntry entry;
		OutputStream out = null;
		byte[] buffer = new byte[1024*4];
		int bytesread;
		while(entries.hasMoreElements()){
			entry = entries.nextElement();
			String nn;
			log("Extracting: "+entry.getName()+"\n",true);
			if (filePath!=null){ //Selective file
				if (filePath.endsWith("/")){ //Directory contents (Not Working Presently) TODO Make extract folder contents only
					if ((entry.getName().toUpperCase().startsWith(filePath.toUpperCase()))&&!entry.getName().toUpperCase().equals(filePath.toUpperCase())){
						nn = entry.getName().substring(filePath.length());
					}else{
						continue;
					}
				}
				else if (!filePath.contains(".")){ //Its a directory without the / (They want the directory)
					if (entry.getName().toUpperCase().startsWith(filePath.toUpperCase())){
						nn = entry.getName();
					}else if (entry.getName().contains(filePath)){
						//hmmm ok?...
						nn = entry.getName().substring(entry.getName().indexOf(filePath));
					}else{
						continue;
					}
				}
				else if (entry.getName().toUpperCase().contains(filePath.toUpperCase())){ //Bingo!
					//Process the name to get only the file
					nn = entry.getName().substring(entry.getName().lastIndexOf("/")+1);
				}
				else continue;
				
			}else{
				nn = entry.getName();
			}
			//(Extract that sucker)
			File n = new File(to+"/"+nn);
			if (entry.getName().endsWith("/")){
				n.mkdirs();
				continue;
			}
			
			try{
				n.getParentFile().mkdirs(); //Little sucker (Gota make parent files first)
				
				out = new FileOutputStream(n);
				in = jar.getInputStream(entry);
				
				//My toys ;)
				float total = in.available();
				int totalby = 0;
				//Write
				while((bytesread=in.read(buffer))!=-1){
					out.write(buffer,0,bytesread);
					totalbytes += bytesread;
					totalby += bytesread;
					start.dis.setText(String.format("%.1f Complete!\n%d bytes written.",(totalby/total)*100,totalbytes));
				}
				//Success
				extracted++;
				log("Extracted: "+entry.getName(),true);
				
			}catch(Exception e){log("ERROR EXTRACTING: "+entry.getName()+"\n"+e.getMessage(),false);return -1;
			}finally{
				try{
					if (in!=null) in.close(); //Close it!
					if (out!=null) out.close(); //Close it!
				}catch(Exception e){}//Ignored
			}
		}
		try{
		jar.close();}catch(Exception e){}
		return extracted;
	}
	
	extractJar(String path){
		if (path==null){
			jarPath = getJarFileName();
		}else{
			jarPath = path;
		}
		to = new File(jarPath).getParentFile();
	}
	
	private String getJarFileName (){
		String result="";
		int from,to;
		try{
		result = this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().toString();
		}catch(Exception e){log(e.getMessage(),false);return null;}
		if (result.contains("jar:file:")){
			from = 9;
		}else if (result.contains("file:")){
			from = 5;
		}else{log("Invalid Excecution!: "+result,false);return null;}
		//We got the path
		//Lets rid the !/
		if (result.contains("!/")){
			to = result.indexOf("!/");
		}else{to=result.length();}
		//Trim it
		result = result.substring(from,to);
		//Lets fix the spaces (%20)
		result = result.replaceAll("%20", " ");
		//We done :D
		log("result= "+result,true);
		return result;
    }
	
	public int extractTo(String extractName,String toPath){ //returns amount extracted
		to = new File(toPath);
		if (!to.isDirectory()){
			log("ExtractTo: Not a valid directory!",true);
			return -1;
		}
		int r = extract(extractName);
		log("Extraction Successful!",false);
		return r;
	}
	
	public static void main(String args[]){ //Debug purposes :D
		extractJar test = new extractJar("C:/Users/Jazy/Desktop/Minecraft Update Installer/MinecraftUpdater.jar");
		test.extract("versions/"); //Works brilliantly!
	}
	
	private void log(String mes,boolean debug){
		try{
			if (start.class!=null){
				start.log("extractJar: "+mes,debug);
			}
		}catch(Exception ignore){}
		System.out.println("extractJar: "+mes);
	}
}
