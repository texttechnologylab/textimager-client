package org.hucompute.textimager.client.rest.ducc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.apache.uima.json.JsonCasDeserializer;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.hucompute.textimager.client.TextImagerOptions.IOFormat;
import org.hucompute.textimager.util.SSHUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import spark.Request;

@Api(value = "Big Data API")
@Path("/big-data")
public class DUCCAPI {
	public static String configFile = "config.prop";
	public static String DUCC_HOME_HOST = "/home/ducc/ducc/apache-uima-ducc";
	public static String DUCC_HOME_CONTAINER = "/home/ducc/ducc/apache-uima-ducc";
	public static String DUCC_SERVICE_SCRIPTS = "/home/ducc/ducc/serviceScripts/";
	
	public static String MONGO_CONNECTION_HOST = "textimager-database";
	public static String MONGO_CONNECTION_DBNAME = "lab";
	public static String MONGO_CONNECTION_USER = "root";
	public static String MONGO_CONNECTION_PW = "rootpassword";
	public static Properties properties = new Properties();

	
	static {
		loadPropertiesFile(DUCCAPI.class.getClassLoader().getResourceAsStream(configFile));
	}
	
	public DUCCAPI() {
	}
	
	public DUCCAPI(String configFilePath) throws FileNotFoundException{
		loadPropertiesFile(new FileInputStream(new File(configFilePath)));
	}

	public static void loadPropertiesFile(InputStream configFile){
	    try {
	        try (InputStream stream = configFile) {
	            properties.load(stream);
	        }
	    } catch (IOException ex) {
	        // handle error
	    }
	    DUCC_HOME_HOST = properties.getProperty("DUCC_LOCAL");
	}
	
	private File getTempFile() throws IOException {
		File tmpDir = Paths.get(DUCC_HOME_HOST,"ducctest/tmp/").toFile();
		tmpDir.mkdirs();
		return File.createTempFile("ducc", ".ducc",tmpDir);
	}

	private File createTempDirectory()
			throws IOException
	{
		final File temp;
		File tmpDir = Paths.get(DUCC_HOME_HOST,"ducctest/tmp/").toFile();
		tmpDir.mkdirs();

		temp = File.createTempFile("temp", Long.toString(System.nanoTime()),tmpDir);

		if(!(temp.delete()))
		{
			throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
		}

		if(!(temp.mkdir()))
		{
			throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
		}

		return (temp);
	}

	/* Returns true if url is valid */
	private static boolean isValidURL(String url) 
	{ 
		/* Try creating a valid URL */
		try { 
			new URL(url).toURI(); 
			return true; 
		} 
		// If there was an Exception 
		// while creating URL object 
		catch (Exception e) { 
			return false; 
		} 
	} 

