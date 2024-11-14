var p,e,network_img_url;

//header for activities table
var activities_table_header = ['Activity Type','Suspect Product','Event','Description','Performed By', 'Timestamp']
var url = $(location).attr('protocol')+"//"+$(location).attr('host');
//for rationale modal
rationale_table_child_dict ={}
detailed_history_table_child_dict ={}



/*
    Alert period drop-down. Uses select2 plugin
    Whenever the alert periods are selected, extract_record API in views.py is called using ajax.
*/
$(document).ready(function () {

	////console.log("Inside document ready - select alert_period ");
	// Whenever the period is selected, extract_record api is called which extracts the details related to period selected 
	$('#alert_period').on('select2:select',function(e){
		////console.log("Inside document ready - select alert_period - select event ");
		alert_id = $('#alert_period').select2('data')[0].id;
		archived = $('#alert_period').select2('data')[0].title;
		selected_period = $('#alert_period').select2('data')[0];

		p = selected_record['product_name'];
		e = selected_record['pt'];

		$("#divLoading").css("display", "block");		
		$.ajax({
			type : "GET",
			url: url+'/record/?alert_id='+alert_id+'&archived='+archived+'&p='+p+'&e='+e,
			dataType: 'json',
			success: function (result) {

				//redirects the request to login page
				if(result.login === 'true'){
					window.location=result.url
				}
    			
				selected_record = result.selected_record;
				comment = result.comment;
				confirm = result.confirm;
    			////console.log("Inside document ready - select alert_period - Calling initialize_node_table ");
    			if(typeof selected_record === "object"){
    				initialize_node_table();
    			}
    			else{
    				result_table();
    				update_period();
    				$('#network_structure').empty();
					var para = document.createElement("p");
					para.setAttribute("id","error_text");
					var node = document.createTextNode("No record found. Please select a record from Result table tab.");
					para.appendChild(node);

					document.getElementById('network_structure').appendChild(para);

					$('#intermediate_scores').hide();
					$('#score_table_div').hide();

    				document.getElementById('product_id').innerText='No record';
    				document.getElementById('event_id').innerText='No record';
    			}
    			
			},
			error :function(XMLHttpRequest, textStatus, errorThrown) { 
				alert("Error: " + errorThrown); 
			}
			,

			complete: function(){
				$("#divLoading").css("display", "none");},	
		});	
			
	});
	
});

//Uncomment this, if there is a need to select target variables from left side panel	
/*
    Dropdown for query variables - using select2 plugin
    The elements to be displayed are created in items list. Whenever an element is selected, change event is triggered
    which creates selected_nodes list and handles coloring and score display in vis.js network.
*/
/*
$(document).ready(function () {

	////console.log("Inside document ready - query variable dropdown");
	var items = [];
	for(i=0; i<non_terminal_nodes.length;i++){
		//to set default node in list
		if(selected_nodes.includes(non_terminal_nodes[i])){
			items.push({'id': non_terminal_nodes[i], 'text': node_label_dict[non_terminal_nodes[i]],'selected':"selected"})	
		}else{
			items.push({'id': non_terminal_nodes[i], 'text': node_label_dict[non_terminal_nodes[i]]})
		}
	}

	$('#query_dropdown_id').select2({
		data: items,
		width:'100%',
		placeholder:'--Select Query Variable--',
		allowClear: true,
	    multiple: true
	});


	$('#query_dropdown_id').on('change',function(e){
		//fetch all the selected nodes in dropdown
		//console.log("Inside document ready - query variable dropdown - change event");
		selected_nodes=[];
		for(var index=0;index<$('#query_dropdown_id').select2('data').length;index++){
			selected_nodes.push($('#query_dropdown_id').select2('data')[index].id);
		}

		//Initialize the labels to refresh the previous data - remove all labels
	    remove_node_labels(non_terminal_nodes);

    	//console.log("Inside document ready - query variable dropdown - change event");
    	for(j=0;j<selected_nodes.length;j++){
	    	var node_label= node_label_dict[selected_nodes[j]]
            var state_list=state_dict[selected_nodes[j]];
            //for(state_index=state_list.length-1; state_index>=0; state_index--){
            node_label=node_label+"\n"+state_label_dict[state_list[0]]+ ':' + selected_record[selected_nodes[j]][0]
            //}

	        try {
                nodes.update({
                    id: selected_nodes[j],
                    label: node_label.replace(' ','\n'),
                    color: {background:'#ffff6e'}
                });
	        }
        	catch (err) {
            	alert(err);
        	}
        }	
	});
});
*/



/* Displays score on intermediate node and highlights pec importance score by default */
function intermediate_node_update(){
	//console.log("Inside document ready - query variable dropdown - change event");

	//Initialize the labels to refresh the previous data - remove all labels
    remove_node_labels(non_terminal_nodes);

    //display score for all non_terminal nodes
    for(j=0;j<non_terminal_nodes.length;j++){
    	var node_label= node_label_dict[non_terminal_nodes[j]]
        var state_list=state_dict[non_terminal_nodes[j]];
        //for(state_index=state_list.length-1; state_index>=0; state_index--){
        node_label=node_label+"\n"+state_label_dict[state_list[0]]+ ':' + selected_record[non_terminal_nodes[j]][0]
        //}

        try {
            nodes.update({
                id: non_terminal_nodes[j],
                label: node_label.replace(' ','\n'),
            });
        }
    	catch (err) {
        	alert(err);
    	}
    }

	//console.log("Inside document ready - query variable dropdown - change event");
	//selected nodes is pec_importance by default. Add nodes in order to highlight
	for(j=0;j<selected_nodes.length;j++){
    	var node_label= node_label_dict[selected_nodes[j]]
        var state_list=state_dict[selected_nodes[j]];
        //for(state_index=state_list.length-1; state_index>=0; state_index--){
        node_label=node_label+ ': \n'+selected_record[selected_nodes[j]][0] +'%'
        //}

        try {
            nodes.update({
                id: selected_nodes[j],
                label: node_label.replace(' ','\n'),
                color: {background:'#ffff6e'},
                //title: rationale_table()
            });
        }
    	catch (err) {
        	alert(err);
    	}
    }
}

/*
    On selecting  the alert period dropdown, a request to alert_period() api in views.py file is made
    This api would render all the previous alert periods.
    This is used to display the elements in the dropdown.
*/
$(document).ready(function () {
	//console.log("Inside bind select2");
  	p = selected_record['product_name']
  	e = selected_record['pt']

	$('#alert_period').select2({
		width:'100%',
		placeholder: '--Select Period--',
		ajax: {
			type : "GET",
			url: url+'/period/',
			data: function (params) {
				var query = {
					alert_id : alert_id,
					archived : archived,
					p : p,
					e : e,
					q: params.term,		
				}
				return query;
			},
			dataType: 'json',
			success: function (result) {	
				//redirects the request to login page
				if(result.login === 'true'){
					window.location=result.url
				}
			}
		},

	});	
});

/*
    Script for Network Structure/DSS Result Tab and default click on Network tab.
    It also handles the column width of dataTable in Result tab as it is hidden initially.
*/
function open_tab(event, tab_name) {
	//console.log("Inside open_tab");
	$("#export_button").css("display","Block");
	//Disable the side details block on tab click
//	document.getElementById('details_block_1').style.display = "none";
//	document.getElementById('details_block_2').style.display = "none";

	var i, tabcontent, tablinks;
	tabcontent = document.getElementsByClassName("tabcontent");

	//setting display style as none in order to not show both
	for (i = 0; i < tabcontent.length; i++) {
		tabcontent[i].style.display = "none";
	}

	// make all the elements having class as tablinks inactive
	tablinks = document.getElementsByClassName("tablinks");
		for (i = 0; i < tablinks.length; i++) {
		tablinks[i].className = tablinks[i].className.replace(" active", "");
	}

	//display the tab clicked and setting it as active
	document.getElementById(tab_name).style.display = "block";
	event.currentTarget.className += " active";
	
	
	if(tab_name==='activities'){
		$("#export_button").css("display","None");
	    activities_table();
	    $('#activities .dataTables_scrollBody').height($(this).height()-288);
	}else if(tab_name == 'table'){
		$("#export_button").css("display","None");
		result_table();
		$('#table .dataTables_scrollBody').height($(this).height()-288);
	}
	// Adjusting datatables column width
	/*
		Handling column header misalignment because of being hidden due to tab.
		Adjusts the column alignment of tables visible on UI, every time table tab is clicked.
	*/
	$.fn.dataTable.tables( { visible: true, api: true } ).columns.adjust();
}


