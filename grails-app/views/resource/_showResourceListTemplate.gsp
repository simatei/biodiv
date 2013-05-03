<%@page import="species.utils.Utils"%>
<%@page import="species.Resource.ResourceType"%>
<%@page import="species.auth.SUser"%>
<%@ page import="species.participation.Observation"%>
<%@ page import="species.groups.SpeciesGroup"%>
<%@ page import="species.Habitat"%>


<div class="observations_list observation" style="clear: both;">
    <div class="mainContentList">
        <div class="mainContent" name="p${params?.offset}">

            <%
            def observationPos = (queryParams.offset != null) ? queryParams.offset : params?.offset
            %>
            <ul class="grid_view thumbnails">

                <g:each in="${resourceInstanceList}" status="i"
                var="resourceInstance">

                <g:if test="${i%4 == 0}">
                <li class="thumbnail" style="clear: both;margin-left:0px;${!inGroupMap || inGroupMap[resourceInstance.id]?'':'background-color:transparent;'}">
                </g:if>
                <g:else>
                <li class="thumbnail" style="${!inGroupMap || inGroupMap[resourceInstance.id]?'':'background-color:transparent;'}">
                </g:else>
                <%def path = resourceInstance?resourceInstance.thumbnailUrl(): null;
                def fullUrl =  resourceInstance?resourceInstance.url: null;

                def imagePath, fullImagePath;
                if(resourceInstance.type == ResourceType.IMAGE) {
                    imagePath = g.createLinkTo(base:grailsApplication.config.speciesPortal.observations.serverURL,	file: path)
                    fullImagePath = g.createLinkTo(base:grailsApplication.config.speciesPortal.observations.serverURL,	file: resourceInstance.fileName)
                } else if(resourceInstance.type == ResourceType.VIDEO){
                    imagePath = g.createLinkTo(base:path,	file: '')
                    fullImagePath = g.createLinkTo(base:fullUrl,	file: '')
                }
                %>
                <div class="snippet tablet">

                    <div class="figure" style="height:150px;">
                        <g:if
                        test="${imagePath}">
                        <g:link url="${fullImagePath}" rel="prettyPhoto[gallery]">
                        <img class="img-polaroid"
                        src="${imagePath}" />
                        </g:link>
                        </g:if>
                    </div>
                    <div class="caption" >
                        <div class="rating pull-right">
                                <form class="ratingForm" method="get" title="Rate it"
                                    action="${uGroup.createLink(controller:'resource', action:'rate', id:resourceInstance.id) }"
                                    class="rating-container">
                                    <obv:rating model="['selected':resourceInstance.rating]"/>
                                </form>
                        </div>
                    </div>
                </div>
                </li>

                </g:each>
            </ul>
        </div>
    </div>

    <g:if test="${instanceTotal > (queryParams.max?:0)}">
    <div class="centered">
        <div class="btn loadMore">
            <span class="progress" style="display: none;">Loading ... </span> <span
                class="buttonTitle">Load more</span>
        </div>
    </div>
    </g:if>

    <%
    activeFilters?.loadMore = true
    activeFilters?.webaddress = userGroup?.webaddress
    %>

    <div class="paginateButtons" style="visibility: hidden; clear: both">
        <p:paginate total="${instanceTotal?:0}" action="${params.action}" controller="${params.controller?:'resource'}"
        userGroup="${userGroup}" userGroupWebaddress="${userGroupWebaddress}"
        max="${queryParams.max}" params="${activeFilters}" />
    </div>


</div>

