
clc;
clear all;
close all;
rng(0);
users=2;            % Number of Users
%% Generation of Walsh code
n =4;                               %Number of  Data Sub-Carriers
walsh=hadamard(n);              
code1=walsh(2,:);                   %Taking 2nd row of walsh code for User1
code2=walsh(4,:);                   %Taking 3rd row of walsh code for User2
d1 = 1000; d2 = 500;    %Distances of users from base station (BS)
a1 = 0.75; a2 = 0.25;   %Power allocation factors
b1 = 0.6; b2 = 0.4;
eta = 4;                %Path loss exponent
%% ------------------Generating data for User1-------------------------------
N=10^6;                             % Number of Bits for  data_user1
data_user1= rand(1,N)>0.5;          % Generation of data for user1
data_user1bpsk = 2*data_user1-1;    % BPSK modulation 0 -> -1; 1 -> 0 
data_user11= rand(1,N)>0.5;  
data_user11bpsk = 2*data_user11-1;    % BPSK modulation 0 -> -1; 1 -> 0 
data_user1bpsk = sqrt(a1)*data_user1bpsk+sqrt(a2)*data_user11bpsk; %noma power coefficients addition
%% ------------------Spreading & IFFT for User1------------------------------
data_user101=data_user1bpsk';
spdata1_user1=data_user101*code1;    % Spreading 
spdata12=(spdata1_user1)';
ifftdata_user1=ifft(spdata12);      % Taking the IFFT
ifftdata12=ifftdata_user1';

%% ------------------Append Cyclic Prefix1 for User1-------------------------
y1=[ifftdata12(:,[(n-2):n]) ifftdata12];
transdata1=y1';
tx_user1=transdata1;  % Transmitting data for user1

%% ------------------Generating data for User2-------------------------------
M=10^6;                             % Number of Bits for  data_user2
data_user2= rand(1,M)>0.5;          % Generation of data for user2
data_user2bpsk = 2*data_user2-1;    % BPSK modulation 0 -> -1; 1 -> 0 
data_user22= rand(1,M)>0.5;          % Generation of data for user2
data_user22bpsk = 2*data_user22-1;    % BPSK modulation 0 -> -1; 1 -> 0 
data_user2bpsk = sqrt(b1)*data_user2bpsk+sqrt(b2)*data_user22bpsk; %noma power coefficients addition
%% -----------------Spreading & IFFT for User2-------------------------------
data_user201=data_user2bpsk';
spdata2=data_user201*code2;          % Spreading 
spdata22=(spdata2)';
ifftdata_user2=ifft(spdata22);      % Taking the IFFT
ifftdata22=ifftdata_user2';
%% -----------------Append Cyclic Prefix1 for User2--------------------------
y2=[ifftdata22(:,[(n-2):n]) ifftdata22];
transdata2=y2';
tx_user2=transdata2;                % Transmitting data for user2

%% ----------------------Adding data for Transmission of All User------------

x=tx_user1+tx_user2;
%Generate rayleigh fading coefficient for both users
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

