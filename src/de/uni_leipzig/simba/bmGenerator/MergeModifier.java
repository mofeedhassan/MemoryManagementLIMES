/**
 * 
 */
package de.uni_leipzig.simba.bmGenerator;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * @author sherif
 *
 */
public class MergeModifier extends Modifier{

	public Property mergeProperty;

	/**
	 * @return the mergeProperty
	 */
	public Property getMergeProperty() {
		return mergeProperty ;
	}


	/**
	 * @param mergeProperty the mergeProperty to set
	 */
	public void setMergeProperty(Property mergeProperty) {
		this.mergeProperty = mergeProperty;
	}


	/* (non-Javadoc)
	 * @see de.uni_leipzig.simba.benchmarker.Modifier#destroy()
	 */
	@Override
	Model destroy(Model subModel) {
		System.out.println();
		long n = (subModel.size() %2 == 0) ? subModel.size():subModel.size()-1;
		System.out.println("Merging " + n + " triples.");

		StmtIterator sItr = subModel.listStatements();

		while (sItr.hasNext()) {
			Statement stat1 = sItr.nextStatement();	

			if(sItr.hasNext()){
				Statement stat2 = sItr.nextStatement();
				List<Statement> stmts = new ArrayList<Statement>(); 
				stmts.add(stat1);
				stmts.add(stat2);
				Statement mergedStatement = merge(stmts);
				destroyedModel.add(mergedStatement);
			}else{ // if a single property just return it
				destroyedModel.add(stat1);
			}
		}
		return destroyedModel;
	}


	private Statement merge(List<Statement> stmts){
		Statement result=null;
		String mergeObjectStr = new String();
		Resource  subject = null;
		Property  predicate = null;
		RDFNode   object = null;

		for(Statement stmt: stmts){
			if(!stmt.getObject().isLiteral()){
				System.err.println(stmt.getObject() + " is not a literal object");
				System.err.println("Can NOT merge a non-literal object!!");
				return stmt;
			}
			subject   = stmt.getSubject();     
			predicate = stmt.getPredicate();   
			object    = stmt.getObject()	; 
			mergeObjectStr = mergeObjectStr.concat(object.asNode().getLiteral().getLexicalForm()+" ");
		}
		mergeObjectStr= mergeObjectStr.substring(0,mergeObjectStr.length()-1); // remove last space
		RDFNode mergeObject= ResourceFactory.createTypedLiteral(mergeObjectStr);
		if(mergeProperty==null){
			result= ResourceFactory.createStatement(subject, predicate, mergeObject);
		}else{
			result= ResourceFactory.createStatement(subject, mergeProperty, mergeObject);
		}
		return result;
	}

	public static void main(String args[]){
		MergeModifier mM=new MergeModifier();
		Resource s = ResourceFactory.createResource("medo.test");
		RDFNode o1= ResourceFactory.createTypedLiteral("koko");
		RDFNode o2= ResourceFactory.createTypedLiteral("Medo");
		Statement stmt1 = ResourceFactory.createStatement(s, RDFS.label, o1);
		Statement stmt2 = ResourceFactory.createStatement(s, RDFS.label, o2);
		List<Statement> stmts= new ArrayList<Statement>();
		stmts.add(stmt1);
		stmts.add(stmt2);
		mM.mergeProperty= ResourceFactory.createProperty("testProperty");
		System.out.println(mM.merge(stmts));

	}
}