function open_modal_tab(event, tab_name) {
	//console.log("Inside open_modal_tab");
		
	
	var i, tabcontent, tablinks;
	tabcontent = document.getElementsByClassName("modal_tabcontent");

	//setting display style as none in order to not show both
	for (i = 0; i < tabcontent.length; i++) {
		tabcontent[i].style.display = "none";
	}

	// make all the elements having class as tablinks inactive
	tablinks = document.getElementsByClassName("modal_tablinks");
		for (i = 0; i < tablinks.length; i++) {
		tablinks[i].className = tablinks[i].className.replace(" active", "");
	}

	//display the tab clicked and setting it as active
	document.getElementById(tab_name).style.display = "block";
	event.currentTarget.className += " active";

	if(tab_name === 'review_history_tab_content'){
		display_detailed_rationale_history();
	}
	else if(tab_name === 'rationale_tab_content'){
		rationale_table();
	}
	
	$.fn.dataTable.tables( { visible: true, api: true } ).columns.adjust();
}

// Updating the labels in the side panel
function update_label(){
	//console.log("Inside update_label - updating product_name and pt");
	document.getElementById('product_id').innerText=selected_record['product_name'];
	document.getElementById('event_id').innerText=selected_record['pt'];
}
// Updating alert periods in the side panel
function update_period(){
	//console.log("Inside update_period - updating period label");
	document.getElementById('period_id').innerText=selected_period['text'];
}
/*
    Function for updating the recommendation on page rendering/ table(DSS Result) row click event.
    It also handles the default selection of confirmation checkbox and displaying comments in modal
    based on database values
*/
function update_recommendation(){
	
	
	document.getElementById('recommendation_id').innerText= 'Confidence in Potential Signal: '+selected_record['pec_importance'][0]+'%';
	

	$('#review_comment').val(comment)
	if(confirm === 'yes'){
		$(".confirm_block input[value='yes'][type='checkbox']").prop("checked", true);
		$(".confirm_block input[value='no'][type='checkbox']").prop("checked", false);
	}
	else if(confirm === 'no'){
		$(".confirm_block input[value='no'][type='checkbox']").prop("checked", true);
		$(".confirm_block input[value='yes'][type='checkbox']").prop("checked", false);
	}
	else{
		$(".confirm_block input[value='yes'][type='checkbox']").prop("checked", false);
		$(".confirm_block input[value='no'][type='checkbox']").prop("checked", false);	
	}
}

/*
    This function is called initially when the html body is loaded
    This handles all the initializations and function call, when the page is being rendered.
    - Handles coloring and score display of selected node in Query variables dropdown.
    - Coloring Terminal nodes based on yes/no values
    - Calling result_table() to load the DSS table in DSS_Result Tab using server side processing
    -redrawAll() - for displaying the vis.js network
    - manual_node_form() - for loading the manual evidence selection form
    - update_recommendation() - for showing the recommendation label
*/
function initialize_node_table(){
	//console.log("Inside initialize_node_table");

	// Coloring default nodes selected in dropdown
	for(j=0;j<selected_nodes.length;j++){
    	var node_label= node_label_dict[selected_nodes[j]]

        var state_list=state_dict[selected_nodes[j]];
        //for(state_index=state_list.length-1; state_index>=0; state_index--){
        node_label=node_label+"\n"+state_label_dict[state_list[0]]+ ':' + selected_record[selected_nodes[j]][0]
        //}

        // setting node values for default selected in drop down
        try {
            nodes.update({
                id: selected_nodes[j],
                label: node_label.replace(' ','\n'),
                color: {background:'#ffff6e'}
            });
        }
    	catch (err) {
        	alert(err);
    	}
	}

	//console.log("Inside initialize_node_table - coloring terminal nodes based on yes and no ");
	remove_node_labels(manual_nodes);
    color_terminal_nodes(terminal_nodes);	
	
	
	// initialize period passed from output_page function
	//console.log("Inside initialize_node_table - Calling update_period");
	update_period();

    // Script to open tabs
	// Open network tab by default - Get the element with id="default_network" and click on it
	//console.log("Inside initialize_node_table - clicking Network Structure Tab");
	document.getElementById("default_network").click();
	redrawAll();	
	manual_node_form();
	update_recommendation();

}

/*
    Script for Network display - This function is called when page is rendered using initialize_node_table() and when
    there is a table row click event to update the network in DSS Result Tab
    - Handles network configuration
    - Node click event to display the Node descriptions
    - update_label() - To update the Product, Event name labels in side panel
    - bottom_table() - To show the bottom table having intermediate scores
*/
function redrawAll() {
	//console.log("Inside redrawAll");

  	var container = document.getElementById('network_structure');
  	var data = {
  		nodes: nodes,
    	edges: edges
    };
    var options = {
    	autoResize: true,
  		height: Math.round($(window).height()*0.588) + 'px',
  		width: '100%',
  		
  		physics: {
    		barnesHut: {
      
      			avoidOverlap: 1
    		},
    		repulsion: {
				centralGravity: 0.2,
				springLength: 200,
				springConstant: 0.05,
				nodeDistance: 500,
				damping: 0.09
			},
			solver: 'barnesHut',
  		},
  		
  		
        nodes:{
        	shape: 'dot',
        	size: 38,
        	font: {
  				color: 'black',
  				size: 32,
  			},
  			color: {background:'#d4e9ef', highlight:{background:'lightgrey'}}			

        },
  		edges:{
    		arrows: {
      			to: {
        			enabled: true,
        			scaleFactor: 0.8,
        			type: "arrow"
        		}
        	},
        	length: 215,
        },
	};

	//to disable zoom out
	var afterzoomlimit = {
			scale: 0.22,
	}
	var network = new vis.Network(container, data, options);

	network.on("zoom",function(){
		if(network.getScale() <= 0.22)
		{
		network.moveTo(afterzoomlimit);
		}
	});
	network.on("afterDrawing", function (ctx) {
    	network_img_url = ctx.canvas.toDataURL();
    
  	});



	// In order to display the details on node click -
	// Side details block is set to none on click the Tab button above
	// Enabled only on node click
	network.on( 'select', function(properties) {
		//console.log("Inside redrawAll - network nodes select event");
	   	var ids = properties.nodes;
	    var clickedNodes = nodes.get(ids);
	    
	    if(clickedNodes.length===0){
			//If clicked anywhere external on the network
	    	document.getElementById('details_block_1').style.display = "none";
	    	document.getElementById('details_block_2').style.display = "none";
	    }
	    else{
	    	if(clickedNodes[0]['id'] === 'pec_importance'){
	    		$('#rationale_modal_click').click();
	    		
	    		//setting text at the bottom of table	    		
	    		document.getElementById('pec_high').innerText =selected_record['pec_importance'][0]+'%';
	    		document.getElementById('case_count').innerText =selected_record['new_count'];
	    		document.getElementById('modal_period_label').innerText = selected_period['text'];
	    		document.getElementById('details_block_1').style.display = "none";
		    	document.getElementById('details_block_2').style.display = "none";
	    		
				//open rationale tab by on modal open
				document.getElementById("default_rationale_tab").click();
				/*
				$('#rationale_modal').on('shown.bs.modal', function (e) {
       				 $.fn.dataTable.tables({ visible: true, api: true }).columns.adjust();
    			});
    			*/
	    	}
	    	else{
		    	//if Clicked on node
		    	$('#form_block').slideUp();
		    	$('#form_arrow').attr('class','down_arrow_img');

		    	var node_id = clickedNodes[0]['id']
		    	document.getElementById('details_block_1').style.display = "block";
		    	document.getElementById('details_block_2').style.display = "block";
		    	document.getElementById('name_id').innerText = node_label_dict[node_id]
		    	document.getElementById('description_id').innerText = node_description_dict[node_id]
		    	if(terminal_nodes.includes(node_id)){
		    		document.getElementById('value_id').style.display = "inline";
		    		document.getElementById('value_label_id').style.display = "inline";
		    		document.getElementById('value_id').innerText = table_label_dict[selected_record[node_id]]
		    	}
		    	else{
		    		document.getElementById('value_id').style.display = "none";
		    		document.getElementById('value_label_id').style.display = "none";		
		    	}
		    }
	    }
	    
	}); 

	//console.log("Inside redrawAll - triggering change event on query dropdown to color the nodes");
	//$( "#query_dropdown_id" ).trigger( "change" );
	intermediate_node_update();
	//console.log("Inside redrawAll - calling update_label");
	update_label();
	//console.log("Inside redrawAll - calling bottom_table");
	bottom_table();	
}


