package org.hucompute.textimager.client.rest;

import static spark.Spark.get;
import static spark.Spark.post;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.uima.UIMAException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.resource.ResourceInitializationException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.codehaus.plexus.util.ExceptionUtils;
import org.hucompute.textimager.client.TextImagerClient;
import org.hucompute.textimager.client.TextImagerOptions;
import org.hucompute.textimager.client.TextImagerOptions.IOFormat;
import org.hucompute.textimager.client.TextImagerOptions.Language;
import org.hucompute.textimager.uima.io.StringReader;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.servlet.SparkApplication;

@SwaggerDefinition(host = "localhost:4567", //
info = @Info(description = "TextImager API", //
version = "v0.1 Beta", //
title = "TextImager API", //
contact = @Contact(name = "Wahed Hemati", url = "https://www.texttechnologylab.org/team/wahed-hemati/") ) , //
schemes = { SwaggerDefinition.Scheme.HTTP } //
		)

//@SwaggerDefinition(host = "textimager.hucompute.org/rest", //
//info = @Info(description = "TextImager API", //
//version = "v0.2.1 Beta", //
//title = "TextImager API", //
//contact = @Contact(name = "Wahed Hemati", url = "https://www.texttechnologylab.org/team/wahed-hemati/") ) , //
//schemes = { SwaggerDefinition.Scheme.HTTPS } //
//		)

@Api
@Path("/")
public class REST implements SparkApplication{
	public REST(){
		System.setProperty("DUCC_HOME", "/home/ducc/ducc");
	}

	public static void main(String...args){
		new REST().init();
	}
	

