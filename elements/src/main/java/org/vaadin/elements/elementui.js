window.org_vaadin_elements_ElementIntegration = function() {
	var _self = this;

	var nextId = 1;
	
	var ids = {
		"0" : this.getElement(this.getParentId())
	};
	
	var boundAttributes = {};
	
	var pendingAttributeChangeEvents = {};
	var pendingCallbacks = [];
	
	var deferFlushCallbackTimeout;
	var deferFlushCallback = function () {
		if (!deferFlushCallbackTimeout) {
			deferFlushCallbackTimeout = window.setTimeout(function() {
				deferFlushCallbackTimeout = null;

				var attributeChanges = [];
				for (var id in pendingAttributeChangeEvents) {
					if (!pendingAttributeChangeEvents.hasOwnProperty(id)) {
						continue;
					}
					var elementEvents = pendingAttributeChangeEvents[id];

					for (var event in elementEvents) {
						if (!elementEvents.hasOwnProperty(event)) {
							continue;
						}

						var attributesToUpdate = boundAttributes[id][event];
						for (var i = 0; i < attributesToUpdate.length; i++) {
							var attribute = attributesToUpdate[i];
							attributeChange = [+id, attribute, ""+ids[id][attribute]];
							attributeChanges.push(attributeChange);
						}
					}
				}
				
				_self.callback(pendingCallbacks, attributeChanges);
				pendingCallbacks = [];
				pendingAttributeChangeEvents = {};
			}, 0);
		}
	}

	var handlers = {
		createElement : function(id, tagName) {
			ids[id] = document.createElement(tagName);
			ids[id]._eid = id;
		},
		createText : function(id, text) {
			ids[id] = document.createTextNode(text);
			ids[id]._eid = id;
		},
		createData : function(id, data) {
			ids[id] = document.createTextNode(data);
		},
		setAttribute : function(id, name, value) {
			ids[id].setAttribute(name, value);
		},
		removeAttribute : function(id, name) {
			ids[id].removeAttribute(name);
		},
		appendChild : function(parentId, childId) {
			ids[parentId].appendChild(ids[childId]);
		},
		remove : function(id) {
			if (ids[id].parentNode) {
				ids[id].parentNode.removeChild(ids[id]);
			}
			delete ids[id];
		},
		bindAttribute: function(id, attribute, event) {
			var elementBindings = boundAttributes[id];
			if (elementBindings === undefined) {
				elementBindings = {};
				boundAttributes[id] = elementBindings;
			}
			
			var eventBindings = elementBindings[event];
			if (eventBindings === undefined) {
				eventBindings = [];
				elementBindings[event] = eventBindings;
				
				ids[id].addEventListener(event, function() {
					var pendingEvents = pendingAttributeChangeEvents[id];
					if (pendingEvents === undefined) {
						pendingEvents = {};
						pendingAttributeChangeEvents[id] = pendingEvents;
					}
					pendingEvents[event] = true;

					deferFlushCallback();
				});
			}
			
			eventBindings.push(attribute);
		},
		eval : function(id, script, params, callbacks) {
			var newFunctionParams = ["e"];
			var functionParams = [ids[id]];
			
			for(var i = 0; i < callbacks.length; i++) {
				var paramId = callbacks[i];
				var callbackId = params[paramId];
				params[paramId] = function() {
					// Convert array-like object to proper array
					var args = Array.prototype.slice.call(arguments, 0);
					pendingCallbacks.push([id, callbackId, args]);
					deferFlushCallback();
				}
			}
			
			for(var i = 0; i < params.length; i++) {
				newFunctionParams.push("$" + i);
				functionParams.push(params[i]);
			}
			
			newFunctionParams.push(script);
			
			Function.apply(Function, newFunctionParams).apply(null, functionParams);
		},
		fetchDom : function(id, includePids) {
			var root = ids["0"];
			var rootTkPid = root.tkPid;
			
			var encodeNode = function(node) {
				var id = node._eid;
				if (id === undefined) {
					id = nextId + "";
					ids[id] = node;
					node._eid = id;
					nextId += 2;
				}
				
				switch (node.nodeType) {
					case 1:
						var result = [node.tagName.toLowerCase(), +id];
						var attributes = node.attributes;
						if (attributes.length) {
							var attrs = {};
							for(var i = 0; i < attributes.length; i++) {
								var attr = attributes.item(i);
								attrs[attr.name] = attr.value;
							}
							result.push(attrs);
						}
						
						if (!node.tkPid || node.tkPid === rootTkPid || includePids.indexOf(node.tkPid) != -1) {
							for(var i = 0; i < node.childNodes.length; i++) {
								result.push(encodeNode(node.childNodes[i]));
							}
						}
						
						return result;
					case 3:
						return [+id, node.textContent];
					default:
						console.error("Unsupported node type: ", node);
						throw node;
				}
			}
			
			var encodedRoot = encodeNode(root);

			_self.getDom(id, encodedRoot);
		}
	}

	this.run = function(commands) {
//		console.log(commands);
		commands.forEach(function(command) {
			var name = command[0];
			var params = command.slice(1);
			handlers[name].apply(null, params);
		});
	}
}