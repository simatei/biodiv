import species.namelist.Utils;
import species.sourcehandler.MappedSpreadsheetConverter;
import species.namelist.NameInfo
import species.participation.*;
import species.auth.*;
import species.*;
import species.participation.SpeciesBulkUpload.Status
import groovy.sql.Sql

def uploadGbifNames(newJob=true){
	def suc = ctx.getBean("speciesUploadService");
	def startTime = new Date()

	
	if(newJob){
		def fileList = ['g1.xlsx']
		fileList.each {speciesDataFile ->
			try{
				speciesDataFile = new File("/home/sandeept/Desktop/gbif/upload/" + speciesDataFile)
				println "---------------- running ---- " + speciesDataFile
				
				if(!suc.validateUserSheetForName(speciesDataFile)){
					println  'Name validation failed !!!' 
					return
				}
				
				def sBulkUploadEntry =  SpeciesBulkUpload.create(SUser.read(1), new Date(), null, speciesDataFile.getAbsolutePath(), null, null, 'namesUpload')
				
				println "bulk entry =================== " + sBulkUploadEntry
			}catch(e){
				e.printStackTrace()
			}
			
		}
	}else{
		SpeciesBulkUpload dl = SpeciesBulkUpload.get(111)
		try{
			//dl.updateStatus(Status.SCHEDULED)
			println '------------------------------- starting -------------'
			suc.upload(dl)
		}catch (Exception e) {
			println " Error while running task $dl"
			e.printStackTrace()
			dl.updateStatus(Status.FAILED)
		}
	}
	
	def endDate = new Date()
	println "Start time " + startTime + "    end time " + endDate
}


def gbifNamesReport(){
	def startTime = new Date()
	def fileList = ['gbif_names_20001_25000.xlsx']
	fileList.each {speciesDataFile ->
		try{
			speciesDataFile = "/home/sandeept/Desktop/gbif/regbifnames/" + speciesDataFile
			println "---------------- running ---- " + speciesDataFile
			List nameInfoList = MappedSpreadsheetConverter.getNames(speciesDataFile, speciesDataFile)
			println "------------------ name info list " + nameInfoList
			File f = NameInfo.writeNamesMapperSheet(nameInfoList, new File(speciesDataFile))
			println "final f =================== " + f
		}catch(e){
			e.printStackTrace()
		}
	}
	
	def endDate = new Date()
	println "Start time " + startTime + "    end time " + endDate
}


def nameParse(){
	int count = 0
	NamesParser namesParser = new NamesParser();
	def rowList = []
	new File("/tmp/names.csv").splitEachLine("\\t") {
		if(!it)
			return
		
		count++
		if(count == 1)
			return
		
		if(count%100 == 0)
			println "----------------- count " + count
					
		def fields = it;
		def id = fields[0].trim()
		def name = fields[1].trim()
		def parsedName = namesParser.parse([name])
		parsedName = parsedName? parsedName[0].canonicalForm : 'Not Parsed'
		def tmp = [id, name, parsedName]
		rowList << tmp
		println '------------ ' + parsedName
	}
	
	new File("/tmp/names_1.csv").withWriter { out ->
		rowList.each {
		  out.println it.join("|")
		}
	}
}
	


def runReportGeneration() {
	println "=========START========= " + new Date()
	//Utils.generateColStats("/apps/git/biodiv/col_8May")
	Utils.downloadColXml("/apps/git/biodiv/col_8May/June4");
	//Utils.testObv()
	println "=========END========= " + new Date()
}


def addFieldForGbif(){
	String concept = 'Nomenclature and Classification'
	String category = "GBIF Taxonomy Hierarchy"
	def language =  Language.findByNameIlike(Language.DEFAULT_LANGUAGE)
	
	def ll = ['Kingdom', 'Phylum', 'Class', 'Order', 'Family', 'Sub-Family', 'Genus', 'Sub-Genus', 'Species', 'Infraspecies']
	
	int ii = 90
	ll.each { subCategory ->
		def f = new Field(language: language, concept:concept, category:category, subCategory:subCategory, description:'Place holder for ' + subCategory, displayOrder:ii, connection:ii)
		f.save(flush:true)
		ii++
	}
}



def getAccepteNameId(String id){
	if(!id)
		return
	
	try{		
		id = Long.parseLong(id)
		TaxonomyDefinition td = TaxonomyDefinition.read(id)
		if(!td){
			return
		}
		
		if("accepted".equalsIgnoreCase(td.status.value())){
			return id
		}
		
		def accList = AcceptedSynonym.fetchAcceptedNames(td)
		//println "synonym id " + id + "   accList " + accList
		if(accList.size() == 1){
			return accList[0].id
		}
	}catch(e){
		println e.message
	}
		
}

def addAcceptedTaxonId(){
	def ds = ctx.getBean("dataSource");
	ds.setUnreturnedConnectionTimeout(500);
	int count = 0
	NamesParser namesParser = new NamesParser();
	def rowList = []
	new File("/tmp/t1.csv").splitEachLine("\\t") {
		if(!it)
			return
		
		count++
		if(count == 1)
			return
		
		if(count%100 == 0)
			println "----------------- count " + count
					
		def fields = it;
		def id = fields[0].trim()
		def name = fields[1].trim()
		def parsedName = fields[2].trim()
		def taxonId  = fields[3]?.trim()
		def accId = getAccepteNameId(taxonId)
		def tmp = [id, name, parsedName, taxonId, accId]
		rowList << tmp
	}
	
	new File("/tmp/t2.csv").withWriter { out ->
		rowList.each {
		  out.println it.join("|")
		}
	}
}


def addRecoAcceptedId(){
	def query = " select distinct(r.taxon_concept_id) from recommendation r, taxonomy_definition  td where r.taxon_concept_id = td.id and td.status = 'SYNONYM' and r.taxon_concept_id is not null; "

	def ds = ctx.getBean("dataSource");
	ds.setUnreturnedConnectionTimeout(500);
	int count = 0
	
	Sql conn = new Sql(ds);
	def idList = conn.rows(query);
	
	idList.each {
		def id = it.taxon_concept_id
		count++
		if(count%100 == 0)
			println "----------------- count " + count
		
		def accId = getAccepteNameId('' + id)
		accId = accId ?: "NULL"
		conn.executeUpdate("update recommendation set accepted_name_id = " + accId + " where taxon_concept_id = " + id )
	}

}

addRecoAcceptedId()
//addAcceptedTaxonId()
//addFieldForGbif()
//nameParse()
//uploadGbifNames(false)
//gbifNamesReport()
//runReportGeneration()
//nohup grails -Dgrails.env=pamba  run-script userscripts/colReport.groovy >> gbif.txt 2>&1
