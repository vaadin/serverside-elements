window.org_vaadin_elements_ElementIntegration = function() {
	var _self = this;

	var nextId = 1;
	
	var ids = {
		"0" : this.getElement(this.getParentId())
	};

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
			ids[id].remove();
			delete ids[id];
		},
		import: function(url) {
			var link = document.createElement("link");
			link.setAttribute("rel", "import");
			link.setAttribute("href", url);
			document.body.appendChild(link);
		},
		eval : function(id, script, params, callbacks) {
			callbacks.forEach(function(i) {
				var callbackId = params[i];
				params[i] = function() {
					// Convert array-like object to proper array
					var args = Array.prototype.slice.call(arguments, 0);
					_self.callback(id, callbackId, args);
				}
			});

			(new Function("e", "param", script))(ids[id], params);
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
		console.log(commands);
		commands.forEach(function(command) {
			var name = command[0];
			var params = command.slice(1);
			handlers[name].apply(null, params);
		});
	}
}