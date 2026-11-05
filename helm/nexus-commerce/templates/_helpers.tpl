{{/*
Expand the name of the chart.
*/}}
{{- define "nexus-commerce.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "nexus-commerce.labels" -}}
helm.sh/chart: {{ include "nexus-commerce.name" . }}
app.kubernetes.io/name: {{ include "nexus-commerce.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end }}
