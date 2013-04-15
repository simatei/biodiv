

<%@ page import="content.Project"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="main" />
<g:set var="entityName"
	value="${message(code: 'project.label', default: 'Project')}" />
<title><g:message code="default.create.label"
		args="[entityName]" /></title>
<r:require modules="add_file" />
<uploader:head />
<style type="text/css">
ul.tagit {
	margin-left: 0px;
}

.location-div {
	margin: 10px;
}

.locations-block {
	
}

textarea {
	max-width: 680px;
}
</style>
</head>
<body>
	<div class="nav">
		<span class="menuButton"><a class="home"
			href="${createLink(uri: '/')}"><g:message
					code="default.home.label" /></a></span> <span class="menuButton"><g:link
				class="list" action="list">
				<g:message code="default.list.label" args="[entityName]" />
			</g:link></span>
	</div>
	<div class="body">

		<%
                def form_action = uGroup.createLink(action:'save', controller:'project', 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
				def form_title = "Create Project"				
				def form_button_name = "Create Project"
				def form_button_val = "Create Project"
				if(params.action == 'edit' || params.action == 'update'){
					form_action = uGroup.createLink(action:'update', controller:'project', id:projectInstance.id, 'userGroup':userGroupInstance, 'userGroupWebaddress':params.webaddress)
					 form_button_name = "Update Project"
					form_button_val = "Update Project"
					form_title = "Update Project"
					
				}
			
            %>
		<h1>
			${form_title}
		</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<g:hasErrors bean="${projectInstance}">
			<div class="errors">
				<g:renderErrors bean="${projectInstance}" as="list" />
			</div>
		</g:hasErrors>

		<form action="${form_action}" method="POST" id="create-project"
			class="project-form form-horizontal" enctype="multipart/form-data">
			<div class="dialog">

				<input name="id" type="hidden" value="${projectInstance?.id}" />

				<div class="super-section">
					<div class="section">

						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'direction', 'error')}">
							<label class="control-label" for="direction"><g:message
									code="project.direction.label" default="Strategic Direction" /></label>

							<div class="controls">
								<g:select name="direction.id"
									from="${content.StrategicDirection.list()}" optionKey="id"
									value="${projectInstance?.direction?.id}" />
							</div>
						</div>

						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'title', 'error')}">
							<label class="control-label" for="title"><g:message
									code="project.title.label" default="Project Title" /><span
								class="req">*</span></label>
							<div class="controls">

								<input type="text" class="input-xxlarge" name="title"
									value="${projectInstance?.title}" required />
							</div>

						</div>

						<div
							class="row control-group ${hasErrors(bean: projectInstance, field: 'summary', 'error')}">

							<label class="control-label" for="summary"><g:message
									code="project.summary.label" default="Summary of the Project" /></label>

							<div class="controls">


								<ckeditor:config var="toolbar_editorToolbar">
									[
    									[ 'Bold', 'Italic', 'Image' ]
									]
									</ckeditor:config>
								<ckeditor:editor name="summary" height="200px"
									toolbar="editorToolbar">
									${projectInstance?.summary}
								</ckeditor:editor>
							</div>
						</div>

						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'tags', 'error')}">

							<label class="control-label" for="tags"><i
								class="icon-tags"></i> <g:message code="project.tags.label"
									default="Project Tags" /></label>

							<div class="controls">


								<ul id="tags" name="tags">
									<g:each in="${projectInstance.tags}" var="tag">
										<li>
											${tag}
										</li>
									</g:each>
								</ul>
							</div>
						</div>
					</div>
				</div>

				<div class="super-section">
					<a data-toggle="collapse" href="#locationsDiv">
						<h5>Location</h5>
					</a>
					<div id="locationsDiv" class="section in collapse">
						<g:render template="locations"
							model="['projectInstance':projectInstance]" />

					</div>

				</div>

				<div class="super-section">

					<a data-toggle="collapse" href="#granteeDetails">
						<h5>Grantee Details</h5>
					</a>
					<div id="granteeDetails" class="section in collapse">
						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'granteeOrganization', 'error')}">
							<label class="control-label" for="granteeOrganization"><g:message
									code="project.granteeOrganization.label"
									default="Grantee Organization" /></label>

							<div class="controls">

								<g:textField name="granteeOrganization"
									value="${projectInstance?.granteeOrganization}" />
							</div>
						</div>


						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'granteeContact', 'error')}">
							<label class="control-label" for="granteeContact"><g:message
									code="project.granteeContact.label" default="Primary Contact" /></label>

							<div class="controls">

								<g:textField name="granteeContact"
									value="${projectInstance?.granteeContact}" />
							</div>
						</div>

						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'granteeEmail', 'error')}">
							<label class="control-label" for="granteeEmail"><g:message
									code="project.granteeEmail.label" default="Email" /></label>

							<div class="controls">
								<div class="input-prepend">
									<span class="add-on"><i class="icon-envelope"></i></span>

									<g:textField name="granteeEmail"
										value="${projectInstance?.granteeEmail}" />
								</div>
							</div>
						</div>

						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'granteeLogo', 'error')}">

							<label class="control-label" for="granteeLogo"><g:message
									code="project.granteeLogo.label" default="GranteeLogo" /></label>

							<div class="controls">

								<g:render template='/UFile/imgUpload'
									model="['name': 'granteeLogo', 'path': projectInstance?.granteeLogo]" />

							</div>
						</div>
					</div>
				</div>




				<div class="super-section">

					<a data-toggle="collapse" href="#projectDetails">
						<h5>Project Details</h5>
					</a>
					<div id="projectDetails" class="section in collapse">
						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'granteeFrom', 'error')}">

							<label class="control-label" for="grantFrom"><g:message
									code="project.grantFrom.label" default="Grant From" /></label>
							<div class="controls">
								<div class="input-prepend">
									<span class="add-on"><i class="icon-calendar"></i></span> <input
										name="grantFrom" type="text" id="grantFrom" class="date-popup"
										value="${projectInstance?.grantFrom?.format('dd/MM/yyyy')}"
										placeholder="Select date" />
								</div>
							</div>
						</div>

						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'granteeTo', 'error')}">
							<label class="control-label" for="grantTo"><g:message
									code="project.grantTo.label" default="Grant To" /></label>
							<div class="controls">
								<div class="input-prepend">
									<span class="add-on"><i class="icon-calendar"></i></span> <input
										name="grantTo" type="text" id="grantTo" class="date-popup"
										value="${projectInstance?.grantTo?.format('dd/MM/yyyy')}"
										placeholder="Select date" />
								</div>
							</div>
						</div>


						<div
							class="control-group ${hasErrors(bean: projectInstance, field: 'granteeAmount', 'error')}">

							<label class="control-label" for="grantedAmount"><g:message
									code="project.grantedAmount.label" default="Granted Amount" /></label>
							<div class="controls">
								<div class="input-prepend input-append">
									<span class="add-on">$</span>

									<g:textField name="grantedAmount"
										value="${fieldValue(bean: projectInstance, field: 'grantedAmount')}" />
									<span class="add-on">.00</span>

								</div>
							</div>

						</div>
					</div>

				</div>
			</div>

			<div class="super-section">

				<a data-toggle="collapse" href="#projectProposalSec">
					<h5>Project Proposal</h5>
				</a>
				<div id="projectProposalSec" class="section">

					<div
						class="control-group ${hasErrors(bean: projectInstance, field: 'projectProposal', 'error')}">

						<label class="control-label" for="projectProposal"><g:message
								code="project.projectProposal.label" default="Project Proposal" /></label>

						<div class="controls">

							<ckeditor:config var="toolbar_editorToolbar">
									[
    									[ 'Bold', 'Italic' ]
									]
									</ckeditor:config>
							<ckeditor:editor name="projectProposal" height="200px"
								toolbar="editorToolbar">
								${projectInstance?.projectProposal}
							</ckeditor:editor>


						</div>
					</div>


					<div
						class="control-group ${hasErrors(bean: projectInstance, field: 'proposalFiles', 'error')}">

						<label class="control-label" for="proposalFiles"><g:message
								code="project.proposalFiles.label"
								default="Project Proposal Files" /></label>

						<div class="controls">

							<fileManager:uploader
								model="['name':'proposalFiles', 'uFiles':projectInstance?.proposalFiles, 'sourceHolder': projectInstance]" />
						</div>
					</div>
				</div>

			</div>

			<div class="super-section">

				<a data-toggle="collapse" href="#projectReportSec">
					<h5>Project Report</h5>
				</a>
				<div id="projectReportSec" class="section in collapse">
					<div
						class="control-group ${hasErrors(bean: projectInstance, field: 'projectReport', 'error')}">

						<label class="control-label" for="projectReport"><g:message
								code="project.projectReport.label" default="Project Report" /></label>
						<div class="controls">
							<ckeditor:config var="toolbar_editorToolbar">
									[
    									[ 'Bold', 'Italic' ]
									]
									</ckeditor:config>
							<ckeditor:editor name="projectReport" height="200px"
								toolbar="editorToolbar">
								${projectInstance?.projectReport}
							</ckeditor:editor>

						</div>

					</div>

					<div
						class="control-group ${hasErrors(bean: projectInstance, field: 'reportFiles', 'error')}">

						<label class="control-label" for="reportFiles"><g:message
								code="project.reportFiles.label" default="Project Report Files" /></label>
						<div class=" controls file-upload">
							<%
											def canUploadFile = true //customsecurity.hasPermissionAsPerGroups([permission:org.springframework.security.acls.domain.BasePermission.WRITE]).toBoolean()
									%>

							<fileManager:uploader
								model="['name':'reportFiles', 'uFiles':projectInstance?.reportFiles, 'sourceHolder': projectInstance]" />
						</div>
					</div>
				</div>
			</div>


			<div class="super-section">
				<a data-toggle="collapse" href="#data-links">
					<h5>Data Contribution</h5>
				</a>
				<div id="data-links" class="section in collapse">
					<g:render template="dataLinks"
						model="['projectInstance':projectInstance]" />

				</div>

			</div>


			<div class="super-section">
				<a data-toggle="collapse" href="#miscSec">
					<h5>Miscelleneous</h5>
				</a>
				<div id="miscSec" class="section in collapse">
					<div
						class="control-group ${hasErrors(bean: projectInstance, field: 'misc', 'error')}">
						<label class="control-label" for="misc"><g:message
								code="project.misc.label" default="Miscelleneous" /></label>
						<div class="controls">


							<ckeditor:config var="toolbar_editorToolbar">
									[
    									[ 'Bold', 'Italic' ]
									]
									</ckeditor:config>
							<ckeditor:editor name="misc" height="200px"
								toolbar="editorToolbar">
								${projectInstance?.misc}
							</ckeditor:editor>



						</div>
					</div>

					<div
						class="control-group ${hasErrors(bean: projectInstance, field: 'miscFiles', 'error')}">

						<label class="control-label" for=miscFiles"><g:message
								code="project.miscFiles.label" default="Miscelleneous Files" /></label>
						<div class="controls file-upload">


							<fileManager:uploader
								model="['name':'miscFiles', 'uFiles':projectInstance?.miscFiles, 'sourceHolder': projectInstance]" />
						</div>


					</div>
				</div>
			</div>
	</div>
	</div>



	<div class="form-actions">


		<g:if test="${projectInstance?.id}">
			<a
				href="${uGroup.createLink(controller:'project', action:'show', id:projectInstance.id)}"
				class="btn btn-danger" style="float: right; margin-right: 30px;">
				Cancel </a>
		</g:if>
		<g:else>
			<a href="${uGroup.createLink(controller:'project', action:'list')}"
				class="btn btn-danger" style="float: right; margin-right: 30px;">
				Cancel </a>
		</g:else>

		<button type="submit" class="btn btn-primary"
			style="float: right; margin-right: 30px;">
			${form_button_name}
		</button>
	</div>
	</form>


	</div>
	<g:render template='location' model="['i':'_clone','hidden':true]" />
	<g:render template='dataLink' model="['i':'_clone','hidden':true]" />



	<r:script>
        $(document).ready(function(){ 
             $("#tags").tagit({
        	select:true, 
        	allowSpaces:true, 
        	placeholderText:'Add some tags',
        	fieldName: 'tags', 
        	autocomplete:{
        		source: '/project/tags'
        	}, 
        	triggerKeys:['enter', 'comma', 'tab'], 
        	maxLength:30
        });
		$(".tagit-hiddenSelect").css('display','none');
		

		
        });
        
        $( ".date-popup" ).datepicker({ 
			changeMonth: true,
			changeYear: true,
			dateFormat: 'dd/mm/yy' 
	});


        </r:script>


</body>
</html>
