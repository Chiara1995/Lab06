package it.polito.tdp.meteo;

import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.meteo.bean.Citta;
import it.polito.tdp.meteo.bean.Rilevamento;
import it.polito.tdp.meteo.bean.SimpleCity;
import it.polito.tdp.meteo.db.MeteoDAO;

public class Model {

	//static final VARIABILE GLOBALE della classe che non può essere modificata dopo l'assegnazione
	//definizione di una costante in Java
	private final static int COST = 100;
	private final static int NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN = 3;
	private final static int NUMERO_GIORNI_CITTA_MAX = 6;
	private final static int NUMERO_GIORNI_TOTALI = 15;
	
	List<Citta> citta=null;
	
	public Model() {
		citta=new ArrayList<Citta>();
	}

	public String getUmiditaMedia(int mese) {
		MeteoDAO mdao=new MeteoDAO();
		String ris="";
		ris+="Torino "+mdao.getAvgRilevamentiLocalitaMese(mese, "Torino")+"\n";
		ris+="Milano "+mdao.getAvgRilevamentiLocalitaMese(mese, "Milano")+"\n";
		ris+="Genova "+mdao.getAvgRilevamentiLocalitaMese(mese, "Genova");
		return ris;
	}

	public String trovaSequenza(int mese) {
		List<SimpleCity> solution=new ArrayList<SimpleCity>();
		solution.add(new SimpleCity("BaseAlgoritmo",Integer.MAX_VALUE));
		int step=1;
		List<SimpleCity> parziale=new ArrayList<SimpleCity>();
		MeteoDAO mdao=new MeteoDAO();
		Citta g=new Citta("Genova", mdao.getAllRilevamentiLocalitaMese(mese, "Genova"));
		Citta t=new Citta("Torino", mdao.getAllRilevamentiLocalitaMese(mese, "Torino"));
		Citta m=new Citta("Milano", mdao.getAllRilevamentiLocalitaMese(mese, "Milano"));
		citta.add(t);
		citta.add(g);
		citta.add(m);
		
		recursive(parziale,step, solution);
		String ris="";
		for(int i=0; i<solution.size(); i++){
			ris+=solution.get(i).getNome()+"\n";
		}
		return ris.trim();
	}
	
	private void recursive(List<SimpleCity> parziale, int step, List<SimpleCity> solution) {
		
		//Controllo della condizione di terminazione
		if(step>NUMERO_GIORNI_TOTALI){
			if( controllaParziale(parziale)){
				if(punteggioSoluzione(parziale)<punteggioSoluzione(solution)){
					solution.clear();
					solution.addAll(parziale);
				}
			}
			return;
				
		}
		
		//Devo generare le nuove soluzioni parziali
		for(Citta c : citta){
			SimpleCity sc=new SimpleCity(c.getNome());
			parziale.add(sc);
			sc.setCosto(calcolaCosto(step, parziale, c));
			recursive(parziale, step+1, solution);
			parziale.remove(step-1);
		}
		
	}


	private int calcolaCosto(int step, List<SimpleCity> parziale, Citta c) {
		int costo=0;
		if(step>1 && !parziale.get(parziale.size()-1).getNome().equals(parziale.get(parziale.size()-2).getNome()))
			costo+=COST;
		String data="";
		for(Rilevamento r : c.getRilevamenti()){
			data=String.valueOf(r.getData());
			if(Integer.parseInt(data.substring(data.length()-2, data.length()))==step){
				costo+=r.getUmidita();
			}
		}
		return costo;
	}

	private int punteggioSoluzione(List<SimpleCity> soluzioneCandidata) {

		int score = 0;
		for(SimpleCity sc : soluzioneCandidata){
			score+=sc.getCosto();
		}
		return score;
	}

	private boolean controllaParziale(List<SimpleCity> parziale) {
		int cont=0;
		for(Citta c : citta){
			cont=0;
			for(SimpleCity sc : parziale){
				if(sc.getNome().equals(c.getNome()))
				cont++;
			}
			if(cont>NUMERO_GIORNI_CITTA_MAX)
				return false;
		}
		cont=0;
		for(int i=0; i<parziale.size(); i++){
			if(i==0)
				cont=1;
			else{
				if(parziale.get(i).getNome().equals(parziale.get(i-1).getNome()))
					cont++;
				else{
					if(cont<NUMERO_GIORNI_CITTA_CONSECUTIVI_MIN)
						return false;
					else
						cont=1;
				}
			}
		}
		return true;
	}

}
