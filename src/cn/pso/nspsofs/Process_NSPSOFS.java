package cn.pso.nspsofs;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.read.ReadFile;
import cn.read.Util;

public class Process_NSPSOFS {

    Particle_NSPSOFS[] swarm;  //粒子群
    double[] global_best;//全局最优解  
    int pcount;//粒子的数量  
    List<Particle_NSPSOFS> F1 = new ArrayList<Particle_NSPSOFS>();
    
    public static String name;
    
    /**
     * 排序
     * @param LeaderSet
     * @return
     */
    public List<Particle_NSPSOFS> sortSet(List<Particle_NSPSOFS> LeaderSet){
    	Particle_NSPSOFS p;
        for (int i = 0; i < LeaderSet.size(); i++){
            for (int j = i+1; j < LeaderSet.size(); j++){
            	if(LeaderSet.get(i).pbest_fitness[0] < LeaderSet.get(j).pbest_fitness[0]){
            		p = LeaderSet.get(i);
            		LeaderSet.set(i, LeaderSet.get(j));
            		LeaderSet.set(j, p);
            	}
            }
        }
        return LeaderSet;
    }
    
    /** 
     * 粒子群初始化 
     * @param n 粒子的数量 
     * @throws IOException 
     */  
    public void init(int n,String name) throws IOException {  
    	Process_NSPSOFS.name = name;
        pcount = n;
        swarm = new Particle_NSPSOFS[pcount];  //粒子群
        global_best = new double[2];
        
        //类的静态成员的初始化  
        Particle_NSPSOFS.c1 =1.49618;  
        Particle_NSPSOFS.c2 =1.49618;  
        Particle_NSPSOFS.w = 0.7298;  
        Particle_NSPSOFS.dims = ReadFile.getFeatureNum(Process_NSPSOFS.name);  
        
        
        for (int i = 0; i < pcount; ++i) { 
            swarm[i] = new Particle_NSPSOFS();  
            swarm[i].initial(ReadFile.getFeatureNum(Process_NSPSOFS.name));  
            swarm[i].evaluate(); 
        }
    }  
    
    
    /** 
     * 粒子群的运行 
     * @throws IOException 
     */  
    public void run(int runtimes,int times) throws IOException {
        int index;  
        int count = 1;
        SortBest_NSPSOFS sb = new SortBest_NSPSOFS();
        List<Particle_NSPSOFS> record;
        while (runtimes > 0) {  
             
        	List<Particle_NSPSOFS> nonDomS = new ArrayList<Particle_NSPSOFS>();//不受控粒子集合
        	
        	
            //求出nonDomS
        	 for (int i = 0; i < pcount;i++){
            	index = -1; 
            	 for (int j = 0; j < pcount; j++){
                	if(swarm[j].pbest_fitness[0] <= swarm[i].pbest_fitness[0]&&swarm[j].pbest_fitness[1] < swarm[i].pbest_fitness[1]){
                		index=1;
                	}else if(swarm[j].pbest_fitness[0] < swarm[i].pbest_fitness[0]&&swarm[j].pbest_fitness[1] <= swarm[i].pbest_fitness[1]){
                		index=1;
                	}
                }
            	
            	for(int k = 0;k<nonDomS.size();k++){
            		if(nonDomS.get(k).pbest_fitness[0] == swarm[i].pbest_fitness[0]&&nonDomS.get(k).pbest_fitness[1] == swarm[i].pbest_fitness[1]){
            			index=1;
            		}
            	}
            	
                if(index == -1){
                	nonDomS.add(swarm[i]);
                }
            }
            
            //将nonDomS中的粒子按照位置排序
            Particle_NSPSOFS p;
            nonDomS = sortSet(nonDomS);
            
            //求拥挤距离,按拥挤距离排序
            List<Particle_NSPSOFS> temp = nonDomS;
            double firstGbestD = 0;
            for (int i = 0; i < nonDomS.size()-1; ++i){
            	double di;
            	if(i==0){
            		di = Double.POSITIVE_INFINITY;//第一个和最后一个粒子拥挤距离为无穷大
            	}else{
            		di = (temp.get(i+1).pbest_fitness[0]-temp.get(i-1).pbest_fitness[0])/ReadFile.getFeatureNum(Process_NSPSOFS.name);
            		di+=temp.get(i+1).pbest_fitness[1]-temp.get(i-1).pbest_fitness[1];
            	}
            	
                for (int j = i+1; j < nonDomS.size(); ++j){
                	double dj;
                	if(j==nonDomS.size()-1){
                		dj = Double.POSITIVE_INFINITY;//第一个和最后一个粒子拥挤距离为无穷大
                	}else{
                		dj = (temp.get(j+1).pbest_fitness[0]-temp.get(j-1).pbest_fitness[0])/ReadFile.getFeatureNum(Process_NSPSOFS.name);
                		dj+=temp.get(j+1).pbest_fitness[1]-temp.get(j-1).pbest_fitness[1];
                	}
                	if(i == 1){
                		firstGbestD = di;
                	}
                	if(di > dj){
                		p = nonDomS.get(i);
                		nonDomS.set(i, nonDomS.get(j));
                		nonDomS.set(j, p);
	                	if(i == 1){
	                		firstGbestD = dj;
	                	}
                	}
                }
            }
            
            //将swarm中的粒子添加到union
            List<Particle_NSPSOFS> union = new ArrayList<Particle_NSPSOFS>();
            Particle_NSPSOFS[] kl = swarm.clone();
            for(int i=0;i<kl.length;i++){
            	union.add(kl[i]);
            }
            
            
            
            //求拥挤距离最小的粒子的集合
            List<Particle_NSPSOFS> gbestD = new ArrayList<Particle_NSPSOFS>();
            for (int i = 0; i < nonDomS.size(); i++){
            	double di;
            	if(i==0||i==nonDomS.size()-1){
            		di = Double.POSITIVE_INFINITY;//第一个和最后一个粒子拥挤距离为无穷大
            	}else{
            		di = (temp.get(i+1).pbest_fitness[0]-temp.get(i-1).pbest_fitness[0])/ReadFile.getFeatureNum(Process_NSPSOFS.name);
            		di+=temp.get(i+1).pbest_fitness[1]-temp.get(i-1).pbest_fitness[1];
            	}
            	if(firstGbestD == di){
            		gbestD.add(temp.get(i));
            	}
            }
            
            if(gbestD.size()==0){
            	gbestD.add(nonDomS.get(0));
            }
            
            index = -1; 
            //每个粒子更新位置和适应值
            for (int i = 0; i < swarm.length; ++i) {
            	if(gbestD.size() == 1){
            		swarm[i].gbest_fitness = gbestD.get(0).pbest_fitness;
            		swarm[i].gbest = gbestD.get(0).pbest;
            	}else{
            		int rand = Util.randomNum(0, gbestD.size(), 1)[0];
            		swarm[i].gbest_fitness = gbestD.get(rand).pbest_fitness;
            		swarm[i].gbest = gbestD.get(rand).pbest;
            	}
                swarm[i].updatev();  
                swarm[i].evaluate();
                union.add(swarm[i]);
            }
            
            //求F1
            F1.clear();
            for (int i = 0; i < union.size(); ++i){
            	index = -1;
                for (int j = 0; j < union.size(); ++j){
                    if (union.get(j).pbest_fitness[0] <= union.get(i).pbest_fitness[0]&&union.get(j).pbest_fitness[1] < union.get(i).pbest_fitness[1]) {
                    	index = 1;                   	
                    }else if(union.get(j).pbest_fitness[0] < union.get(i).pbest_fitness[0]&&union.get(j).pbest_fitness[1] <= union.get(i).pbest_fitness[1]){
                    	index = 1;
                    }
                }
                
            	for(int k = 0;k<F1.size();k++){
            		if(F1.get(k).pbest_fitness[0] == union.get(i).pbest_fitness[0]&&F1.get(k).pbest_fitness[1] == union.get(i).pbest_fitness[1]){
            			index=1;
            		}
            	}
                
                
                if(index == -1){
                	F1.add(union.get(i));
                }
            }
            /**
             *将Union中不受控制粒子加到F1集合中并把这些粒子从Union中除去，
             *再在新的Union中把不受控制粒子加到F2中并把这些粒子从Union中除去，
             *重复步骤得到F=(F1,F2,F3...)
             */
            swarm = new Particle_NSPSOFS[pcount];
            List<Particle_NSPSOFS> Fi = new ArrayList<Particle_NSPSOFS>();
            List<Particle_NSPSOFS> swarmTemp = new ArrayList<Particle_NSPSOFS>();
            INNER:while(true){
            	
            	 for (int i = 0; i < union.size(); ++i){
                	index = -1;
                    for (int j = 0; j < union.size(); ++j){
                        if (union.get(j).pbest_fitness[0] <= union.get(i).pbest_fitness[0]&&union.get(j).pbest_fitness[1] < union.get(i).pbest_fitness[1]) {
                        	index = 1;                  	
                        }else if(union.get(j).pbest_fitness[0] < union.get(i).pbest_fitness[0]&&union.get(j).pbest_fitness[1] <= union.get(i).pbest_fitness[1]){
                        	index = 1;
                        }
                    }
                    
                	for(int k = 0;k<Fi.size();k++){
                		if(Fi.get(k).pbest_fitness[0] == union.get(i).pbest_fitness[0]&&Fi.get(k).pbest_fitness[1] == union.get(i).pbest_fitness[1]){
                			index=1;
                		}
                	}
                    
                    if(index == -1){
                    	Fi.add(union.get(i));
                    	union.remove(i);
                    }
                }
                
            	 Fi = sortSet(Fi);
            	 temp = Fi;
                
            	 for (int i = 0; i < Fi.size(); ++i){
		        	double di;
		        	if(i==0||i==Fi.size()-1){
		        		di = Double.POSITIVE_INFINITY;//第一个和最后一个粒子拥挤距离为无穷大
		        	}else{
		        		di = (temp.get(i+1).pbest_fitness[0]-temp.get(i-1).pbest_fitness[0])/ReadFile.getFeatureNum(Process_NSPSOFS.name);
		        		di+=temp.get(i+1).pbest_fitness[1]-temp.get(i-1).pbest_fitness[1];
		        	}
		            for (int j = i+1; j < Fi.size(); ++j){
		            	double dj;
		            	if(j==0||j==Fi.size()-1){
		            		dj = Double.POSITIVE_INFINITY;//第一个和最后一个粒子拥挤距离为无穷大
		            	}else{
		            		dj = (temp.get(j+1).pbest_fitness[0]-temp.get(j-1).pbest_fitness[0])/ReadFile.getFeatureNum(Process_NSPSOFS.name);
		            		dj+=temp.get(j+1).pbest_fitness[1]-temp.get(j-1).pbest_fitness[1];
		            	}
		            	if(di > dj){
		            		p = Fi.get(i);
		            		Fi.set(i, Fi.get(j));
		            		Fi.set(j, p);
		            	}
		            }
		        }

	            for(Particle_NSPSOFS i:Fi){
	            	swarmTemp.add(i);
	                if(swarmTemp.size() == swarm.length){
	                    for(int j = 0;j<swarmTemp.size();j++){
	                    	swarm[j] = swarmTemp.get(j);
	                    }
	                	break INNER;
	                }
	            }
            }
	        record = F1;
	        if(count%10==0 || count==1){
	        	sb.getNSResult(Process_NSPSOFS.name, record, count+"-"+times);
	        }
	        count++;
	        runtimes--;  
	    }
	}
    /** 
     * 显示程序求解结果 
     */  
    public void showresult(List<Particle_NSPSOFS> list) {
    	INNER:for(int i = 0 ; i<F1.size() ; i++){
    		int k=0;
    		for(Particle_NSPSOFS p:list){
    			if(p.pbest_fitness[0]==F1.get(i).pbest_fitness[0]){
    				if(p.pbest_fitness[1]<=F1.get(i).pbest_fitness[1]){
    					continue INNER;
    				}else{
    					k=list.indexOf(p);
    				}
    			}
    		}
    		if(k>0){
    			list.remove(k);
    		}
    		list.add(F1.get(i));
    	}
    }  
}
