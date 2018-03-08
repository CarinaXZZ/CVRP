
import java.io.IOException;  
import java.util.Random;  
  
public class cvrp {  
  
    private int MAX_GEN;// number of times of iteration 
    private int N;// neighbourhood
    private int l;// tabu length
    private int shopNum; // number of shop 
  
    private double[][] distance; // the distance between 2 shops   
  
    private int[] candidate;// initial path
    private int[] bestCandidate;// best path  
    private double bestCost;//best cost
    private int[] LocalCandidate;// current best path  
    private double localCost;// cost of current path
    private int[] tempCandidate; 
    private double tempCost;   
    private int[][] tabu;// tabu list  
  
    private int t;// current iteration times
  
    private Random random;  
    
    public cvrp(){
    	
    }
  
    public cvrp(int a, int b, int c, int d) {  
        shopNum = a; //number of shops
        MAX_GEN = b; // iteration times
        N = c; // neighbourhood 
        l = d; // tabu length
    }  
    
    @SuppressWarnings("resource")  
    // initial tabu searching
    // read the data from fruitybun250.vrp where store the location and demand of the shops
    private void init(String filename) throws IOException {  
        // read the data
    	CVRPData.readFile(filename);
        distance = new double[shopNum][shopNum];  
        for(int i = 0; i < shopNum - 1; i++){
        	distance[i][i] = 0;
        	for(int j = i + 1; j < shopNum; j++){
        		// getDistance is use to get the distance between 2 shops
        		// start from node 2
        		distance[i][j] = CVRPData.getDistance(i + 2,j + 2);
        	}
        }
        distance[shopNum - 1][shopNum - 1] = 0;

  
        candidate = new int[shopNum];  
        bestCandidate = new int[shopNum];  
        bestCost = Integer.MAX_VALUE;  
        LocalCandidate = new int[shopNum];  
        localCost = Integer.MAX_VALUE;  
        tempCandidate = new int[shopNum];  
        tempCost = Integer.MAX_VALUE;  
  
        tabu = new int[l][shopNum];  
        t = 0;  
  
        random = new Random(System.currentTimeMillis());  
  
    }  
  
    // initialise the candidate  
    void initGroup() {  
        int i, j;  
        // randomly set candidate between 0-249
        candidate[0] = random.nextInt(65535) % shopNum;  
        for (i = 1; i < shopNum;) 
        {  
            candidate[i] = random.nextInt(65535) % shopNum;  
            // make sure every candidate is different
            for (j = 0; j < i; j++) {  
                if (candidate[i] == candidate[j]) {  
                    break;  
                }  
            }  
            if (j == i) {  
                i++;  
            }  
        }  
    }  
  
    // copy candidate1 to candidate2  
    public void copyGh(int[] candidate1, int[] candidate2) {  
        for (int i = 0; i < shopNum; i++) {  
        	candidate2[i] = candidate1[i];  
        }  
    }  
    // calculate the costs
    public double evaluate(int[] chr) {  
    	int capacity = 500; //capacity of the truck
        double cost = 0;  
        for(int i = 0; i < shopNum; i++){
           //read the demand of the node from the data file
      	   int demand = CVRPData.getDemand(chr[i]+2);
           //start of one truck route
      	   if(capacity == 500){
      		   cost = cost + CVRPData.getDistance(1, chr[i]+2);
      		   capacity = capacity -demand;
      	   }
      	   else if (capacity - demand >= 0) { 
      		   cost = cost + CVRPData.getDistance(chr[i]+2, chr[i-1]+2);
      		   capacity = capacity - demand;
      	   }
      	   //end of one truck route
      	   else {
      		   cost = cost + CVRPData.getDistance(chr[i-1]+2, 1);
      		   capacity = 500;
      		   i--;
      	   }
         }
         cost = cost + CVRPData.getDistance(chr[248]+2, 1);
        return cost;  
    }  
  
