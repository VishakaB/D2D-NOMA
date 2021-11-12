//System.out.println("4: ");

	{
		color=red;
		//physical layer 
		//Generation of Walsh codes
		boolean[][] walsh=hadamard(nbcodes); 
		boolean [] code = new boolean [nbcodes];

        code = walsh[id_walshindex.get(id)];  
        //System.out.println("walsh: "+walsh[id_walshindex.get(id)]);
        //data generation
        //int N = 10000;
        
        int [] userdata =  data_generator(N,id); //generate data sequence
        int [] muserdata = new int [N]; //initalize modulated data sequence
        
		//modulation
		for (int i=0; i<userdata.length; i++) 
			{
			 	muserdata[i] = 2*userdata[i] - 1;
			}
		
		//code and data multiplication 
		//data_user201*code2
		double sum = 0; 
		int row = N;
		int column= nbcodes;
        int [][] cuserdata =  new int [row][column];  
        
        for (int r = 0; r < row; r++) 
        {
			for (int c = 0; c < column; c++) 
			{
				//System.out.println(String.format("code[c]", code1[c]));
				if(code[c]==true)
	    			{     		        	
						cuserdata[r][c] = muserdata[r]*1;
						//System.out.println("txc: "+cuserdata[r][c]);
					}
				else 
					{
						cuserdata[r][c] = muserdata[r]*0;
					}
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
			  	y_c = "code"+";"+id;
			  	//System.out.println("5: ");
			  	for (int i = 0; i < muserdata.length; i++) 
		    		{    
		    			y_st += ";"+String.valueOf(muserdata[i]);		    			
			    	}
			     for (int r = 0; r < row; r++) 
			        {
						for (int c = 0; c < column; c++) 
							{    		        	
								y_c += ";"+cuserdata[r][c];
							}
					} 
			    System.out.println("transfer by txid: "+id);
			    sendToAllConnected(y_c);
			  	//sendToAllConnected(y_st);
			}								
		main.energy_source_relay_main += transmitE;	//energy consumption
	}