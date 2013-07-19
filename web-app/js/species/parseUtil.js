String.prototype.splitCSV = function(sep) {
	for (var thisCSV = this.split(sep = sep || ","), x = thisCSV.length - 1, tl; x >= 0; x--) {
		if (thisCSV[x].replace(/"\s+$/, '"').charAt(thisCSV[x].length - 1) == '"') {
			if ((tl = thisCSV[x].replace(/^\s+"/, '"')).length > 1 && tl.charAt(0) == '"') {
				thisCSV[x] = thisCSV[x].replace(/^\s*"|"\s*$/g, '').replace(/""/g, '"');
			} else if (x) {
				thisCSV.splice(x - 1, 2, [thisCSV[x - 1], thisCSV[x]].join(sep));
			} else thisCSV = thisCSV.shift().split(sep).concat(thisCSV);
		} else thisCSV[x].replace(/""/g, '"');
	} return thisCSV;
}

	
function parseData(csvFile, options) {
	var defaults = {
		tableClass: "CSVTable",
		theadClass: "",
		thClass: "",
		tbodyClass: "",
		trClass: "",
		tdClass: "",
		loadingImage: "",
		loadingText: "Loading CSV data...",
		separator: ",",
		startLine: 0
	};	
	var options = $.extend(defaults, options);
	var rowData = new Array();
	var columns = new Array();

	$.get(csvFile, function(data) {
		//console.log(rowData.length)
		var lines = data.replace('\r','').split('\n');
		var printedLines = 0;
		var headerCount = 0;
		var error = '';
		$.each(lines, function(lineCount, line) {
			if ((lineCount == options.startLine) && (typeof(options.headers) == 'undefined')) {
				var headers = line.splitCSV(options.separator);
				headerCount = headers.length;
				$.each(headers, function(headerCount, header) {
					columns.push({id:header, name: header, field: header, editor: Slick.Editors.Text, minWidth: 200});
					//console.log(columns.length)
				});
			} else if (lineCount >= options.startLine) {
				var items = line.splitCSV(options.separator);
				//console.log(items)
				if (items.length > 1) {
					printedLines++;
					if (items.length != headerCount) {
						error += 'error on line ' + lineCount + ': Item count (' + items.length + ') does not match header count (' + headerCount + ') \n';
					}
					var d = (rowData[lineCount-1] = {});
					$.each(items, function(itemCount, item) {
						var dataKey = columns[itemCount]['field']
						d[dataKey] = item;
					});
					//console.log(rowData.length)
				}
			}
		});
		if (error) {
			alert("Error: "+error);
		}
		if(options.callBack){
			options.callBack(rowData, columns);
		}
	});
}