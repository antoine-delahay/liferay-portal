AUI.add(
	'liferay-ddm-form-field-captcha',
	function(A) {
		var CaptchaField = A.Component.create(
			{
				ATTRS: {
					type: {
						value: 'captcha'
					},
					_formGroupNode: {
						value: null
					}
				},

				EXTENDS: Liferay.DDM.Renderer.Field,

				NAME: 'liferay-ddm-form-field-captcha',

				prototype: {
					initializer: function() {
						var instance = this;

						instance._eventHandlers.push(
							instance.bindContainerEvent('click', instance._onClickRefresh, '.icon-refresh')
						);
					},

					getTemplate: function() {
						var instance = this;

						var container = instance.fetchContainer();

						var _formGroupNode = instance.get('_formGroupNode');

						if (!_formGroupNode) {
							instance.set('_formGroupNode', container.one('.form-group').clone());
						}

						_formGroupNode = instance.get('_formGroupNode');

						var fieldName = _formGroupNode.attr('data-fieldname');

						return '<div class="form-group" data-fieldname="' + fieldName + '"></div>';
					},

					getTemplateRenderer: function() {
						var instance = this;

						return A.bind('renderTemplate', instance);
					},

					getValue: function() {
						return '';
					},

					render: function() {
						var instance = this;

						var container = instance.get('container');

						container.empty();

						var _formGroupNode = instance.get('_formGroupNode');

						_formGroupNode.appendTo(container);

						return instance;
					},

					renderTemplate: function() {
						var instance = this;

						return instance._valueContainer().html();
					},

					_onClickRefresh: function() {
						var instance = this;

						var container = instance.get('container');

						var captchaNode = container.one('.captcha');

						var baseURL = captchaNode.attr('src');

						var url = Liferay.Util.addParams('t=' + Date.now(), baseURL);

						captchaNode.attr('src', url);
					}
				}
			}
		);

		Liferay.namespace('DDM.Field').Captcha = CaptchaField;
	},
	'',
	{
		requires: ['liferay-ddm-form-renderer-field']
	}
);