/*
    Functions related to dataTable displayed under DSS Result tab. Handles data extraction to be displayed using
    server side processing(ajax)
    On clicking of table row, the record clicked is extracted using extract_selected_record api and following functions
    are called.
    - redrawAll() - to update the vis.js network
    manual_node_form() - to update the manual evidence form
    update_recommendation() - to update the recommendation for the selected row.
*/

function result() {
	//console.log("Inside result function - sets table created in result_table into dataTable");

	var data_table =  $("#data_table").DataTable({

		"processing": true,
        "serverSide": true,
		"stateSave": true,
		"ajax":{
			type: "GET",
			"url": url+'/records/?alert_id='+alert_id+'&archived='+archived+'&p='+p+'&e='+e,
			"dataSrc": function ( result ) {
				//redirects the request to login page
                if(result.login === 'true'){
					window.location=result.url
				}
				return result.data
            },
			beforeSend: function () {
				if (typeof data_table != "undefined") {
					var xhr = data_table.settings()[0].jqXHR;
					if (xhr && xhr.readyState != 4) {
						xhr.abort();
					}
				}
			}
		},

		"scrollX": true,
		"scrollY": true,
		"select": true,
		"order":[],
		columnDefs: [
            { 'max-width': 150, targets: '_all' }
        ],
        "fixedColumns": true,
		oLanguage: {sProcessing: "<img height='25' width='25' src='../static/DSS/images/ajax-loader.gif'>"},
		"createdRow": function( row, data, dataIndex){
    		
        	if(data[0] === selected_record['product_name'] &&  data[1] === selected_record['pt']){
            	$(row).addClass('row_selected');
        	}
    	},
		
    }
	
	);


    // data table: row click event event
    $("#data_table tbody").on('click','tr',function(event) {

    	//console.log("Inside result function - click event on table row");
    	// Code for highlighting clicked row
		$($('#data_table').dataTable().fnGetNodes()).removeClass('row_selected');
		$(this).addClass('row_selected');


		p = data_table.row(this).data()[0];
		e = data_table.row(this).data()[1];
		$("#divLoading").css("display", "block");
		$.ajax({
			type : "GET",
			url: url+'/selected_record/?alert_id='+alert_id+'&archived='+archived+'&p='+p+'&e='+e,
			dataType: 'json',
			success: function (result) {
				//redirects the request to login page
				if(result.login === 'true'){
					window.location=result.url
				}
				
				selected_record = result.selected_record;
				comment = result.comment
				confirm = result.confirm


				remove_node_labels(manual_nodes);
               	color_terminal_nodes(terminal_nodes);		
				
    			//console.log("Inside document ready - select alert_period - Calling initialize_node_table ");
    			document.getElementById("default_network").click();
        		redrawAll();
        		manual_node_form();
        		update_recommendation();
				$("#divLoading").css("display", "none");
			},
			error :function(XMLHttpRequest, textStatus, errorThrown) { 
				alert("Error: " + errorThrown); 
			}
			
		});		
	
	});
}

// Creates the header for the dataTable to be displayed in DSS Result Table Tab.
function result_table(){
	//console.log("Inside result_table function - creates table");
	$('#table').empty();
	var result_table=document.createElement('TABLE');
	result_table.setAttribute("id","data_table");
    result_table.setAttribute("class","display dataTable"); 

    //creating table header
    var head=result_table.createTHead();

    var row = head.insertRow();
    for(i=0;i<result_table_header.length;i++){
    	var cell=row.insertCell(-1);
    	cell.setAttribute("align","left");
    	if(terminal_nodes.includes(result_table_header[i])){
    		cell.innerHTML=node_label_dict[result_table_header[i]];
    	}
    	else{
    		cell.innerHTML=	result_table_header[i];
    	}

    }
    document.getElementById('table').appendChild(result_table);

    result();

}

// loads body data for dataTable under Activities tab using ajax
function activities_table_body(){
    //console.log("Inside activities_table_body function - sets table created in activities_table into dataTable");
	var data_table =  $("#activities_table").DataTable({

		"processing": true,
        "serverSide": true,
		"stateSave": true,
		"ajax":{
			type: "GET",
			"url": url+'/activities_record/?&alert_id='+alert_id+'&archived='+archived+'&p='+p+'&e='+e,
			"dataSrc": function ( result ) {
				//redirects the request to login page
                if(result.login == 'true'){
					window.location=result.url
				}
				return result.data
            },
			beforeSend: function () {
				if (typeof data_table != "undefined") {
					var xhr = data_table.settings()[0].jqXHR;
					if (xhr && xhr.readyState != 4) {
						xhr.abort();
					}
				}
			}
		},

		"scrollX": true,
		"scrollY": true,
		"select": true,
		"order":[[5,"desc"]],
		columnDefs: [
            { width: 150, targets: '_all' }
        ],
        "fixedColumns": true,
		oLanguage: {sProcessing: "<img height='25' width='25' src='../static/DSS/images/ajax-loader.gif'>"},
    });
}

// Creates the header for the dataTable to be displayed in Activities Tab.
function activities_table(){
	//console.log("Inside activities_table function - creates table");
	$('#activities').empty();
	if($("#activities_table").length){
    	$('#activities_table').remove();
    }
	var activities_table=document.createElement('TABLE');
	activities_table.setAttribute("id","activities_table");
    activities_table.setAttribute("class","display dataTable");

    //creating table header
    var head=activities_table.createTHead();

    var row = head.insertRow();
    for(i=0;i<activities_table_header.length;i++){
    	var cell=row.insertCell(-1);
    	cell.setAttribute("align","left");
    	if(terminal_nodes.includes(activities_table_header[i])){
    		cell.innerHTML=node_label_dict[activities_table_header[i]];
    	}
    	else{
    		cell.innerHTML=	activities_table_header[i];
    	}
    }
    document.getElementById('activities').appendChild(activities_table);
    activities_table_body();
}


// Displays the intermediate score table under Network Tab
function bottom_table(){

	$('#intermediate_scores').show();
	$('#score_table_div').show();
	//console.log("Inside bottom_table - empty score_table_div tag");
	$('#score_table_div').empty();

	//console.log("Inside bottom_table - creating table and setting id and class");
    var table=document.createElement('TABLE');
	
    table.setAttribute("id","score_table");
    table.setAttribute("class","score_table display dataTable"); 


    jQuery(document).ready(function() {
        $(document).on("preInit.dt", function(){
            $(".dataTables_filter input[type='search']").attr("maxlength", 100);
          });
	    $(table).DataTable( {
			"scrollX": true,
			"paging" : false,
			"searching" : false,
			"info": false,
			"ordering": false
			} );
	});
	
    
    //Score table - bottom
    //creating table header
    var head=table.createTHead();

    var row = head.insertRow();
    for(i=0;i<non_terminal_nodes.length;i++){
    	var cell=row.insertCell(-1);
    	cell.setAttribute("align","left");
    	cell.innerHTML=node_label_dict[non_terminal_nodes[i]];

    }

    //creating table body - bottom
    body=table.createTBody();
    var row = body.insertRow();
    // to remove cursor point from row_id
    row.setAttribute("class","score_table");


	for(node_index=0;node_index<non_terminal_nodes.length;node_index++){
    	var cell = row.insertCell(-1);
    	cell.setAttribute("align","left");
		var cell_label='';
        for(i=0;i<json_records_keys.length;i++){

        	if (json_records_keys[i]===non_terminal_nodes[node_index]){
        			var state_list = state_dict[non_terminal_nodes[node_index]];
        			for(state_index=0; state_index<state_list.length;state_index++){
        				cell_label = cell_label + table_label_dict[state_list[state_index]]+ ': ' + selected_record[json_records_keys[i]][state_index]+'<br>';
        			}
		    }
        }
        cell.innerHTML = cell_label;
		       
	}	

    
    document.getElementById('score_table_div').appendChild(table);
}


