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
import org.hucompute.textimager.util.XmlFormatter;
import org.parboiled.common.FileUtils;

public class TextImagerClientCLI {

	private final static String HELP_OPTION = "help";
	private final static String SERVICES_FILE_OPTION = "services-file";
	private final static String PIPELINE_OPTION = "pipeline";
	private final static String INPUT_OPTION = "input";
	private final static String INPUT_TEXT_OPTION = "input-text";
	private final static String OUTPUT_OPTION = "output";
	private final static String OUTPUT_OVERWRITE_OPTION = "output-overwrite";

	private static Options createOptionsProcessSingle() {
		final Options options = new Options();

		options.addOption(Option.builder("h")
				.longOpt(HELP_OPTION)
				.required(false)
				.hasArg(true)
				.desc("Print this help.")
				.build());

		options.addOption(Option.builder("s")
				.longOpt(SERVICES_FILE_OPTION)
				.required(false)
				.hasArg(true)
				.desc("XML file containing the services info.")
				.build());

		options.addOption(Option.builder("p")
				.longOpt(PIPELINE_OPTION)
				.required(true)
				.hasArg(true)
				.desc("The annotators, comma separated.")
				.build());

		options.addOption(Option.builder("i")
				.longOpt(INPUT_OPTION)
				.required(false)
				.hasArg(true)
				.desc("Input file or directory to be processed.")
				.build());
		
		options.addOption(Option.builder("I")
				.longOpt(INPUT_TEXT_OPTION)
				.required(false)
				.hasArg(true)
				.desc("Input text to be processed.")
				.build());

		options.addOption(Option.builder("o")
				.longOpt(OUTPUT_OPTION)
				.required(true)
				.hasArg(true)
				.desc("Output file or directory.")
				.build());

		options.addOption(Option.builder()
				.longOpt(OUTPUT_OVERWRITE_OPTION)
				.required(false)
				.hasArg(false)
				.desc("Override output file (does not apply to collection).")
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

	public static void main(String[] args) {
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
		
		String pipelineArg = commandLine.getOptionValue(PIPELINE_OPTION);
		String[] pipeline = null;
		try {
			pipeline = pipelineArg.split(" ", -1);
		} catch (Exception ex) {
			System.err.println("error parsing pipeline: " + ex.getMessage());
			ex.printStackTrace();
			System.exit(1);
		}
		System.out.println("pipeline: " + pipelineArg);
		
		boolean allowOverwrite = commandLine.hasOption(OUTPUT_OVERWRITE_OPTION);
		System.out.println("allow overwriting output: " + allowOverwrite);
		
		if (commandLine.hasOption(INPUT_TEXT_OPTION)) {
			String inputText = commandLine.getOptionValue(INPUT_TEXT_OPTION);
			System.out.println("input text: " + inputText);
			
			File outputFile = null;
			try {
				outputFile = new File(commandLine.getOptionValue(OUTPUT_OPTION));
				if (outputFile.isDirectory()) {
					throw new Exception("output can not be a directory.");
				} else if (outputFile.exists()) {
					if (allowOverwrite) {
						System.out.println("overwriting output file :" + outputFile.getAbsolutePath());
					} else {
						throw new Exception("output already exists.");
					}
				}
				System.out.println("output file: " + outputFile.getAbsolutePath());
			} catch (Exception ex) {
				System.err.println("error getting output: " + ex.getMessage());
				ex.printStackTrace();
				System.exit(1);
			}

			processWithText(servicesXmlFile, pipeline, outputFile, inputText);
			
		} else if (commandLine.hasOption(INPUT_OPTION)) {
			
			// TODO cleanup, add collection processing
			File inputFile = null;
			File outputFile = null;
			try {
				inputFile = new File(commandLine.getOptionValue(INPUT_OPTION));
				outputFile = new File(commandLine.getOptionValue(OUTPUT_OPTION));
				if (!inputFile.isDirectory() && !outputFile.isDirectory()) {
					if (outputFile.exists()) {
						if (allowOverwrite) {
							System.out.println("overwriting output file :" + outputFile.getAbsolutePath());
						} else {
							throw new Exception("output already exists.");
						}
					}
					System.out.println("output file: " + outputFile.getAbsolutePath());

					System.out.println("input text from file: " + inputFile.getAbsolutePath());
					String inputText = FileUtils.readAllText(inputFile);
					
					processWithText(servicesXmlFile, pipeline, outputFile, inputText);
				} else {
					throw new Exception("input or output can not be a directory.");
				}
			} catch (Exception ex) {
				System.err.println("error getting output: " + ex.getMessage());
				ex.printStackTrace();
				System.exit(1);
			}

			
		} else {
			System.err.println("please specify some input.");
			printHelp(options);
			System.exit(1);
		}
	}
	
	private static void processWithText(String servicesXmlFilename, String[] pipeline, File outputFile, String inputText) {
		TextImagerClient client = new TextImagerClient();
		client.setConfigFile(servicesXmlFilename);
		try {
			CAS output = client.process(inputText, pipeline);
			PrintWriter writer = new PrintWriter(outputFile);
			writer.print(XmlFormatter.getPrettyString(output));
			writer.flush();
			writer.close();
		} catch (Exception e) {
			System.err.println("error processing: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}
}
