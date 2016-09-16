package species.trait

import species.participation.DownloadLog;
import content.eml.UFile;
import species.sourcehandler.importer.AbstractImporter;
import species.sourcehandler.importer.CSVTraitsImporter;
import org.apache.commons.io.FilenameUtils;
import java.util.Date;
import species.auth.SUser;
import species.sourcehandler.XMLConverter;
import species.TaxonomyDefinition;
import species.ScientificName.TaxonomyRank

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import species.trait.Trait;
import species.Species;
import species.Field;
import species.trait.Trait.DataTypes;
import species.trait.Trait.TraitTypes;
import species.trait.Trait.Units;
import species.formatReader.SpreadsheetReader;
import groovy.sql.Sql
import species.Language;
import species.License;


class TraitService {

    static transactional=false;

    //    static final BIODIV_NAMESPACE = "http://indiabiodiversity.org/schema/terms/";
    public static final int IMPORT_BATCH_SIZE = 10;

    //protected Dataset dataset;
    //    private String resourceURI = "http://localhost.indiabiodiversity.org/species/show/" 

    private static Map propertyValueIcons;
    def springSecurityService;
    def utilsService;
    def grailsApplication;
    def dataSource;

    void init() {
        // Make a TDB-backed dataset
        def traitsDir = new File(grailsApplication.config.speciesPortal.traits.databaseDir);
        if(!traitsDir.exists()) {
            traitsDir.mkdir();
        }
        String directory = grailsApplication.config.speciesPortal.traits.databaseDir;
        // dataset = getDataset(directory);
        // TDB.getContext().set(TDB.symUnionDefaultGraph, true) ;

        //TODO: populating icons from file
        //TOBE removed
        CSVReader reader = getCSVReader(new File("${grailsApplication.config.speciesPortal.traits.traitValueFile}"))
        reader.readNext();//headers

        propertyValueIcons = [:];
        String[] row = reader.readNext();
        while(row) {
            for(int i=0; i<1; i++) {
                if(row[i]) {
                    row[i] = row[i].trim().toLowerCase().replaceAll('\\s+', '_');
                }
            }
            if(row[0] && row[1] && row[2]) {
                if(!propertyValueIcons.get(row[0])) {
                    propertyValueIcons[row[0]]= [:];
                }
                propertyValueIcons[row[0]][row[1].trim().toLowerCase().replaceAll('\\s+', '_')] = row[2];
            }
            row = reader.readNext();
        }
    }

    //TO BE DELETED AND MOVED TO UTILSSERVICE
    private CSVReader getCSVReader(File file) {
        char separator = '\t'
        if(file.exists()) {
            CSVReader reader = new CSVReader(new FileReader(file), separator, CSVWriter.NO_QUOTE_CHARACTER);
            return reader
        }
        return null;
    }

    private Long findTaxonIdFromName(String name) {
        XMLConverter converter = new XMLConverter();
        TaxonomyDefinition taxon = converter.getTaxonConceptFromName(name, TaxonomyRank.SPECIES.ordinal(), false, null);
        return taxon ? taxon.id : null;
    }

    private boolean isValidTrait(String traitName, traitValue, Long id) {
        println "Validating trait ${traitName} with value ${traitValue} for taxon ${id}"
        //FIX: HACK till loading of traits and validation is inplace
        //return true;

        Trait trait = Trait.findByName(traitName);
        //if(!trait) return false;
        boolean isValid = false;
        isValidType(traitName,traitValue,trait)
        return true;
        trait.valueContraints.each { constraint ->
            isValid = isValid && constraint.validate(trait, traitValue);
        }
        return isValid && isValidPropertyAsPerTaxon(trait, id);
    }

    private boolean isValidType (String traitName, TraitValue traitValue, Trait trait){
        //Trait trait=Trait.findByName(traitName);
        //ValueConstraint traitvalueconstraint= ValueConstraint.findAllByTraitId(trait.id.toLong())
        boolean isValid = false;
        println "==========Value Constarints========"+trait?.valueConstraint
        println "======Trait========"+trait?.id
        println "=======Trait Name========"+traitName
        println "========Trait Value======="+traitValue

        trait?.valueConstraint.each {
            def value = it?.toUpperCase().trim()
            switch(value){
                case 'CATEGORICAL':
                break;
                case 'INTEGER':
                Integer.parseInt(value)
                break;
                case 'FLOAT':
                Double.parseDouble(value)
                break;
                case 'TEXT':
                break;
                case 'BOOLEAN':
                println "====Boolean====="+Boolean.parseBoolean(value)
                break;
                case 'ORDERED':
                break;
                case 'RANGE':
                return value.indexOf('-') != -1
                break;
                case 'DATE':
                return UtilsService.parseDate(value) != null
            }
        }
        return true
    }