//highlighting node border based on clickling node label in manual node form
function node_highlight(id){
	
	if(nodes.get(id)['borderWidth'] === 3){
		nodes.update({
        		id: id,
        		borderWidth: 1,
    		});

	}else{
		nodes.update({
        		id: id,
        		borderWidth: 3,
    		});

	}

	
}
// To display the manual evidence checkbox form
function manual_node_form(){

	if($("#manual_node_table").length){
    	$('#manual_node_table').remove();        
    }
	var tbody = document.createElement("tbody");
	tbody.setAttribute('id','manual_node_table')
	
	var td = ''
	var label = ''
	var tr = ''
	var input = ''
	var text = ''
	for(index = 0; index<manual_nodes.length; index++){
		tr = document.createElement("tr");

		td = document.createElement("td");
		label = document.createElement("label");
		label.innerHTML  = node_label_dict[manual_nodes[index]];
		label.setAttribute('id',manual_nodes[index]);
		label.setAttribute('style', "cursor:pointer");
		label.setAttribute('onclick', "node_highlight(id)");
		td.appendChild(label);
		tr.appendChild(td);

		
		td = document.createElement("td");
		div = document.createElement("div"); 
		div.setAttribute("class","checkbox-custom");
		label = document.createElement("label");
		input = document.createElement('input');
		input.setAttribute("type","checkbox");
		input.setAttribute("class","form-check-input");
		input.setAttribute("value",state_dict[manual_nodes[index]][0]);
		// to set checkbox based on db values
		if(terminal_nodes.includes(manual_nodes[index])){
			if(selected_record[manual_nodes[index]] === state_dict[manual_nodes[index]][0]){
				input.setAttribute("checked","true");
			}
		}
		input.setAttribute("name",manual_nodes[index]);
		input.setAttribute("style","cursor:pointer");
		div.appendChild(input);
		text = document.createTextNode(table_label_dict[state_dict[manual_nodes[index]][0]]);
		label.appendChild(text);
		div.appendChild(label)
		td.appendChild(div);
		tr.appendChild(td);


		td = document.createElement("td");
		div = document.createElement("div"); 
		div.setAttribute("class","checkbox-custom");
		label = document.createElement("label");
		input = document.createElement('input');
		input.setAttribute("type","checkbox");
		input.setAttribute("class","form-check-input");
		input.setAttribute("value",state_dict[manual_nodes[index]][1]);
		// to set checkbox based on db values
		if(terminal_nodes.includes(manual_nodes[index])){
			if(selected_record[manual_nodes[index]] === state_dict[manual_nodes[index]][1]){
				input.setAttribute("checked","true");
			}
		}
		input.setAttribute("name",manual_nodes[index]);
		input.setAttribute("style","cursor:pointer");
		div.appendChild(input);
		text = document.createTextNode(table_label_dict[state_dict[manual_nodes[index]][1]]);
		label.appendChild(text);
		div.appendChild(label)
		td.appendChild(div);
		tr.appendChild(td);

		tbody.appendChild(tr);	
	}
	

	document.getElementById('form_table').appendChild(tbody);


	// To act as radio button and provide unselecting option
	// the selector will match all input controls of type :checkbox
	// and attach a click event handler 
	$("input:checkbox").on('click', function() {
		
	  var $chk_box = $(this);
	  if ($chk_box.is(":checked")) {
	    // the name of the box is retrieved using the .attr() method
	    // as it is assumed and expected to be immutable
	    var group = "input:checkbox[name='" + $chk_box.attr("name") + "']";
	    // the checked state of the group/box on the other hand will change
	    // and the current value is retrieved using .prop() method
	    $(group).prop("checked", false);
	    $chk_box.prop("checked", true);
	  } else {
	    $chk_box.prop("checked", false);
	  }
	});
}



function getFormData($form){
    var unindexed_array = $form.serializeArray();
    var indexed_array = {};

    $.map(unindexed_array, function(n, i){
        indexed_array[n['name']] = n['value'];
    });

    return indexed_array;
}


//On manual evidence form submission
$(document).ready(function () {

	$("#manual_node_form").submit(function(obj) {

    obj.preventDefault(); // avoid to execute the actual submit of the form.
    
    var form = $(this);
    form_url = form.attr('action');

    data = getFormData(form)
    data['p'] = p
    data['e'] = e
    data['alert_id'] = alert_id
    data['archived'] = archived.toString()
    data['record'] = JSON.stringify(selected_record)
    
    $.ajax({
		type: "POST",
		url: form_url,
		data: JSON.stringify(data),
		success: function(result)
		{
			//redirects the request to login page
			if(result.login === 'true'){
				window.location=result.url
			}

		   	selected_record = result.selected_record; 
		   	form_submitted_node = result.form_submitted_node
		   	
		   	remove_node_labels(manual_nodes);
		   	color_terminal_nodes(form_submitted_node);
		   	update_recommendation();
		    redrawAll();
		    document.getElementById('details_block_1').style.display = "none";
		    document.getElementById('details_block_2').style.display = "none";
		},
		error :function(XMLHttpRequest, textStatus, errorThrown) { 
			alert("Error: " + errorThrown); 
		}  
    });
    


	});
});


//Initialize the labels to refresh the previous data - remove all labels and colors from nodes
function remove_node_labels(node_list){
	for (i=0; i<node_list.length;i++){
    	try {
    		nodes.update({
        		id: node_list[i],
        		label: node_label_dict[node_list[i]].replace(' ','\n'),
        		color: {background:'#d4e9ef'}
    		});
    	}
    	catch (err) {
        	alert(err);
    	}

	}

}

//Colors the nodes in node_list based on yes/no values
function color_terminal_nodes(node_list){

   	//color based on values
   	//console.log('*********************88')
   	
	for (var j=0; j<node_list.length;j++){
		
		if((selected_record[node_list[j]].toLowerCase()==='yes' && node_effect[node_list[j]]==='direct')
			|| (selected_record[node_list[j]].toLowerCase()==='no' && node_effect[node_list[j]]==='inverse')){
    		try {
    			
        		nodes.update({
            		id: node_list[j],
            		label: node_label_dict[node_list[j]].replace(' ','\n')+"\n"+state_label_dict[selected_record[node_list[j]]],
    				color: {background:'#ff9a98'},
    			});
    		

	    	}

        	catch (err) {
            	alert(err);
        	}
        }
    	else{
    		try{
    		
        		nodes.update({
            		id: node_list[j],
            		label: node_label_dict[node_list[j]].replace(' ','\n')+"\n"+state_label_dict[selected_record[node_list[j]]],
    				color: {background:'#90ee90'},

    			});
    			
	    	}
        	
            catch (err) {
               	alert(err);
        	}


    	}

	}

}


// Toggle manual node form on click event
$(document).ready(function () {
	//$('#form_block').slideUp();
	document.getElementById('form_clickable').addEventListener('click', clickDiv);

	function clickDiv() {


		if (document.getElementById('form_block').style.display === "none") {
 		    $('#form_arrow').attr('class','up_arrow_img');
// 		    document.getElementById('details_block_1').style.display = "none";
// 		    document.getElementById('details_block_2').style.display = "none";
		}
		else{
			$('#form_arrow').attr('class','down_arrow_img');	
		}

		$('#form_block').slideToggle();   
	}
});

// Toggle query variables widget on click event
$(document).ready(function () {

	document.getElementById('query_variables_clickable').addEventListener('click', click_QueryVariable_Div);

	function click_QueryVariable_Div() {
		if (document.getElementById('query_variables_block').style.display === "none") {
 		    $('#query_variables_arrow').attr('class','up_arrow_img');
		}
		else{
			$('#query_variables_arrow').attr('class','down_arrow_img');
		}
		$('#query_variables_block').slideToggle();
	}
});



