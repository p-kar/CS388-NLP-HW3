#!/usr/bin/env bash

java -cp .:stanford-corenlp.jar ActiveLearningDependencyParser \
-mode train \
-seedPath data/wsj_00.conllx \
-trainPath data/wsj_01_03.conllx \
-testPath data/wsj_20.conllx \
-oraclePolicy margin \
-modelPath outputs/model_margin.model \
-statsFile outputs/stats_margin.csv \
-embeddingPath data/en-cw.txt \
-tmpDirPath tmp_margin/ \
-seedSetSize 50 \
-maxIter 500 \
-maxALEpochs 20 \
-maxNewLabels 1500

