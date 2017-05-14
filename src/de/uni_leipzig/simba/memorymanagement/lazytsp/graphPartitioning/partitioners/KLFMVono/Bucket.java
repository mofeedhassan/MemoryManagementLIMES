package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners.KLFMVono;

public class Bucket/* extends map<Net::Weight,CellList>*/{
//
//	Cell::Square m_Square;
//    Net::Weight m_SumGain;
//    Partition m_Partition;
//    
//	public Bucket()
//	{
//	    //ctor
//	    m_Square=0;
//	    m_SumGain=0;
//	}
//
//	void Bucket::FillByGain(CellList cl)
//	{
//	    m_Square += cl.GetSquare();
//	    m_SumGain=0;
//	    while (!cl.empty()) {
//	        Net::Weight g=cl.begin()->GetGain();
//	        m_SumGain+=(g);
//	        CellList& bl=(*this)[g];
//	        cl.TransferTo(cl.begin(),bl);
//	        }
//	    cl.InvalidateGain();
//	}
//
//	//#ifdef  ALGORITHM_VERBOSE
//	void Bucket::dbg(long id)
//	{
//	    std::cout << "BUCKET #" << id << " SQ=" << m_Square  << " GAIN=" << GetGain() << std::endl;
//	    for(std::map<Net::Weight,CellList>::iterator i=begin();i!=end();i++){
//	        Net::Weight g=i->first;
//	        CellList& bl=i->second;
//	        std::cout << " Gain " << g << " - " << std::flush;
//	        for(std::list<Cell>::iterator j=bl.begin();j!=bl.end();j++){
//	            std::cout << j->GetId() << " ";
//	            }
//	        std::cout << std::endl;
//	        }
//	}
//	//#endif
//
//	void Bucket::IncrementGain(Net::Weight g)
//	{
//	    m_SumGain+=g;
//	}
}
