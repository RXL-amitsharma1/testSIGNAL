<%@ page import="grails.util.Holders; com.rxlogix.Constants; com.rxlogix.enums.ReportFormat" %>
<div class="panel-heading pv-sec-heading">
  <div class="row">
    <div class="col-md-4"></div>
    <div class="col-md-8 grid_margin">
      <div class="pos-rel pull-right filter-box-agg ">
        <g:if test="${!isArchived}">
          <div class="pos-ab1 form-inline filter-box">
            <div class="form-group  mtp-25">
              <label id="filter-label pull-left">Filter</label>
              <select id="advanced-filter" class="form-control advanced-filter-dropdown"></select>
              <a href="#" class="edit-filter pv-ic ${buttonClass == "hidden"?"invisible":""}" title="Edit Filter" tabindex="0" id="editAdvancedFilter"><i
                      class="mdi mdi-pencil font-24 "></i></a>
              <a href="#" class="add-filter pv-ic ${buttonClass}" title="Add Filter" tabindex="0" id="addAdvancedFilter"><i
                      class="mdi  mdi-plus font-24 "></i></a>
            </div>
          </div>
        </g:if>
      <!------------------=================--------------pinned icon code started-------------================----------------------->
        <span style="padding-top:20px">
          <!------------==================-------Field selection code start------==========------------------->
          <a href="javascript:void(0)" class="grid-pin collapse theme-color" id="ic-configureQuantitativeFields" data-fieldconfigurationbarid="quantitativeFields" data-pagetype="quantitative_alert" title="${message(code: 'app.label.choosefields')}">
            <i class="mdi mdi-settings-outline font-24"></i>
          </a>
          <!------------==================---------------Field selection code end----------==========------------------->

          <!------------==================--------------export to code start---------------==========------------------->
          <span class="grid-pin collapse theme-color dropdown" id="exportTypes">
            <span  tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip dropbtn" data-toggle="dropdown" accesskey="s" title="${message(code: 'app.label.exportTo')}">
              <i class="mdi mdi-export font-24"></i>
              <span class="caret hidden"></span>
            </span>
            <ul class="dropdown-menu save-list dropdown-content dropdown-menu-right ddm-padding">
              <span class="dropdown">
                <strong class="font-12 title-spacing">Export</strong>
                <g:if test="${isLatest && appType == Constants.AlertConfigType.SINGLE_CASE_ALERT}">
                  <li><g:link controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                              params="${[outputFormat: ReportFormat.XLSX,id : id, alertName : domainName,
                                         callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false,cumulativeExport: true]}">
                    <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16" /><g:message code="cumulative.export" />
                  </g:link></li>
                </g:if>
                <li><g:link controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                            params="${[outputFormat: ReportFormat.DOCX,id : id, alertName : domainName,
                                       callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false,isArchived: isArchived]}">
                  <img src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16" /><g:message code="save.as.word" />
                </g:link>
                </li>
                <li><g:link controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                            params="${[outputFormat: ReportFormat.XLSX,id : id, alertName : domainName,
                                       callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false,isArchived: isArchived]}">
                  <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16" /><g:message code="save.as.excel" />
                </g:link></li>
                <li><g:link controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                            params="${[outputFormat: ReportFormat.PDF,id : id, alertName : domainName,
                                       callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false,isArchived: isArchived]}">
                  <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16" /><g:message code="save.as.pdf" />
                </g:link></li>
                <g:set var="userService" bean="userService"/>
                <g:if test="${callingScreen != Constants.Commons.DASHBOARD && callingScreen != Constants.Commons.TRIGGERED_ALERTS}">
                  <STRONG class="font-12 title-spacing">Detection Summary</STRONG>
                  <li>
                    <a target="_blank"
                       href="exportSignalSummaryReport?outputFormat=DOCX&id=${executedConfigId}&cumulative=${cumulative}"><img
                            src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16"/><g:message
                            code="save.as.word"/></a>
                  </li>
                  <li>
                    <a target="_blank"
                       href="exportSignalSummaryReport?outputFormat=XLSX&id=${executedConfigId}&cumulative=${cumulative}"><img
                            src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16"/><g:message
                            code="save.as.excel"/></a>
                  </li>
                  <li>
                    <a target="_blank"
                       href="exportSignalSummaryReport?outputFormat=PDF&id=${executedConfigId}&cumulative=${cumulative}"><img
                            src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16"/><g:message
                            code="save.as.pdf"/></a>
                  </li>
                </g:if>
              </span>
            </ul>
          </span>
        <!------------==================--------------export to code end---------------==========------------------->

        <!------------==================-------Save view code start------==========------------------->
          <g:if test="${callingScreen == Constants.Commons.REVIEW || callingScreen == Constants.Commons.DASHBOARD}">
            <span class="grid-pin collapse theme-color dropdown ${buttonClass}" id="saveViewTypes1">
              <span  tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip dropbtn" data-toggle="dropdown" accesskey="s" title="${message(code: 'app.label.saveview')}">
                <i class="mdi mdi-star-outline font-24"></i>
                <span class="caret hidden"></span>
              </span>
              <ul class="dropdown-menu save-list dropdown-content dropdown-menu-right ddm-padding">
                <li><a href="#" tabindex="0" class="updateView ps5"><span><g:message code="default.button.save.label"/></span></a></li>
                <li><a href="#" tabindex="0" class="saveView ps5"><span><g:message code="app.label.saveAs"/></span></a></li>
                <li><a href="#" tabindex="0"  class="editView ps5"><span><g:message code="default.button.edit.label"/></span></a></li>
              </ul>
            </span>
          </g:if>
        <!------------==================----------Save view code end-------==========------------------->

        <!------------==================-------Disposition code start------==========------------------->
          <g:if test="${(callingScreen == Constants.Commons.REVIEW || callingScreen == Constants.Commons.DASHBOARD)}">
            <span class="grid-pin collapse theme-color dropdown" id="dispositionTypes2">
              <span  tabindex="0" class="dropdown-toggle grid-menu-tooltip dropbtn" data-toggle="dropdown" accesskey="s" title="${message(code: 'app.label.disposition')}">
                <i class="mdi mdi-checkbox-marked-outline font-24"></i>
              </span>
              <ul class="dropdown-menu dropdown-content dropdown-menu-right export-type-list disposition-ico disposition-li" id="dispositionTypes"></ul>
            </span>
          </g:if>
        <!------------==================-------Disposition code end------==========-------------------->

        <!------------==================-------Filter code start------==========------------------->
          <a href="javascript:void(0)" class="grid-pin collapse theme-color" id="ic-toggle-column-filters" data-fieldconfigurationbarid="quantitativeFields" data-pagetype="quantitative_alert" title="${message(code: 'app.label.filter')}">
            <i class="mdi mdi-filter-outline font-24"></i>
          </a>
          <!------------==================-------Filter code end--------==========------------------->

          <!------------==================-------Alert level disposition code start------==========------------------->
         <g:if test="${alertDispositionList && callingScreen != Constants.Commons.DASHBOARD}">
          <span class="grid-pin collapse theme-color dropdown ${buttonClass}" id="ic-alert-level-disposition">
              <span tabindex="0" class="dropdown-toggle grid-menu-tooltip" data-toggle="dropdown" accesskey="l" title="${message(code: 'alert.level.disposition.label')}">
                <i class="mdi mdi-alpha-d-box-outline font-24"></i>
              </span>
            <ul class="dropdown-menu dropdown-content col-min-150 dropdown-menu-right ul-ddm-child alert-disp-dmm">
              <g:render template="/includes/popover/bulkDispositionSelect" model="[alertDispositionList: alertDispositionList]"/>
            </ul>
          </span>
         </g:if>
          <!---------------===============--------Alert level disposition code closed---------==========-------------->

          <!------------==================-------Generated report code start------==========------------------->
          <span class="grid-pin collapse theme-color dropdown" id="ic-generated-report">
            <g:if test="${reportUrl && !onlyShareAccess}">
              <a tabindex="0" target="_blank" href="${reportUrl}">
                <i data-title="Generated Report" class="mdi mdi-table grid-menu-tooltip font-24" accesskey="g"></i>
              </a>
            </g:if>
          </span>
          <!------------==================-------Generated report code end------==========------------------->

          <!------------==================-------Data analysis code start------==========------------------->
          <g:if test="${Holders.config.signal.spotfire.enabled  &&  !onlyShareAccess && callingScreen != Constants.Commons.DASHBOARD && (isPVAEnabled || isVaersAvailable || isFaersAvailable)}">
          <span class="grid-pin collapse theme-color dropdown" id="ic-data-analysis">
            <span  tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip"  data-toggle="dropdown" accesskey="s" title="${message(code: 'app.label.dataAnalysis')}">
              <i class="mdi mdi-chart-bar font-24"></i>
            </span>
            <ul class="dropdown-menu dropdown-content dropdown-menu-right ul-ddm-child data-analysis-button">
              <g:each var="entry" in="${analysisStatus}">
                <g:if test="${entry.key == 'PR_DATE_RANGE' && selectedDatasource.contains("pva")}">
                  <g:if test="${entry.value.status == 2}">
                    <li><a href="${entry.value.url}" data-status=${entry.value.status}
                    target="_blank"><span class="hyperLinkColor">Current Period Analysis</span>
                    </a></li>
                  </g:if>
                  <g:else>
                    <li class="${buttonClass}"><a href="javascript:void(0)"
                           class="generateCurrentAnalysis" data-id=${executedConfigId} data-status=${entry.value.status}><span>${entry.value.message}</span>
                    </a></li>
                  </g:else>

                </g:if>
                <g:elseif test="${entry.key == 'CUMULATIVE' && selectedDatasource.contains("pva")}">
                  <g:if test="${entry.value.status == 2}">
                    <li><a href="${entry.value.url}" data-status=${entry.value.status}
                    target="_blank"><span class="hyperLinkColor">Cumulative Period Analysis</span>
                    </a></li>
                  </g:if>
                  <g:else>
                    <li class="${buttonClass}"><a href="javascript:void(0)"
                           class="generateCumulativeAnalysis" data-id=${executedConfigId} data-status=${entry.value.status}><span>${entry.value.message}</span>
                    </a></li>
                  </g:else>
                </g:elseif>
                <g:elseif test="${entry.key == 'PR_DATE_RANGE_FAERS' && selectedDatasource.contains("faers")}">
                  <g:if test="${entry.value.status == 2}">
                    <li><a href="${entry.value.url}" data-status=${entry.value.status}
                    target="_blank"><span class="hyperLinkColor">Current Period Analysis (FAERS)</span>
                    </a></li>
                  </g:if>
                  <g:else>
                    <li class="${buttonClass}"><a href="javascript:void(0)"
                           class="generateCurrentAnalysisFaers" data-id=${executedConfigId} data-status=${entry.value.status}><span>${entry.value.message}</span>
                    </a></li>
                  </g:else>
                </g:elseif>
                <g:elseif test="${entry.key == 'CUMULATIVE_FAERS' && selectedDatasource.contains("faers")}">
                  <g:if test="${entry.value.status == 2}">
                    <li><a href="${entry.value.url}" data-status=${entry.value.status}
                    target="_blank"><span class="hyperLinkColor">Cumulative Period Analysis (FAERS)</span>
                    </a></li>
                  </g:if>
                  <g:else>
                    <li class="${buttonClass}"><a href="javascript:void(0)"
                           class="generateCumulativeAnalysisFaers" data-id=${executedConfigId} data-status=${entry.value.status}><span>${entry.value.message}</span>
                    </a></li>
                  </g:else>
                </g:elseif>

                <g:elseif test="${entry.key == 'PR_DATE_RANGE_VAERS' && selectedDatasource.contains("vaers")}">
                  <g:if test="${entry.value.status == 2}">
                    <li><a href="${entry.value.url}" data-status=${entry.value.status}
                    target="_blank"><span class="hyperLinkColor">Current Period Analysis (VAERS)</span>
                    </a></li>
                  </g:if>
                  <g:else>
                    <li class="${buttonClass}"><a href="javascript:void(0)"
                                                  class="generateCurrentAnalysisVaers" data-id=${executedConfigId} data-status=${entry.value.status}><span>${entry.value.message}</span>
                    </a></li>
                  </g:else>
                </g:elseif>
                <g:elseif test="${entry.key == 'CUMULATIVE_VAERS' && selectedDatasource.contains("vaers")}">
                  <g:if test="${entry.value.status == 2}">
                    <li><a href="${entry.value.url}" data-status=${entry.value.status}
                    target="_blank"><span class="hyperLinkColor">Cumulative Period Analysis (VAERS)</span>
                    </a></li>
                  </g:if>
                  <g:else>
                    <li class="${buttonClass}"><a href="javascript:void(0)"
                                                  class="generateCumulativeAnalysisVaers" data-id=${executedConfigId} data-status=${entry.value.status}><span>${entry.value.message}</span>
                    </a></li>
                  </g:else>
                </g:elseif>

              </g:each>

            </ul>
          </span>
          </g:if>
          <!------------==================-------Data analysis code end------------------==========------------------->
        </span>

      <!------------------=================--------------pinned icon code closed--------------================----------------------->

      <!-----------==========------second filter code---------========------------->
        <g:if test="${callingScreen == Constants.Commons.REVIEW || callingScreen == Constants.Commons.DASHBOARD}">
          <div class="pos-ab form-inline search-box" >
            <div class="form-group  mtp-25" style="display: none;">
              <label><g:message code="app.label.view.instance.select.view"/></label>
              <select class="form-control viewSelect" accesskey="j" style="display: none">
                <g:each in="${viewsList}" var="view">
                  <g:if test="${viewId == view.id}">
                    <option value="${view.id}" selected="selected">${view.name}${view.defaultView}</option>
                  </g:if>
                  <g:else>
                    <option value="${view.id}">${view.name}${view.defaultView}</option>
                  </g:else>
                </g:each>
              </select>
              <g:if test="${!isArchived}">
                <a href="#" class="editView m-l-5 pv-ic" title="Edit view" tabindex="0"><i
                        class="mdi mdi-magnify"></i></a>
              </g:if>
            </div>
          </div>
        </g:if>

        <span class="ico-menu pull-right">
          <!------------------------------------------------------------list menu code start----------------------------------------------------------------------------->
          <span class="dropdown grid-icon" id="reportIconMenu">
            <span class="dropdown-toggle" data-toggle="dropdown" style="float: left;">
              <i class="mdi mdi-format-list-bulleted mr-10 font-24 pull-right mr-10" style="margin-right:5px"></i></span>
            <ul class="dropdown-menu ul-ddm">

              <li class="li-pin-width">
                <a class="test field-config-bar-toggle text-left-prop ul-ddm-hide" href="#" id="configureQuantitativeFields" data-fieldconfigurationbarid="quantitativeFields"
                   data-pagetype="quantitative_alert">
                  <i class="mdi mdi-settings-outline"></i>
                  <span tabindex="0">
                    Field Selection
                  </span>
                </a>

                <a href="javascript:void(0)" class="text-right-prop" >
                  <span class="pin-unpin-rotate pull-right mdi mdi-pin" data-id="#ic-configureQuantitativeFields" title="Pin to top"  data-toggle="collapse"  data-title="Field selection"></span>
                </a>

              </li>
            <!----------===================--------------export to menu code start------------===================----------------------->
                <li class="li-pin-width dropdown-submenu">
                  <a class="test text-left-prop" href="#">
                    <i class="mdi mdi-export"></i> <span tabindex="0" class="dropdown-toggle exportPanel grid-menu-tooltip" data-toggle="dropdown"  accesskey="x">
                    Export To
                  </span>
                  </a>
                  <a href="javascript:void(0)" class="text-right-prop" data-toggle="collapse">
                    <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-title="Export To" data-id="#exportTypes"></span>
                  </a>
                  <ul class="dropdown-menu export-type-list ul-ddm-child" id="exportTypes">
                    <strong class="font-12 title-spacing">Export</strong>
                    <g:if test="${isLatest && appType == Constants.AlertConfigType.SINGLE_CASE_ALERT}">
                      <li><g:link controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                                  params="${[outputFormat: ReportFormat.XLSX,id : id, alertName : domainName,
                                             callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false,cumulativeExport: true]}">
                        <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16" /><g:message code="cumulative.export" />
                      </g:link></li>
                    </g:if>
                    <li><g:link controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                                params="${[outputFormat: ReportFormat.DOCX,id : id, alertName : domainName,
                                           callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false, isArchived: isArchived]}">
                      <img src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16" /><g:message code="save.as.word" />
                    </g:link>
                    </li>
                    <li><g:link controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                                params="${[outputFormat: ReportFormat.XLSX,id : id, alertName : domainName,
                                           callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false, isArchived: isArchived]}">
                      <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16" /><g:message code="save.as.excel" />
                    </g:link></li>
                    <li><g:link controller="${domainName}" action="exportReport" class="m-r-30 export-report"
                                params="${[outputFormat: ReportFormat.PDF,id : id, alertName : domainName,
                                           callingScreen: callingScreen, length: '-1', start: '0', adhocRun: false, isArchived: isArchived]}">
                      <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16" /><g:message code="save.as.pdf" />
                    </g:link></li>
                    <g:set var="userService" bean="userService"/>
                    <g:if test="${callingScreen != Constants.Commons.DASHBOARD && callingScreen != Constants.Commons.TRIGGERED_ALERTS}">
                      <STRONG class="font-12 title-spacing">Detection Summary</STRONG>
                      <li>
                        <a target="_blank"
                           href="exportSignalSummaryReport?outputFormat=DOCX&id=${executedConfigId}&cumulative=${cumulative}&isArchived=${isArchived}"><img
                                src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16"/><g:message
                                code="save.as.word"/></a>
                      </li>
                      <li>
                        <a target="_blank"
                           href="exportSignalSummaryReport?outputFormat=XLSX&id=${executedConfigId}&cumulative=${cumulative}&isArchived=${isArchived}"><img
                                src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16"/><g:message
                                code="save.as.excel"/></a>
                      </li>
                      <li>
                        <a target="_blank"
                           href="exportSignalSummaryReport?outputFormat=PDF&id=${executedConfigId}&cumulative=${cumulative}&isArchived=${isArchived}"><img
                                src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16"/><g:message
                                code="save.as.pdf"/></a>
                      </li>
                    </g:if>
                  </ul>
                </li>

            <!---------------=================---------export to menu code end----------==============--------------------->

              <g:if test="${!isArchived && (callingScreen == Constants.Commons.REVIEW || callingScreen == Constants.Commons.DASHBOARD)}">
                <li class="li-pin-width dropdown-submenu ${buttonClass}">
                  <a class="test text-left-prop"  href="#">
                    <i class="mdi mdi-star-outline"></i>
                    <span tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip" data-toggle="dropdown" accesskey="s">
                      Save View
                    </span>
                  </a>
                  <a href="javascript:void(0)" class="text-right-prop" data-toggle="collapse" >
                    <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-id="#saveViewTypes1" data-title="Save View"></span>
                  </a>
                  <ul class="dropdown-menu save-list col-min-150 ul-ddm-child" id="saveViewTypes">
                    <li><a href="#" tabindex="0" class="updateView ps5"><span><g:message code="default.button.save.label"/></span></a></li>
                    <li><a href="#" tabindex="0" class="saveView ps5"><span><g:message code="app.label.saveAs"/></span></a></li>
                    <li><a href="#" tabindex="0"  class="editView ps5"><span><g:message code="default.button.edit.label"/></span></a></li>
                  </ul>
                </li>
              </g:if>

              <g:if test="${(callingScreen == Constants.Commons.REVIEW || callingScreen == Constants.Commons.DASHBOARD)}">
                <li class="dropdown-submenu li-pin-width">
                  <a class="test text-left-prop" href="#">
                    <i class="mdi mdi-checkbox-marked-outline"></i> <span class="dropdown-toggle grid-menu-tooltip" data-toggle="dropdown" tabindex="0" role="button" accesskey="x"  aria-expanded="true">
                    <g:message code="app.label.disposition" />
                  </span>
                  </a>
                  <a href="javascript:void(0)" class="text-right-prop" data-toggle="collapse" >
                    <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-id="#dispositionTypes2" data-title="Disposition"></span>
                  </a>
                  <ul class="dropdown-menu export-type-list disposition-ico" id="dispositionTypes"></ul>
                </li>
              </g:if>


              <g:if test="${!isArchived}">
                <li class="li-pin-width">
                  <a class="test text-left-prop ul-ddm-hide" id="toggle-column-filters" href="#">
                    <i class="mdi mdi-filter-outline"></i>
                    <span data-title="Filters" class="test" tabindex="0" role="button" accesskey="y">
                      Filters</span>
                  </a>
                  <a href="javascript:void(0)" class="text-right-prop" data-toggle="collapse" >
                    <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-id="#ic-toggle-column-filters" data-title="Filters"></span>
                  </a>
                </li>
              </g:if>


              <g:if test="${alertDispositionList && callingScreen != Constants.Commons.DASHBOARD}">
                <li class="dropdown-submenu li-pin-width ${buttonClass}">
                  <a tabindex="0" class="m-r-10 grid-menu-tooltip text-left-prop">
                    <i class="mdi mdi-alpha-d-box-outline" title="${message(code :'alert.level.disposition.label')}"></i>
                    <span data-target="#bulkDispositionPopover" role="button" data-toggle="modal-popover" data-placement="left" accesskey="l">
                      Alert level disposition
                    </span>
                  </a>
                  <a href="javascript:void(0)" class="text-right-prop" data-toggle="collapse" data-target="#ic-alert-level-disposition">
                    <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-id="#ic-alert-level-disposition" data-title="Alert level Disposition"></span>
                  </a>
                  <ul class="dropdown-menu col-min-150 alert-disp-dmm">
                    <g:render template="/includes/popover/bulkDispositionSelect" model="[alertDispositionList: alertDispositionList]"/>
                  </ul>
                </li>
              </g:if>

              <g:if test="${reportUrl && !onlyShareAccess}">
                <li class="dropdown-submenu li-pin-width">
                  <a tabindex="0" class="text-left-prop" target="_blank" href="${reportUrl}">
                    <i data-title="Generated Report"
                       class="mdi mdi-table grid-menu-tooltip" accesskey="g"></i>
                    <span>Generated Report</span>
                  </a>
                  <a href="javascript:void(0)" class="text-right-prop" data-toggle="collapse" >
                    <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-id="#ic-generated-report" data-title="Generated Report"></span>
                  </a>
                </li>
              </g:if>

              <g:if test="${Holders.config.signal.spotfire.enabled  &&  !onlyShareAccess && callingScreen != Constants.Commons.DASHBOARD && (isPVAEnabled || isVaersAvailable || isFaersAvailable)}">
                <li class="dropdown-submenu li-pin-width">
                  <a class="test text-left-prop" href="#">
                    <i class="mdi mdi-chart-bar"></i>
                    <span  tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip"  data-toggle="dropdown" accesskey="s">
                      Data Analysis
                    </span>
                  </a>
                  <a href="javascript:void(0)" class="text-right-prop" data-toggle="collapse" >
                    <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-id="#ic-data-analysis" data-title="Data Analysis"></span>
                  </a>
                  <ul class="dropdown-menu ul-ddm-child data-analysis-button">
                    <g:each var="entry" in="${analysisStatus}">
                      <g:if test="${entry.key == 'PR_DATE_RANGE' && selectedDatasource.contains("pva")}">
                        <g:if test="${entry.value.status == 2}">
                          <li><a href="${entry.value.url}" data-status=${entry.value.status}
                          target="_blank"><span class="hyperLinkColor">Current Period Analysis</span>
                          </a></li>
                        </g:if>
                        <g:else>
                          <li class="${buttonClass}"><a href="javascript:void(0)"
                                 class="generateCurrentAnalysis" data-id=${executedConfigId} data-status=${entry.value.status}><span>${entry.value.message}</span>
                          </a></li>
                        </g:else>

                      </g:if>
                      <g:elseif test="${entry.key == 'CUMULATIVE' && selectedDatasource.contains("pva")}">
                        <g:if test="${entry.value.status == 2}">
                          <li><a href="${entry.value.url}" data-status=${entry.value.status}
                          target="_blank"><span class="hyperLinkColor">Cumulative Period Analysis</span>
                          </a></li>
                        </g:if>
                        <g:else>
                          <li class="${buttonClass}"><a href="javascript:void(0)"
                                 class="generateCumulativeAnalysis" data-id=${executedConfigId} data-status=${entry.value.status}><span>${entry.value.message}</span>
                          </a></li>
                        </g:else>
                      </g:elseif>
                      <g:elseif test="${entry.key == 'PR_DATE_RANGE_FAERS' && selectedDatasource.contains("faers")}">
                        <g:if test="${entry.value.status == 2}">
                          <li><a href="${entry.value.url}" data-status=${entry.value.status}
                          target="_blank"><span class="hyperLinkColor">Current Period Analysis (FAERS)</span>
                          </a></li>
                        </g:if>
                        <g:else>
                          <li class="${buttonClass}"><a href="javascript:void(0)"
                                 class="generateCurrentAnalysisFaers" data-id=${executedConfigId} data-status=${entry.value.status}><span>${entry.value.message}</span>
                          </a></li>
                        </g:else>
                      </g:elseif>
                      <g:elseif test="${entry.key == 'CUMULATIVE_FAERS' && selectedDatasource.contains("faers")}">
                        <g:if test="${entry.value.status == 2}">
                          <li><a href="${entry.value.url}" data-status=${entry.value.status}
                          target="_blank"><span class="hyperLinkColor">Cumulative Period Analysis (FAERS)</span>
                          </a></li>
                        </g:if>
                        <g:else>
                          <li class="${buttonClass}"><a href="javascript:void(0)"
                                 class="generateCumulativeAnalysisFaers" data-id=${executedConfigId} data-status=${entry.value.status}><span>${entry.value.message}</span>
                          </a></li>
                        </g:else>
                      </g:elseif>

                      <g:elseif test="${entry.key == 'PR_DATE_RANGE_VAERS' && selectedDatasource.contains("vaers")}">
                        <g:if test="${entry.value.status == 2}">
                          <li><a href="${entry.value.url}" data-status=${entry.value.status}
                          target="_blank"><span class="hyperLinkColor">Current Period Analysis (VAERS)</span>
                          </a></li>
                        </g:if>
                        <g:else>
                          <li class="${buttonClass}"><a href="javascript:void(0)"
                                                        class="generateCurrentAnalysisVaers" data-id=${executedConfigId} data-status=${entry.value.status}><span>${entry.value.message}</span>
                          </a></li>
                        </g:else>
                      </g:elseif>
                      <g:elseif test="${entry.key == 'CUMULATIVE_VAERS' && selectedDatasource.contains("vaers")}">
                        <g:if test="${entry.value.status == 2}">
                          <li><a href="${entry.value.url}" data-status=${entry.value.status}
                          target="_blank"><span class="hyperLinkColor">Cumulative Period Analysis (VAERS)</span>
                          </a></li>
                        </g:if>
                        <g:else>
                          <li class="${buttonClass}"><a href="javascript:void(0)"
                                                        class="generateCumulativeAnalysisVaers" data-id=${executedConfigId} data-status=${entry.value.status}><span>${entry.value.message}</span>
                          </a></li>
                        </g:else>
                      </g:elseif>

                    </g:each>
                  </ul>
                </li>
              </g:if>


            </ul>
          </span>
          <!-------------------------------------list menu ended---------------------------------------------------------------------------------------------->
        </span>

      </div>
    </div>
    <!------------------------====-----code end---------------===========-------------->

  </div>
</div>

<div class="row">

  <div class="views-list col-md-12 bookmarkstrip">
  </div>
</div>