    private boolean isValidPropertyAsPerTaxon(Trait trait, Long id) {
        TaxonomyDefinition taxonInstance = TaxonomyDefinition.read(id);
        if(!taxonInstance || !trait) return false;
        List<TaxonomyDefinition> parentTaxon = taxonInstance.parentTaxon();
        return parentTaxon.intersect(trait.taxon).size() > 0
    }

    void loadTraitDefinitions(String file, Language languageInstance) {
        CSVReader reader = getCSVReader(new File(file))
        String[] headers = reader.readNext();//headers
        String[] row = reader.readNext();
        Trait traitInstance
        while(row) {
            Trait trait = new Trait();
            headers.eachWithIndex { header, index ->

                switch(header.toLowerCase()) {

                    case 'trait' :
                    traitInstance = Trait.findByName(row[index].toLowerCase().trim())
                    if(!traitInstance){trait.name = row[index].toLowerCase().trim();}
                    else{traitInstance.name = row[index].toLowerCase().trim();}
                    break;

                    case 'values' : 
                    if(!traitInstance){trait.values = row[index].trim()}
                    else{traitInstance.values = row[index].trim()}
                    break;

                    case 'datatype' : 
                    if(!traitInstance){trait.dataTypes = Trait.fetchDataTypes(row[index].trim())}
                    else{traitInstance.dataTypes = Trait.fetchDataTypes(row[index].trim())}
                    break;

                    case 'traittype' :
                    if(!traitInstance){trait.traitTypes = Trait.fetchTraitTypes(row[index].trim())}
                    else{traitInstance.traitTypes = Trait.fetchTraitTypes(row[index].trim())}
                    break;

                    case 'units' : 
                    if(!traitInstance){trait.units = Trait.fetchUnits(row[index].trim())}
                    else{traitInstance.units = Trait.fetchUnits(row[index].trim())}
                    break;

                    case 'source' : 
                    if(!traitInstance){trait.source = row[index].trim()}
                    else{traitInstance.source = row[index].trim()}
                    break;

                    case 'icon' : 
                    if(!traitInstance){trait.icon = row[index].trim()}
                    else{traitInstance.icon = row[index].trim()}
                    break;

                    case 'taxonid':
                    //TODO: if taxon id is wrong catch exception/trow exception
                    if(!traitInstance){row[index].tokenize(",").each {trait.addToTaxonomyDefinition(TaxonomyDefinition.read(Long.parseLong(it.trim())))}}
                    else{row[index].tokenize(",").each {traitInstance.addToTaxonomyDefinition(TaxonomyDefinition.read(Long.parseLong(it.trim())))}}
                    break;

                    case 'definition':
                    if(!traitInstance){trait.description = row[index].trim()}
                    else{traitInstance.description = row[index].trim()}
                    break;

                    case 'field':
                    if(!traitInstance){trait.field = getField(row[index], languageInstance);}
                    else{traitInstance.field = getField(row[index], languageInstance);}
                    break;

                } 
            }
            //println traitInstance.values
            if(!trait.hasErrors() && !trait.save(flush:true) && !traitInstance) {
                trait.errors.allErrors.each { log.error it }
            }
            else if(traitInstance) {
            if(!traitInstance?.save(flush:true)){
                traitInstance.errors.allErrors.each { log.error it }
            }
        }

            row = reader.readNext();
        }
    }

    private Field getField(String string, Language languageInstance) {
        def f = string.tokenize("|");

        Field field;
        if(f.size() == 1) {
            f = Field.findByLanguageAndConcept(languageInstance, f[0].trim());
        } else if (f.size() == 2) {
            f = Field.findByLanguageAndConceptAndCategory(languageInstance, f[0].trim(), f[1].trim());
        } else  if (f.size() == 3) {
            f = Field.findByLanguageAndConceptAndCategoryAndSubCategory(languageInstance, f[0].trim(), f[1].trim(), f[2].trim());
        } 
    }

    private  getTaxon(String taxonList) {
        def x = TaxonomyDefinition.read(Long.parseLong(taxonList.trim()));
        if(x) {
            return x;
        }
    }



