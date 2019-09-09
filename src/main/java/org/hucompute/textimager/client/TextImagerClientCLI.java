package org.hucompute.textimager.client;

import java.io.File;
import java.io.PrintWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.uima.cas.CAS;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.hucompute.textimager.client.TextImagerOptions.IOFormat;
import org.hucompute.textimager.client.TextImagerOptions.Language;
import org.hucompute.textimager.util.XmlFormatter;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class TextImagerClientCLI {

	private final static String HELP_OPTION = "help";
	private final static String SERVICES_FILE_OPTION = "services-file";
	private final static String PIPELINE_OPTION = "pipeline";
	private final static String PIPELINE_FORCE_OPTION = "pipeline-force";
	private final static String INPUT_OPTION = "input";
	private final static String INPUT_FORMAT_OPTION = "input-format";
	private final static String INPUT_LANG_OPTION = "input-language";
	private final static String INPUT_TEXT_OPTION = "input-text";
	private final static String INPUT_FILE_SUFFIX_OPTION = "input-file-suffix";
	private final static String INPUT_FILE_ENCODING_OPTION = "input-encoding";
	private final static String OUTPUT_OPTION = "output";
	private final static String OUTPUT_FORMAT_OPTION = "output-format";
	private final static String OUTPUT_OVERWRITE_OPTION = "output-overwrite";
	private final static String OUTPUT_PRETTY_OPTION = "output-pretty";
	private final static String OUTPUT_PRINT_OPTION = "output-print";

	private static Options createOptionsProcessSingle() {
		final Options options = new Options();

		options.addOption(Option.builder("h")
				.longOpt(HELP_OPTION)
				.required(false)
				.hasArg(false)
				.desc("Print this help.")
				.build());

		options.addOption(Option.builder("s")
				.longOpt(SERVICES_FILE_OPTION)
				.required(false)
				.hasArg(true)
				.argName("path")
				.desc("XML file containing the services info.")
				.build());

		options.addOption(Option.builder("p")
				.longOpt(PIPELINE_OPTION)
				.required(true)
				.hasArg(true)
				.argName("annotators")
				.desc("The annotators, comma separated.")
				.build());
		
		options.addOption(Option.builder()
				.longOpt(PIPELINE_FORCE_OPTION)
				.required(false)
				.hasArg(false)
				.desc("Force pipeline.")
				.build());

		options.addOption(Option.builder("i")
				.longOpt(INPUT_OPTION)
				.required(false)
				.hasArg(true)
				.argName("path")
				.desc("Input file or directory to be processed.")
				.build());

		options.addOption(Option.builder("I")
				.longOpt(INPUT_TEXT_OPTION)
				.required(false)
				.hasArg(true)
				.argName("text")
				.desc("Input text to be processed.")
				.build());

		options.addOption(Option.builder("o")
				.longOpt(OUTPUT_OPTION)
				.required(true)
				.hasArg(true)
				.argName("path")
				.desc("Output file or directory.")
				.build());

		options.addOption(Option.builder()
				.longOpt(OUTPUT_OVERWRITE_OPTION)
				.required(false)
				.hasArg(false)
				.desc("Override output file (does not apply to collection).")
				.build());

		options.addOption(Option.builder()
				.longOpt(OUTPUT_PRETTY_OPTION)
				.required(false)
				.hasArg(false)
				.desc("Pretty print output.")
				.build());

		options.addOption(Option.builder()
				.longOpt(OUTPUT_PRINT_OPTION)
				.required(false)
				.hasArg(false)
				.desc("Print output.")
				.build());

		options.addOption(Option.builder()
				.longOpt(OUTPUT_FORMAT_OPTION)
				.required(false)
				.hasArg(true)
				.argName("format")
				.desc("Output file format. Supported output formats are TCF, XMI, TEI, CONLL2000, CONLL2002, CONLL2006, CONLL2009, BINARYCAS")
				.build());

		options.addOption(Option.builder()
				.longOpt(INPUT_FORMAT_OPTION)
				.required(false)
				.hasArg(true)
				.argName("format")
				.desc("Input file format.")
				.build());

		options.addOption(Option.builder()
				.longOpt(INPUT_LANG_OPTION)
				.required(false)
				.hasArg(true)
				.argName("language")
				.desc("Input language.")
				.build());

		options.addOption(Option.builder()
				.longOpt(INPUT_FILE_SUFFIX_OPTION)
				.required(false)
				.hasArg(true)
				.argName("suffix")
				.desc("Input search for file suffix.")
				.build());

		options.addOption(Option.builder()
				.longOpt(INPUT_FILE_ENCODING_OPTION)
				.required(false)
				.hasArg(true)
				.argName("encoding")
				.desc("Input source encoding.")
				.build());

		return options;
	}

	private static void printUsage(final Options options) {
		final HelpFormatter formatter = new HelpFormatter();
		final PrintWriter pw = new PrintWriter(System.out);
		formatter.printUsage(pw, 80, "TextImagerClientCLI", options);
		pw.flush();
	}

	private static void printHelp(final Options options) {
		final HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("TextImagerClientCLI", "Use the TextImagerClient to process documents with DUCC.", options, "https://github.com/texttechnologylab/textimager-client");
	}

	public static void main(String[] args) throws Exception {
		System.out.println("TextImagerClientCLI");

		final Options options = createOptionsProcessSingle();

		if (args.length < 1) {
			printHelp(options);
			System.exit(1);
		}

		CommandLine commandLine = null;
		try {
			CommandLineParser cmdLineParser = new DefaultParser();
			commandLine = cmdLineParser.parse(options, args);
		} catch (ParseException parseException) {
			System.err.println("error parsing arguments: " + parseException.getMessage());
			printHelp(options);
			System.exit(1);
		}
		if (commandLine == null) {
			System.err.println("unexpected error parsing arguments");
			printHelp(options);
			System.exit(1);
		}

		if (commandLine.hasOption(HELP_OPTION)) {
			printHelp(options);
			System.exit(0);
		}

		String servicesXmlFile = null;
		if (commandLine.hasOption(SERVICES_FILE_OPTION)) {
			servicesXmlFile = new File(commandLine.getOptionValue(SERVICES_FILE_OPTION)).getAbsolutePath();
			System.out.println("services file:" + servicesXmlFile);
		} else {
			servicesXmlFile = TextImagerClientCLI.class.getResource("/services.xml").getFile();
			System.out.println("using internal services file");
		}

		if (!commandLine.hasOption(PIPELINE_OPTION)) {
			System.err.println("error getting pipeline argument.");
			System.exit(1);
		}
		String pipeline = commandLine.getOptionValue(PIPELINE_OPTION);
		System.out.println("pipeline: " + pipeline);

		boolean allowOverwrite = commandLine.hasOption(OUTPUT_OVERWRITE_OPTION);
		System.out.println("allow overwriting output: " + allowOverwrite);

		boolean prettyPrint = commandLine.hasOption(OUTPUT_PRETTY_OPTION);
		System.out.println("pretty print: " + prettyPrint);

		boolean printOutput = commandLine.hasOption(OUTPUT_PRINT_OPTION);
		System.out.println("print output: " + printOutput);

		boolean forcePipeline = commandLine.hasOption(PIPELINE_FORCE_OPTION);
		System.out.println("force pipeline: " + forcePipeline);

		IOFormat inputFormat = IOFormat.TXT;
		if (commandLine.hasOption(INPUT_FORMAT_OPTION)) {
			try {
				inputFormat = IOFormat.valueOf(commandLine.getOptionValue(INPUT_FORMAT_OPTION));
			} catch (IllegalArgumentException ex) {
				System.err.println("error setting input format: " + ex.getMessage());
				System.exit(1);
			}
		}
		System.out.println("input format: " + inputFormat);

		// empty file suffix == use default extensions
		String fileSuffix = "";
		if (commandLine.hasOption(INPUT_FILE_SUFFIX_OPTION)) {
			fileSuffix = commandLine.getOptionValue(INPUT_FILE_SUFFIX_OPTION);
			System.out.println("fileSuffix: " + fileSuffix);
		}

		IOFormat outputFormat = IOFormat.XMI;
		if (commandLine.hasOption(OUTPUT_FORMAT_OPTION)) {
			try {
				outputFormat = IOFormat.valueOf(commandLine.getOptionValue(OUTPUT_FORMAT_OPTION));
			} catch (IllegalArgumentException ex) {
				System.err.println("error setting output format: " + ex.getMessage());
				System.exit(1);
			}
		}
		System.out.println("output format: " + outputFormat);

		Language inputLanguage = Language.unknown;
		if (commandLine.hasOption(INPUT_LANG_OPTION)) {
			try {
				inputLanguage = Language.valueOf(commandLine.getOptionValue(INPUT_LANG_OPTION));
			} catch (IllegalArgumentException ex) {
				System.err.println("error setting input language: " + ex.getMessage());
				System.exit(1);
			}
		}
		System.out.println("input language: " + inputLanguage);
		
		String sourceEncoding = "";
		if (commandLine.hasOption(INPUT_FILE_ENCODING_OPTION)) {
			try {
				sourceEncoding = commandLine.getOptionValue(INPUT_FILE_ENCODING_OPTION);
			} catch (IllegalArgumentException ex) {
				System.err.println("error setting input encoding: " + ex.getMessage());
				System.exit(1);
			}
		}
		System.out.println("input encoding: " + sourceEncoding);

		if (commandLine.hasOption(INPUT_TEXT_OPTION)) {
			// 1) input is direct text -> output must be file
			String inputText = commandLine.getOptionValue(INPUT_TEXT_OPTION);

			File outputFile = null;
			//			try {
			outputFile = new File(commandLine.getOptionValue(OUTPUT_OPTION));
			//TODO:Check file
			checkOutputFile(outputFile, allowOverwrite);
			//			} catch (Exception ex) {
			//				System.err.println("error getting output: " + ex.getMessage());
			//				ex.printStackTrace();
			//				System.exit(1);
			//			}

			processWithText(servicesXmlFile, pipeline, outputFile, outputFormat, prettyPrint, printOutput, inputText);

		} else if (commandLine.hasOption(INPUT_OPTION) && commandLine.hasOption(OUTPUT_OPTION)) {
			File inputFile = null;
			File outputFile = null;
			try {
				inputFile = new File(commandLine.getOptionValue(INPUT_OPTION));
				outputFile = new File(commandLine.getOptionValue(OUTPUT_OPTION));
			} catch (Exception ex) {
				System.err.println("error getting output: " + ex.getMessage());
				ex.printStackTrace();
				System.exit(1);
			}

			if (!inputFile.isDirectory() && !outputFile.isDirectory()) {
				// 1) input and output is file -> process input to output file
				try {
					checkOutputFile(outputFile, allowOverwrite);
				} catch (Exception ex) {
					System.err.println("error getting output: " + ex.getMessage());
					ex.printStackTrace();
					System.exit(1);
				}

				processWithFile(servicesXmlFile, pipeline, outputFile, outputFormat, prettyPrint, printOutput, inputFile);

			} else if (inputFile.isDirectory() && (!outputFile.exists() || outputFile.isDirectory())) {
				// 2) input and output is directory -> process collections
				try {
					if (!outputFile.exists()) {
						System.out.println("creatig output directory: " + outputFile.getAbsolutePath());
						outputFile.mkdirs();
					}
				} catch (Exception ex) {
					System.err.println("error creating output directory: " + ex.getMessage());
					ex.printStackTrace();
					System.exit(1);
				}

				processWithCollection(servicesXmlFile, pipeline, outputFile, outputFormat, inputFile, inputFormat, inputLanguage, forcePipeline, fileSuffix, sourceEncoding);

			} else {
				System.err.println("input and output must either both be a file or directory.");
				printHelp(options);
				System.exit(1);
			}
		} else {
			System.err.println("please specify some input and output.");
			printHelp(options);
			System.exit(1);
		}
	}

	private static void checkOutputFile(File outputFile, boolean allowOverwrite) throws Exception {
		if (outputFile.isDirectory()) {
			throw new Exception("output can not be a directory.");
		} else if (outputFile.exists()) {
			if (allowOverwrite) {
				System.out.println("overwriting output file :" + outputFile.getAbsolutePath());
			} else {
				throw new Exception("output already exists. If you want to overwrite file add the option: --" + OUTPUT_OVERWRITE_OPTION);
			}
		}
		System.out.println("output file: " + outputFile.getAbsolutePath());
	}

	private static void processWithCollection(String servicesXmlFilename, String pipeline, File outputFile, IOFormat outputFormat, File inputFile, IOFormat inputFormat, Language inputLanguage, boolean forcePipeline, String fileSuffix, String sourceEncoding) {
		System.out.println("processing collection: " + inputFile.getAbsolutePath());

		TextImagerClient client = new TextImagerClient();
		client.setConfigFile(servicesXmlFilename);
		try {
			String[] annotators = pipeline.split(",");
			client.processCollection(inputFile, inputFormat, inputLanguage, annotators, outputFormat, outputFile.getAbsolutePath(), forcePipeline, fileSuffix, sourceEncoding);
		} catch (Exception e) {
			System.err.println("error processing: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}	
	}

	private static void processWithFile(String servicesXmlFilename, String pipeline, File outputFile, IOFormat outputFormat, boolean prettyPrint, boolean printOutput, File inputFile) {
		System.out.println("processing input file: " + inputFile.getAbsolutePath());

		TextImagerClient client = new TextImagerClient();
		client.setConfigFile(servicesXmlFilename);
		try {
			CAS output = client.process(inputFile, pipeline);
			DocumentMetaData.create(output).setDocumentId("Inline Document");
			if(outputFormat == IOFormat.XMI){
				PrintWriter writer = new PrintWriter(outputFile);
				if (prettyPrint) {
					String outputString = XmlFormatter.getPrettyString(output);
					writer.print(outputString);
					if (printOutput) {
						System.out.println(outputString);
					}
				} else {
					String outputString = XmlFormatter.getString(output);
					writer.print(outputString);
					if (printOutput) {
						System.out.println(outputString);
					}
				}
				writer.flush();
				writer.close();
			}
			else
				SimplePipeline.runPipeline(output, TextImagerOptions.getWriter(outputFormat, outputFile.getPath(),true));
		} catch (Exception e) {
			System.err.println("error processing: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	private static void processWithText(String servicesXmlFilename, String pipeline, File outputFile, IOFormat outputFormat, boolean prettyPrint, boolean printOutput, String inputText) {
		System.out.println("processing input text.");

		TextImagerClient client = new TextImagerClient();
		client.setConfigFile(servicesXmlFilename);
		try {
			CAS output = client.process(inputText, pipeline);
			try{
				DocumentMetaData.create(output).setDocumentId("Inline Document");
			}catch(IllegalStateException e){
				DocumentMetaData.get(output).setDocumentId("Inline Document");
			}
			if(outputFormat == IOFormat.XMI){
				PrintWriter writer = new PrintWriter(outputFile);
				if (prettyPrint) {
					String outputString = XmlFormatter.getPrettyString(output);
					writer.print(outputString);
					if (printOutput) {
						System.out.println(outputString);
					}
				} else {
					String outputString = XmlFormatter.getString(output);
					writer.print(outputString);
					if (printOutput) {
						System.out.println(outputString);
					}
				}
				writer.flush();
				writer.close();
			}
			else
				SimplePipeline.runPipeline(output, TextImagerOptions.getWriter(outputFormat, outputFile.getPath(),true));

		} catch (Exception e) {
			System.err.println("error processing: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
