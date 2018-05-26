var POWERstraight = 200
var POWERspin     = 100
var TIMEstraight  = 2000
var TIMEspin      = 743

var driving=true;

var scriptCommands = [];
var scriptCommandsPos = 0;
var currentCom;
var programRunning = false;

var robotVersion = 0;

$(function() {	
	
	$("#btForward").bind("click", function() {
		if(driving) {			
			robotAjax("set_drive_straight("+POWERstraight+")");
		} else {
			addScriptElement("img/forward.jpg");
		}
	});					
	$("#btSpinCCW").bind("click", function() {
		if(driving) {
			robotAjax("set_drive_spin_ccw("+POWERspin+")");
		} else {
			addScriptElement("img/spinCCW.jpg");
		}
	});					
	$("#btStop").bind("click", function() {
		if(driving) {
			robotAjax("set_drive_stop()");	
		} else {
			//nothing for now
		}
	});					
	$("#btSpinCW").bind("click", function() {
		if(driving) {
			robotAjax("set_drive_spin_cw("+POWERspin+")");
		} else {
			addScriptElement("img/spinCW.jpg");
		}
	});					
	$("#btBack").bind("click", function() {
		if(driving) {
			robotAjax("set_drive_straight(-"+POWERstraight+")");
		} else {
			addScriptElement("img/back.jpg");
		}
	});				

	$("#btProgram").bind("click", function() {
		driving = false;
		$("#program").show();
		$("#btDrive").removeClass("btn-primary");
		$("#btProgram").addClass("btn-primary");
	});					
	$("#btDrive").bind("click", function() {
		driving = true;
		$("#program").hide();
		$("#btDrive").addClass("btn-primary");
		$("#btProgram").removeClass("btn-primary");
	});

	$("#btClear").bind("click", function() {
		$("#programScript").empty();
		$("#programScript").append($("<img onclick='scriptClick(event)' class='cursor' height='75' src='img/cursor.jpg'>"));
	});					
	$("#btRunProg").bind("click", runScript);					
	$("#btStopProg").bind("click", function() {
		scriptCommandsPos = scriptCommands.length;
	});				
});

function addScriptElement(type) {
	// change cursor to NEXT
	var cur = $(".cursor");
	cur.removeClass("cursor");
	cur.attr("src","img/next.jpg");				
	// add element at cursor
	var ne = $("<img class='programElement' onclick='scriptClick(event)' height='75'>").attr("src",type);
	cur.after(ne);
	// add cursor after element
	ne.after($("<img onclick='scriptClick(event)' class='cursor' height='75' src='img/cursor.jpg'>"));
}

function robotAjax(cmd,cb) {
	$.post("/robot?command="+cmd,cb);
}

function scriptClick(e) {
	if(programRunning) return;
	var ca = $(e.currentTarget);
	var type = ca.attr("src");
	if(type==="img/next.jpg") {
		var ce = $(".cursor");
		ce.removeClass("cursor");
		ce.attr("src","img/next.jpg");
		ca.attr("src","img/cursor.jpg");
		ca.addClass("cursor");
	} else if(type!=="img/cursor.jpg") {
		var ne = ca.next();
		var bf = ca.prev();
		ca.remove();
		if(ne.hasClass("cursor")) {
			bf.remove();
		} else {
			ne.remove();
		}					
	}
}

function nextScriptCommand() {
	if(scriptCommandsPos==scriptCommands.length) {
		robotAjax("stop",function() {
			programRunning = false;
			$("#glassPane").hide();
			$("#btClear").prop("disabled",false);
			$("#btRunProg").prop("disabled",false);
			$("#btStopProg").prop("disabled",true);
			$("#btDrive").prop("disabled",false);
			$("#btProgram").prop("disabled",false);
		});
		return;
	}
	var cmd = $(scriptCommands[scriptCommandsPos]);
	++scriptCommandsPos;

	var type = cmd.attr("src");
	currentCom = cmd;
	var i = type.indexOf(".jpg");
	var orgSrc = type;
	cmd.attr("src",type.substring(0,i)+"CUR"+type.substring(i));
	var time = 0;
	if(type==="img/forward.jpg") {
		type = "set_drive_straight("+POWERstraight+")";
		time = TIMEstraight;
	} else if(type==="img/back.jpg") {
		type = "set_drive_straight(-"+POWERstraight+")";
		time = TIMEstraight;
	} else if(type==="img/spinCW.jpg") {
		type = "set_drive_spin_cw("+POWERspin+")";
		time = TIMEspin;
	} else if(type==="img/spinCCW.jpg") {
		type = "set_drive_spin_ccw("+POWERspin+")"
		time = TIMEspin[robotVersion];
	} 

	robotAjax(type,function() {
		setTimeout(function() {
			cmd.attr("src",orgSrc);
			nextScriptCommand();
		},time);
	});

}

function runScript() {
	programRunning = true;
	$("#glassPane").show();
	$("#btClear").prop("disabled",true);
	$("#btRunProg").prop("disabled",true);
	$("#btStopProg").prop("disabled",false);
	$("#btDrive").prop("disabled",true);
	$("#btProgram").prop("disabled",true);

	scriptCommands = $(".programElement");
	scriptCommandsPos = 0;
	nextScriptCommand();												
}
