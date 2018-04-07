#!/usr/bin/env bash

java -cp .:stanford-corenlp.jar ActiveLearningDependencyParser \
-mode train \
-seedPath data/wsj_00.conllx \
-trainPath data/wsj_01_03.conllx \
-testPath data/wsj_20.conllx \
-oraclePolicy raw \
-modelPath outputs/model_raw.model \
-statsFile outputs/stats_raw.csv \
-embeddingPath data/en-cw.txt \
-tmpDirPath tmp_raw/ \
-seedSetSize 50 \
-maxIter 500 \
-maxALEpochs 20 \
-maxNewLabels 1500