// Export functionality - uses html2pdf plugin for exporting data to html
$(document).ready(function () {	
	$('#export_button').click(function () {

		var container= document.createElement('div');        

		var network_img = document.createElement('img');
		network_img.setAttribute("height",'720px');
		network_img.setAttribute("width",'1000px');
		network_img.src = network_img_url;
		

        var product= document.getElementById("product_id").cloneNode(true);
        var event = document.getElementById("event_id").cloneNode(true);
        var period = document.getElementById("period_id").cloneNode(true);
        var legend = document.getElementById("network_legend").cloneNode(true);
        //setting padding based on pdf page 
        legend.setAttribute("style",'padding-left:520px');
        network_img.setAttribute("style",'padding-right:250px')
        container.appendChild(product);
        container.appendChild(document.createElement('br'));
        container.appendChild(event);
        container.appendChild(document.createElement('br'));
        container.appendChild(period);
		container.appendChild(document.createElement('br'));
        container.appendChild(legend);
        container.appendChild(document.createElement('br'));
        container.appendChild(network_img);

        //add container to body and set attributes before rendering on pdf(for testing)
       	//document.body.appendChild(container);        

        //intermediate scores table  - creating it in transpose format to handle scroll issue.
	    var table=document.createElement('TABLE'); 
	    table.setAttribute('style','border: 1px solid #bebebe !important')

	    
	    body=table.createTBody();
		for(node_index=0;node_index<non_terminal_nodes.length;node_index++){
	    	var row = body.insertRow();
	    	row.setAttribute('style','border: 1px solid #bebebe !important')
			var cell=row.insertCell(-1);
	    	cell.innerHTML=node_label_dict[non_terminal_nodes[node_index]]+" :";
	    	
	    	cell = row.insertCell(-1);
			var cell_label='';
	        for(i=0;i<json_records_keys.length;i++){

	        	if (json_records_keys[i]===non_terminal_nodes[node_index]){
	        			var state_list = state_dict[non_terminal_nodes[node_index]];
	        			for(state_index=0; state_index<state_list.length;state_index++){
	        				cell_label = cell_label + table_label_dict[state_list[state_index]]+ ': ' + selected_record[json_records_keys[i]][state_index]+'<br>';
	        			}
			    }
	        }
	        cell.innerHTML = cell_label;
			       
		}

		// new page element - Intermediate score table
		var container2 = document.createElement('div');
		var text = document.createElement('label');
		text.style.fontSize = '14px';
		text.innerHTML = "Intermediate Scores : ";
	    container2.appendChild(text);
		container2.appendChild(document.createElement('br'));
	    container2.appendChild(table);


	    // Options is used to configure the margin and image quality
        var opt =  {
            margin: [28,15,15,15],
            filename: product.innerHTML+'_'+event.innerHTML+'_'+period.innerHTML + '.pdf',
            image: {type: 'jpeg', quality: 1},
            pagebreak: {
                mode: ['css', 'whiteline'],
                avoid: ['img', '.row']
            },
            html2canvas: {
                scale: 4,
                dpi: 300,
                letterRendering: true,
                logging: true
            },
            jsPDF: {
                unit: 'mm',
                format: 'a4',
                orientation: 'portrait'
            },
            
        };


        // header company logo
        var img = new Image();
		img.src = ('../static/DSS/images/company-logo.png');
        

        html2pdf().from(container).set(opt).toPdf().get('pdf').then(function (pdf) {
        	pdf.addPage();
		}).from(container2).toContainer().toCanvas().toPdf().get('pdf').then(function (pdf) {
			// Your code to alter the pdf object.
			var totalPages = pdf.internal.getNumberOfPages();
			for (i = 1; i <= totalPages; i++) {
				pdf.setPage(i);
				
				//pdf header
				pdf.setFontSize(14);
				pdf.setFontType("bold");
				pdf.addImage(img, 'PNG', 15, 5, 20, 10);
				pdf.text("Decision Support System", 80, 24 );
				pdf.setLineCap(2);
				pdf.setLineWidth(0.5); 
				pdf.line(15, 25,pdf.internal.pageSize.getWidth()-15,25); // horizontal line
				
				
				//pdf footer
				pdf.setFontType("normal");
				pdf.setFontSize(9);
				pdf.text(i + ' of '+ totalPages, 15,  pdf.internal.pageSize.getHeight() - 10)
				pdf.text("RxLogix Corporation India  All Rights Reserved © 2020",pdf.internal.pageSize.getWidth() - 100, pdf.internal.pageSize.getHeight() - 10);
			}
		}).save();

		
	});

});


// To sync between confirmation checkbox and modal checkbox
$(document).ready(function () {
	$(".confirm_block input[value='yes'][type='checkbox']").change(function(){	
		$(".confirm_block input[value='yes'][type='checkbox']").prop("checked", $(this).prop("checked"));
	});

	$(".confirm_block input[value='no'][type='checkbox']").change(function(){	
		$(".confirm_block input[value='no'][type='checkbox']").prop("checked", $(this).prop("checked"));
	});

});


//On review/confirmation form submission
function review_storage(){
    
    var form_url = '/dss_review/'
    comment = $('#review_comment').val();
    e = selected_record['pt'];
    p = selected_record['product_name'];
    confirm = $("input[name='confirm']:checked").val();
    if(confirm === undefined){
    	confirm =''
    }
    
    $.ajax({
		type: "POST",
		url: form_url,
		data : JSON.stringify({"comment":comment, "confirm":confirm, "alert_id":alert_id,"archived":archived.toString(),"p":p,"e":e,"common_name":common_name }),
		//data: form.serialize()+'&alert_id='+alert_id+'&archived='+archived+'&p='+p+'&comment='+comment+'&e='+e,
		success: function(result)
		{
			//redirects the request to login page
			if(result.login === 'true'){
				window.location=result.url
			}
			//console.log('Form submission completed');
			activities_table();
		},
		error :function(XMLHttpRequest, textStatus, errorThrown) { 
			alert("Error: " + errorThrown); 
		}       
       		
       		
    });
};


/*
Rationale table to summarize the network results. This is rendered in the title attribute of
pec_importance node in network
*/

function format_rationale_table_child ( child_nodes ) {
    
    var childNodes = $();    
    for (child_node_index = 0;child_node_index< child_nodes.length;child_node_index++){
    	
        childNodes = childNodes.add('<tr>'+
							        	'<td></td>'+
          								'<td>' + rationale_table_child_dict[child_nodes[child_node_index]][0] +'</td>'+
          								'<td class ="text-center"><span class =" badge badge-'+rationale_table_child_dict[child_nodes[child_node_index]][1]+'">' + rationale_table_child_dict[child_nodes[child_node_index]][1] +'</span></td>'+
                   						'<td class ="text-center">' + rationale_table_child_dict[child_nodes[child_node_index]][2] +'</td>'+
                   						'<td>' + rationale_table_child_dict[child_nodes[child_node_index]][3] +'</td>'+
                					'</tr>');
    }        
    return childNodes ;  
}


