import edu.stanford.nlp.parser.nndep.DependencyParser;
import edu.stanford.nlp.parser.nndep.DependencyTree;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.nndep.Config;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.util.Properties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * This class is an active learning wrapper over the DependencyParser
 * which is part of the Stanford CoreNLP group
 *
 * @author Pratyush Kar
 */
public class ActiveLearningDependencyParser {
	private static void deleteDir(File file) {
	    File[] contents = file.listFiles();
	    if (contents != null) {
	        for (File f : contents) {
	            deleteDir(f);
	        }
	    }
	    file.delete();
	}

	private static void makeTempDir(String path) {
		File dir = new File(path);
		// check if directory exists
		if (dir.exists()) {
			System.out.print("tmp directory exists. Deleting directory...");
			// deleting directory
			deleteDir(dir);
			System.out.println(" done");
		}
		// attempt to create the directory here
		boolean successful = dir.mkdir();
		if (successful) {
	      // creating the directory succeeded
	      System.out.println("tmp directory was created successfully");
	    }
	    else {
	      // creating the directory failed
	      System.out.println("Failed trying to create tmp directory");
	    }
	}
	
	private static List<DependencyTree> runDependencyParserEpoch(int epochNum, int numWords,
			Properties prop, String trainPath, String testPath, String modelPath, 
			String embeddingPath, List<ActiveLearningParserEpochStats> stats) {
		System.out.printf("\n############### EPOCH %d ################\n", epochNum);
		System.out.println("Num. training words: " + numWords);
		// train the model on the training set and write it to modelPath
        DependencyParser p = new DependencyParser(prop);
        p.train(trainPath, null, modelPath, embeddingPath);
        	// Load a saved model
 		DependencyParser model = DependencyParser.loadFromModelFile(modelPath);
 		// Test model on test data
 		double las = model.testCoNLL(testPath, null);
 		// add epoch statistics to the list
 		stats.add(new ActiveLearningParserEpochStats(epochNum, numWords, las));
 		// returns parse trees for all the sentences in test data using model
 		List<DependencyTree> predictedParses = model.testCoNLLProb(testPath);
 		
 		return predictedParses;
	}
	
	public static void loadConllFileRaw(String inFile, List<RawTextTree> rawTrees,
			boolean unlabeled, boolean cPOS)
	  {
	    CoreLabelTokenFactory tf = new CoreLabelTokenFactory(false);

	    try (BufferedReader reader = IOUtils.readerFromString(inFile)) {

	      List<CoreLabel> sentenceTokens = new ArrayList<>();
	      DependencyTree tree = new DependencyTree();
	      RawTextTree rtree = new RawTextTree();

	      for (String line : IOUtils.getLineIterable(reader, false)) {
	        String[] splits = line.split("\t");
	        if (splits.length < 10) {
	          if (sentenceTokens.size() > 0) {
	            rawTrees.add(rtree);
	            CoreMap sentence = new CoreLabel();
	            sentence.set(CoreAnnotations.TokensAnnotation.class, sentenceTokens);
	            tree = new DependencyTree();
	            rtree = new RawTextTree();
	            sentenceTokens = new ArrayList<>();
	          }
	        } else {
	          String word = splits[1],
	                  pos = cPOS ? splits[3] : splits[4],
	                  depType = splits[7];

	          int head = -1;
	          try {
	            head = Integer.parseInt(splits[6]);
	          } catch (NumberFormatException e) {
	            continue;
	          }

	          CoreLabel token = tf.makeToken(word, 0, 0);
	          token.setTag(pos);
	          token.set(CoreAnnotations.CoNLLDepParentIndexAnnotation.class, head);
	          token.set(CoreAnnotations.CoNLLDepTypeAnnotation.class, depType);
	          sentenceTokens.add(token);

	          if (!unlabeled) {
	            tree.add(head, depType);
	          	rtree.add(line);
	          }
	          else {
	            tree.add(head, Config.UNKNOWN);
	            rtree.add(line);
	          }
	        }
	      }
	    } catch (IOException e) {
	      throw new RuntimeIOException(e);
	    }
	  }
	
	public static void writeConllFileRaw(String outFile, List<RawTextTree> rtrees) {
		try {
	      PrintWriter output = IOUtils.getPrintWriter(outFile);
	      for (int i = 0; i < rtrees.size(); i++) {
	    	    output.println(rtrees.get(i));
	      }
	      output.close();
	    }
	    catch (Exception e) {
	      throw new RuntimeIOException(e);
	    }
	}
	
	public static void writeStatsFile(String outFile, List<ActiveLearningParserEpochStats> stats) {
		try {
	      PrintWriter output = IOUtils.getPrintWriter(outFile);
	      output.println("Epoch, NumTrainingWords, LAS");
	      for (int i = 0; i < stats.size(); i++) {
	    	  	ActiveLearningParserEpochStats s = stats.get(i);
	    	    output.println(s.getEpochNum() + ", " + s.getNumTrainingWords() + ", " + s.getLasScore());
	      }
	      output.close();
	    }
	    catch (Exception e) {
	      throw new RuntimeIOException(e);
	    }
	}

