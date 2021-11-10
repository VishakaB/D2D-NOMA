clear all;
users=2;            % Number of Users
%------------------Generation of Walsh code--------------------------------
n =4;                               %Number of  Data Sub-Carriers
walsh=hadamard(n);              
code1=walsh(2,:);                   %Taking 2nd row of walsh code for User1
code2=walsh(4,:);                   %Taking 3rd row of walsh code for User2
%------------------Generating data for User1-------------------------------
N=10^4;                             % Number of Bits for  data_user1
data_user1= rand(1,N)>0.5;          % Generation of data for user1
data_user1bpsk = 2*data_user1-1;    % BPSK modulation 0 -> -1; 1 -> 0 
%------------------Spreading & IFFT for User1------------------------------
data_user11=data_user1bpsk';
spdata1_user1=data_user11*code1;    % Spreading 
spdata12=(spdata1_user1)';
ifftdata_user1=ifft(spdata12);      % Taking the IFFT
ifftdata12=ifftdata_user1';

%------------------Append Cyclic Prefix1 for User1-------------------------
y1=[ifftdata12(:,[(n-2):n]) ifftdata12];
transdata1=y1';
tx_user1=transdata1;                % Transmitting data for user1
%------------------Generating data for User2-------------------------------
M=10^4;                             % Number of Bits for  data_user2
data_user2= rand(1,M)>0.5;          % Generation of data for user2
data_user2bpsk = 2*data_user2-1;    % BPSK modulation 0 -> -1; 1 -> 0 
%-----------------Spreading & IFFT for User2-------------------------------
data_user21=data_user2bpsk';
spdata2=data_user21*code2;          % Spreading 
spdata22=(spdata2)';
ifftdata_user2=ifft(spdata22);      % Taking the IFFT
ifftdata22=ifftdata_user2';
%-----------------Append Cyclic Prefix1 for User2--------------------------
y2=[ifftdata22(:,[(n-2):n]) ifftdata22];
transdata2=y2';
tx_user2=transdata2;                % Transmitting data for user2
%----------------------Adding data for Transmission of All User------------
x=tx_user1+tx_user2;
%----------------------Creating Rayleigh Channel---------------------------
Taps=4;                                        % Number of Taps
p1=0.5/2.3;                                     % Power of Tap1
p2=0.9/2.3;                                     % Power of Tap2
p3=0.7/2.3;                                     % Power of Tap3
p4=0.2/2.3;
gain1=sqrt(p1/2)*[randn(1,N) + j*randn(1,N)];   % Gain for Tap1
gain2=sqrt(p2/2)*[randn(1,N) + j*randn(1,N)];   % Gain for Tap2
gain3=sqrt(p3/2)*[randn(1,N) + j*randn(1,N)];   % Gain for Tap3
gain4=sqrt(p4/2)*[randn(1,N) + j*randn(1,N)];   % Gain for Tap4
x11=x(:);
x12=reshape(x11,1,length(x11));
i=1:length(x12);        
delay1=1; 
for i=delay1+1:length(x12) % Producing one sample delay in Tap2 w.r.t. Tap1
   x13(i)=x(i-delay1);
end
delay2=2;
for i=delay2+1:length(x12) % Producing two sample delay in Tap2 w.r.t. Tap1
   x14(i)=x(i-delay2);
end
delay3=3;
for i=delay3+1:length(x12) % Producing three sample delay in Tap2 w.r.t. Tap1
   x15(i)=x(i-delay3);
end
x1=reshape(x13,(n+3),length(x13)/(n+3));
x2=reshape(x14,(n+3),length(x14)/(n+3));
x3=reshape(x15,(n+3),length(x15)/(n+3));
ch1=repmat(gain1,(n+3),1);     
ch2=repmat(gain2,(n+3),1);
ch3=repmat(gain3,(n+3),1);
ch4=repmat(gain4,(n+3),1);
data_channel=x.*ch1+x1.*ch2+x2.*ch3+x3.*ch4;  % Passing data through channel 
%------------------------Addition of AWGN noise ---------------------------
data_noise1=data_channel(:);
data_noise2=reshape(data_noise1,1,length(data_noise1));
noise = 1/sqrt(2)*[randn(1,length(data_noise2)) + j*randn(1,length(data_noise2))]; 
snr = [0:20];                 % multiple Eb/N0 values
for i = 1:length(snr)
 y = data_noise2 + (sqrt(1)*10^(-snr(i)/20))*noise; %Addition of Noise
 
 
%--------------------------Receiver ---------------------------------------
data_received =y;           %fadded data received with awgn noise
%---------------------Removing Cyclic Prefix-------------------------------
rx1=reshape(data_received,(n+3),length(data_received)/(n+3));
rx12=rx1';
rx13 = rx12(:,[(4:(n+3))]); 
rx14=rx13';
%-----------------Taking FFT ----------------------------------------------
fft_data_received =fft(rx14);
%----------------equilization of the channel-------------------------------
channel_response=fft([gain1;gain2;gain3;gain4],n);
data_equilized=fft_data_received.*conj(channel_response);
%----------------BER of Data User1-----------------------------------------
recdata11=(data_equilized'*code1')';
recdata12=real(recdata11)>0;
errors_user1(i) = size(find([data_user1- recdata12]),2); %Errors for User1
SBer1 = errors_user1/N;                              % simulated ber user1
%----------------BER of Data User2-----------------------------------------
recdata21=(data_equilized'*code2')';
recdata22=real(recdata21)>0;
errors_user2(i) = size(find([data_user2- recdata22]),2); %Errors for User1
SBer2 = errors_user2/M;                               % simulated ber user2
end
% ------------------------Theoretical Result-------------------------------
snrlnr=10.^(snr/10);
TBer = 0.5*erfc(sqrt(snrlnr)); % Theoretical BER for AWGN
TBerf = 0.5.*(1-sqrt(snrlnr./(snrlnr+1)));% theoretical BER fro Flat fadding
%-------------------Displaying Result--------------------------------------       
figure
semilogy(snr,TBer,'c*-','LineWidth',2);
hold on;
semilogy(snr,TBerf,'r-','LineWidth',3);
hold on;
semilogy(snr,SBer1,'bd','LineWidth',4);
hold on;
semilogy(snr,SBer2,'go-','LineWidth',1);
axis([0 20 10^-5 0.5]);
grid on
legend('Theoratical BER for BPSK on AWGN ','Theoratical BER for BPSK on Rayleigh Channel ' ,'Simulated BER for User13','Simulated BER for User24');
xlabel('Eb/No, dB');
ylabel('Bit Error Rate');
title('BER Vs Eb/No on Rayleigh Channel')