	private boolean checkIfRemoteDirectoryExists(String path) throws IOException{
		final SSHClient ssh = new SSHClient();
		ssh.addHostKeyVerifier(new PromiscuousVerifier());
		String username = SSHUtils.SSH_USER;
		File privateKey = new File(SSHUtils.RSA_KEY_PATH);
		KeyProvider keys = ssh.loadKeys(privateKey.getPath());
		ssh.connect(SSHUtils.SERVER_URL, SSHUtils.SERVER_SSH_PORT);
		ssh.authPublickey(username, keys);

		Session session = null;
		try {
			session = ssh.startSession();
			final Command cmd = session.exec("[ -d /home/ducc/ducc/texte ] && echo \"true\" || echo \"false\"");
			String output = (IOUtils.toString(cmd.getInputStream())).replace("\n", "").replace(System.lineSeparator(), "");
			return Boolean.parseBoolean(output);
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

	private File handleFiles(Request req) throws IOException, ServletException {
		File file = null;

		// Up/Download mitgegeben?
		if (req.queryParams().contains("url")) {
			if(isValidURL(req.queryParams("url"))){
				// URL, Datei downloaden
				file = getTempFile();
				String url = req.queryParams("url");
				FileUtils.copyURLToFile(new URL(url), file);
			}else if(new File(req.queryParams("url")).exists() || checkIfRemoteDirectoryExists(req.queryParams("url"))){
				return new File(req.queryParams("url"));
			}
		}else{
			req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
			// Datei Upload
			file = getTempFile();
			try (InputStream is = req.raw().getPart("file").getInputStream()) {
				FileUtils.copyInputStreamToFile(is, file);
			}
		}

		if (file != null) {
			System.out.println(file);
			// Entpacken
			//			File unpackDir = com.google.common.io.Files.createTempDir();
			File unpackDir = createTempDirectory();
			ZipFile zipFile = new ZipFile(file);
			try {
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements()) {
					ZipEntry entry = entries.nextElement();
					File entryDestination = new File(unpackDir, entry.getName());
					if (entry.isDirectory()) {
						entryDestination.mkdirs();
					} else {
						entryDestination.getParentFile().mkdirs();
						InputStream in = zipFile.getInputStream(entry);
						FileUtils.copyInputStreamToFile(in, entryDestination);
						in.close();
					}
				}
			} catch (Exception ex) {
				return null;
			} finally {
				zipFile.close();
			}
			return unpackDir.getAbsoluteFile();
		}
		return null;
	}

	private static String getInputReader(String inputFormat){
		switch (org.hucompute.textimager.client.TextImagerOptions.IOFormat.valueOf(inputFormat)) {
		case TXT:
			return Paths.get(DUCC_SERVICE_SCRIPTS,"io/TextReader.xml").toString();
		case TEI:
			return Paths.get(DUCC_SERVICE_SCRIPTS,"io/TeiReader.xml").toString();
		case TCF:
			return Paths.get(DUCC_SERVICE_SCRIPTS,"io/TcfReader.xml").toString();
		case HTML:
			return Paths.get(DUCC_SERVICE_SCRIPTS,"io/EnhancedHtmlReader.xml").toString();
		case XMI:
			return Paths.get(DUCC_SERVICE_SCRIPTS,"io/XmiReader.xml").toString();
		case WIKIDRAGON:
			return Paths.get(DUCC_SERVICE_SCRIPTS,"io/WikiDragonCassandraCollectionReader.xml").toString();
		case TEI_TTLAB:
			return Paths.get(DUCC_SERVICE_SCRIPTS,"io/TeiReaderTTLab.xml").toString();
		default:
			return null;
		}
	}

	private static String jobFilename(long duccId) {
		return Paths.get(DUCC_HOME_HOST,"ducctest/logs/", Long.toString(duccId) + ".job").toString();
	}

	public Properties getJobProperties(){
		Properties prop = new Properties();
		prop.setProperty("process_deployments_max", "30");
		prop.setProperty("process_memory_size", "20");
		prop.setProperty("process_per_item_time_max", "1800");
		prop.setProperty("process_pipeline_count", "1");
		prop.setProperty("process_failures_limit", "99564");
		prop.setProperty("process_initialization_failures_cap", "9999");

		prop.setProperty("working_directory", Paths.get(DUCC_HOME_CONTAINER,"ducctest").toString());
		prop.setProperty("log_directory", Paths.get(DUCC_HOME_CONTAINER,"ducctest/logs").toString());
		
		prop.setProperty("driver_jvm_args", "\"-Xmx20g -Dfile.encoding=utf-8\"");
		prop.setProperty("driver_exception_handler_arguments", "\"max_job_errors=99564 max_timeout_retrys_per_workitem=0\"");
		
		prop.setProperty("process_error_window_threshold", "20");
		prop.setProperty("process_error_window_size", "100");

		prop.setProperty("classpath", "/home/ducc/ducc/apache-uima-ducc/lib/uima-ducc/workitem/uima-ducc-workitem-v2.jar:"
				+ "/home/ducc/ducc/apache-uima-ducc/apache-uima/lib/*:"
				+ "/home/ducc/ducc/apache-uima-ducc/apache-uima/apache-activemq/lib/*:"
				+ "/home/ducc/ducc/apache-uima-ducc/apache-uima/apache-activemq/lib/optional/*:"
//				+ "/home/ducc/ducc/jars/sub2/*"
				+ "/home/ducc/ducc/jarsTagMe/*:"
				+ "/home/ducc/ducc/jars/textimager-uima-deploy-0.0.2-models.jar:"
				+ "/home/ducc/ducc/jars/textimager-uima-deploy-0.3.0-source.jar"
//				+ "/home/ducc/ducc/jars/textimager-uima-deploy-0.3.2-source.jar"
				.replace("$DUCC_HOME", DUCC_HOME_CONTAINER));
//				prop.setProperty("debug", "");
//				prop.setProperty("all_in_one", "remote");
		return prop;
	}

	@POST
	@Path("/analyse")
	@Consumes({"multipart/form-data"})
	@ApiOperation(value = "Create a DUCC Job.")
	@ApiImplicitParams (value = {
			@ApiImplicitParam(dataType = "java.io.File", name = "file", required = false,paramType = "form",allowMultiple=true,type="file", format= "binary", value = "Upload function not supported by Swagger. Please use another REST-Client!"),
			@ApiImplicitParam(dataType = "string", name = "url", required = false, paramType = "query",value="This resource will be downloaded and processed. Can be a document or ZIP. Can also be a URI on the server, which must be accesable by ducc user"),
			@ApiImplicitParam(dataType = "string", name = "language", required = true,paramType = "query",value="Language of the corpus", allowableValues="en,de,la"),
			@ApiImplicitParam(dataType = "string", name = "inputFormat", required = true,paramType = "query",value="Inputformat of documents in the corpus.", allowableValues="TXT,TEI,TCF,XMI,WIKIDRAGON,HTML,TEI_TTLAB"),
			@ApiImplicitParam(dataType = "string", name = "fileSuffix", required = true,paramType = "query",value="Filesuffix of documents in corpus."),
			@ApiImplicitParam(dataType = "boolean", name = "sortBySize", required = false,paramType = "query",defaultValue="false",value="Sort files to be processed by filesize."),
			@ApiImplicitParam(dataType = "string", name = "pipeline", required = true,paramType = "query", value="Tools inside pipeline. Predefined pipelines are \"textimager\",\"biofid\". List of available annotators can be found here: http://service.hucompute.org/urls_v2.xml",allowMultiple=true),
			@ApiImplicitParam(dataType = "integer", name = "process_memory_size", required = false,paramType = "query",value="Minimum memory usage per deployment in GB, default is \"20\" (depends on actual pipeline, e.g. use \"30\" for TextImager default pipeline)"),
			@ApiImplicitParam(dataType = "integer", name = "process_deployments_max", required = false,paramType = "query",value="Maximum number of deployments, default is \"30\""),
			@ApiImplicitParam(dataType = "integer", name = "process_per_item_time_max", required = false,paramType = "query",value="Maximum time (in seconds) to wait, till processing of item will receive a timeout, default is \"1800\"."),
			@ApiImplicitParam(dataType = "string", name = "outputFormat", required = false,paramType = "query",value="Output format",defaultValue="XMI", allowableValues="XMI,MONGO"),
			@ApiImplicitParam(dataType = "string", name = "outputLocation", required = false,paramType = "query",value="Output location. Only considered if outputFormat != MONGO"),
			@ApiImplicitParam(dataType = "string", name = "outputCompression", required = false,paramType = "query",defaultValue="GZIP",value="Compression method. Only capable if outputFormat != MONGO.", allowableValues="NONE, GZIP, BZIP2, XZ"),
			@ApiImplicitParam(dataType = "string", name = "outputMongoConnectionString", required = false, paramType = "query", value="Simplified MongoDB connection string like \"mongodb://username:password@host:port/db?authSource=admin\". Leave empty to use TextImager default database"),
			@ApiImplicitParam(dataType = "string", name = "session", required = false,paramType = "query",value="Description"),
			@ApiImplicitParam(dataType = "string", name = "description", required = false, paramType = "query", value="Short description, visible in the DUCC UI"),
			@ApiImplicitParam(dataType = "string", name = "modificationUser", required = false, paramType = "query", value="Name of the user that started this modification."),
			@ApiImplicitParam(dataType = "string", name = "modificationComment", required = false, paramType = "query", value="Short comment with details about this modification."),
	}
			)
	public JSONObject analyse(@ApiParam(hidden=true)Request request) throws XPathExpressionException, NullPointerException, UIMAException, JAXBException, IOException, SAXException, ParserConfigurationException{
		Properties prop = getJobProperties();

		String inputFormat = request.queryParams("inputFormat");
		String language = request.queryParams("language");
		String fileSuffix = request.queryParams("fileSuffix");
		
		String modificationUser = "";
		if(request.queryParams().contains("modificationUser"))
			modificationUser = request.queryParams("modificationUser");
		
		String modificationComment = "";
		if(request.queryParams().contains("modificationComment"))
			modificationComment = request.queryParams("modificationComment");

		if(request.queryParams().contains("process_deployments_max"))
			prop.setProperty("process_deployments_max", request.queryParams("process_deployments_max"));

		if(request.queryParams().contains("process_memory_size"))
			prop.setProperty("process_memory_size",  request.queryParams("process_memory_size"));

		if(request.queryParams().contains("process_per_item_time_max"))
			prop.setProperty("process_per_item_time_max", request.queryParams("process_per_item_time_max"));


		prop.setProperty("driver_descriptor_CR", getInputReader(inputFormat));
		
		if (request.queryParams().contains("description")) {
			prop.setProperty("description", "\"" + request.queryParams("description") + "\"");
		}

		String uuid = UUID.randomUUID().toString().split("-")[0];

		if(IOFormat.valueOf(inputFormat) != IOFormat.WIKIDRAGON){
			// "zip" Up/Downloads handeln:
			// Die Funktion prüft ob ein Parameter "ducc_file_upload" oder "ducc_url_download" vorhanden ist,
			// wenn ja wird die Datei geladen und entpackt
			// dann wird der Pfad zum Ordner zurückgegeben
			// die Datei wird nicht gelöscht
			// die Parameter werden aus props gelöscht, die Properties können dann ganz normal an Ducc gegeben werden
			File filename = null;
			try {
				filename = handleFiles(request);
			} catch (Exception ex) {
				System.out.println("Error handeling files.");  
				ex.printStackTrace();
				return new JSONObject()
						.put("status", "error")
						.put("message", "Error handeling files.");
			}
			System.out.println("Unpack dir: " + filename);
			String driver_descriptor_CR_overrides = "sourceLocation=" + filename + " patterns=[+]**/*."+fileSuffix+" language="+language;
			if(request.queryParams().contains("sortBySize") && request.queryParams("sortBySize").equals("true"))
				driver_descriptor_CR_overrides+=" sortBySize=true";
			
			if(inputFormat.equals("XMI"))
				driver_descriptor_CR_overrides+=" addDocumentMetadata=false";
			
			driver_descriptor_CR_overrides+=" targetLocation=" + request.queryParams("outputLocation");

			// modification meta data
			if (!modificationUser.isEmpty()) {
				driver_descriptor_CR_overrides +=" docModificationUser='" + modificationUser + "'";
			}
			if (!modificationComment.isEmpty()) {
				driver_descriptor_CR_overrides +=" docModificationComment='" + modificationComment + "'";
			}
			
			prop.setProperty("driver_descriptor_CR_overrides", "\""+driver_descriptor_CR_overrides+"\"");

			if(request.queryParams().contains("outputFormat") && request.queryParams("outputFormat").trim().equals("XMI")){
				String overrides = "\"overwrite=true targetLocation="+request.queryParams("outputLocation");
				if(request.queryParams().contains("outputCompression") && !request.queryParams("outputCompression").equals("NONE"))
					overrides+=" compression=\"" + request.queryParams("outputCompression") + "\"";
				
				overrides +="\"";
				prop.setProperty("process_descriptor_CC_overrides", overrides);
				prop.setProperty("process_descriptor_CC", Paths.get(DUCC_SERVICE_SCRIPTS,"io/XmiWriter.xml").toString());
			}
			else{
				// MongoDB
				String mongoHost = MONGO_CONNECTION_HOST;
				String mongoDB = MONGO_CONNECTION_DBNAME;
				String mongoUser = MONGO_CONNECTION_USER;
				String mongoPass = MONGO_CONNECTION_PW;
				
				// Use connection string
				if (request.queryParams().contains("outputMongoConnectionString")) {
					String outputMongoConnectionString = request.queryParams("outputMongoConnectionString");
					if (!outputMongoConnectionString.isEmpty()) {
						ConnectionString mongoConnectionString = new ConnectionString(outputMongoConnectionString);
						mongoHost = mongoConnectionString.getHosts().get(0);
						mongoDB = mongoConnectionString.getDatabase();
						mongoUser = mongoConnectionString.getUsername();
						mongoPass = String.valueOf(mongoConnectionString.getPassword());
					}
				}
				
				prop.setProperty("process_descriptor_CC_overrides", String.format("\""
						+ "mongo_connection_collectionname=%s "
						+ "mongo_connection_host=%s "
						+ "mongo_connection_dbname=%s "
						+ "mongo_connection_user=%s "
						+ "mongo_connection_pw=%s\"",
						uuid,
						mongoHost,
						mongoDB,
						mongoUser,
						mongoPass));
				prop.setProperty("process_descriptor_CC", Paths.get(DUCC_SERVICE_SCRIPTS,"io/MongoWriter.xml").toString());
			}
		}
		else{
			prop.setProperty("driver_descriptor_CR_overrides", request.queryParams("url"));
			prop.setProperty("process_descriptor_CC_overrides", request.queryParams("url"));
			prop.setProperty("process_descriptor_CC", Paths.get(DUCC_SERVICE_SCRIPTS,"io/WikiDragonCassandraWriter.xml").toString());
		}

		/**
		 * TODO auslagern Pipeline Erstellung
		 */
		//		List<String> pipeline = Arrays.asList(request.queryParams("pipeline").split(",")).stream().map(x -> x.trim()).collect(Collectors.toList());
		String[]pipeline = request.queryMap().toMap().get("pipeline");

		for (String string : pipeline) {
			if(string.equals("textimager")){
				if(language.equals("de")){
					pipeline = new String[]{
							"SpaCyMultiTagger",
							"ParagraphSplitter",
							"LanguageToolLemmatizer",
							"MateMorphTagger",
							"FastTextDDC2LemmaNoPunctPOSNoFunctionwordsWithCategoriesTextImagerService",
							"text2cwcSplitThemaWebservice",
							"text2cwcSplitRaumWebservice",
							"text2cwcSplitZeitWebservice",
							"FastTextWikipediaDisambigService",
							"HeidelTime",
							"TextBlobSentiment"};
				}else if(language.equals("en")){
					pipeline = new String[]{
							"SpaCyMultiTagger",
							"ParagraphSplitter",
							"LanguageToolLemmatizer",
							"FastTextDDC2LemmaNoPunctTextImagerService",
							"text2cwcENSplitRaumWebservice",
							"text2cwcENSplitThemaWebservice",
							"text2cwcENSplitZeitWebservice",
							"HeidelTime",
							"TextBlobSentiment"};
				}
			}
			else if(string.equals("biofid")){
				if(language.equals("de")){
					pipeline = new String[]{
							"SpaCyMultiTagger",
							"ParagraphSplitter",
							"LanguageToolLemmatizer",
							"MateMorphTagger",
							"FastTextDDC2LemmaNoPunctPOSNoFunctionwordsWithCategoriesTextImagerService",
							"text2cwcSplitThemaWebservice",
							"text2cwcSplitRaumWebservice",
							"text2cwcSplitZeitWebservice",
							"FastTextWikipediaDisambigService",
							"HeidelTime",
							"TextBlobSentiment",
							"TagMeLocalAnnotator",
							"WikidataHyponyms",
							"BIOfidTreeGazetteer",
							"EuroWordNetTagger"};
				}
			}
		}

		HashMap<String, String>options = new HashMap<>();
		for (String annotator : pipeline) {
			if(options.containsKey(language))
				options.put(language, options.get(language)+","+annotator);
			else
				options.put(language, annotator);
		}

		ArrayList<ServiceDataholder> pipeServices = PipelineTest.constructPipeline(options);
		//			IOUtils.toString(input)
		String baseDescriptor = "";
		try{
			baseDescriptor = IOUtils.toString(DUCCAPI.class.getClassLoader().getResourceAsStream("baseDescriptor.xml"));
		}catch(NullPointerException e){
			baseDescriptor = IOUtils.toString(DUCCAPI.class.getClassLoader().getResourceAsStream("resources/baseDescriptor.xml"));
		}
		baseDescriptor = baseDescriptor.replace("xxxxx", PipelineTest.getDelegateAnalysisEngineSpecifiers(pipeServices));
		baseDescriptor = baseDescriptor.replace("yyyyy", PipelineTest.getFixedFlow(pipeServices));
		File descriptor = File.createTempFile("descriptor",".xml");

		FileUtils.writeStringToFile(descriptor, baseDescriptor);
		System.out.println(descriptor.getPath());
		FileUtils.moveFile(descriptor, Paths.get(DUCC_HOME_HOST,"ducctest/DuccRestTest/aes",descriptor.getName()).toFile());
		prop.setProperty("process_descriptor_AE", Paths.get(DUCC_HOME_CONTAINER,"ducctest/DuccRestTest/aes",descriptor.getName()).toString());

		System.out.println(prop);
		prop.entrySet().stream().map(x->x.getKey()+"\t"+x.getValue()).forEach(System.out::println);
		//			
		long duccId = -1;
		try {  
			duccId = SSHUtils.sshDuccJobSubmit(prop);
			//			DuccJobSubmit ds = new DuccJobSubmit(prop, null);  
			//			boolean rc = ds.execute();  
			// If the return is ’true’ then as best the API can tell, the submit worked  
			if ( duccId > -1 ) {  
				//				duccId = ds.getDuccId();

				// Startzeitpunkt des Jobs merken
				// TODO: Später im RM speichern
				long timeNowMillis = System.currentTimeMillis();
				System.out.println("Job started: " + timeNowMillis);
				try {
					PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(jobFilename(duccId)))));
					writer.println(timeNowMillis);
					writer.flush();
					writer.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} else {  
				System.out.println("Could not submit job");  
			}  
		}  
		catch(Exception e) {  
			System.out.println("Cannot initialize: " + e);  
		}
		JSONObject output = new JSONObject();
		output.put("jobId", duccId);
		//		if(request.queryParams("session")!=null)
		//			ResourceManagerUtil.createJob("BreakIteratorSegmenter", language, null, duccId,uuid,"xx",request.queryParams("session"));
		return output;
	}


	@Path("/cancel")
	@GET
	@ApiOperation(value = "Cancel running job.")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success", response=JSONObject.class)
	})
	public JSONObject cancel(@ApiParam(example = "22246") @QueryParam("jobId") long jobId) throws IOException{
		try {
			boolean rc = SSHUtils.sshDuccJobCancel(jobId);
			if (rc) {
				return new JSONObject()
						.put("status", "success");
				//						.put("message", cancelJob.getResponseMessage());
			} else {
				return new JSONObject()
						.put("status", "error");
				//						.put("message", cancelJob.getResponseMessage());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new JSONObject()
				.put("status", "error")
				.put("message", "unknown error");
	}

	@Path("/jobInfo")
	@GET
	@ApiOperation(value = "Get status of running job.")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success", response=JSONObject.class)
	})
	public JSONObject getJobInfos(@ApiParam(example = "22246") @QueryParam("jobId")  long duccId) {
		// Prüfen, wann Job gestartet wurde, sowie berechnen wie lange es noch
		// laufen könnte
		long millis = -1;
		try {
			if(!new File(jobFilename(duccId)).exists())
				return new JSONObject()
						.put("status", "error")
						.put("message", "Job not found.");
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(jobFilename(duccId))));
			String line = reader.readLine();
			reader.close();
			if (line != null) {
				millis = Long.parseLong(line);
				/*SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm");
				Date resultdate = new Date(millis);
				System.out.println(sdf.format(resultdate));*/

			} else {
				// Fehler
				return new JSONObject()
						.put("status", "error")
						.put("message", "could not get start time for job");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new JSONObject()
					.put("status", "error")
					.put("message", e.getMessage());
		}

		if (millis < 0) {
			// TODO Fehler...
		}


		JSONObject jsonResult = new JSONObject();

		long millisNow = System.currentTimeMillis();
		long millisDuration = millisNow - millis;
		long secsDuration = TimeUnit.MILLISECONDS.toSeconds(millisDuration);

		int urlTimeout = 1 * 60 * 1000;

		int total = 0;

		try {

			File jdFile = Arrays.asList(Paths.get(DUCC_HOME_HOST,"ducctest/logs/" ,Long.toString(duccId)).toFile().listFiles()).stream().filter(x->x.getName().contains("-JD-")).collect(Collectors.toList()).get(0);
			Scanner scanner = new Scanner(jdFile);

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if(line.contains("resources to be read")){
					total = Integer.parseInt(line.replaceAll(".*?\\[(.*?)\\].*", "$1"));	
				}
			}
		} catch(FileNotFoundException e) { 
			//handle this
		}		

		String status =null;
		try {
			List<String>lines = FileUtils.readLines(Paths.get(DUCC_HOME_HOST,"ducctest/logs/" , Long.toString(duccId),"ducc.log").toFile());
			for (String string : lines) {
				String line = string.replaceAll(".*? .*? .*? ", "");
				if(line.contains("Completed")){
					status = "Completed";
					break;
				}
				else
					status = line.trim();
			}
		} catch (IOException e1) {
			jsonResult.put("state", "Initializing");
			return jsonResult;
		}

		//processed Items
		HashSet<Integer>seqNos = new HashSet<>();
		int done = 0;
		try {
			Scanner scanner = new Scanner(Paths.get(DUCC_HOME_HOST,"ducctest/logs/" , Long.toString(duccId),"work-item-state.json").toFile());
			while (scanner.hasNextLine()) {
				seqNos.add(Integer.parseInt(scanner.nextLine().replaceAll(".*?seqNo\":\"(.*?)\".*", "$1")));
			}
			done = seqNos.size();
		} catch (FileNotFoundException e) {
			jsonResult.put("state", "Initializing");
			return jsonResult;
		}

		jsonResult.put("state", status);
		jsonResult.put("total", total);
		jsonResult.put("done", done);

		if (!status.equals("Completed")) {
			if (status.equals("Running") && done > 0) {
				long millisNeeded = millisDuration / done;
				long millisToCompletion = millisNeeded * total;
				long hours = TimeUnit.MILLISECONDS.toSeconds(millisToCompletion) / 3600;
				long minutes = (TimeUnit.MILLISECONDS.toSeconds(millisToCompletion) % 3600) / 60;
				long seconds = TimeUnit.MILLISECONDS.toSeconds(millisToCompletion) % 60;

				String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);

				jsonResult.put("completion", timeString);
			}
		}
		return jsonResult;
	}

	@Path("/listJobs")
	@GET
	@ApiOperation(value = "List all jobs.")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success", response=JSONObject.class)
	})
	public JSONArray getAllJobs() throws FileNotFoundException, IOException{
		JSONArray output = new JSONArray();
		File[]jobFolders = Paths.get(DUCC_HOME_HOST,"ducctest/logs").toFile().listFiles();
		for (File file : jobFolders) {
			if(file.isDirectory()){
				try{
					JSONObject tmp = new JSONObject();
					int jobId = Integer.parseInt(file.getName());
					tmp.put("jobId", jobId);
					Properties prop = new Properties();
					prop.load(new FileInputStream(Paths.get(file.getAbsolutePath(),"job-specification.properties").toFile()));
					tmp.put("name", prop.get("description"));
					tmp.put("location", getCollectionnameFromJobId(jobId));
					//				tmp.put("timestamp", descriptioncurrentObject.get("created"));
					tmp.put("jobInfo", getJobInfos(jobId));
					output.put(tmp);
				}catch(Exception e){
					//TODO: fehler besser abfangen
					e.printStackTrace();
				}
			}
		}
		return output;
	}
	
	private String getCollectionnameFromJobId(long jobId) throws IOException{
		List<String> job_specification = FileUtils.readLines(Paths.get(DUCC_HOME_HOST,"ducctest/logs/",Long.toString(jobId),"job-specification.properties").toFile());
		for (String string : job_specification) {
			if(string.contains("process_descriptor_CC_overrides=mongo_connection_collectionname")){
				String[]params = string.split(" ");
				for (String param : params) {
					String split[] = param.split("\\\\=");
					if(split[0].contains("mongo_connection_collectionname"))
						return split[1];
				}
			}
		}
		return null;
	}
	
	@Path("/listDocuments")
	@GET
	@ApiOperation(value = "List documents from job.")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success", response=JSONObject.class)
	})
	public Object listDocuments(
			@ApiParam(example = "22246") @QueryParam("jobId") long duccId,
			@ApiParam(example = "100") @QueryParam("limit")int limit, 
			@ApiParam(example = "0") @QueryParam("page")int page,
			@ApiParam(example = "test1.txt") @QueryParam("search")String search) throws IOException {
		MongoClient client = getMongoClient();
		MongoDatabase db = client.getDatabase("lab");
		System.out.println(getCollectionnameFromJobId(duccId));
		MongoCollection<Document> coll = db.getCollection(getCollectionnameFromJobId(duccId));
		Document projection = new Document("_views._InitialView.DocumentMetaData.documentId", 1);
		Document find = new Document();

		if(search != null)
			find.append("_views._InitialView.DocumentMetaData.documentId", search);

		Iterable<Document> result = null;
		if(limit > 0)
			result = coll.find(find).projection(projection).limit(limit).skip(limit*page);
		else
			result = coll.find(find).projection(projection);

		JSONObject outputObject = new JSONObject();
		JSONArray output = new JSONArray();
		for (Document document : result) {
			JSONObject tmp = new JSONObject();
			tmp.put("_id", document.get("_id", ObjectId.class).toHexString());
			tmp.put("documentId", ((Document)document.get("_views", Document.class).get("_InitialView", Document.class).get("DocumentMetaData", ArrayList.class).get(0)).getString("documentId"));
			output.put(tmp);
		}
		outputObject.put("documentIds", output);
		outputObject.put("page", page);
		client.close();
		if(limit == -1)
			return output;
		else
			return outputObject;
	}
	
	private MongoClient getMongoClient(){
		int port = 27017;
		String authDB = "admin";

		MongoCredential credential = MongoCredential.createScramSha1Credential(MONGO_CONNECTION_USER,
				authDB,
				MONGO_CONNECTION_PW.toCharArray());
		MongoClient mongoClient = new MongoClient(new ServerAddress(MONGO_CONNECTION_HOST, port), Arrays.asList(credential));
		return mongoClient;
	}

	@Path("/document")
	@GET
	@ApiOperation(value = "Get document from job. ")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success", response=String.class)
	})
	public String getDocument(@ApiParam(example = "35") @QueryParam("jobId") long duccId,@ApiParam(example = "5ef2042cda6ad56b750a728d") @QueryParam(value="_id") String _id) throws IOException, UIMAException {
		MongoClient client = getMongoClient();
		MongoDatabase db = client.getDatabase("lab");
		MongoCollection<Document> coll = db.getCollection(getCollectionnameFromJobId(duccId));
		Iterable<Document> result = coll.find(new Document("_id", new ObjectId(_id)));
		JCas cas = JCasFactory.createJCas();
		new JsonCasDeserializer().deserialize(cas,new JSONObject(result.iterator().next().toJson()));
		client.close();
		return org.hucompute.textimager.util.XmlFormatter.getPrettyString(cas.getCas());
	}


	public static void main(String...args) throws JSONException, IOException{
		DUCCAPI api = new DUCCAPI();
//		System.out.println(api.getAllJobs().toString(4));
		System.out.println(api.listDocuments(35,100,0,null));
	}
}
