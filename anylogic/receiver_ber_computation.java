//physical layer 
//noise addition 
//code multiply with data 
color = green;

//if (!buffer_tr.isEmpty()){
receivedmsg=msg;
nbrelayers=0;
String[] message = receivedmsg.split(";");
lastsender=message[1];

//int N = 10000;
int [] rx = new int [N]; 
if(message[0].equals("code"))
	{System.out.println("reception @ rx: "+id+";"+time(SECOND));
	 for (int k = 1; k < message.length-1; k++) //ignore the first msg[0] code
		{    
			rx[k] = Integer.valueOf(message[k]);
			//System.out.println("rx: "+rx[k]);
    	}
	
//add channel effect and noise

double [] channel_real = new double [N];

double [] channel_imag = new double [N];

for (int k = 0; k <message.length-1; k++)
	{
	    channel_real[k]= (Math.random() * 5);;   // the real part
		
		channel_imag[k] = (Math.random() * 5);;   // the imaginary part 	    
	}   

double [] rxf_real = new double [N];
double [] rxf_imag = new double [N];

//adding channel fading effect on the transmitted signal	
for (int k = 0; k <rx.length-1; k++)
	{
		rxf_real[k] = channel_real[k]*rx[k];
			
		rxf_imag[k] = channel_imag[k]*rx[k];
	}
	
// noise  
double [] noise_real = new double [N];
double [] noise_imag = new double [N];

for (int k = 0; k <message.length-1; k++)
	{
	    noise_real[k]= (Math.random() * 5);;   // the real part
		noise_imag[k] = (Math.random() * 5);;   // the imaginary part 	    
	}   
	
double [] y_real = new double [N];
double [] y_imag = new double [N];
for (int k = 0; k <message.length-1; k++)
	{
		y_real[k] = rxf_real[k]+noise_real[k];
		y_imag[k] = rxf_imag[k]+noise_imag[k];
	}
//decoding phase
//equalization 
double [] eq_real = new double [N];
double [] eq_imag = new double [N];
double [] eq_abs  = new double [N];

for (int j = 0; j <message.length-1; j++)
	{
		eq_real[j] = y_real[j]/channel_real[j];
		eq_imag[j] = y_imag[j]/channel_imag[j];
		double term1=Math.pow(eq_real[j] ,2);
		double term2=Math.pow(eq_imag[j] ,2);
		eq_abs[j]  = Math.pow(term1+term2,0.5);
	}	
//checking and estimating the received value as bit 0 or bit 1
int [] est_bit  = new int [N];
for (int h = 0; h <message.length-1; h++)
	{
		if (eq_abs[h]>0)
			{
				est_bit[h] = 1;
		    }
		else
			{
				est_bit[h] = 0;
			}
	}

//measure total bit error
int diff =0;
for (int h = 0; h <message.length-1; h++)
	{
		//if(est_bit[h]!=Double.valueOf(message[h+1]))
		//System.out.println("est rx: "+abs(est_bit[h]-rx[h]));
		if(abs(est_bit[h]-rx[h])==0.0)
			{
				
				diff = 1+diff;
				//System.out.println("diff: "+diff);
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