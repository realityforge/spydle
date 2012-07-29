## v1.2:

* Stop modelling the name of metrics in a LDAP-esque fashion and instead used a simple dotted string. The change was
  made to make it more compatible with existing data stores such as Graphite and OpenTSDB.

## v1.1.1:

* Remove duplicate attribute names in graphite service.

## v1.1:

* Move the probe specific configuration below a key named "config".
* Remove characters that are not a '_', a character or a letter from the generated keys in graphite service.

## v1.0:

* Initial release