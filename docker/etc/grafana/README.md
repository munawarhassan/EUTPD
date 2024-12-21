
## Simple export/import of Data Sources in Grafana

### Export all Grafana data sources to data_sources folder

```bash
$ mkdir -p data_sources && curl -s "http://localhost:3000/api/datasources"  -u admin:admin|jq -c -M '.[]'|split -l 1 - data_sources/
```

This exports each data source to a separate JSON file in the data_sources folder.

### Load data sources back in from folder

This submits every file that exists in the data_sources folder to Grafana as a new data source definition.

```bash
for i in data_sources/*; do \
    curl -X "POST" "http://localhost:3000/api/datasources" \
    -H "Content-Type: application/json" \
     --user admin:secret \
     --data-binary @$i \
done
```

