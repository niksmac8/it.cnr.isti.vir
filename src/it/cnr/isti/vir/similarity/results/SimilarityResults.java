/*******************************************************************************
 * Copyright (c) 2013, Fabrizio Falchi (NeMIS Lab., ISTI-CNR, Italy)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met: 
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package it.cnr.isti.vir.similarity.results;

import it.cnr.isti.vir.classification.AbstractLabel;
import it.cnr.isti.vir.classification.ILabeled;
import it.cnr.isti.vir.features.AbstractFeaturesCollector;
import it.cnr.isti.vir.features.AbstractFeaturesCollector_Labeled_HasID;
import it.cnr.isti.vir.features.FeaturesCollectorArr;
import it.cnr.isti.vir.features.localfeatures.ALocalFeature;
import it.cnr.isti.vir.features.localfeatures.ALocalFeaturesGroup;
import it.cnr.isti.vir.file.ArchiveException;
import it.cnr.isti.vir.file.FeaturesCollectorsArchives;
import it.cnr.isti.vir.id.AbstractID;
import it.cnr.isti.vir.id.IDClasses;
import it.cnr.isti.vir.id.IDInteger;
import it.cnr.isti.vir.id.IHasID;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

public class SimilarityResults<E> implements ISimilarityResults<E> {

	protected final Collection<ObjectWithDistance<E>> coll;
	protected E query;
	
	protected ALocalFeaturesGroup excludedGroup = null;
	
	public ALocalFeaturesGroup getExcludedGroup() {
		return excludedGroup;
	}

	public void setExcludedGroup(ALocalFeaturesGroup excludedGroup) {
		this.excludedGroup = excludedGroup;
	}

	private class SimilarityResultsIterator<E> implements Iterator<E>{
		 
//        private OneWayNode curPos;
//        private int curModCount;
		private E next;
		private Iterator<E> internalIterator;
 
        private SimilarityResultsIterator(Iterator<E> givenIt){
        	internalIterator = givenIt;
        	this.setNext();
        }
        
        private void setNext() {
        	if (!internalIterator.hasNext()) {
        		next = null;
        		return;
        	}
        	next = internalIterator.next();
        	while ( ((ALocalFeature) ((ObjectWithDistance) next).getObj()).getLinkedGroup() == excludedGroup ) {
        		if (!internalIterator.hasNext()) {
        			next = null;
        			return;
        		}
            	next = internalIterator.next();
        	}
        }
 
        public boolean hasNext(){
            return next != null;
        }
 
        public E next(){
            if (! this.hasNext() )
                throw new IllegalStateException();
//            if (this.curModCount != modCount)
//                throw new ConcurrentModificationException()
            E data = next;
            setNext();
            return data;
        }
 
        public void remove(){
            throw new UnsupportedOperationException();
        }
    }  
	
	public SimilarityResults( E query, int initSize) {
		this.query = query;
		coll = new ArrayList<ObjectWithDistance<E>>(initSize);
	}
	
	public SimilarityResults(int initSize) {
		this(null, initSize);
	}
	
	public SimilarityResults(Collection<ObjectWithDistance<E>> given ) {
		this(null, given);
	}
	
	public SimilarityResults(ObjectWithDistance<E>[] given ) {
		this(null, given);
	}
	
	public SimilarityResults(ObjectWithDistance<E> given ) {
		this.query = null;
		coll = new ArrayList<ObjectWithDistance<E>>(1);
		coll.add(given);
	}
	
	public SimilarityResults(E query, Collection<ObjectWithDistance<E>> given ) {
		this.query = query;
		coll = given;
	}
	
	public SimilarityResults(E query, ObjectWithDistance<E>[] given ) {
		this.query = query;
		if ( given == null ) {
			coll = null;
		} else {
			coll = new ArrayList<ObjectWithDistance<E>>(given.length);
			for (int i=0; i<given.length; i++)
				coll.add( given[i]);
		}	
	}
	
//	public SimilarityResults(E query) {
//		this.query = query;
//		coll = new LinkedList<ObjectWithDistance<E>>();
//	}
//	
	
	public void setQuery(E query ) {
		this.query = query;
	}
	
	public E getQuery() {
		return query;
	}
	
	public void removeFirst() {
		coll.remove(coll.iterator().next());
	}
	
	public AbstractID getQueryID() {
		AbstractID id = null;
		if ( AbstractID.class.isInstance(query))
			id = (AbstractID) query;	
		else
			id = ((IHasID) query).getID();
		return id;
	}
	

		
	public Iterator<ObjectWithDistance<E>> iterator() {
		if ( excludedGroup == null ) return coll.iterator();
		//System.out.println("Results Size " + coll.size());
		return new SimilarityResultsIterator(coll.iterator());
	}

	@Override
	public int size() {
		if ( coll == null ) return 0;
		return coll.size();
	}
	
	@Override
	public boolean  equalResults(ISimilarityResults<E> that) {
		if ( !this.query.equals(((SimilarityResults) that).query)) return false;
		if ( this.size() != that.size() ) return false;
		Iterator<ObjectWithDistance<E>> itThis = this.iterator();
		for ( Iterator<ObjectWithDistance<E>> itThat = that.iterator(); itThat.hasNext(); ) {
			if ( ! itThis.next().equals(itThat.next()) ) return false;
		}
		return true;
	}


	public void writeIDData_old(DataOutputStream out) throws IOException {
		
		// QUERY ID
		AbstractID id = null;
		if ( AbstractID.class.isInstance(query))
			id = (AbstractID) query;	
		else
			id = ((IHasID) query).getID();
		out.writeInt(IDClasses.getClassID(id.getClass()));
		id.writeData(out);
		
		// RESULTS 
		out.writeInt(coll.size());
		for (Iterator<ObjectWithDistance<E>> itThis = this.iterator(); itThis.hasNext(); ) {
			ObjectWithDistance<E> obj = itThis.next();
			obj.writeIDFloat(out);
		}
	}
	
	public void writeIDData(DataOutputStream out) throws IOException {
		
		// QUERY ID
		AbstractID qID = null;
		if ( AbstractID.class.isInstance(query))
			qID = (AbstractID) query;	
		else
			qID = ((IHasID) query).getID();
		
		// Write ID Class
		IDClasses.writeClass( qID.getClass(), out );

		// Write Query ID 
		qID.writeData(out);
		
		// Result size
		out.writeInt(coll.size());
		
		// Reslts
		for (Iterator<ObjectWithDistance<E>> itThis = this.iterator(); itThis.hasNext(); ) {
			ObjectWithDistance<E> obj = itThis.next();
			obj.writeIDFloat(out);
		}
		
	}
	
	public SimilarityResults(DataInputStream in ) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
//		int idType = in.readInt();
		// work around for old Cophir
//		if ( idType != 1001 ) { 
//			throw new IOException("IDType error!");
//		}
		
		Constructor<? extends AbstractID> idc = IDClasses.readClass(in).getConstructor(DataInputStream.class);
		
		query = (E) idc.newInstance(in);

		int size = in.readInt();
		coll = new ArrayList(size);
		
		for (int i=0; i<size; i++) {
			coll.add(new ObjectWithDistance(in));
		}
	}
	
//	public SimilarityResults(DataInputStream in  ) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
//		int idType = in.readInt();
//		// work around for old Cophir
////		if ( idType != 1001 ) { 
////			throw new IOException("IDType error!");
////		}
//		
//		query = (E) new IDInteger(in);
//		//query = (E) IDClasses.readData(in);
//		int size = in.readInt();
//		coll = new ArrayList(size);
//		
//		for (int i=0; i<size; i++) {
//			coll.add(new ObjectWithDistance(in));
//		}
//	}
	
	public static SimilarityResults[] readArray(DataInputStream in ) throws IOException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		LinkedList<SimilarityResults> list = new LinkedList<SimilarityResults>();
		while ( in.available() > 0) {
			list.add( new SimilarityResults(in) );
		} 
		
		SimilarityResults[] arr = new SimilarityResults[list.size()];
		int i=0;
		for ( Iterator<SimilarityResults> it = list.iterator(); it.hasNext(); i++ ) {
			arr[i]=it.next();
		}
		return arr;
	}

	@Override
	public ISimilarityResults getResultsIDs() {
		if ( AbstractID.class.isInstance(query) ) return this;
		
		// RESULTS 
		LinkedList list = new LinkedList();
		for (Iterator<ObjectWithDistance<E>> itThis = coll.iterator(); itThis.hasNext(); ) {
			ObjectWithDistance<E> obj = itThis.next();
			list.add(obj.getIDObjectWithDistance());
		}
				
		SimilarityResults resIDs = new SimilarityResults(list);
		resIDs.setQuery(((IHasID)query).getID());
		return resIDs;
	}

	public SimilarityResults getRemovingLFByGroup(ALocalFeaturesGroup toRemoveGroup) {
		SimilarityResults res = new SimilarityResults(this.size());
		for (Iterator<ObjectWithDistance<E>> itThis = coll.iterator(); itThis.hasNext(); ) {
			ObjectWithDistance<E> obj = itThis.next();
			if ( ! (toRemoveGroup == ((ALocalFeature) obj.obj).getLinkedGroup()) ) {
				res.coll.add(obj);
			}
		}		
		
		return res;
	}

	@Override
	public ObjectWithDistance<E> getFirst() {
		if ( coll == null || coll.size() == 0 ) return null;
		return coll.iterator().next();
	}
	
	
	public String toString() {
		//String tStr = query.toString() + "\t";
		String tStr = "";
		if ( query != null ) {
			AbstractID id = ((IHasID) query).getID();
			if ( id != null ) tStr = id.toString() + "\t";
		}
		
		for (Iterator<ObjectWithDistance<E>> itThis = this.iterator(); itThis.hasNext(); ) {
			tStr += itThis.next().toString() +"\t";
		}
		return tStr;
	}
	
	public String getHtmlTableRow_Images(String preFix, String postFix ) {
		return getHtmlTableRow_Images( preFix, postFix, null, null);
	}
	
	public String getHtmlTableRow_Images(String preFix, String postFix, Integer subStringInt, Integer height) {
		String tStr = "<tr>\n";
		if ( height == null) height = 100;
		if ( subStringInt == null )
			tStr += "<td>"+ ((IHasID)query).getID()+ "<br>" + "<img src=\"" + preFix + ((IHasID)query).getID()+ postFix + "\" height=\""+height+"\"></td>";
		else
			tStr += "<td>"+ ((IHasID)query).getID() + "<br>" +  "<img src=\"" + preFix + ((IHasID)query).getID().toString().substring(0, subStringInt)+ "/"  + ((IHasID)query).getID()+ postFix + "\" height=\""+height+"\"></td>";
		for (Iterator<ObjectWithDistance<E>> itThis = this.iterator(); itThis.hasNext(); ) {
			ObjectWithDistance<E> curr = itThis.next();
			AbstractID id = ((IHasID)curr.obj).getID();
			if ( subStringInt == null ) {
				tStr += "<td>" + String.format("%.3f", curr.dist) + "<br>" +  "<img src=\"" + preFix + id + postFix + "\" title=\""+((IHasID)curr.obj).getID() + " d ";
				tStr += String.format("%.3f", curr.dist);
				tStr += "\" height=\""+height+"\"></td>";
			} else 
				tStr += "<td>" + String.format("%.3f", curr.dist) + "<br>" +  "<img src=\"" + preFix + id.toString().substring(0, subStringInt) + "/" + id + postFix + "\" title=\""+((IHasID)curr.obj).getID() + " d " + String.format("%.3f", curr.dist) + "\" height=\""+height+"\"></td>";
		}
		
		return tStr + "</tr>";
	}
	
	public String getHtmlTableRow_Images_Cophir(String preFix, String postFix, Integer height) {
		return getHtmlTableRow_Images_Cophir(preFix, postFix, height, Integer.MAX_VALUE);
	}
	
	public String getHtmlTableRow_Images_Cophir(String preFix, String postFix, Integer height, Integer k) {
		String tStr = "<tr>\n";
		if ( height == null) height = 100;
		String idPath = getCoPhIRIDPath(((IHasID)query).getID());
		// Query
		tStr += "<td>"+ ((IHasID)query).getID()+ "<br>" + "<img src=\"" + preFix + idPath + postFix + "\" height=\""+height+"\"></td>";
		
		int count = 0;
		// Results
		for (Iterator<ObjectWithDistance<E>> itThis = this.iterator(); itThis.hasNext() && count < k; ) {
			ObjectWithDistance<E> curr = itThis.next();
			AbstractID id = ((IHasID)curr.obj).getID();
			idPath = getCoPhIRIDPath(id);
			tStr += "<td>" + String.format(Locale.ENGLISH, "%.9f", curr.dist) + "<br>" +  "<img src=\"" + preFix + idPath + postFix + "\" title=\""+((IHasID)curr.obj).getID() + " d ";
			tStr += String.format("%.3f", curr.dist);
			tStr += "\" height=\""+height+"\"></td>";
			count++;
		}
		
		return tStr + "</tr>";
	}
	
	public static String getCoPhIRIDPath(AbstractID id) {
		int idInt = ((IDInteger) id).id;
		String idStr = String.format("%09d", idInt);
		if ( idInt <= 999999999 ) {			
			return idStr.substring(0, 3) + "/" + idStr.substring(3, 6) + "/" + idStr;
		} else {
			return idStr.substring(0, 4) + "/" + idStr.substring(4, 7) + "/" + idStr;
		}
		
	}


	@Override
	public Collection<AbstractFeaturesCollector_Labeled_HasID> getFCs( FeaturesCollectorsArchives archives ) throws ArchiveException {
		ArrayList<AbstractFeaturesCollector_Labeled_HasID> res = new ArrayList();
		for (Iterator<ObjectWithDistance<E>> itThis = this.iterator(); itThis.hasNext(); ) {
			ObjectWithDistance<E> curr = itThis.next();
			AbstractID id = ((IHasID) curr.obj).getID();
			AbstractLabel label = null;
			if ( curr.obj instanceof ILabeled )
				label = ((ILabeled) curr.obj).getLabel();
			
			AbstractFeaturesCollector temp = (AbstractFeaturesCollector) archives.get( id );
			FeaturesCollectorArr currFC = new FeaturesCollectorArr( temp.getFeatures(), id, label );
			
			res.add( currFC );
						
		}
		return res;
	}

	
}
