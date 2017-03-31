// csrf 
$(function () {
var token = $("meta[name='_csrf']").attr("content");
var header = $("meta[name='_csrf_header']").attr("content");
$(document).ajaxSend(function(e, xhr, options) {
	xhr.setRequestHeader(header, token);
});
});


var seed = 1;
function random() {
    var x = Math.sin(seed++) * 1000;
    return x - Math.floor(x);
}
var generateColors = [];
var generateBorderColors = [];
for(var k = 0; k < 100; k++) {
	var color = Math.floor(random()*256) + ',' + Math.floor(random()*256) + ',' + Math.floor(random()*256);
	generateColors.push('rgba(' + color + ', 0.2)');
	generateBorderColors.push('rgba(' + color + ', 1)'); 
}
function chartBar(data, id, unite){
	if(typeof $(id).get(0) != "undefined"){
    	var listLabels = [];
    	var listValeurs = [];
    	$.each(data, function(index, value) {
    		listLabels.push(index);
    		listValeurs.push(value);
        });
        var  barChartData = {
            	labels : listLabels,
            	datasets : [
            		{
            			backgroundColor: generateColors,
                        borderColor: generateBorderColors,
                        borderWidth: 1,
            			data : listValeurs
            		}
            	]
            }
     	var ctx = $(id).get(0).getContext("2d");
    	myBar = new Chart(ctx, {
    		type: 'bar',
    		data: barChartData,
    		options: {responsive : true,
    			legend: {
    				display: false
    			},
    			tooltips: {
                	mode: 'label',
                	bodyFontSize :  15,
                	titleFontSize: 16,
                	callbacks: {
                        label: function (t, e) {
                        	if( t.yLabel!=0){
	                            return  e.labels[t.index] + " : " + t.yLabel + unite;
                        	}
                        },
                        title: function (t, e) {
                        	return  e.labels[t.index];
                        }
                	}
    			},
    	        scales: {
    	        	xAxes: [{
    	                ticks: {
    	                    callback: function(value) {
    	                    	if(value.length>10){
    	                    		value = value.substr(0, 10) + "...";
    	                    	}
    	                        return value;
    	                    },
    	                }
    	            }],    	        	
    	            yAxes: [{
    	                ticks: {
    	                    beginAtZero:true
    	                }
    	            }]
    	        }
    		}
    	});
	}	
}

function chartLine(data, id, unite){
	if(typeof $(id).get(0) != "undefined"){   	
	var inlineTitles = [];
	var inlineLabels = [];
	var inlineValeurs = [];
	var inlineDatasets = [];
	var a=0;
	$.each(data, function(year, value) {
		var keyMois = Object.keys(value);
		for(i=1;i<=12;i++){
			inlineLabels.push(i);
			if(i.toString() in value){
	    		inlineValeurs.push(value[i]);
			} else {
	    		inlineValeurs.push(0);
			}			
		}
	    inlineDatasets.push({
	         label: year,
	         backgroundColor: generateColors[a],
	         borderColor: generateColors[a],
	         pointColor: generateBorderColors[a],
	         pointBorderColor: "#fff",
	         pointHoverBorderColor: "#fff",
	         pointBackgroundColor: generateColors[a],
	         data: inlineValeurs
	    });
	    a++;
	    inlineValeurs = [];
	});	     
		var dataMois = {
		    labels: ["Jan", "Fev", "Mar", "Avr", "Mai", "Juin", "Juil", "Août", "Sept", "Oct", "Nov", "Déc"],
		    datasets: inlineDatasets
		};       	
		var ctx3 =  $(id).get(0).getContext("2d");
		var myLineChart = new Chart(ctx3, {
		type: 'line',
		data: dataMois,
		options: {
			 scales:{
				 type:"category",
	                xAxes:[{
	        			ticks: {
	        				autoSkip: false
	        			}
	                }],
	                yAxes:[{
	                	type: "linear"
	                }]
	            },
	            legend: {
	                position: 'bottom'
	            },
	            tooltips: {
	            	mode: 'label'
	            },
	            hover: {
	                mode: 'dataset'
	            }
		}
	});
	}
}