    // exchange the neighbour  
    public void neighbour(int[] can, int[] tempCan) {  
        int i, temp;  
        int ran1, ran2;  
  
        for (i = 0; i < shopNum; i++) {  
            tempCan[i] = can[i];  
        }  
        ran1 = random.nextInt(65535) % shopNum;  
        ran2 = random.nextInt(65535) % shopNum;  
        while (ran1 == ran2) {  
            ran2 = random.nextInt(65535) % shopNum;  
        }  
        temp = tempCan[ran1];  
        tempCan[ran1] = tempCan[ran2];  
        tempCan[ran2] = temp;  
    }  
  
    // find if the candidate in the tabu list  
    public int judge(int[] tempCan) {  
        int i, j;  
        int flag = 0;  
        for (i = 0; i < l; i++) {  
            flag = 0;  
            for (j = 0; j < shopNum; j++) {  
                if (tempCan[j] != tabu[i][j]) {  
                    flag = 1;// not the same 
                    break;  
                }  
            }  
            if (flag == 0)// same  
            {  
                // return 1;  
                break;  
            }  
        }  
        if (i == l)
        {  
            return 0;  
        } else {  
            return 1;
        }  
    }  
   
    // release tabu and add new candidate
    public void jiejinji(int[] tempCan) {  
        int i, j, k;  
        // delete first candidate in the tabu list
        // move forward the follow candidates
        for (i = 0; i < l - 1; i++) {  
            for (j = 0; j < shopNum; j++) {  
                tabu[i][j] = tabu[i + 1][j];  
            }  
        }  
  
        // add new candidate into tabu list  
        for (k = 0; k < shopNum; k++) {  
            tabu[l - 1][k] = tempCan[k];  
        }  
  
    }  
  
    public void solve() {  
        int nn;  
        // initial Candidate
        initGroup();  
        copyGh(candidate, bestCandidate);// copy current candidate to the best candidate 
        bestCost = evaluate(candidate);  
        //iteration
        while (t < MAX_GEN) {  
            nn = 0;  
            localCost = Integer.MAX_VALUE;  
            while (nn < N) {  
                neighbour(candidate, tempCandidate);// find current candidate's neighbour  
                // judge if the candidate is inside the tabu list
                if (judge(tempCandidate) == 0)// if not
                {  
                    tempCost = evaluate(tempCandidate);  
                    if (tempCost < localCost) {  
                        copyGh(tempCandidate, LocalCandidate);  
                        localCost = tempCost;  
                    }  
                    nn++;  
                }  
            } 
            // judge the cost of current candidate better than the best one or not
            if (localCost < bestCost) {  
            	// if yes
            	// copy current candidate to the best one
                copyGh(LocalCandidate, bestCandidate);  
                bestCost = localCost;
                //System.out.println(bestCost);
                
            }
            
            copyGh(LocalCandidate, candidate);  
            
            jiejinji(LocalCandidate);  
            t++;  
            
        }  
        //System.out.println("best:"+bestCost);
         
    } 
    //print the route of the trucks and the cost
    public void truck(){ 
	   System.out.println("login zx16649 32567");
	   System.out.println("name Carina Xu");
	   System.out.println("algorithm tuba search is used");
	   System.out.print("costs ");  
       System.out.println(bestCost);  
       //System.out.println("best path：");  
//       for (int i = 0; i < shopNum; i++) { 
//           System.out.print(bestGh[i]+ 2 + ",");  
//       } 
//       System.out.println();
       // print the route for each trucks
       int capacity = 500;
       for(int i = 0; i < shopNum; i++){
    	   int demand = CVRPData.getDemand(bestCandidate[i]+2);
    	   if(capacity == 500){
    		   capacity = capacity -demand;
    		   int node = bestCandidate[i] + 2;
    		   System.out.print("1->"+ node +"->");
    	   }
    	   else if (capacity - demand >= 0) { 
    		   capacity = capacity - demand;
    		   System.out.print(bestCandidate[i]+2 +"->");
    	   }
    	   else {
    		   capacity = 500;
    		   i--;
    		   System.out.println("1");
    	   }
       }
       System.out.println("1");
       
   }

   
    public static void main(String[] args) throws IOException {  
       //System.out.println("Start....");  
       cvrp tabu = new cvrp(249, 1500000, 300, 50);  
       tabu.init("./fruitybun250.vrp");
       tabu.solve();
       tabu.truck();
    }  
}  

