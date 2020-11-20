package org.hucompute.textimager.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.hucompute.textimager.client.rest.ducc.DUCCAPI;

import joptsimple.internal.Strings;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

public class SSHUtils {
    public static String RSA_KEY_PATH; 
    public static String SERVER_URL;
    public static int SERVER_SSH_PORT;
    public static String SSH_USER;
    
	
	static {

		RSA_KEY_PATH = Paths.get(DUCCAPI.properties.getProperty("DUCC_LOCAL"),"id_rsa").toAbsolutePath().toString(); 
		SERVER_URL = DUCCAPI.properties.getProperty("SSH_SERVER_URL");
		SERVER_SSH_PORT = Integer.parseInt(DUCCAPI.properties.getProperty("SSH_SERVER_SSH_PORT"));
		SSH_USER = DUCCAPI.properties.getProperty("SSH_USER");
		
	}

	public static String runRemoteCommand(String ... cmds) throws IOException{
		System.out.println(Arrays.toString(cmds));
		System.out.println(SERVER_URL + "\t" + SERVER_SSH_PORT);
		System.out.println(SSH_USER);
		System.out.println(RSA_KEY_PATH);
		
		final SSHClient ssh = new SSHClient();
		ssh.addHostKeyVerifier(new PromiscuousVerifier());
		File privateKey = new File(RSA_KEY_PATH);
		KeyProvider keys = ssh.loadKeys(privateKey.getPath());
		ssh.connect(SERVER_URL, SERVER_SSH_PORT);
		ssh.authPublickey(SSH_USER, keys);
		String command = Strings.join(cmds, "; ");
		
		

		
		Session session = null;
		try {
			session = ssh.startSession();
			System.out.println("======");
			System.out.println(command);
			System.out.println("======");
			final Command cmd = session.exec(command);
			
			
			return (IOUtils.toString(cmd.getInputStream(),Charset.defaultCharset()));
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		} 
		finally {
			try {
				if (session != null) {
					session.close();
				}
			} catch (IOException e) {
				// Do Nothing   
			}
			ssh.disconnect();
		}
	}
	
	public static long sshDuccJobSubmit(Properties prop) throws IOException{
		StringBuilder params = new StringBuilder();
		for (Entry<Object, Object> entry : prop.entrySet()) {
			params.append("--" + entry.getKey()).append(" ").append(entry.getValue()).append(" ");
		}
		
		String command = ("cd /home/ducc/ducc/apache-uima-ducc/bin && ./ducc_submit "+ params.toString()+"").replace(System.lineSeparator(), " ").replace("\n", " ");
		String output = runRemoteCommand(command);
		System.out.println(command);
		System.out.println(output);
		return Long.parseLong(output.replaceAll(".*?Job (.*?) submitted.*", "$1").trim());
	}
	
	//TODO: Fehler abfangen
	public static boolean sshDuccJobCancel(long jobId) throws IOException{
		String output = runRemoteCommand(
				"cd /home/ducc/ducc/apache-uima-ducc/bin",
				"./ducc_cancel --id "+ jobId)
				.replace(System.lineSeparator(), " ")
				.replace("\n", " ");
		System.out.println(output);
		return true;
	}
}
