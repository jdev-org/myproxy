package fr.landel.myproxy.proxypac;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


import fr.landel.myproxy.utils.Logger;

public class ProxyPac {

	private static final Logger LOG = new Logger(ProxyPac.class);
	
	/**
	 * 
	 * @param path
	 * @param destinationUrl
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public String readProxyPacContent(String path, URI destinationUrl) throws MalformedURLException, IOException {
		
		String proxies = "";
		LOG.info("Get information from proxy pac", path);
		if (path != null) {
			
			HttpURLConnection connnexion = null;
			try {
				connnexion = (HttpURLConnection) new URL(path).openConnection(Proxy.NO_PROXY);
				connnexion.setConnectTimeout(1000);
				connnexion.setReadTimeout(1000);
				connnexion.setRequestProperty("accept", "application/x-ns-proxy-autoconfig, */*;q=0.8");
				if (connnexion.getResponseCode() != 200) {
					LOG.error("Proxy pac server returned :", connnexion.getResponseCode());
					throw new IOException("Proxy pac server returned : " + connnexion.getResponseCode() + " " + connnexion.getResponseMessage());
				}
	
				// PrintWriter object for file3.txt 
		        PrintWriter pw = new PrintWriter("fullproxypac.js"); 
		        
		        InputStream is = getClass().getClassLoader().getResourceAsStream("ProxyPacAdditionalFunction.js");
				BufferedReader bfr = new BufferedReader(new InputStreamReader(is));
				 String line = bfr.readLine(); 
		          
			        // loop to copy each line of  
			        // file1.txt to  file3.txt 
			        while (line != null) 
			        { 
			            pw.println(line); 
			            line = bfr.readLine(); 
			        } 
				
				BufferedReader br = new BufferedReader(new InputStreamReader(connnexion.getInputStream(), "ISO-8859-1"));
				line = br.readLine(); 
		          
		        // loop to copy each line of  
		        // file1.txt to  file3.txt 
		        while (line != null) 
		        { 
		            pw.println(line); 
		            line = br.readLine(); 
		        } 
		          
		        pw.flush(); 
		          
		        // closing resources 
		        br.close(); 
		        pw.close(); 
				
				BufferedReader brFinal = new BufferedReader(new FileReader("fullproxypac.js"));

				
				ScriptEngineManager manager = new ScriptEngineManager();
				ScriptEngine engine = manager.getEngineByName("JavaScript");
				// read script file
				engine.eval(brFinal);

				Invocable inv = (Invocable) engine;
				// call function from script file
				// TODO get host for url
				proxies = (String) inv.invokeFunction("FindProxyForURL", new Object[] {destinationUrl.toURL().toString(), destinationUrl.getHost() });
				
				br.close();
			} catch (ScriptException | NoSuchMethodException e) {
				LOG.error(e.getMessage(), e);
			} finally {
				if (connnexion != null) {
					connnexion.disconnect();
				}
			}
	}
	
		return proxies;
	}
}
