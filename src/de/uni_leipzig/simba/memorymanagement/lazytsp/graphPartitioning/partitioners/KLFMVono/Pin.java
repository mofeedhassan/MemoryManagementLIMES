package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners.KLFMVono;

public class Pin extends Id{
	
	Cell m_Cell;
    Net m_Net;
    
	public Pin()
	{
	    //ctor
	    SetCell(null);
	    SetNet(null);
	}
	
	Cell GetCell()
    {
        return m_Cell;
    }
    void SetCell(Cell val)
    {
        m_Cell = val;
    }
    Net GetNet()
    {
        return m_Net;
    }
    void SetNet(Net val)
    {
        m_Net = val;
    }

}
