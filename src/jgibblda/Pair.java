package jgibblda;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class Pair <T1,T2> implements Comparable<Pair<T1,T2>>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -627033812379793927L;
	
	  /**
	   * Direct access is deprecated.  Use first().
	   *
	   * @serial
	   */
	  public T1 first;

	  /**
	   * Direct access is deprecated.  Use second().
	   *
	   * @serial
	   */
	  public T2 second;

	  public Pair() {
	    // first = null; second = null; -- default initialization
	  }

	  public Pair(T1 first, T2 second) {
	    this.first = first;
	    this.second = second;
	  }

	  public T1 getFirst() {
	    return first;
	  }

	  public T2 getSecond() {
	    return second;
	  }

	  public void setFirst(T1 o) {
	    first = o;
	  }

	  public void setSecond(T2 o) {
	    second = o;
	  }

	  @Override
	  public String toString() {
	    return "(" + first + "-" + second + ")";
	  }


	public boolean same(T1 t1, T2 t2) {
		return t1 == null ? t2 == null : t1.equals(t2);
	}


	  @Override
	  @SuppressWarnings("unchecked")
	  public boolean equals(Object o) {
	    if (o instanceof Pair) {
	      Pair p = (Pair) o;
	      return (first == null ? p.first == null : first.equals(p.first)) && (second == null ? p.second == null : second.equals(p.second));
	    } else {
	      return false;
	    }
	  }

	  /**
	   * Compares this <code>Pair</code> to another object.
	   * If the object is a <code>Pair</code>, this function will work providing
	   * the elements of the <code>Pair</code> are themselves comparable.
	   * It will then return a value based on the pair of objects, where
	   * <code>p &gt; q iff p.first() &gt; q.first() ||
	   * (p.first().equals(q.first()) && p.second() &gt; q.second())</code>.
	   * If the other object is not a <code>Pair</code>, it throws a
	   * <code>ClassCastException</code>.
	   *
	   * @param another the <code>Object</code> to be compared.
	   * @return the value <code>0</code> if the argument is a
	   *         <code>Pair</code> equal to this <code>Pair</code>; a value less than
	   *         <code>0</code> if the argument is a <code>Pair</code>
	   *         greater than this <code>Pair</code>; and a value
	   *         greater than <code>0</code> if the argument is a
	   *         <code>Pair</code> less than this <code>Pair</code>.
	   * @throws ClassCastException if the argument is not a
	   *                            <code>Pair</code>.
	   * @see java.lang.Comparable
	   */
	  @SuppressWarnings("unchecked")
	  public int compareTo(Pair<T1,T2> another) {
	    int comp = ((Comparable<T1>) getFirst()).compareTo(another.getFirst());
	    if (comp != 0) {
	      return comp;
	    } else {
	      return ((Comparable<T2>) getSecond()).compareTo(another.getSecond());
	    }
	  }
	
	//redefine hashCode is important since only this way, HashMap can directly be used with Pair as 
	//the key.
	private volatile int hashCode = 0; // (See Item 48)
	  @Override
	  public int hashCode() {
	    if (hashCode == 0) {      
	      int firstHash  = (first == null ? 0 : first.hashCode());
	      int secondHash = (second == null ? 0 : second.hashCode());
	        
	      hashCode = firstHash*31 + secondHash;      
	    }
	    return hashCode;
	  }
	  
		
	
    public static void main(String[] args) {
		SortedMap<Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>,Pair<String,String>> linkMap = 
			new TreeMap<Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>,Pair<String,String>>();
        Pair[] nameArray = {
//            new Pair("John", "Lennon"),
//            new Pair("Karl", "Marx"),
//            new Pair("Groucho", "Marx"),
//            new Pair("Oscar", "Grouch"),
            new Pair(20, "Lennon"),
            new Pair(10, "Marx"),
            new Pair(15, "Marx"),
            new Pair(8, "Grouch"),
            new Pair(10, "Lennon")
        };
        Pair testPair = new Pair(20,"Lennon");
        List<Pair> names = Arrays.asList(nameArray);
        Collections.sort(names);
        System.out.println(names);
        System.out.println(names.contains(testPair));
        
        Pair[] numArr = {
              new Pair(20, 5),
              new Pair(20, 15),
              new Pair(10, 12),
              new Pair(10, 19),
              new Pair(15, 6),
              new Pair(15, 16),
              new Pair(8, 7),
              new Pair(8, 27),
              new Pair(8, 4),
              new Pair(8, 10),
              new Pair(10, 15),
              new Pair(10, 25)
          };
        Pair<Pair<Integer,Integer>,Pair<Integer,Integer>> pairOfPair1 = new Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>();
        pairOfPair1.setFirst(numArr[0]);
        pairOfPair1.setSecond(numArr[0]);
        Pair<Pair<Integer,Integer>,Pair<Integer,Integer>> pairOfPair2 = new Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>();
        pairOfPair2.setFirst(numArr[2]);
        pairOfPair2.setSecond(numArr[3]);
        Pair<Pair<Integer,Integer>,Pair<Integer,Integer>> pairOfPair3 = new Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>();
        pairOfPair3.setFirst(numArr[8]);
        pairOfPair3.setSecond(numArr[6]);
        Pair<Pair<Integer,Integer>,Pair<Integer,Integer>> pairOfPair4 = new Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>();
        pairOfPair4.setFirst(numArr[9]);
        pairOfPair4.setSecond(numArr[7]);
        Pair<Pair<Integer,Integer>,Pair<Integer,Integer>> pairOfPair5 = new Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>();
        pairOfPair5.setFirst(numArr[11]);
        pairOfPair5.setSecond(numArr[10]);
          List<Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>> complexPairList = new ArrayList<Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>>();
          complexPairList.add(pairOfPair1);
          complexPairList.add(pairOfPair2);
          Collections.sort(complexPairList);
          System.out.println(complexPairList);
          System.out.println(complexPairList.contains(pairOfPair1));
          SortedMap<Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>,Integer> complexSMP = new TreeMap<Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>,Integer>();
          complexSMP.put(pairOfPair1, 0);
          complexSMP.put(pairOfPair2, 0);
          complexSMP.put(pairOfPair3, 0);
          complexSMP.put(pairOfPair4, 0);
          System.out.println(complexSMP.containsKey(pairOfPair1));
          Iterator<Pair<Pair<Integer,Integer>,Pair<Integer,Integer>>> iterCSMP = complexSMP.keySet().iterator();
          while(iterCSMP.hasNext()){
        	  System.out.println(iterCSMP.next());
          }
          
          Pair[] stringArray = {
                new Pair("John", "Lennon"),
                new Pair("Karl", "Marx"),
                new Pair("Groucho", "Marx"),
                new Pair("Oscar", "Grouch"),
                new Pair("Dingcheng", "Li"),
                new Pair("Qiongying", "Xiu"),
                new Pair("Hongfang", "Liu"),
                new Pair("Tao", "Tao"),
                new Pair("Liwei", "Wang"),
                new Pair("Xiaoyang", "Ruan")
            };
          
          for(int i=0;i<stringArray.length;i++){
        	linkMap.put(pairOfPair1, stringArray[1]);
        	linkMap.put(pairOfPair2, stringArray[2]);
        	linkMap.put(pairOfPair3, stringArray[3]);
        	linkMap.put(pairOfPair4, stringArray[4]);
          }
          System.out.println(linkMap.containsKey(pairOfPair1)+" "+linkMap.containsKey(pairOfPair5));
    }
}
