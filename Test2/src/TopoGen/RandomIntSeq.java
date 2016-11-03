package TopoGen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomIntSeq {
	List<Integer> _randomSeq = new ArrayList<>();
	int nextIndex = 0;
	
	public RandomIntSeq() {
	}
	
	public void add(int value) {
		_randomSeq.add(value);
	}
	public void clear() {
		_randomSeq.clear();
	}
	public void size() {
		_randomSeq.size();
	}
	public void resetGen(){
		nextIndex  = 0;
		shuffle();
	}
	public int get(){
		if (_randomSeq.size() == 0) {
			throw new RuntimeException(
					"Random sequnce generatior is not intilizes");
		}
		shuffle();
		int result = _randomSeq.get(nextIndex);
		nextIndex += 1;
		if (nextIndex == _randomSeq.size()){
			resetGen();
		}
		return result;
	}
	
	public List<Integer> getSequnce() {
		return _randomSeq;
	}

	private void shuffle() {
		Random rnd = ThreadLocalRandom.current();
	    for (int i = _randomSeq.size() - 1; i > 0; i--)
	    {
	      int index = rnd.nextInt(i + 1);
	      // Simple swap
	      int a = _randomSeq.get(index);
	      _randomSeq.set(index, _randomSeq.get(i));
	      _randomSeq.set(i, a);
	    }
	}
}