$(document).ready(function() {


	$("#AddMoreLogins").on("click",function (e) {                                                                                                                                                                                           
		var inputDiv = $(' \
				<div class="input-group"> \
				<input class="respLogin autocompleteLogin form-control" name="logins" \
				value="" \
				type="text" /> <span class="input-group-addon"> <a href="#" \
				class="btn btn-xs btn-danger removeclass"><span class="glyphicon glyphicon-minus" aria-hidden="true"><!--  --></span></span></a></span>\
				</div> \
		').appendTo('#respLogins');    
		addAutocompleteLogin(inputDiv.find('input'));
		return false;
	});

	$("body").on("click","#respLogins .removeclass", function(e){
		if( $("#respLogins").find("input").length > 0 ) {
			$(this).closest('.input-group').remove();
		}
		return false;
	}) ;


	$("#AddMoreViewerLogins").on("click",function (e) {      
		var inputDiv = $(' \
				<div class="input-group"> \
				<input class="viewerLogin autocompleteLogin form-control" name="viewerLogins2Add" \
				value="" \
				type="text" /> <span class="input-group-addon"><a href="#" \
				class="btn btn-xs btn-danger removeclass"><span class="glyphicon glyphicon-minus" aria-hidden="true"><!--  --></span></a></span> \
				</div> \
		').appendTo('#viewerLogins');      
		addAutocompleteLogin(inputDiv.find('input'));
		return false;
	});

	$("body").on("click","#viewerLogins .removeclass", function(e){
		if( $("#viewerLogins").find("input").length > 0 ) {
			$(this).closest('.input-group').remove();
		}
		return false;
	}) ;
	
	function addAutocompleteLogin(selector) {
		$(selector).each(function(){       
			$(this).autocomplete({
				source: function(request, response) {
					$.ajax({
						url : searchLoginsJsonUrl,
						type: 'POST',
						dataType : 'json',
						data : {
							loginPrefix : request.term,
						},
						success : function(data) {

							response($.map(data, function(item){
								return {
									label: item,
									value: item
								};
							}));
						}
					});
				},				
				minLength: 4,
				maxLength: 8
			});
		});
	}
	
	addAutocompleteLogin('.autocompleteLogin');
	
	updateDbleMontant = function() {
		$('#dbleMontant').prop('readonly', $('#_sciencesconf_id:checkbox').is(':checked') || $('#_freeAmount_id:checkbox').is(':checked'));
	};
	
	$('#_freeAmount_id:checkbox').change(function () {     
		updateDbleMontant();
		if($(this).is(':checked')) {
			$('#_sciencesconf_id:checkbox').prop('checked', false);
		}
	});
	$('#_sciencesconf_id:checkbox').change(function () {     
		updateDbleMontant();
		if($(this).is(':checked')) {
			$('#_freeAmount_id:checkbox').prop('checked', false);
		}
	});
	updateDbleMontant();
	
	//Stats
	if(typeof statsUrl != "undefined"){
	    $.ajax({
	        url: statsUrl,
	        type: 'GET',
	        dataType : 'json',
	        success : function(data) {
				chartBar(data.montants, "#montantsEvt","€");
				chartBar(data.participants, "#participantsEvt"," transactions");
				chartBar(data.transactions, "#transactions"," transactions");
				chartBar(data.cumul, "#cumul"," €");
				chartLine(data.transactionsMonth, "#transactionsMonth"," transactions");
				chartLine(data.cumulMonth, "#cumulMonth"," €");
	        }
	    });
	}
	
	//Initialize bootstrap tooltip
	$("body").tooltip({ selector: '[data-toggle=tooltip]' });
	//footable
	var children=$('.table thead tr').children();
	$(".btnCrudTable").attr("data-type","html");
	//Event table
	$('#listEvts .table:not(:empty)').footable({
		"columns": [{},{"breakpoints": "xs"},{"breakpoints": "xs sm md"},{"breakpoints": "xs sm"},
			{"breakpoints": "xs sm md"},{"breakpoints": "xs sm md"},{}]		
	});
	$('#showEvts .table:not(:empty)').footable({
		"columns": [{"breakpoints": "xs sm"},{},{"breakpoints": "xs"},{"breakpoints": "xs"},
			{"breakpoints": "xs","type": "html"},{},{"breakpoints": "xs"}]		
	});
	$('#listFees2 .table:not(:empty)').footable({
		"columns": [{},{"breakpoints": "xs"},{},{"breakpoints": "xs"},
			{"breakpoints": "xs"},{}]		
	});
	$('#listFees .table:not(:empty)').footable({
		"columns": [{},{"breakpoints": "xs"},{"breakpoints": "xs sm"},{},
			{"breakpoints": "xs sm"},{"breakpoints": "xs sm"},{}]		
	});
	
	if($('#registerForm').length >0 ){
		$('#registerForm').bootstrapValidator({
			message: 'This value is not valid',
			feedbackIcons: {
				valid: 'glyphicon glyphicon-ok',
				invalid: 'glyphicon glyphicon-remove',
				validating: 'glyphicon glyphicon-refresh'
			},
			fields: {
				amount: {
					validators: {
						notEmpty: {
							message: pay_registration_fees_validator_notempty
						},
						regexp: {
	                        regexp: /^[0-9]+[,\.]?[0-9]*$/i,
	                        message:  pay_registration_fees_validator_amount
	                    }
					}
				},
				mail: {
					validators: {
						notEmpty: {
							message: pay_registration_fees_validator_mail_message
						},
						emailAddress: {
							message: pay_registration_fees_validator_mail_notempty
						}
					}
				},
				field1: {
					validators: {
						notEmpty: {
							message: pay_registration_fees_validator_notempty
						},
					}
				},
				field2: {
					validators: {
						notEmpty: {
							message: pay_registration_fees_validator_notempty
						},
					}
				}
			}
		});
	}
});