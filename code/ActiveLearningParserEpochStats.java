
public class ActiveLearningParserEpochStats {
	// epoch number
	private int epochNum;
	// number of words used for training in this epoch
	private int numTrainingWords;
	// Label attachment score (LAS) for this epoch
	private double lasScore;
	
	public ActiveLearningParserEpochStats (int epoch, int words, double las) {
		epochNum = epoch;
		numTrainingWords = words;
		lasScore = las;
	}

	public int getEpochNum() {
		return epochNum;
	}

	public void setEpochNum(int epochNum) {
		this.epochNum = epochNum;
	}

	public int getNumTrainingWords() {
		return numTrainingWords;
	}

	public void setNumTrainingWords(int numTrainingWords) {
		this.numTrainingWords = numTrainingWords;
	}

	public double getLasScore() {
		return lasScore;
	}

	public void setLasScore(double lasScore) {
		this.lasScore = lasScore;
	}
}
