# CS388-NLP-HW3
HW3 of the Natural Language Processing course: Active Learning for Neural Dependency Parsing

~~~~
$ java -cp stanford-corenlp.jar edu.stanford.nlp.parser.nndep.DependencyParser \
-trainFile data/wsj_01_03.conllx \
-testFile data/wsj_20.conllx \
-embedFile data/en-cw.txt -embeddingSize 50 \
-model outputs/output_model \
-maxIter 50 \
-outFile outputs/annotations.conllx
~~~~

~~~~
cat wsj-conllx/00/* > wsj_00.conllx
cat `find . -regex './wsj\-conllx\/0[1-3]\/.*'` > wsj_01_03.conllx
cat wsj-conllx/20/* > wsj_20.conllx
~~~~