    public static void main(String[] args) {
    		Properties props = StringUtils.argsToProperties(args);
    		// Parser mode (train, test)
    		String mode = "train";
    		if (props.containsKey("mode")) {
    			mode = props.getProperty("mode");
    		}
    		// Seed data path (chooses the first 50 sentences to initialize the parser)
    		String seedPath = "data/wsj_00.conllx";
    		if (props.containsKey("seedPath")) {
    			seedPath = props.getProperty("seedPath");
    		}
        // Training data path
        String trainPath = "data/wsj_01_03.conllx";
        if (props.containsKey("trainPath")) {
			trainPath = props.getProperty("trainPath");
		}
        // Test data Path
        String testPath = "data/wsj_20.conllx";
        if (props.containsKey("testPath")) {
			testPath = props.getProperty("testPath");
		}
        // Path to embedding vectors file
        String embeddingPath = "data/en-cw.txt";
        if (props.containsKey("embeddingPath")) {
			embeddingPath = props.getProperty("embeddingPath");
		}
        // Path where model is to be saved
        String modelPath = "outputs/model_margin";
        if (props.containsKey("modelPath")) {
			modelPath = props.getProperty("modelPath");
		}
        // Path where test data annotations are stored
        String testAnnotationsPath = "outputs/test_annotation.conllx";
        if (props.containsKey("testAnnotationsPath")) {
        		testAnnotationsPath = props.getProperty("testAnnotationsPath");
		}
        // File path where training stats for the parser are stored
        String statsFile = "outputs/stats_margin.csv";
        if (props.containsKey("statsFile")) {
        		statsFile = props.getProperty("statsFile");
		}
        // Active learning oracle policy (random, length, raw, margin)
        String oraclePolicy = "random";
        if (props.containsKey("oraclePolicy")) {
        		oraclePolicy = props.getProperty("oraclePolicy");
		}
        // tmp directory path
        String tmpDirPath = "tmp/";
        if (props.containsKey("tmpDirPath")) {
        		tmpDirPath = props.getProperty("tmpDirPath");
		}
        // annotated trees file path (used for training by the active learner)
        String labelledPath = "tmp/labelled.conllx";
        if (props.containsKey("labelledPath")) {
        		labelledPath = props.getProperty("labelledPath");
		}
        // unannotated trees file path (used for testing by the active learner)
        String unlabelledPath = "tmp/unlabelled.conllx";
        if (props.containsKey("unlabelledPath")) {
        		unlabelledPath = props.getProperty("unlabelledPath");
		}
        // initially seed set size
        int seedSetSize = 50;
        if (props.containsKey("seedSetSize")) {
        		seedSetSize = Integer.parseInt(props.getProperty("seedSetSize"));
		}
        // max iterations for each epoch
        String maxIter = "25";
        if (props.containsKey("maxIter")) {
	    		maxIter = props.getProperty("maxIter");
		}
        // max number of active learning epochs
        int maxALEpochs = 100;
        if (props.containsKey("maxALEpochs")) {
        		maxALEpochs = Integer.parseInt(props.getProperty("maxALEpochs"));
		}
        // max number of labelled words introduced in each epoch
        int maxNewLabels = 1500;
        if (props.containsKey("maxNewLabels")) {
        		maxNewLabels = Integer.parseInt(props.getProperty("maxNewLabels"));
		}
        
        System.out.println("############### ACTIVE LEARNING NEURAL DEPENDENCY PARSER ###############");
        System.out.println("CONFIGURATION:-");
        System.out.println("mode:                " + mode);
        System.out.println("seedPath:            " + seedPath);
        System.out.println("trainPath:           " + trainPath);
        System.out.println("testPath:            " + testPath);
        System.out.println("oraclePolicy:        " + oraclePolicy);
        System.out.println("modelPath:           " + modelPath);
        System.out.println("statsFile:           " + statsFile);
        System.out.println("embeddingPath:       " + embeddingPath);
        System.out.println("testAnnotationsPath: " + testAnnotationsPath);
        System.out.println("tmpDirPath:          " + tmpDirPath);
        System.out.println("labelledPath:        " + labelledPath);
        System.out.println("unlabelledPath:      " + unlabelledPath);
        System.out.println("seedSetSize:         " + seedSetSize);
        System.out.println("maxIter:             " + maxIter);
        System.out.println("maxALEpochs:         " + maxALEpochs);
        System.out.println("maxNewLabels:        " + maxNewLabels);
        System.out.println();
        
        // initalize classifier properties
        Properties prop = new Properties();
        prop.setProperty("maxIter", maxIter);
        Config config = new Config(prop);
        
        // Create temporary directory
        makeTempDir(tmpDirPath);
        
        // Initialize labelled and unlabelled data
        List<RawTextTree> rawTrees = new ArrayList<>();
        loadConllFileRaw(seedPath, rawTrees, config.unlabeled, config.cPOS);
        // choose the first seedSetSize trees for the seed set
        rawTrees = rawTrees.subList(0, Math.min(rawTrees.size(), seedSetSize));
        writeConllFileRaw(labelledPath, rawTrees);
        // calculate the number of labelled words in the training set
        int numTrainingWords = 0;
        for (int i = 0; i < rawTrees.size(); ++i)
        		numTrainingWords += rawTrees.get(i).getNumWords();
        // put all training set trees in the unlabelled set initially 
        rawTrees = new ArrayList<>();
        loadConllFileRaw(trainPath, rawTrees, config.unlabeled, config.cPOS);
        writeConllFileRaw(unlabelledPath, rawTrees);
        
        // set up arrays for recording metrics
        List<ActiveLearningParserEpochStats> stats = new ArrayList<ActiveLearningParserEpochStats>();
        
        for (int i = 0; i < maxALEpochs; ++i) {
        		
        		// train the classifier on the labelled examples
        		List<DependencyTree> predictedParses = runDependencyParserEpoch(i, numTrainingWords,
        				prop, labelledPath, unlabelledPath, modelPath, embeddingPath, stats);
        		
        		// read labelled trees in memory
			List<RawTextTree> rawLabelledTrees = new ArrayList<>();
			loadConllFileRaw(labelledPath, rawLabelledTrees, config.unlabeled, config.cPOS);
			// read unlabelled trees in memory
			List<RawTextTree> rawUnlabelledTrees = new ArrayList<>();
			loadConllFileRaw(unlabelledPath, rawUnlabelledTrees, config.unlabeled, config.cPOS);
			System.out.println("Labelled Trees:" + rawLabelledTrees.size());
			System.out.println("Unlabelled Trees:" + rawUnlabelledTrees.size());
			
			// terminate epochs if no unlabelled trees left
			if (rawUnlabelledTrees.size() == 0) {
				break;
			}
        		
			// update the set of labelled trees based on oracle policy
        		switch (oraclePolicy) {
        			case "random":
        				// Choose sentences randomly and add to the labelled set
        				Collections.shuffle(rawUnlabelledTrees);
        				break;
        			
        			case "length":
        				// Choose sentences based on their lengths
        				// maximum length sentences chosen for the next epoch
        				Comparator<RawTextTree> cmp_length = new Comparator<RawTextTree>() {
        					public int compare(RawTextTree t1, RawTextTree t2) {
        						// descending order
        						return t2.getNumWords() - t1.getNumWords();
        					}
        				};
        				Collections.sort(rawUnlabelledTrees, cmp_length);
        				break;
        			
        			case "raw":
        				// Choose sentences based on the raw probability score
        				// sentences with maximum raw uncertainity chosen for the next epoch
        				for (int j = 0; j < predictedParses.size(); ++j) {
        					RawTextTree tree = rawUnlabelledTrees.get(j);
        					tree.setScore(predictedParses.get(j).RawScore);
        				}
        				Comparator<RawTextTree> cmp_raw = new Comparator<RawTextTree>() {
        					public int compare(RawTextTree t1, RawTextTree t2) {
        						// ascending order
        						if (t1.getScore() > t2.getScore())
        							return 1;
        						else
        							return -1;
        					}
        				};
        				Collections.sort(rawUnlabelledTrees, cmp_raw);
        				break;
        			case "margin":
        				// Choose sentences based on the raw probability score
        				// sentences with maximum raw uncertainity chosen for the next epoch
        				for (int j = 0; j < predictedParses.size(); ++j) {
        					RawTextTree tree = rawUnlabelledTrees.get(j);
        					tree.setScore(predictedParses.get(j).MarginScore);
        				}
        				Comparator<RawTextTree> cmp_margin = new Comparator<RawTextTree>() {
        					public int compare(RawTextTree t1, RawTextTree t2) {
        						// ascending order
        						if (t1.getScore() > t2.getScore())
        							return 1;
        						else
        							return -1;
        					}
        				};
        				Collections.sort(rawUnlabelledTrees, cmp_margin);
        				break;
        			default:
        				System.out.println("Error: Oracle policy should be one of - [random, length, raw, margin]");
        				return;
        		}
        		int idx = 0;
			int numNewWords = 0;
			while (numNewWords < maxNewLabels && idx < rawUnlabelledTrees.size()) {
				RawTextTree tree = rawUnlabelledTrees.get(idx);
				rawLabelledTrees.add(tree);
				idx++;
				numNewWords += tree.getNumWords();
				numTrainingWords += tree.getNumWords();
			}
			// update labelled data
			writeConllFileRaw(labelledPath, rawLabelledTrees);
			// update unlabelled data
			writeConllFileRaw(unlabelledPath, rawUnlabelledTrees.subList(idx, rawUnlabelledTrees.size()));
        }
        // write stats file with training stats
        writeStatsFile(statsFile, stats);
    }
}
