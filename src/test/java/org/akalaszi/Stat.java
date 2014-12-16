package org.akalaszi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import chemaxon.marvin.modelling.mm.mmff94.MMFF94;
import chemaxon.marvin.modelling.struc.MolGeom;
import chemaxon.struc.Molecule;

public class Stat {
	/**
	 * Create some statistics for a specific project.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Molecule[] mols = MolGeom.readMols("out2_part-r-00000.sdf");
		List<Molecule> ret = new ArrayList<Molecule>();
		ListMultimap<String, Double> multimap = ArrayListMultimap.create();

		for (Molecule molecule : mols) {
			if (molecule.getDim() == 3) {
				// ret.add(molecule);
				String id = molecule.getProperty("keyId");
				MMFF94 mm = new MMFF94();
				mm.init(molecule);
				multimap.put(id, mm.getEnergy());
			}
		}
		for (String id : multimap.keySet()) {
			List<Double> energy = multimap.get(id);
			System.out.println(id + ": " + energy);
		}
		
		// MolGeom.writeMol(ret, "3D_out2_part-r-00000.sdf");

		// Iterator<Entry<String, Integer>> it = counts.entrySet().iterator();
		// while (it.hasNext()) {
		// Entry<String, Integer> e = it.next();
		// System.err.println(e.getKey() + "\t" + e.getValue().toString());
		// }
	}
}
