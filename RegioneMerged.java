
public class RegioneMerged {

	public int start_row ;
	public int end_row ;
	public int start_col ;
	public int end_col;
	public String cellRef; //id della regione, cioè della cella in alto a sinistra
	
	public RegioneMerged(int sr, int er, int sc, int ec,String cellRef)
	{
		this.start_row = sr;
		this.end_row = er;
		this.start_col = sc;
		this.end_col = ec;
		this.cellRef = cellRef;
	}
	
}
