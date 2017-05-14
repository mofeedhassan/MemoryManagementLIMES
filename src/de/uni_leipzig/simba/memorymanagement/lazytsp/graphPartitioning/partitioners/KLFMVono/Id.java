package de.uni_leipzig.simba.memorymanagement.lazytsp.graphPartitioning.partitioners.KLFMVono;

public class Id {
		public  Id()
		{
	    //ctor
	    m_Id=-1;
		}
        public String GetName() { return m_Name; }
        public void SetName(String val) { m_Name = val; }
        public  long GetId() { return m_Id; }
        public void SetId(long val) { m_Id = val; }

    	public String m_Name;
    	public long m_Id;
}