    void loadTraitValues(String file, Language languageInstance) {

        CSVReader reader = getCSVReader(new File(file))
        String[] headers = reader.readNext();//headers
        String[] row = reader.readNext();
        while(row) {
            TraitValue traitValue = new TraitValue();
            Trait trait
            headers.eachWithIndex { header, index ->
                switch(header.toLowerCase()) {
                    case 'trait' : 
                    trait = Trait.findByName(row[index].trim().toLowerCase())
                    traitValue.trait= trait
                    break;
                    case 'value' :
                    traitValue.value=row[index].trim();
                    break;
                    case 'source' : 
                    traitValue.source=row[index].trim()
                    break;
                    case 'icon' : 
                    traitValue.icon=row[index].trim()
                    break;
                    case 'definition' : 
                    traitValue.description=row[index].trim()
                    break;

                } 
            }
            if(validateTrait(trait,traitValue.value)){
                if(!traitValue.hasErrors() && !traitValue.save()) {
                    traitValue.errors.allErrors.each { log.error it }
                }
            }
            row = reader.readNext();
        }
    }

    private boolean validateTrait(Trait trait, String value){
       // Trait traitObj = Trait.findById(trait);
        def rValue
        trait.traitTypes.each{
            switch(it) {
                case "SINGLE_CATEGORICAL":
                def f = trait.values.tokenize("|");
                rValue=f.contains(value);
                break;
                case "MULTIPLE_CATEGORICAL":
                def f = trait.values.tokenize("|");
                rValue=f.contains(value);
                break;
                case "BOOLEAN":
                println "value"+value
                def f = trait.values.tokenize("|");
                rValue=f.contains(value);
                /*                if(value.toLowerCase()=='true' || value.toLowerCase()=='false'){
                                  rValue=true
                }
                else{
                rValue=false
                }*/
                break;
                case "RANGE":
                def f = trait.values.tokenize("|");
                rValue=f.contains(value);
                /* def f = traitObj.values.tokenize("|");
                if(value.indexOf('>')>=0 || value.indexOf('<')>=0){
                rValue=true
                }
                else{
                rValue=false
                }*/
                break;
                case "DATE":
                def f = trait.values.tokenize("|");
                rValue=f.contains(value);
                /*return UtilsService.parseDate(value) != null*/
                break;
            }
        }
        println "rValue="+rValue;
        return rValue;
    }

    void loadFacts(String file, Language languageInstance) {
        CSVReader reader = getCSVReader(new File(file))
        String[] headers = reader.readNext();//headers
        File spreadSheet = new File(file);
        SpreadsheetReader.readSpreadSheet(spreadSheet.getAbsolutePath()).get(0).each{ m ->
            def attribution = m['attribution']
            SUser contributor = SUser.findByEmail(m['contributor'].trim())
            def license =License.findByName(License.fetchLicenseType(("cc " + m['licence']).toUpperCase()))

            TaxonomyDefinition taxon = TaxonomyDefinition.findById(m['taxonid'].trim())
            m.each{key,value->
                Fact fact = new Fact();
                Trait trait = Trait.findByName(key.toLowerCase().trim())
                println "key======>"+key
                println "trait"+trait     
                fact.trait = trait
                fact.traitValue = value
                fact.taxon = taxon
                fact.attribution = attribution
                fact.contributor = contributor
                fact.license = license
                if(fact.trait && fact.traitValue){
                    if(!fact.hasErrors() && !fact.save()) {                        
                        fact.errors.allErrors.each { log.error it }
                    }
                }
            }
            println "=========================================================================="
        }
    }

    List listTraits(def params){
        println "params"+params
        def sql =  Sql.newInstance(dataSource);
        def query = sql.rows("select trait_taxonomy_definition_id from trait_taxonomy_definition where taxonomy_definition_id=:taxonId",[taxonId:params.taxon?.toLong()]);
        println "================="+query
        List<Trait> traitList = []
        for (row in query) {
            traitList.add(Trait.findById(row.getProperty("trait_taxonomy_definition_id")))
        }
        println "=================="+traitList.id
        //def traitList=Trait.findAllByTaxonomyDefinition(TaxonomyDefinition.get(params.taxon?.toLong()))
        //def traitList=TaxonomyDefinition.findById(params.taxon?.toLong())
        //println "+++++++++++++++++"+traitList.traitTaxonomyDefinition

        return traitList
    }

    Map showTrait(Long id) {
        Trait trait = Trait.findById(id)
        TaxonomyDefinition coverage = trait.taxonomyDefinition
        def taxons = [:];
        Field field;
        taxons = TraitFacts.findAllByTrait(trait);
        field = Field.findById(trait.fieldId);
        println "field"+field.concept
        println "taxons"+taxons.taxon.name
        println "coverage"+coverage.name
        return [trait:trait, coverage:coverage.name, species:taxons.taxon.name, field:field.concept];
    }

}


