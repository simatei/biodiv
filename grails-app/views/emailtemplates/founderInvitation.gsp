<%@page import="species.utils.Utils"%>
<%@ page contentType="text/html"%>

Hai ${founder.name.capitalize()},
<br/><br/><br/>
<g:link controller="SUser" action="show" id="${fromUser.id }" base="${species.utils.Utils.getDomainServerUrl(request)}">${fromUser.name.capitalize()}</g:link> is inviting you to be a founder for the group <g:link controller="userGroup" action="show" id="${userGroupInstance.id }" base="${species.utils.Utils.getDomainServerUrl(request)}">${userGroupInstance.name}</g:link> on <b>domain</b>.
<br/><br/> 

Please <a href="${uri}" title="Confirmation code">click here</a> to confirm the invitation.
<br/><br/><br/>
Thank you,<br/>
The Portal Team