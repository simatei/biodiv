<div class="span12 super-section" style="clear: both; margin-left:0px;">

	<div class="section"
		style="position: relative; overflow: visible; clear: both;">

		<h3><g:message code="document.cov.taxonomic.coverage" /> </h3>



		<label class="control-label"><g:message code="default.species.habitats.label" /> </label>

		<div class="filters controls textbox" style="position: relative;">
			<obv:showGroupFilter
				model="['observationInstance':observationInstance, 'hideAdvSearchBar':true]" />
		</div>
	</div>
	<hr>
	<obv:showMapInput model="[observationInstance:coverageInstance, obvInfoFeeder:null, locationHeading:'Geographical Coverage', 'sourceInstance':sourceInstance, webaddress:params.webaddress]"></obv:showMapInput>
</div>

<asset:script>
$(document).ready(function() {
	intializesSpeciesHabitatInterest();
	
});
</asset:script>

