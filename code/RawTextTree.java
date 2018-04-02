import java.util.ArrayList;
import java.util.List;

/**
 * loadConllFile and writeConllFile in Util are not inverses of each other
 * This class stores the dependency tree in the raw string format
 *
 * @author Pratyush Kar
 */
public class RawTextTree {
	private List<String> branches;
	private double score;
	
	public RawTextTree() { 
		branches = new ArrayList<String>();
		score = 0.0;
	}
	
	public void setScore(double score) {
		this.score = score;
	}
	
	public double getScore() {
		return score;
	}
	
	public void add(String b) {
		branches.add(b);
	}
	
	public int getNumWords() {
		return branches.size();
	}
	
	public String toString() {
		String ret = "";
		for (String l: branches) {
			ret += l + "\n";
		}
		return ret;
	}	
}
