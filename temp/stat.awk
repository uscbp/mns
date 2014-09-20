BEGIN { 
        m_tilt=0;
        m_bank=0; 
        N=0;
	}

	{ if ($1=="@TILT:") {
	   tilt[N]=$2;
           bank[N]=$6;
           m_tilt = (m_tilt * N + tilt[N]) / (N+1);
           m_bank = (m_bank * N + bank[N]) / (N+1);
           N++;

        v_tilt=0;
        v_bank=0;
	for (i=0;i<N;i++) {
	   v_tilt += (tilt[i] - m_tilt)*(tilt[i] - m_tilt);
	   v_bank += (bank[i] - m_bank)*(bank[i] - m_bank);
	 }
         v_tilt = sqrt(v_tilt/N);
	 v_bank = sqrt(v_bank/N);
	 print N, ") TILT  (mean,var)", m_tilt, " , ", v_tilt;
	 print N, ") BANK  (mean,var)", m_bank, " , ", v_bank;
         }
        }

