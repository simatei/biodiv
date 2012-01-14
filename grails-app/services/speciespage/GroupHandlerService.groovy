package speciespage

import org.apache.commons.logging.LogFactory;

import species.Classification;
import species.Species;
import species.TaxonomyDefinition;
import species.TaxonomyDefinition.TaxonomyRank;
import species.TaxonomyRegistry;
import species.formatReader.SpreadsheetReader;
import species.groups.SpeciesGroup;
import species.groups.SpeciesGroupMapping;
import species.sourcehandler.XMLConverter;

class GroupHandlerService {

	static transactional = false

	private static final log = LogFactory.getLog(this);

	def grailsApplication
	def sessionFactory

	static int GROUP_UPDATION_BATCH = 20;

	def speciesGroupMappings;


	/**
	 * 
	 * @param file
	 * @param contentSheetNo
	 * @param contentHeaderRowNo
	 * @return
	 */
	def loadGroups(String file, contentSheetNo, contentHeaderRowNo) {
		log.debug "Loading groups and their association with species";
		List<Map> content = SpreadsheetReader.readSpreadSheet(file, contentSheetNo, contentHeaderRowNo);
		//TODO:sort groups in name and rank order
		content.each { row ->
			String name = row.get("group");
			String canonicalName = row.get("name");
			String rank = row.get("rank");
			String parentGroupName = row.get("parent group");

			int taxonRank = XMLConverter.getTaxonRank(rank);
			TaxonomyDefinition taxonConcept = TaxonomyDefinition.findByCanonicalFormAndRank(canonicalName, taxonRank);
			SpeciesGroup parentGroup = SpeciesGroup.findByName(parentGroupName);
			SpeciesGroupMapping speciesGroupMapping = new SpeciesGroupMapping(taxonName:canonicalName, rank:taxonRank, taxonConcept:taxonConcept);
			SpeciesGroup group = addGroup(name, parentGroup, speciesGroupMapping);
		}
		updateGroups();
	}

	/**
	 * Adds a group with given name and associates given mappings
	 * @param name
	 * @param parentGroup
	 * @return
	 */
	SpeciesGroup addGroup(String name, SpeciesGroup parentGroup, SpeciesGroupMapping speciesGroupMapping) {
		if(name) {
			SpeciesGroup group = SpeciesGroup.findByName(name);

			if(!group) {
				group = new SpeciesGroup(name:name, parentGroup:parentGroup);
			}

			def mapping = SpeciesGroupMapping.findByTaxonNameAndRank(speciesGroupMapping.taxonName, speciesGroupMapping.rank);
			if(!mapping) {
				group.addToSpeciesGroupMapping(speciesGroupMapping);
				if(!group.save(flush:true)) {
					log.error "Unable to save group : "+name;
					group.errors.allErrors.each { log.error it }
				}
			}
			return group;
		}
		else {
			log.error "Group name cannot be empty";
		}
	}

	/**
	 * Updates group for all species by going along its hierarchy and checking 
	 * parent if at any level has a corresponding group mapping.
	 * A species should not have multiple paths in the same classification 
	 * @return
	 */
	int updateGroups() {
		int noOfUpdations = 0;
		int offset = 0;
		int limit = GROUP_UPDATION_BATCH;
		int noOfFailures = 0;
		
		def taxonConcepts;
		
		long startTime = System.currentTimeMillis();
		while(true) {
			taxonConcepts = TaxonomyDefinition.findAll("from TaxonomyDefinition as t where t.rank = :rank and t.group is null",
					[rank: TaxonomyRank.SPECIES.ordinal()], [max:limit, offset:offset]);
			
			if(!taxonConcepts) break;
			
			taxonConcepts.each { taxonConcept ->
				if(updateGroup(taxonConcept)) {
					noOfUpdations ++;
				} else {
					noOfFailures++;
				}
			}
			
			offset = noOfFailures; 
			cleanUpGorm();
			log.debug "Updated group for taxonConcepts ${noOfUpdations}"
		}

		if(noOfUpdations) {
			cleanUpGorm();
			log.debug "Updated group for taxonConcepts ${noOfUpdations} in total"
		}
		
		log.debug "Time taken to update groups for taxonConcepts ${noOfUpdations} is ${System.currentTimeMillis()-startTime}(msec)";
		return noOfUpdations;
	}

	/**
	 * Updates group for all species by going along its hierarchy and checking
	 * parent if at any level has a corresponding group mapping.
	 * A species should not have multiple paths in the same classification
	 * @return
	 */
	int updateGroups(List<Species> species) {
		int noOfUpdations = 0;

		species.each { s ->
			if(updateGroup(s.taxonConcept)) {
				noOfUpdations ++;
			}
			if(noOfUpdations % GROUP_UPDATION_BATCH == 0) {
				cleanUpGorm();
			}
		}

		if(noOfUpdations) {
			cleanUpGorm();
		}
		return noOfUpdations;
	}
	
	/**
	 * Tries to deduce group for the taxon concept based on its hierarchy 
	 * and updates group for itself and all of its child concepts   
	 */
	boolean updateGroup(TaxonomyDefinition taxonConcept) {
		//parentTaxon has hierarchies from all classifications
		return updateGroup(taxonConcept, getGroupByHierarchy(taxonConcept, taxonConcept.parentTaxon()));
	}

	/**
	 * Updates group for taxonConcept and all concepts below this under any of the hierarchies
	 * @param taxonConcept
	 * @param group
	 * @return
	 */
	boolean updateGroup(TaxonomyDefinition taxonConcept, SpeciesGroup group) {
		//log.debug "Updating group associations for taxon concept : "+taxonConcept;
		int noOfUpdations = 0;

		if(taxonConcept && group) {

			if(!group.equals(taxonConcept.group)) {
				taxonConcept.group = group;
				if(taxonConcept.save()) {
					log.debug "Setting group '${group.name}' for taxonConcept '${taxonConcept.name}'"
					noOfUpdations++;
				} else {
					taxonConcept.errors.allErrors.each { log.error it }
				}
			}
		}
		return noOfUpdations ?: false;
	}

	/**
	 * returns the groups if there is a match with mappings defined 
	 */
	private SpeciesGroup getGroupByMapping(TaxonomyDefinition taxonConcept) {
		SpeciesGroup group;
		if(!speciesGroupMappings) {
			speciesGroupMappings = SpeciesGroupMapping.listOrderByRank('desc');
		}
		
		speciesGroupMappings.each { mapping ->
			if((taxonConcept.name.trim().equals(mapping.taxonName)) && taxonConcept.rank == mapping.rank) {
				group = mapping.speciesGroup;
			}
		}
		return group;
	}

	/**
	 * returns the group for the closest ancestor.
	 * 
	 */
	private SpeciesGroup getGroupByHierarchy(TaxonomyDefinition taxonConcept, List<TaxonomyDefinition> parentTaxon) {
		int rank = TaxonomyRank.KINGDOM.ordinal();

		SpeciesGroup group;
		parentTaxon.sort { it.rank };

		log.debug "Parent Taxon : "+parentTaxon 
		for(int i=parentTaxon.size() -1; i>=0; i--) {
			group = getGroupByMapping(parentTaxon.get(i))
			if(group) {
				break;
			}
		}
		return group;
	}

	/**
	 *
	 */
	private void cleanUpGorm() {
		def hibSession = sessionFactory?.getCurrentSession()
		if(hibSession) {
			log.debug "Flushing and clearing session"
			hibSession.flush()
			hibSession.clear()
			speciesGroupMappings.each { mapping ->
				if(!mapping.isAttached()) {
					mapping.attach();
				}
			}			
		}
	}
}