	@Path("/language")
	@GET
	@ApiOperation(value = "Get language per document.")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success", response=JSONObject.class)
	})
	public JSONArray getLanguageMulti(@QueryParam("document") @ApiParam(name = "document", value = "Input documents", required = true)String[] document) throws Exception{
		try {
			TextImagerClient client = new TextImagerClient();
			List<CAS> output = client.processCollection(CollectionReaderFactory.createCollectionReader(StringReader.class, StringReader.PARAM_DOCUMENT_TEXT,document),TextImagerOptions.Language.unknown, new String[]{"HucomputeLanguageDetection"}, 2);
			JSONArray json = new JSONArray();
			for (CAS cas : output) {
				json.put(cas.getDocumentLanguage());
				cas.release();	
			}
			return json;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
			//			return "error"+"\n"//+ExceptionUtils.getStackTrace(e);
		}  	
	};

	@POST
	@Path("/language")
	@Consumes({"multipart/form-data"})
	@ApiOperation(value = "Get language per document. (Upload function not supported by Swagger. Please use another REST-Client!)")
	@ApiImplicitParams (value = {
			@ApiImplicitParam(dataType = "file", name = "file", required = true,paramType = "form",allowMultiple=true, type= "file", format= "binary")})
	public JSONArray getLanguageMultiFile(@ApiParam(hidden = true,name = "upload_file") File collectionPath) throws Exception{
		try {
			TextImagerClient client = new TextImagerClient();
			List<CAS> output = client.processCollection(collectionPath,IOFormat.TXT,Language.unknown,new String[]{"HucomputeLanguageDetection"}, 2);
			JSONArray json = new JSONArray();
			for (CAS cas : output) {
				json.put(cas.getDocumentLanguage());
				cas.release();	
			}
			return json;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
			//			return "error"+"\n"//+ExceptionUtils.getStackTrace(e);
		}  	
	};

	@Path("/process")
	@GET
	@ApiOperation(value = "Process documents with given pipeline.")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success", response=JSONObject.class)
	})
	public JSONArray process(
			@QueryParam("document") @ApiParam(name = "document", value = "Input documents", required = true)String[] document,
			@QueryParam("pipeline") @ApiParam(name = "pipeline", value = "Pipeline", required = true)String[] pipeline,
			@QueryParam("language") @ApiParam(name = "language", value = "Language", required = true, allowableValues="en,de,la")String language,
			@QueryParam("outputFormat") @ApiParam(name = "outputFormat", value = "Output Format", defaultValue="XMI", required = false,allowableValues="TCF,XMI,CONLL2000,CONLL2002,CONLL2006,CONLL2009,CONLL2012,CONLLU,TEI")String outputFormat, 
			@ApiParam(hidden = true)Response res) throws ResourceInitializationException, Exception{
		java.nio.file.Path tmpFolder = Files.createTempDirectory("textImager");

		JSONArray json = new JSONArray();
		
		TextImagerClient client = new TextImagerClient();
		
		ExceptionCollectorListener listener = new ExceptionCollectorListener();
		
		try {
			client.processCollection(
				CollectionReaderFactory.createCollectionReader(StringReader.class, StringReader.PARAM_DOCUMENT_TEXT,document,StringReader.PARAM_LANGUAGE,language),
				TextImagerOptions.Language.valueOf(language), 
				pipeline, 
				2,
				TextImagerOptions.getWriter(IOFormat.valueOf(outputFormat), tmpFolder.toFile().toString()),
				listener
			);
		
			for (File file: tmpFolder.toFile().listFiles()) {
				if(file.getName().toLowerCase().startsWith("typesystem"))
					continue;
				json.put(FileUtils.readFileToString(file));
			}

			if (listener.hasErrors()) {
				for (Map.Entry<String, CasError> error : listener.getErrors().entrySet()) {
					JSONObject jsonError = new JSONObject();
					jsonError.put("docId", error.getValue().docId);
					jsonError.put("status", error.getValue().status);
					jsonError.put("type", "cas_error");

					JSONObject jsonST = new JSONObject();
					for (Map.Entry<String, String> stacktrace : error.getValue().exceptions.entrySet())
					{
						jsonST.put(stacktrace.getKey(), stacktrace.getValue());
					}
					jsonError.put("exceptions", jsonST);
					
					json.put(jsonError);
				}
				res.status(400);
			}
		}
		catch (Exception ex) {
			JSONObject jsonError = new JSONObject();
			jsonError.put("type", "error");

			JSONObject jsonST = new JSONObject();

			String fullst = ExceptionUtils.getFullStackTrace(ex);
			
			ex.printStackTrace();
			
			if (fullst.contains("could not find service")) {
				jsonST.put("Service Not Defined Error", fullst);
			}
			else if (fullst.contains("No enum constant org.hucompute.textimager.client.TextImagerOptions.Language")) {
				jsonST.put("Language Input Error", fullst);
			}
			else {
				jsonST.put("Unknown Error", fullst);
			}
			jsonError.put("exceptions", jsonST);
			
			json.put(jsonError);
			
			res.status(400);
		}
		
		FileUtils.deleteDirectory(tmpFolder.toFile());
		return json;
	}

	
	@POST
	@Path("/process")
	@Consumes({"multipart/form-data"})
	@ApiOperation(value = "Process documents with given pipeline. (Upload function not supported by Swagger. Please use another REST-Client!)")
	@ApiImplicitParams (value = {
			@ApiImplicitParam(dataType = "file", name = "file", required = true,paramType = "form",allowMultiple=true, type= "file", format= "binary")})
	public JSONArray processFile(
			@ApiParam(hidden = true,name = "upload_file") File collectionPath,
			@QueryParam("pipeline") @ApiParam(name = "pipeline", value = "Pipeline", required = true)String[] pipeline,
			@QueryParam("language") @ApiParam(name = "language", value = "Language", required = true,allowableValues="en,de,la")String language,
			@QueryParam("outputFormat") @ApiParam(name = "outputFormat", value = "Output Format", defaultValue="XMI", required = false,allowableValues="TCF,XMI,CONLL2000,CONLL2002,CONLL2006,CONLL2009,CONLL2012,CONLLU,TEI")String outputFormat) throws ResourceInitializationException, Exception{
		try {
			java.nio.file.Path tmpFolder = Files.createTempDirectory("textImager");

			TextImagerClient client = new TextImagerClient();
			client.processCollection(
					collectionPath,
					IOFormat.TXT,
					Language.valueOf(language),
					pipeline,
					IOFormat.valueOf(outputFormat),
					tmpFolder.toFile().toString());
			JSONArray json = new JSONArray();
			for (File file: tmpFolder.toFile().listFiles()) {
				if(file.getName().toLowerCase().startsWith("typesystem"))
					continue;
				json.put(FileUtils.readFileToString(file));
			}
			FileUtils.deleteDirectory(tmpFolder.toFile());
			return json;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
			//			return "error"+"\n"//+ExceptionUtils.getStackTrace(e);
		}  	
	};
	
	private java.nio.file.Path saveRequestFiles(Request req) throws Exception{
		int maxFileSize = 100000000;  // the maximum size allowed for uploaded files
		java.nio.file.Path tmpFolder = Files.createTempDirectory("textImager");


		// apache commons-fileupload to handle file upload
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(maxFileSize);

		factory.setRepository(tmpFolder.toFile());

		ServletFileUpload fileUpload = new ServletFileUpload(factory);
		fileUpload.setFileSizeMax(100000000000l);

		fileUpload.setHeaderEncoding("UTF-8");

		List<FileItem> items = fileUpload.parseRequest(req.raw());
		fileUpload.getItemIterator(req.raw());
		for (FileItem fileItem : items) {
			File tmp = java.nio.file.Paths.get(tmpFolder.toString(),fileItem.getName()).toFile();
			fileItem.write(tmp);
		}
		return tmpFolder;
	}
	
	
	static String jobFilename(long duccId) {
		return "/home/ducc/ducc/ducctest/logs/" + Long.toString(duccId) + ".job";
	}
	
	
	/**
	 * Database zeugs
	 */

	MongoCredential credential = null;

	MongoClient mongoClient = null;
	MongoDatabase db = mongoClient.getDatabase("lab");

	@Path("/database/aggregate")
	@GET
	@ApiOperation(value = "Get Document from database")
	@ApiResponses(value = { 
			@ApiResponse(code = 200, message = "Success", response=JSONObject.class)
	})
	public JSONArray aggregate(
			@QueryParam("collectionId") @ApiParam(name = "collectionId", value = "Collection Id", required = true)String collectionId,
			@QueryParam("documentId") @ApiParam(name = "documentId", value = "Document Id", required = true)String documentId,
			String mongoQuery){

		MongoCollection<Document> coll = db.getCollection(collectionId);
		BasicDBObject id = new BasicDBObject();
		id.put("_id", new ObjectId(documentId));
		List<Document> q= new ArrayList<>();
		JSONArray mongoQueryJson = new JSONArray(mongoQuery);
		for (Object object : mongoQueryJson) {
			q.add(Document.parse(object.toString()));
		}
		q.add(0, Document.parse("{$match:"+id+"}"));
		JSONArray output = new JSONArray();
		for (Document object : coll.aggregate(q)) {
			output.put(new JSONObject(object.toJson()));
		}
		System.out.println(output.toString(4));
		return output;
	}
	
	/**
	 * 
	 * @param args
	 */
	@Override
	public void init() {
		System.setProperty("DUCC_HOME", "/home/ducc/ducc");
		Spark.staticFileLocation("html");
		try {
			// Build swagger json description
			final String swaggerJson = SwaggerParser.getSwaggerJson("org.hucompute.textimager.client.rest");
			get("/swagger", (req, res) -> {
				return swaggerJson;
			});

		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}

		get("doku", (req, res) -> {
			res.redirect("doku/"); return null;
		});

		get("language", (req, res) -> {
			res.status(200);
			res.type("application/json");
			return getLanguageMulti(req.queryMap().toMap().get("document"));
		});

		post("language", (req, res) -> {
			java.nio.file.Path tmpFolder = saveRequestFiles(req);
			JSONArray output = getLanguageMultiFile(tmpFolder.toFile());
			FileUtils.deleteDirectory(tmpFolder.toFile());
			res.status(200);
			res.type("application/json");
			return output;
		});

		get("process", (req, res) -> {
			res.status(200);
			res.type("application/xml");
			return process(req.queryMap().toMap().get("document"),req.queryMap().toMap().get("pipeline"),req.queryParams("language"),req.queryParams("outputFormat"),res);
		});

		post("process", (req, res) -> {
			java.nio.file.Path tmpFolder = saveRequestFiles(req);
			JSONArray output = processFile(tmpFolder.toFile(),req.queryMap().toMap().get("pipeline"),req.queryParams("language"),req.queryParams("outputFormat"));
			FileUtils.deleteDirectory(tmpFolder.toFile());
			res.status(200);
			res.type("application/json");
			return output;
		});
		
		post("/database/aggregate", (request, response) -> {
			response.status(200);
			response.type("application/json");
			System.out.println(request.body());
			return aggregate(request.queryParams("collectionId"),request.queryParams("documentId"),request.body());
		});
	}
}
