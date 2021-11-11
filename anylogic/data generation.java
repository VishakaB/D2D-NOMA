//System.out.println("4: ");

	{
		color=red;
		//physical layer 
		//Generation of Walsh codes
		boolean[][] walsh=hadamard(4); 
		boolean [] code1 =new boolean [4];
		boolean [] code2 =new boolean [4];
		boolean [] code3 =new boolean [4];
		
        if(thisid==110)
        	{
        		code1= walsh[0];//Taking 2nd row of walsh code for User1
        	}
        else if(thisid==210)
        	{
        		code2 = walsh[2]; //Taking 3rd row of walsh code for User2
        	}
        else
        	{
        		code3 = walsh[3]; //Taking 3rd row of walsh code for User2
        	}
           
        //data generation
        //int N = 10000;
        
        boolean [] userdata =  data_generator(N); //generate data sequence
        int [] muserdata = new int [N]; //initalize modulated data sequence
        
		//modulation
		for (int i=0; i<userdata.length; i++) 
			{
			  if (userdata[i]==true)
				  {
				  	muserdata[i] = 2*1 - 1;
				  }
			  else
				  {
				  	muserdata[i] = - 1;
				  }
			}
		
		//code and data multiplication 
		//data_user201*code2
		double sum = 0; 
        int [] cuserdata =  new int [N];   
    	for (int i = 0; i < 4; i++) 
    		{    if(code1[i]==true)
	    			{     		        	 	
		        	 	cuserdata[i] = muserdata[i] * 1;  
		        	 	//System.out.println("code1: "+cuserdata[i] );
		        	 }    
		         else
			         {
			         	cuserdata[i] = 0;
			         }
	    	}
	    	
		color = red;
		int msgid=(int)uniform(0,main.nbphones+1000);//unique msg id 
								
		//emergency call count
		if(nbrelayers==0 && !alreadyCalled.contains(id)&& main.emergcallids.get(String.valueOf(id)+";"+String.valueOf(msgid))==null&&String.valueOf(id)!=null)
			{
				main.emergcallids.put(String.valueOf(id)+";"+String.valueOf(msgid),String.valueOf(id)+" ts:"+time(SECOND));
			
				main.EmergCallStatusTime.put(String.valueOf(id),inCoverage); 
	    		main.EmergCalloriginatedTime.put(String.valueOf(id), String.valueOf(time(SECOND))); //latency	
	    		main.EmergCallLocx.put(String.valueOf(id),Location_phoneX); 
	    		main.EmergCallLocy.put(String.valueOf(id), Location_phoneY);
				
				if(main.emergcallids.get(String.valueOf(id)+";"+String.valueOf(msgid))==null)//avoiding any null element entering the emerg call ids array
		  			{
		  				main.emergcallids.remove(String.valueOf(id)+";"+String.valueOf(msgid));
		  			}
		  		else
		  			{   
		  				main.totalEmergCalls++;//only if the emerg call ids array is not null emergency call is counted
		  			}
			}
		if(main.emergcallids.get(String.valueOf(id)+";"+String.valueOf(msgid))!=null)//can include retransmissions
  			{				
				alreadyCalled.add(id);
			  	sendToAllConnected("emergency" + ";"+ String.valueOf(id)+";"+String.valueOf(msgid) +";"+ String.valueOf(rssiWatts_max)+";"+String.valueOf(id));//sending 
			  	y_st = "code";
			  	//System.out.println("5: ");
			  	for (int i = 0; i < muserdata.length; i++) 
		    		{    
		    			y_st += ";"+(muserdata[i]);
		    			
			    	}
			    System.out.println("transfer by txid: "+id);
			  	sendToAllConnected(y_st);
			}								
		main.energy_source_relay_main += transmitE;	//energy consumption
	}