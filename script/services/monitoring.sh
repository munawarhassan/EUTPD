

function monitoring_build() {
  log ""
}

function monitoring_up() {
  if [[ $pull -eq 1 ]]; then
    docker pull "graphiteapp/graphite-statsd:${GRAPHITE_TAG}"
    docker pull "grafana/grafana:${GRAFANA_TAG}"
  fi
}



function monitoring_down() {
  log ""
}

function monitoring_clean() {
  stack_volume_rm "monitoring"
}

function monitoring_push() {
  log ""
}
