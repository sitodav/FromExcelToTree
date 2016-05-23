import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;


//classe che costruisce un albero dato un foglio excel
/*HP utilizzo:
 * -i nodi sono le REGIONI di celle 
 * -il livello dell'albero cresce spostandosi di colonna (come se l'albero fosse orizzontalizzato, la radice è la col 0 etc)
 * -il riferimento alla cella in un nodo è quello della cella in alto a sinistra della regione
 * -NEL CASO IN CUI SI ABBIA UNA REGIONE (NODO) PIU' LUNGA (CHE COPRE UN NUMERO MAGGIORE DI RIGHE) RISPETTO ALLA REGIONE PADRE
 * 		ALLORA VIENE SPLITTATA SECONDO LA GRANDEZZA DELLA REGIONE PADRE
 * - il file excel deve essere pretrattato, inserendo tutte le celle che risultano null tramite l'utility
 * -occorre sapere a priori qual è l'ultima colonna
 * -si usa il flag radiceFantasma per indicare che il nodo non esiste realmente come regione/cella in excel, ma è la radice fittizia usata per considerare
 *      tutte le regioni della colonna 0esima come sue figlie
 *      
 *
 */

public class Nodo {

	XSSFSheet sheet;
	ArrayList<Nodo> figli = new ArrayList<Nodo>();
	int[] boundsRegione; //[startrow,endrow,startcol,endcol]
	int[] boundsRegionePadre; //usati per sovraframmentare la regione di questo nodo, nel caso spiegato sopra
	XSSFCell cellaStart;
	UtilExc util;
	String stringValue;
	int lastColIndex;
	private int lvl;
	Connection db;
	
	
	public Nodo(boolean isGhostRoot,UtilExc util,XSSFSheet sheet,int[] boundsRegionePadre, XSSFCell cellaStart,int lastColumnIndex,Connection db)
	{
		this.util = util;
		this.cellaStart = cellaStart;
		this.boundsRegionePadre = boundsRegionePadre;
		this.sheet = sheet;
		this.lastColIndex = lastColumnIndex;
		this.db = db;
		
		stringValue = cellaStart != null ? cellaStart.toString() : "";
		
		if(isGhostRoot)
		{
			boundsRegione = boundsRegionePadre;
		}
		else if(cellaStart == null) 
		{
			boundsRegione = new int[]{boundsRegionePadre[0], boundsRegionePadre[0], boundsRegionePadre[2]+1,boundsRegionePadre[3]+1};
		}
		else
		{
			boundsRegione = util.getBoundsRegioneAppartenenza(cellaStart);
			//controllo se è il caso di sovraframmentare
			if(boundsRegione[1] > boundsRegionePadre[1]) //allora la regione attuale esce fuori da quella del padre, e non va bene
			{
				
				boundsRegione[1] = boundsRegionePadre[1]; //limite sup regioen padre
			}
			if(boundsRegione[0] < boundsRegionePadre[0])
			{
				boundsRegione[0] = boundsRegionePadre[0]; //limite inf della regione padre
			}
			
		}
		
		this.lvl = boundsRegione[2];
		
		
		
		String refA = new CellReference(boundsRegione[0],boundsRegione[2]).formatAsString();
//		System.out.println(refA+" "+boundsRegione[0]+" "+boundsRegione[1]+" "+boundsRegione[2]+" "+boundsRegione[3]+" :"+stringValue);
		
		
		
		//risolvo i figli (cioè tutte le distinte regioni contenute nelle righe appartenenti al range della regione attuale
		//ma dalla prima colonna successiva all'ultima della regione attuale)
		//a patto che il foglio non sia terminato
		int colDeiFigli = boundsRegione[3]+1;
		
		if(colDeiFigli > lastColumnIndex)
		{
			return;
		}
		
		HashMap<String,Boolean> temp = new HashMap<String,Boolean>();  //NB: piu' iR diversi potrebbero appartenere alla stessa regione e quindi quella regione va presa una sola volta comunque
																		//uso questa struttura di appoggio dove le chiavi sono i rif della regione (riferimento cella in alto a sx)
		if(stringValue.equals("Commercio ambulante "))
		{
			System.out.println("");
		}
		
		for(int iR = boundsRegione[0]; iR<= boundsRegione[1];iR++) 
		{							
			XSSFCell cellaRappresentativaRegione = null;
			
			RegioneMerged regioneDelFiglio = util.isCellInRegion(iR, colDeiFigli);
			 
			if(regioneDelFiglio == null) //allora è cella unitaria (oppure la cella figlia non esiste)
			{ 
				temp.put(new CellReference(iR,colDeiFigli).formatAsString(),true); //salvo come riferimento della regione (in realtà è cella unitaria) proprio quello della cella
				cellaRappresentativaRegione = sheet.getRow(iR).getCell(colDeiFigli);
			}
			else if(temp.containsKey(regioneDelFiglio.cellRef)) //allora la cella singola vagliata (che appartiene ad una regione) ha la regioen di appartenenza che già era stata considerata
			{
				continue;
			}
			else
			{
				temp.put(regioneDelFiglio.cellRef, true);
				cellaRappresentativaRegione = sheet.getRow(regioneDelFiglio.start_row).getCell(regioneDelFiglio.start_col);
				
				
			}
			//lancio ricorsivamente nodo verso questa regione figlia individuata
			
			figli.add(new Nodo(false,util,sheet,this.boundsRegione,cellaRappresentativaRegione,lastColumnIndex,db));
		}
		
	}
	
	public void rappresentaSottoAlbero()
	{
		
		String ref = new CellReference(boundsRegione[0],boundsRegione[2]).formatAsString();
		
		
		System.out.print("\n");
		for(int k = 0;k<boundsRegione[2];k++)
		{
			System.out.print("--");
		}
		
		System.out.print("ref: "+ref+", lvl: "+boundsRegione[2]+" > "+stringValue);
		
		for(Nodo figlio : figli)
		{
			figlio.rappresentaSottoAlbero();
		}
		
	}
	
	public int getLvlNodo()
	{
		//il livello è l'indice colonna della regione
		return boundsRegione[2];
	}
	
	
	 
	
	

	
	
	
}
