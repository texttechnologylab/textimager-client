package org.hucompute.textimager.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import joptsimple.internal.Strings;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;

public class SSHUtils {
	
	public static String RSA_KEY_PATH = "/home/ahemati/workspaceGitNew/textimager-server/ducc/id_rsa"; 
	public static String SERVER_URL = "localhost";
	public static int SERVER_SSH_PORT = 2222;
	
	public static String runRemoteCommand(String ... cmds) throws IOException{
		final SSHClient ssh = new SSHClient();
		ssh.addHostKeyVerifier(new PromiscuousVerifier());
		String username = "root";
		File privateKey = new File(RSA_KEY_PATH);
		KeyProvider keys = ssh.loadKeys(privateKey.getPath());
		ssh.connect(SERVER_URL, SERVER_SSH_PORT);
		ssh.authPublickey(username, keys);
		String command = Strings.join(cmds, "; ");
		Session session = null;
		try {
			session = ssh.startSession();
			final Command cmd = session.exec(command);
			return (IOUtils.toString(cmd.getInputStream(),Charset.defaultCharset()));
		} finally {
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
		String output = runRemoteCommand(
				("su - ducc -c 'cd /home/ducc/apache-uima-ducc/bin && ./ducc_submit "+ params.toString()+"'")).replace(System.lineSeparator(), " ").replace("\n", " ");
		System.out.println(("su - ducc -c 'cd /home/ducc/apache-uima-ducc/bin && ./ducc_submit "+ params.toString().replace(System.lineSeparator(), " ").replace("\n", " ")+"'"));
		return Long.parseLong(output.replaceAll(".*?Job (.*?) submitted.*", "$1"));
	}
	
	//TODO: Fehler abfangen
	public static boolean sshDuccJobCancel(long jobId) throws IOException{
		String output = runRemoteCommand(
				"cd /home/ducc/apache-uima-ducc/bin",
				"./ducc_cancel --id "+ jobId)
				.replace(System.lineSeparator(), " ")
				.replace("\n", " ");
		System.out.println(output);
		return true;
	}
}
