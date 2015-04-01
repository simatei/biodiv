<style type="text/css">

.tab_accept{
    border: 1px solid #ccc;
}
.tab_accept_inner{
    color:white;
    padding-top:3px;
    border-right: 1px solid #ccc;
    text-align: center;
}
.tab_div{
    border-bottom: 1px solid #ccc;
    padding: 5px 0px 0px 0px;
    height: 27px;
}
.tab_select{
  height: 22px;
  padding: 0px;
}
</style>

<div class="row-fluid tab_accept" style="border: 1px solid #ccc;">
  <div class="span3 tab_accept_inner"><label>Name</label></div>
  <div class="span3 tab_accept_inner"><label>Source</label></div>
  <div class="span4 tab_accept_inner"><label>Contributor</label></div>
  <div class="span2 tab_accept_inner"><label>Action</label></div>
</div>
<g:set var="typeClass" value="${type}id"/>
<g:each in="${1..4}">
<div class="row-fluid tab_div singleRow">
 <span class="tab_form">
  <div class="span3"> 
      <input type="hidden" class = "${typeClass}" name="${typeClass}" value=""/>
            <input type="text" class="nameInputs span12" name="value">
  </div>
  <div class="span3"><input type="text" class="nameInputs span12" name="source"></div>
  <div class="span4"><input type="text" class="nameInputs span12" name="contributor"></div>
  <div class="span2">
    <button class="btn btn-mini btn-primary addEdit" onClick='modifyContent(this, "${type}");' rel="add"><i class="icon-ok icon-white"></i></button>
    <button class="btn btn-mini delete" onClick='modifyContent(this, "${type}");' rel='delete'><i class="icon-remove"></i></button>
  </div>
 </span> 
</div>
</g:each>


<button class="btn btn-success btn-mini add_new_row">Add Row</button>