%% ------------------------Addition of AWGN noise ---------------------------
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
    recdata12=real(recdata11./(sqrt(a1)))>0;
    errors_user1(i) = size(find([data_user1- recdata12]),2); %Errors for User1
    SBer1 = errors_user1/N;                              % simulated ber user1
    
    %ber of the second message of user 1
    %correlation coefficient 2nd iteration 2nd symbol x2
    resi = (recdata11 - recdata12.*(code1*channel_response)*sqrt(a1))./((code1*channel_response)*sqrt(a2));
    x2_hat = real(resi)>0;
    errors_user11(i) = size(find([data_user11- x2_hat]),2); %Errors for User1
    SBer11 = errors_user11/N;    % simulated ber user1
    %theoretical ber 
    g1 = (norm(channel_response)).^2;
    g2 = (norm(channel_response)).^2;
    gam_a = 2*((sqrt(a1)-sqrt(a2))^2)*mean(g1)*snr(i);
    gam_b = 2*((sqrt(a1)+sqrt(a2))^2)*mean(g1)*snr(i);
    
    omega_p = a1./2*norm(channel_response);
    omega_mu = a2*norm(channel_response);
    omega_n = 2*a1*norm(channel_response);
    gammaH = a1./noise*norm(channel_response);
    gammaj = 2*a2./noise*norm(channel_response);
    term1 = (1-sqrt(gammaj/(gammaj+2)));
    term2 = omega_mu/(omega_mu+omega_p);
    term3 = -omega_mu/(omega_mu+omega_n);
    berul_1(i) = 0.5 * (term1+term2+term3); 
    %ber_th1(i) = 0.25*(2 - sqrt(gam_a/(2+gam_a)) - sqrt(gam_b/(2+gam_b)));
    
    gam_c = 2*a2*mean(g2)*snr(i);
    gam_d = 2*((sqrt(a2) + sqrt(a1))^2)*mean(g2)*snr(i);
    gam_e = 2*((sqrt(a2) + 2*sqrt(a1))^2)*mean(g2)*snr(i);
    gam_f = 2*((-sqrt(a2) + sqrt(a1))^2)*mean(g2)*snr(i);
    gam_g = 2*((-sqrt(a2) + 2*sqrt(a1))^2)*mean(g2)*snr(i);
    
    gc = (1 - sqrt(gam_c/(2+gam_c)));%eq 25
    gd = (1-sqrt(gam_d/(2+gam_d)));
    ge = (1-sqrt(gam_e/(2+gam_e)));
    gf = (1-sqrt(gam_f/(2+gam_f)));
    gg = (1-sqrt(gam_g/(2+gam_g)));
    
    term4 =  1-sqrt(gammaH/(1+gammaH));
    term5 =  omega_mu/(omega_mu+omega_p);
    berul_2(i) = 0.25 * (term4+term5); 
    ber_th2(i) = 0.5*gc - 0.25*gd + 0.25*(ge+gf-gg);
    
    gamma1(i) = a1*mean(g1)/(a2*mean(g1) + 1)*snr(i);
    gamma2(i) = a2*mean(g2)*snr(i);
    
    %----------------BER of Data User2-----------------------------------------
    recdata21=(data_equilized'*code2')';
    recdata22=real(recdata21)>0;
    errors_user2(i) = size(find([data_user2- recdata22]),2); %Errors for User1
    SBer2 = errors_user2/M;  
    
    y3_dash = recdata21 - sqrt(b1)*recdata22;%sic
    x3_hat = real(y3_dash)>0;
    errors_user33(i) = size(find([data_user22- x3_hat]),2); %Errors for User1
    SBer33 = errors_user33/M;  % simulated ber user2
end

% ------------------------Theoretical Result-------------------------------
snrlnr=10.^(snr/10);
TBer = 0.5*erfc(sqrt(snrlnr)); % Theoretical BER for AWGN
TBerf = 0.5.*(1-sqrt(snrlnr./(snrlnr+1)));% theoretical BER fro Flat fadding

%-------------------Displaying Result--------------------------------------       
figure
%semilogy(snr,TBer,'c*-','LineWidth',2);
%hold on;
%semilogy(snr,TBerf,'r-','LineWidth',2);
hold on;
%semilogy(snr,SBer1,'bd-','LineWidth',2);
hold on;
semilogy(snr,SBer11,'bs-','LineWidth',2);
hold on;
%semilogy(snr,SBer2,'mo-','LineWidth',2);
hold on;
%semilogy(snr,SBer33,'m*-','LineWidth',2);
hold on;
%semilogy(snr,berul_2,'ro-','LineWidth',2);
axis([0 20 0.3 0.5]);
grid on
%legend('Theoratical BER for BPSK on AWGN ','Theoratical BER for BPSK on Rayleigh Channel ' ,'Simulated BER for User13','Simulated BER for User24','Simulated BER for User2','Simulated BER for User3');
legend('Theoretical OMA ' ,'Simulated: user 1 $x_1$','Simulated: user 1 $x_2$ ','Simulated: user 2 $x_3$','Simulated: user 2 $x_4$','theoretical BER for $x_1$','Interpreter','latex');

xlabel('SNR, dB');
ylabel('Bit Error Rate');
title('BER Vs SNR on Rayleigh Channel')