function rationale_table(){
	var table=document.createElement('TABLE'); 
    //table.setAttribute('style','border: 1px solid #bebebe !important ;')
    table.setAttribute("id","rationale_table_dt");
    table.setAttribute("class","display dataTable"); 

    jQuery(document).ready(function() {
	    var rationale_table_element = $(table).DataTable({
			"scrollX": false,
			"paging" : false,
			"searching" : false,
			"info": false,
			"order": [[2,"desc"]],
			"autoWidth": false,
			"columnDefs": [
   							{ orderable: false,width:1, targets: 0},
						],
		});


	    // Add event listener for opening and closing details - individual row
		$('#rationale_table_dt').on('click', 'td.details-control', function () {
			var tr = $(this).closest('tr');
			var row = rationale_table_element.row(tr);
			if (row.child.isShown()) {
			  // This row is already open - close it
			  row.child.hide();
			  tr.removeClass('shown');
			} else {		  
			  row.child(format_rationale_table_child(tr.data("key-child").split(','))).show();
			  tr.addClass('shown');
			}
	    });

		// Expand and collapse all the rows
		$("#ExpandCollapseAll").click(function () {

			if(this.classList.contains('double_down')){
				this.classList.remove('double_down');
				this.classList.add('double_up');
				
				// Enumerate all rows
		        rationale_table_element.rows().every(function(index){
		        	
		            // If row has details collapsed
		            if(!this.child.isShown()){
		                // Open this row
		                
		                tr = this.node()		                
		                if(tr.hasAttribute('data-key-child')){
			                this.child(format_rationale_table_child(tr.getAttribute("data-key-child").split(','))).show();
		                	$(this.node()).addClass('shown');
		                }
		                
		            }
		        });
				
			}
			else if(this.classList.contains('double_up')){
				this.classList.remove('double_up');
				this.classList.add('double_down');
				rationale_table_element.rows().every(function(){
           		 // If row has details expanded
		            if(this.child.isShown()){
		                // Collapse row details
		                this.child.hide();
		                $(this.node()).removeClass('shown');
		            }
		        });
			}
			else{
				console.log('class else down present');
				this.classList.remove('double_down');
				this.classList.add('double_up');
				rationale_table_element.rows(':not(.parent)').nodes().to$().find('td:first-child').trigger('click');
			}
		});


	});

	
    //creating head data
    var head=table.createTHead();
    var row = head.insertRow();
    row.setAttribute('class','modal_dataTable_Header')

    var cell=row.insertCell(-1);
	cell.setAttribute("align","left");   
	cell.setAttribute("id","ExpandCollapseAll");   
	cell.setAttribute("class","double_down");   
	
	cell.innerHTML='';

	var cell=row.insertCell(-1);
	cell.setAttribute("align","left");   
	cell.innerHTML='PV Concept';
	//cell.innerHTML = "<img id = 'rational_table_img' src = '../static/DSS/images/double_down.png' style='width:10px;height:10px;cursor:pointer;' class = 'down' onclick = toggle_tree('rational_table_img','rationale_table_dt')> &nbspPV Concept";

	cell=row.insertCell(-1);    
	cell.setAttribute("align","center");
	cell.innerHTML='Potential Signal'
	//cell.setAttribute('onclick', "sortTable('rationale_table_dt', 1)");

	cell=row.insertCell(-1);    
	cell.setAttribute("align","center");
	cell.innerHTML='Score Confidence'

	cell=row.insertCell(-1);    
	cell.setAttribute("align","left");
	cell.setAttribute('class','col-min-100 col-max-300 cell-break')
	cell.innerHTML='Rationale'
	
    //creating body	
    body=table.createTBody();
    
    var node_list = Object.keys(rationale_order_dict)
	for(i=0; i < node_list.length;i++){

		if(rationale_parent_dict[node_list[i]] === ''){
			row = body.insertRow();
			child_nodes = []
			child_nodes = Object.keys(rationale_parent_dict).filter(function(key) {return rationale_parent_dict[key] === node_list[i]});
	
			cell=row.insertCell(-1);    
			cell.setAttribute("align","left");
			cell.innerHTML = '';
			if(child_nodes.length>0){				
				cell.setAttribute('class',"details-control");
				row.setAttribute('data-key-child',child_nodes)
			}

			
			
			cell=row.insertCell(-1);    
			cell.setAttribute("align","left");
			cell.innerHTML=node_label_dict[node_list[i]]

			cell=row.insertCell(-1);    
			cell.setAttribute("align","center");
			if(typeof selected_record[node_list[i]]!="object" && selected_record[node_list[i]]!=undefined){
				if((selected_record[node_list[i]].toLowerCase()==='yes' && node_effect[node_list[i]]==='direct')
				|| (selected_record[node_list[i]].toLowerCase()==='no' && node_effect[node_list[i]]==='inverse')){
					cell.innerHTML = '<span class ="badge badge-Yes">Yes</span>'
					
				}
				else{
					cell.innerHTML = '<span class ="badge badge-No">No</span>'
					
				}
			}
			else if(typeof selected_record[node_list[i]]==="object"){
				if((selected_record[node_list[i]][0]>rationale_threshold_dict[node_list[i]] && node_effect[node_list[i]]==='direct')
				|| (selected_record[node_list[i]][0]<rationale_threshold_dict[node_list[i]]&& node_effect[node_list[i]]==='inverse')){
					cell.innerHTML = '<span class ="badge badge-Yes">Yes</span>'	
					
				}
				else{
					cell.innerHTML = '<span class ="badge badge-No">No</span>'
					
				}
			}
			else{
				cell.innerHTML = ''
			}


			cell=row.insertCell(-1);    
			cell.setAttribute("align","center");
			if(typeof selected_record[node_list[i]]==="object"){
				if((selected_record[node_list[i]][0]>rationale_threshold_dict[node_list[i]] && node_effect[node_list[i]]==='direct')
					|| (selected_record[node_list[i]][0]>rationale_threshold_dict[node_list[i]]&& node_effect[node_list[i]]==='inverse')){
					cell.innerHTML = selected_record[node_list[i]][0] +'%'
				}
				else{
					cell.innerHTML = selected_record[node_list[i]][1] +'%'

				}
			}
			else{
				cell.innerHTML = ''
			}


			cell=row.insertCell(-1);    
			cell.setAttribute("align","left");
			if(typeof selected_record[node_list[i]]!="object" && selected_record[node_list[i]]!=undefined){
				if((selected_record[node_list[i]].toLowerCase()==='yes' && node_effect[node_list[i]]==='direct')
				|| (selected_record[node_list[i]].toLowerCase()==='no' && node_effect[node_list[i]]==='inverse')){
					
					temp_str = rationale_text_potential_yes[node_list[i]].replace(/\n/g,' <br>')
					matched_strings = temp_str.match(/=[a-zA-Z0-9_]+/g)
					if (matched_strings != null){

						for(matched_index=0;matched_index<matched_strings.length;matched_index++){
							if(selected_record[matched_strings[matched_index].replace('=','')]==='New'){
								temp_str = temp_str.replace(new RegExp( ',.*'+matched_strings[matched_index], 'g' ),'')	
							}
							temp_str = temp_str.replace(matched_strings[matched_index],selected_record[matched_strings[matched_index].replace('=','')])
							
						}
					}

					//Explicity handling rationale for severity increase
                	if (node_list[i] === 'severity_incr'){
                		
	                    boolean_params = 'New seriousness type observed: <br>'
	                    percent_params = 'Increase in existing seriousness: <br>'
    	                for(ind = 0;ind<boolean_severity.length;ind++){
        	                if(parseInt(selected_record[boolean_severity[ind]])===1){
            	                boolean_params = boolean_params + severity_rationale_dict[boolean_severity[ind]] + ', '
        	                }
    	                }
    	                if(boolean_params !== 'New seriousness type observed: <br>'){
	                        temp_str = temp_str + '<br>' +  boolean_params.slice(0,-2)
    	                }

                    	for(ind =0;ind<percent_severity.length;ind++){

	                        if(parseFloat(selected_record[percent_severity[ind]])>parseFloat(severity_incr_threshold)){

								regex_str = severity_rationale_dict[percent_severity[ind]].replace(/\n/g,' <br>')
								matched_strings = regex_str.match(/=[a-zA-Z0-9_]+/g)
								if (matched_strings != null){

									for(matched_index=0;matched_index<matched_strings.length;matched_index++){
										if(selected_record[matched_strings[matched_index].replace('=','')]==='New'){
											temp_str = temp_str.replace(new RegExp( ',.*'+matched_strings[matched_index], 'g' ),'')	
										}
										regex_str = regex_str.replace(matched_strings[matched_index],selected_record[matched_strings[matched_index].replace('=','')])
									}
								}	                            
	                            percent_params = percent_params +  regex_str +', '
	                        }

                    	}
                    	if(percent_params !== 'Increase in existing seriousness: <br>'){
	                        temp_str = temp_str + '<br>' +  percent_params.slice(0,-2)
    	                }
                    }

					cell.innerHTML = temp_str;
				}
				else{

					temp_str = rationale_text_potential_no[node_list[i]].replace(/\n/g,' <br>')
					matched_strings = temp_str.match(/=[a-zA-Z0-9_]+/g)
					if (matched_strings != null){
						for(matched_index=0;matched_index<matched_strings.length;matched_index++){
							
							if(selected_record[matched_strings[matched_index].replace('=','')]==='New'){
								temp_str = temp_str.replace(new RegExp( ',.*'+matched_strings[matched_index], 'g' ),'')	
							}
							temp_str = temp_str.replace(matched_strings[matched_index],selected_record[matched_strings[matched_index].replace('=','')])
						
						}
					}
					cell.innerHTML = temp_str;
					
				}
			}
			else if(typeof selected_record[node_list[i]]==="object"){
				if((selected_record[node_list[i]][0]>rationale_threshold_dict[node_list[i]] && node_effect[node_list[i]]==='direct')
				|| (selected_record[node_list[i]][0]<rationale_threshold_dict[node_list[i]]&& node_effect[node_list[i]]==='inverse')){
					temp_str = rationale_text_potential_yes[node_list[i]].replace(/\n/g,' <br>')
					matched_strings = temp_str.match(/=[a-zA-Z0-9_]+/g)
					if (matched_strings != null){
						for(matched_index=0;matched_index<matched_strings.length;matched_index++){
							if(selected_record[matched_strings[matched_index].replace('=','')]==='New'){
								temp_str = temp_str.replace(new RegExp( ',.*'+matched_strings[matched_index], 'g' ),'')	
							}
							temp_str = temp_str.replace(matched_strings[matched_index],selected_record[matched_strings[matched_index].replace('=','')])
							
						}
					}
					cell.innerHTML = temp_str;
				}
				else{
					temp_str = rationale_text_potential_no[node_list[i]].replace(/\n/g,' <br>')
					matched_strings = temp_str.match(/=[a-zA-Z0-9_]+/g)
					if (matched_strings != null){
						for(matched_index=0;matched_index<matched_strings.length;matched_index++){
							if(selected_record[matched_strings[matched_index].replace('=','')]==='New'){
								temp_str = temp_str.replace(new RegExp( ',.*'+matched_strings[matched_index], 'g' ),'')	
							}
							temp_str = temp_str.replace(matched_strings[matched_index],selected_record[matched_strings[matched_index].replace('=','')])
							
						}
					}
					cell.innerHTML = temp_str;
					

				}
			}
			else{
				cell.innerHTML = ''
			}
		}
		else{
			
			rationale_table_child_dict[node_list[i]]= []
			rationale_table_child_dict[node_list[i]].push(node_label_dict[node_list[i]])

			if(typeof selected_record[node_list[i]]!="object" && selected_record[node_list[i]]!=undefined){
				if((selected_record[node_list[i]].toLowerCase()==='yes' && node_effect[node_list[i]]==='direct')
				|| (selected_record[node_list[i]].toLowerCase()==='no' && node_effect[node_list[i]]==='inverse')){
					rationale_table_child_dict[node_list[i]].push('Yes')
				}
				else{
					rationale_table_child_dict[node_list[i]].push('No')
				}
			}
			else if(typeof selected_record[node_list[i]]==="object"){
				if((selected_record[node_list[i]][0]>rationale_threshold_dict[node_list[i]] && node_effect[node_list[i]]==='direct')
				|| (selected_record[node_list[i]][0]<rationale_threshold_dict[node_list[i]]&& node_effect[node_list[i]]==='inverse')){
					rationale_table_child_dict[node_list[i]].push('Yes')
				}
				else{
					rationale_table_child_dict[node_list[i]].push('No')
				}
			}
			else{
				rationale_table_child_dict[node_list[i]].push(' ')
			}

			
			if(typeof selected_record[node_list[i]]==="object"){
				if((selected_record[node_list[i]][0]>rationale_threshold_dict[node_list[i]] && node_effect[node_list[i]]==='direct')
					|| (selected_record[node_list[i]][0]>rationale_threshold_dict[node_list[i]]&& node_effect[node_list[i]]==='inverse')){
					rationale_table_child_dict[node_list[i]].push(selected_record[node_list[i]][0] +'%')
				}
				else{
					rationale_table_child_dict[node_list[i]].push(selected_record[node_list[i]][1] +'%')

				}
			}
			else{
				rationale_table_child_dict[node_list[i]].push(' ')
			}


			if(typeof selected_record[node_list[i]]!="object" && selected_record[node_list[i]]!=undefined){
				if((selected_record[node_list[i]].toLowerCase()==='yes' && node_effect[node_list[i]]==='direct')
				|| (selected_record[node_list[i]].toLowerCase()==='no' && node_effect[node_list[i]]==='inverse')){
					
					temp_str = rationale_text_potential_yes[node_list[i]].replace(/\n/g,' to<br>')
					matched_strings = temp_str.match(/=[a-zA-Z0-9_]+/g)
					if (matched_strings != null){
						for(matched_index=0;matched_index<matched_strings.length;matched_index++){
							if(selected_record[matched_strings[matched_index].replace('=','')]==='New'){
								temp_str = temp_str.replace(new RegExp( ',.*'+matched_strings[matched_index], 'g' ),'')	
							}
							temp_str = temp_str.replace(matched_strings[matched_index],selected_record[matched_strings[matched_index].replace('=','')])
							
						}
					}
					rationale_table_child_dict[node_list[i]].push(temp_str);
				}
				else{

					temp_str = rationale_text_potential_no[node_list[i]].replace(/\n/g,' to<br>')
					matched_strings = temp_str.match(/=[a-zA-Z0-9_]+/g)
					if (matched_strings != null){
						for(matched_index=0;matched_index<matched_strings.length;matched_index++){
							
							if(selected_record[matched_strings[matched_index].replace('=','')]==='New'){
								temp_str = temp_str.replace(new RegExp( ',.*'+matched_strings[matched_index], 'g' ),'')	
							}
							temp_str = temp_str.replace(matched_strings[matched_index],selected_record[matched_strings[matched_index].replace('=','')])
							
						}
					}
					rationale_table_child_dict[node_list[i]].push(temp_str)
					
				}
			}
			else if(typeof selected_record[node_list[i]]==="object"){
				if((selected_record[node_list[i]][0]>rationale_threshold_dict[node_list[i]] && node_effect[node_list[i]]==='direct')
				|| (selected_record[node_list[i]][0]<rationale_threshold_dict[node_list[i]]&& node_effect[node_list[i]]==='inverse')){
					temp_str = rationale_text_potential_yes[node_list[i]].replace(/\n/g,' to<br>')
					matched_strings = temp_str.match(/=[a-zA-Z0-9_]+/g)
					if (matched_strings != null){
						for(matched_index=0;matched_index<matched_strings.length;matched_index++){
							
							if(selected_record[matched_strings[matched_index].replace('=','')]==='New'){
								temp_str = temp_str.replace(new RegExp( ',.*'+matched_strings[matched_index], 'g' ),'')	
							}
							temp_str = temp_str.replace(matched_strings[matched_index],selected_record[matched_strings[matched_index].replace('=','')])
							
						}
					}
					rationale_table_child_dict[node_list[i]].push(temp_str)
				}
				else{
					temp_str = rationale_text_potential_no[node_list[i]].replace(/\n/g,' to<br>')
					matched_strings = temp_str.match(/=[a-zA-Z0-9_]+/g)
					if (matched_strings != null){
						for(matched_index=0;matched_index<matched_strings.length;matched_index++){
							if(selected_record[matched_strings[matched_index].replace('=','')]==='New'){
								temp_str = temp_str.replace(new RegExp( ',.*'+matched_strings[matched_index], 'g' ),'')	
							}
							temp_str = temp_str.replace(matched_strings[matched_index],selected_record[matched_strings[matched_index].replace('=','')])
						}
					}
					rationale_table_child_dict[node_list[i]].push(temp_str)

				}
			}
			else{
				rationale_table_child_dict[node_list[i]].push(' ')
			}

		}	

	}
	
	$('#rationale_tab_content').empty();
	document.getElementById('rationale_tab_content').appendChild(table);
	
}



