//physical layer 
//noise addition 
//code multiply with data 
color = green;

//if (!buffer_tr.isEmpty()){
receivedmsg = msg;
nbrelayers = 0;
String[] message = receivedmsg.split(";");
lastsender=message[1];

boolean[][] walsh=hadamard(nbcodes); 
boolean [] code =new boolean [nbcodes];

double [] rx = new double [40000];
int row = N;
int column= nbcodes;
double [][] rxm = new double [row][column]; 
if(message[0].equals("code"))
{	
	int txid = Integer.valueOf(message[1]);
	System.out.println("reception @ rx: "+id+";"+txid+";"+time(SECOND));
	code = walsh[id_walshindex.get(txid)];
		
	for (int k = 0; k < message.length-2; k++) //ignore the first msg[0] code
		{    
			rx[k] = Double.valueOf(message[k+2]);
			//System.out.println("rx: "+rx[k]);			
    	}
	int p = 0;
    for (int r = 0; r < N; r++) 
	    {
			for (int c = 0; c < nbcodes; c++) 
				{    		        	
					rxm[r][c] = rx[p];
					p+=1;
					//System.out.println(" rxm: "+id+";"+rxm[r][c]);
				}			
		}
		
//add channel effect and noise
double [][] channel_real = new double [row][column];

double [][] channel_imag = new double [row][column];

for (int r = 0; r < N; r++) 
	{
			for (int c = 0; c < nbcodes; c++) 
				{    
				    channel_real[r][c]= (Math.random());   // the real part
					
					channel_imag[r][c] = (Math.random());   // the imaginary part 	   
				} 
	}   
double [][] rxf_real = new double [row][column];
double [][] rxf_imag = new double [row][column];

//adding channel fading effect on the transmitted signal	
for (int r = 0; r < N; r++) 
	{       //int cc=0;
			//System.out.println(" rxm: "+rxm[r][cc]+ channel_real[r][cc]);	
			for (int c = 0; c < nbcodes; c++) 
				{  
					rxf_real[r][c] = channel_real[r][c]*rxm[r][c];
					
					rxf_imag[r][c] = channel_imag[r][c]+rxm[r][c];
				}
	}
	
// noise  
double [][] noise_real = new double [row][column];
double [][] noise_imag = new double [row][column];

for (int r = 0; r < N; r++) 
	{       //int cc=0;
			//System.out.println(" rxm: "+rxm[r][cc]+ channel_real[r][cc]);	
			for (int c = 0; c < nbcodes; c++) 
				{  
			    noise_real[r][c]= (Math.random());   // the real part
				noise_imag[r][c] = (Math.random());   // the imaginary part 	 
				}   
	}  

double [][] y_real = new double [row][column];
double [][] y_imag = new double [row][column];
for (int r = 0; r < N; r++) 
	{       //int cc=0;
			//System.out.println(" rxm: "+rxm[r][cc]+ channel_real[r][cc]);	
			for (int c = 0; c < nbcodes; c++) 
				{  
					y_real[r][c]= rxf_real[r][c]+noise_real[r][c];
					y_imag[r][c]= rxf_imag[r][c]+noise_imag[r][c];
				}
	}
//decoding phase
//multiplication by code
//recdata11=(data_equilized'*code1')'
double [] yuserdata_real = new double [row];
double [] yuserdata_imag = new double [row];
double [] rxchannel_real = new double [row];
double [] rxchannel_imag = new double [row];
for (int r = 0; r < row; r++) 
    {
		for (int c = 0; c < column; c++) 
			{
				//System.out.println(String.format("code[c]", code1[c]));
				if(code[c]==true)
	    			{     		        	
						yuserdata_real[r] = yuserdata_real[r] + y_real[r][c]*1;
						yuserdata_imag[r] = yuserdata_imag[r] + y_imag[r][c]*1;
						rxchannel_real[r]= rxchannel_real[r]+channel_real[r][c]*1;
						rxchannel_imag[r]= rxchannel_imag[r]+channel_imag[r][c]*1;
						//System.out.println("txc: "+cuserdata[r][c]);
					}
				else 
					{
						yuserdata_real[r] = yuserdata_real[r] + y_real[r][c]*0;
						yuserdata_imag[r] = yuserdata_imag[r] + y_imag[r][c]*0;
						rxchannel_real[r] = rxchannel_real[r]+channel_real[r][c]*0;
						rxchannel_imag[r] = rxchannel_imag[r]+channel_imag[r][c]*0;
					}
			}
	} 
	
//equalization 	 
double [] eq_real = new double [row];
double [] eq_imag = new double [row];
double [] eq_abs  = new double [row];

for (int r = 0; r < row; r++) 
    {
		eq_real[r] = yuserdata_real[r]/rxchannel_real[r];
		eq_imag[r] = yuserdata_imag[r]/rxchannel_imag[r];
		double term1=Math.pow(eq_real[r] ,2);
		double term2=Math.pow(eq_imag[r] ,2);
		eq_abs[r]  = Math.pow(term1+term2,0.5);
	}	
	
	//checking and estimating the received value as bit 0 or bit 1
int [] est_bit  = new int [N];

for (int r = 0; r < row; r++) 
    {
		if (eq_abs[r]>0)
			{
				est_bit[r] = 1;
		    }
		else
			{
				est_bit[r] = 0;
			}
	}
	
//measure total bit error
int diff =0;
for (int r = 0; r < row; r++) 
	{
		//if(est_bit[h]!=Double.valueOf(message[h+1]))
		for (int c = 0; c < column; c++) 
			{//System.out.println("est rx: "+abs(est_bit[h]-rx[h]));
				if(abs(est_bit[r]-rxm[r][c])==0.0)
					{
						
						diff = 1+diff;
						//System.out.println("diff: "+diff);
					}
			}
	}
//System.out.println("diff: "+diff+";"+(message.length-1));	
//ber calculation

float ber = (float) diff/(message.length-1); 	
String strDouble = String.format("%.4f", ber );
System.out.println("ber: "+strDouble);
Done = true;	
}
//if an emergency call is received : energy counter
if(message[0].equals("emergency")) 	
	{    
		congestion_min++;
		if(!alreadyrelayed.contains("emergency"+";"+ message[1]+";"+message[2]+";"+message[3]))
			{
				nbcalls++;
			}	
		main.congestion_sum = 1+ main.congestion_sum;
		if(congestion_min==0)congestion_min = 1;
		if(nbcalls==0)nbcalls = 1;
		if(congestion_min>=200)
		    {
		    	congestion_high = true;
		    	n0_th = main.n0_th;
		    }
		else if(congestion_min>=10 && congestion_min <200)
			{
				congestion_medium = true;
				n0_th = main.n0_th +1;
			}
		else
			{
				//congestion_less = true;
				n0_th = main.n0_th+1;
			}
		
		if(main.rosenbrockTmax==true)rosenbrock();
		if(Tmax<0)Tmax = 0;
		else if(Tmax>=120)Tmax = 120;
		waitingTime=min(Tmax,1*(1-ic)*(1/(locAccuracy+0.1))*(Double.valueOf(message[3])/rssiWatts_max)*(100/min(energyLevel,100)));
		
		main.energy_source_relay_main += receiveE;	
		buffer_tr.put(message[1],time(SECOND)+waitingTime);	
		buffer_rssi.put(message[1],message[3]);
		buffer_msgid.put(message[1],message[2]);
		buffer_relayid.put(message[1],message[4]);
		
		//System.out.println("rssi ratio:" +(Double.valueOf(message[3])/rssiWatts_max));
		main.rssi_ratio.add(Double.valueOf(message[3])/rssiWatts_max);	
	}
	
if (main.EmergCallReachedgNBTime.containsKey(message[1]) && inCoverage == true)
	{
		buffer_tr.remove(message[1]);
		alreadyrelayed.add("emergency"+";"+ message[1]+";"+message[2] +";"+message[3]);		
	}	
	
String[] arrayKeys = buffer_nbrelayers.keySet().toArray( new String[buffer_nbrelayers.size()]);		
for(int k=0;k<buffer_nbrelayers.size();k++)
	{	
		if(buffer_nbrelayers.get(arrayKeys[k])>=(main.max_rs_th-buffer_tr.size()))
			{
				buffer_tr.remove(arrayKeys[k]);
				buffer_nbrelayers.remove(arrayKeys[k]);	
				alreadyrelayed.add("emergency"+";"+ arrayKeys[k]+";"+buffer_msgid.get(arrayKeys[k]) +";"+buffer_rssi.get(arrayKeys[k]));	
			}
	}
	//}