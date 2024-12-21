# i18n Properties Naming Convention

## Conventions

* General structure, where each module corresponds to its Maven project, and sections are semantically grouped together.

	`app.[module].[section].{more sections if required}.[key]`

* Keys should be all in lowercase; non-alphanumeric, non-period characters are discouraged. Properties are handled as ISO 8859-1 encoded by default; non-conformant characters are forbidden. (If you need to insert a Unicode character into a properties file as a value, use \u0000 notation, though Unicode characters must not be used in property keys.)	

	`app.web.about.copyright=Copyright \u00A9 {0} Devacfr.`

* Singular tags are preferred over plural. (Exceptions: situations where the intent is clearer with the plural, e.g. settings)

	`app.web.admin.commit.*`
	
	instead of

	`app.web.admin.commits.*`

* Tags and semantic groups with multiple words should generally be concatenated without separating characters or camel casing.

	`app.web.admin.service.*`

	instead of

	`app.web.admin.service.*, app.web.admin.notifaction-request.*, app.web.admin.notifaction.request.*`

* Common keys should be specified in a "global" group at the level of re-use.

	A key commonly used across the web module:

	`app.web.global.cancel`

	A key commonly used across the entire project:

	`app.global.appname`

* Verbs should typically go at the end of a key. (Exception: if there are no other similar keys and it makes sense to concatenate, do so; e.g. `app.web.service.createemail` if there were no other keys in `app.web.service.email.*`.)

	`app.web.service.email.create`

	instead of

	`app.web.service.create.email`

* If there are multiple related keys that justify creating a new group, create that group.
	
	Refactor:

	`app.web.service.createemail, app.web.service.sendemail`

	into:

	`app.web.service.email.create, app.web.service.email.send`

* Key sections should progress from broader scope to narrower scope.

	`app.web.settings.admin.database.hostname`

	instead of

	`app.web.admin.database.settings.hostname`