function display_detailed_rationale_history(){
	////console.log('Detailed history')
	////console.log(p,e,alert_id,archived)
	$.ajax({
		type: "POST",
		url: url+'/detailed_history/',
		data : JSON.stringify({"p":p, "e":e, "alert_id":alert_id,"archived":archived.toString()}),
		
		success: function(result)
		{
			//redirects the request to login page
			if(result.login === 'true'){
				window.location=result.url
			}

			create_rationale_history_table(result.data)
			
			
		},
		error :function(XMLHttpRequest, textStatus, errorThrown) { 
			alert("Error: " + errorThrown); 
		}
	});       
       		
}



/* 
To show the child rows in DSS details modal
This will be called on clicking expand button having id = 'history_ExpandCollapseAll'
*/
function format_detailed_history_table_child ( child_nodes ) {
    
    var childNodes = $();    
    for (child_node_index = 0;child_node_index< child_nodes.length;child_node_index++){
    	

    	element = '<tr><td></td>' + '<td>'+detailed_history_table_child_dict[child_nodes[child_node_index]][0] +'</td>'
    	for(index = 1;index<detailed_history_table_child_dict[child_nodes[child_node_index]].length;index++){
    		element = element + '<td class ="text-center"><span class = "badge badge-'+detailed_history_table_child_dict[child_nodes[child_node_index]][index]+'">'+detailed_history_table_child_dict[child_nodes[child_node_index]][index] +'</span></td>'
    	}
    	element = element + '</tr>'
        childNodes = childNodes.add(element);
    }        
    return childNodes ;  
}


function create_rationale_history_table(period_score_dict){
	var table=document.createElement('TABLE'); 
    //table.setAttribute('style','border: 1px solid #bebebe !important ;')
    table.setAttribute("id","rationale_history");
    table.setAttribute("class","display dataTable"); 



    jQuery(document).ready(function() {
	    var history_table_element = $(table).DataTable({
			"scrollX": false,
			"paging" : false,
			"searching" : false,
			"info": false,
			"order": [],
			"autoWidth": false,
			"columnDefs": [
   							{ orderable: false, width:1, targets: 0}
						],
		});


	    // Add event listener for opening and closing details - individual row
		$('#rationale_history').on('click', 'td.details-control', function () {
			var tr = $(this).closest('tr');
			var row = history_table_element.row(tr);
			if (row.child.isShown()) {
			  // This row is already open - close it
			  row.child.hide();
			  tr.removeClass('shown');
			} else {		  
			  row.child(format_detailed_history_table_child(tr.data("key-child").split(','))).show();
			  tr.addClass('shown');
			}
	    });

		// Expand and collapse all the rows
		$("#history_ExpandCollapseAll").click(function () {

			if(this.classList.contains('double_down')){
				this.classList.remove('double_down');
				this.classList.add('double_up');
				
				// Enumerate all rows
		        history_table_element.rows().every(function(index){
		        	
		            // If row has details collapsed
		            if(!this.child.isShown()){
		                // Open this row
		                
		                tr = this.node()		                
		                if(tr.hasAttribute('data-key-child')){
			                this.child(format_detailed_history_table_child(tr.getAttribute("data-key-child").split(','))).show();
		                	$(this.node()).addClass('shown');
		                }
		                
		            }
		        });
				
			}
			else if(this.classList.contains('double_up')){
				this.classList.remove('double_up');
				this.classList.add('double_down');
				history_table_element.rows().every(function(){
           		 // If row has details expanded
		            if(this.child.isShown()){
		                // Collapse row details
		                this.child.hide();
		                $(this.node()).removeClass('shown');
		            }
		        });
			}
			else{
				console.log('class else down present');
				this.classList.remove('double_down');
				this.classList.add('double_up');
				history_table_element.rows(':not(.parent)').nodes().to$().find('td:first-child').trigger('click');
			}
		});


	});



    var head=table.createTHead();
    var row = head.insertRow();
    row.setAttribute('class','modal_dataTable_Header')

	var cell=row.insertCell(-1); 
	cell.setAttribute("align","left");   
	cell.setAttribute("id","history_ExpandCollapseAll");   
	cell.setAttribute("class","double_down");   
	cell.innerHTML='';

	cell=row.insertCell(-1); 
	cell.setAttribute("align","left");   
	cell.innerHTML='PV Concept';

    
	period_score_dict_keys = Object.keys(period_score_dict)
    for(i=0;i<period_score_dict_keys.length;i++){
		var cell=row.insertCell(-1); 
		cell.setAttribute("align","center");   
		cell.innerHTML= period_score_dict_keys[i].replace(' - ',' to<br>')
    }

	
    body=table.createTBody();    

    var node_list = Object.keys(rationale_order_dict)
    row_count =0
	for(i=0; i < node_list.length;i++){
		
		if(rationale_parent_dict[node_list[i]] === ''){
			row_count = row_count + 1;
			row = body.insertRow();
			child_nodes = []
			child_nodes = Object.keys(rationale_parent_dict).filter(function(key) {return rationale_parent_dict[key] === node_list[i]});
	
			cell=row.insertCell(-1);    
			cell.setAttribute("align","left");
			cell.innerHTML = '';
			if(child_nodes.length>0){				
				cell.setAttribute('class',"details-control");
				row.setAttribute('data-key-child',child_nodes)
			}

			
			cell=row.insertCell(-1);    
			cell.setAttribute("align","left");
			cell.innerHTML=node_label_dict[node_list[i]]


			for(period_index = 0; period_index<period_score_dict_keys.length; period_index++){
				cell=row.insertCell(-1);    
				cell.setAttribute("align","center");		

				

				if(typeof period_score_dict[period_score_dict_keys[period_index]][node_list[i]]!="object" 
					&& period_score_dict[period_score_dict_keys[period_index]][node_list[i]]!=undefined){
						if((period_score_dict[period_score_dict_keys[period_index]][node_list[i]].toLowerCase()==='yes' && node_effect[node_list[i]]==='direct')
						|| (period_score_dict[period_score_dict_keys[period_index]][node_list[i]].toLowerCase()==='no' && node_effect[node_list[i]]==='inverse')){
							cell.innerHTML = '<span class ="badge badge-Yes">Yes</span>'
						}
					else{
						cell.innerHTML = '<span class ="badge badge-No">No</span>'
					}

				}
				else if(typeof period_score_dict[period_score_dict_keys[period_index]][node_list[i]]==="object"){
					if((period_score_dict[period_score_dict_keys[period_index]][node_list[i]][0]>rationale_threshold_dict[node_list[i]] && node_effect[node_list[i]]==='direct')
					|| (period_score_dict[period_score_dict_keys[period_index]][node_list[i]][0]<rationale_threshold_dict[node_list[i]] && node_effect[node_list[i]]==='inverse')){
						cell.innerHTML = '<span class ="badge badge-Yes">Yes</span>'
					}
					else{
						cell.innerHTML = '<span class ="badge badge-No">No</span>'
					}
				}
				else{
					cell.innerHTML = ''
				}

			}
		}
		else{
			detailed_history_table_child_dict[node_list[i]]= []
			detailed_history_table_child_dict[node_list[i]].push(node_label_dict[node_list[i]])


			for(period_index = 0; period_index<period_score_dict_keys.length; period_index++){

				if(typeof period_score_dict[period_score_dict_keys[period_index]][node_list[i]]!="object" 
					&& period_score_dict[period_score_dict_keys[period_index]][node_list[i]]!=undefined){
						if((period_score_dict[period_score_dict_keys[period_index]][node_list[i]].toLowerCase()==='yes' && node_effect[node_list[i]]==='direct')
						|| (period_score_dict[period_score_dict_keys[period_index]][node_list[i]].toLowerCase()==='no' && node_effect[node_list[i]]==='inverse')){
							detailed_history_table_child_dict[node_list[i]].push('Yes')
						}
					else{
						detailed_history_table_child_dict[node_list[i]].push('No')
					}

				}
				else if(typeof period_score_dict[period_score_dict_keys[period_index]][node_list[i]]==="object"){
					if((period_score_dict[period_score_dict_keys[period_index]][node_list[i]][0]>rationale_threshold_dict[node_list[i]] && node_effect[node_list[i]]==='direct')
					|| (period_score_dict[period_score_dict_keys[period_index]][node_list[i]][0]<rationale_threshold_dict[node_list[i]] && node_effect[node_list[i]]==='inverse')){
						detailed_history_table_child_dict[node_list[i]].push('Yes')						
					}
					else{
						detailed_history_table_child_dict[node_list[i]].push('No')
					}
				}
				else{
					detailed_history_table_child_dict[node_list[i]].push(' ')
				}

			}
		}

	}
	
	footer = table.createTFoot();
	row = footer.insertRow();

	if(row_count%2 === 0){
		row.setAttribute('class','odd');	
	}
	else{
		row.setAttribute('class','even');	
	}
	
	cell=row.insertCell(-1);    
	cell.setAttribute("align","left");
	cell.innerHTML=''

	cell=row.insertCell(-1);    
	cell.setAttribute("align","left");
	cell.innerHTML='DSS Score (Confidene in <br>Potential signal)'
	
	for(period_index = 0; period_index<period_score_dict_keys.length; period_index++){
		cell=row.insertCell(-1);    
		cell.setAttribute("align","center");			
		cell.innerHTML = '<b>'+period_score_dict[period_score_dict_keys[period_index]]['pec_importance'][0]+'%</b>'

		if(parseFloat(period_score_dict[period_score_dict_keys[period_index]]['pec_importance'][0])>dss_threshold_dict['red']){
			cell.style.color = '#efa376';
		}
		else if(parseFloat(period_score_dict[period_score_dict_keys[period_index]]['pec_importance'][0])>dss_threshold_dict['yellow']){
			cell.style.color = '#ffd300';	
		}
		else{
			cell.style.color = '#70c1b3';
		}
	}

    $('#review_history_tab_content').empty();
	document.getElementById('review_history_tab_content').appendChild(table);
	
}